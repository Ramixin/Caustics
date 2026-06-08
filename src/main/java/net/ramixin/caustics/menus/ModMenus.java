package net.ramixin.caustics.menus;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.ramixin.caustics.Caustics;

public class ModMenus {

    public static final MenuType<TuningForkMenu> TUNING_FORK_MENU_TYPE = register("tuning_fork", TuningForkMenu::new);

    public static void onInitialize() {

    }

    private static <T extends AbstractContainerMenu> MenuType<T> register(String name, MenuType.MenuSupplier<T> supplier) {
        return Registry.register(BuiltInRegistries.MENU, Caustics.id(name), new MenuType<>(supplier, FeatureFlags.DEFAULT_FLAGS));
    }

}
