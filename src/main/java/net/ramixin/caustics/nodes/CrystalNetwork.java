package net.ramixin.caustics.nodes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.networking.clientbound.NetworkSyncPayload;
import net.ramixin.caustics.nodes.steppers.NodeBuilder;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;
import java.util.function.Function;

public class CrystalNetwork extends SavedData {

    private static final Codec<CrystalNetwork> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CrystalNode.CODEC.listOf().fieldOf("nodes").forGetter(CrystalNetwork::getNodes),
            BlockPos.CODEC.listOf().listOf().fieldOf("builders").forGetter(CrystalNetwork::getBuildersAsList)
    ).apply(instance, CrystalNetwork::new));
    @SuppressWarnings("DataFlowIssue") // doesn't want null datafixer
    private static final SavedDataType<CrystalNetwork> TYPE = new SavedDataType<>(
            Caustics.id("crystal_network"),
            CrystalNetwork::new,
            CODEC,
            null
    );

    private final Map<NodeBuilder, CrystalNode> rebuildBuilders = new HashMap<>();
    private final Map<NodeBuilder, List<BlockPos>> newBuilders = new HashMap<>();

    private final Map<BlockPos, CrystalNode> sapphireToNode = new HashMap<>();
    private final Map<BlockPos, CrystalNode> sunstoneToNode = new HashMap<>();
    private final Map<BlockPos, CrystalNode> topazToNode = new HashMap<>();
    private final Map<BlockPos, CrystalNode> tourmalineToNode = new HashMap<>();
    private final Map<BlockPos, CrystalNode> peridotToNode = new HashMap<>();

    private final List<CrystalNode> nodes = new ArrayList<>();
    private final List<CrystalNode> routerNodes = new ArrayList<>();
    private final List<CrystalNode> jammerNodes = new ArrayList<>();

    private final Mutable<Function<UUID, Optional<ServerPlayer>>> playerGetter = new MutableObject<>();
    private final Set<UUID> syncingPlayers = new HashSet<>();

    private CrystalNetwork() {}

    private CrystalNetwork(List<CrystalNode> nodes, List<List<BlockPos>> builders) {
        int delay = 0;
        for(CrystalNode node : nodes) {
            registerNode(node);
            NodeBuilder builder = new NodeBuilder(node.data().sapphireClusters());
            builder.pause(delay++);
            rebuildBuilders.put(builder, node);
        }
        for(List<BlockPos> positions : builders) {
            NodeBuilder builder = new NodeBuilder(positions);
            builder.pause(delay++);
            newBuilders.put(builder, positions);
        }
    }

    public static CrystalNetwork get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void tick(ServerLevel level) {
        if(playerGetter.get() == null)
            playerGetter.setValue(playerGetter(level));
        boolean syncDirty = false;
        for(CrystalNode node : nodes) {
            node.tick(level);
            if(node.consumeSyncingDirty())
                syncDirty = true;
        }
        tickRebuildBuilders(level);
        tickNewBuilders(level);

        if(syncDirty)
            syncAll();
    }

    private Function<UUID, Optional<ServerPlayer>> playerGetter(ServerLevel level) {
        return (uuid) -> {
            Player player = level.getPlayerByUUID(uuid);
            if(!(player instanceof ServerPlayer serverPlayer)) {
                syncingPlayers.remove(uuid);
                return Optional.empty();
            }
            return Optional.of(serverPlayer);
        };
    }

    private void tickRebuildBuilders(ServerLevel level) {
        for(NodeBuilder builder : List.copyOf(rebuildBuilders.keySet())) {
            builder.tick(level);
            if(builder.isBuilding()) continue;

            CrystalNode oldNode = rebuildBuilders.get(builder);
            Optional<NodeData> maybeData = builder.build(level);
            rebuildBuilders.remove(builder);

            if(maybeData.isEmpty()) {
                unregisterNode(oldNode);
                setDirty();
                return;
            }

            NodeData newData = maybeData.get();
            if(!newData.equals(oldNode.data())) {
                Caustics.LOGGER.debug("Rebuilding node with data: {}", newData);
                unregisterNode(oldNode);
                CrystalNode newNode = oldNode.withData(newData);
                registerNode(newNode);
                scheduleRebuild(newNode);
                setDirty();
            } else
                scheduleRebuild(oldNode);
        }
    }

    private void tickNewBuilders(ServerLevel level) {
        for(NodeBuilder builder : List.copyOf(newBuilders.keySet())) {
            builder.tick(level);
            if(builder.isBuilding()) continue;

            builder.build(level).ifPresent(data -> {
                CrystalNode newNode = new CrystalNode(data);
                registerNode(newNode);
                scheduleRebuild(newNode);
                setDirty();
            });
            newBuilders.remove(builder);
        }
    }

    private void scheduleRebuild(CrystalNode node) {
        NodeBuilder builder = new NodeBuilder(node.data().sapphireClusters());
        builder.pause(20);
        rebuildBuilders.put(builder, node);
    }

    private void registerNode(CrystalNode node) {
        nodes.add(node);

        if(!node.data().topazClusters().isEmpty()) {
            for(BlockPos pos : node.data().topazClusters()) topazToNode.put(pos, node);
            routerNodes.add(node);
        }
        if(!node.data().tourmalineClusters().isEmpty()) {
            for(BlockPos pos : node.data().tourmalineClusters()) tourmalineToNode.put(pos, node);
            jammerNodes.add(node);
        }

        for(BlockPos pos : node.data().sapphireClusters()) sapphireToNode.put(pos, node);
        for(BlockPos pos : node.data().sunstoneClusters()) sunstoneToNode.put(pos, node);
        for(BlockPos pos : node.data().peridotClusters()) peridotToNode.put(pos, node);
    }

    private void unregisterNode(CrystalNode node) {
        nodes.remove(node);
        routerNodes.remove(node);
        jammerNodes.remove(node);
        node.data().sapphireClusters().forEach(sapphireToNode::remove);
        node.data().sunstoneClusters().forEach(sunstoneToNode::remove);
        node.data().tourmalineClusters().forEach(tourmalineToNode::remove);
        node.data().topazClusters().forEach(topazToNode::remove);
        node.data().peridotClusters().forEach(peridotToNode::remove);
    }

    public void generateNodeAt(BlockPos pos) {
        NodeBuilder builder = new NodeBuilder(pos);
        newBuilders.put(builder, List.of(pos));
        setDirty();
    }

    public void addBuilder(NodeBuilder builder, List<BlockPos> positions) {
        newBuilders.put(builder, positions);
    }

    public void printNodes() {
        if(nodes.isEmpty()) System.out.println("no nodes");
        else nodes.forEach(System.out::println);
    }

    public void nuke() {
        nodes.clear();
        rebuildBuilders.clear();
        newBuilders.clear();
        sapphireToNode.clear();
        sunstoneToNode.clear();
        setDirty();
    }

    private List<List<BlockPos>> getBuildersAsList() {
        return List.copyOf(newBuilders.values());
    }

    private List<CrystalNode> getNodes() {
        return nodes;
    }

    public Optional<CrystalNode> getNodeAt(BlockPos pos) {
        CrystalNode sapphireNode = sapphireToNode.get(pos);
        if(sapphireNode != null) return Optional.of(sapphireNode);
        CrystalNode topazNode = topazToNode.get(pos);
        if(topazNode != null) return Optional.of(topazNode);
        CrystalNode tourmalineNode = tourmalineToNode.get(pos);
        if(tourmalineNode != null) return Optional.of(tourmalineNode);
        CrystalNode sunstoneNode = sunstoneToNode.get(pos);
        if(sunstoneNode != null) return Optional.of(sunstoneNode);
        CrystalNode peridotNode = peridotToNode.get(pos);
        if(peridotNode != null) return Optional.of(peridotNode);
        return Optional.empty();
    }

    public Optional<CrystalNode> getNodeForBuilder(NodeBuilder builder) {
        return Optional.ofNullable(rebuildBuilders.get(builder));
    }

    public void syncAll() {
        if(syncingPlayers.isEmpty()) return;
        NetworkSyncPayload payload = getSyncPayload();
        for(UUID uuid : syncingPlayers)
            updateSyncer(uuid, payload);
    }

    @Override
    public void setDirty() {
        syncAll();
        super.setDirty();
    }

    public void startSyncing(UUID uuid) {
        NetworkSyncPayload payload = getSyncPayload();
        updateSyncer(uuid, payload);
        syncingPlayers.add(uuid);
    }

    private void updateSyncer(UUID uuid, NetworkSyncPayload payload) {
        Optional<ServerPlayer> player = playerGetter.get().apply(uuid);
        if(player.isEmpty()) return;
        ServerPlayNetworking.send(player.get(), payload);
    }

    private NetworkSyncPayload getSyncPayload() {
        List<NodeSyncData> data = new ArrayList<>();
        for(CrystalNode node : nodes) {
            Optional<NodeSyncData> maybeData = node.createSyncData();
            maybeData.ifPresent(data::add);
        }
        return new NetworkSyncPayload(data);
    }

    public void stopSyncing(UUID uuid) {
        syncingPlayers.remove(uuid);
    }
}