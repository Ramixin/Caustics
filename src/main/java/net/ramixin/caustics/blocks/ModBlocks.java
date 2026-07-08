package net.ramixin.caustics.blocks;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.blocks.budding.*;
import net.ramixin.caustics.blocks.mirror.MirrorBlock;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ModBlocks {

    public static final CrystalBlockGroup SAPPHIRE_GROUP = registerGroup("sapphire", BuddingSapphireBlock::new, NetworkClusterBlock::new);
    public static final CrystalBlockGroup BERYL_GROUP = registerGroup("beryl", BuddingBerylBlock::new);
    public static final CrystalBlockGroup PERIDOT_GROUP = registerGroup("peridot", BuddingPeridotBlock::new);
    public static final CrystalBlockGroup TOPAZ_GROUP = registerGroup("topaz", BuddingTopazBlock::new, NetworkClusterBlock::new);
    public static final CrystalBlockGroup SUNSTONE_GROUP = registerGroup("sunstone", BuddingSunstoneBlock::new);
    public static final CrystalBlockGroup SELENITE_GROUP = registerGroup("selenite", BuddingSeleniteBlock::new, ChargeClusterBlock::new);
    public static final CrystalBlockGroup TOURMALINE_GROUP = registerGroup("tourmaline", BuddingTourmalineBlock::new, NetworkClusterBlock::new);

    public static final Block MIRROR = register("mirror", MirrorBlock::new, BlockBehaviour.Properties.of().strength(0.3F).sound(SoundType.GLASS));

    public static void onInitialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS).register(event -> {
            registerGroupTabEntries(TOURMALINE_GROUP,event);
            registerGroupTabEntries(SELENITE_GROUP,event);
            registerGroupTabEntries(SUNSTONE_GROUP,event);
            registerGroupTabEntries(TOPAZ_GROUP,event);
            registerGroupTabEntries(PERIDOT_GROUP,event);
            registerGroupTabEntries(BERYL_GROUP,event);
            registerGroupTabEntries(SAPPHIRE_GROUP,event);
        });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(event ->
                event.insertAfter(Blocks.LODESTONE, ModBlocks.MIRROR)
        );
    }

    private static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> constructor, BlockBehaviour.Properties properties) {
        Identifier id = Caustics.id(name);
        T block = Registry.register(BuiltInRegistries.BLOCK, id, constructor.apply(properties.setId(ResourceKey.create(Registries.BLOCK, id))));
        Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).useBlockDescriptionPrefix()));
        return block;
    }

    private static CrystalBlockGroup registerGroup(String name, Function<BlockBehaviour.Properties, Block> buddingConstructor) {
        return registerGroup(name, buddingConstructor, props -> new AmethystClusterBlock(7, 10, props));
    }

    private static CrystalBlockGroup registerGroup(String name, Function<BlockBehaviour.Properties, Block> buddingConstructor, Function<BlockBehaviour.Properties, Block> clusterConstructor) {
        Function<BlockBehaviour.Properties, Block> blockConstructor = Block::new;
        BiFunction<Integer, Integer, Function<BlockBehaviour.Properties, Block>> budConstructor = (h, w) -> props -> new AmethystClusterBlock(h, w, props);

        BlockBehaviour.Properties blockProps = BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK);
        BlockBehaviour.Properties buddingBlockProps = BlockBehaviour.Properties.ofFullCopy(Blocks.BUDDING_AMETHYST);
        BlockBehaviour.Properties clusterProps = BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_CLUSTER);
        BlockBehaviour.Properties budProps = BlockBehaviour.Properties.ofFullCopy(Blocks.LARGE_AMETHYST_BUD);
        BlockBehaviour.Properties mediumBudProps = BlockBehaviour.Properties.ofFullCopy(Blocks.MEDIUM_AMETHYST_BUD);
        BlockBehaviour.Properties smallBudProps = BlockBehaviour.Properties.ofFullCopy(Blocks.SMALL_AMETHYST_BUD);

        Block block = register(name+"_block", blockConstructor, blockProps);
        Block budding = register("budding_"+name, buddingConstructor, buddingBlockProps);
        Block cluster = register(name+"_cluster", clusterConstructor, clusterProps);
        Block bud = register("large_"+name+"_bud", budConstructor.apply(5, 10), budProps);
        Block mediumBud = register("medium_"+name+"_bud", budConstructor.apply(4, 10), mediumBudProps);
        Block smallBud = register("small_"+name+"_bud", budConstructor.apply(3, 10), smallBudProps);

        return new CrystalBlockGroup(block, budding, cluster, bud, mediumBud, smallBud);
    }

    private static void registerGroupTabEntries(CrystalBlockGroup group, FabricCreativeModeTabOutput output) {
        output.insertAfter(Blocks.BUDDING_AMETHYST, group.block());
        output.insertAfter(Blocks.BUDDING_AMETHYST, group.buddingBlock());
        output.insertAfter(Blocks.AMETHYST_CLUSTER, group.cluster());
        output.insertAfter(Blocks.LARGE_AMETHYST_BUD, group.largeBud());
        output.insertAfter(Blocks.MEDIUM_AMETHYST_BUD, group.mediumBud());
        output.insertAfter(Blocks.SMALL_AMETHYST_BUD, group.smallBud());
    }
}
