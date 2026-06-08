package net.ramixin.caustics.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record Frequency(String network, String node) {

    public static final Codec<Frequency> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("network").forGetter(Frequency::network),
            Codec.STRING.fieldOf("node").forGetter(Frequency::node)
    ).apply(instance, Frequency::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Frequency> STREAM_CODEC = StreamCodec.of(
            (buf, frequency) -> {
                buf.writeUtf(frequency.network());
                buf.writeUtf(frequency.node());
            },
            (buf) -> new Frequency(buf.readUtf(), buf.readUtf())
    );

}
