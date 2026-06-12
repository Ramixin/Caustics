package net.ramixin.caustics.nodes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record NodeData(Set<BlockPos> sapphireClusters, Set<BlockPos> peridotClusters, Set<BlockPos> topazClusters, Set<BlockPos> sunstoneClusters, Set<BlockPos> seleniteClusters, Set<BlockPos> tourmalineClusters) {

    public static final Codec<NodeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().fieldOf("sapphireClusters").forGetter(NodeData::sapphireList),
            BlockPos.CODEC.listOf().fieldOf("peridotClusters").forGetter(NodeData::peridotList),
            BlockPos.CODEC.listOf().fieldOf("topazClusters").forGetter(NodeData::topazList),
            BlockPos.CODEC.listOf().fieldOf("sunstoneClusters").forGetter(NodeData::sunstoneList),
            BlockPos.CODEC.listOf().fieldOf("seleniteClusters").forGetter(NodeData::seleniteList),
            BlockPos.CODEC.listOf().fieldOf("tourmalineClusters").forGetter(NodeData::tourmalineList)
    ).apply(instance, NodeData::new));

    public NodeData() {
        this(new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>());
    }

    public NodeData(List<BlockPos> blockPos, List<BlockPos> blockPos1, List<BlockPos> blockPos2, List<BlockPos> blockPos3, List<BlockPos> blockPos4, List<BlockPos> blockPos5) {
        this(Set.copyOf(blockPos), Set.copyOf(blockPos1), Set.copyOf(blockPos2), Set.copyOf(blockPos3), Set.copyOf(blockPos4), Set.copyOf(blockPos5));
    }

    public List<BlockPos> sapphireList() {
        return List.copyOf(sapphireClusters);
    }

    public List<BlockPos> peridotList() {
        return List.copyOf(peridotClusters);
    }

    public List<BlockPos> topazList() {
        return List.copyOf(topazClusters);
    }

    public List<BlockPos> sunstoneList() {
        return List.copyOf(sunstoneClusters);
    }

    public List<BlockPos> seleniteList() {
        return List.copyOf(seleniteClusters);
    }

    public List<BlockPos> tourmalineList() {
        return List.copyOf(tourmalineClusters);
    }
}
