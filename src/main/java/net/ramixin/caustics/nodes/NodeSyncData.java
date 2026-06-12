package net.ramixin.caustics.nodes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.ramixin.caustics.items.components.NetworkFrequency;

import java.util.List;
import java.util.Map;

public record NodeSyncData(List<BlockPos> sapphirePositions, List<BlockPos> topazPositions, List<NetworkFrequency> frequencies, Map<BlockPos, String> clusterNames) {

    public static final StreamCodec<FriendlyByteBuf, NodeSyncData> STREAM_CODEC = StreamCodec.of(NodeSyncData::write, NodeSyncData::new);

    public NodeSyncData(FriendlyByteBuf buf) {
        List<BlockPos> sapphirePositions = buf.readList(BlockPos.STREAM_CODEC);
        List<BlockPos> routerPositions = buf.readList(BlockPos.STREAM_CODEC);
        List<NetworkFrequency> frequencies = buf.readList(NetworkFrequency.STREAM_CODEC);
        Map<BlockPos, String> names = buf.readMap(BlockPos.STREAM_CODEC, FriendlyByteBuf::readUtf);
        this(sapphirePositions, routerPositions, frequencies, names);
    }

    private static void write(FriendlyByteBuf buf, NodeSyncData payload) {
        buf.writeCollection(payload.sapphirePositions, BlockPos.STREAM_CODEC);
        buf.writeCollection(payload.topazPositions, BlockPos.STREAM_CODEC);
        buf.writeCollection(payload.frequencies, NetworkFrequency.STREAM_CODEC);
        buf.writeMap(payload.clusterNames, BlockPos.STREAM_CODEC, FriendlyByteBuf::writeUtf);
    }
}
