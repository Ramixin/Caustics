package net.ramixin.caustics.nodes.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.networking.clientbound.NodeSyncPayload;
import net.ramixin.caustics.nodes.CrystalNode;
import net.ramixin.caustics.nodes.NodeData;
import net.ramixin.caustics.nodes.NodeSyncData;
import net.ramixin.caustics.nodes.steppers.NodeBuilder;

import java.util.*;

public class NodeWorker {

    protected static final Codec<NodeWorker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CrystalNode.CODEC.listOf().fieldOf("nodes").forGetter(NodeWorker::getNodes),
            BlockPos.CODEC.listOf().listOf().fieldOf("builders").forGetter(NodeWorker::getBuildersAsList)
    ).apply(instance, NodeWorker::new));

    private final List<CrystalNode> nodes = new ArrayList<>();
    private final Map<NodeBuilder, CrystalNode> rebuildBuilders = new HashMap<>();
    private final Map<NodeBuilder, List<BlockPos>> newBuilders = new HashMap<>();

    private NodeWorker(List<CrystalNode> nodes, List<List<BlockPos>> builders) {
        int delay = 0;
        for(CrystalNode node : nodes) {
            nodes.add(node);
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

    protected NodeWorker() { }

    protected void tick(ServerLevel level, CrystalNetwork network) {
        Tracker tracker = network.getTracker();
        tickNodes(level, tracker);
        tickRebuildBuilders(level, network);
        tickNewBuilders(level, network);
    }

    private void tickNodes(ServerLevel level, Tracker tracker) {
        for(CrystalNode node : nodes) {
            node.tick(level, tracker);
        }
    }

    private void tickRebuildBuilders(ServerLevel level, CrystalNetwork network) {
        for (NodeBuilder builder : List.copyOf(rebuildBuilders.keySet())) {
            builder.tick(level);
            if (builder.isBuilding()) continue;

            CrystalNode oldNode = rebuildBuilders.get(builder);
            Optional<NodeData> maybeData = builder.build(level, this);
            rebuildBuilders.remove(builder);

            if (maybeData.isEmpty()) {
                unregister(oldNode, network);
                return;
            }

            NodeData newData = maybeData.get();
            if (!newData.equals(oldNode.data())) {
                unregister(oldNode, network);
                CrystalNode newNode = oldNode.withData(newData);
                register(newNode, network);
                scheduleRebuild(newNode);
            } else
                scheduleRebuild(oldNode);
        }
    }

    private void tickNewBuilders(ServerLevel level, CrystalNetwork network) {
        for(NodeBuilder builder : List.copyOf(newBuilders.keySet())) {
            builder.tick(level);
            if(builder.isBuilding()) continue;

            builder.build(level, this).ifPresent(data -> {
                CrystalNode newNode = new CrystalNode(data);
                register(newNode, network);
                scheduleRebuild(newNode);
            });
            newBuilders.remove(builder);
        }
    }

    private void register(CrystalNode node, CrystalNetwork network) {
        nodes.add(node);
        network.getIndex().indexNode(node);
        network.getTracker().push(Tracker.Item.NODE_SYNC, Tracker.Item.REBUILD_ROUTING, Tracker.Item.DIRTY);
    }

    private void unregister(CrystalNode node, CrystalNetwork network) {
        nodes.remove(node);
        network.getIndex().unindexNode(node);
        network.getTracker().push(Tracker.Item.NODE_SYNC, Tracker.Item.REBUILD_ROUTING, Tracker.Item.DIRTY);
    }

    private void scheduleRebuild(CrystalNode node) {
        Set<BlockPos> positions = new HashSet<>(node.data().sapphireClusters());
        positions.addAll(node.data().topazClusters());
        positions.addAll(node.data().tourmalineClusters());
        NodeBuilder builder = new NodeBuilder(positions);
        builder.pause(20);
        rebuildBuilders.put(builder, node);
    }

    protected void generateNodeAt(BlockPos pos) {
        NodeBuilder builder = new NodeBuilder(pos);
        newBuilders.put(builder, List.of(pos));
    }

    public void addNewBuilder(NodeBuilder nodeBuilder, List<BlockPos> potentialStarts) {
        newBuilders.put(nodeBuilder, potentialStarts);
    }

    private List<List<BlockPos>> getBuildersAsList() {
        return newBuilders.values().stream().toList();
    }

    public Optional<CrystalNode> getNodeForBuilder(NodeBuilder builder) {
        return Optional.ofNullable(rebuildBuilders.get(builder));
    }

    public List<CrystalNode> getNodes() {
        return nodes;
    }

    protected NodeSyncPayload createSyncPayload() {
        List<NodeSyncData> data = new ArrayList<>();
        for(CrystalNode node : nodes) {
            Optional<NodeSyncData> maybeData = node.createSyncData();
            maybeData.ifPresent(data::add);
        }
        return new NodeSyncPayload(data);
    }

    protected void clear() {
        rebuildBuilders.clear();
        newBuilders.clear();
    }

    protected void printNodes() {
        for(CrystalNode node : nodes) {
            Caustics.LOGGER.info(node.toString());
        }
    }

}
