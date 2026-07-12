package net.ramixin.caustics.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.ramixin.caustics.client.ducks.GuiGraphicsExtractorDuck;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class TooltipRenderer {

    private final Consumer<Consumer<GuiGraphicsExtractor>> tooltipBatcher;
    private final Font textRenderer;
    private final MutableInt height = new MutableInt();
    private final Mutable<Function<List<Component>, Integer>> alignment = new MutableObject<>(null);

    public TooltipRenderer(GuiGraphicsExtractor context) {
        GuiGraphicsExtractorDuck duck = GuiGraphicsExtractorDuck.get(context);
        duck.caustics$enableTooltipBatching();
        this.tooltipBatcher = duck::caustics$addTooltipToBatch;
        this.textRenderer = Minecraft.getInstance().font;
    }

    public void render(List<Component> text, int yOffset) {
        tooltipBatcher.accept(context -> {
            if(alignment.get() == null) throw new IllegalStateException("TooltipRenderer alignment not set");
            context.tooltip(
                    textRenderer,
                    text.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList(),
                    alignment.get().apply(text),
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
        tooltipBatcher.accept(_ -> height.setValue(0));
    }

    public void leftAlign(int xOffset) {
        tooltipBatcher.accept(_ -> alignment.setValue((_) -> xOffset));
    }

    public void rightAlign(int xOffset) {
        tooltipBatcher.accept(_ -> alignment.setValue((text) -> rightAlignInternal(xOffset, text)));
    }

    private int rightAlignInternal(int xOffset, List<Component> text) {
        int width = 0;
        for(Component component : text) {
            int componentWidth = getTextWidth(component);
            if(componentWidth > width)
                width = componentWidth;
        }
        return xOffset - width;
    }

    public int getTextWidth(Component text) {
        return textRenderer.width(text);
    }

}
