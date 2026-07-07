package net.ramixin.caustics.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.networking.bidirectional.SelectionSyncPayload;
import net.ramixin.caustics.networking.clientbound.FrequencySyncPayload;
import net.ramixin.caustics.networking.clientbound.NodeSyncPayload;
import net.ramixin.caustics.networking.clientbound.RoutingSyncPayload;
import net.ramixin.caustics.networking.clientbound.SignalRangeSyncPayload;

public class ModClientNetworking {

    public static void onInitialize() {
        ClientPlayNetworking.registerGlobalReceiver(NodeSyncPayload.TYPE, ModClientNetworking::handleNodeSync);
        ClientPlayNetworking.registerGlobalReceiver(SignalRangeSyncPayload.TYPE, ModClientNetworking::handleSignalRangeSync);
        ClientPlayNetworking.registerGlobalReceiver(FrequencySyncPayload.TYPE, ModClientNetworking::handleFrequencySync);
        ClientPlayNetworking.registerGlobalReceiver(RoutingSyncPayload.TYPE, ModClientNetworking::handleRoutingSync);
        ClientPlayNetworking.registerGlobalReceiver(SelectionSyncPayload.TYPE, ModClientNetworking::handleSelectionSync);
    }

    private static void handleNodeSync(NodeSyncPayload payload, ClientPlayNetworking.Context ctx) {
        ClientCrystalNetwork.getInstance().onNodeSync(payload.nodeData());
    }

    private static void handleSignalRangeSync(SignalRangeSyncPayload payload, ClientPlayNetworking.Context ctx) {
        int val = payload.newValue();
        CausticsClient.MAX_SIGNAL_RANGE = val * val;
    }

    private static void handleFrequencySync(FrequencySyncPayload payload, ClientPlayNetworking.Context ctx) {
        ClientCrystalNetwork.getInstance().frequencyRegistry().syncWith(payload);
    }

    private static void handleRoutingSync(RoutingSyncPayload payload, ClientPlayNetworking.Context ctx) {
        ClientCrystalNetwork.getInstance().onRoutingSync(payload.routingTables());
    }

    private static void handleSelectionSync(SelectionSyncPayload selectionSyncPayload, ClientPlayNetworking.Context context) {
        ClientCrystalNetwork.getInstance().setSelection(selectionSyncPayload);
        Caustics.LOGGER.info("Selection sapphire synced, but not peridot!!!!");
    }

}
