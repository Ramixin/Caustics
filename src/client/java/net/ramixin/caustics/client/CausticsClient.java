package net.ramixin.caustics.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientHotbarScrollEvents;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.nodes.NodesRenderPipeline;
import net.ramixin.caustics.networking.clientbound.FrequencySyncPayload;
import net.ramixin.caustics.networking.clientbound.NodeSyncPayload;
import net.ramixin.caustics.networking.clientbound.SignalRangeSyncPayload;
import net.ramixin.caustics.utils.LookUtil;

import java.util.Optional;

public class CausticsClient implements ClientModInitializer {

    public static final Identifier ALIDADE_GUI_TEXTURE = Caustics.id("textures/misc/alidade_scope.png");

    public static final LookManager LOOK_MANAGER = new LookManager();

    public static int MAX_SIGNAL_RANGE = 256;

    @Override
    public void onInitializeClient() {
        NodesRenderPipeline.getInstance().onInitialize();

        ClientPlayNetworking.registerGlobalReceiver(NodeSyncPayload.TYPE, (payload, _) -> ClientCrystalNetwork.getInstance().onNodeSync(payload.nodeData()));
        ClientPlayNetworking.registerGlobalReceiver(SignalRangeSyncPayload.TYPE, (payload, _) -> {
            int val = payload.newValue();
            Caustics.LOGGER.info("Signal range changed to {} on client", val);
            MAX_SIGNAL_RANGE = val * val;
        });
        ClientPlayNetworking.registerGlobalReceiver(FrequencySyncPayload.TYPE, (payload, _) -> ClientCrystalNetwork.getInstance().onFrequencySync(payload.frequencies(), payload.frequencyNames()));

        ClientHotbarScrollEvents.ALLOW.register((inventory, _, _, _, dy) -> {
            Optional<BlockPos> lookingAt = LookUtil.getLookingAt(inventory.player, ClientCrystalNetwork.getInstance().getTargetablePositions());
            if(lookingAt.isEmpty()) {
                ClientCrystalNetwork.getInstance().clearScrollPos();
                return true;
            }
            ClientCrystalNetwork.getInstance().deltaScrollPos(-dy);
            return false;
        });

        LevelRenderEvents.END_MAIN.register(_ -> LOOK_MANAGER.wipe());
    }

    public static void onAlidadeAttack(LocalPlayer player) {
//        BlockPos[] positions = ClientCrystalNetwork.getNodes().stream().flatMap(node -> node.positions().stream()).toArray(BlockPos[]::new);
//        Vec3[] vectors = ModUtils.calculateUnitVectors(player, positions);
//        double[] angles = ModUtils.calculateDisplacementAngles(player, vectors);
//        Optional<Integer> closest = ModUtils.closestLooking(angles);
//        if(closest.isEmpty()) return;
//        boolean isAmbiguous = ModUtils.isAmbiguous(vectors, closest.get());
    }
}
