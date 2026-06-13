package net.ramixin.caustics.networking.clientbound;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.items.components.Frequency;
import net.ramixin.caustics.nodes.NodeSyncData;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

public record NetworkSyncPayload(List<NodeSyncData> nodeData, Map<BlockPos, Frequency> frequencies) implements CustomPacketPayload {

    public static final Type<NetworkSyncPayload> TYPE = new Type<>(Caustics.id("network_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkSyncPayload> CODEC = StreamCodec.of(
            NetworkSyncPayload::write,
            NetworkSyncPayload::new
    );

    public NetworkSyncPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readList(NodeSyncData.STREAM_CODEC), buf.readMap(BlockPos.STREAM_CODEC, Frequency.STREAM_CODEC));
    }

    private static void write(RegistryFriendlyByteBuf buf, NetworkSyncPayload payload) {
        buf.writeCollection(payload.nodeData(), NodeSyncData.STREAM_CODEC);
        buf.writeMap(payload.frequencies(), BlockPos.STREAM_CODEC, Frequency.STREAM_CODEC);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
