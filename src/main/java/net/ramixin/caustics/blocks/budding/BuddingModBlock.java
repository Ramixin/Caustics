package net.ramixin.caustics.blocks.budding;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.ramixin.caustics.blocks.CrystalBlockGroup;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class BuddingModBlock extends AmethystBlock {

    public static final int GROWTH_CHANCE = 5;
    private static final Direction[] DIRECTIONS = Direction.values();
    private final Supplier<CrystalBlockGroup> groupGetter;

    public BuddingModBlock(final BlockBehaviour.Properties properties, Supplier<CrystalBlockGroup> getter) {
        super(properties);
        this.groupGetter = getter;
    }

    @Override
    protected void randomTick(final @NonNull BlockState state, final @NonNull ServerLevel level, final @NonNull BlockPos pos, final RandomSource random) {
        if (random.nextInt(GROWTH_CHANCE) != 0) return;
        Direction growDirection = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos growPos = pos.relative(growDirection);
        BlockState relativeState = level.getBlockState(growPos);
        Optional<Block> nextStage;
        CrystalBlockGroup group = groupGetter.get();

        if(BuddingAmethystBlock.canClusterGrowAtState(relativeState))
            nextStage = Optional.of(group.smallBud());
        else
            nextStage = group.nextStage(relativeState.getBlock());

        if (nextStage.isEmpty()) return;
        BlockState targetState = nextStage.get().defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, growDirection)
                .setValue(AmethystClusterBlock.WATERLOGGED, relativeState.getFluidState().is(Fluids.WATER));
        level.setBlockAndUpdate(growPos, targetState);

    }
}
