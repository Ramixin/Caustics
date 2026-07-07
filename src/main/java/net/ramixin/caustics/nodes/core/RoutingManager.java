package net.ramixin.caustics.nodes.core;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.ModGameRules;
import net.ramixin.caustics.networking.clientbound.RoutingSyncPayload;
import net.ramixin.caustics.nodes.routing.Route;
import net.ramixin.caustics.nodes.routing.RoutingTable;

import java.util.*;

import static net.ramixin.caustics.utils.RoutingUtil.canConnect;

public class RoutingManager {

    private final Map<BlockPos, RoutingTable> routingTables = new HashMap<>();
    private int signalRange = 256;

    public void tick(ServerLevel level, CrystalNetwork network) {
        Tracker tracker = network.getTracker();
        int tempSignal = level.getGameRules().get(ModGameRules.SIGNAL_RANGE);
        if(tempSignal != signalRange) {
            signalRange = tempSignal;
            tracker.push(Tracker.Task.ROUTING_SYNC);
        }

        if(tracker.consume(Tracker.Task.REBUILD_ROUTING)) {
            tracker.push(Tracker.Task.ROUTING_SYNC);
            NodeIndex index = network.nodeIndex();
            Set<BlockPos> travels = index.getPositionsOfType(NodeIndex.Type.SAPPHIRE);
            Set<BlockPos> routers = index.getPositionsOfType(NodeIndex.Type.TOPAZ);
            Set<BlockPos> jammers = index.getPositionsOfType(NodeIndex.Type.TOURMALINE);
            rebuildTables(network, travels, routers, jammers, signalRange);
        }
    }

    protected RoutingManager() {}

    public void rebuildTables(CrystalNetwork network, Set<BlockPos> travels, Set<BlockPos> routers, Set<BlockPos> jammers, int maxSignalDist) {
        routingTables.clear();
        for(BlockPos router : routers) {
            RoutingTable routingTable = new RoutingTable(router, travels, routers, jammers, maxSignalDist, network);
            if(routingTable.size() > 0)
                routingTables.put(router, routingTable);
        }
    }

    public Map<BlockPos, RoutingTable> getTrackedTables(BlockPos pos) {
        int maxDist = (signalRange + 16) * (signalRange + 16);
        Map<BlockPos, RoutingTable> relevantTables = new HashMap<>();
        for(BlockPos router : routingTables.keySet()) {
            if(router.distSqr(pos) <= maxDist)
                relevantTables.put(router, routingTables.get(router));
        }
        return relevantTables;
    }

    protected RoutingSyncPayload createSyncPayload(ServerPlayer player) {
        return new RoutingSyncPayload(getTrackedTables(player.blockPosition()));
    }

    public Optional<Route> findRoute(CrystalNetwork network, BlockPos from, BlockPos to) {
        Set<BlockPos> jammers = network.nodeIndex().getPositionsOfType(NodeIndex.Type.TOURMALINE);
        int maxDist = signalRange * signalRange;
        if(canConnect(from, to, jammers, maxDist)) return Optional.of(new Route(List.of(), to));
        Route curRoute = null;
        for(BlockPos routerPos : routingTables.keySet()) {
            RoutingTable table = routingTables.get(routerPos);
            if(!table.hasRoute(to)) continue;
            if(!canConnect(from, routerPos, jammers, maxDist)) continue;
            if(curRoute == null || table.getRoute(to).length() < curRoute.length())
                curRoute = table.getRoute(to);
        }
        return Optional.ofNullable(curRoute);
    }

    protected void clear() {
        routingTables.clear();
    }

    public void printRouting() {
        int routeCount = 0;
        for(RoutingTable table : routingTables.values())
            routeCount += table.size();
        Caustics.LOGGER.info("{} tables\n{} routes\n{}", routingTables.size(), routeCount, routingTables);
    }
}
