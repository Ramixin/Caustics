package net.ramixin.caustics.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.registries.ModRegistries;

import java.util.HashSet;
import java.util.Set;

public interface MixsonUtil {

    static Set<Identifier> getViableHandles() {
        return filterKeys(ModRegistries.HANDLE.keySet(), "handle");
    }

    static Set<Identifier> getViableDecorations() {
        return filterKeys(ModRegistries.DECORATION.keySet(), "decoration");
    }

    private static Set<Identifier> filterKeys(Set<Identifier> set, String registryName) {
        Set<Identifier> filtered = new HashSet<>();
        for(Identifier key : set) {
            if(!BuiltInRegistries.ITEM.containsKey(key)) Caustics.LOGGER.warn("Unknown {} item: {}", registryName, key);
            else filtered.add(key);
        }
        return filtered;
    }

    static Identifier createId(String prefix, Identifier handle, Identifier decoration, boolean hasCore) {
        return Caustics.id(String.format("%sleaper_%s_%s_%s", prefix, handle, decoration, hasCore).replace(":", "_"));
    }

}
