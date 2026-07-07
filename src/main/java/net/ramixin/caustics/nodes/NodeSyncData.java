package net.ramixin.caustics.nodes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record NodeSyncData(List<BlockPos> sapphirePositions, List<BlockPos> topazPositions, List<BlockPos> peridotPositions, List<BlockPos> sunstonePositions, List<BlockPos> tourmalinePositions, boolean visible) {

    public static final StreamCodec<FriendlyByteBuf, NodeSyncData> STREAM_CODEC = StreamCodec.of(NodeSyncData::write, NodeSyncData::new);

    public NodeSyncData(FriendlyByteBuf buf) {
        List<BlockPos> sapphirePositions = buf.readList(BlockPos.STREAM_CODEC);
        List<BlockPos> routerPositions = buf.readList(BlockPos.STREAM_CODEC);
        List<BlockPos> peridotPositions = buf.readList(BlockPos.STREAM_CODEC);
        List<BlockPos> sunstonePositions = buf.readList(BlockPos.STREAM_CODEC);
        List<BlockPos> tourmalinePositions = buf.readList(BlockPos.STREAM_CODEC);
        this(sapphirePositions, routerPositions, peridotPositions, sunstonePositions, tourmalinePositions, buf.readBoolean());
    }

    private static void write(FriendlyByteBuf buf, NodeSyncData payload) {
        buf.writeCollection(payload.sapphirePositions, BlockPos.STREAM_CODEC);
        buf.writeCollection(payload.topazPositions, BlockPos.STREAM_CODEC);
        buf.writeCollection(payload.peridotPositions, BlockPos.STREAM_CODEC);
        buf.writeCollection(payload.sunstonePositions, BlockPos.STREAM_CODEC);
        buf.writeCollection(payload.tourmalinePositions, BlockPos.STREAM_CODEC);
        buf.writeBoolean(payload.visible);
    }
}
