package net.ramixin;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.world.level.gamerules.GameRule;
import net.ramixin.caustics.Caustics;

public class ModGameRules {

    public static final GameRule<Integer> STEPS_PER_TICK = GameRuleBuilder.forInteger(64).minValue(1).buildAndRegister(Caustics.id("steps_per_tick"));

    public static void onInitialize() {

    }

}
