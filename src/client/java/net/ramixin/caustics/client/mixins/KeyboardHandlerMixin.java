package net.ramixin.caustics.client.mixins;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
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
            if(hud.charTyped(event))
                ci.cancel();
        }
    }

    @Definition(id = "minecraft", field = "Lnet/minecraft/client/KeyboardHandler;minecraft:Lnet/minecraft/client/Minecraft;")
    @Definition(id = "screen", field = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;")
    @Expression("this.minecraft.screen == null")
    @ModifyExpressionValue(method = "keyPress", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean controlKeyboardIfActive(boolean handlesGameInput, @Local(argsOnly = true, name = "event") KeyEvent event) {
        if(!handlesGameInput) return false;
        if(!(CausticsClient.hud instanceof ListeningHud hud)) return true;
        return !hud.keyPressed(event);
    }

}
