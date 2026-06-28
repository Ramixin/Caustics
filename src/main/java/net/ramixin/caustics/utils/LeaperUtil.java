package net.ramixin.caustics.utils;

import net.minecraft.world.item.ItemStack;
import net.ramixin.caustics.items.components.LeaperMaterial;
import net.ramixin.caustics.items.components.ModDataComponents;
import net.ramixin.caustics.registries.Handle;

public interface LeaperUtil {

    static int getChargeUpTicks(ItemStack stack) {
        LeaperMaterial material = stack.get(ModDataComponents.LEAPER_MATERIAL);
        if(material == null) throw new IllegalStateException("Leaper material is null");
        Handle handle = material.handle().value();
        return switch(handle.chargeUpType()) {
            case QUICK -> 40;
            case NORMAL -> 80;
            case SLOW -> 120;
        };
    }

    static int getCooldownTicks(ItemStack stack) {
        LeaperMaterial material = stack.get(ModDataComponents.LEAPER_MATERIAL);
        if(material == null) throw new IllegalStateException("Leaper material is null");
        Handle handle = material.handle().value();
        return switch(handle.cooldownType()) {
            case QUICK -> 600;
            case NORMAL -> 1200;
            case SLOW -> 2400;
        };
    }

}
