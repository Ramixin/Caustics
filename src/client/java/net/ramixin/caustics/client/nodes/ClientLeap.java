package net.ramixin.caustics.client.nodes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.ramixin.caustics.networking.clientbound.LeapStartPayload;

public record ClientLeap(long startTick, int maxTicks, BlockPos sapphirePos) {

    public ClientLeap(LeapStartPayload payload) {
        this(payload.startTick(), payload.maxTicks(), payload.sapphirePos());
    }

    public double progress() {
        ClientLevel level = Minecraft.getInstance().level;
        if(level == null) return 0;
        long ticks = level.getGameTime();
        long elapsed = ticks - startTick;
        return (double) elapsed / maxTicks;
    }

}
