package net.ramixin.caustics.client.nodes;

import net.minecraft.core.BlockPos;
import net.ramixin.caustics.nodes.NodeSyncData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ClientCrystalNetwork {

    private static final List<ClientCrystalNode> nodes = new ArrayList<>();
    private static final HashMap<BlockPos, ClientCrystalNode> posToNode = new HashMap<>();

    public static void onSync(List<NodeSyncData> syncData) {
        nodes.clear();
        for(NodeSyncData data : syncData) {
            nodes.add(ClientCrystalNode.fromSyncData(data));
            for(BlockPos pos : data.sapphirePositions()) posToNode.put(pos, ClientCrystalNode.fromSyncData(data));
        }
    }

    public static List<ClientCrystalNode> getNodes() {
        return nodes;
    }

    public static Optional<ClientCrystalNode> getNodeAt(BlockPos pos) {
        return Optional.ofNullable(posToNode.get(pos));
    }

}
