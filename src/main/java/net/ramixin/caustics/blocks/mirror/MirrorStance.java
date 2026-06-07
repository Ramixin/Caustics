package net.ramixin.caustics.blocks.mirror;

import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum MirrorStance implements StringRepresentable {
    UP("up", -45),
    DOWN("down", 45),
    FRONT("front", 0);

    public final String name;
    public final int reflectionYaw;
    MirrorStance(String name, int reflectionYaw) {
        this.name = name;
        this.reflectionYaw = reflectionYaw;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public @NonNull String getSerializedName() {
        return this.name;
    }
}
