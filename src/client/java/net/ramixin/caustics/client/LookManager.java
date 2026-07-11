package net.ramixin.caustics.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.rendering.node.IconHolder;
import net.ramixin.caustics.client.rendering.node.NodeIcon;
import net.ramixin.caustics.nodes.routing.Route;
import net.ramixin.caustics.utils.LookUtil;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Set;

public class LookManager {

    private BlockPos[] positions;
    private Route[] routes;
    private Vec3[] vectors;
    private double[] angles;
    private NodeIcon[] icons;
    private final Mutable<Set<Integer>> indices = new MutableObject<>();

    public BlockPos[] getPositions() {
        if(positions == null) {
            Pair<BlockPos[], Route[]> positionsAndRoutes = ClientCrystalNetwork.getInstance().getTargetablePositions();
            positions = positionsAndRoutes.getFirst();
            routes = positionsAndRoutes.getSecond();
        }
        return positions;
    }

    public NodeIcon[] getIcons() {
        if(icons == null) {
            BlockPos[] positions = getPositions();
            icons = new NodeIcon[positions.length];
            IconHolder iconHolder = ClientCrystalNetwork.getInstance().iconHolder();
            for (int i = 0; i < positions.length; i++)
                icons[i] = iconHolder.get(positions[i]).orElseThrow();

        }
        return icons;
    }

    public Route[] getRoutes() {
        if(routes == null) {
            Pair<BlockPos[], Route[]> positionsAndRoutes = ClientCrystalNetwork.getInstance().getTargetablePositions();
            positions = positionsAndRoutes.getFirst();
            routes = positionsAndRoutes.getSecond();
        }
        return routes;
    }

    public Vec3[] getVectors() {
        if(vectors == null) {
            BlockPos[] positions = getPositions();
            if(Minecraft.getInstance().player == null) throw new IllegalStateException("Player is null");
            vectors = LookUtil.calculateUnitVectors(Minecraft.getInstance().player, positions);
        }
        return vectors;
    }

    public double[] getAngles() {
        if(angles == null) {
            Vec3[] vectors = getVectors();
            if(Minecraft.getInstance().player == null) throw new IllegalStateException("Player is null");
            angles = LookUtil.calculateDisplacementAngles(Minecraft.getInstance().player, vectors);
        }
        return angles;
    }

    public Set<Integer> getAmbiguityIndices() {
        if(indices.get() == null) {
            Vec3[] vectors = getVectors();
            if(Minecraft.getInstance().player == null) throw new IllegalStateException("Player is null");
            indices.setValue(LookUtil.ambiguousPositions(vectors));
        }
        return indices.get();
    }

    public void wipe() {
        positions = null;
        vectors = null;
        angles = null;
        icons = null;
        indices.setValue(null);
    }

}
