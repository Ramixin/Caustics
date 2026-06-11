package net.ramixin.caustics.client.mixins;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.ramixin.caustics.client.ducks.GuiGraphicsExtractorDuck;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorMixin implements GuiGraphicsExtractorDuck {

    @Shadow
    private @Nullable Runnable deferredTooltip;
    @Unique
    private final List<Consumer<GuiGraphicsExtractor>> tooltipBatches = new ArrayList<>();

    @Unique
    @Override
    public void caustics$enableTooltipBatching() {
        Runnable original = this.deferredTooltip;
        this.deferredTooltip = () -> {
            if (original != null) original.run();
            for (Consumer<GuiGraphicsExtractor> runnable : this.tooltipBatches)
                runnable.accept((GuiGraphicsExtractor)(Object)this);
        };
    }

    @Unique
    @Override
    public void caustics$addTooltipToBatch(Consumer<GuiGraphicsExtractor> runnable) {
        tooltipBatches.add(runnable);
    }
}
