package net.ramixin.caustics.client.rendering.huds;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.ramixin.caustics.ModTags;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.nodes.cache.SimpleIconCache;
import net.ramixin.caustics.client.nodes.icons.CollimatorIcon;
import net.ramixin.caustics.client.rendering.TooltipRenderer;

import java.util.Optional;

public class CollimatorHud extends AbstractHud<CollimatorIcon, CollimatorHud.HudRenderState, SimpleIconCache<CollimatorIcon>> {

    private static final Component SELECTED_HEADER = Component.translatable("caustics.collimator.selected");
    private static final Component LOOKING_HEADER = Component.translatable("caustics.collimator.name");

    public CollimatorHud() {
        super(ClientCrystalNetwork.getInstance().caches().collimator(), ModTags.Items.COLLIMATOR_LENS);
    }

    @Override
    protected HudRenderState extractHudLooking(Optional<Integer> maybeClosest, float partialTicks) {
        if(maybeClosest.isEmpty()) return null;
        int closestIndex = maybeClosest.get();
        BlockPos closestPos = cache.getPositions()[closestIndex];

        return null;
    }

    @Override
    protected HudRenderState extractHudSelected(Optional<BlockPos> maybeSelected) {
        return null;
    }

    @Override
    protected void renderHud(TooltipRenderer renderer, HudRenderState state) {

    }

    protected record HudRenderState(Component header, float progress) implements ProgressState<HudRenderState> {
        @Override
        public HudRenderState withProgress(float progress) {
            return new HudRenderState(header, progress);
        }
    }
}
