package net.ramixin.caustics.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.items.components.Frequency;
import net.ramixin.caustics.networking.bidirectional.AlidadeSelectionSyncPayload;
import net.ramixin.caustics.networking.clientbound.*;
import net.ramixin.caustics.networking.serverbound.FrequencyChangePayload;
import net.ramixin.caustics.networking.serverbound.FrequencyRenamedPayload;
import net.ramixin.caustics.networking.serverbound.RequestSyncPayload;
import net.ramixin.caustics.nodes.core.CrystalNetwork;
import net.ramixin.caustics.nodes.core.FrequencyRegistry;

import java.util.Optional;

public class ModNetworking {

    public static void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(NodeSyncPayload.TYPE, NodeSyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SignalRangeSyncPayload.TYPE, SignalRangeSyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FrequencySyncPayload.TYPE, FrequencySyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RoutingSyncPayload.TYPE, RoutingSyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(LeapStartPayload.TYPE, LeapStartPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(LeapDropPayload.TYPE, LeapDropPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(AlidadeSelectionSyncPayload.TYPE, AlidadeSelectionSyncPayload.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(RequestSyncPayload.TYPE, RequestSyncPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(AlidadeSelectionSyncPayload.TYPE, AlidadeSelectionSyncPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(LeapStartPayload.TYPE, LeapStartPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(FrequencyChangePayload.TYPE, FrequencyChangePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(FrequencyRenamedPayload.TYPE, FrequencyRenamedPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestSyncPayload.TYPE, ModNetworking::handleResyncRequest);
        ServerPlayNetworking.registerGlobalReceiver(AlidadeSelectionSyncPayload.TYPE, ModNetworking::handleSelectionSync);
        ServerPlayNetworking.registerGlobalReceiver(FrequencyChangePayload.TYPE, ModNetworking::handleFrequencyChange);
        ServerPlayNetworking.registerGlobalReceiver(FrequencyRenamedPayload.TYPE, ModNetworking::handleFrequencyRenamed);
    }

    private static void handleResyncRequest(RequestSyncPayload payload, ServerPlayNetworking.Context ctx) {
        CrystalNetwork network = CrystalNetwork.get(ctx.player().level());
        network.synchronizer().resync(ctx.player(), network);
    }

    private static void handleSelectionSync(AlidadeSelectionSyncPayload alidadeSelectionSyncPayload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        CrystalNetwork.get(player.level()).leaptionHandler().setSelection(player.getUUID(), alidadeSelectionSyncPayload.sapphirePos(), alidadeSelectionSyncPayload.peridotPos());
    }

    private static void handleFrequencyChange(FrequencyChangePayload frequencyChange, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        FrequencyRegistry registry = CrystalNetwork.get(player.level()).frequencyRegistry();
        Optional<Frequency> frequency = frequencyChange.frequency();
        Caustics.LOGGER.info("Change Received: {} -> {}", frequencyChange.pos(), frequency);
        if(frequency.isPresent())
            registry.register(frequencyChange.pos(), frequency.get());
        else
            registry.free(frequencyChange.pos());
    }

    private static void handleFrequencyRenamed(FrequencyRenamedPayload rename, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        Caustics.LOGGER.info("Rename Received: {} -> {}", rename.pos(), rename.newName());
        FrequencyRegistry registry = CrystalNetwork.get(player.level()).frequencyRegistry();
        Optional<Frequency> maybeFreq = registry.getFrequencyAt(rename.pos());
        if(maybeFreq.isEmpty())
            registry.register(rename.pos(), Frequency.fromName(rename.newName()));
        else {
            registry.register(maybeFreq.get(), rename.newName());
        }
    }
}
