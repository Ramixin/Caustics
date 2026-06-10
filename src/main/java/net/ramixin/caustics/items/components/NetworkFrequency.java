package net.ramixin.caustics.items.components;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.ramixin.caustics.CodecUtils;
import org.jspecify.annotations.NonNull;

import java.util.UUID;
import java.util.function.Consumer;

public record NetworkFrequency(UUID uuid) implements TooltipProvider {

    public static final Codec<NetworkFrequency> CODEC = UUIDUtil.CODEC.xmap(NetworkFrequency::new, NetworkFrequency::uuid);
    public static final Codec<NetworkFrequency> STRINGABLE_CODEC = CodecUtils.UUID_STRING_CODEC.xmap(NetworkFrequency::new, NetworkFrequency::uuid);

    public static final StreamCodec<FriendlyByteBuf, NetworkFrequency> STREAM_CODEC = StreamCodec.of(
            (buf, networkFrequency) -> buf.writeUUID(networkFrequency.uuid()),
            (buf) -> new NetworkFrequency(buf.readUUID())
    );

    @Override
    public void addToTooltip(Item.@NonNull TooltipContext context, @NonNull Consumer<Component> consumer, @NonNull TooltipFlag flag, @NonNull DataComponentGetter components) {
        consumer.accept(Component.translatable("caustics.network_frequency.tooltip").withStyle(ChatFormatting.GRAY));
        consumer.accept(Component.literal(uuid.toString().split("-")[0]).withStyle(ChatFormatting.GRAY));
    }
}
