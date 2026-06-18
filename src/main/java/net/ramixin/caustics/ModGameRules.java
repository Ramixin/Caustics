package net.ramixin.caustics;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.ramixin.caustics.networking.clientbound.SignalRangeSyncPayload;

public class ModGameRules {

    public static final GameRule<Integer> STEPS_PER_TICK = register(64, 1, "steps_per_tick");
    public static final GameRule<Integer> MAX_STEPS = register(512, 1, "max_steps");
    public static final GameRule<Integer> SIGNAL_RANGE = register(256, 32, "signal_range");

    public static void onInitialize() {
        GameRuleEvents.changeCallback(SIGNAL_RANGE).register((value, server) -> {
            SignalRangeSyncPayload payload = new SignalRangeSyncPayload(value);
            for(ServerPlayer player : server.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, payload);
            }
        });
    }

    private static GameRule<Integer> register(int defaultValue, int minValue, String path) {
        return GameRuleBuilder.forInteger(defaultValue).minValue(minValue).category(GameRuleCategory.UPDATES).buildAndRegister(Caustics.id(path));
    }

}
