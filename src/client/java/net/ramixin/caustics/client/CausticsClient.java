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
import net.ramixin.caustics.client.nodes.cache.AlidadeIconCache;
import net.ramixin.caustics.client.nodes.icons.AlidadeIcon;
import net.ramixin.caustics.client.rendering.AlidadeHudRenderer;
import net.ramixin.caustics.client.rendering.LeapParticleRenderPipeline;
import net.ramixin.caustics.client.rendering.NodeRenderPipeline;
import net.ramixin.caustics.items.components.SpyglassLens;
import net.ramixin.caustics.networking.bidirectional.SelectionSyncPayload;
import net.ramixin.caustics.utils.LookUtil;

import java.util.List;
import java.util.Optional;

public class CausticsClient implements ClientModInitializer {

    public static final Identifier ALIDADE_GUI_TEXTURE = Caustics.id("textures/misc/alidade_scope.png");
    public static final Identifier DOWSER_GUI_TEXTURE = Caustics.id("textures/misc/dowser_scope.png");
    public static final Identifier COLLIMATOR_GUI_TEXTURE = Caustics.id("textures/misc/collimator_scope.png");

    public static int MAX_SIGNAL_RANGE = 256 * 256;

    public static final RenderStateDataKey<Double> OPACITY_KEY = RenderStateDataKey.create(() -> "caustics:opacity");
    public static final RenderStateDataKey<Double> OPACITY_DEFAULT_KEY = RenderStateDataKey.create(() -> "caustics:opacity_default");

    @Override
    public void onInitializeClient() {
        NodeRenderPipeline.getInstance().onInitialize();
        LeapParticleRenderPipeline.getInstance().onInitialize();
        AlidadeHudRenderer.getInstance().onInitialize();
        ModClientNetworking.onInitialize();
        ModMixsonClient.onInitialize();

        ClientHotbarScrollEvents.ALLOW.register((inventory, _, _, _, dy) -> {
            if(!inventory.player.isUsingItem()) return true;
            if(!SpyglassLens.isAlidade(inventory.player.getUseItem())) return true;
            AlidadeIconCache alidadeCache = ClientCrystalNetwork.getInstance().caches().alidade();
            BlockPos[] positions = alidadeCache.getPositions();
            Optional<BlockPos> lookingAt = LookUtil.getLookingAt(inventory.player, positions);
            if(lookingAt.isEmpty()) {
                ClientCrystalNetwork.getInstance().clearScrollPos();
                return true;
            }
            AlidadeIcon icon = alidadeCache.get(lookingAt.get());
            ClientCrystalNetwork.getInstance().deltaScrollPos(-dy, icon);
            return false;
        });

        LevelRenderEvents.END_MAIN.register(_ -> ClientCrystalNetwork.getInstance().caches().wipeAll());
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> ClientCrystalNetwork.getInstance().nuke());
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            if(minecraft.level == null) return;
            if(minecraft.level.tickRateManager().isFrozen()) return;
            ClientCrystalNetwork.getInstance().tick();
        });
    }

    public static void onAlidadeAttack() {
        AlidadeIconCache alidadeCache = ClientCrystalNetwork.getInstance().caches().alidade();
        Optional<Integer> closest = LookUtil.calculateClosestLooking(alidadeCache.getAngles());
        if(closest.isEmpty()) return;
        BlockPos sapphirePos = alidadeCache.getPositions()[closest.get()];
        Optional<ClientNode> maybeNode = ClientCrystalNetwork.getInstance().getSapphireNodeAt(sapphirePos);
        if(maybeNode.isEmpty()) return;
        List<BlockPos> peridotPositions = maybeNode.get().peridot();
        int scrollPos = ClientCrystalNetwork.getInstance().getSelectedScrollPos();
        if(scrollPos >= peridotPositions.size() || scrollPos < 0) return;
        AlidadeIcon icon = ClientCrystalNetwork.getInstance().caches().alidade().get(sapphirePos);
        icon.bump();
        BlockPos peridotPos = peridotPositions.get(scrollPos);
        ClientCrystalNetwork.getInstance().selectNode(sapphirePos);
        ClientPlayNetworking.send(new SelectionSyncPayload(sapphirePos, peridotPos));

    }
}
