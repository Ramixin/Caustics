package net.ramixin.caustics.client.ducks;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.Consumer;

public interface GuiGraphicsExtractorDuck {

    void caustics$enableTooltipBatching();

    void caustics$addTooltipToBatch(Consumer<GuiGraphicsExtractor> tooltip);

    void caustics$setAlpha(int alpha);

    int caustics$getAlpha();

    static GuiGraphicsExtractorDuck get(GuiGraphicsExtractor context) {
        return (GuiGraphicsExtractorDuck) context;
    }

}
