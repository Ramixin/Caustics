package net.ramixin.caustics.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.ramixin.caustics.client.ducks.GuiGraphicsExtractorDuck;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.function.Consumer;

public class TooltipRenderer {

    private final Consumer<Consumer<GuiGraphicsExtractor>> tooltipBatcher;
    private final Font textRenderer;
    private final MutableInt height = new MutableInt();

    public TooltipRenderer(GuiGraphicsExtractor context, Font textRenderer) {
        GuiGraphicsExtractorDuck duck = GuiGraphicsExtractorDuck.get(context);
        duck.caustics$enableTooltipBatching();
        this.tooltipBatcher = duck::caustics$addTooltipToBatch;
        this.textRenderer = textRenderer;
    }

    public void render(List<Component> text, int xOffset, int yOffset) {
        tooltipBatcher.accept(context -> {
            context.tooltip(
                    textRenderer,
                    text.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList(),
                    xOffset,
                    yOffset + (int) height.get(),
                    DefaultTooltipPositioner.INSTANCE,
                    null);
            height.add(yOffset);
            if(text.isEmpty()) return;
            if(text.size() == 1) height.add(16);
            else height.add(8 + text.size() * 10);
        });

    }

    public void resetHeight() {
        tooltipBatcher.accept(context -> height.setValue(0));
    }

    public int getTextWidth(String text) {
        return textRenderer.width(text);
    }

    public int getTextWidth(Component text) {
        return textRenderer.width(text);
    }

}
