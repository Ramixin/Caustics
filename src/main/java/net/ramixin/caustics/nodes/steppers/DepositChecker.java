package net.ramixin.caustics.nodes.steppers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.ramixin.caustics.blocks.ModBlocks;

import java.util.Optional;

public class DepositChecker extends AbstractChecker<Optional<BlockPos>> {

    public DepositChecker(BlockPos pos) {
        super(pos);
    }

    @Override
    protected Optional<BlockPos> defaultValue() {
        return Optional.empty();
    }

    @Override
    protected Optional<BlockPos> hitBlockValue(ServerLevel level) {
        Optional<BlockPos> maybeOut = backtrack();
        if(maybeOut.isEmpty()) return Optional.empty();
        BlockPos outPos = maybeOut.get();
        while(!level.getBlockState(outPos).isAir() || !level.getBlockState(outPos.above()).isAir()) {
            Optional<BlockPos> back = backtrack();
            if(back.isEmpty()) return Optional.empty();
            outPos = back.get();
        }
        return Optional.of(outPos);
    }

    @Override
    protected Optional<BlockPos> leftBuildHeightValue() {
        return Optional.empty();
    }

    @Override
    protected boolean validClusterBlock(BlockState state) {
        return state.is(ModBlocks.PERIDOT_GROUP.cluster());
    }
}
