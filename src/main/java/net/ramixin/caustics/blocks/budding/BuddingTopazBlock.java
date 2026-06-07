package net.ramixin.caustics.blocks.budding;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.AmethystBlock;
import net.ramixin.caustics.blocks.ModBlocks;
import org.jspecify.annotations.NonNull;

public class BuddingTopazBlock extends BuddingModBlock {

    public static final MapCodec<BuddingTopazBlock> CODEC = simpleCodec(BuddingTopazBlock::new);

    public BuddingTopazBlock(Properties properties) {
        super(properties, () -> ModBlocks.TOPAZ_GROUP);
    }

    @Override
    public @NonNull MapCodec<? extends AmethystBlock> codec() {
        return CODEC;
    }
}
