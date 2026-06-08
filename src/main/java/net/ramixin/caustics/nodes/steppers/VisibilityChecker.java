package net.ramixin.caustics.nodes.steppers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.ModGameRules;
import net.ramixin.caustics.blocks.ModBlocks;
import net.ramixin.caustics.blocks.mirror.MirrorBlock;
import net.ramixin.caustics.blocks.mirror.MirrorStance;

import java.util.Optional;

public class VisibilityChecker {

    private final BlockPos startPos;
    private BlockPos curPos;
    private Direction curDir;
    private int stepsLeft = -1;
    private int pauseTicks = 0;
    private boolean mustOrient = true;
    private boolean cachedVisibility = false;

    public VisibilityChecker(BlockPos pos) {
        this.startPos = pos;
        this.curPos = pos;
    }

    public void tick(ServerLevel level) {
        if(mustOrient) {
            BlockState state = level.getBlockState(curPos);
            curDir = state.getValue(AmethystClusterBlock.FACING);
            mustOrient = false;
        }
        if(stepsLeft == -1) {
            stepsLeft = level.getGameRules().get(ModGameRules.MAX_STEPS);
        }
        if(pauseTicks > 0) {
            pauseTicks--;
            return;
        }

        int steps = level.getGameRules().get(ModGameRules.STEPS_PER_TICK);
        for(int i = 0; i < steps; i++) {
            if(stepsLeft == 0) {
                Caustics.LOGGER.error("Visibility checker ran out of steps");
                verdict(false);
                return;
            }

            boolean proceed = step(level);
            stepsLeft--;
            if(!proceed)
                return;
        }
    }

    public boolean step(ServerLevel level) {
        curPos = curPos.relative(curDir);
        BlockState nextState = level.getBlockState(curPos);
        if(nextState.is(ModBlocks.MIRROR)) {
            Optional<Direction> maybeDir = applyMirror(nextState);
            if(maybeDir.isEmpty()) {
                Caustics.LOGGER.error("mirror state cannot reflect at {}", curPos);
                verdict(false);
                return false;
            }
            curDir = maybeDir.get();
        }
        else if(!nextState.is(BlockTags.AIR)) {
            //Caustics.LOGGER.error("non-air block at {}", curPos);
            verdict(false);
            return false;
        }
        if(level.isOutsideBuildHeight(curPos)) {
            verdict(true);
            return false;
        }
        return true;
    }

    public boolean isVisible() {
        return cachedVisibility;
    }

    public void verdict(boolean visible) {
        cachedVisibility = visible;
        mustOrient = true;
        curPos = startPos;
        pauseTicks = 40;
        stepsLeft = -1;
    }

    private Optional<Direction> applyMirror(BlockState state) {
        Direction facing = state.getValue(MirrorBlock.FACING);
        Direction oppositeFacing = facing.getOpposite();
        MirrorStance stance = state.getValue(MirrorBlock.STANCE);

        return switch(stance) {
            case FRONT -> {
                if(oppositeFacing == curDir) yield Optional.of(curDir.getClockWise());
                Direction rotated = curDir.getCounterClockWise();
                if(oppositeFacing == rotated) yield Optional.of(rotated);
                yield Optional.empty();
            }

            case UP -> {
                if(curDir == Direction.DOWN) yield Optional.of(facing);
                if(oppositeFacing == curDir) yield Optional.of(Direction.UP);
                yield Optional.empty();
            }
            case DOWN -> {
                if(curDir == Direction.UP) yield Optional.of(facing);
                if(oppositeFacing == curDir) yield Optional.of(Direction.DOWN);
                yield Optional.empty();
            }
        };
    }

}
