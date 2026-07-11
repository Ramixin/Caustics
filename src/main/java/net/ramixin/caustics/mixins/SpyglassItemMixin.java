package net.ramixin.caustics.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpyglassItem;
import net.minecraft.world.level.Level;
import net.ramixin.caustics.items.AlidadeItem;
import net.ramixin.caustics.items.components.SpyglassLens;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(SpyglassItem.class)
public abstract class SpyglassItemMixin extends Item {

    public SpyglassItemMixin(Properties properties) {
        super(properties);
    }

    @WrapMethod(method = "use")
    private InteractionResult behaveAsAlidadeOnUseIfHasCorrectLens(Level level, Player player, InteractionHand hand, Operation<InteractionResult> original) {
        ItemStack stack = player.getItemInHand(hand);
        if(!SpyglassLens.isAlidade(stack)) return original.call(level, player, hand);
        Optional<InteractionResult> result = AlidadeItem.use(level, player, hand);
        //noinspection OptionalIsPresent
        if(result.isPresent()) return result.get();
        return original.call(level, player, hand);
    }

    @WrapMethod(method = "releaseUsing")
    private boolean behaveAsAlidadeOnReleaseIfHasCorrectLens(ItemStack itemStack, Level level, LivingEntity entity, int remainingTime, Operation<Boolean> original) {
        boolean originalValue = original.call(itemStack, level, entity, remainingTime);
        if(!SpyglassLens.isAlidade(itemStack)) return originalValue;
        return AlidadeItem.releaseUsing(level, entity, originalValue);
    }

}
