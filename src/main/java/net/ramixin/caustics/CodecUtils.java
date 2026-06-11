package net.ramixin.caustics;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;

public interface CodecUtils {

    Codec<BlockPos> STRINGABLE_BLOCK_POS_CODEC = Codec.STRING.xmap(
            s -> {
                String[] splits = s.split(",");
                int x = Integer.parseInt(splits[0].substring(1));
                int y = Integer.parseInt(splits[1]);
                int z = Integer.parseInt(splits[2].substring(0, splits[2].length() - 1));
                return new BlockPos(x, y, z);
            },
            pos -> "[" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "]"
    );
}
