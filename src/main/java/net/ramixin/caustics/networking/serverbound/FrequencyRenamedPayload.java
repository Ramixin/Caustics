package net.ramixin.caustics.networking.serverbound;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.ramixin.caustics.Caustics;
import org.jspecify.annotations.NonNull;

public record FrequencyRenamedPayload(BlockPos pos, String newName) implements CustomPacketPayload {

    public static final Type<FrequencyRenamedPayload> TYPE = new Type<>(Caustics.id("frequency_renamed"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FrequencyRenamedPayload> CODEC = StreamCodec.ofMember(FrequencyRenamedPayload::write, FrequencyRenamedPayload::new);

    private FrequencyRenamedPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readUtf());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(newName);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
