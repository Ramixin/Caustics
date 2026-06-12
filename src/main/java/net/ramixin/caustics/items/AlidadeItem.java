package net.ramixin.caustics.items;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpyglassItem;
import net.minecraft.world.level.Level;
import net.ramixin.caustics.nodes.CrystalNetwork;
import org.jspecify.annotations.NonNull;

public class AlidadeItem extends SpyglassItem {

    public AlidadeItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        if (!(level instanceof ServerLevel serverLevel))
            return InteractionResult.PASS;
        if(player.isShiftKeyDown()) {

            return InteractionResult.PASS;
        }
        CrystalNetwork network = CrystalNetwork.get(serverLevel);
        network.startSyncing(player.getUUID());
        return super.use(level, player, hand);
    }

    @Override
    public boolean releaseUsing(@NonNull ItemStack itemStack, @NonNull Level level, @NonNull LivingEntity entity, int remainingTime) {
        boolean original = super.releaseUsing(itemStack, level, entity, remainingTime);
        if(!(level instanceof ServerLevel serverLevel)) return original;
        if(!(entity instanceof Player player)) return original;
        CrystalNetwork network = CrystalNetwork.get(serverLevel);
        network.stopSyncing(player.getUUID());
        return original;
    }
}
