package net.ramixin.caustics.nodes.routing;

import net.minecraft.core.BlockPos;
import net.ramixin.caustics.nodes.Node;

import java.util.List;
import java.util.Optional;

public class NodeMappedRoute extends Route {

    private final List<Node> pathNodes;
    private final Node sapphireNode;

    protected NodeMappedRoute(List<BlockPos> path, BlockPos sapphirePos, List<Node> pathNodes, Node sapphireNode) {
        super(path, sapphirePos);
        this.pathNodes = pathNodes;
        this.sapphireNode = sapphireNode;
    }

    public List<Node> nodes() {
        return pathNodes;
    }

    public int calculateLightLost() {
        int total = 0;
        int max = pathNodes.size() * 15 + 15;
        for(int i = 0; i < pathNodes.size(); i++) {
            Node node = pathNodes.get(i);
            BlockPos pos = path.get(i);
            Optional<Integer> maybeLight = node.getLightLevelAt(pos);
            if(maybeLight.isEmpty()) continue;
            total += maybeLight.get();
        }
        Optional<Integer> maybeSapphireLight = sapphireNode.getLightLevelAt(sapphirePos);
        if(maybeSapphireLight.isEmpty()) return max - total - 15;
        total += maybeSapphireLight.get();
        return max - total;
    }
}
