package net.ramixin.caustics.blocks.mirror;

import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum MirrorStance implements StringRepresentable {
    UP("up"),
    DOWN("down"),
    FRONT("front");

    public final String name;

    MirrorStance(String name) {
        this.name = name;
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
