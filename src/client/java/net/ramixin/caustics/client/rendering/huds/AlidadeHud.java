package net.ramixin.caustics.client.rendering.huds;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.ramixin.caustics.ModTags;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.nodes.ClientNode;
import net.ramixin.caustics.client.nodes.cache.AlidadeIconCache;
import net.ramixin.caustics.client.nodes.icons.AlidadeIcon;
import net.ramixin.caustics.client.rendering.TooltipRenderer;
import net.ramixin.caustics.networking.bidirectional.AlidadeSelectionSyncPayload;
import net.ramixin.caustics.nodes.routing.Route;
import net.ramixin.caustics.utils.LookUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.ramixin.caustics.client.rendering.RenderUtil.*;

public class AlidadeHud extends AbstractHud<AlidadeIcon, AlidadeHud.HudRenderState, AlidadeIconCache> implements ListeningHud {

    private static final Component UNNAMED_PERIDOT = Component.translatable("caustics.frequency.unnamed.peridot");
    private static final String EMPTY_PREFIX = "  ";
    private static final Component SELECTED_HEADER = Component.translatable("caustics.hud.alidade.selected");
    private static final Component LOOKING_HEADER = Component.translatable("caustics.hud.collimator.looking");

    public AlidadeHud() {
        super(ClientCrystalNetwork.getInstance().caches().alidade(), ModTags.Items.ALIDADE_LENS);
    }

    @Override
    protected HudRenderState extractHudLooking(Optional<Integer> maybeClosest, float partialTicks) {
        if(maybeClosest.isEmpty()) return null;
        int closestIndex = maybeClosest.get();

        BlockPos closestPos = cache.getPositions()[closestIndex];
        Route route = cache.getRoutes()[closestIndex];
        cache.setLastLooking(closestPos);
        float progress = Mth.lerp(partialTicks, prevTicksLooking, ticksLooking) / 2f;

        return extractHud(LOOKING_HEADER, closestPos, cache.getScrollPos(), route, false, progress);
    }

    @Override
    protected HudRenderState extractHudSelected(Optional<BlockPos> selectedNode) {
        if(selectedNode.isEmpty()) return null;
        BlockPos pos = selectedNode.get();
        int scrollPos = cache.getSelectedScrollPos();
        int i = -1;
        AlidadeIconCache cache = ClientCrystalNetwork.getInstance().caches().alidade();
        BlockPos[] positions = cache.getPositions();
        for(int j = 0; j < positions.length; j++) {
            if(!positions[j].equals(pos)) continue;
            i = j;
            break;
        }
        if(i == -1) return null;
        return extractHud(SELECTED_HEADER, pos, scrollPos, cache.getRoutes()[i], true, 1f);
    }

    private HudRenderState extractHud(Component header, BlockPos pos, int scrollPos, Route route, boolean single, float progress) {
        Optional<ClientNode> maybeClosestNode = ClientCrystalNetwork.getInstance().getSapphireNodeAt(pos);
        if(maybeClosestNode.isEmpty()) return null;
        ClientNode closestNode = maybeClosestNode.get();
        Component nodeName = extractNodeName(pos);
        List<Component> deposits = extractDeposits(closestNode, scrollPos, single);
        List<Component> routeStrings = extractRoute(route);
        return new HudRenderState(header, nodeName, deposits, routeStrings, progress);
    }

    private List<Component> extractDeposits(ClientNode node, int scrollPos, boolean single) {
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

    @Override
    protected void renderHud(TooltipRenderer renderer, HudRenderState state) {
        renderer.setAlpha((int) (255 * state.progress));
        renderer.render(state.header, 22);

        renderer.render(List.of(state.nodeName), 1);
        List<Component> names = state.deposits;
        if (!names.isEmpty()) {
            renderer.render(List.of(Component.translatable("caustics.node.deposit")), 10);
            renderer.render(names, 1);
        }

        if (state.route.isEmpty())
            renderer.render(List.of(Component.translatable("caustics.node.route_direct")), 10);
        else {
            renderer.render(List.of(Component.translatable("caustics.node.route_start")), 10);
            renderer.render(state.route, 1);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean released) {
        if(released) return false;
        if(event.button() != 0) return false;
        Optional<Integer> closest = LookUtil.calculateClosestLooking(cache.getAngles());
        if(closest.isEmpty()) {
            cache.resetSelection();
            return false;
        }
        BlockPos sapphirePos = cache.getPositions()[closest.get()];
        Optional<ClientNode> maybeNode = ClientCrystalNetwork.getInstance().getSapphireNodeAt(sapphirePos);
        if(maybeNode.isEmpty()) return false;
        List<BlockPos> peridotPositions = maybeNode.get().peridot();
        int scrollPos = cache.getSelectedScrollPos();
        if(scrollPos >= peridotPositions.size() || scrollPos < 0) return false;
        AlidadeIcon icon = cache.get(sapphirePos);
        icon.bump();
        BlockPos peridotPos = peridotPositions.get(scrollPos);
        cache.select(sapphirePos);
        ClientPlayNetworking.send(new AlidadeSelectionSyncPayload(sapphirePos, peridotPos));
        return true;
    }

    @Override
    public boolean mouseScrolled(double dy) {
        BlockPos[] positions = cache.getPositions();
        Optional<BlockPos> lookingAt = LookUtil.getLookingAt(Minecraft.getInstance().player, positions);
        if(lookingAt.isEmpty()) {
            cache.resetScrollPos();
            return false;
        }
        AlidadeIcon icon = cache.get(lookingAt.get());
        cache.deltaScrollPos(-dy, icon);
        return true;
    }

    protected record HudRenderState(Component header, Component nodeName, List<Component> deposits, List<Component> route, float progress) implements ProgressState<HudRenderState> {

        @Override
        public HudRenderState withProgress(float progress) {
            return new HudRenderState(header, nodeName, deposits, route, progress);
        }
    }
}
