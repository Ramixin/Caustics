package net.ramixin.caustics.items;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.ramixin.caustics.blocks.ModBlocks;
import net.ramixin.caustics.items.components.Frequency;
import net.ramixin.caustics.items.components.ModDataComponents;
import net.ramixin.caustics.nodes.core.CrystalNetwork;

import java.util.Optional;

public interface AlidadeItem {

    static Optional<InteractionResult> use(Level level, Player player, InteractionHand hand) {
        if(!(level instanceof ServerLevel serverLevel))
            return Optional.of(InteractionResult.PASS);
        CrystalNetwork network = CrystalNetwork.get(serverLevel);
        if(player.isShiftKeyDown()) {
            HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(player, EntitySelector.CAN_BE_PICKED, player.blockInteractionRange());
            if(!(hitResult instanceof BlockHitResult blockHitResult)) return Optional.of(InteractionResult.PASS);
            BlockPos pos = blockHitResult.getBlockPos();
            if(!level.getBlockState(pos).is(ModBlocks.SUNSTONE_GROUP.cluster())) return Optional.of(InteractionResult.PASS);
            Optional<Frequency> maybeFreq = network.frequencyRegistry().getFrequencyAt(pos);
            maybeFreq.ifPresent(frequency -> player.getItemInHand(hand).set(ModDataComponents.FREQUENCY, frequency));
            return Optional.of(InteractionResult.PASS);
        }

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
