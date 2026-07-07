package net.ramixin.caustics.networking.clientbound;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.ramixin.caustics.Caustics;
import org.jspecify.annotations.NonNull;

import java.util.UUID;
import java.util.function.Function;

public sealed interface LeapStatusPayload extends CustomPacketPayload {

    Type<LeapStatusPayload> TYPE = new Type<>(Caustics.id("leap_result"));
    StreamCodec<RegistryFriendlyByteBuf, LeapStatusPayload> CODEC = StreamCodec.ofMember(
            LeapStatusPayload::write,
            LeapStatusPayload::read
    );

    static void sendFailure(ServerPlayer player, String reason) {
        ServerPlayNetworking.send(player, new Failure(reason));
    }

    default void write(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(status());
    }

    static LeapStatusPayload read(RegistryFriendlyByteBuf buf) {
        return buf.readEnum(Status.class).constructor.apply(buf);
    }

    Status status();

    default @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    record Success() implements LeapStatusPayload {
        private Success(RegistryFriendlyByteBuf buf) {
            this();
        }

        @Override
        public Status status() {
            return Status.SUCCESS;
        }
    }

    record Interrupt() implements LeapStatusPayload {
        private Interrupt(RegistryFriendlyByteBuf buf) {
            this();
        }

        @Override
        public Status status() {
            return Status.INTERRUPT;
        }
    }

    record Started(UUID player, BlockPos destination) implements LeapStatusPayload {
        public Started(RegistryFriendlyByteBuf buf) {
            this(buf.readUUID(), buf.readBlockPos());
        }

        @Override
        public void write(RegistryFriendlyByteBuf buf) {
            LeapStatusPayload.super.write(buf);
            buf.writeUUID(player);
            buf.writeBlockPos(destination);
        }

        @Override
        public Status status() {
            return Status.STARTED;
        }
    }

    record Failure(String reason) implements LeapStatusPayload {
        public Failure(RegistryFriendlyByteBuf buf) {
            this(buf.readUtf());
        }

        @Override
        public void write(RegistryFriendlyByteBuf buf) {
            LeapStatusPayload.super.write(buf);
            buf.writeUtf(reason);
        }

        @Override
        public Status status() {
            return Status.FAILURE;
        }
    }

    enum Status {
        SUCCESS(Success::new),
        INTERRUPT(Interrupt::new),
        STARTED(Started::new),
        FAILURE(Failure::new)

        ;

        private final Function<RegistryFriendlyByteBuf, LeapStatusPayload> constructor;

        Status(Function<RegistryFriendlyByteBuf, LeapStatusPayload> constructor) {
            this.constructor = constructor;
        }
    }

}
