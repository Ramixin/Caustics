package net.ramixin.caustics.client.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.ramixin.caustics.client.entities.ClientLeapGhost;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    @Final
    private PlayerSkinRenderCache playerSkinRenderCache;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/ClientMannequin;registerOverrides(Lnet/minecraft/client/renderer/PlayerSkinRenderCache;)V"))
    private void overrideEntityFactoryForLeapGhost(GameConfig gameConfig, CallbackInfo ci) {
        ClientLeapGhost.overrideFactory(this.playerSkinRenderCache);
    }

}
