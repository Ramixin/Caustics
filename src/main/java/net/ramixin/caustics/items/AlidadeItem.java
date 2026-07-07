package net.ramixin.caustics.items;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpyglassItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.ramixin.caustics.blocks.ModBlocks;
import net.ramixin.caustics.items.components.Frequency;
import net.ramixin.caustics.items.components.ModDataComponents;
import net.ramixin.caustics.nodes.core.CrystalNetwork;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class AlidadeItem extends SpyglassItem {

    public AlidadeItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        if(!(level instanceof ServerLevel serverLevel))
            return InteractionResult.PASS;
        CrystalNetwork network = CrystalNetwork.get(serverLevel);
        if(player.isShiftKeyDown()) {
            HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(player, EntitySelector.CAN_BE_PICKED, player.blockInteractionRange());
            if(!(hitResult instanceof BlockHitResult blockHitResult)) return InteractionResult.PASS;
            BlockPos pos = blockHitResult.getBlockPos();
            if(!level.getBlockState(pos).is(ModBlocks.SUNSTONE_GROUP.cluster())) return InteractionResult.PASS;
            Optional<Frequency> maybeFreq = network.frequencyRegistry().getFrequencyAt(pos);
            maybeFreq.ifPresent(frequency -> player.getItemInHand(hand).set(ModDataComponents.FREQUENCY, frequency));
            return InteractionResult.PASS;
        }

        network.synchronizer().addRealtime(player.getUUID());
        return super.use(level, player, hand);
    }

    @Override
    public boolean releaseUsing(@NonNull ItemStack itemStack, @NonNull Level level, @NonNull LivingEntity entity, int remainingTime) {
        boolean original = super.releaseUsing(itemStack, level, entity, remainingTime);
        if(!(level instanceof ServerLevel serverLevel)) return original;
        if(!(entity instanceof Player player)) return original;
        CrystalNetwork network = CrystalNetwork.get(serverLevel);
        network.synchronizer().removeRealtime(player.getUUID());
        return original;
    }
}
