package net.ramixin.caustics.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.ModItems;
import net.ramixin.caustics.blocks.CrystalBlockGroup;
import net.ramixin.caustics.blocks.ModBlocks;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class CausticsModelProvider extends FabricModelProvider {

    private static final ModelTemplate CLUSTER_TEMPLATE = item("cluster");
    private static final ModelTemplate LARGE_BUD_TEMPLATE = item("large_bud");
    private static final ModelTemplate MEDIUM_BUD_TEMPLATE = item("medium_bud");
    private static final ModelTemplate SMALL_BUD_TEMPLATE = item("small_bud");

    public CausticsModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(@NonNull BlockModelGenerators blockModelGenerators) {
        generateGroupBlocks(ModBlocks.SAPPHIRE_GROUP, blockModelGenerators);
        generateGroupBlocks(ModBlocks.CINNABAR_GROUP, blockModelGenerators);
        generateGroupBlocks(ModBlocks.PERIDOT_GROUP, blockModelGenerators);
        generateGroupBlocks(ModBlocks.TOPAZ_GROUP, blockModelGenerators);
        generateGroupBlocks(ModBlocks.SUNSTONE_GROUP, blockModelGenerators);
        generateGroupBlocks(ModBlocks.SELENITE_GROUP, blockModelGenerators);
        generateGroupBlocks(ModBlocks.TOURMALINE_GROUP, blockModelGenerators);
    }

    @Override
    public void generateItemModels(@NonNull ItemModelGenerators generators) {

        generators.generateFlatItem(ModItems.SAPPHIRE_SHARD, ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(ModItems.CINNABAR_SHARD, ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(ModItems.PERIDOT_SHARD, ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(ModItems.TOPAZ_SHARD, ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(ModItems.SUNSTONE_SHARD, ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(ModItems.SELENITE_SHARD, ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(ModItems.TOURMALINE_SHARD, ModelTemplates.FLAT_ITEM);

        generators.generateSpyglass(ModItems.ALIDADE);

        generateGroupItems(ModBlocks.SAPPHIRE_GROUP, generators);
        generateGroupItems(ModBlocks.CINNABAR_GROUP, generators);
        generateGroupItems(ModBlocks.PERIDOT_GROUP, generators);
        generateGroupItems(ModBlocks.TOPAZ_GROUP, generators);
        generateGroupItems(ModBlocks.SUNSTONE_GROUP, generators);
        generateGroupItems(ModBlocks.SELENITE_GROUP, generators);
        generateGroupItems(ModBlocks.TOURMALINE_GROUP, generators);




    }

    private static void generateGroupBlocks(CrystalBlockGroup group, BlockModelGenerators generators) {
        generators.createTrivialCube(group.block());
        generators.createTrivialCube(group.buddingBlock());
        generators.createAmethystCluster(group.cluster());
        generators.createAmethystCluster(group.largeBud());
        generators.createAmethystCluster(group.mediumBud());
        generators.createAmethystCluster(group.smallBud());
    }

    private static void generateGroupItems(CrystalBlockGroup group, ItemModelGenerators generators) {
        registerBlockItem(group.cluster(), CLUSTER_TEMPLATE, generators);
        registerBlockItem(group.largeBud(), LARGE_BUD_TEMPLATE, generators);
        registerBlockItem(group.mediumBud(), MEDIUM_BUD_TEMPLATE, generators);
        registerBlockItem(group.smallBud(), SMALL_BUD_TEMPLATE, generators);
    }

    private static void registerBlockItem(Block block, ModelTemplate template, ItemModelGenerators generator) {
        Identifier itemModel = template.create(block.asItem(), TextureMapping.singleSlot(TextureSlot.LAYER0, new Material(ModelLocationUtils.getModelLocation(block))), generator.modelOutput);
        generator.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(itemModel));
    }

    private static ModelTemplate item(String parent) {
        return new ModelTemplate(Optional.of(Caustics.id("item/" + parent)), Optional.empty(), TextureSlot.LAYER0);
    }
}
