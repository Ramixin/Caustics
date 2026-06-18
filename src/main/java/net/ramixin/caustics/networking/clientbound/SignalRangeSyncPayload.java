package net.ramixin.caustics.networking.clientbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.ramixin.caustics.Caustics;
import org.jspecify.annotations.NonNull;

public record SignalRangeSyncPayload(int newValue) implements CustomPacketPayload {

    public static final Type<SignalRangeSyncPayload> TYPE = new Type<>(Caustics.id("request_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SignalRangeSyncPayload> CODEC = StreamCodec.ofMember(SignalRangeSyncPayload::write, SignalRangeSyncPayload::new);

    private SignalRangeSyncPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readInt());
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(newValue);
    }
}
