package net.ramixin.caustics.client.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.resources.Identifier;
import net.ramixin.caustics.client.ducks.GuiGraphicsExtractorDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TooltipRenderUtil.class)
public abstract class TooltipRenderUtilMixin {

    @WrapOperation(method = "extractTooltipBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private static void changeTooltipAlpha(GuiGraphicsExtractor instance, RenderPipeline renderPipeline, Identifier location, int x, int y, int width, int height, Operation<Void> original) {
        GuiGraphicsExtractorDuck duck = GuiGraphicsExtractorDuck.get(instance);
        int alpha = duck.caustics$getAlpha();
        if(alpha == 255) {
            original.call(instance, renderPipeline, location, x, y, width, height);
            return;
        }
        int color = alpha << 24 | 0xFFFFFF;
        instance.blitSprite(renderPipeline, location, x, y, width, height, color);
    }

}
