package net.ramixin.caustics.menus;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class TuningForkMenu extends AbstractContainerMenu {

    @SuppressWarnings("unused") // player
    public TuningForkMenu(int containerId, Inventory inventory, Player player) {
        this(containerId, inventory);
    }

    @SuppressWarnings("unused") // inventory
    protected TuningForkMenu(int containerId, Inventory inventory) {
        super(ModMenus.TUNING_FORK_MENU_TYPE, containerId);
    }


    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int slotIndex) {
        throw new IllegalStateException("Tuning fork menu does not support item transfers");
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return true;
    }
}
