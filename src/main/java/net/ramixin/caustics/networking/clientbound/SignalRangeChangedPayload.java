package net.ramixin.caustics.networking.clientbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.ramixin.caustics.Caustics;
import org.jspecify.annotations.NonNull;

public record SignalRangeChangedPayload(int newValue) implements CustomPacketPayload {

    public static final Type<SignalRangeChangedPayload> TYPE = new Type<>(Caustics.id("request_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SignalRangeChangedPayload> CODEC = StreamCodec.of(SignalRangeChangedPayload::write, SignalRangeChangedPayload::new);

    private SignalRangeChangedPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readInt());
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void write(RegistryFriendlyByteBuf buf, SignalRangeChangedPayload payload) {
        buf.writeInt(payload.newValue);
    }
}
