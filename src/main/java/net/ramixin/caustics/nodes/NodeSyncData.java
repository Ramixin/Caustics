package net.ramixin.caustics.nodes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.ramixin.caustics.items.components.NetworkFrequency;

import java.util.List;

public record NodeSyncData(List<BlockPos> sapphirePositions, List<NetworkFrequency> frequencies) {

    public static final StreamCodec<FriendlyByteBuf, NodeSyncData> STREAM_CODEC = StreamCodec.of(NodeSyncData::write, NodeSyncData::new);

    public NodeSyncData(FriendlyByteBuf buf) {
        List<BlockPos> sapphirePositions = buf.readList(BlockPos.STREAM_CODEC);
        List<NetworkFrequency> frequencies = buf.readList(NetworkFrequency.STREAM_CODEC);
        this(sapphirePositions, frequencies);
    }

    private static void write(FriendlyByteBuf buf, NodeSyncData payload) {
        buf.writeCollection(payload.sapphirePositions, BlockPos.STREAM_CODEC);
        buf.writeCollection(payload.frequencies, NetworkFrequency.STREAM_CODEC);
    }
}
