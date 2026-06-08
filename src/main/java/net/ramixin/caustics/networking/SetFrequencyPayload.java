package net.ramixin.caustics.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.items.ModDataComponents;
import net.ramixin.caustics.items.ModItems;
import net.ramixin.caustics.items.components.Frequency;
import org.jspecify.annotations.NonNull;

public record SetFrequencyPayload(String network, String node) implements CustomPacketPayload {

    public static final Type<SetFrequencyPayload> PACKET_ID = new Type<>(Caustics.id("set_frequency"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetFrequencyPayload> PACKET_CODEC = StreamCodec.ofMember(SetFrequencyPayload::write, SetFrequencyPayload::new);

    public SetFrequencyPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readUtf());
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(network);
        buf.writeUtf(node);
    }

    public void serverHandle(ServerPlayNetworking.Context ctx) {
        Player player = ctx.player();
        ItemStack stack = player.getMainHandItem();
        if(!stack.is(ModItems.TUNING_FORK)) return;
        stack.set(ModDataComponents.FREQUENCY, new Frequency(network, node));
    }
}
