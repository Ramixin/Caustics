package net.ramixin.caustics.client.rendering;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.client.CausticsClient;
import net.ramixin.caustics.client.LookManager;
import net.ramixin.caustics.client.rendering.node.NodeIcon;
import net.ramixin.caustics.items.components.SpyglassLens;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class NodesRenderPipeline extends AbstractRenderPipeline<NodesRenderPipeline.NodeRenderState> {

    private static final RenderPipeline PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Caustics.id("pipeline/node"))
            .withDepthStencilState(Optional.empty())
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .build()
    );

    private static final List<NodeRenderState> RENDER_STATES = new ArrayList<>();

    private static final NodesRenderPipeline INSTANCE = new NodesRenderPipeline();

    protected NodesRenderPipeline() {
        super("alidade_nodes", PIPELINE, false);
    }

    public static NodesRenderPipeline getInstance() {
        return INSTANCE;
    }

    @Override
    protected void extract(LevelExtractionContext ctx) {
        RENDER_STATES.clear();


        Player player = Minecraft.getInstance().player;
        if(player == null) return;
        if(!SpyglassLens.isAlidade(player.getUseItem())) return;
        if(!player.isUsingItem()) return;

        ClientLevel level = ctx.level();
        if(level.isRaining()) return;
        LookManager lookManager = CausticsClient.LOOK_MANAGER;
        BlockPos[] positions = lookManager.getPositions();
        NodeIcon[] icons = lookManager.getIcons();
        if(positions.length != icons.length)
            throw new IllegalStateException("Positions and icons must be the same length");
        Set<Integer> ambiguities = lookManager.getAmbiguityIndices();
        float partialTicks = ctx.deltaTracker().getGameTimeDeltaPartialTick(false);
        for(int i = 0; i < positions.length; i++) {
            BlockPos pos = positions[i];
            NodeIcon icon = icons[i];
            if(icon == null) continue;
            boolean ambiguous = ambiguities.contains(i);
            double angle = Mth.lerp(partialTicks, icon.previousAngle(), icon.angle());
            float lookedAt = Mth.lerp(partialTicks, icon.previousLookedAt(), icon.lookedAt()) / 2f;

            RENDER_STATES.add(new NodeRenderState(pos.getX(), pos.getY(), pos.getZ(), angle, lookedAt, ambiguous));
        }
    }

    @Override
    protected void getVertices(LevelRenderContext ctx, NodeRenderState state) {
        PoseStack matrices = ctx.poseStack();
        Vec3 cameraPos = ctx.levelState().cameraRenderState.pos;

        double dx = cameraPos.x - state.x;
        double dy = cameraPos.y - state.y;
        double dz = cameraPos.z - state.z;
        double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
        float scale = (float) (dist * Mth.lerp(state.lookedAt, 0.05f, 0.0625f));

        Vec2[] offsets = new Vec2[] {
                new Vec2(0, 1),
                new Vec2(1, 0),
                new Vec2(0, -1f),
                new Vec2(-1, 0)
        };

        Vector3fc[] vertices = billboardVertices(new Vec3(state.x, state.y, state.z), cameraPos, offsets, scale, state.angle / Math.PI / 32);

        matrices.pushPose();
        Matrix4f matrix = matrices.last().pose();

        int value = (int) (state.lookedAt * 255);
        int color;
        if(state.ambiguous) color = 0x99_00_00_FF;
        else color = 0x99_00_00_00 | (255-value << 16) | ((value) << 8);

        BufferBuilder buffer = getBuffer();
        for(int i = 0; i < 4; i++) {
            Vector3fc v = vertices[i];
            buffer.addVertex(matrix, v.x(), v.y(), v.z()).setColor(color);
        }
        matrices.popPose();
    }

    @Override
    protected void applyRenderPass(RenderPass renderPass, MeshData.DrawState drawParameters, GpuBuffer vertices, VertexFormat format, GpuBuffer indices, VertexFormat.IndexType indexType) {
        renderPass.setVertexBuffer(0, vertices);
        renderPass.setIndexBuffer(indices, indexType);

        // The base vertex is the starting index when we copied the data into the vertex buffer divided by vertex size
        //noinspection ConstantValue
        renderPass.drawIndexed(0 / format.getVertexSize(), 0, drawParameters.indexCount(), 1);
    }

    @Override
    protected List<NodeRenderState> getStates() {
        return RENDER_STATES;
    }

    protected record NodeRenderState(int x, int y, int z, double angle, float lookedAt, boolean ambiguous) { }
}
