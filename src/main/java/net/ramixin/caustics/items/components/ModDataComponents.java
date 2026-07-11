package net.ramixin.caustics.items.components;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.items.components.predicates.SpyglassLensPredicate;

import java.util.function.UnaryOperator;

public class ModDataComponents {

    public static final DataComponentType<Frequency> FREQUENCY = register("frequency", (builder) -> builder.persistent(Frequency.CODEC).networkSynchronized(Frequency.STREAM_CODEC));
    public static final DataComponentType<LeaperMaterial> LEAPER_MATERIAL = register("leaper_material", (builder) -> builder.persistent(LeaperMaterial.CODEC).networkSynchronized(LeaperMaterial.STREAM_CODEC));
    public static final DataComponentType<LeaperCharge> LEAPER_CHARGE = register("leaper_charge", (builder) -> builder.persistent(LeaperCharge.CODEC).networkSynchronized(LeaperCharge.STREAM_CODEC));
    public static final DataComponentType<SpyglassLens> SPYGLASS_LENS = register("spyglass_lens", (builder) -> builder.persistent(SpyglassLens.CODEC).networkSynchronized(SpyglassLens.STREAM_CODEC));

    public static final DataComponentPredicate.Type<SpyglassLensPredicate> SPYGLASS_LENS_PREDICATE = registerPredicate("spyglass_lens_predicate", SpyglassLensPredicate.CODEC);

    public static void onInitialize() {
        ItemComponentTooltipProviderRegistry.addLast(ModDataComponents.FREQUENCY);
        ItemComponentTooltipProviderRegistry.addLast(ModDataComponents.LEAPER_MATERIAL);
        ItemComponentTooltipProviderRegistry.addLast(ModDataComponents.LEAPER_CHARGE);
        ItemComponentTooltipProviderRegistry.addLast(ModDataComponents.SPYGLASS_LENS);

        DefaultItemComponentEvents.MODIFY.register(context ->
                context.modify(Items.SPYGLASS, builder ->
                        builder.set(SPYGLASS_LENS, new SpyglassLens(BuiltInRegistries.ITEM.wrapAsHolder(Items.AMETHYST_SHARD)))
                )
        );
    }

    private static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Caustics.id(id), builderOperator.apply(DataComponentType.builder()).build());
    }

    private static <T extends DataComponentPredicate> DataComponentPredicate.Type<T> registerPredicate(String id, Codec<T> codec) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE, id, new DataComponentPredicate.ConcreteType<>(codec));
    }

}
