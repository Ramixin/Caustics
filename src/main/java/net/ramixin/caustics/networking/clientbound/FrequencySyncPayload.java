package net.ramixin.caustics.networking.clientbound;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.items.components.Frequency;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public record FrequencySyncPayload(Map<BlockPos, Frequency> frequencies, Map<Frequency, String> frequencyNames) implements CustomPacketPayload {

    public static final Type<FrequencySyncPayload> TYPE = new Type<>(Caustics.id("frequency_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FrequencySyncPayload> CODEC = StreamCodec.ofMember(
            FrequencySyncPayload::write,
            FrequencySyncPayload::new
    );

    private FrequencySyncPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readMap(BlockPos.STREAM_CODEC, Frequency.STREAM_CODEC), buf.readMap(Frequency.STREAM_CODEC, FriendlyByteBuf::readUtf));
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeMap(frequencies, BlockPos.STREAM_CODEC, Frequency.STREAM_CODEC);
        buf.writeMap(frequencyNames, Frequency.STREAM_CODEC, FriendlyByteBuf::writeUtf);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
