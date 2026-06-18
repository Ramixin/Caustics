package net.ramixin.caustics;

import net.fabricmc.fabric.api.biome.v1.BiomeModification;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class ModFeatures {

    public interface Placed {

        ResourceKey<PlacedFeature> SAPPHIRE_GEODE = getKey("sapphire");
        ResourceKey<PlacedFeature> BERYL_GEODE = getKey("beryl");
        ResourceKey<PlacedFeature> PERIDOT_GEODE = getKey("peridot");
        ResourceKey<PlacedFeature> TOPAZ_GEODE = getKey("topaz");
        ResourceKey<PlacedFeature> SUNSTONE_GEODE = getKey("sunstone");
        ResourceKey<PlacedFeature> SELENITE_GEODE = getKey("selenite");
        ResourceKey<PlacedFeature> TOURMALINE_GEODE = getKey("tourmaline");

        private static ResourceKey<PlacedFeature> getKey(String crystal) {
            return ResourceKey.create(Registries.PLACED_FEATURE, Caustics.id(crystal + "_geode"));
        }
    }

    public static void onInitialize() {

        BiomeModification modification = BiomeModifications.create(Caustics.id("geodes"));

        modification.add(
                ModificationPhase.ADDITIONS,
                BiomeSelectors.foundInOverworld(),
                (_, modContext) -> {
                    modContext.getGenerationSettings().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Placed.SAPPHIRE_GEODE);
                    modContext.getGenerationSettings().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Placed.BERYL_GEODE);
                    modContext.getGenerationSettings().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Placed.PERIDOT_GEODE);
                    modContext.getGenerationSettings().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Placed.TOPAZ_GEODE);
                    modContext.getGenerationSettings().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Placed.SUNSTONE_GEODE);
                    modContext.getGenerationSettings().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Placed.SELENITE_GEODE);
                    modContext.getGenerationSettings().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Placed.TOURMALINE_GEODE);
                }
        );

    }



}
