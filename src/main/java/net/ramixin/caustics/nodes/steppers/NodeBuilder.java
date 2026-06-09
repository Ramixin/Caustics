package net.ramixin.caustics.nodes.steppers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.ModGameRules;
import net.ramixin.caustics.ModTags;
import net.ramixin.caustics.blocks.ModBlocks;
import net.ramixin.caustics.nodes.CrystalNetwork;
import net.ramixin.caustics.nodes.CrystalNode;
import net.ramixin.caustics.nodes.NodeData;

import java.util.*;

public class NodeBuilder {

    private final NodeData data = new NodeData();
    private final List<BlockPos> potentialStarts = new ArrayList<>();
    private boolean filteredStarts = false;
    private int stepsLeft = -1;
    private final Queue<BlockPos> posQueue = new LinkedList<>();
    private final Set<BlockPos> crystalBlocks = new HashSet<>();
    private final Set<BlockPos> clusterBlocks = new HashSet<>();
    private final Set<BlockPos> visitedPositions = new HashSet<>();
    private int pauseTicks = 0;
    
    public NodeBuilder(BlockPos startingPos) {
        this.potentialStarts.add(startingPos);
    }

    public NodeBuilder(List<BlockPos> startingPosList) {
        this.potentialStarts.addAll(startingPosList);
    }

    public void tick(ServerLevel level) {
        if(stepsLeft == -1) {
            stepsLeft = level.getGameRules().get(ModGameRules.MAX_STEPS);
        }

        if(pauseTicks > 0) {
            pauseTicks--;
            return;
        }

        for(int i = potentialStarts.size() - 1; i >= 0; i--) {
            BlockPos pos = potentialStarts.get(i);
            if(!level.getBlockState(pos).is(ModBlocks.SAPPHIRE_GROUP.cluster()))
                potentialStarts.remove(i);
        }
        filteredStarts = true;
        if(potentialStarts.isEmpty()) return;
        BlockPos first = potentialStarts.getFirst();
        clusterBlocks.add(first);
        posQueue.add(first.relative(level.getBlockState(first).getValue(AmethystClusterBlock.FACING).getOpposite()));


        int stepsPerTick = level.getGameRules().get(ModGameRules.STEPS_PER_TICK);
        for(int i = 0; i < stepsPerTick; i++) {
            if(stepsLeft == 0) {
                Caustics.LOGGER.error("Node builder ran out of steps");
                return;
            }

            if(posQueue.isEmpty()) return;
            BlockPos pos = posQueue.poll();
            if(visitedPositions.contains(pos)) continue;
            stepsLeft--;
            visitedPositions.add(pos);
            step(level, pos);
        }
    }

    public void pause(int ticks) {
        pauseTicks = ticks;
    }

    public void step(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if(state.is(ModTags.Blocks.CLUSTER))
            clusterBlocks.add(pos);
        else if(state.is(ModTags.Blocks.CRYSTAL)) {
            crystalBlocks.add(pos);
            posQueue.add(pos.above());
            posQueue.add(pos.below());
            posQueue.add(pos.north());
            posQueue.add(pos.south());
            posQueue.add(pos.east());
            posQueue.add(pos.west());
        }
    }

    public boolean isBuilding() {
        return (!posQueue.isEmpty() || !filteredStarts) && (stepsLeft > 0 || stepsLeft == -1);
    }

    public Optional<CrystalNode> build(ServerLevel level, Optional<CrystalNode> maybeOldNode) {

        boolean overlapping = populateClusters(level);
        if(overlapping) return Optional.empty();
        if(data.sapphireClusters().isEmpty()) return Optional.empty();

        for(BlockPos pos : data.sapphireClusters()) {
            potentialStarts.remove(pos);
        }

        if(!potentialStarts.isEmpty()) {
            CrystalNetwork network = CrystalNetwork.get(level);
            network.addBuilder(new NodeBuilder(potentialStarts), potentialStarts);
        }

        NodeBuilder nodeBuilder = new NodeBuilder(data.sapphireClusters());
        nodeBuilder.pause(20);

        CrystalNode node;
        node = maybeOldNode.map(oldNode -> new CrystalNode(data.toImmutable(), oldNode.networks(), nodeBuilder, oldNode.checkers())).orElseGet(() -> new CrystalNode(data.toImmutable(), new HashMap<>()));
        return Optional.of(node);
    }

    private boolean populateClusters(ServerLevel level) {
        CrystalNetwork network = CrystalNetwork.get(level);
        for(BlockPos pos : clusterBlocks) {
            BlockState state = level.getBlockState(pos);
            if(!state.is(ModTags.Blocks.CLUSTER)) {
                Caustics.LOGGER.error("Found non-cluster block in cluster block list: {} @ {}", state.getBlock(), pos);
                continue;
            } //somehow getting air in here. No clue how
            Direction facing = state.getValue(AmethystClusterBlock.FACING);

            if(!crystalBlocks.contains(pos.relative(facing.getOpposite())))
                continue;

             if(state.is(ModBlocks.PERIDOT_GROUP.cluster()))
                data.peridotClusters().add(pos);
            else if(state.is(ModBlocks.TOPAZ_GROUP.cluster()))
                data.topazClusters().add(pos);
            else if(state.is(ModBlocks.SELENITE_GROUP.cluster()))
                data.seleniteClusters().add(pos);
            else if(state.is(ModBlocks.SUNSTONE_GROUP.cluster()))
                data.sunstoneClusters().add(pos);
            else if(state.is(ModBlocks.TOURMALINE_GROUP.cluster()))
                data.tourmalineClusters().add(pos);
            else if(state.is(ModBlocks.SAPPHIRE_GROUP.cluster())) {
                Optional<CrystalNode> maybeNode = network.getNodeAt(pos);
                if(maybeNode.isPresent() && maybeNode.get().builder() != this)
                    return true;
                data.sapphireClusters().add(pos);
            }
        }
        return false;
    }
    
}
