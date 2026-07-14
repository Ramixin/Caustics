package net.ramixin.caustics.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.ramixin.caustics.items.components.ModDataComponents;
import net.ramixin.caustics.items.components.SpyglassLens;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder {

    @ModifyReturnValue(method = "getItemName", at = @At("RETURN"))
    private Component prefixSpyglassNameIfAlidade(Component original) {
        if(original == CommonComponents.EMPTY) return original;
        if(this.get(DataComponents.CUSTOM_NAME) != null) return original;
        SpyglassLens lens = this.get(ModDataComponents.SPYGLASS_LENS);
        if(lens == null) return original;
        Identifier key = BuiltInRegistries.ITEM.getKey(lens.item().value());
        MutableComponent prefix = Component.translatable(String.format("%s.spyglass_lens.%s", key.getNamespace(), key.getPath()));
        return prefix.append(" ").append(original);
    }

}
