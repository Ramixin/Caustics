package net.ramixin.caustics.client.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerModelType;
import net.ramixin.caustics.client.entities.ClientLeapGhost;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Shadow
    protected abstract <T extends Avatar & ClientAvatarEntity> AvatarRenderer<@NonNull T> getAvatarRenderer(Map<PlayerModelType, AvatarRenderer<@NonNull T>> renderers, T entity);

    @Unique
    private Map<PlayerModelType, AvatarRenderer<ClientLeapGhost>> ghostRenderers = Map.of();

    @SuppressWarnings("unchecked")
    @WrapMethod(method = "getRenderer(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;")
    private <T extends Entity> EntityRenderer<? super T, ?> addGhostRenderersToRendererGet(T entity, Operation<EntityRenderer<? super T, ?>> original) {
        if(!(entity instanceof ClientLeapGhost clientLeapGhost)) return original.call(entity);
        return (EntityRenderer<? super T, ?>) this.getAvatarRenderer(ghostRenderers, clientLeapGhost);
    }

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void reloadGhostRenderersOnReload(ResourceManager resourceManager, CallbackInfo ci, @Local(name = "context") EntityRendererProvider.Context context) {
        ghostRenderers = EntityRenderers.createAvatarRenderers(context);
    }

}
