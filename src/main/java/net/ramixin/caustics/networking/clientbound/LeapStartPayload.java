package net.ramixin.caustics.networking.clientbound;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.ramixin.caustics.Caustics;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record LeapStartPayload(UUID player, long startTick, int maxTicks, BlockPos sapphirePos) implements CustomPacketPayload {

    public static final Type<LeapStartPayload> TYPE = new Type<>(Caustics.id("leap_start"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LeapStartPayload> CODEC = StreamCodec.ofMember(
            LeapStartPayload::write,
            LeapStartPayload::new
    );

    private LeapStartPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readLong(), buf.readInt(), buf.readBlockPos());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(player);
        buf.writeLong(startTick);
        buf.writeInt(maxTicks);
        buf.writeBlockPos(sapphirePos);
    }


    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
