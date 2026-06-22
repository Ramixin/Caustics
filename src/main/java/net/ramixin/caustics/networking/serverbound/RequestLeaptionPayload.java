package net.ramixin.caustics.networking.serverbound;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.ramixin.caustics.Caustics;
import org.jspecify.annotations.NonNull;

public record RequestLeaptionPayload(BlockPos sapphirePos, BlockPos peridotPos) implements CustomPacketPayload {

    public static final Type<RequestLeaptionPayload> TYPE = new Type<>(Caustics.id("request_leaption"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestLeaptionPayload> CODEC = StreamCodec.ofMember(RequestLeaptionPayload::write, RequestLeaptionPayload::new);

    private RequestLeaptionPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readBlockPos());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(sapphirePos);
        buf.writeBlockPos(peridotPos);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
