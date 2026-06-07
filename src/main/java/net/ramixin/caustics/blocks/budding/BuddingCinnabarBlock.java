package net.ramixin.caustics.blocks.budding;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.AmethystBlock;
import net.ramixin.caustics.blocks.ModBlocks;
import org.jspecify.annotations.NonNull;

public class BuddingCinnabarBlock extends BuddingModBlock {

    public static final MapCodec<BuddingCinnabarBlock> CODEC = simpleCodec(BuddingCinnabarBlock::new);

    public BuddingCinnabarBlock(Properties properties) {
        super(properties, () -> ModBlocks.CINNABAR_GROUP);
    }

    @Override
    public @NonNull MapCodec<? extends AmethystBlock> codec() {
        return CODEC;
    }
}
