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
    private boolean started = false;
    private int stepsLeft = 0;
    private final Queue<BlockPos> posQueue = new LinkedList<>();
    private final Set<BlockPos> crystalBlocks = new HashSet<>();
    private final Set<BlockPos> clusterBlocks = new HashSet<>();
    private final Set<BlockPos> visitedPositions = new HashSet<>();
    private int pauseTicks = 0;

    public NodeBuilder(BlockPos startingPos) {
        this.potentialStarts.add(startingPos);
    }

    public NodeBuilder(Collection<BlockPos> startingPosList) {
        this.potentialStarts.addAll(startingPosList);
    }

    public void tick(ServerLevel level) {
        if(!started) {
            stepsLeft = level.getGameRules().get(ModGameRules.MAX_STEPS);
            potentialStarts.removeIf(pos -> !level.getBlockState(pos).is(ModBlocks.SAPPHIRE_GROUP.cluster()));
            started = true;
            if(potentialStarts.isEmpty()) return;
            BlockPos first = potentialStarts.getFirst();
            clusterBlocks.add(first);
            posQueue.add(first.relative(level.getBlockState(first).getValue(AmethystClusterBlock.FACING).getOpposite()));
        }

        if(pauseTicks > 0) {
            pauseTicks--;
            return;
        }

        if(posQueue.isEmpty()) return;

        int stepsPerTick = level.getGameRules().get(ModGameRules.STEPS_PER_TICK);
        for(int i = 0; i < stepsPerTick; i++) {
            if(stepsLeft == 0) {
                Caustics.LOGGER.error("Node builder ran out of steps");
                return;
            }

            if(posQueue.isEmpty()) return;
            BlockPos pos = posQueue.poll();
            if(!visitedPositions.add(pos)) continue;
            stepsLeft--;
            step(level, pos);
        }
    }

    public void pause(int ticks) {
        pauseTicks = ticks;
    }

    private void step(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if(state.is(ModTags.Blocks.CLUSTER))
            clusterBlocks.add(pos);
        else if(state.is(ModTags.Blocks.CRYSTAL)) {
            crystalBlocks.add(pos);
            posQueue.addAll(List.of(pos.above(), pos.below(), pos.north(), pos.south(), pos.east(), pos.west()));
        }
    }

    public boolean isBuilding() {
        return (!posQueue.isEmpty() || !started) && (stepsLeft > 0 || stepsLeft == -1);
    }

    public Optional<NodeData> build(ServerLevel level) {
        if(populateClusters(level)) return Optional.empty();
        if(data.sapphireClusters().isEmpty()) return Optional.empty();

        potentialStarts.removeAll(data.sapphireClusters());
        if(!potentialStarts.isEmpty()) {
            CrystalNetwork.get(level).addBuilder(new NodeBuilder(potentialStarts), potentialStarts);
        }


        return Optional.of(data.toImmutable());
    }

    private boolean populateClusters(ServerLevel level) {
        CrystalNetwork network = CrystalNetwork.get(level);
        for(BlockPos pos : clusterBlocks) {
            BlockState state = level.getBlockState(pos);
            if(!state.is(ModTags.Blocks.CLUSTER)) {
                Caustics.LOGGER.error("Found non-cluster block in cluster block list: {} @ {}", state.getBlock(), pos);
                continue;
            }
            Direction facing = state.getValue(AmethystClusterBlock.FACING);
            if(!crystalBlocks.contains(pos.relative(facing.getOpposite()))) continue;

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
                if(maybeNode.isPresent()) {
                    CrystalNode node = maybeNode.get();
                    Optional<CrystalNode> maybeOtherNode = network.getNodeForBuilder(this);
                    if(maybeOtherNode.isPresent()) {
                        CrystalNode otherNode = maybeOtherNode.get();
                        if(otherNode != node)
                            return true;
                    } else
                        return true;
                }
                data.sapphireClusters().add(pos);
            }
        }
        return false;
    }
    
}
