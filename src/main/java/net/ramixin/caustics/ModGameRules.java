package net.ramixin.caustics;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.world.level.gamerules.GameRule;

public class ModGameRules {

    public static final GameRule<Integer> STEPS_PER_TICK = GameRuleBuilder.forInteger(64).minValue(1).buildAndRegister(Caustics.id("steps_per_tick"));
    public static final GameRule<Integer> MAX_STEPS = GameRuleBuilder.forInteger(512).minValue(1).buildAndRegister(Caustics.id("max_steps"));

    public static void onInitialize() {

    }

}
