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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.client.CausticsClient;
import net.ramixin.caustics.client.LookManager;
import net.ramixin.caustics.items.ModItems;
import net.ramixin.caustics.utils.LookUtil;
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
        super("alidade_nodes", PIPELINE);
    }

    public static NodesRenderPipeline getInstance() {
        return INSTANCE;
    }

    @Override
    protected void extract(LevelExtractionContext ctx) {
        RENDER_STATES.clear();


        Player player = Minecraft.getInstance().player;
        if(player == null) return;
        if(!player.getMainHandItem().is(ModItems.ALIDADE)) return;
        if(!player.isUsingItem()) return;

        ClientLevel level = ctx.level();
        if(level.isRaining()) return;
        extractNodes();
    }

    private void extractNodes() {
        LookManager lookManager = CausticsClient.LOOK_MANAGER;
        BlockPos[] positions = lookManager.getPositions();
        double[] angles = lookManager.getAngles();
        Optional<Integer> closest = LookUtil.calculateClosestLooking(angles);
        Set<Integer> ambiguities = lookManager.getAmbiguityIndices();
        for(int i = 0; i < positions.length; i++) {
            BlockPos pos = positions[i];
            boolean ambiguous = ambiguities.contains(i);
            boolean lookingAt = !ambiguous && closest.isPresent() && closest.get().equals(i);

            RENDER_STATES.add(new NodeRenderState(pos.getX(), pos.getY(), pos.getZ(), lookingAt, ambiguous));
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
        float scale = (float) (dist * (state.lookingAt ? 0.0625f : 0.05f));

        Vec2[] offsets = new Vec2[] {
                new Vec2(0, 1),
                new Vec2(1, 0),
                new Vec2(0, -1),
                new Vec2(-1, 0)
        };

        Vector3fc[] vertices = billboardVertices(new Vec3(state.x, state.y, state.z), cameraPos, offsets, scale);

        Matrix4f matrix = matrices.last().pose();

        int color;
        if(state.ambiguous) color = 0xFF_00_00_FF;
        else if(state.lookingAt) color = 0xFF_00_FF_00;
        else color = 0xFF_FF_00_00;

        BufferBuilder buffer = getBuffer();
        for(int i = 0; i < 4; i++) {
            Vector3fc v = vertices[i];
            buffer.addVertex(matrix, v.x(), v.y(), v.z()).setColor(color);
        }
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

    protected record NodeRenderState(int x, int y, int z, boolean lookingAt, boolean ambiguous) { }
}
