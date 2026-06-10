package net.ramixin.caustics.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.Identifier;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.nodes.NodesRenderPipeline;
import net.ramixin.caustics.networking.clientbound.NetworkSyncPayload;

public class CausticsClient implements ClientModInitializer {

    public static final Identifier ALIDADE_GUI_TEXTURE = Caustics.id("textures/misc/alidade_scope.png");

    @Override
    public void onInitializeClient() {
        NodesRenderPipeline.getInstance().onInitialize();

        ClientPlayNetworking.registerGlobalReceiver(NetworkSyncPayload.TYPE, (payload, _) -> ClientCrystalNetwork.onSync(payload.nodeData()));
    }
}
