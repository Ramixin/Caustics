package net.ramixin.caustics.nodes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public record NodeData(List<BlockPos> sapphireClusters, List<BlockPos> peridotClusters, List<BlockPos> topazClusters, List<BlockPos> sunstoneClusters, List<BlockPos> seleniteClusters, List<BlockPos> tourmalineClusters) {

    public static final Codec<NodeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().fieldOf("sapphireClusters").forGetter(NodeData::sapphireClusters),
            BlockPos.CODEC.listOf().fieldOf("peridotClusters").forGetter(NodeData::peridotClusters),
            BlockPos.CODEC.listOf().fieldOf("topazClusters").forGetter(NodeData::topazClusters),
            BlockPos.CODEC.listOf().fieldOf("sunstoneClusters").forGetter(NodeData::sunstoneClusters),
            BlockPos.CODEC.listOf().fieldOf("seleniteClusters").forGetter(NodeData::seleniteClusters),
            BlockPos.CODEC.listOf().fieldOf("tourmalineClusters").forGetter(NodeData::tourmalineClusters)
    ).apply(instance, NodeData::new));

    public NodeData() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public NodeData toImmutable() {
        return new NodeData(List.copyOf(sapphireClusters), List.copyOf(peridotClusters), List.copyOf(topazClusters), List.copyOf(sunstoneClusters), List.copyOf(seleniteClusters), List.copyOf(tourmalineClusters));
    }
}
