package net.ramixin.caustics.nodes.steppers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.ramixin.caustics.ModTags;

public class VisibilityChecker extends AbstractChecker<Boolean> {

    public VisibilityChecker(BlockPos pos) {
        super(pos);
    }

    @Override
    protected Boolean defaultValue() {
        return false;
    }

    @Override
    protected Boolean hitBlockValue(ServerLevel level, BlockPos pos) {
        return false;
    }

    @Override
    protected Boolean leftBuildHeightValue() {
        return true;
    }

    @Override
    protected boolean validClusterBlock(BlockState state) {
        return state.is(ModTags.Blocks.NETWORK_CLUSTER);
    }
}
