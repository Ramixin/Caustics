package net.ramixin.caustics.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.networking.bidirectional.SelectionSyncPayload;
import net.ramixin.caustics.networking.clientbound.*;
import net.ramixin.caustics.networking.serverbound.RequestSyncPayload;
import net.ramixin.caustics.nodes.core.CrystalNetwork;

public class ModNetworking {

    public static void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(NodeSyncPayload.TYPE, NodeSyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SignalRangeSyncPayload.TYPE, SignalRangeSyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FrequencySyncPayload.TYPE, FrequencySyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RoutingSyncPayload.TYPE, RoutingSyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(LeapStatusPayload.TYPE, LeapStatusPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SelectionSyncPayload.TYPE, SelectionSyncPayload.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(RequestSyncPayload.TYPE, RequestSyncPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SelectionSyncPayload.TYPE, SelectionSyncPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestSyncPayload.TYPE, ModNetworking::handleResyncRequest);
        ServerPlayNetworking.registerGlobalReceiver(SelectionSyncPayload.TYPE, ModNetworking::handleSelectionSync);
    }

    private static void handleResyncRequest(RequestSyncPayload payload, ServerPlayNetworking.Context ctx) {
        CrystalNetwork network = CrystalNetwork.get(ctx.player().level());
        network.synchronizer().resync(ctx.player(), network);
    }

    private static void handleSelectionSync(SelectionSyncPayload selectionSyncPayload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        Caustics.LOGGER.info("received selection from {}: sapphire {}, peridot {}", player.getName().getString(), selectionSyncPayload.sapphirePos(), selectionSyncPayload.peridotPos());
        CrystalNetwork.get(player.level()).leaptionHandler().setSelection(player.getUUID(), selectionSyncPayload.sapphirePos(), selectionSyncPayload.peridotPos());
    }

}
