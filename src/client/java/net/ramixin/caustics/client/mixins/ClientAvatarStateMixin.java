package net.ramixin.caustics.client.mixins;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.entity.ClientAvatarState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@SuppressWarnings("NonExtendableApiUsage")
@Mixin(ClientAvatarState.class)
public class ClientAvatarStateMixin implements FabricRenderState {

    @Unique
    @Nullable
    private Map<RenderStateDataKey<?>, Object> renderStateData;

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getData(@NonNull RenderStateDataKey<T> key) {
        return renderStateData == null ? null : (T) renderStateData.get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @NonNull T getDataOrDefault(@NonNull RenderStateDataKey<T> key, @NonNull T defaultValue) {
        return renderStateData == null ? defaultValue : (T) renderStateData.getOrDefault(key, defaultValue);
    }

    @Override
    public <T> void setData(@NonNull RenderStateDataKey<T> key, T value) {
        if (renderStateData == null) {
            renderStateData = new Reference2ObjectOpenHashMap<>();
        }

        renderStateData.put(key, value);
    }

    @Override
    public void clearExtraData() {
        if (renderStateData != null) {
            renderStateData.clear();
        }
    }

}
