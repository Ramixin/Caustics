package net.ramixin.caustics.items.components.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.ramixin.caustics.items.components.ModDataComponents;
import net.ramixin.caustics.items.components.SpyglassLens;
import org.jspecify.annotations.NonNull;

public record SpyglassLensPredicate(TagKey<Item> tag) implements DataComponentPredicate {

    public static final Codec<SpyglassLensPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(SpyglassLensPredicate::tag)
    ).apply(instance, SpyglassLensPredicate::new));

    @Override
    public boolean matches(@NonNull DataComponentGetter components) {
        SpyglassLens lens = components.get(ModDataComponents.SPYGLASS_LENS);
        if(lens == null) return false;
        return lens.item().is(tag);
    }
}
