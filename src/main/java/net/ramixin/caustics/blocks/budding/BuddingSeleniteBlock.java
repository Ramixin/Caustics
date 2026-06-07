package net.ramixin.caustics.blocks.budding;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.AmethystBlock;
import net.ramixin.caustics.blocks.ModBlocks;
import org.jspecify.annotations.NonNull;

public class BuddingSeleniteBlock extends BuddingModBlock {

    public static final MapCodec<BuddingSeleniteBlock> CODEC = simpleCodec(BuddingSeleniteBlock::new);

    public BuddingSeleniteBlock(Properties properties) {
        super(properties, () -> ModBlocks.SELENITE_GROUP);
    }

    @Override
    public @NonNull MapCodec<? extends AmethystBlock> codec() {
        return CODEC;
    }
}
