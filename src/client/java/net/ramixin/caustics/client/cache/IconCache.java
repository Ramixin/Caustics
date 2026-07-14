package net.ramixin.caustics.client.cache;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.ramixin.caustics.client.nodes.icons.NodeIcon;
import net.ramixin.caustics.utils.LookUtil;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public abstract class IconCache<T extends NodeIcon> {

    public static final RandomSource RANDOM = RandomSource.create();

    protected final Map<BlockPos, T> cache = new HashMap<>();
    private final Function<Integer, T[]> arrayConstructor;
    private final Function<BlockPos, T> constructor;

    private Vec3[] vectors;
    private double[] angles;
    private T[] icons;
    private final Mutable<Set<Integer>> indices = new MutableObject<>();

    public IconCache(Function<Integer, T[]> arrayConstructor, Function<BlockPos, T> constructor) {
        this.arrayConstructor = arrayConstructor;
        this.constructor = constructor;
    }

    public abstract BlockPos[] getPositions();

    public void add(BlockPos pos, T icon) {
        cache.put(pos, icon);
    }

    public T get(BlockPos pos) {
        return cache.computeIfAbsent(pos, constructor);
    }

    public void tick() {
        double[] angles = getAngles();
        int closest = LookUtil.calculateClosestLooking(angles).orElse(-1);
        T[] icons = getIcons();
        for(int i = 0; i < icons.length; i++) {
            icons[i].tick(i == closest);
        }
    }

    public T[] getIcons() {
        if(icons != null) return icons;
        BlockPos[] positions = getPositions();
        icons = arrayConstructor.apply(positions.length);
        for (int i = 0; i < positions.length; i++)
            icons[i] = cache.get(positions[i]);
        return icons;
    }

    public Vec3[] getVectors() {
        if(vectors != null) return vectors;
        BlockPos[] positions = getPositions();
        if(Minecraft.getInstance().player == null) throw new IllegalStateException("Player is null");
        vectors = LookUtil.calculateUnitVectors(Minecraft.getInstance().player, positions);
        return vectors;
    }

    public double[] getAngles() {
        if(angles != null) return angles;
        Vec3[] vectors = getVectors();
        if(Minecraft.getInstance().player == null) throw new IllegalStateException("Player is null");
        angles = LookUtil.calculateDisplacementAngles(Minecraft.getInstance().player, vectors);
        return angles;
    }

    public Set<Integer> getAmbiguityIndices() {
        if(indices.get() != null) return indices.get();
        Vec3[] vectors = getVectors();
        if(Minecraft.getInstance().player == null) throw new IllegalStateException("Player is null");
        indices.setValue(LookUtil.ambiguousPositions(vectors));
        return indices.get();
    }

    public void wipe() {
        vectors = null;
        angles = null;
        icons = null;
        indices.setValue(null);
    }

    public void clear() {
        cache.clear();
    }
}
