package net.ramixin.caustics.networking.clientbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.ramixin.caustics.Caustics;
import org.jspecify.annotations.NonNull;

public record LeapResultPayload(Status status) implements CustomPacketPayload {

    public static final Type<LeapResultPayload> TYPE = new Type<>(Caustics.id("leap_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LeapResultPayload> CODEC = StreamCodec.ofMember(
            LeapResultPayload::write,
            LeapResultPayload::new
    );

    private LeapResultPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readEnum(Status.class));
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(status);
    }

    public static LeapResultPayload createSuccess() {
        return new LeapResultPayload(Status.SUCCESS);
    }

    public static LeapResultPayload createFailure() {
        return new LeapResultPayload(Status.FAILURE);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private enum Status {
        SUCCESS,
        FAILURE
    }

}
