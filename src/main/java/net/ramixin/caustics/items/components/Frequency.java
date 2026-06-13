package net.ramixin.caustics.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jspecify.annotations.NonNull;

import java.util.UUID;
import java.util.function.Consumer;

public record Frequency(String name, UUID uuid) implements TooltipProvider {

    public static final Codec<Frequency> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(Frequency::name),
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(Frequency::uuid)
    ).apply(instance, Frequency::new));

    public static final StreamCodec<FriendlyByteBuf, Frequency> STREAM_CODEC = StreamCodec.of(Frequency::write, Frequency::new);

    private Frequency(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readUtf(), friendlyByteBuf.readUUID());
    }

    public Frequency(String str) {
        this(str, UUID.nameUUIDFromBytes(str.getBytes()));
    }

    public static Frequency unnamed(String unnamedName) {
        return new Frequency(unnamedName, UUID.randomUUID());
    }

    private static void write(FriendlyByteBuf friendlyByteBuf, Frequency frequency) {
        friendlyByteBuf.writeUtf(frequency.name);
        friendlyByteBuf.writeUUID(frequency.uuid);
    }

    @Override
    public void addToTooltip(Item.@NonNull TooltipContext context, @NonNull Consumer<Component> consumer, @NonNull TooltipFlag flag, @NonNull DataComponentGetter components) {
        consumer.accept(Component.translatable("caustics.network_frequency.tooltip").withStyle(ChatFormatting.GRAY));
        consumer.accept(Component.literal(name).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Frequency freq)) return false;
        return freq.uuid.equals(uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
