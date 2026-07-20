package net.ramixin.caustics.client.rendering.renderers;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.ramixin.caustics.client.nodes.cache.AbstractIconCache;
import net.ramixin.caustics.client.nodes.icons.NodeIcon;

import java.util.ArrayDeque;
import java.util.Set;

public abstract class NodeRenderer<I extends NodeIcon, S> {

    private final ArrayDeque<S> states = new ArrayDeque<>();
    private final AbstractIconCache<I> cache;
    private final TagKey<Item> lensTag;

    public NodeRenderer(AbstractIconCache<I> cache, TagKey<Item> lensTag) {
        this.cache = cache;
        this.lensTag = lensTag;
    }

    public abstract S extractIcon(I icon, Set<Integer> ambiguities, int i, float partialTicks);

    public abstract void renderIcon(LevelRenderContext ctx, BufferBuilder buffer, S state);

    public boolean extract(LevelExtractionContext ctx) {
        I[] icons = cache.getIcons();
        Set<Integer> ambiguities = cache.getAmbiguityIndices();
        float partialTicks = ctx.deltaTracker().getGameTimeDeltaPartialTick(false);
        for(int i = 0; i < icons.length; i++) {
            I icon = icons[i];
            states.add(extractIcon(icon, ambiguities, i, partialTicks));
        }
        return !states.isEmpty();
    }

    public void render(LevelRenderContext ctx, BufferBuilder buffer) {
        while(!states.isEmpty())
            renderIcon(ctx, buffer, states.poll());
    }

    public TagKey<Item> getLensTag() {
        return lensTag;
    }
}
