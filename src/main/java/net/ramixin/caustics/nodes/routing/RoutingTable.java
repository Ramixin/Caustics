package net.ramixin.caustics.nodes.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.ramixin.caustics.items.components.Frequency;
import net.ramixin.caustics.nodes.core.CrystalNetwork;

import java.util.*;

import static net.ramixin.caustics.utils.RoutingUtil.canConnect;

public class RoutingTable {

    private final Map<BlockPos, Route> routes;

    public static final StreamCodec<FriendlyByteBuf, RoutingTable> STREAM_CODEC = StreamCodec.ofMember(RoutingTable::write, RoutingTable::new);

    private RoutingTable(FriendlyByteBuf buf) {
        routes = buf.readMap(BlockPos.STREAM_CODEC, Route.STREAM_CODEC);
    }

    public RoutingTable(BlockPos pos, Set<BlockPos> travels, Set<BlockPos> routers, Set<BlockPos> jammers, int maxSignalDist, CrystalNetwork network) {
        Set<BlockPos> locs = new HashSet<>(travels);
        locs.addAll(routers);
        Map<BlockPos, BlockPos> searchTree = new HashMap<>();
        searchTree.put(pos, null);
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(pos);

        int maxDist = maxSignalDist * maxSignalDist;
        while(!queue.isEmpty()) {
            BlockPos current = queue.poll();
            Set<Frequency> currentNetworks = network.getNetworks(current);
            for(BlockPos loc : locs) {
                if(searchTree.containsKey(loc)) continue;
                if(!canConnect(current, loc, jammers, maxDist)) continue;
                Set<Frequency> locNetworks = network.getNetworks(loc);
                if(!currentNetworks.isEmpty() && !locNetworks.isEmpty() && Collections.disjoint(currentNetworks, locNetworks)) continue;
                searchTree.put(loc, current);
                if(routers.contains(loc))
                    queue.add(loc);
            }
        }
        Map<BlockPos, Route> routes = new HashMap<>();
        for(BlockPos reached : searchTree.keySet()) {
            if(reached.equals(pos)) continue;
            if(!travels.contains(reached)) continue;
            routes.put(reached, new Route(reached, searchTree));
        }
        this.routes = routes;
    }



    public boolean hasRoute(BlockPos pos) {
        return routes.containsKey(pos);
    }

    public Route getRoute(BlockPos pos) {
        return routes.get(pos);
    }

    public Set<BlockPos> keySet() {
        return routes.keySet();
    }

    public int size() {
        return routes.size();
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeMap(routes, BlockPos.STREAM_CODEC, Route.STREAM_CODEC);
    }

    @Override
    public String toString() {
        return "RoutingTable[" +
                "routes=" + routes + ']';
    }

}
