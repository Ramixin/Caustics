package net.ramixin.caustics.client.nodes;

import net.minecraft.core.BlockPos;
import net.ramixin.caustics.items.components.NetworkFrequency;
import net.ramixin.caustics.nodes.NodeSyncData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ClientCrystalNode(List<BlockPos> positions, List<NetworkFrequency> frequencies, Map<BlockPos, String> clusterNames) {

    public static ClientCrystalNode fromSyncData(NodeSyncData syncData) {
        return new ClientCrystalNode(syncData.sapphirePositions(), syncData.frequencies(), new LinkedHashMap<>(syncData.clusterNames()));
    }

    public List<String> getDepositNames() {
        return List.copyOf(clusterNames.values());
    }

    public Optional<String> getDeposit(int index) {
        return clusterNames.values().stream().skip(index).findFirst();
    }

}
