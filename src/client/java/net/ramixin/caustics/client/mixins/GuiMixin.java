package net.ramixin.caustics.client.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.ramixin.caustics.client.CausticsClient;
import net.ramixin.caustics.items.components.SpyglassLens;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private boolean usingAlidade = false;
    @Unique
    private boolean usingDowser = false;
    @Unique
    private boolean usingCollimator = false;

    @Inject(method = "extractCameraOverlays", at = @At("HEAD"))
    private void updateUsingSpecialSpyglassDuringExtraction(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        assert this.minecraft.player != null;
        usingAlidade = SpyglassLens.isAlidade(this.minecraft.player.getUseItem());
        usingDowser = SpyglassLens.isDowser(this.minecraft.player.getUseItem());
        usingCollimator = SpyglassLens.isCollimator(this.minecraft.player.getUseItem());
    }

    @WrapOperation(method = "extractSpyglassOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"))
    private void changeScopingTextureIfUsingSpecialSpyglass(GuiGraphicsExtractor instance, RenderPipeline renderPipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, Operation<Void> original) {
        if(usingAlidade)
            texture = CausticsClient.ALIDADE_GUI_TEXTURE;
        else if(usingDowser)
            texture = CausticsClient.DOWSER_GUI_TEXTURE;
        else if(usingCollimator)
            texture = CausticsClient.COLLIMATOR_GUI_TEXTURE;
        original.call(instance, renderPipeline, texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

}
