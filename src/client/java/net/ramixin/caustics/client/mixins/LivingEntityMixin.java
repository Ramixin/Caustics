package net.ramixin.caustics.client.mixins;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.ramixin.caustics.client.CausticsClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "stopUsingItem", at = @At("HEAD"))
    private void closeHudIfStopUsing(CallbackInfo ci) {
        if(((LivingEntity)(Object)this) instanceof AbstractClientPlayer) {
            CausticsClient.hud = null;
        }
    }

}
