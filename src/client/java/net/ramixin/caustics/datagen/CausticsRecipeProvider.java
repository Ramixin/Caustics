package net.ramixin.caustics.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.blocks.ModBlocks;
import net.ramixin.caustics.items.ModItems;
import net.ramixin.caustics.items.components.ModDataComponents;
import net.ramixin.caustics.items.components.SpyglassLens;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class CausticsRecipeProvider extends FabricRecipeProvider {

    public CausticsRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected @NonNull RecipeProvider createRecipeProvider(HolderLookup.@NonNull Provider registries, @NonNull RecipeOutput output) {
        return new RecipeProvider(registries, output) {
            @Override
            public void buildRecipes() {

                twoByTwoPacker(RecipeCategory.DECORATIONS, ModBlocks.SAPPHIRE_GROUP.block(), ModItems.SAPPHIRE_SHARD);
                twoByTwoPacker(RecipeCategory.DECORATIONS, ModBlocks.BERYL_GROUP.block(), ModItems.BERYL_SHARD);
                twoByTwoPacker(RecipeCategory.DECORATIONS, ModBlocks.PERIDOT_GROUP.block(), ModItems.PERIDOT_SHARD);
                twoByTwoPacker(RecipeCategory.DECORATIONS, ModBlocks.TOPAZ_GROUP.block(), ModItems.TOPAZ_SHARD);
                twoByTwoPacker(RecipeCategory.DECORATIONS, ModBlocks.SUNSTONE_GROUP.block(), ModItems.SUNSTONE_SHARD);
                twoByTwoPacker(RecipeCategory.DECORATIONS, ModBlocks.SELENITE_GROUP.block(), ModItems.SELENITE_SHARD);
                twoByTwoPacker(RecipeCategory.DECORATIONS, ModBlocks.TOURMALINE_GROUP.block(), ModItems.TOURMALINE_SHARD);

                shaped(RecipeCategory.MISC, ModBlocks.MIRROR, 2)
                        .pattern(" T ")
                        .pattern("SDS")
                        .define('T', Blocks.TINTED_GLASS)
                        .define('S', Items.STICK)
                        .define('D', Blocks.POLISHED_DEEPSLATE_SLAB)
                        .unlockedBy("has_glass", has(Blocks.TINTED_GLASS))
                        .save(output, getKey("mirror"));

                createSpyglassRecipe(this, ModItems.BERYL_SHARD, "beryl").save(output, getKey("alidade"));
                createSpyglassRecipe(this, ModItems.TOURMALINE_SHARD, "tourmaline").save(output, getKey("dowser"));
                createSpyglassRecipe(this, ModItems.SUNSTONE_SHARD, "sunstone").save(output, getKey("collimator"));
            }
        };
    }

    private static ShapedRecipeBuilder createSpyglassRecipe(RecipeProvider provider, Item crystal, String crystal_name) {
            return new ShapedRecipeBuilder(
                    BuiltInRegistries.ITEM,
                    RecipeCategory.TOOLS,
                    new ItemStackTemplate(Items.SPYGLASS, DataComponentPatch.builder().set(
                            ModDataComponents.SPYGLASS_LENS,
                            new SpyglassLens(BuiltInRegistries.ITEM.wrapAsHolder(crystal))
                    ).build()))
                    .pattern(" # ")
                    .pattern(" C ")
                    .pattern(" C ")
                    .define('#', crystal)
                    .define('C', Items.COPPER_INGOT)
                    .unlockedBy("has_"+ crystal_name, provider.has(crystal));
    }

    private static ResourceKey<Recipe<?>> getKey(String name) {
        return ResourceKey.create(Registries.RECIPE, Caustics.id(name));
    }

    @Override
    public @NonNull String getName() {
        return "Caustics Recipe Provider";
    }
}
