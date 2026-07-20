package net.ramixin.caustics.client.mixins;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.ramixin.caustics.client.CausticsClient;
import net.ramixin.caustics.client.rendering.huds.ListeningHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void stealKeyTypedIfListeningHud(long handle, CharacterEvent event, CallbackInfo ci) {
        if(handle != this.minecraft.getWindow().handle()) return;
        if(CausticsClient.hud instanceof ListeningHud hud) {
            hud.charTyped(event);
            ci.cancel();
        }
    }

}
