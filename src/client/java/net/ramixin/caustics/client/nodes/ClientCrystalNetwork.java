package net.ramixin.caustics.client.nodes;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.ramixin.caustics.client.CausticsClient;
import net.ramixin.caustics.networking.bidirectional.SelectionSyncPayload;
import net.ramixin.caustics.networking.clientbound.FrequencySyncPayload;
import net.ramixin.caustics.nodes.Network;
import net.ramixin.caustics.nodes.NodeSyncData;
import net.ramixin.caustics.nodes.core.FrequencyRegistry;
import net.ramixin.caustics.nodes.routing.Route;
import net.ramixin.caustics.nodes.routing.RoutingTable;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;

import static net.ramixin.caustics.utils.RoutingUtil.canConnect;

public class ClientCrystalNetwork implements Network {

    private static final ClientCrystalNetwork INSTANCE = new ClientCrystalNetwork();

    private final HashMap<BlockPos, ClientNode> sapphireToNode = new HashMap<>();
    private final HashMap<BlockPos, ClientNode> topazToNode = new HashMap<>();
    private final HashMap<BlockPos, ClientNode> tourmalineToNode = new HashMap<>();

    private final MutableInt scrollPos = new MutableInt();
    private final Mutable<BlockPos> lastLookingAt = new MutableObject<>();

    private final FrequencyRegistry registry = new FrequencyRegistry();

    private final Map<BlockPos, RoutingTable> routingTables = new HashMap<>();

    private final Mutable<BlockPos> selectedNode = new MutableObject<>();
    private final MutableInt selectedScrollPos = new MutableInt();

    public static ClientCrystalNetwork getInstance() {
        return INSTANCE;
    }

    public void onNodeSync(List<NodeSyncData> syncData) {
        sapphireToNode.clear();
        topazToNode.clear();

        for(NodeSyncData data : syncData) {
            ClientNode node = ClientNode.fromSyncData(data);
            for(BlockPos pos : data.sapphirePositions()) sapphireToNode.put(pos, node);
            for(BlockPos pos : data.topazPositions()) topazToNode.put(pos, node);
            for(BlockPos pos : data.tourmalinePositions()) tourmalineToNode.put(pos, node);
        }
    }

    public Optional<ClientNode> getSapphireNodeAt(BlockPos pos) {
        return Optional.ofNullable(sapphireToNode.get(pos));
    }

    public Pair<BlockPos[], Route[]> getTargetablePositions() {
        List<BlockPos> visiblePositions = new ArrayList<>();
        List<Route> routes = new ArrayList<>();
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null) throw new IllegalStateException("Player is null");

        Set<BlockPos> jammers = tourmalineToNode.keySet();
        for(BlockPos pos : sapphireToNode.keySet()) {
            if(!sapphireToNode.get(pos).visible()) continue;
            if(pos.distToCenterSqr(player.position()) >= CausticsClient.MAX_SIGNAL_RANGE) continue;
            if(!canConnect(player.blockPosition(), pos, jammers, CausticsClient.MAX_SIGNAL_RANGE)) continue;
            visiblePositions.add(pos);
            routes.add(new Route(List.of(), pos));
        }


        for(BlockPos pos : routingTables.keySet()) {
            if(pos.distToCenterSqr(player.position()) >= CausticsClient.MAX_SIGNAL_RANGE) continue;
            if(!canConnect(player.blockPosition(), pos, jammers, CausticsClient.MAX_SIGNAL_RANGE)) continue;
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

    public void clearScrollPos() {
        scrollPos.setValue(0);
    }

    public void deltaScrollPos(double dy) {
        if(dy < 0) {
            if(scrollPos.intValue() > 0) scrollPos.decrement();
        } else {
            BlockPos lookingAt = lastLookingAt.get();
            if(lookingAt == null) return;
            ClientNode node = sapphireToNode.get(lookingAt);
            if(scrollPos.intValue() < node.peridotPositions().size()-1) scrollPos.increment();
        }

    }

    public int getScrollPos() {
        return scrollPos.intValue();
    }

    public void setLastLookingAt(BlockPos pos) {
        if(!pos.equals(lastLookingAt.get())) clearScrollPos();
        lastLookingAt.setValue(pos);
    }

    public void onRoutingSync(Map<BlockPos, RoutingTable> routingTables) {
        this.routingTables.clear();
        this.routingTables.putAll(routingTables);
    }

    public void selectNode(BlockPos pos) {
        ClientNode node = sapphireToNode.get(pos);
        if(node == null) return;
        if(node.peridotPositions().isEmpty()) return;
        selectedNode.setValue(pos);
        selectedScrollPos.setValue(scrollPos.intValue());
    }

    public void deselectNode() {
        selectedNode.setValue(null);
    }

    public Optional<BlockPos> getSelectedNode() {
        BlockPos pos = selectedNode.get();
        if(pos == null) return Optional.empty();
        ClientNode node = sapphireToNode.get(pos);
        if(node == null) {
            selectedNode.setValue(null);
            return Optional.empty();
        }
        return Optional.of(pos);
    }

    public int getSelectedScrollPos() {
        return selectedScrollPos.intValue();
    }

    public void nuke() {
        registry.syncWith(new FrequencySyncPayload(Map.of(), Map.of()));
        routingTables.clear();
        sapphireToNode.clear();
        selectedNode.setValue(null);
    }

    @Override
    public FrequencyRegistry frequencyRegistry() {
        return registry;
    }

    public void setSelection(SelectionSyncPayload payload) {
        selectedNode.setValue(payload.sapphirePos());
    }
}
