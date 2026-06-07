package net.ramixin.caustics.nodes;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerLevel;
import net.ramixin.caustics.nodes.builders.NodeBuilder;
import org.jspecify.annotations.NonNull;

public record CrystalNode(BlocksOfInterest blocks, NodeBuilder builder) {

    public static final Codec<CrystalNode> CODEC = BlocksOfInterest.CODEC.xmap(
            blocksOfInterest -> new CrystalNode(blocksOfInterest, new NodeBuilder(blocksOfInterest.sapphireClusters())),
            CrystalNode::blocks
    );

    public void tick(ServerLevel level) {
        builder.tick(level);
    }

    @Override
    public @NonNull String toString() {
        return "sapphire clusters: " + blocks.sapphireClusters();
    }
}
