package net.ramixin.caustics.items;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.ramixin.caustics.items.components.Frequency;
import net.ramixin.caustics.menus.TuningForkMenu;
import net.ramixin.caustics.networking.SetFrequencyPayload;
import org.jspecify.annotations.NonNull;

public class TuningForkItem extends Item {

    public TuningForkItem(Properties properties) {
        super(properties);
    }

    //sneak + right = configure frequency
    //right = set node to frequency
    //left = debug visibility


    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        if(!(level instanceof ServerLevel)) return InteractionResult.PASS;

        player.openMenu(getMenuProvider());
        if(!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        ItemStack tuningFork = serverPlayer.getMainHandItem();
        Frequency frequency = tuningFork.get(ModDataComponents.FREQUENCY);
        if(frequency != null)
            ServerPlayNetworking.send(serverPlayer, new SetFrequencyPayload(frequency.network(), frequency.node()));

        return InteractionResult.SUCCESS;
    }

    private MenuProvider getMenuProvider() {
        return new SimpleMenuProvider(TuningForkMenu::new, Component.translatable("container.caustics.tuning_fork"));
    }
}
