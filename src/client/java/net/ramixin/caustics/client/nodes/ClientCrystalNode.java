package net.ramixin.caustics.client.nodes;

import net.minecraft.core.BlockPos;
import net.ramixin.caustics.items.components.NetworkFrequency;
import net.ramixin.caustics.nodes.NodeSyncData;

import java.util.List;

public record ClientCrystalNode(List<BlockPos> positions, List<NetworkFrequency> frequencies) {

    public static ClientCrystalNode fromSyncData(NodeSyncData syncData) {
        return new ClientCrystalNode(syncData.sapphirePositions(), syncData.frequencies());
    }

}
