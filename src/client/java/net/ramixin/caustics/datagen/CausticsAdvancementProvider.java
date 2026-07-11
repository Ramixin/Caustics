package net.ramixin.caustics.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.ModTags;
import net.ramixin.caustics.items.components.ModDataComponents;
import net.ramixin.caustics.items.components.predicates.SpyglassLensPredicate;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class CausticsAdvancementProvider extends FabricAdvancementProvider {

    protected CausticsAdvancementProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(HolderLookup.@NonNull Provider registryLookup, @NonNull Consumer<AdvancementHolder> consumer) {
        HolderLookup<EntityType<?>> entityTypes = registryLookup.lookupOrThrow(Registries.ENTITY_TYPE);
        HolderLookup<Item> items = registryLookup.lookupOrThrow(Registries.ITEM);

        Advancement.Builder.advancement()
                .display(
                        Items.SPYGLASS,
                        Component.translatable("advancements.caustics.alidade_at_player.title"),
                        Component.translatable("advancements.caustics.alidade_at_player.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("look_at_player", UsingItemTrigger.TriggerInstance.lookingAt(
                        EntityPredicate.Builder.entity().subPredicate(PlayerPredicate.Builder.player().setLookingAt(
                                EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(entityTypes, EntityType.PLAYER))
                        ).build()),  ItemPredicate.Builder.item().of(items, Items.SPYGLASS).withComponents(
                                DataComponentMatchers.Builder.components().partial(
                                        ModDataComponents.SPYGLASS_LENS_PREDICATE,
                                        new SpyglassLensPredicate(ModTags.Items.ALIDADE_LENS)
                                ).build()
                        )
                ))
                .parent(createPlaceholder(Identifier.withDefaultNamespace("adventure/spyglass_at_parrot")))
                .save(consumer, Caustics.id("alidade_at_player"));
    }
}
