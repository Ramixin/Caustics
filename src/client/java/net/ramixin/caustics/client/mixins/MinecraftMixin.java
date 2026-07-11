package net.ramixin.caustics.client.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.ramixin.caustics.client.CausticsClient;
import net.ramixin.caustics.client.entities.ClientLeapGhost;
import net.ramixin.caustics.items.components.SpyglassLens;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    @Final
    public Options options;

    @Shadow
    @Final
    private PlayerSkinRenderCache playerSkinRenderCache;
    @Unique
    private boolean alidadeAttacking = false;

    @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z", ordinal = 0))
    private boolean checkIfAttackedWhileUsingAlidade(LocalPlayer instance, Operation<Boolean> original) {
        boolean orig = original.call(instance);
        if(!canAlidadeAttack(instance, orig)) {
            alidadeAttacking = false;
            return orig;
        }
        if(alidadeAttacking) return true; //orig can only be true
        alidadeAttacking = true;
        CausticsClient.onAlidadeAttack();
        return true;
    }

    @Unique
    private boolean canAlidadeAttack(LocalPlayer instance, boolean orig) {
        if(!orig) return false;
        if(!this.options.keyUse.isDown()) return false;
        if(!this.options.keyAttack.isDown()) return false;
        return SpyglassLens.isAlidade(instance.getUseItem());
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/ClientMannequin;registerOverrides(Lnet/minecraft/client/renderer/PlayerSkinRenderCache;)V"))
    private void overrideEntityFactoryForLeapGhost(GameConfig gameConfig, CallbackInfo ci) {
        ClientLeapGhost.overrideFactory(this.playerSkinRenderCache);
    }

}
