package net.ramixin.caustics.items;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.nodes.core.CrystalNetwork;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class LeaperItem extends Item {

    public LeaperItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        return super.use(level, player, hand);
    }

    @Override
    public void onUseTick(@NonNull Level level, @NonNull LivingEntity livingEntity, @NonNull ItemStack itemStack, int ticksRemaining) {
        super.onUseTick(level, livingEntity, itemStack, ticksRemaining);
    }

    @Override
    public boolean releaseUsing(@NonNull ItemStack itemStack, @NonNull Level level, @NonNull LivingEntity entity, int remainingTime) {
        Caustics.LOGGER.info("released with remaining time: {}", remainingTime);
        boolean original = super.releaseUsing(itemStack, level, entity, remainingTime);
        if(remainingTime > 0) return original;
        Caustics.LOGGER.info("enough time passed");
        if(!(level instanceof ServerLevel serverLevel)) return original;
        Caustics.LOGGER.info("is on server");
        if(!(entity instanceof ServerPlayer player)) return original;
        Caustics.LOGGER.info("is player");
        CrystalNetwork network = CrystalNetwork.get(serverLevel);
        Optional<BlockPos> maybePos = network.getLeapPos(player.getUUID());
        if(maybePos.isEmpty()) return original;
        BlockPos pos = maybePos.get();
        Caustics.LOGGER.info("has leap pos: {}", pos);
        player.teleportTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        player.getCooldowns().addCooldown(itemStack, 40);
        return original;
    }

    @Override
    public int getUseDuration(@NonNull ItemStack itemStack, @NonNull LivingEntity user) {
        return 20;
    }

    @Override
    public @NonNull ItemUseAnimation getUseAnimation(@NonNull ItemStack itemStack) {
        return ItemUseAnimation.SPEAR;
    }
}
