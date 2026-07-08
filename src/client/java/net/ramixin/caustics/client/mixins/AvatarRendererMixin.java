package net.ramixin.caustics.client.mixins;

import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.ramixin.caustics.client.CausticsClient;
import net.ramixin.caustics.client.ClientLeap;
import net.ramixin.caustics.client.entities.ClientLeapGhost;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private <AvatarLikeEntity extends Avatar & ClientAvatarEntity> void setOpacityIfLeapGhostOrPlayer(AvatarLikeEntity entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        if((entity instanceof ClientLeapGhost ghost))
            setOpacity(state, ghost.getProfileId(), false);
        else if(entity instanceof AbstractClientPlayer player)
            setOpacity(state, player.getUUID(), true);
    }

    @Unique
    private static void setOpacity(AvatarRenderState state, UUID uuid, boolean invert) {
        state.setData(CausticsClient.OPACITY_DEFAULT_KEY, invert ? 1d : 0d);
        Optional<ClientLeap> maybeLeap = ClientCrystalNetwork.getInstance().getLeap(uuid);
        if(maybeLeap.isEmpty()) return;
        double progress = maybeLeap.get().progress();
        state.setData(CausticsClient.OPACITY_KEY, invert ? 1d - progress : progress);
    }

}
