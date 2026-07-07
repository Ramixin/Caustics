package net.ramixin.caustics.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.ramixin.caustics.nodes.core.CrystalNetwork;
import org.jspecify.annotations.NonNull;

public class ChargeClusterBlock extends AmethystClusterBlock {

    public static final BooleanProperty CHARGED = BooleanProperty.create("charged");

    public ChargeClusterBlock(Properties props) {
        super(7, 10, props);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NonNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(CHARGED));
    }

    @Override
    protected void randomTick(@NonNull BlockState state, @NonNull ServerLevel level, @NonNull BlockPos pos, @NonNull RandomSource random) {
        super.randomTick(state, level, pos, random);
        CrystalNetwork network = CrystalNetwork.get(level);
        if(!network.nodeWorker().isSeleniteVisible(pos)) return;
        if(network.nodeWorker().getSeleniteLightLevel(pos) < 10) return;
        if(random.nextInt(5) != 2) return;
        level.setBlockAndUpdate(pos, state.setValue(CHARGED, true));
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(CHARGED);
    }
}
