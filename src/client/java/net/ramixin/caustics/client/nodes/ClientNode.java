package net.ramixin.caustics.client.nodes;

import net.minecraft.core.BlockPos;
import net.ramixin.caustics.nodes.NodeSyncData;

import java.util.List;

public record ClientNode(List<BlockPos> sapphirePositions, List<BlockPos> topazPositions, List<BlockPos> peridotPositions, List<BlockPos> sunstonePositions, boolean visible) {

    public static ClientNode fromSyncData(NodeSyncData syncData) {
        return new ClientNode(syncData.sapphirePositions(), syncData.topazPositions(), syncData.peridotPositions(), syncData.sunstonePositions(), syncData.visible());
    }

}
