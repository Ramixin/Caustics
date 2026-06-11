package net.ramixin.caustics.nodes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.ramixin.caustics.items.components.NetworkFrequency;

import java.util.List;
import java.util.Optional;

public record NodeSyncData(List<BlockPos> sapphirePositions, List<NetworkFrequency> frequencies, Optional<String> name) {

    public static final StreamCodec<FriendlyByteBuf, NodeSyncData> STREAM_CODEC = StreamCodec.of(NodeSyncData::write, NodeSyncData::new);

    public NodeSyncData(FriendlyByteBuf buf) {
        List<BlockPos> sapphirePositions = buf.readList(BlockPos.STREAM_CODEC);
        List<NetworkFrequency> frequencies = buf.readList(NetworkFrequency.STREAM_CODEC);
        Optional<String> name = buf.readOptional(FriendlyByteBuf::readUtf);
        this(sapphirePositions, frequencies, name);
    }

    private static void write(FriendlyByteBuf buf, NodeSyncData payload) {
        buf.writeCollection(payload.sapphirePositions, BlockPos.STREAM_CODEC);
        buf.writeCollection(payload.frequencies, NetworkFrequency.STREAM_CODEC);
        buf.writeOptional(payload.name, FriendlyByteBuf::writeUtf);
    }
}
