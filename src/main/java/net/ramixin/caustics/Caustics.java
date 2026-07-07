package net.ramixin.caustics;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.ramixin.caustics.blocks.ModBlocks;
import net.ramixin.caustics.entities.ModEntities;
import net.ramixin.caustics.items.ModItems;
import net.ramixin.caustics.items.components.ModDataComponents;
import net.ramixin.caustics.menus.ModMenus;
import net.ramixin.caustics.networking.ModNetworking;
import net.ramixin.caustics.nodes.core.CrystalNetwork;
import net.ramixin.caustics.registries.ModRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Caustics implements ModInitializer {

    public static final String MOD_ID = "caustics";
    public static final String MOD_NAME = "Caustics";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing (1/1)");
        ModRegistries.onInitialize();
        ModItems.onInitialize();
        ModDataComponents.onInitialize();
        ModBlocks.onInitialize();
        ModEntities.onInitialize();
        ModFeatures.onInitialize();
        ModMixson.onInitialize();
        ModGameRules.onInitialize();
        ModMenus.onInitialize();
        ModNetworking.onInitialize();

        if(FabricLoader.getInstance().isDevelopmentEnvironment())
            CommandRegistrationCallback.EVENT.register(ModCommands::onInitialize);

        ServerTickEvents.END_LEVEL_TICK.register(level -> CrystalNetwork.get(level).tick(level));

        ServerPlayerEvents.JOIN.register(player -> {
            CrystalNetwork network = CrystalNetwork.get(player.level());
            network.synchronizer().joinSync(player, network);
        });


    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

}
