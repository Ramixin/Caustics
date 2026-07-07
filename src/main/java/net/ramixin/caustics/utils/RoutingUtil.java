package net.ramixin.caustics.utils;

import net.minecraft.core.BlockPos;

import java.util.Set;

public interface RoutingUtil {

    static boolean canConnect(BlockPos pos, BlockPos end, Set<BlockPos> jammers, int maxDist) {
        if(pos.distToCenterSqr(end.getX(), end.getY(), end.getZ()) > maxDist) return false;
        for(BlockPos jammer : jammers)
            if(isJammed(pos, end, jammer, maxDist))
                return false;
        return true;
    }

    static boolean isJammed(BlockPos a, BlockPos b, BlockPos jammer, int maxDist) {
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

}
