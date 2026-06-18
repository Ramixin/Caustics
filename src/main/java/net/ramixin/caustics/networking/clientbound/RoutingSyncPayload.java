package net.ramixin.caustics.networking.clientbound;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.nodes.routing.RoutingTable;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public record RoutingSyncPayload(Map<BlockPos, RoutingTable> routingTables) implements CustomPacketPayload {

    public static final Type<RoutingSyncPayload> TYPE = new Type<>(Caustics.id("frequency_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RoutingSyncPayload> CODEC = StreamCodec.ofMember(
            RoutingSyncPayload::write,
            RoutingSyncPayload::new
    );

    private RoutingSyncPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readMap(BlockPos.STREAM_CODEC, RoutingTable.STREAM_CODEC));
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeMap(routingTables, BlockPos.STREAM_CODEC, RoutingTable.STREAM_CODEC);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
