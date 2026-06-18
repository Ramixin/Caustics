package net.ramixin.caustics.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.ramixin.caustics.nodes.core.CrystalNetwork;
import org.jspecify.annotations.NonNull;

public class NetworkClusterBlock extends AmethystClusterBlock {

    public NetworkClusterBlock(Properties props) {
        super(7, 10, props);
    }

    @Override
    protected void onPlace(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if(level instanceof ServerLevel serverLevel)
            CrystalNetwork.get(serverLevel).generateNodeAt(pos);
    }
}
