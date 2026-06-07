package net.ramixin.caustics;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.ramixin.ModGameRules;
import net.ramixin.caustics.blocks.ModBlocks;
import net.ramixin.caustics.features.ModFeatures;
import net.ramixin.caustics.nodes.CrystalNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Caustics implements ModInitializer {

    public static final String MOD_ID = "caustics";
    public static final String MOD_NAME = "Caustics";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing (1/1)");
        ModItems.onInitialize();
        ModBlocks.onInitialize();
        ModFeatures.onInitialize();
        ModMixson.onInitialize();
        ModGameRules.onInitialize();

        CommandRegistrationCallback.EVENT.register(ModCommands::onInitialize);

        ServerTickEvents.END_LEVEL_TICK.register(level -> CrystalNetwork.get(level).tick(level));
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

}
