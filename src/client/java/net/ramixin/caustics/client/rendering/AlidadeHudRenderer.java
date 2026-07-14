package net.ramixin.caustics.client.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.client.cache.AlidadeIconCache;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.nodes.ClientNode;
import net.ramixin.caustics.items.components.SpyglassLens;
import net.ramixin.caustics.nodes.routing.Route;
import net.ramixin.caustics.utils.LookUtil;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.ramixin.caustics.client.rendering.RenderUtil.*;

public class AlidadeHudRenderer {
    
    private static final AlidadeHudRenderer INSTANCE = new AlidadeHudRenderer();
    private static final Component UNNAMED_PERIDOT = Component.translatable("caustics.frequency.unnamed.peridot");
    private static final String EMPTY_PREFIX = "  ";

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
        if(!player.isUsingItem()) return;
        if(!SpyglassLens.isAlidade(player.getUseItem())) return;

        ClientLevel level = ctx.level();
        if(!level.isRaining()) {
            AlidadeIconCache alidadeViewCache = ClientCrystalNetwork.getInstance().iconIndex().alidadeCache();
            double[] angles = alidadeViewCache.getAngles();
            extractHudLooking(LookUtil.calculateClosestLooking(angles));
        }
        extractHudSelected(ClientCrystalNetwork.getInstance().getSelectedNode());
    }

    private void extractHudLooking(Optional<Integer> maybeClosest) {
        if(maybeClosest.isEmpty()) return;
        int closestIndex = maybeClosest.get();

        AlidadeIconCache cache = ClientCrystalNetwork.getInstance().iconIndex().alidadeCache();
        BlockPos closestPos = cache.getPositions()[closestIndex];
        Route route = cache.getRoutes()[closestIndex];
        ClientCrystalNetwork.getInstance().setLastLookingAt(closestPos);

        LOOKING_RENDER_STATE = extractHud(closestPos, ClientCrystalNetwork.getInstance().getScrollPos(), route, false);
    }

    private void extractHudSelected(Optional<BlockPos> selectedNode) {
        if(selectedNode.isEmpty()) return;
        BlockPos pos = selectedNode.get();
        int scrollPos = ClientCrystalNetwork.getInstance().getSelectedScrollPos();
        int i = -1;
        AlidadeIconCache cache = ClientCrystalNetwork.getInstance().iconIndex().alidadeCache();
        BlockPos[] positions = cache.getPositions();
        for(int j = 0; j < positions.length; j++) {
            if(!positions[j].equals(pos)) continue;
            i = j;
            break;
        }
        if(i == -1) return;
        SELECTED_RENDER_STATE = extractHud(pos, scrollPos, cache.getRoutes()[i], true);
    }

    private HudRenderState extractHud(BlockPos pos, int scrollPos, Route route, boolean single) {
        Optional<ClientNode> maybeClosestNode = ClientCrystalNetwork.getInstance().getSapphireNodeAt(pos);
        if(maybeClosestNode.isEmpty()) return null;
        ClientNode closestNode = maybeClosestNode.get();
        Component nodeName = extractNodeName(pos);
        List<Component> deposits = extractDepositsPositions(closestNode, scrollPos, single);
        List<Component> routeStrings = extractRoute(route);
        return new HudRenderState(nodeName, deposits, routeStrings);
    }

    private List<Component> extractDepositsPositions(ClientNode node, int scrollPos, boolean single) {
        List<BlockPos> peridotPositions = node.peridot();
        if(scrollPos >= peridotPositions.size() || scrollPos < 0) return List.of();
        if(single) return List.of(getFrequencyName(peridotPositions.get(scrollPos), UNNAMED_PERIDOT));

        if(peridotPositions.size() < 4) {
            List<Component> names = new ArrayList<>(peridotPositions.size());
            for(int i = 0; i < peridotPositions.size(); i++) {
                BlockPos pos = peridotPositions.get(i);
                MutableComponent component = getFrequencyName(pos, UNNAMED_PERIDOT);
                if(i == scrollPos) names.add(component);
                else names.add(component.withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
            }
            return names;
        }
        int start = Math.clamp(scrollPos - 1, 0, peridotPositions.size() - 3);
        int end = start + 3;
        List<BlockPos> positionsToMap = peridotPositions.subList(start, end);
        int offset = scrollPos - start;

        List<Component> names = new ArrayList<>(positionsToMap.size());
        for(int i = 0; i < positionsToMap.size(); i++) {
            MutableComponent component = getFrequencyName(positionsToMap.get(i), UNNAMED_PERIDOT);
            String prefix;
            if(i == 0 && start > 0) prefix = "↑ ";
            else if(i == positionsToMap.size() - 1 && end < peridotPositions.size()) prefix = "↓ ";
            else prefix = EMPTY_PREFIX;

            if(i == offset) names.add(Component.literal(prefix).append(component));
            else names.add(Component.literal(prefix).append(component.withStyle(ChatFormatting.ITALIC)).withStyle(ChatFormatting.GRAY));
        }
        return names;
    }

    private void renderHud(@NonNull GuiGraphicsExtractor evilGraphics, @NonNull DeltaTracker deltaTracker) {
        if(LOOKING_RENDER_STATE == null && SELECTED_RENDER_STATE == null) return;
        int mouseX = (int) Minecraft.getInstance().mouseHandler.xpos();
        int mouseY = (int) Minecraft.getInstance().mouseHandler.ypos();
        TooltipRenderer renderer = new TooltipRenderer(evilGraphics);

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
        renderer.render(List.of(state.nodeName), 1);
        List<Component> names = state.deposits;
        if(!names.isEmpty()) {
            renderer.render(List.of(Component.translatable("caustics.node.deposit")), 10);
            renderer.render(names, 1);
        }

        if(state.route.isEmpty())
            renderer.render(List.of(Component.translatable("caustics.node.route_direct")), 10);
        else {
            renderer.render(List.of(Component.translatable("caustics.node.route_start")), 10);
            renderer.render(state.route, 1);
        }
    }

    public static AlidadeHudRenderer getInstance() {
        return INSTANCE;
    }

    private record HudRenderState(Component nodeName, List<Component> deposits, List<Component> route) { }
}
