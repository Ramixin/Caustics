package net.ramixin.caustics.client.mixins;

import net.minecraft.world.item.Item;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.ducks.ItemTooltipContextDuck;
import net.ramixin.caustics.nodes.Network;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net/minecraft/world/item/Item$TooltipContext$2")
public abstract class ItemTooltipContext$2Mixin implements Item.TooltipContext, ItemTooltipContextDuck {

    @Override
    public Network caustics$getNetwork() {
        return ClientCrystalNetwork.getInstance();
    }
}
