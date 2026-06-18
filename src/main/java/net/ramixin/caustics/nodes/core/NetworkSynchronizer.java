package net.ramixin.caustics.nodes.core;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.ramixin.caustics.ModGameRules;
import net.ramixin.caustics.networking.clientbound.FrequencySyncPayload;
import net.ramixin.caustics.networking.clientbound.NodeSyncPayload;
import net.ramixin.caustics.networking.clientbound.RoutingSyncPayload;
import net.ramixin.caustics.networking.clientbound.SignalRangeSyncPayload;
import net.ramixin.caustics.nodes.PlayerAccess;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class NetworkSynchronizer {

    private final Set<UUID> realtimePlayers = new HashSet<>();
    private final PlayerAccess access = new PlayerAccess();

    protected NetworkSynchronizer() {}

    protected void tick(ServerLevel level, CrystalNetwork network) {
        access.attach(level);
        Tracker tracker = network.getTracker();

        if(tracker.consume(Tracker.Item.NODE_SYNC))
            syncNodes(network);

        if(tracker.consume(Tracker.Item.FREQUENCY_SYNC))
            syncFrequencies(network);

        if(tracker.consume(Tracker.Item.ROUTING_SYNC))
            syncRouting(network);
    }

    protected void addRealtime(UUID player) {
        realtimePlayers.add(player);
    }

    protected void removeRealtime(UUID player) {
        realtimePlayers.remove(player);
    }

    protected void joinSync(ServerPlayer player, CrystalNetwork network) {
        FrequencySyncPayload payload = network.getRegistry().createSyncPayload();
        ServerPlayNetworking.send(player, payload);
        ServerPlayNetworking.send(player, new SignalRangeSyncPayload(player.level().getGameRules().get(ModGameRules.SIGNAL_RANGE)));
    }

    protected void resync(ServerPlayer player, CrystalNetwork network) {
        NodeSyncPayload nodePayload = network.getWorker().createSyncPayload();
        ServerPlayNetworking.send(player, nodePayload);
        FrequencySyncPayload freqPayload = network.getRegistry().createSyncPayload();
        ServerPlayNetworking.send(player, freqPayload);
        RoutingSyncPayload routingPayload = network.getManager().createSyncPayload(player);
        ServerPlayNetworking.send(player, routingPayload);
    }

    private void syncNodes(CrystalNetwork network) {
        NodeSyncPayload payload = network.getWorker().createSyncPayload();
        for(UUID uuid : realtimePlayers) {
            Optional<ServerPlayer> maybePlayer = access.fromUUID(uuid);
            if(maybePlayer.isEmpty()) continue;
            ServerPlayNetworking.send(maybePlayer.get(), payload);
        }

    }

    private void syncFrequencies(CrystalNetwork network) {
        FrequencySyncPayload payload = network.getRegistry().createSyncPayload();
        for(ServerPlayer player : access.getAll())
            ServerPlayNetworking.send(player, payload);
    }

    private void syncRouting(CrystalNetwork network) {
        for(UUID uuid : realtimePlayers) {
            Optional<ServerPlayer> maybePlayer = access.fromUUID(uuid);
            if(maybePlayer.isEmpty()) continue;
            RoutingSyncPayload payload = network.getManager().createSyncPayload(maybePlayer.get());
            ServerPlayNetworking.send(maybePlayer.get(), payload);
        }
    }
}
