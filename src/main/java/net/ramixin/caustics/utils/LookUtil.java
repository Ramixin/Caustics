package net.ramixin.caustics.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public interface LookUtil {

    float lookThreshold = 3f;
    float ambiguityThreshold = 1f;

    static Optional<BlockPos> getLookingAt(Player player, BlockPos[] positions) {
        Vec3[] vectors = calculateUnitVectors(player, positions);
        double[] angles = calculateDisplacementAngles(player, vectors);
        return calculateClosestLooking(angles).map(i -> positions[i]);
    }

    static Vec3[] calculateUnitVectors(Player player, BlockPos[] positions) {
        Vec3 playerPos = player.position();
        Vec3[] vectors = new Vec3[positions.length];
        for(int i = 0; i < positions.length; i++) {
            BlockPos pos = positions[i];
            vectors[i] = new Vec3(pos)
                    .add(0.5, -1, 0.5)
                    .subtract(playerPos)
                    .normalize();
        }
        return vectors;
    }

    static double[] calculateDisplacementAngles(Player player, Vec3[] vectors) {
        Vec3 lookVec = player.getLookAngle();
        double[] angles = new double[vectors.length];
        for (int i = 0; i < vectors.length; i++) {
            double dot = vectors[i].dot(lookVec);
            angles[i] = Math.toDegrees(Math.acos(Math.clamp(dot, -1.0, 1.0)));
        }
        return angles;
    }

    static Optional<Integer> calculateClosestLooking(double[] angles) {
        double bestAngle = Double.MAX_VALUE;
        int bestIndex = -1;
        for(int i = 0; i < angles.length; i++) {
            double angle = angles[i];
            if(angle > lookThreshold || angle >= bestAngle) continue;
            bestAngle = angle;
            bestIndex = i;
        }
        if(bestIndex == -1) return Optional.empty();
        return Optional.of(bestIndex);
    }

    static Set<Integer> ambiguousPositions(Vec3[] vectors) {
        Set<Integer> ambiguities = new HashSet<>();
        for(int i = 0; i < vectors.length; i++)
            for(int j = i + 1; j < vectors.length; j++) {
                double angle = Math.toDegrees(Math.acos(Math.clamp(vectors[i].dot(vectors[j]), -1.0, 1.0)));
                if(angle <= ambiguityThreshold) {
                    ambiguities.add(i);
                    ambiguities.add(j);
                }
            }
        return ambiguities;
    }

    static boolean isAmbiguous(Vec3[] vectors, int i) {
        for(int j = 0; j < vectors.length; j++) {
            if(i == j) continue;
            double angle = Math.toDegrees(Math.acos(Math.clamp(vectors[i].dot(vectors[j]), -1.0, 1.0)));
            if(angle <= ambiguityThreshold) return true;
        }
        return false;
    }
}
