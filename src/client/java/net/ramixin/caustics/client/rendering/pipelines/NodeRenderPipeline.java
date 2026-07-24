package net.ramixin.caustics.client.rendering.pipelines;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.Player;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.client.rendering.renderers.AlidadeRenderer;
import net.ramixin.caustics.client.rendering.renderers.CollimatorRenderer;
import net.ramixin.caustics.client.rendering.renderers.DowserRenderer;
import net.ramixin.caustics.client.rendering.renderers.NodeRenderer;
import net.ramixin.caustics.items.components.SpyglassLens;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NodeRenderPipeline extends AbstractRenderPipeline {

    private static final RenderPipeline PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Caustics.id("pipeline/node"))
            .withDepthStencilState(Optional.empty())
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .build()
    );

    private static final NodeRenderPipeline INSTANCE = new NodeRenderPipeline();

    private final List<NodeRenderer<?, ?>> RENDERERS = new ArrayList<>();
    private boolean collectVertices = false;

    protected NodeRenderPipeline() {
        super("alidade_nodes", PIPELINE);
    }

    public static NodeRenderPipeline getInstance() {
        return INSTANCE;
    }

    public void registerRenderer(NodeRenderer<?, ?> renderer) {
        RENDERERS.add(renderer);
    }

    @Override
    protected void extract(LevelExtractionContext ctx) {

        Player player = Minecraft.getInstance().player;
        if(player == null) return;
        if(!player.isUsingItem()) return;

        ClientLevel level = ctx.level();
        if(level.isRaining()) return;
        boolean result = false;
        for(NodeRenderer<?, ?> renderer : RENDERERS)
            if(SpyglassLens.is(player.getUseItem(), renderer.getLensTag()))
                result |= renderer.extract(ctx);
        collectVertices = result;
    }

    @Override
    protected void getVertices(LevelRenderContext ctx, Runnable submit) {
        if(!collectVertices) return;
        collectVertices = false;
        BufferBuilder buffer = getBuffer();
        for(NodeRenderer<?, ?> renderer : RENDERERS)
            renderer.render(ctx, buffer);

        submit.run();
    }

    @Override
    protected void applyRenderPass(RenderPass renderPass, MeshData.DrawState drawParameters, GpuBuffer vertices, GpuBuffer indices, VertexFormat.IndexType indexType) {
        renderPass.setVertexBuffer(0, vertices);
        renderPass.setIndexBuffer(indices, indexType);
        renderPass.drawIndexed(0, 0, drawParameters.indexCount(), 1);
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

        registerRenderer(new AlidadeRenderer());
        registerRenderer(new DowserRenderer());
        registerRenderer(new CollimatorRenderer());
    }
}
