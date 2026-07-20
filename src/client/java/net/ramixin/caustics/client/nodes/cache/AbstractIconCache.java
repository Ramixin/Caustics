package net.ramixin.caustics.client.nodes.cache;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.ramixin.caustics.client.nodes.ClientNode;
import net.ramixin.caustics.client.nodes.icons.NodeIcon;
import net.ramixin.caustics.utils.LookUtil;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public abstract class AbstractIconCache<T extends NodeIcon> {

    protected final Map<BlockPos, T> cache = new HashMap<>();
    private final Map<BlockPos, ClientNode> nodeMap = new HashMap<>();
    private final Function<Integer, T[]> arrayConstructor;
    private final Function<BlockPos, T> constructor;

    private Vec3[] vectors;
    private double[] angles;
    private T[] icons;
    private final Mutable<Set<Integer>> indices = new MutableObject<>();

    private int scrollPos = 0;
    private int selectedScrollPos = 0;
    private BlockPos selectedPos;
    private BlockPos lastLookingAt;

    public AbstractIconCache(Function<Integer, T[]> arrayConstructor, Function<BlockPos, T> constructor) {
        this.arrayConstructor = arrayConstructor;
        this.constructor = constructor;
    }

    public abstract BlockPos[] getPositions();

    public void add(BlockPos pos, T icon) {
        cache.put(pos, icon);
    }

    public void associateNode(BlockPos pos, ClientNode node) {
        nodeMap.put(pos, node);
    }

    public T get(BlockPos pos) {
        return cache.computeIfAbsent(pos, constructor);
    }

    public Optional<ClientNode> getNode(BlockPos pos) {
        return Optional.ofNullable(nodeMap.get(pos));
    }

    public int getScrollPos() {
        return scrollPos;
    }

    public int getSelectedScrollPos() {
        return selectedScrollPos;
    }

    public void setLastLooking(BlockPos pos) {
        if(!pos.equals(lastLookingAt)) scrollPos = 0;
        lastLookingAt = pos;
    }

    public void deltaScrollPos(double dy, NodeIcon icon) {
        if(dy < 0) {
            if(scrollPos > 0) {
                scrollPos--;
                icon.negativeBump();
            }
        } else {
            if(lastLookingAt == null) return;
            ClientNode node = nodeMap.get(lastLookingAt);
            if(scrollPos < node.peridot().size()-1) {
                scrollPos++;
                icon.bump();
            }
        }
    }

    public Optional<BlockPos> getSelectedPos() {
        if(selectedPos == null) return Optional.empty();
        if(!nodeMap.containsKey(selectedPos)) {
            selectedPos = null;
            return Optional.empty();
        }
        return Optional.of(selectedPos);
    }

    public void resetScrollPos() {
        scrollPos = 0;
    }

    public void selectNode(BlockPos pos) {
        ClientNode node = nodeMap.get(pos);
        if(node == null) return;
        if(node.peridot().isEmpty()) return;
        selectedPos = pos;
        selectedScrollPos = scrollPos;
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
        clearMap();
    }

    public void clearMap() {
        nodeMap.clear();
    }
}
