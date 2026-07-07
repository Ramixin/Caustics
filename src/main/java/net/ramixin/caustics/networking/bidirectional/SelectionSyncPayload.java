package net.ramixin.caustics.networking.bidirectional;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.ramixin.caustics.Caustics;
import org.jspecify.annotations.NonNull;

public record SelectionSyncPayload(BlockPos sapphirePos, BlockPos peridotPos) implements CustomPacketPayload {

    public static final Type<SelectionSyncPayload> TYPE = new Type<>(Caustics.id("selection_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectionSyncPayload> CODEC = StreamCodec.ofMember(SelectionSyncPayload::write, SelectionSyncPayload::new);

    private SelectionSyncPayload(RegistryFriendlyByteBuf buf) {
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
