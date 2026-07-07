package net.ramixin.caustics.nodes.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.blocks.ModBlocks;
import net.ramixin.caustics.networking.clientbound.NodeSyncPayload;
import net.ramixin.caustics.nodes.Node;
import net.ramixin.caustics.nodes.NodeData;
import net.ramixin.caustics.nodes.NodeSyncData;
import net.ramixin.caustics.nodes.steppers.NodeBuilder;
import net.ramixin.caustics.nodes.steppers.VisibilityChecker;

import java.util.*;

public class NodeWorker {

    protected static final Codec<NodeWorker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Node.CODEC.listOf().fieldOf("nodes").forGetter(NodeWorker::getNodes),
            BlockPos.CODEC.listOf().listOf().fieldOf("builders").forGetter(NodeWorker::getBuildersAsList)
    ).apply(instance, NodeWorker::new));

    private final List<Node> nodes = new ArrayList<>();
    private final Map<NodeBuilder, Node> rebuildBuilders = new HashMap<>();
    private final Map<NodeBuilder, List<BlockPos>> newBuilders = new HashMap<>();

    private final HashMap<BlockPos, VisibilityChecker> seleniteCheckers = new HashMap<>();

    private NodeWorker(List<Node> nodes, List<List<BlockPos>> builders) {
        int delay = 0;
        for(Node node : nodes) {
            this.nodes.add(node);
            Set<BlockPos> positions = new HashSet<>(node.data().sapphireClusters());
            positions.addAll(node.data().topazClusters());
            positions.addAll(node.data().tourmalineClusters());
            NodeBuilder builder = new NodeBuilder(positions);
            builder.pause(delay++);
            rebuildBuilders.put(builder, node);
        }
        for(List<BlockPos> positions : builders) {
            NodeBuilder builder = new NodeBuilder(positions);
            builder.pause(delay++);
            newBuilders.put(builder, positions);
        }
    }

    public boolean isSeleniteVisible(BlockPos pos) {
        return seleniteCheckers.computeIfAbsent(pos, VisibilityChecker::new).getValue();
    }

    public int getSeleniteLightLevel(BlockPos pos) {
        return seleniteCheckers.computeIfAbsent(pos, VisibilityChecker::new).skyBrightness();
    }

    protected NodeWorker() { }

    protected void tick(ServerLevel level, CrystalNetwork network) {
        Tracker tracker = network.getTracker();
        tickNodes(level, tracker);
        tickRebuildBuilders(level, network);
        tickNewBuilders(level, network);

        Set<BlockPos> scheduledRemovals = new HashSet<>();
        for(BlockPos pos : seleniteCheckers.keySet())
            if(!level.getBlockState(pos).is(ModBlocks.SELENITE_GROUP.cluster())) scheduledRemovals.add(pos);
            else seleniteCheckers.get(pos).tick(level, network.getTracker());
        seleniteCheckers.keySet().removeAll(scheduledRemovals);
    }

    private void tickNodes(ServerLevel level, Tracker tracker) {
        for(Node node : nodes) {
            node.tick(level, tracker);
        }
    }

    private void tickRebuildBuilders(ServerLevel level, CrystalNetwork network) {
        for (NodeBuilder builder : List.copyOf(rebuildBuilders.keySet())) {
            builder.tick(level);
            if (builder.isBuilding()) continue;

            Node oldNode = rebuildBuilders.get(builder);
            Optional<NodeData> maybeData = builder.build(level, this);
            rebuildBuilders.remove(builder);

            if (maybeData.isEmpty()) {
                unregister(oldNode, network);
                return;
            }

            NodeData newData = maybeData.get();
            if (!newData.equals(oldNode.data())) {
                unregister(oldNode, network);
                Node newNode = oldNode.withData(newData);
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
                Node newNode = new Node(data);
                register(newNode, network);
                scheduleRebuild(newNode);
            });
            newBuilders.remove(builder);
        }
    }

    private void register(Node node, CrystalNetwork network) {
        nodes.add(node);
        network.nodeIndex().indexNode(node);
        network.getTracker().push(Tracker.Task.NODE_SYNC, Tracker.Task.REBUILD_ROUTING, Tracker.Task.DIRTY);
    }

    private void unregister(Node node, CrystalNetwork network) {
        nodes.remove(node);
        network.nodeIndex().unindexNode(node);
        network.getTracker().push(Tracker.Task.NODE_SYNC, Tracker.Task.REBUILD_ROUTING, Tracker.Task.DIRTY);
    }

    private void scheduleRebuild(Node node) {
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

    public Optional<Node> getNodeForBuilder(NodeBuilder builder) {
        return Optional.ofNullable(rebuildBuilders.get(builder));
    }

    public List<Node> getNodes() {
        return nodes;
    }

    protected NodeSyncPayload createSyncPayload() {
        List<NodeSyncData> data = new ArrayList<>();
        for(Node node : nodes) {
            Optional<NodeSyncData> maybeData = node.createSyncData();
            maybeData.ifPresent(data::add);
        }
        return new NodeSyncPayload(data);
    }

    protected void clear() {
        rebuildBuilders.clear();
        newBuilders.clear();
    }

    public void printNodes() {
        if(nodes.isEmpty())
            Caustics.LOGGER.info("No nodes");
        else for(Node node : nodes)
            Caustics.LOGGER.info(node.toString());
    }

}
