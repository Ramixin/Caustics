package net.ramixin.caustics.blocks.budding;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.AmethystBlock;
import net.ramixin.caustics.blocks.ModBlocks;
import org.jspecify.annotations.NonNull;

public class BuddingSapphireBlock extends BuddingModBlock {

    public static final MapCodec<BuddingSapphireBlock> CODEC = simpleCodec(BuddingSapphireBlock::new);

    public BuddingSapphireBlock(Properties properties) {
        super(properties, () -> ModBlocks.SAPPHIRE_GROUP);
    }

    @Override
    public @NonNull MapCodec<? extends AmethystBlock> codec() {
        return CODEC;
    }
}
