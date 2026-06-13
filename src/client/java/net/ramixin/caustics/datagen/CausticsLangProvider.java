package net.ramixin.caustics.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.ramixin.caustics.blocks.CrystalBlockGroup;
import net.ramixin.caustics.blocks.ModBlocks;
import net.ramixin.caustics.items.ModItems;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class CausticsLangProvider extends FabricLanguageProvider {

    protected CausticsLangProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(packOutput, registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.@NonNull Provider registryLookup, @NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add(ModItems.SAPPHIRE_SHARD, "Sapphire Shard");
        translationBuilder.add(ModItems.CINNABAR_SHARD, "Cinnabar Shard");
        translationBuilder.add(ModItems.PERIDOT_SHARD, "Peridot Shard");
        translationBuilder.add(ModItems.TOPAZ_SHARD, "Topaz Shard");
        translationBuilder.add(ModItems.SUNSTONE_SHARD, "Sunstone Shard");
        translationBuilder.add(ModItems.SELENITE_SHARD, "Selenite Shard");
        translationBuilder.add(ModItems.TOURMALINE_SHARD, "Tourmaline Shard");

        generateGroupTranslations("Sapphire", ModBlocks.SAPPHIRE_GROUP, translationBuilder);
        generateGroupTranslations("Cinnabar", ModBlocks.CINNABAR_GROUP, translationBuilder);
        generateGroupTranslations("Peridot", ModBlocks.PERIDOT_GROUP, translationBuilder);
        generateGroupTranslations("Topaz", ModBlocks.TOPAZ_GROUP, translationBuilder);
        generateGroupTranslations("Sunstone", ModBlocks.SUNSTONE_GROUP, translationBuilder);
        generateGroupTranslations("Selenite", ModBlocks.SELENITE_GROUP, translationBuilder);
        generateGroupTranslations("Tourmaline", ModBlocks.TOURMALINE_GROUP, translationBuilder);

        translationBuilder.add(ModItems.ALIDADE, "Alidade");
        translationBuilder.add(ModBlocks.MIRROR, "Mirror");

        translationBuilder.add("caustics.network_frequency.tooltip", "Tuned Frequency:");
        translationBuilder.add("caustics.node.frequencies", "Node Networks:");
        translationBuilder.add("caustics.node.deposit", "Target Deposit:");
    }

    private static void generateGroupTranslations(String name, CrystalBlockGroup group, TranslationBuilder translationBuilder) {
        translationBuilder.add(group.block(), "Block of " + name);
        translationBuilder.add(group.buddingBlock(), "Budding " + name);
        translationBuilder.add(group.cluster(), name + " Cluster");
        translationBuilder.add(group.largeBud(), "Large " + name + " Bud");
        translationBuilder.add(group.mediumBud(), "Medium " + name + " Bud");
        translationBuilder.add(group.smallBud(), "Small " + name + " Bud");
    }
}
