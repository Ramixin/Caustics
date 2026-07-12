package net.ramixin.caustics.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.ramixin.caustics.items.components.SpyglassLens;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder {

    @ModifyReturnValue(method = "getItemName", at = @At("RETURN"))
    private Component prefixSpyglassNameIfAlidade(Component original) {
        if(original == CommonComponents.EMPTY) return original;
        if(this.get(DataComponents.CUSTOM_NAME) != null) return original;
        ItemStack stack = (ItemStack) (Object) this;
        MutableComponent prefix;
        if(SpyglassLens.isAlidade(stack)) prefix = Component.translatable("caustics.spyglass_lens.alidade");
        else if(SpyglassLens.isDowser(stack)) prefix = Component.translatable("caustics.spyglass_lens.dowser");
        else if(SpyglassLens.isTelescope(stack)) prefix = Component.translatable("caustics.spyglass_lens.telescope");
        else return original;
        return prefix.append(" ").append(original);
    }

}
