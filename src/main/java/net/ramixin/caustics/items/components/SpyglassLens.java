package net.ramixin.caustics.items.components;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.ramixin.caustics.ModTags;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public record SpyglassLens(Holder<Item> item) implements TooltipProvider {

    public static final Codec<SpyglassLens> CODEC = Item.CODEC.xmap(SpyglassLens::new, SpyglassLens::item);
    public static final StreamCodec<RegistryFriendlyByteBuf, SpyglassLens> STREAM_CODEC = Item.STREAM_CODEC.map(SpyglassLens::new, SpyglassLens::item);

    public static boolean isCollimator(ItemStack stack) {
        return is(stack, ModTags.Items.COLLIMATOR_LENS);
    }

    public static boolean isTelescope(ItemStack stack) {
        return is(stack, ModTags.Items.TELESCOPE_LENS);
    }

    public static boolean isAlidade(ItemStack stack) {
        return is(stack, ModTags.Items.ALIDADE_LENS);
    }

    public static boolean isDowser(ItemStack stack) {
        return is(stack, ModTags.Items.DOWSER_LENS);
    }

    public static boolean is(ItemStack stack, TagKey<Item> lens_tag) {
        SpyglassLens lens = stack.get(ModDataComponents.SPYGLASS_LENS);
        if(lens == null) return false;
        return lens.item.is(lens_tag);
    }

    @Override
    public void addToTooltip(Item.@NonNull TooltipContext context, @NonNull Consumer<Component> consumer, @NonNull TooltipFlag flag, @NonNull DataComponentGetter components) {
        consumer.accept(Component.translatable("caustics.spyglass_lens.tooltip", Component.translatable(item.value().getDescriptionId())).withStyle(ChatFormatting.GRAY));
    }
}
