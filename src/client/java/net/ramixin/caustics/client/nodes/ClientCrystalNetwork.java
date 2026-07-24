package net.ramixin.caustics.client.nodes;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.ramixin.caustics.client.CausticsClient;
import net.ramixin.caustics.networking.bidirectional.AlidadeSelectionSyncPayload;
import net.ramixin.caustics.networking.clientbound.FrequencySyncPayload;
import net.ramixin.caustics.networking.clientbound.LeapDropPayload;
import net.ramixin.caustics.networking.clientbound.LeapStartPayload;
import net.ramixin.caustics.nodes.Network;
import net.ramixin.caustics.nodes.NodeSyncData;
import net.ramixin.caustics.nodes.core.FrequencyRegistry;
import net.ramixin.caustics.nodes.routing.Route;
import net.ramixin.caustics.nodes.routing.RoutingTable;

import java.util.*;

import static net.ramixin.caustics.utils.RoutingUtil.canConnect;

public class ClientCrystalNetwork implements Network {

    private static final ClientCrystalNetwork INSTANCE = new ClientCrystalNetwork();

    private final HashMap<BlockPos, ClientNode> sapphireToNode = new HashMap<>();
    private final HashMap<BlockPos, ClientNode> topazToNode = new HashMap<>();
    private final Set<BlockPos> tourmaline = new HashSet<>();

    private final FrequencyRegistry registry = new FrequencyRegistry();

    private final Map<BlockPos, RoutingTable> routingTables = new HashMap<>();

    private final HashMap<UUID, ClientLeap> leaps = new HashMap<>();

    private final LeapParticleEngine particleEngine = new LeapParticleEngine();
    private final IconCaches caches = new IconCaches();

    public static ClientCrystalNetwork getInstance() {
        return INSTANCE;
    }

    public void tick() {
        particleEngine.tick();
        if(Minecraft.getInstance().level == null) return;
        for(AbstractClientPlayer player : Minecraft.getInstance().level.players()) {
            if(!leaps.containsKey(player.getUUID())) continue;
            particleEngine.addParticle(player);
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null) return;
        if(!player.isUsingItem()) return;
        if(!player.getUseItem().is(Items.SPYGLASS)) return;
        caches.tick();
    }

    public LeapParticleEngine particleEngine() {
        return particleEngine;
    }

    public void onNodeSync(List<NodeSyncData> syncData) {
        if(Minecraft.getInstance().level == null) return;
        caches.clearMapAll();
        sapphireToNode.clear();
        topazToNode.clear();
        tourmaline.clear();

        for(NodeSyncData data : syncData) {
            ClientNode node = new ClientNode(data, caches);
            for(BlockPos pos : data.sapphirePositions()) sapphireToNode.put(pos, node);
            for(BlockPos pos : data.topazPositions()) topazToNode.put(pos, node);
            tourmaline.addAll(data.tourmalinePositions());
        }
    }

    public Optional<ClientNode> getSapphireNodeAt(BlockPos pos) {
        return Optional.ofNullable(sapphireToNode.get(pos));
    }

    public Set<BlockPos> getJammerPositions() {
        return tourmaline;
    }

    public Pair<BlockPos[], Route[]> getTargetablePositions() {
        List<BlockPos> visiblePositions = new ArrayList<>();
        List<Route> routes = new ArrayList<>();
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null) throw new IllegalStateException("Player is null");

        for(BlockPos pos : sapphireToNode.keySet()) {
            if(!sapphireToNode.get(pos).visible()) continue;
            if(pos.distToCenterSqr(player.position()) >= CausticsClient.MAX_SIGNAL_RANGE) continue;
            if(!canConnect(player.blockPosition(), pos, tourmaline, CausticsClient.MAX_SIGNAL_RANGE)) continue;
            visiblePositions.add(pos);
            routes.add(new Route(List.of(), pos));
        }

        for(BlockPos pos : routingTables.keySet()) {
            if(pos.distToCenterSqr(player.position()) >= CausticsClient.MAX_SIGNAL_RANGE) continue;
            if(!canConnect(player.blockPosition(), pos, tourmaline, CausticsClient.MAX_SIGNAL_RANGE)) continue;
            RoutingTable table = routingTables.get(pos);
            for(BlockPos nodePos : table.keySet()) {
                Route route = table.getRoute(nodePos);
                if(!isRouteVisible(route)) continue;
                int index = visiblePositions.indexOf(nodePos);
                if(index == -1) {
                    visiblePositions.add(nodePos);
                    routes.add(route);
                    continue;
                }
                if(route.length() < routes.get(index).length()) {
                    visiblePositions.set(index, nodePos);
                    routes.set(index, route);
                }
            }
        }
        return new Pair<>(visiblePositions.toArray(BlockPos[]::new), routes.toArray(Route[]::new));
    }

    private boolean isRouteVisible(Route route) {
        for(BlockPos pos : route.immutablePath())
            if(!isNodeVisible(pos)) return false;
        return true;
    }

    private boolean isNodeVisible(BlockPos pos) {
        if(sapphireToNode.containsKey(pos)) return sapphireToNode.get(pos).visible();
        else if(topazToNode.containsKey(pos)) return topazToNode.get(pos).visible();
        return false;
    }

    public Set<BlockPos> getActiveJammers(Vec3 position) {
        Set<BlockPos> result = new HashSet<>();
        for(BlockPos pos : tourmaline) {
            if(position.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < CausticsClient.MAX_SIGNAL_RANGE) result.add(pos);
        }
        return result;
    }

    public void addLeap(LeapStartPayload payload) {
        leaps.put(payload.player(), new ClientLeap(payload));
    }

    public void removeLeap(LeapDropPayload payload) {
        leaps.remove(payload.player());
    }

    public Optional<ClientLeap> getLeap(UUID profileId) {
        return Optional.ofNullable(leaps.get(profileId));
    }

    public void onRoutingSync(Map<BlockPos, RoutingTable> routingTables) {
        this.routingTables.clear();
        this.routingTables.putAll(routingTables);
    }

    public IconCaches caches() {
        return caches;
    }

    public void nuke() {
        registry.syncWith(new FrequencySyncPayload(Map.of(), Map.of()));
        routingTables.clear();
        sapphireToNode.clear();
        leaps.clear();
        particleEngine.clear();
        caches.clearAll();
    }

    @Override
    public FrequencyRegistry frequencyRegistry() {
        return registry;
    }

    public void setSelection(AlidadeSelectionSyncPayload payload) {
        caches.alidade().select(payload.sapphirePos());
    }
}
