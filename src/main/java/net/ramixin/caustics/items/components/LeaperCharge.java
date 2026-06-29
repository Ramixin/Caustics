package net.ramixin.caustics.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public record LeaperCharge(int charge, int maxCharge) implements TooltipProvider {

    public static final Codec<LeaperCharge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("charge").forGetter(LeaperCharge::charge),
            Codec.INT.fieldOf("max_charge").forGetter(LeaperCharge::maxCharge)
    ).apply(instance, LeaperCharge::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LeaperCharge> STREAM_CODEC = StreamCodec.ofMember(LeaperCharge::write, LeaperCharge::new);

    private LeaperCharge(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(charge);
        buf.writeInt(maxCharge);
    }

    public double percentage() {
        return (double) charge / maxCharge;
    }

    public LeaperCharge increment() {
        return new LeaperCharge(charge + 1, maxCharge);
    }

    public LeaperCharge decrement() {
        return new LeaperCharge(charge - 1, maxCharge);
    }

    @Override
    public void addToTooltip(Item.@NonNull TooltipContext context, @NonNull Consumer<Component> consumer, @NonNull TooltipFlag flag, @NonNull DataComponentGetter components) {
        if(!flag.isAdvanced()) return;
        consumer.accept(Component.translatable("caustics.leaper_charge.tooltip", charge, maxCharge));
    }
}
