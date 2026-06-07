package net.ramixin.caustics.blocks.mirror;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.ramixin.caustics.Caustics;
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
        return state.setValue(STANCE, getStance(shifting, lookingUp)).setValue(GRIP, getGrip(shifting, lookingUp, placedOn, facing));
    }

    private MirrorStance getStance(boolean shifting, boolean lookingUp) {
        if(!shifting) return MirrorStance.FRONT;
        return lookingUp ? MirrorStance.UP : MirrorStance.DOWN;
    }

    private MirrorGrip getGrip(boolean shifting, boolean lookingUp, Direction placedOn, Direction facing) {
        if(placedOn == Direction.UP && (!shifting || lookingUp)) return MirrorGrip.STANDING;
        if(placedOn == Direction.DOWN && (!shifting || !lookingUp)) return MirrorGrip.UP;
        if(placedOn.getClockWise() == facing) return MirrorGrip.LEFT;
        if((shifting && placedOn.getCounterClockWise() == facing) || (!shifting && placedOn == facing)) return MirrorGrip.RIGHT;
        if(shifting && placedOn == facing) return MirrorGrip.BACK;
        Caustics.LOGGER.error("Invalid grip combination: {}, {}, {}, {}", placedOn, facing, shifting, lookingUp);
        return MirrorGrip.STANDING;
    }
}
