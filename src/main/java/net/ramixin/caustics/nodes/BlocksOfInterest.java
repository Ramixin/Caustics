package net.ramixin.caustics.nodes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public record BlocksOfInterest(List<BlockPos> sapphireClusters, List<BlockPos> cinnabarClusters, List<BlockPos> peridotClusters, List<BlockPos> topazClusters, List<BlockPos> sunstoneClusters, List<BlockPos> seleniteClusters, List<BlockPos> tourmalineClusters) {

    public static final Codec<BlocksOfInterest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().fieldOf("sapphireClusters").forGetter(BlocksOfInterest::sapphireClusters),
            BlockPos.CODEC.listOf().fieldOf("cinnabarClusters").forGetter(BlocksOfInterest::cinnabarClusters),
            BlockPos.CODEC.listOf().fieldOf("peridotClusters").forGetter(BlocksOfInterest::peridotClusters),
            BlockPos.CODEC.listOf().fieldOf("topazClusters").forGetter(BlocksOfInterest::topazClusters),
            BlockPos.CODEC.listOf().fieldOf("sunstoneClusters").forGetter(BlocksOfInterest::sunstoneClusters),
            BlockPos.CODEC.listOf().fieldOf("seleniteClusters").forGetter(BlocksOfInterest::seleniteClusters),
            BlockPos.CODEC.listOf().fieldOf("tourmalineClusters").forGetter(BlocksOfInterest::tourmalineClusters)
    ).apply(instance, BlocksOfInterest::new));

    public BlocksOfInterest() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public BlocksOfInterest toImmutable() {
        return new BlocksOfInterest(List.copyOf(sapphireClusters), List.copyOf(cinnabarClusters), List.copyOf(peridotClusters), List.copyOf(topazClusters), List.copyOf(sunstoneClusters), List.copyOf(seleniteClusters), List.copyOf(tourmalineClusters));
    }
}
