package net.ramixin.caustics.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.ramixin.caustics.ducks.ItemTooltipContextDuck;
import net.ramixin.caustics.nodes.Network;
import net.ramixin.caustics.nodes.core.CrystalNetwork;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/item/Item$TooltipContext$2")
public abstract class ItemTooltipContext$2Mixin implements Item.TooltipContext, ItemTooltipContextDuck {

    @Shadow
    private Level val$level;

    @Override
    public Network caustics$getNetwork() {
        if(!(val$level instanceof ServerLevel serverLevel)) throw new UnsupportedOperationException("level is not a server level");
        return CrystalNetwork.get(serverLevel);
    }
}
