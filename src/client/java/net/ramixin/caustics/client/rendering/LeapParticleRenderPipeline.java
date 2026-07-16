package net.ramixin.caustics.client.rendering;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.nodes.LeapParticle;
import net.ramixin.caustics.client.nodes.LeapParticleEngine;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.ramixin.caustics.client.rendering.RenderUtil.billboardVertices;

public class LeapParticleRenderPipeline extends AbstractRenderPipeline {

    private static final RenderPipeline PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
            .withLocation(Caustics.id("pipeline/leap_particle"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR, VertexFormat.Mode.QUADS)
            .build()
    );
    private static final LeapParticleRenderPipeline INSTANCE = new LeapParticleRenderPipeline();

    private final List<RenderState> RENDER_STATES = new ArrayList<>();
    private DrawCommand DRAW_COMMAND = null;

    protected LeapParticleRenderPipeline() {
        super("leap_particles", PIPELINE);
    }

    public static LeapParticleRenderPipeline getInstance() {
        return INSTANCE;
    }

    @Override
    protected void extract(LevelExtractionContext ctx) {
        if(Minecraft.getInstance().level == null) return;
        RENDER_STATES.clear();
        DRAW_COMMAND = null;

        LeapParticleEngine particleEngine = ClientCrystalNetwork.getInstance().particleEngine();
        List<AbstractClientPlayer> players = ctx.level().players();
        for(AbstractClientPlayer player : players) {
            UUID uuid = player.getUUID();
            List<LeapParticle> particles = particleEngine.getParticles(uuid);
            if(particles.isEmpty()) continue;
            int count = particles.size();
            float partialTicks = ctx.deltaTracker().getGameTimeDeltaPartialTick(false);
            double[] x = new double[count];
            double[] y = new double[count];
            double[] z = new double[count];
            float[] u = new float[count];
            float[] v = new float[count];
            float[] opacity = new float[count];
            int[] lightmap = new int[count];
            float[] scale = new float[count];
            for(int i = 0; i < count; i++) {
                LeapParticle particle = particles.get(i);
                Vec3 position = particle.position();
                Vec3 oldPosition = particle.oldPosition();
                x[i] = Mth.lerp(partialTicks, oldPosition.x(), position.x());
                y[i] = Mth.lerp(partialTicks, oldPosition.y(), position.y());
                z[i] = Mth.lerp(partialTicks, oldPosition.z(), position.z());
                u[i] = particle.u();
                v[i] = particle.v();
                opacity[i] = (float) particle.opacity();
                lightmap[i] = LevelRenderer.getLightCoords(Minecraft.getInstance().level, BlockPos.containing(position.x(), position.y(), position.z()));
                scale[i] = (float) particle.scale(partialTicks);
            }
            PlayerSkin skin = Minecraft.getInstance().playerSkinRenderCache().getOrDefault(player.getProfile()).playerSkin();
            RenderState renderState = new RenderState(skin.body().texturePath(), x, y, z, u, v, opacity, lightmap, scale);
            RENDER_STATES.add(renderState);
        }
    }

    @Override
    protected void getVertices(LevelRenderContext ctx, Runnable submit) {
        for(RenderState state : RENDER_STATES) {
            getStateVertices(ctx, state);
            submit.run();
        }
    }

    protected void getStateVertices(LevelRenderContext ctx, RenderState state) {
        PoseStack matrices = ctx.poseStack();
        Vec3 cameraPos = ctx.levelState().cameraRenderState.pos;
        BufferBuilder buffer = getBuffer();
        for(int i = 0; i < state.x.length; i++) {
            Vec2[] offsets = new Vec2[] {
                    new Vec2(1f, 1), //bottom right
                    new Vec2(1f, -1f), //upper right
                    new Vec2(-1f, -1), //upper left
                    new Vec2(-1f, 1) //bottom left
            };
            Vector3fc[] vertices = billboardVertices(state.x[i], state.y[i], state.z[i], cameraPos, offsets, 0.04f * state.scale[i], 0);
            Matrix4f matrix = matrices.last().pose();
            int color = ((int)(state.opacity[i] * 255) << 24) | 0xFF_FF_FF;

            float minU = state.u[i] / 64f;
            float maxU = (state.u[i] + 1) / 64f;
            float minV = state.v[i] / 64f;
            float maxV = (state.v[i] + 1) / 64f;

            buffer.addVertex(matrix, vertices[0].x(), vertices[0].y(), vertices[0].z()).setColor(color).setUv(maxU, maxV).setLight(state.lightmap[i]); //bottom right
            buffer.addVertex(matrix, vertices[1].x(), vertices[1].y(), vertices[1].z()).setColor(color).setUv(maxU, minV).setLight(state.lightmap[i]); //upper right
            buffer.addVertex(matrix, vertices[2].x(), vertices[2].y(), vertices[2].z()).setColor(color).setUv(minU, minV).setLight(state.lightmap[i]); //upper right
            buffer.addVertex(matrix, vertices[3].x(), vertices[3].y(), vertices[3].z()).setColor(color).setUv(minU, maxV).setLight(state.lightmap[i]); //bottom left
        }
        DRAW_COMMAND = new DrawCommand(state.skin);
    }

    @Override
    protected void applyRenderPass(RenderPass renderPass, MeshData.DrawState drawParameters, GpuBuffer vertices, VertexFormat format, GpuBuffer indices, VertexFormat.IndexType indexType) {
        if(DRAW_COMMAND == null) return;
        renderPass.setVertexBuffer(0, vertices);
        renderPass.setIndexBuffer(indices, indexType);
        GpuBufferSlice projectionMatrixBuffer = RenderSystem.getProjectionMatrixBuffer();
        if(projectionMatrixBuffer == null) return;
        GpuBufferSlice shaderFog = RenderSystem.getShaderFog();
        if(shaderFog == null) return;
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(DRAW_COMMAND.skin);
        TextureSetup setup = TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler());

        renderPass.setUniform("Projection", projectionMatrixBuffer);
        renderPass.setUniform("Fog", shaderFog);
        renderPass.bindTexture("Sampler2", Minecraft.getInstance().gameRenderer.lightmap(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
        renderPass.bindTexture("Sampler0", setup.texure0(), setup.sampler0());
        renderPass.drawIndexed(0, 0, drawParameters.indexCount(), 1);
    }

    protected record RenderState(Identifier skin, double[] x, double[] y, double[] z, float[] u, float[] v, float[] opacity, int[] lightmap, float[] scale) { }
    private record DrawCommand(Identifier skin) { }
}
