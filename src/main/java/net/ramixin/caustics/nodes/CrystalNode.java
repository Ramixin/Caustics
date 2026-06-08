package net.ramixin.caustics.nodes;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.ramixin.caustics.nodes.steppers.NodeBuilder;
import net.ramixin.caustics.nodes.steppers.VisibilityChecker;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;

public record CrystalNode(BlocksOfInterest blocks, NodeBuilder builder, HashMap<BlockPos, VisibilityChecker> checkers) {

    public static final Codec<CrystalNode> CODEC = BlocksOfInterest.CODEC.xmap(
            blocksOfInterest -> new CrystalNode(blocksOfInterest, new NodeBuilder(blocksOfInterest.sapphireClusters())),
            CrystalNode::blocks
    );

    public CrystalNode(BlocksOfInterest blocks, NodeBuilder builder) {
        HashMap<BlockPos, VisibilityChecker> deducers = new HashMap<>();
        for(BlockPos pos : blocks.sapphireClusters()) {
            deducers.put(pos, new VisibilityChecker(pos));
        }

        this(blocks, builder, deducers);
    }

    public CrystalNode(BlocksOfInterest blocks, NodeBuilder builder, HashMap<BlockPos, VisibilityChecker> checkers) {
        this.blocks = blocks;
        this.builder = builder;
        HashMap<BlockPos, VisibilityChecker> newCheckers = new HashMap<>();
        for(BlockPos pos : blocks.sapphireClusters()) {
            if(checkers.containsKey(pos)) {
                newCheckers.put(pos, checkers.get(pos));
            } else
                newCheckers.put(pos, new VisibilityChecker(pos));
        }

        this.checkers = newCheckers;
    }

    public void tick(ServerLevel level) {
        builder.tick(level);
        for(VisibilityChecker deducer : checkers.values()) {
            deducer.tick(level);
        }
    }

    public boolean visibleClusterAt(BlockPos pos) {
        if(!checkers.containsKey(pos)) return false;
        return checkers.get(pos).isVisible();
    }

    @Override
    public @NonNull String toString() {
        return "sapphire clusters: " + blocks.sapphireClusters();
    }

}
