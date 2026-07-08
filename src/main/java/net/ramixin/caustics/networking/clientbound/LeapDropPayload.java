package net.ramixin.caustics.networking.clientbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.ramixin.caustics.Caustics;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record LeapDropPayload(UUID player) implements CustomPacketPayload {

    public static final Type<LeapDropPayload> TYPE = new Type<>(Caustics.id("leap_drop"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LeapDropPayload> CODEC = StreamCodec.ofMember(
            LeapDropPayload::write,
            LeapDropPayload::new
    );

    private LeapDropPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readUUID());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(player);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
