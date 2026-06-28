package net.ramixin.caustics.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.level.Level;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.items.components.ModDataComponents;
import net.ramixin.caustics.nodes.core.CrystalNetwork;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.UUID;

import static net.ramixin.caustics.utils.LeaperUtil.getChargeUpTicks;
import static net.ramixin.caustics.utils.LeaperUtil.getCooldownTicks;

public class LeaperItem extends Item {

    public LeaperItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(stack.has(ModDataComponents.LEAPER_MATERIAL))
            player.startUsingItem(hand);
        return super.use(level, player, hand);
    }

    @Override
    public void onUseTick(@NonNull Level level, @NonNull LivingEntity livingEntity, @NonNull ItemStack itemStack, int ticksRemaining) {
        super.onUseTick(level, livingEntity, itemStack, ticksRemaining);
        if(!(level instanceof ServerLevel serverLevel)) return;
        if(!(livingEntity instanceof Player player)) return;
        int timeUsed = 72000 - ticksRemaining;
        int chargeUpTicks = getChargeUpTicks(itemStack);
        if(timeUsed < chargeUpTicks) return;
        CrystalNetwork network = CrystalNetwork.get(serverLevel);
        if(network.getLeapPos(player.getUUID()).isPresent())
            releaseUsing(itemStack, level, player, ticksRemaining);

    }

    @Override
    public boolean releaseUsing(@NonNull ItemStack itemStack, @NonNull Level level, @NonNull LivingEntity entity, int remainingTime) {
        int used = 72000 - remainingTime;
        int chargeUpTicks = getChargeUpTicks(itemStack);
        boolean original = super.releaseUsing(itemStack, level, entity, used);
        if(used < chargeUpTicks) return original;
        if(!(level instanceof ServerLevel serverLevel)) return original;
        if(!(entity instanceof ServerPlayer player)) return original;
        CrystalNetwork network = CrystalNetwork.get(serverLevel);
        Optional<BlockPos> maybePos = network.getLeapPos(player.getUUID());
        if(maybePos.isEmpty()) return original;
        BlockPos pos = maybePos.get();
        player.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        int cooldownTicks = player.isCreative() ? 30 : getCooldownTicks(itemStack);
        updateCooldownComponent(itemStack);
        player.getCooldowns().addCooldown(itemStack, cooldownTicks);
        entity.stopUsingItem();
        return original;
    }

    @Override
    public int getUseDuration(@NonNull ItemStack itemStack, @NonNull LivingEntity user) {
        return 72000;
    }

    @Override
    public @NonNull ItemUseAnimation getUseAnimation(@NonNull ItemStack itemStack) {
        return ItemUseAnimation.BOW;
    }

    private static void updateCooldownComponent(ItemStack stack) {
        UseCooldown cooldown = stack.get(DataComponents.USE_COOLDOWN);
        if(cooldown != null) return;
        int ticks = getCooldownTicks(stack);
        String group = UUID.randomUUID().toString().toLowerCase();
        stack.set(DataComponents.USE_COOLDOWN, new UseCooldown(ticks, Optional.of(Caustics.id(group))));
    }
}
