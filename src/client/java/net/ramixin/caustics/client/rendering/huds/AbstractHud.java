package net.ramixin.caustics.client.rendering.huds;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.client.CausticsClient;
import net.ramixin.caustics.client.nodes.cache.AbstractIconCache;
import net.ramixin.caustics.client.nodes.icons.NodeIcon;
import net.ramixin.caustics.client.rendering.TooltipRenderer;
import net.ramixin.caustics.items.components.SpyglassLens;
import net.ramixin.caustics.utils.LookUtil;

import java.util.Optional;

public abstract class AbstractHud<I extends NodeIcon, S extends AbstractHud.ProgressState<S>, C extends AbstractIconCache<I>> {

    protected final C cache;
    private final TagKey<Item> lensTag;
    protected S lookingState;
    protected S selectedState;
    protected int ticksLooking = 0;
    protected int prevTicksLooking = 0;

    public AbstractHud(C cache, TagKey<Item> lensTag) {
        this.cache = cache;
        this.lensTag = lensTag;
    }

    public static void onInitialize() {
        LevelRenderEvents.END_EXTRACTION.register((m) -> { if(CausticsClient.hud != null) CausticsClient.hud.extract(m); });
        HudElementRegistry.addLast(Caustics.id("spyglass_hud"), (g, _) -> { if(CausticsClient.hud != null) CausticsClient.hud.renderHud(g); });
        ClientTickEvents.START_CLIENT_TICK.register((m) -> { if(CausticsClient.hud != null) CausticsClient.hud.onTick(m); });
    }


    protected abstract S extractHudLooking(Optional<Integer> maybeClosest, float partialTicks);

    protected abstract S extractHudSelected(Optional<BlockPos> maybeSelected);

    protected abstract void renderHud(TooltipRenderer renderer, S state);

    private void renderHud(GuiGraphicsExtractor evilGraphics) {
        if(lookingState == null && selectedState == null) return;
        int mouseX = (int) Minecraft.getInstance().mouseHandler.xpos();
        int mouseY = (int) Minecraft.getInstance().mouseHandler.ypos();
        TooltipRenderer renderer = new TooltipRenderer(evilGraphics);

        if(lookingState != null) {
            renderer.leftAlign(105);
            renderHud(renderer, lookingState);
        }
        renderer.resetHeight();
        if(selectedState != null) {
            renderer.rightAlign(351);
            renderHud(renderer, selectedState);
        }

        evilGraphics.extractDeferredElements(mouseX, mouseY, 0);
    }

    protected void onTick(Minecraft minecraft) {
        Player player = minecraft.player;
        if(player == null) return;
        if(!player.isUsingItem() || !SpyglassLens.is(player.getUseItem(), lensTag)) return;
        prevTicksLooking = ticksLooking;
        double[] angles = cache.getAngles();
        if(LookUtil.calculateClosestLooking(angles).isEmpty()) {
            if(ticksLooking > 0) ticksLooking--;
        } else
        if(ticksLooking < 2) ticksLooking++;
    }

    protected void extract(LevelExtractionContext ctx) {
        Player player = Minecraft.getInstance().player;
        if(player == null) return;
        if(!player.isUsingItem() || !SpyglassLens.is(player.getUseItem(),lensTag)) {
            ticksLooking = 0;
            prevTicksLooking = 0;
            lookingState = null;
            selectedState = null;
            return;
        }
        float partialTicks = ctx.deltaTracker().getGameTimeDeltaPartialTick(true);
        if(ticksLooking != 0 && lookingState != null)
            lookingState = lookingState.withProgress(Mth.lerp(partialTicks, prevTicksLooking, ticksLooking) / 2f);
        else
            lookingState = null;
        selectedState = null;


        ClientLevel level = ctx.level();
        if(!level.isRaining()) {
            double[] angles = cache.getAngles();
            lookingState = extractHudLooking(LookUtil.calculateClosestLooking(angles), partialTicks);
        }
        selectedState = extractHudSelected(cache.getSelectedPos());
    }

    protected interface ProgressState<T extends ProgressState<T>> {
        T withProgress(float progress);
    }
}
