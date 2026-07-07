package net.ramixin.caustics.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseCooldown;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.items.components.LeaperMaterial;
import net.ramixin.caustics.items.components.ModDataComponents;
import net.ramixin.caustics.registries.Handle;

import java.util.Optional;
import java.util.UUID;

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

    static void updateCooldownComponent(ItemStack stack) {
        UseCooldown cooldown = stack.get(DataComponents.USE_COOLDOWN);
        if(cooldown != null) return;
        int ticks = getCooldownTicks(stack);
        String group = UUID.randomUUID().toString().toLowerCase();
        stack.set(DataComponents.USE_COOLDOWN, new UseCooldown(ticks, Optional.of(Caustics.id(group))));
    }

}
