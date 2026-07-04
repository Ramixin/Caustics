package net.ramixin.caustics;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.ramixin.caustics.blocks.ModBlocks;
import net.ramixin.caustics.entities.ModEntities;
import net.ramixin.caustics.items.ModItems;
import net.ramixin.caustics.items.components.ModDataComponents;
import net.ramixin.caustics.menus.ModMenus;
import net.ramixin.caustics.networking.clientbound.FrequencySyncPayload;
import net.ramixin.caustics.networking.clientbound.NodeSyncPayload;
import net.ramixin.caustics.networking.clientbound.RoutingSyncPayload;
import net.ramixin.caustics.networking.clientbound.SignalRangeSyncPayload;
import net.ramixin.caustics.networking.serverbound.RequestLeaptionPayload;
import net.ramixin.caustics.networking.serverbound.RequestSyncPayload;
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

        if(FabricLoader.getInstance().isDevelopmentEnvironment())
            CommandRegistrationCallback.EVENT.register(ModCommands::onInitialize);

        ServerTickEvents.END_LEVEL_TICK.register(level -> CrystalNetwork.get(level).tick(level));

        ServerPlayerEvents.JOIN.register(player -> CrystalNetwork.get(player.level()).joinSync(player));

        PayloadTypeRegistry.clientboundPlay().register(NodeSyncPayload.TYPE, NodeSyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SignalRangeSyncPayload.TYPE, SignalRangeSyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FrequencySyncPayload.TYPE, FrequencySyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RoutingSyncPayload.TYPE, RoutingSyncPayload.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(RequestSyncPayload.TYPE, RequestSyncPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RequestLeaptionPayload.TYPE, RequestLeaptionPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestSyncPayload.TYPE, (_, ctx) ->
                CrystalNetwork.get(ctx.player().level()).resync(ctx.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(RequestLeaptionPayload.TYPE, (payload, ctx) ->
                CrystalNetwork.get(ctx.player().level()).requestLeaption(ctx.player(), payload.route(), payload.peridotPos())
        );
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

}
