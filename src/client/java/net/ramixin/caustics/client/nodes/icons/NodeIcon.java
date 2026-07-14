package net.ramixin.caustics.client.nodes.icons;

import net.minecraft.core.BlockPos;

public abstract class NodeIcon {

    private final BlockPos pos;

    public NodeIcon(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }

    public abstract void tick(boolean lookingAt);

    public abstract void bump();

    public abstract void negativeBump();
}
