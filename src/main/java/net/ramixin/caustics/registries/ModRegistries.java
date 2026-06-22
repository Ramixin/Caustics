package net.ramixin.caustics.registries;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.ramixin.caustics.Caustics;

import static net.ramixin.caustics.registries.TimingType.*;

public class ModRegistries {

    public static final ResourceKey<Registry<Handle>> HANDLE_KEY = ResourceKey.createRegistryKey(Caustics.id("leapers_handle"));
    public static final Registry<Handle> HANDLE = FabricRegistryBuilder.create(HANDLE_KEY).buildAndRegister();

    public static final ResourceKey<Registry<Item>> DECORATION_KEY = ResourceKey.createRegistryKey(Caustics.id("decoration"));
    public static final Registry<Item> DECORATION = FabricRegistryBuilder.create(DECORATION_KEY).buildAndRegister();

    public static void onInitialize() {
        register("stick", new Handle(Items.STICK, NORMAL, NORMAL), HANDLE);
        register("blaze_rod", new Handle(Items.BLAZE_ROD, QUICK, SLOW), HANDLE);
        register("breeze_rod", new Handle(Items.BREEZE_ROD, SLOW, QUICK), HANDLE);

        register("gold_ingot", Items.GOLD_INGOT, DECORATION);
        register("diamond", Items.DIAMOND, DECORATION);
        register("emerald", Items.EMERALD, DECORATION);
        register("amethyst_shard", Items.AMETHYST_SHARD, DECORATION);
        register("quartz", Items.QUARTZ, DECORATION);
    }

    public static <T> void register(String id, T item, Registry<T> registry) {
        Registry.register(registry, Identifier.withDefaultNamespace(id), item);
    }

}
