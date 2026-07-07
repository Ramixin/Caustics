package net.ramixin.caustics.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.ramixin.caustics.ducks.ItemTooltipContextDuck;
import net.ramixin.caustics.nodes.Network;
import org.jspecify.annotations.NonNull;

import java.util.UUID;
import java.util.function.Consumer;

public record Frequency(UUID uuid) implements TooltipProvider {

    public static final Codec<Frequency> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(Frequency::uuid)
    ).apply(instance, Frequency::new));

    public static final Codec<Frequency> STRINGABLE_CODEC = Codec.STRING.xmap(
            s -> new Frequency(UUID.fromString(s)),
            frequency -> frequency.uuid.toString()
    );

    public static final StreamCodec<FriendlyByteBuf, Frequency> STREAM_CODEC = StreamCodec.of(Frequency::write, Frequency::new);

    private Frequency(FriendlyByteBuf buf) {
        this(buf.readUUID());
    }

    public static Frequency fromName(String name) {
        return new Frequency(UUID.nameUUIDFromBytes(name.getBytes()));
    }

    public static Frequency unnamed() {
        return new Frequency(UUID.randomUUID());
    }

    private static void write(FriendlyByteBuf friendlyByteBuf, Frequency frequency) {
        friendlyByteBuf.writeUUID(frequency.uuid);
    }

    @Override
    public void addToTooltip(Item.@NonNull TooltipContext context, @NonNull Consumer<Component> consumer, @NonNull TooltipFlag flag, @NonNull DataComponentGetter components) {
        consumer.accept(Component.translatable("caustics.frequency.tooltip_header").withStyle(ChatFormatting.GRAY));
        Network network = ((ItemTooltipContextDuck)context).caustics$getNetwork();
        MutableComponent name = network.frequencyRegistry().getFrequencyName(this).map(Component::literal).orElseGet(() -> Component.translatable("caustics.frequency.unnamed"));
        consumer.accept(name.withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Frequency(UUID uuid1))) return false;
        return uuid1.equals(uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
