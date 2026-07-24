package net.ramixin.caustics.nodes.core;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.ramixin.caustics.ModGameRules;
import net.ramixin.caustics.networking.bidirectional.AlidadeSelectionSyncPayload;
import net.ramixin.caustics.networking.clientbound.FrequencySyncPayload;
import net.ramixin.caustics.networking.clientbound.NodeSyncPayload;
import net.ramixin.caustics.networking.clientbound.RoutingSyncPayload;
import net.ramixin.caustics.networking.clientbound.SignalRangeSyncPayload;
import net.ramixin.caustics.nodes.PlayerAccess;
import net.ramixin.caustics.nodes.leaption.Selection;

import java.util.*;

public class NetworkSynchronizer {

    private final Set<UUID> realtimePlayers = new HashSet<>();
    private final Queue<UUID> readyPlayers = new LinkedList<>();
    private final PlayerAccess access = new PlayerAccess();

    protected NetworkSynchronizer() {}

    protected void tick(ServerLevel level, CrystalNetwork network) {
        access.attach(level);
        Tracker tracker = network.getTracker();

        while(!readyPlayers.isEmpty()) {
            UUID uuid = readyPlayers.poll();
            initRealtime(uuid, network);
        }

        if(tracker.consume(Tracker.Task.NODE_SYNC))
            syncNodes(network);

        if(tracker.consume(Tracker.Task.FREQUENCY_SYNC))
            syncFrequencies(network);

        if(tracker.consume(Tracker.Task.ROUTING_SYNC))
            syncRouting(network);
    }

    public void addRealtime(UUID player) {
        readyPlayers.add(player);
    }

    public void removeRealtime(UUID player) {
        realtimePlayers.remove(player);
        readyPlayers.remove(player);
    }

    public void joinSync(ServerPlayer player, CrystalNetwork network) {
        FrequencySyncPayload payload = network.frequencyRegistry().createSyncPayload();
        ServerPlayNetworking.send(player, payload);
        ServerPlayNetworking.send(player, new SignalRangeSyncPayload(player.level().getGameRules().get(ModGameRules.SIGNAL_RANGE)));
        Selection selection = network.leaptionHandler().getSelection(player.getUUID());
        if(selection != null)
            ServerPlayNetworking.send(player, new AlidadeSelectionSyncPayload(selection.sapphirePos(), selection.peridotPos()));
    }

    public void resync(ServerPlayer player, CrystalNetwork network) {
        NodeSyncPayload nodePayload = network.nodeWorker().createSyncPayload();
        ServerPlayNetworking.send(player, nodePayload);
        FrequencySyncPayload freqPayload = network.frequencyRegistry().createSyncPayload();
        ServerPlayNetworking.send(player, freqPayload);
        RoutingSyncPayload routingPayload = network.routingManager().createSyncPayload(player);
        ServerPlayNetworking.send(player, routingPayload);
    }

    private void initRealtime(UUID uuid, CrystalNetwork network) {
        Optional<ServerPlayer> maybePlayer = access.fromUUID(uuid);
        if(maybePlayer.isEmpty()) return;
        ServerPlayer player = maybePlayer.get();
        realtimePlayers.add(uuid);
        NodeSyncPayload nodePayload = network.nodeWorker().createSyncPayload();
        ServerPlayNetworking.send(player, nodePayload);
        RoutingSyncPayload routingPayload = network.routingManager().createSyncPayload(player);
        ServerPlayNetworking.send(player, routingPayload);
    }

    private void syncNodes(CrystalNetwork network) {
        NodeSyncPayload payload = network.nodeWorker().createSyncPayload();
        for(UUID uuid : realtimePlayers) {
            Optional<ServerPlayer> maybePlayer = access.fromUUID(uuid);
            if(maybePlayer.isEmpty()) continue;
            ServerPlayNetworking.send(maybePlayer.get(), payload);
        }

    }

    private void syncFrequencies(CrystalNetwork network) {
        FrequencySyncPayload payload = network.frequencyRegistry().createSyncPayload();
        for(ServerPlayer player : access.getAll())
            ServerPlayNetworking.send(player, payload);
    }

    private void syncRouting(CrystalNetwork network) {
        for(UUID uuid : realtimePlayers) {
            Optional<ServerPlayer> maybePlayer = access.fromUUID(uuid);
            if(maybePlayer.isEmpty()) continue;
            RoutingSyncPayload payload = network.routingManager().createSyncPayload(maybePlayer.get());
            ServerPlayNetworking.send(maybePlayer.get(), payload);
        }
    }
}
