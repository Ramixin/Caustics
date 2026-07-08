package net.ramixin.caustics.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.networking.bidirectional.SelectionSyncPayload;
import net.ramixin.caustics.networking.clientbound.*;

public class ModClientNetworking {

    public static void onInitialize() {
        ClientPlayNetworking.registerGlobalReceiver(NodeSyncPayload.TYPE, ModClientNetworking::handleNodeSync);
        ClientPlayNetworking.registerGlobalReceiver(SignalRangeSyncPayload.TYPE, ModClientNetworking::handleSignalRangeSync);
        ClientPlayNetworking.registerGlobalReceiver(FrequencySyncPayload.TYPE, ModClientNetworking::handleFrequencySync);
        ClientPlayNetworking.registerGlobalReceiver(RoutingSyncPayload.TYPE, ModClientNetworking::handleRoutingSync);
        ClientPlayNetworking.registerGlobalReceiver(SelectionSyncPayload.TYPE, ModClientNetworking::handleSelectionSync);
        ClientPlayNetworking.registerGlobalReceiver(LeapStartPayload.TYPE, ModClientNetworking::handleLeapStart);
        ClientPlayNetworking.registerGlobalReceiver(LeapDropPayload.TYPE, ModClientNetworking::handleLeapDrop);
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

    private static void handleLeapStart(LeapStartPayload payload, ClientPlayNetworking.Context ctx) {
        ClientCrystalNetwork.getInstance().addLeap(payload);
    }

    private static void handleLeapDrop(LeapDropPayload payload, ClientPlayNetworking.Context ctx) {
        ClientCrystalNetwork.getInstance().removeLeap(payload);
    }

}
