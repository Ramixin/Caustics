package net.ramixin.caustics.mixins;

import net.minecraft.world.item.Item;
import net.ramixin.caustics.ducks.ItemTooltipContextDuck;
import net.ramixin.caustics.nodes.Network;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.TooltipContext.class)
public interface ItemTooltipContextMixin extends ItemTooltipContextDuck  {

    default Network caustics$getNetwork() {
        throw new UnsupportedOperationException("network was not set");
    }

}
