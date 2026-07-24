package net.ramixin.caustics.networking.serverbound;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.items.components.Frequency;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record FrequencyChangePayload(BlockPos pos, Optional<Frequency> frequency) implements CustomPacketPayload {

    public static final Type<FrequencyChangePayload> TYPE = new Type<>(Caustics.id("frequency_change"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FrequencyChangePayload> CODEC = StreamCodec.ofMember(FrequencyChangePayload::write, FrequencyChangePayload::new);

    private FrequencyChangePayload(RegistryFriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readOptional(Frequency.STREAM_CODEC));
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeOptional(frequency, Frequency.STREAM_CODEC);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
