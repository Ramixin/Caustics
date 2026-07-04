package net.ramixin.caustics.nodes.core;

import net.minecraft.core.BlockPos;
import net.ramixin.caustics.nodes.Node;
import net.ramixin.caustics.nodes.NodeData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NodeIndex {

    private final Map<BlockPos, NodeVariant> typedMap = new HashMap<>();

    protected NodeIndex() {}

    protected NodeIndex(NodeWorker worker) {
        for(Node node : worker.getNodes())
            indexNode(node);
    }

    protected void indexNode(Node node) {
        Type.SAPPHIRE.index(node, this);
        Type.SUNSTONE.index(node, this);
        Type.TOPAZ.index(node, this);
        Type.TOURMALINE.index(node, this);
        Type.PERIDOT.index(node, this);
    }

    protected void unindexNode(Node node) {
        Type.SAPPHIRE.unindex(node, this);
        Type.SUNSTONE.unindex(node, this);
        Type.TOPAZ.unindex(node, this);
        Type.TOURMALINE.unindex(node, this);
        Type.PERIDOT.unindex(node, this);
    }

    protected Optional<Node> getNodeAt(BlockPos pos, Type type) {
        NodeVariant variant = typedMap.get(pos);
        if(variant == null)
            return Optional.empty();
        if(!variant.isType(type))
            return Optional.empty();
        return Optional.of(variant.node());
    }

    protected Optional<Node> getNodeAt(BlockPos pos) {
        NodeVariant variant = typedMap.get(pos);
        if(variant == null)
            return Optional.empty();
        return Optional.of(variant.node());
    }

    protected Set<BlockPos> getPositionsOfType(Type type) {
        return typedMap.entrySet().stream()
                .filter(entry -> entry.getValue().isType(type))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    protected void clear() {
        typedMap.clear();
    }

    public enum Type {
        SAPPHIRE(NodeVariant.Sapphire::new, NodeData::sapphireClusters),
        SUNSTONE(NodeVariant.Sunstone::new, NodeData::sunstoneClusters),
        TOPAZ(NodeVariant.Topaz::new, NodeData::topazClusters),
        TOURMALINE(NodeVariant.Tourmaline::new, NodeData::tourmalineClusters),
        PERIDOT(NodeVariant.Peridot::new, NodeData::peridotClusters)

        ;

        private final Function<Node, NodeVariant> variantFactory;
        private final Function<NodeData, Set<BlockPos>> positionsFactory;

        Type(Function<Node, NodeVariant> variantFactory, Function<NodeData, Set<BlockPos>> positionsFactory) {
            this.variantFactory = variantFactory;
            this.positionsFactory = positionsFactory;
        }

        public void index(Node node, NodeIndex index) {
            NodeVariant variant = this.variantFactory.apply(node);
            Set<BlockPos> positions = this.positionsFactory.apply(node.data());
            for(BlockPos pos : positions)
                index.typedMap.put(pos, variant);
        }

        public void unindex(Node node, NodeIndex index) {
            Set<BlockPos> positions = this.positionsFactory.apply(node.data());
            for(BlockPos pos : positions)
                index.typedMap.remove(pos);
        }
    }

    private sealed interface NodeVariant {

        Type type();

        default boolean isType(Type type) {
            return this.type() == type;
        }

        Node node();
        
        record Sapphire(Node node) implements NodeVariant {
            @Override
            public Type type() {
                return Type.SAPPHIRE;
            }
        }
        record Sunstone(Node node) implements NodeVariant {
            @Override
            public Type type() {
                return Type.SUNSTONE;
            }
        }
        record Topaz(Node node) implements NodeVariant {
            @Override
            public Type type() {
                return Type.TOPAZ;
            }
        }
        record Tourmaline(Node node) implements NodeVariant {
            @Override
            public Type type() {
                return Type.TOURMALINE;
            }
        }
        record Peridot(Node node) implements NodeVariant {
            @Override
            public Type type() {
                return Type.PERIDOT;
            }
        }

    }
}
