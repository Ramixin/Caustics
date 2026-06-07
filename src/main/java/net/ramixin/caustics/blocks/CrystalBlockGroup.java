package net.ramixin.caustics.blocks;

import net.minecraft.world.level.block.Block;

import java.util.Optional;

public record CrystalBlockGroup(Block block, Block buddingBlock, Block cluster, Block largeBud, Block mediumBud, Block smallBud) {

    public Optional<Block> nextStage(Block current) {
        if(current == smallBud) return Optional.of(mediumBud);
        if(current == mediumBud) return Optional.of(largeBud);
        if(current == largeBud) return Optional.of(cluster);
        return Optional.empty();
    }

}
