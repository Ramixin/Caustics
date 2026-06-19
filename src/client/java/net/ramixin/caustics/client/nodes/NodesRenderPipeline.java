package net.ramixin.caustics.client.nodes;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.client.CausticsClient;
import net.ramixin.caustics.client.LookManager;
import net.ramixin.caustics.client.TooltipRenderer;
import net.ramixin.caustics.items.ModItems;
import net.ramixin.caustics.items.components.Frequency;
import net.ramixin.caustics.nodes.routing.Route;
import net.ramixin.caustics.utils.LookUtil;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Most of the technical parts of this class were "borrowed" from the Fabric Docs example for world rendering.
 * The page can be found here: <a href="https://docs.fabricmc.net/develop/rendering/world">link</a>
 **/
public class NodesRenderPipeline {

    private static final RenderPipeline PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Caustics.id("pipeline/node"))
            .withDepthStencilState(Optional.empty())
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .build()
    );

    private static final List<NodeRenderState> RENDER_STATES = new ArrayList<>();
    private static HudRenderState HUD_RENDER_STATE = null;
    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private BufferBuilder buffer;
    private MappableRingBuffer vertexBuffer;

    private static final NodesRenderPipeline INSTANCE = new NodesRenderPipeline();

    public static NodesRenderPipeline getInstance() {
        return INSTANCE;
    }

    public void onInitialize() {
        LevelRenderEvents.END_EXTRACTION.register(this::extract);
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(this::renderAndDrawNodes);
        HudElementRegistry.addLast(Caustics.id("alidade_node_info"), NodesRenderPipeline::renderHud);
    }

    private void extract(LevelExtractionContext ctx) {
        RENDER_STATES.clear();
        HUD_RENDER_STATE = null;

        Optional<Integer> closest = extractNodes();
        closest.ifPresent(this::extractHud);
    }

    private Optional<Integer> extractNodes() {

        Player player = Minecraft.getInstance().player;
        if(player == null) return Optional.empty();
        if(!player.getMainHandItem().is(ModItems.ALIDADE)) return Optional.empty();
        if(!player.isUsingItem()) return Optional.empty();
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
        return closest;
    }

    private void extractHud(int closestIndex) {
        LookManager lookManager = CausticsClient.LOOK_MANAGER;

        BlockPos closestPos = lookManager.getPositions()[closestIndex];
        Route route = lookManager.getRoutes()[closestIndex];

        ClientCrystalNetwork.getInstance().setLastLookingAt(closestPos);
        Optional<ClientCrystalNode> maybeClosestNode = ClientCrystalNetwork.getInstance().getTargetableNodeAt(closestPos);
        if(maybeClosestNode.isEmpty()) return;
        ClientCrystalNode closestNode = maybeClosestNode.get();
        int scrollPos = ClientCrystalNetwork.getInstance().getScrollPos();
        Component nodeName = extractNodeName(closestPos);
        Optional<Component> depositName = closestNode.peridotPositions().isEmpty() ? Optional.empty() : Optional.of(extractDepositName(closestNode, scrollPos));
        List<Component> routeStrings = extractRoute(route);
        HUD_RENDER_STATE = new HudRenderState(nodeName, depositName, routeStrings);
    }

    private List<Component> extractRoute(Route route) {
        List<Component> routeStrings = new ArrayList<>();
        List<BlockPos> path = route.immutablePath();
        for(int i = 0; i < path.size() - 1; i++) {
            routeStrings.add(extractNodeName(path.get(i)));
        }
        return routeStrings;
    }

    private static Component extractNodeName(BlockPos pos) {
        Optional<Frequency> maybeFreq = ClientCrystalNetwork.getInstance().getFrequencyAt(pos);
        if(maybeFreq.isEmpty()) return Component.translatable("caustics.node.unknown_travel");
        Optional<String> maybeNodeName = ClientCrystalNetwork.getInstance().getFrequencyName(maybeFreq.get());
        return maybeNodeName.map(Component::literal).orElseGet(() -> Component.translatable("caustics.node.unnamed_travel"));
    }

    private static Component extractDepositName(ClientCrystalNode node, int scrollPos) {
        if(scrollPos >= node.peridotPositions().size() || scrollPos < 0) return Component.translatable("caustics.node.scroll_oob");
        BlockPos pos = node.peridotPositions().get(scrollPos);
        Optional<Frequency> maybeFreq = ClientCrystalNetwork.getInstance().getFrequencyAt(pos);
        if(maybeFreq.isEmpty()) return Component.translatable("caustics.node.unknown_deposit");
        Optional<String> maybeDepositName = ClientCrystalNetwork.getInstance().getFrequencyName(maybeFreq.get());
        return maybeDepositName.map(Component::literal).orElseGet(() -> Component.translatable("caustics.node.unnamed_deposit"));
    }

    private void renderAndDrawNodes(LevelRenderContext ctx) {
        for(NodeRenderState state : RENDER_STATES) {
            renderNode(ctx, state);
            drawFilledThroughWalls(Minecraft.getInstance());
        }
    }

    private static void renderHud(@NonNull GuiGraphicsExtractor evilGraphics, @NonNull DeltaTracker deltaTracker) {
        if(HUD_RENDER_STATE == null) return;
        int mouseX = (int) Minecraft.getInstance().mouseHandler.xpos();
        int mouseY = (int) Minecraft.getInstance().mouseHandler.ypos();
        TooltipRenderer renderer = new TooltipRenderer(evilGraphics, Minecraft.getInstance().font);

        renderer.render(List.of(Component.translatable("caustics.node.name")), -5, 20);
        renderer.render(List.of(HUD_RENDER_STATE.nodeName()), -5, 1);

        if(HUD_RENDER_STATE.maybeDepositName().isPresent()) {
            renderer.render(List.of(Component.translatable("caustics.node.deposit")), -5, 10);
            renderer.render(List.of(HUD_RENDER_STATE.maybeDepositName().get()), -5, 1);

        }

        if(HUD_RENDER_STATE.route().isEmpty())
            renderer.render(List.of(Component.translatable("caustics.node.route_direct")), -5, 10);
        else {
            renderer.render(List.of(Component.translatable("caustics.node.route_start")), -5, 10);
            renderer.render(HUD_RENDER_STATE.route(), -5, 1);
        }

        evilGraphics.extractDeferredElements(mouseX, mouseY, 0);
    }

    private void renderNode(LevelRenderContext ctx, NodeRenderState state) {
        PoseStack matrices = ctx.poseStack();
        Vec3 cameraPos = ctx.levelState().cameraRenderState.pos;

        if (this.buffer == null)
            this.buffer = new BufferBuilder(ALLOCATOR, PIPELINE.getVertexFormatMode(), PIPELINE.getVertexFormat());

        double dx = cameraPos.x - state.x;
        double dy = cameraPos.y - state.y;
        double dz = cameraPos.z - state.z;
        double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
        float scale = (float) (dist * (state.lookingAt ? 0.0625f : 0.05f));

        Camera camera = Minecraft.getInstance()
                .gameRenderer
                .getMainCamera();

        Vector3f right = camera.leftVector().negate(new Vector3f());
        Vector3f up = camera.upVector().negate(new Vector3f());

        float cx = (float)(state.x + 0.5 - cameraPos.x);
        float cy = (float)(state.y + 0.5 - cameraPos.y);
        float cz = (float)(state.z + 0.5 - cameraPos.z);

        Matrix4f matrix = matrices.last().pose();

        int color;
        if(state.ambiguous) color = 0xFF_00_00_FF;
        else if(state.lookingAt) color = 0xFF_00_FF_00;
        else color = 0xFF_FF_00_00;

        buffer.addVertex(matrix, cx + up.x * scale, cy + up.y * scale, cz + up.z * scale).setColor(color);
        buffer.addVertex(matrix, cx + right.x * scale, cy + right.y * scale, cz + right.z * scale).setColor(color);
        buffer.addVertex(matrix, cx - up.x * scale, cy - up.y * scale, cz - up.z * scale).setColor(color);
        buffer.addVertex(matrix, cx - right.x * scale, cy - right.y * scale, cz - right.z * scale).setColor(color);
    }

    private void drawFilledThroughWalls(Minecraft client) {
        MeshData builtBuffer = this.buffer.buildOrThrow();
        MeshData.DrawState drawParameters = builtBuffer.drawState();
        VertexFormat format = drawParameters.format();

        GpuBuffer vertices = this.upload(drawParameters, format, builtBuffer);

        draw(client, builtBuffer, drawParameters, vertices, format);

        this.vertexBuffer.rotate();
        this.buffer = null;
    }

    private GpuBuffer upload(MeshData.DrawState drawParameters, VertexFormat format, MeshData builtBuffer) {
        // Calculate the size needed for the vertex buffer
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

        // Initialize or resize the vertex buffer as needed
        if (this.vertexBuffer == null || this.vertexBuffer.size() < vertexBufferSize) {
            if (this.vertexBuffer != null) {
                this.vertexBuffer.close();
            }

            this.vertexBuffer = new MappableRingBuffer(() -> Caustics.MOD_ID + " node render pipeline", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
        }

        // Copy vertex data into the vertex buffer
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(this.vertexBuffer.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
        }

        return this.vertexBuffer.currentBuffer();
    }

    private static void draw(Minecraft client, MeshData builtBuffer, MeshData.DrawState drawParameters, GpuBuffer vertices, VertexFormat format) {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;

        if (NodesRenderPipeline.PIPELINE.getVertexFormatMode() == VertexFormat.Mode.QUADS) {
            // Sort the quads if there is translucency
            builtBuffer.sortQuads(ALLOCATOR, RenderSystem.getProjectionType().vertexSorting());
            // Upload the index buffer
            ByteBuffer byteBuffer = builtBuffer.indexBuffer();
            if(byteBuffer == null) return;
            indices = NodesRenderPipeline.PIPELINE.getVertexFormat().uploadImmediateIndexBuffer(byteBuffer);
            indexType = builtBuffer.drawState().indexType();
        } else {
            // Use the general shape index buffer for non-quad draw modes
            RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(NodesRenderPipeline.PIPELINE.getVertexFormatMode());
            indices = shapeIndexBuffer.getBuffer(drawParameters.indexCount());
            indexType = shapeIndexBuffer.type();
        }

        // Actually execute the draw
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        GpuTextureView colorTextureView = client.getMainRenderTarget().getColorTextureView();
        if(colorTextureView == null) return;
        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> Caustics.MOD_ID + " node render pipeline rendering", colorTextureView, OptionalInt.empty(), client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(NodesRenderPipeline.PIPELINE);

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            // Bind texture if applicable:
            // Sampler0 is used for texture inputs in vertices
            // renderPass.bindTexture("Sampler0", textureSetup.texure0(), textureSetup.sampler0());

            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);

            // The base vertex is the starting index when we copied the data into the vertex buffer divided by vertex size
            //noinspection ConstantValue
            renderPass.drawIndexed(0 / format.getVertexSize(), 0, drawParameters.indexCount(), 1);
        }

        builtBuffer.close();
    }

    private record NodeRenderState(int x, int y, int z, boolean lookingAt, boolean ambiguous) { }

    private record HudRenderState(Component nodeName, Optional<Component> maybeDepositName, List<Component> route) { }
}
