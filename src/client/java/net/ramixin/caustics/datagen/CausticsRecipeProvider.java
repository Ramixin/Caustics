package net.ramixin.caustics.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.blocks.ModBlocks;
import net.ramixin.caustics.items.ModItems;
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

                shaped(RecipeCategory.TOOLS, ModItems.TUNING_FORK)
                        .pattern("A A")
                        .pattern("ACA")
                        .pattern(" S ")
                        .define('A', Items.AMETHYST_SHARD)
                        .define('C', Items.COPPER_INGOT)
                        .define('S', Items.STICK)
                        .unlockedBy("has_iron", has(Items.AMETHYST_SHARD))
                        .save(output, getKey("tuning_fork"));

                shaped(RecipeCategory.TOOLS, ModItems.ALIDADE)
                        .pattern(" C ")
                        .pattern(" G ")
                        .pattern(" G ")
                        .define('C', ModItems.BERYL_SHARD)
                        .define('G', Items.GOLD_INGOT)
                        .unlockedBy("has_beryl", has(ModItems.BERYL_SHARD))
                        .save(output, getKey("alidade"));

            }
        };
    }

    private static ResourceKey<Recipe<?>> getKey(String name) {
        return ResourceKey.create(Registries.RECIPE, Caustics.id(name));
    }

    @Override
    public @NonNull String getName() {
        return "Caustics Recipe Provider";
    }
}
