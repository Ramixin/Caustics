package net.ramixin.caustics.networking.clientbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.nodes.NodeSyncData;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record NodeSyncPayload(List<NodeSyncData> nodeData) implements CustomPacketPayload {

    public static final Type<NodeSyncPayload> TYPE = new Type<>(Caustics.id("network_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NodeSyncPayload> CODEC = StreamCodec.ofMember(
            NodeSyncPayload::write,
            NodeSyncPayload::new
    );

    public NodeSyncPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readList(NodeSyncData.STREAM_CODEC));
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeCollection(nodeData, NodeSyncData.STREAM_CODEC);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
