package net.ramixin.caustics.client.nodes.cache;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.ramixin.caustics.client.CausticsClient;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.nodes.icons.NodeIcon;
import net.ramixin.caustics.utils.RoutingUtil;

import java.util.Set;
import java.util.function.Function;

public class SimpleIconCache<T extends NodeIcon> extends AbstractIconCache<T> {

    private BlockPos[] positions;

    public SimpleIconCache(Function<Integer, T[]> arrayConstructor, Function<BlockPos, T> constructor) {
        super(arrayConstructor, constructor);
    }

    @Override
    public BlockPos[] getPositions() {
        if(positions != null) return positions;
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null) throw new IllegalStateException("Player is null");
        BlockPos pos = player.blockPosition();
        Set<BlockPos> keys = cache.keySet();
        Set<BlockPos> jammers = ClientCrystalNetwork.getInstance().getJammerPositions();
        return keys
                .stream()
                .filter(blockPos -> RoutingUtil.canConnect(pos, blockPos, jammers, CausticsClient.MAX_SIGNAL_RANGE))
                .toArray(BlockPos[]::new);
    }

    @Override
    public void wipe() {
        super.wipe();
        positions = null;
    }
}
