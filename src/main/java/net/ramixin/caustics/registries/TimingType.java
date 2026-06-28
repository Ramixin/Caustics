package net.ramixin.caustics.registries;

import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum TimingType implements StringRepresentable {
    QUICK,
    NORMAL,
    SLOW

    ;

    private final String name;

    TimingType() {
        this.name = this.toString().toLowerCase();
    }

    @Override
    public @NonNull String getSerializedName() {
        return name;
    }
}
