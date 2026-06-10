package net.ramixin.caustics.networking.serverbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.ramixin.caustics.Caustics;
import org.jspecify.annotations.NonNull;

public class RequestSyncPayload implements CustomPacketPayload {

    public static final Type<RequestSyncPayload> TYPE = new Type<>(Caustics.id("request_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestSyncPayload> CODEC = StreamCodec.unit(new RequestSyncPayload());

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
