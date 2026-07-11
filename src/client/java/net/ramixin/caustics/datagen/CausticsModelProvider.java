package net.ramixin.caustics.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.blocks.ChargeClusterBlock;
import net.ramixin.caustics.blocks.CrystalBlockGroup;
import net.ramixin.caustics.blocks.ModBlocks;
import net.ramixin.caustics.items.ModItems;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

import static net.minecraft.client.data.models.BlockModelGenerators.*;

public class CausticsModelProvider extends FabricModelProvider {

    private static final ModelTemplate CLUSTER_TEMPLATE = item("cluster");
    private static final ModelTemplate LARGE_BUD_TEMPLATE = item("large_bud");
    private static final ModelTemplate MEDIUM_BUD_TEMPLATE = item("medium_bud");
    private static final ModelTemplate SMALL_BUD_TEMPLATE = item("small_bud");

    private static final PropertyDispatch<VariantMutator> ROTATIONS_COLUMN_WITH_FACING = PropertyDispatch.modify(BlockStateProperties.FACING).select(Direction.DOWN, X_ROT_180).select(Direction.UP, NOP).select(Direction.NORTH, X_ROT_90).select(Direction.SOUTH, X_ROT_90.then(Y_ROT_180)).select(Direction.WEST, X_ROT_90.then(Y_ROT_270)).select(Direction.EAST, X_ROT_90.then(Y_ROT_90));

    public CausticsModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(@NonNull BlockModelGenerators generators) {
        generateGroupBlocks(ModBlocks.SAPPHIRE_GROUP, generators, false);
        generateGroupBlocks(ModBlocks.BERYL_GROUP, generators, false);
        generateGroupBlocks(ModBlocks.PERIDOT_GROUP, generators, false);
        generateGroupBlocks(ModBlocks.TOPAZ_GROUP, generators, false);
        generateGroupBlocks(ModBlocks.SUNSTONE_GROUP, generators, false);
        generateGroupBlocks(ModBlocks.SELENITE_GROUP, generators, true);
        generateGroupBlocks(ModBlocks.TOURMALINE_GROUP, generators, false);

        Block selenite = ModBlocks.SELENITE_GROUP.cluster();
        TextureMapping chargedTextures = TextureMapping.cross(selenite);
        TextureMapping dischargedTextures = TextureMapping.cross(TextureMapping.getBlockTexture(selenite, "_discharged"));
        MultiVariant wallModelOn = plainVariant(ModelTemplates.CROSS.create(selenite, chargedTextures, generators.modelOutput));
        MultiVariant wallModelOff = plainVariant(ModelTemplates.CROSS.createWithSuffix(selenite, "_discharged", dischargedTextures, generators.modelOutput));
        generators.blockStateOutput.accept(MultiVariantGenerator.dispatch(selenite).with(createBooleanModelDispatch(ChargeClusterBlock.CHARGED, wallModelOn, wallModelOff)).with(ROTATIONS_COLUMN_WITH_FACING));
    }

    @Override
    public void generateItemModels(@NonNull ItemModelGenerators generators) {

        generators.generateFlatItem(ModItems.SAPPHIRE_SHARD, ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(ModItems.BERYL_SHARD, ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(ModItems.PERIDOT_SHARD, ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(ModItems.TOPAZ_SHARD, ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(ModItems.SUNSTONE_SHARD, ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(ModItems.SELENITE_SHARD, ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(ModItems.TOURMALINE_SHARD, ModelTemplates.FLAT_ITEM);

        generators.generateSpyglass(ModItems.TUNING_FORK);

        generateGroupItems(ModBlocks.SAPPHIRE_GROUP, generators);
        generateGroupItems(ModBlocks.BERYL_GROUP, generators);
        generateGroupItems(ModBlocks.PERIDOT_GROUP, generators);
        generateGroupItems(ModBlocks.TOPAZ_GROUP, generators);
        generateGroupItems(ModBlocks.SUNSTONE_GROUP, generators);
        generateGroupItems(ModBlocks.SELENITE_GROUP, generators);
        generateGroupItems(ModBlocks.TOURMALINE_GROUP, generators);

    }

    private static void generateGroupBlocks(CrystalBlockGroup group, BlockModelGenerators generators, boolean skipCluster) {
        generators.createTrivialCube(group.block());
        generators.createTrivialCube(group.buddingBlock());
        if(!skipCluster)
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
