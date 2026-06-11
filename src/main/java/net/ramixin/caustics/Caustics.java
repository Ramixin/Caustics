package net.ramixin.caustics;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.ramixin.caustics.blocks.ModBlocks;
import net.ramixin.caustics.items.ModItems;
import net.ramixin.caustics.items.components.ModDataComponents;
import net.ramixin.caustics.menus.ModMenus;
import net.ramixin.caustics.networking.clientbound.NetworkSyncPayload;
import net.ramixin.caustics.networking.serverbound.RequestSyncPayload;
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
        ModDataComponents.onInitialize();
        ModBlocks.onInitialize();
        ModFeatures.onInitialize();
        ModMixson.onInitialize();
        ModGameRules.onInitialize();
        ModMenus.onInitialize();

        if(FabricLoader.getInstance().isDevelopmentEnvironment())
            CommandRegistrationCallback.EVENT.register(ModCommands::onInitialize);

        ServerTickEvents.END_LEVEL_TICK.register(level -> CrystalNetwork.get(level).tick(level));

        PayloadTypeRegistry.clientboundPlay().register(NetworkSyncPayload.TYPE, NetworkSyncPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RequestSyncPayload.TYPE, RequestSyncPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestSyncPayload.TYPE, (_, ctx) -> {
            CrystalNetwork network = CrystalNetwork.get(ctx.player().level());
            network.stopSyncing(ctx.player().getUUID());
        });
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

}
