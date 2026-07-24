package net.ramixin.caustics.client.rendering.huds;

import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public interface ListeningHud {

    default boolean charTyped(final CharacterEvent event) {
        return false;
    }

    default boolean mouseClicked(final MouseButtonEvent event, boolean released) {
        return false;
    }

    default boolean mouseScrolled(double dy) {
        return false;
    }

    default boolean keyPressed(KeyEvent event) {
        return false;
    }
}
