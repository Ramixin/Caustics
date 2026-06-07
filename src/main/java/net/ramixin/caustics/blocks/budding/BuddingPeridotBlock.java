package net.ramixin.caustics.blocks.budding;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.AmethystBlock;
import net.ramixin.caustics.blocks.ModBlocks;
import org.jspecify.annotations.NonNull;

public class BuddingPeridotBlock extends BuddingModBlock {

    public static final MapCodec<BuddingPeridotBlock> CODEC = simpleCodec(BuddingPeridotBlock::new);

    public BuddingPeridotBlock(Properties properties) {
        super(properties, () -> ModBlocks.PERIDOT_GROUP);
    }

    @Override
    public @NonNull MapCodec<? extends AmethystBlock> codec() {
        return CODEC;
    }
}
