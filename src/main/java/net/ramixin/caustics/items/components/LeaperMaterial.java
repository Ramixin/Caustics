package net.ramixin.caustics.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.ramixin.caustics.registries.Handle;
import net.ramixin.caustics.registries.TimingType;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public record LeaperMaterial(Holder<Handle> handle, Holder<Item> decoration, boolean hasCore) implements TooltipProvider {

    public static final Codec<LeaperMaterial> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Handle.HOLDER_CODEC.fieldOf("handle").forGetter(LeaperMaterial::handle),
            Item.CODEC.fieldOf("decoration").forGetter(LeaperMaterial::decoration),
            Codec.BOOL.fieldOf("has_core").orElse(false).forGetter(LeaperMaterial::hasCore)
    ).apply(instance, LeaperMaterial::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LeaperMaterial> STREAM_CODEC = StreamCodec.ofMember(LeaperMaterial::write, LeaperMaterial::new);

    public LeaperMaterial(RegistryFriendlyByteBuf buf) {
        this(Handle.HOLDER_STREAM_CODEC.decode(buf), Item.STREAM_CODEC.decode(buf), buf.readBoolean());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        Handle.HOLDER_STREAM_CODEC.encode(buf, handle);
        Item.STREAM_CODEC.encode(buf, decoration);
        buf.writeBoolean(hasCore);
    }

    @Override
    public void addToTooltip(Item.@NonNull TooltipContext context, @NonNull Consumer<Component> consumer, @NonNull TooltipFlag flag, @NonNull DataComponentGetter components) {
        if(!flag.isAdvanced()) return;
        TimingType chargeUpType = this.handle.value().chargeUpType();
        TimingType cooldownType = this.handle.value().cooldownType();
        consumer.accept(Component.translatable("caustics.leaper_matieral.tooltip_charge_up", chargeUpType.getSerializedName()).withStyle(ChatFormatting.GRAY));
        consumer.accept(Component.translatable("caustics.leaper_matieral.tooltip_cooldown", cooldownType.getSerializedName()).withStyle(ChatFormatting.GRAY));

    }
}
