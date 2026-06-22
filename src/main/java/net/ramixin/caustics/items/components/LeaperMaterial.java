package net.ramixin.caustics.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.ramixin.caustics.registries.Handle;

public record LeaperMaterial(Holder<Handle> handle, Holder<Item> decoration, boolean hasCore) {

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

}
