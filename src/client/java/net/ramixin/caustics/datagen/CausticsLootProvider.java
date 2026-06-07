package net.ramixin.caustics.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.ramixin.caustics.ModItems;
import net.ramixin.caustics.blocks.CrystalBlockGroup;
import net.ramixin.caustics.blocks.ModBlocks;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CausticsLootProvider extends FabricBlockLootSubProvider {

    protected CausticsLootProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(packOutput, registriesFuture);
    }

    @Override
    public void generate() {
        generateGroup(ModBlocks.SAPPHIRE_GROUP, ModItems.SAPPHIRE_SHARD);
        generateGroup(ModBlocks.CINNABAR_GROUP, ModItems.CINNABAR_SHARD);
        generateGroup(ModBlocks.PERIDOT_GROUP, ModItems.PERIDOT_SHARD);
        generateGroup(ModBlocks.TOPAZ_GROUP, ModItems.TOPAZ_SHARD);
        generateGroup(ModBlocks.SUNSTONE_GROUP, ModItems.SUNSTONE_SHARD);
        generateGroup(ModBlocks.SELENITE_GROUP, ModItems.SELENITE_SHARD);
        generateGroup(ModBlocks.TOURMALINE_GROUP, ModItems.TOURMALINE_SHARD);
    }

    private void generateGroup(CrystalBlockGroup group, Item shard) {
        dropSelf(group.block());
        dropWhenSilkTouch(group.largeBud());
        dropWhenSilkTouch(group.mediumBud());
        dropWhenSilkTouch(group.smallBud());
        add(group.cluster(), LootTable.lootTable().withPool(LootPool.lootPool().add(
                LootItem.lootTableItem(group.cluster()).when(
                        MatchTool.toolMatches(ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components().partial(DataComponentPredicates.ENCHANTMENTS, EnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate(registries.get(Enchantments.SILK_TOUCH).orElseThrow(), MinMaxBounds.Ints.atLeast(1))))).build()))
                        ).otherwise(
                                LootItem.lootTableItem(shard).when(
                                        MatchTool.toolMatches(ItemPredicate.Builder.item().of(registries.lookupOrThrow(Registries.ITEM), ItemTags.CLUSTER_MAX_HARVESTABLES))
                                ).apply(
                                        SetItemCountFunction.setCount(new ConstantValue(4))
                                ).apply(
                                        ApplyBonusCount.addOreBonusCount(registries.get(Enchantments.SILK_TOUCH).orElseThrow())
                                ).otherwise(LootItem.lootTableItem(shard)
                                        .apply(
                                                SetItemCountFunction.setCount(new ConstantValue(2))
                                        ).apply(
                                                ApplyExplosionDecay.explosionDecay()
                                        )
                                )
                )
                ))
        );


    }
}
