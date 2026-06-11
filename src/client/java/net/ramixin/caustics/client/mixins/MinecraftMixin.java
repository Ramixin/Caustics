package net.ramixin.caustics.client.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.ramixin.caustics.client.CausticsClient;
import net.ramixin.caustics.items.ModItems;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MinecraftMixin {


    @Shadow
    @Final
    public Options options;

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
        CausticsClient.onAlidadeAttack(instance);
        return true;
    }

    @Unique
    private boolean canAlidadeAttack(LocalPlayer instance, boolean orig) {
        if(!orig) return false;
        if(!this.options.keyUse.isDown()) return false;
        if(!this.options.keyAttack.isDown()) return false;
        return instance.getMainHandItem().is(ModItems.ALIDADE);
    }

}
