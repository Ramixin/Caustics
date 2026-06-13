package net.ramixin.caustics.nodes.routing;

import net.minecraft.core.BlockPos;

import java.util.*;

public class Route {
    private final List<BlockPos> path;

    protected Route(BlockPos pos, Map<BlockPos, BlockPos> searchTree) {
        List<BlockPos> path = new ArrayList<>();
        path.add(pos);
        BlockPos last = searchTree.get(pos);
        while(last != null) {
            path.add(last);
            last = searchTree.get(last);
        }
        Collections.reverse(path);
        this.path = path;
    }

    public Route(List<BlockPos> path) {
        this.path = path;
    }

    public Route() {
        this(new ArrayList<>());
    }

    public Route extend(BlockPos pos) {
        List<BlockPos> newPath = new ArrayList<>(path);
        newPath.add(pos);
        return new Route(newPath);
    }

    public List<BlockPos> immutablePath() {
        return List.copyOf(path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return "Route[" +
                "path=" + path + ']';
    }


}
