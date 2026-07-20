package net.ramixin.caustics.client.nodes.cache;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.nodes.icons.DowserIcon;

import java.util.Set;

public class DowserIconCache extends AbstractIconCache<DowserIcon> {

    public DowserIconCache() {
        super(DowserIcon[]::new, DowserIcon::new);
    }

    @Override
    public BlockPos[] getPositions() {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null) throw new IllegalStateException("Player is null");
        Set<BlockPos> activeJammers = ClientCrystalNetwork.getInstance().getActiveJammers(player.position());
        return activeJammers.toArray(BlockPos[]::new);
    }
}
