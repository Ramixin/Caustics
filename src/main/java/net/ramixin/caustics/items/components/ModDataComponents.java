package net.ramixin.caustics.items.components;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.ramixin.caustics.Caustics;

import java.util.function.UnaryOperator;

public class ModDataComponents {

    public static final DataComponentType<Frequency> NETWORK_FREQUENCY = register("network_frequency", (builder) -> builder.persistent(Frequency.CODEC).networkSynchronized(Frequency.STREAM_CODEC));

    public static void onInitialize() {

    }

    private static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Caustics.id(id), builderOperator.apply(DataComponentType.builder()).build());
    }

}
