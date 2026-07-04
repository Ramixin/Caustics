package net.ramixin.caustics.nodes.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.items.components.Frequency;
import net.ramixin.caustics.nodes.Node;
import net.ramixin.caustics.nodes.Network;
import net.ramixin.caustics.nodes.routing.NodeMappedRoute;
import net.ramixin.caustics.nodes.routing.Route;
import net.ramixin.caustics.nodes.steppers.NodeBuilder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class CrystalNetwork extends SavedData implements Network {

    private static final Codec<CrystalNetwork> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NodeWorker.CODEC.fieldOf("worker").forGetter(CrystalNetwork::getWorker),
            FrequencyRegistry.CODEC.fieldOf("registry").forGetter(CrystalNetwork::getRegistry)
    ).apply(instance, CrystalNetwork::new));

    @SuppressWarnings("DataFlowIssue") // doesn't want null datafixer
    private static final SavedDataType<CrystalNetwork> TYPE = new SavedDataType<>(
            Caustics.id("crystal_network"),
            CrystalNetwork::new,
            CODEC,
            null
    );

    private final NodeIndex index;
    private final NodeWorker worker;
    private final FrequencyRegistry registry;
    private final RoutingManager manager = new RoutingManager();
    private final NetworkSynchronizer synchronizer = new NetworkSynchronizer();
    private final LeaptionHandler handler = new LeaptionHandler();

    private final Tracker tracker = new Tracker();

    private CrystalNetwork() {
        this.worker = new NodeWorker();
        this.index = new NodeIndex();
        this.registry = new FrequencyRegistry();
    }

    private CrystalNetwork(NodeWorker worker, FrequencyRegistry registry) {
        this.worker = worker;
        this.index = new NodeIndex(worker);
        this.registry = registry;
        tracker.lazyPush(Tracker.Task.REBUILD_ROUTING);
    }

    public static CrystalNetwork get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void tick(ServerLevel level) {
        tracker.begin();

        worker.tick(level, this);
        registry.tick(this);
        manager.tick(level, this);
        handler.tick(level, this);
        synchronizer.tick(level, this);

        if(tracker.consume(Tracker.Task.DIRTY))
            setDirty();
    }

    protected NodeIndex getIndex() {
        return index;
    }

    protected NodeWorker getWorker() {
        return worker;
    }

    protected Tracker getTracker() {
        return tracker;
    }

    public FrequencyRegistry getRegistry() {
        return registry;
    }

    protected RoutingManager getManager() {
        return manager;
    }

    public void generateNodeAt(BlockPos pos) {
        this.worker.generateNodeAt(pos);
        setDirty();
    }

    public Optional<Node> getNodeForBuilder(NodeBuilder builder) {
        return this.worker.getNodeForBuilder(builder);
    }

    public Set<Frequency> getNetworks(BlockPos pos) {
        Optional<Node> maybeNode = index.getNodeAt(pos);
        if(maybeNode.isEmpty()) return Set.of();
        Node node = maybeNode.get();
        Set<BlockPos> sunstones = node.data().sunstoneClusters();

        Set<Frequency> networks = new HashSet<>();
        for(BlockPos sunstone : sunstones) {
            Optional<Frequency> maybeFreq = registry.getFrequencyAt(sunstone);
            if(maybeFreq.isEmpty()) continue;
            networks.add(maybeFreq.get());
        }
        return networks;
    }

    public boolean isSeleniteVisible(BlockPos pos) {
        return worker.isSeleniteVisible(pos);
    }

    public int getSeleniteLightLevel(BlockPos pos) {
        return worker.getSeleniteLightLevel(pos);
    }

    public void joinSync(ServerPlayer player) {
        this.synchronizer.joinSync(player, this);
    }

    public void resync(ServerPlayer player) {
        this.synchronizer.resync(player, this);
    }

    public void startSyncing(UUID uuid) {
        this.synchronizer.addRealtime(uuid);
    }

    public void stopSyncing(UUID uuid) {
        this.synchronizer.removeRealtime(uuid);
    }

    public void nuke() {
        index.clear();
        worker.clear();
        registry.clear();
        manager.clear();
        handler.clear();
        setDirty();
    }

    public void printNodes() {
        worker.printNodes();
    }

    public void printRouting() {
        Caustics.LOGGER.info(manager.toString());
    }

    @Override
    public Optional<String> getFrequencyName(Frequency frequency) {
        return this.registry.getFrequencyName(frequency);
    }

    public void setFrequencyName(Frequency frequency, String freqStr) {
        registry.register(frequency, freqStr);
    }

    public Optional<Node> getNodeAt(BlockPos pos) {
        return index.getNodeAt(pos);
    }

    public Optional<Node> getNodeAt(BlockPos pos, NodeIndex.Type type) {
        return index.getNodeAt(pos, type);
    }

    public void requestLeaption(ServerPlayer player, Route route, BlockPos peridotPos) {
        BlockPos sapphirePos = route.sapphirePos();
        Optional<Node> maybeNode = this.index.getNodeAt(sapphirePos, NodeIndex.Type.SAPPHIRE);
        if(maybeNode.isEmpty()) return;
        Node node = maybeNode.get();
        Set<BlockPos> peridots = node.data().peridotClusters();
        if(!peridots.contains(peridotPos)) return;
        Optional<NodeMappedRoute> maybeNodeMapped = route.nodeMapped(this);
        if(maybeNodeMapped.isEmpty()) return;
        handler.startLeap(player.getUUID(), node, maybeNodeMapped.get(), sapphirePos, peridotPos);
    }

    public Optional<BlockPos> getLeapPos(UUID uuid) {
        return handler.getLeapPos(uuid);
    }

    public void markLeapCompleted(UUID uuid) {
        handler.markLeapCompleted(uuid);
    }
}