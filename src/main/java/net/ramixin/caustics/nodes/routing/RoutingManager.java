package net.ramixin.caustics.nodes.routing;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RoutingManager {

    private final Map<BlockPos, RoutingTable> routingTables = new HashMap<>();


    public void rebuildTables(Set<BlockPos> travels, Set<BlockPos> routers, Set<BlockPos> jammers, int maxSignalDist) {
        routingTables.clear();
        for(BlockPos router : routers)
            routingTables.put(router, new RoutingTable(router, travels, routers, jammers, maxSignalDist));
    }

    @Override
    public String toString() {
        int routeCount = 0;
        for(RoutingTable table : routingTables.values())
            routeCount += table.size();
        return String.format("%s tables\n%s routes\n%s", routingTables.size(), routeCount, routingTables);
    }
}
