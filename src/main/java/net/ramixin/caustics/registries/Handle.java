package net.ramixin.caustics.registries;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.item.Item;

public record Handle(Item item, TimingType cooldownType, TimingType chargeUpType) {

    public static final Codec<Holder<Handle>> HOLDER_CODEC = RegistryFixedCodec.create(ModRegistries.HANDLE_KEY);

    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Handle>> HOLDER_STREAM_CODEC = ByteBufCodecs.holderRegistry(ModRegistries.HANDLE_KEY);

}
