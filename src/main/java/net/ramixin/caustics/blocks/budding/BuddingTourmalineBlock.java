package net.ramixin.caustics.blocks.budding;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.AmethystBlock;
import net.ramixin.caustics.blocks.ModBlocks;
import org.jspecify.annotations.NonNull;

public class BuddingTourmalineBlock extends BuddingModBlock {

    public static final MapCodec<BuddingTourmalineBlock> CODEC = simpleCodec(BuddingTourmalineBlock::new);

    public BuddingTourmalineBlock(Properties properties) {
        super(properties, () -> ModBlocks.TOURMALINE_GROUP);
    }

    @Override
    public @NonNull MapCodec<? extends AmethystBlock> codec() {
        return CODEC;
    }
}
