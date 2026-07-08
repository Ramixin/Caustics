package net.ramixin.caustics.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientHotbarScrollEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.nodes.ClientNode;
import net.ramixin.caustics.client.nodes.NodesRenderPipeline;
import net.ramixin.caustics.networking.bidirectional.SelectionSyncPayload;
import net.ramixin.caustics.utils.LookUtil;

import java.util.List;
import java.util.Optional;

public class CausticsClient implements ClientModInitializer {

    public static final Identifier ALIDADE_GUI_TEXTURE = Caustics.id("textures/misc/alidade_scope.png");

    public static final LookManager LOOK_MANAGER = new LookManager();

    public static int MAX_SIGNAL_RANGE = 256;

    public static final RenderStateDataKey<Double> OPACITY_KEY = RenderStateDataKey.create(() -> "caustics:opacity");
    public static final RenderStateDataKey<Double> OPACITY_DEFAULT_KEY = RenderStateDataKey.create(() -> "caustics:opacity_default");

    @Override
    public void onInitializeClient() {
        NodesRenderPipeline.getInstance().onInitialize();
        ModClientNetworking.onInitialize();

        ClientHotbarScrollEvents.ALLOW.register((inventory, _, _, _, dy) -> {
            Optional<BlockPos> lookingAt = LookUtil.getLookingAt(inventory.player, LOOK_MANAGER.getPositions());
            if(lookingAt.isEmpty()) {
                ClientCrystalNetwork.getInstance().clearScrollPos();
                return true;
            }
            ClientCrystalNetwork.getInstance().deltaScrollPos(-dy);
            return false;
        });

        LevelRenderEvents.END_MAIN.register(_ -> LOOK_MANAGER.wipe());
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> ClientCrystalNetwork.getInstance().nuke());

        ModMixsonClient.onInitialize();
    }

    public static void onAlidadeAttack() {
        Optional<Integer> closest = LookUtil.calculateClosestLooking(LOOK_MANAGER.getAngles());
        if(closest.isEmpty()) return;
        BlockPos sapphirePos = LOOK_MANAGER.getPositions()[closest.get()];
        Optional<ClientNode> maybeNode = ClientCrystalNetwork.getInstance().getSapphireNodeAt(sapphirePos);
        if(maybeNode.isEmpty()) return;
        List<BlockPos> peridotPositions = maybeNode.get().peridotPositions();
        int scrollPos = ClientCrystalNetwork.getInstance().getSelectedScrollPos();
        if(scrollPos >= peridotPositions.size() || scrollPos < 0) return;
        BlockPos peridotPos = peridotPositions.get(scrollPos);
        ClientCrystalNetwork.getInstance().selectNode(sapphirePos);
        ClientPlayNetworking.send(new SelectionSyncPayload(sapphirePos, peridotPos));

    }
}
