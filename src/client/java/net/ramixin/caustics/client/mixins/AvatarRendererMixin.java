package net.ramixin.caustics.client.mixins;

import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.item.ItemStack;
import net.ramixin.caustics.client.CausticsClient;
import net.ramixin.caustics.client.entities.ClientLeapGhost;
import net.ramixin.caustics.items.components.LeaperMaterial;
import net.ramixin.caustics.items.components.ModDataComponents;
import net.ramixin.caustics.utils.LeaperUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private <AvatarLikeEntity extends Avatar & ClientAvatarEntity> void setLeapPercentageIfLeapGhost(AvatarLikeEntity entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        if(!(entity instanceof ClientLeapGhost ghost)) return;
        state.setData(CausticsClient.OPACITY_DEFAULT_KEY, 0d);
        ItemStack stack = ghost.getUseItem();
        LeaperMaterial material = stack.get(ModDataComponents.LEAPER_MATERIAL);
        if(material == null) return;
        int chargeUpTicks = LeaperUtil.getChargeUpTicks(stack);
        state.setData(CausticsClient.GHOST_PROGRESS_KEY, (double) ghost.getTicksUsingItem() / chargeUpTicks);
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private <AvatarLikeEntity extends Avatar & ClientAvatarEntity> void setLeapPercentageIfPlayer(AvatarLikeEntity entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        if(!(entity instanceof AbstractClientPlayer player)) return;
        state.setData(CausticsClient.OPACITY_DEFAULT_KEY, 1d);
        ItemStack stack = player.getUseItem();
        LeaperMaterial material = stack.get(ModDataComponents.LEAPER_MATERIAL);
        if(material == null) return;
        int chargeUpTicks = LeaperUtil.getChargeUpTicks(stack);
        double progress = (double) player.getTicksUsingItem() / chargeUpTicks;
        state.setData(CausticsClient.PLAYER_PROGRESS_KEY, 1 - progress);
    }

}
