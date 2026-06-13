package net.ramixin.caustics.client.nodes;

import net.minecraft.core.BlockPos;
import net.ramixin.caustics.nodes.NodeSyncData;

import java.util.List;

public record ClientCrystalNode(List<BlockPos> sapphirePositions, List<BlockPos> topazPositions, List<BlockPos> peridotPositions, List<BlockPos> sunstonePositions) {

    public static ClientCrystalNode fromSyncData(NodeSyncData syncData) {
        return new ClientCrystalNode(syncData.sapphirePositions(), syncData.topazPositions(), syncData.peridotPositions(), syncData.sunstonePositions());
    }

}
