package net.ramixin.caustics.nodes.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;

public class RoutingTable {

    private final Map<BlockPos, Route> routes;

    public static final StreamCodec<FriendlyByteBuf, RoutingTable> STREAM_CODEC = StreamCodec.ofMember(RoutingTable::write, RoutingTable::new);

    private RoutingTable(FriendlyByteBuf buf) {
        routes = buf.readMap(BlockPos.STREAM_CODEC, Route.STREAM_CODEC);
    }

    public RoutingTable(BlockPos pos, Set<BlockPos> travels, Set<BlockPos> routers, Set<BlockPos> jammers, int maxSignalDist) {
        Set<BlockPos> locs = new HashSet<>(travels);
        locs.addAll(routers);
        Map<BlockPos, BlockPos> searchTree = new HashMap<>();
        searchTree.put(pos, null);
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(pos);

        int maxDist = maxSignalDist * maxSignalDist;
        while(!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for(BlockPos loc : locs) {
                if(searchTree.containsKey(loc)) continue;
                if(!canConnect(current, loc, jammers, maxDist)) continue;
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

    private static boolean canConnect(BlockPos pos, BlockPos end, Set<BlockPos> jammers, int maxDist) {
        if(pos.distToCenterSqr(end.getX(), end.getY(), end.getZ()) > maxDist) return false;
        for(BlockPos jammer : jammers)
            if(isJammed(pos, end, jammer, maxDist))
                return false;
        return true;
    }

    private static boolean isJammed(BlockPos a, BlockPos b, BlockPos jammer, int maxDist) {
        boolean insideA = a.distToCenterSqr(jammer.getX(), jammer.getY(), jammer.getZ()) < maxDist;
        boolean insideB = b.distToCenterSqr(jammer.getX(), jammer.getY(), jammer.getZ()) < maxDist;

        if(insideA && insideB) return false;
        if(insideA || insideB) return true;

        double ax = a.getX();
        double ay = a.getY();
        double az = a.getZ();
        double bx = b.getX();
        double by = b.getY();
        double bz = b.getZ();
        double jx = jammer.getX();
        double jy = jammer.getY();
        double jz = jammer.getZ();

        double dx = bx - ax;
        double dy = by - ay;
        double dz = bz - az;
        double sqrLen = dx*dx + dy*dy + dz*dz;
        if(sqrLen < 1e-6) return false;

        double t = ((jx - ax) * dx + (jy - ay) * dy + (jz - az) * dz) / sqrLen;
        if(t < 0 || t > 1) return false;

        double closestX = ax + t * dx;
        double closestY = ay + t * dy;
        double closestZ = az + t * dz;
        double sqrDist = (jx - closestX) * (jx - closestX) + (jy - closestY) * (jy - closestY) + (jz - closestZ) * (jz - closestZ);

        return sqrDist <= maxDist;

    }

    public boolean hasRoute(BlockPos pos) {
        return routes.containsKey(pos);
    }

    public Route getRoute(BlockPos pos) {
        return routes.get(pos);
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
