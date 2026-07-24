package net.ramixin.caustics.client.mixins;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.ramixin.caustics.client.CausticsClient;
import net.ramixin.caustics.client.rendering.huds.ListeningHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow
    public abstract double getScaledYPos(Window window);

    @Shadow
    public abstract double getScaledXPos(Window window);

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void stealMouseClickIfListeningHud(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        if(handle != this.minecraft.getWindow().handle()) return;
        if(CausticsClient.hud instanceof ListeningHud hud) {
            Window window = this.minecraft.getWindow();
            double xm = this.getScaledXPos(window);
            double ym = this.getScaledYPos(window);
            if(hud.mouseClicked(new MouseButtonEvent(xm, ym, rawButtonInfo), action != 1))
                ci.cancel();
        }
    }

}
