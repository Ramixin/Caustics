package net.ramixin.caustics.client.nodes.icons;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class NodeIcon {

    public static final RandomSource RANDOM = RandomSource.create();

    private final BlockPos pos;
    private int lookedAt;
    private int previousLookedAt;

    public NodeIcon(BlockPos pos) {
        this.pos = pos;
    }

    public Vec3 getCenterPos() {
        return pos.getCenter();
    }

    public void tick(boolean lookingAt) {
        previousLookedAt = lookedAt;
        if(lookingAt && lookedAt < 2)
            lookedAt++;
        else if(!lookingAt && lookedAt > 0)
            lookedAt--;
    }

    public void bump() {

    }

    public void negativeBump() {

    }

    public int lookedAt() {
        return lookedAt;
    }

    public int previousLookedAt() {
        return previousLookedAt;
    }
}
