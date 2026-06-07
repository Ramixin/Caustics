package net.ramixin.caustics;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpyglassItem;

import java.util.function.Function;

public class ModItems {

    public static final Item SAPPHIRE_SHARD = register("sapphire_shard", Item::new, new Item.Properties());
    public static final Item CINNABAR_SHARD = register("cinnabar_shard", Item::new, new Item.Properties());
    public static final Item PERIDOT_SHARD = register("peridot_shard", Item::new, new Item.Properties());
    public static final Item TOPAZ_SHARD = register("topaz_shard", Item::new, new Item.Properties());
    public static final Item SUNSTONE_SHARD = register("sunstone_shard", Item::new, new Item.Properties());
    public static final Item SELENITE_SHARD = register("selenite_shard", Item::new, new Item.Properties());
    public static final Item TOURMALINE_SHARD = register("tourmaline_shard", Item::new, new Item.Properties());

    public static final Item ALIDADE = register("alidade", SpyglassItem::new, new Item.Properties());

    public static void onInitialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(event ->
                event.insertAfter(Items.AMETHYST_SHARD,
                    SAPPHIRE_SHARD, CINNABAR_SHARD, PERIDOT_SHARD, TOPAZ_SHARD, SUNSTONE_SHARD, SELENITE_SHARD, TOURMALINE_SHARD));

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(event ->
                event.insertAfter(Items.SPYGLASS, ALIDADE)
        );
    }

    private static <T extends Item> T register(String name, Function<Item.Properties, T> constructor, Item.Properties properties) {
        Identifier id = Caustics.id(name);
        T item = constructor.apply(properties.setId(ResourceKey.create(Registries.ITEM, id)));
        Registry.register(BuiltInRegistries.ITEM, id, item);
        return item;
    }


}
