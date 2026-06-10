package net.ramixin.caustics.client.nodes;

import net.ramixin.caustics.nodes.NodeSyncData;

import java.util.ArrayList;
import java.util.List;

public class ClientCrystalNetwork {

    private static final List<ClientCrystalNode> nodes = new ArrayList<>();

    public static void onSync(List<NodeSyncData> syncData) {
        nodes.clear();
        for(NodeSyncData data : syncData) {
            nodes.add(ClientCrystalNode.fromSyncData(data));
        }
    }

    public static List<ClientCrystalNode> getNodes() {
        return nodes;
    }

}
