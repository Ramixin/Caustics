package net.ramixin.caustics.items;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.items.components.NetworkFrequency;

import java.util.function.UnaryOperator;

public class ModDataComponents {

    public static final DataComponentType<NetworkFrequency> NETWORK_FREQUENCY = register("network_frequency", (builder) -> builder.persistent(NetworkFrequency.CODEC).networkSynchronized(NetworkFrequency.STREAM_CODEC));

    public static void onInitialize() {

    }

    private static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Caustics.id(id), builderOperator.apply(DataComponentType.builder()).build());
    }

}
