package net.ramixin.caustics.client.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.client.CausticsClient;
import net.ramixin.caustics.client.LookManager;
import net.ramixin.caustics.client.TooltipRenderer;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.nodes.ClientNode;
import net.ramixin.caustics.items.components.SpyglassLens;
import net.ramixin.caustics.nodes.routing.Route;
import net.ramixin.caustics.utils.LookUtil;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

import static net.ramixin.caustics.client.rendering.RenderUtil.*;

public class AlidadeHudRenderer {
    
    private static final AlidadeHudRenderer INSTANCE = new AlidadeHudRenderer();

    private HudRenderState LOOKING_RENDER_STATE = null;
    private HudRenderState SELECTED_RENDER_STATE = null;

    private AlidadeHudRenderer() {

    }

    public void onInitialize() {
        LevelRenderEvents.END_EXTRACTION.register(this::extract);
        HudElementRegistry.addLast(Caustics.id("alidade_node_info"), this::renderHud);
    }

    private void extract(LevelExtractionContext ctx) {
        LOOKING_RENDER_STATE = null;
        SELECTED_RENDER_STATE = null;

        Player player = Minecraft.getInstance().player;
        if(player == null) return;
        if(!SpyglassLens.isAlidade(player.getUseItem())) return;
        if(!player.isUsingItem()) return;

        ClientLevel level = ctx.level();
        if(!level.isRaining()) {
            LookManager lookManager = CausticsClient.LOOK_MANAGER;
            double[] angles = lookManager.getAngles();
            extractHudLooking(LookUtil.calculateClosestLooking(angles));
        }
        extractHudSelected(ClientCrystalNetwork.getInstance().getSelectedNode());
    }

    private void extractHudLooking(Optional<Integer> maybeClosest) {
        if(maybeClosest.isEmpty()) return;
        int closestIndex = maybeClosest.get();

        LookManager lookManager = CausticsClient.LOOK_MANAGER;
        BlockPos closestPos = lookManager.getPositions()[closestIndex];
        Route route = lookManager.getRoutes()[closestIndex];
        ClientCrystalNetwork.getInstance().setLastLookingAt(closestPos);

        LOOKING_RENDER_STATE = extractHud(closestPos, ClientCrystalNetwork.getInstance().getScrollPos(), route);
    }

    private void extractHudSelected(Optional<BlockPos> selectedNode) {
        if(selectedNode.isEmpty()) return;
        BlockPos pos = selectedNode.get();
        int scrollPos = ClientCrystalNetwork.getInstance().getSelectedScrollPos();
        int i = -1;
        BlockPos[] positions = CausticsClient.LOOK_MANAGER.getPositions();
        for(int j = 0; j < positions.length; j++) {
            if(!positions[j].equals(pos)) continue;
            i = j;
            break;
        }
        if(i == -1) return;
        SELECTED_RENDER_STATE = extractHud(pos, scrollPos, CausticsClient.LOOK_MANAGER.getRoutes()[i]);
    }

    private HudRenderState extractHud(BlockPos pos, int scrollPos, Route route) {
        Optional<ClientNode> maybeClosestNode = ClientCrystalNetwork.getInstance().getSapphireNodeAt(pos);
        if(maybeClosestNode.isEmpty()) return null;
        ClientNode closestNode = maybeClosestNode.get();
        Component nodeName = extractNodeName(pos);
        Optional<Component> depositName = closestNode.peridotPositions().isEmpty() ? Optional.empty() : Optional.of(extractDepositName(closestNode, scrollPos));
        List<Component> routeStrings = extractRoute(route);
        return new HudRenderState(nodeName, depositName, routeStrings);
    }

    private void renderHud(@NonNull GuiGraphicsExtractor evilGraphics, @NonNull DeltaTracker deltaTracker) {
        if(LOOKING_RENDER_STATE == null && SELECTED_RENDER_STATE == null) return;
        int mouseX = (int) Minecraft.getInstance().mouseHandler.xpos();
        int mouseY = (int) Minecraft.getInstance().mouseHandler.ypos();
        TooltipRenderer renderer = new TooltipRenderer(evilGraphics, Minecraft.getInstance().font);

        if(SELECTED_RENDER_STATE != null) {
            renderer.rightAlign(351);
            renderer.render(List.of(Component.translatable("caustics.node.selected")), 22);
            renderHudInternal(SELECTED_RENDER_STATE, renderer);
        }

        renderer.resetHeight();

        if(LOOKING_RENDER_STATE != null) {
            renderer.leftAlign(105);
            renderer.render(List.of(Component.translatable("caustics.node.name")), 22);
            renderHudInternal(LOOKING_RENDER_STATE, renderer);
        }

        evilGraphics.extractDeferredElements(mouseX, mouseY, 0);
    }

    private static void renderHudInternal(HudRenderState state, TooltipRenderer renderer) {
        renderer.render(List.of(state.nodeName()), 1);

        if(state.maybeDepositName().isPresent()) {
            renderer.render(List.of(Component.translatable("caustics.node.deposit")), 10);
            renderer.render(List.of(state.maybeDepositName().get()), 1);

        }

        if(state.route().isEmpty())
            renderer.render(List.of(Component.translatable("caustics.node.route_direct")), 10);
        else {
            renderer.render(List.of(Component.translatable("caustics.node.route_start")), 10);
            renderer.render(state.route(), 1);
        }
    }
    
    public static AlidadeHudRenderer getInstance() {
        return INSTANCE;
    }

    private record HudRenderState(Component nodeName, Optional<Component> maybeDepositName, List<Component> route) { }
    
}
