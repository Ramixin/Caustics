package net.ramixin.caustics;

import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface CodecUtils {

    Codec<UUID> UUID_STRING_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);


    static <K, V> Codec<Map<V, K>> invertMap(Codec<Map<K, V>> codec) {
        return codec.xmap(
                kvMap -> {
                    Map<V, K> inverted = new HashMap<>();
                    for(Map.Entry<K, V> entry : kvMap.entrySet()) {
                        inverted.put(entry.getValue(), entry.getKey());
                    }
                    return inverted;
                },
                vkMap -> {
                    Map<K, V> reverted = new HashMap<>();
                    for(Map.Entry<V, K> entry : vkMap.entrySet()) {
                        reverted.put(entry.getValue(), entry.getKey());
                    }
                    return reverted;
                }
        );
    }
}
