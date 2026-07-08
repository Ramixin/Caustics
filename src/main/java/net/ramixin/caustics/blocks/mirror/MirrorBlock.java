package net.ramixin.caustics.blocks.mirror;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MirrorBlock extends Block {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<MirrorStance> STANCE = EnumProperty.create("stance", MirrorStance.class);
    public static final EnumProperty<MirrorGrip> GRIP = EnumProperty.create("grip", MirrorGrip.class);

    private static final MapCodec<MirrorBlock> CODEC = simpleCodec(MirrorBlock::new);
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 12, 14);

    public MirrorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected @NonNull VoxelShape getShape(@NonNull BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos, @NonNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING, STANCE, GRIP));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NonNull BlockPlaceContext ctx) {
        Player player = ctx.getPlayer();
        if(player == null) return super.getStateForPlacement(ctx);
        Direction placedOn = ctx.getClickedFace();
        Direction facing = ctx.getHorizontalDirection().getOpposite();
        BlockState state = defaultBlockState().setValue(FACING, facing);
        boolean shifting = player.isShiftKeyDown();
        boolean lookingUp = player.getXRot() >= 0;
        MirrorGrip grip = getGrip(shifting, lookingUp, placedOn, facing);
        if(grip == null) return null;
        return state.setValue(STANCE, getStance(shifting, lookingUp)).setValue(GRIP, grip);
    }

    @Override
    protected void neighborChanged(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if(!canSurvive(state, level, pos)) level.destroyBlock(pos, true);
    }

    @Override
    protected boolean canSurvive(@NonNull BlockState state, @NonNull LevelReader level, @NonNull BlockPos pos) {
        MirrorGrip grip = state.getValue(GRIP);
        Direction facing = state.getValue(FACING);
        return switch (grip) {
            case STANDING -> level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
            case UP -> level.getBlockState(pos.above()).isFaceSturdy(level, pos.above(), Direction.DOWN);
            case RIGHT, BACK -> {
                BlockPos relPos = pos.relative(facing.getOpposite());
                yield level.getBlockState(relPos).isFaceSturdy(level, relPos, facing);
            }
            case LEFT -> {
                BlockPos relPos = pos.relative(facing.getClockWise());
                yield level.getBlockState(relPos).isFaceSturdy(level, relPos, facing.getCounterClockWise());
            }
        };
    }

    private MirrorStance getStance(boolean shifting, boolean lookingUp) {
        if(!shifting) return MirrorStance.FRONT;
        return lookingUp ? MirrorStance.UP : MirrorStance.DOWN;
    }

    private @Nullable MirrorGrip getGrip(boolean shifting, boolean lookingUp, Direction placedOn, Direction facing) {
        if(placedOn == Direction.UP && (!shifting || lookingUp)) return MirrorGrip.STANDING;
        if(placedOn == Direction.DOWN && (!shifting || !lookingUp)) return MirrorGrip.UP;
        if(placedOn.getClockWise() == facing) return MirrorGrip.LEFT;
        if((shifting && placedOn.getCounterClockWise() == facing) || (!shifting && placedOn == facing)) return MirrorGrip.RIGHT;
        if(shifting && placedOn == facing) return MirrorGrip.BACK;
        return null;
    }
}
