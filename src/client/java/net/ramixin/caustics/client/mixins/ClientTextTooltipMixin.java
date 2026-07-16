package net.ramixin.caustics.client.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.ramixin.caustics.client.ducks.GuiGraphicsExtractorDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientTextTooltip.class)
public class ClientTextTooltipMixin {

    @ModifyArg(method = "extractText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V"), index = 4)
    private static int changeTextAlpha(int original, @Local(argsOnly = true, name = "graphics") GuiGraphicsExtractor graphics) {
        GuiGraphicsExtractorDuck duck = GuiGraphicsExtractorDuck.get(graphics);
        if(duck.caustics$getAlpha() == 255) return original;
        return duck.caustics$getAlpha() << 24 | 0xFFFFFF;
    }

}
