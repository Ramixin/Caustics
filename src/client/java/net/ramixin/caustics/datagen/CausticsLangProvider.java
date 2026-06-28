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
        translationBuilder.add(ModItems.BERYL_SHARD, "Beryl Shard");
        translationBuilder.add(ModItems.PERIDOT_SHARD, "Peridot Shard");
        translationBuilder.add(ModItems.TOPAZ_SHARD, "Topaz Shard");
        translationBuilder.add(ModItems.SUNSTONE_SHARD, "Sunstone Shard");
        translationBuilder.add(ModItems.SELENITE_SHARD, "Selenite Shard");
        translationBuilder.add(ModItems.TOURMALINE_SHARD, "Tourmaline Shard");

        generateGroupTranslations("Sapphire", ModBlocks.SAPPHIRE_GROUP, translationBuilder);
        generateGroupTranslations("Beryl", ModBlocks.BERYL_GROUP, translationBuilder);
        generateGroupTranslations("Peridot", ModBlocks.PERIDOT_GROUP, translationBuilder);
        generateGroupTranslations("Topaz", ModBlocks.TOPAZ_GROUP, translationBuilder);
        generateGroupTranslations("Sunstone", ModBlocks.SUNSTONE_GROUP, translationBuilder);
        generateGroupTranslations("Selenite", ModBlocks.SELENITE_GROUP, translationBuilder);
        generateGroupTranslations("Tourmaline", ModBlocks.TOURMALINE_GROUP, translationBuilder);

        translationBuilder.add(ModItems.ALIDADE, "Alidade");
        translationBuilder.add(ModBlocks.MIRROR, "Mirror");
        translationBuilder.add(ModItems.LEAPER, "Leaper");

        translationBuilder.add("caustics.frequency.tooltip_header", "Tuned Frequency:");
        translationBuilder.add("caustics.frequency.unnamed", "Unnamed Frequency");

        translationBuilder.add("caustics.leaper_matieral.tooltip_charge_up", "Charge Up: %s");
        translationBuilder.add("caustics.leaper_matieral.tooltip_cooldown", "Cooldown: %s");

        translationBuilder.add("caustics.node.unknown_travel", "Unknown Node");
        translationBuilder.add("caustics.node.unnamed_travel", "Unnamed Node");
        translationBuilder.add("caustics.node.scroll_oob", "ScrollPos is OOB");
        translationBuilder.add("caustics.node.unknown_deposit", "Unknown Deposit");
        translationBuilder.add("caustics.node.unnamed_deposit", "Unnamed Deposit");
        translationBuilder.add("caustics.node.frequencies", "Node Networks:");
        translationBuilder.add("caustics.node.deposit", "Target Deposit:");
        translationBuilder.add("caustics.node.route_start", "Route:");
        translationBuilder.add("caustics.node.route_direct", "Direct Connection");
        translationBuilder.add("caustics.node.name", "Identified Node:");
        translationBuilder.add("caustics.node.selected", "Selected Node:");
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
