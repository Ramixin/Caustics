package net.ramixin.caustics.nodes.core;

import net.minecraft.core.BlockPos;
import net.ramixin.caustics.nodes.CrystalNode;

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
        for(CrystalNode node : worker.getNodes())
            indexNode(node);
    }

    protected void indexNode(CrystalNode node) {
        Type.SAPPHIRE.index(node, this);
        Type.SUNSTONE.index(node, this);
        Type.TOPAZ.index(node, this);
        Type.TOURMALINE.index(node, this);
        Type.PERIDOT.index(node, this);
    }

    protected void unindexNode(CrystalNode node) {
        Type.SAPPHIRE.unindex(node, this);
        Type.SUNSTONE.unindex(node, this);
        Type.TOPAZ.unindex(node, this);
        Type.TOURMALINE.unindex(node, this);
        Type.PERIDOT.unindex(node, this);
    }

    protected Optional<CrystalNode> getNodeAt(BlockPos pos, Type type) {
        NodeVariant variant = typedMap.get(pos);
        if(variant == null)
            return Optional.empty();
        if(!variant.isType(type))
            return Optional.empty();
        return Optional.of(variant.node());
    }

    protected Optional<CrystalNode> getNodeAt(BlockPos pos) {
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

    protected enum Type {
        SAPPHIRE(NodeVariant.Sapphire::new, node -> node.data().sapphireClusters()),
        SUNSTONE(NodeVariant.Sunstone::new, node -> node.data().sunstoneClusters()),
        TOPAZ(NodeVariant.Topaz::new, node -> node.data().topazClusters()),
        TOURMALINE(NodeVariant.Tourmaline::new, node -> node.data().tourmalineClusters()),
        PERIDOT(NodeVariant.Peridot::new, node -> node.data().peridotClusters())

        ;

        private final Function<CrystalNode, NodeVariant> variantFactory;
        private final Function<CrystalNode, Set<BlockPos>> positionsFactory;

        Type(Function<CrystalNode, NodeVariant> variantFactory, Function<CrystalNode, Set<BlockPos>> positionsFactory) {
            this.variantFactory = variantFactory;
            this.positionsFactory = positionsFactory;
        }

        public void index(CrystalNode node, NodeIndex index) {
            NodeVariant variant = this.variantFactory.apply(node);
            Set<BlockPos> positions = this.positionsFactory.apply(node);
            for(BlockPos pos : positions)
                index.typedMap.put(pos, variant);
        }

        public void unindex(CrystalNode node, NodeIndex index) {
            Set<BlockPos> positions = this.positionsFactory.apply(node);
            for(BlockPos pos : positions)
                index.typedMap.remove(pos);
        }
    }

    private sealed interface NodeVariant {

        Type type();

        default boolean isType(Type type) {
            return this.type() == type;
        }

        CrystalNode node();
        
        record Sapphire(CrystalNode node) implements NodeVariant {
            @Override
            public Type type() {
                return Type.SAPPHIRE;
            }
        }
        record Sunstone(CrystalNode node) implements NodeVariant {
            @Override
            public Type type() {
                return Type.SUNSTONE;
            }
        }
        record Topaz(CrystalNode node) implements NodeVariant {
            @Override
            public Type type() {
                return Type.TOPAZ;
            }
        }
        record Tourmaline(CrystalNode node) implements NodeVariant {
            @Override
            public Type type() {
                return Type.TOURMALINE;
            }
        }
        record Peridot(CrystalNode node) implements NodeVariant {
            @Override
            public Type type() {
                return Type.PERIDOT;
            }
        }

    }
}
