package net.ramixin.caustics.blocks.mirror;

import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum MirrorGrip implements StringRepresentable {
    STANDING("standing"),
    LEFT("left"),
    RIGHT("right"),
    UP("up"),

    BACK("back")

    ;

    public final String name;
    MirrorGrip(String name) {
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
