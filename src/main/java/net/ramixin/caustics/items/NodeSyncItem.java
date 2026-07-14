package net.ramixin.caustics.items;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.ramixin.caustics.nodes.core.CrystalNetwork;

import java.util.Optional;

public interface NodeSyncItem {

    static Optional<InteractionResult> use(Level level, Player player) {
        if(!(level instanceof ServerLevel serverLevel))
            return Optional.of(InteractionResult.PASS);
        CrystalNetwork network = CrystalNetwork.get(serverLevel);
        network.synchronizer().addRealtime(player.getUUID());
        return Optional.empty();
    }

    static boolean releaseUsing(Level level, LivingEntity entity, boolean original) {
        if(!(level instanceof ServerLevel serverLevel)) return original;
        if(!(entity instanceof Player player)) return original;
        CrystalNetwork network = CrystalNetwork.get(serverLevel);
        network.synchronizer().removeRealtime(player.getUUID());
        return original;
    }
}
