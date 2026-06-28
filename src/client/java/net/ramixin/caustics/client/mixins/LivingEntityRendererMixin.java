package net.ramixin.caustics.client.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.ramixin.caustics.client.CausticsClient;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @WrapMethod(method = "getRenderType")
    private <S extends LivingEntityRenderState> @Nullable RenderType forceTransparencyIfLeapRelatedEntity(S state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing, Operation<RenderType> original) {
        boolean leaping = state.getData(CausticsClient.GHOST_PROGRESS_KEY) != null || state.getData(CausticsClient.PLAYER_PROGRESS_KEY) != null;
        return original.call(state, isBodyVisible, forceTransparent || leaping, appearGlowing);
    }

    @ModifyReturnValue(method = "getModelTint", at = @At("RETURN"))
    private <S extends LivingEntityRenderState> int applyOpacityIfHasProgressDataKey(int original, @Local(argsOnly = true, name = "state") S state) {
        Double defaultOpacity = state.getData(CausticsClient.OPACITY_DEFAULT_KEY);
        if(defaultOpacity == null) return original;

        Double ghostProg = state.getData(CausticsClient.GHOST_PROGRESS_KEY);
        Double playerProg = state.getData(CausticsClient.PLAYER_PROGRESS_KEY);
        double progress;
        if(ghostProg != null)
            progress = ghostProg;
        else progress = Objects.requireNonNullElse(playerProg, defaultOpacity);

        int alpha = (int) (progress * 255);
        return (original & 0x00_FF_FF_FF) | (alpha << 24);
    }



}
