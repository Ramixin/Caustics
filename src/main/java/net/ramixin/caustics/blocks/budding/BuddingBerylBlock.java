package net.ramixin.caustics.blocks.budding;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.AmethystBlock;
import net.ramixin.caustics.blocks.ModBlocks;
import org.jspecify.annotations.NonNull;

public class BuddingBerylBlock extends BuddingModBlock {

    public static final MapCodec<BuddingBerylBlock> CODEC = simpleCodec(BuddingBerylBlock::new);

    public BuddingBerylBlock(Properties properties) {
        super(properties, () -> ModBlocks.BERYL_GROUP);
    }

    @Override
    public @NonNull MapCodec<? extends AmethystBlock> codec() {
        return CODEC;
    }
}
