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
import java.util.Stack;

public abstract class AbstractChecker<T> {

    private final BlockPos startPos;
    private BlockPos curPos;
    private Direction curDir;
    private int stepsLeft = -1;
    private int pauseTicks = 0;
    private boolean mustOrient = true;
    private T cachedVal = defaultValue();
    private final Stack<BlockPos> visited = new Stack<>();
    private boolean syncingDirty = false;

    public AbstractChecker(BlockPos pos) {
        this.startPos = pos;
        this.curPos = pos;
    }

    public boolean consumeSyncingDirty() {
        boolean val = syncingDirty;
        syncingDirty = false;
        return val;
    }

    protected abstract T defaultValue();

    protected abstract T hitBlockValue(ServerLevel level, BlockPos pos);

    protected abstract T leftBuildHeightValue();

    protected abstract boolean validClusterBlock(BlockState state);

    public void tick(ServerLevel level) {
        if(mustOrient) {
            BlockState state = level.getBlockState(curPos);
            if(!validClusterBlock(state)) {
                verdict(defaultValue());
                return;
            }
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
                Caustics.LOGGER.error("checker ran out of steps");
                verdict(defaultValue());
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
        visited.push(curPos);
        BlockState nextState = level.getBlockState(curPos);
        if(nextState.is(ModBlocks.MIRROR)) {
            Optional<Direction> maybeDir = applyMirror(nextState);
            if(maybeDir.isEmpty()) {
                verdict(hitBlockValue(level, curPos));
                return false;
            }
            curDir = maybeDir.get();
        }
        else if(!nextState.is(BlockTags.AIR)) {
            verdict(hitBlockValue(level, curPos));
            return false;
        }
        if(level.isOutsideBuildHeight(curPos)) {
            verdict(leftBuildHeightValue());
            return false;
        }
        return true;
    }

    public T getValue() {
        return cachedVal;
    }

    protected void verdict(T val) {
        cachedVal = val;
        mustOrient = true;
        curPos = startPos;
        pauseTicks = 40;
        stepsLeft = -1;
        syncingDirty = true;
    }

    protected Optional<BlockPos> backtrack() {
        if(visited.isEmpty()) return Optional.empty();
        return Optional.of(visited.pop());
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
