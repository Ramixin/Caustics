package net.ramixin.caustics.items;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.ModTags;
import net.ramixin.caustics.items.components.ModDataComponents;
import net.ramixin.caustics.items.components.SpyglassLens;

import java.util.function.Function;

public class ModItems {

    public static final Item SAPPHIRE_SHARD = register("sapphire_shard", Item::new, new Item.Properties());
    public static final Item BERYL_SHARD = register("beryl_shard", Item::new, new Item.Properties());
    public static final Item PERIDOT_SHARD = register("peridot_shard", Item::new, new Item.Properties());
    public static final Item TOPAZ_SHARD = register("topaz_shard", Item::new, new Item.Properties());
    public static final Item SUNSTONE_SHARD = register("sunstone_shard", Item::new, new Item.Properties());
    public static final Item SELENITE_SHARD = register("selenite_shard", Item::new, new Item.Properties());
    public static final Item TOURMALINE_SHARD = register("tourmaline_shard", Item::new, new Item.Properties());

    public static final Item TUNING_FORK = register("tuning_fork", TuningForkItem::new, new Item.Properties().stacksTo(1));

    public static final Item LEAPER = register("leaper", LeaperItem::new, new Item.Properties().stacksTo(1));

    public static void onInitialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(event ->
                event.insertAfter(Items.AMETHYST_SHARD,
                    SAPPHIRE_SHARD, BERYL_SHARD, PERIDOT_SHARD, TOPAZ_SHARD, SUNSTONE_SHARD, SELENITE_SHARD, TOURMALINE_SHARD));

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(event -> {
            event.insertAfter(Items.BRUSH, TUNING_FORK);

            for(Holder<Item> itemHolder : BuiltInRegistries.ITEM.getTagOrEmpty(ModTags.Items.LENS)) {
                Item item = itemHolder.value();
                ItemStack stack = new ItemStack(Items.SPYGLASS);
                stack.set(ModDataComponents.SPYGLASS_LENS, new SpyglassLens(BuiltInRegistries.ITEM.wrapAsHolder(item)));
                event.insertAfter(Items.SPYGLASS, stack);
            }
        });
    }

    private static <T extends Item> T register(String name, Function<Item.Properties, T> constructor, Item.Properties properties) {
        Identifier id = Caustics.id(name);
        T item = constructor.apply(properties.setId(ResourceKey.create(Registries.ITEM, id)));
        Registry.register(BuiltInRegistries.ITEM, id, item);
        return item;
    }


}
