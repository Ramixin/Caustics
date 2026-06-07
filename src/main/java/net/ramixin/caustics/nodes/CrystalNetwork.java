package net.ramixin.caustics.nodes;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.nodes.builders.NodeBuilder;

import java.util.*;

public class CrystalNetwork extends SavedData {

    private static final Codec<CrystalNetwork> CODEC = Codec.pair(CrystalNode.CODEC.listOf(), BlockPos.CODEC.listOf().listOf()).xmap(
            listListPair -> new CrystalNetwork(listListPair.getFirst(), listListPair.getSecond()),
            crystalNetwork -> Pair.of(List.copyOf(crystalNetwork.nodes), List.copyOf(crystalNetwork.builders.values()))
    );
    @SuppressWarnings("DataFlowIssue") // doesn't want null datafixer
    private static final SavedDataType<CrystalNetwork> TYPE = new SavedDataType<>(
            Caustics.id("crystal_network"),
            CrystalNetwork::new,
            CODEC,
            null
    );

    private final HashMap<NodeBuilder, List<BlockPos>> builders = new HashMap<>();
    private final HashMap<BlockPos, CrystalNode> sapphireToNode = new HashMap<>();
    private final HashSet<CrystalNode> nodes = new HashSet<>();

    private CrystalNetwork() {}

    private CrystalNetwork(List<CrystalNode> nodes, List<List<BlockPos>> builders) {
        int delay = 0;
        for(CrystalNode node : nodes) {
            for(BlockPos pos : node.blocks().sapphireClusters()) {
                sapphireToNode.put(pos, node);
            }
            node.builder().pause(delay++);
            this.nodes.add(node);
        }
        for(List<BlockPos> positions : builders) {
            NodeBuilder builder = new NodeBuilder(positions);
            builder.pause(delay++);
            this.builders.put(builder, positions);
        }
    }


    public static CrystalNetwork get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean hasNodeAt(BlockPos pos) {
        return sapphireToNode.containsKey(pos);
    }

    public Optional<CrystalNode> getNodeAt(BlockPos pos) {
        return Optional.ofNullable(sapphireToNode.get(pos));
    }

    public void tick(ServerLevel level) {
        for (CrystalNode node : Set.copyOf(nodes)) {
            node.tick(level);
            if(node.builder().isBuilding()) continue;
            unregisterNode(node);
            setDirty();

            Optional<CrystalNode> maybeNewNode = node.builder().build(level);
            if(maybeNewNode.isEmpty()) continue;
            CrystalNode newNode = maybeNewNode.get();
            if(!newNode.builder().isBuilding()) {
                System.out.println("node has no more sapphire clusters");
                continue;
            }
            registerNode(newNode);

        }

        for (NodeBuilder builder : List.copyOf(builders.keySet())) {
            builder.tick(level);
            if(builder.isBuilding()) continue;
            builders.remove(builder);
            Optional<CrystalNode> maybeNode = builder.build(level);
            if(maybeNode.isEmpty()) continue;
            CrystalNode newNode = maybeNode.get();

            if(!newNode.builder().isBuilding()) continue;
            registerNode(newNode);
            setDirty();
        }
    }

    private void registerNode(CrystalNode node) {
        nodes.add(node);
        for(BlockPos pos : node.blocks().sapphireClusters()) {
            sapphireToNode.put(pos, node);
        }
    }

    private void unregisterNode(CrystalNode node) {
        nodes.remove(node);
        for(BlockPos pos : node.blocks().sapphireClusters()) {
            sapphireToNode.remove(pos);
        }
    }

    public void generateNodeAt(BlockPos pos) {
        NodeBuilder nodeBuilder = new NodeBuilder(pos);
        List<BlockPos> positions = new ArrayList<>();
        positions.add(pos);
        builders.put(nodeBuilder, positions);
        setDirty();
    }

    public void addBuilder(NodeBuilder builder, List<BlockPos> positions) {
        builders.put(builder, positions);
    }

    public void printNodes() {
        for(CrystalNode node : nodes) {
            System.out.println(node);
        }
        if(nodes.isEmpty()) System.out.println("no nodes");
    }

    public void nuke() {
        this.nodes.clear();
        this.builders.clear();
        this.sapphireToNode.clear();
        setDirty();
    }
}
