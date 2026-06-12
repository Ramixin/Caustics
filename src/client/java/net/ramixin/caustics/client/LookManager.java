package net.ramixin.caustics.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.ramixin.caustics.ModUtils;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.HashSet;
import java.util.Set;

public class LookManager {

    private BlockPos[] positions;
    private Vec3[] vectors;
    private double[] angles;
    private final Mutable<Set<Integer>> indices = new MutableObject<>();
    private final Mutable<Set<BlockPos>> ambiguities = new MutableObject<>();

    public BlockPos[] getPositions() {
        if(positions == null)
            positions = ClientCrystalNetwork.getTargetablePositions();
        return positions;
    }

    public Vec3[] getVectors() {
        if(vectors == null) {
            BlockPos[] positions = getPositions();
            if(Minecraft.getInstance().player == null) throw new IllegalStateException("Player is null");
            vectors = ModUtils.calculateUnitVectors(Minecraft.getInstance().player, positions);
        }
        return vectors;
    }

    public double[] getAngles() {
        if(angles == null) {
            Vec3[] vectors = getVectors();
            if(Minecraft.getInstance().player == null) throw new IllegalStateException("Player is null");
            angles = ModUtils.calculateDisplacementAngles(Minecraft.getInstance().player, vectors);
        }
        return angles;
    }

    public Set<Integer> getAmbiguityIndices() {
        if(indices.get() == null) {
            Vec3[] vectors = getVectors();
            if(Minecraft.getInstance().player == null) throw new IllegalStateException("Player is null");
            indices.setValue(ModUtils.ambiguousPositions(vectors));
        }
        return indices.get();
    }

    public Set<BlockPos> getAmbiguities() {
        if(ambiguities.get() == null) {
            Set<Integer> indices = getAmbiguityIndices();
            BlockPos[] positions = getPositions();
            Set<BlockPos> set = new HashSet<>();
            for(int i : indices)
                set.add(positions[i]);
            ambiguities.setValue(set);
        }
        return ambiguities.get();
    }

    public void wipe() {
        positions = null;
        vectors = null;
        angles = null;
        indices.setValue(null);
        ambiguities.setValue(null);
    }

}
