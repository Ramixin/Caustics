package net.ramixin.caustics.blocks.budding;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.AmethystBlock;
import net.ramixin.caustics.blocks.ModBlocks;
import org.jspecify.annotations.NonNull;

public class BuddingSunstoneBlock extends BuddingModBlock {

    public static final MapCodec<BuddingSunstoneBlock> CODEC = simpleCodec(BuddingSunstoneBlock::new);

    public BuddingSunstoneBlock(Properties properties) {
        super(properties, () -> ModBlocks.SUNSTONE_GROUP);
    }

    @Override
    public @NonNull MapCodec<? extends AmethystBlock> codec() {
        return CODEC;
    }
}
