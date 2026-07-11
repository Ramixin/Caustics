package net.ramixin.caustics.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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
import net.ramixin.caustics.client.rendering.AlidadeHudRenderer;
import net.ramixin.caustics.client.rendering.LeapParticleRenderPipeline;
import net.ramixin.caustics.client.rendering.NodesRenderPipeline;
import net.ramixin.caustics.client.rendering.node.NodeIcon;
import net.ramixin.caustics.items.components.SpyglassLens;
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
        LeapParticleRenderPipeline.getInstance().onInitialize();
        AlidadeHudRenderer.getInstance().onInitialize();
        ModClientNetworking.onInitialize();
        ModMixsonClient.onInitialize();

        ClientHotbarScrollEvents.ALLOW.register((inventory, _, _, _, dy) -> {
            if(!inventory.player.isUsingItem()) return true;
            if(!SpyglassLens.isAlidade(inventory.player.getUseItem())) return true;
            Optional<BlockPos> lookingAt = LookUtil.getLookingAt(inventory.player, LOOK_MANAGER.getPositions());
            if(lookingAt.isEmpty()) {
                ClientCrystalNetwork.getInstance().clearScrollPos();
                return true;
            }
            Optional<NodeIcon> maybeIcon = ClientCrystalNetwork.getInstance().iconHolder().get(lookingAt.get());
            ClientCrystalNetwork.getInstance().deltaScrollPos(-dy, maybeIcon);
            return false;
        });

        LevelRenderEvents.END_MAIN.register(_ -> LOOK_MANAGER.wipe());
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> ClientCrystalNetwork.getInstance().nuke());
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            if(minecraft.level == null) return;
            if(minecraft.level.tickRateManager().isFrozen()) return;
            ClientCrystalNetwork.getInstance().tick();
        });
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
        Optional<NodeIcon> nodeIcon = ClientCrystalNetwork.getInstance().iconHolder().get(sapphirePos);
        nodeIcon.ifPresent(NodeIcon::bump);
        BlockPos peridotPos = peridotPositions.get(scrollPos);
        ClientCrystalNetwork.getInstance().selectNode(sapphirePos);
        ClientPlayNetworking.send(new SelectionSyncPayload(sapphirePos, peridotPos));

    }
}
