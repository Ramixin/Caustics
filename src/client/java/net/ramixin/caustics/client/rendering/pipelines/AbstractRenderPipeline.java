package net.ramixin.caustics.client.rendering.pipelines;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.ramixin.caustics.Caustics;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public abstract class AbstractRenderPipeline {

    private final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.BIG_BUFFER_SIZE);
    private final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private final Vector3f MODEL_OFFSET = new Vector3f();
    private final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    private MappableRingBuffer vertexBuffer;
    private BufferBuilder buffer;
    private final String name;
    private final RenderPipeline pipeline;

    protected AbstractRenderPipeline(String name, RenderPipeline pipeline) {
        this.name = name;
        this.pipeline = pipeline;
    }

    public void onInitialize() {
        LevelRenderEvents.END_EXTRACTION.register(this::extract);
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(this::render);
    }

    protected abstract void extract(LevelExtractionContext ctx);

    protected abstract void getVertices(LevelRenderContext ctx, Runnable submit);

    protected abstract void applyRenderPass(RenderPass renderPass, MeshData.DrawState drawParameters, GpuBuffer vertices, GpuBuffer indices, VertexFormat.IndexType indexType);

    protected void render(LevelRenderContext ctx) {
        getVertices(ctx, this::finish);
    }

    protected GpuBuffer upload(MeshData.DrawState drawParameters, VertexFormat format, MeshData builtBuffer) {
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();
        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
            }

            vertexBuffer = new MappableRingBuffer(() -> Caustics.idString(name+"_buffer"), GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
        }

        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertexBuffer.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
        }

        return vertexBuffer.currentBuffer();
    }

    protected void draw(MeshData builtBuffer, MeshData.DrawState drawParameters, GpuBuffer vertices) {
        Minecraft client = Minecraft.getInstance();
        // Sort the quads if there is translucency
        builtBuffer.sortQuads(ALLOCATOR, RenderSystem.getProjectionType().vertexSorting());
        // Upload the index buffer
        ByteBuffer byteBuffer = builtBuffer.indexBuffer();
        if(byteBuffer == null) return;
        GpuBuffer indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(byteBuffer);
        VertexFormat.IndexType indexType = builtBuffer.drawState().indexType();

        // Actually execute the draw
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        GpuTextureView colorTextureView = client.getMainRenderTarget().getColorTextureView();
        if(colorTextureView == null) return;
        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> Caustics.idString(name+"_renderpass"), colorTextureView, OptionalInt.empty(), client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            applyRenderPass(renderPass, drawParameters, vertices, indices, indexType);
        }

        builtBuffer.close();
    }

    protected BufferBuilder getBuffer() {
        if (this.buffer == null)
            this.buffer = new BufferBuilder(getAllocator(), pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
        return buffer;
    }

    protected ByteBufferBuilder getAllocator() {
        return ALLOCATOR;
    }

    private void finish() {
        MeshData builtBuffer = this.buffer.buildOrThrow();
        MeshData.DrawState drawParameters = builtBuffer.drawState();
        VertexFormat format = drawParameters.format();

        GpuBuffer vertices = upload(drawParameters, format, builtBuffer);
        draw(builtBuffer, drawParameters, vertices);

        this.vertexBuffer.rotate();
        this.buffer = null;
    }

}
