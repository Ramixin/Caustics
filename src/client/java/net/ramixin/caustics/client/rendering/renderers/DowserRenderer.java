package net.ramixin.caustics.client.rendering.renderers;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.ramixin.caustics.ModTags;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.nodes.icons.DowserIcon;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

import java.util.Set;

import static net.ramixin.caustics.client.rendering.RenderUtil.billboardVertices;
import static net.ramixin.caustics.client.rendering.RenderUtil.getDistanceScale;

public class DowserRenderer extends NodeRenderer<DowserIcon, DowserRenderer.RenderState> {

    public DowserRenderer() {
        super(ClientCrystalNetwork.getInstance().caches().dowserCache(), ModTags.Items.DOWSER_LENS);
    }

    @Override
    public RenderState extractIcon(DowserIcon icon, Set<Integer> ambiguities, int i, float partialTicks) {
        Vec3 pos = icon.getCenterPos();
        boolean ambiguous = ambiguities.contains(i);
        float lookedAt = Mth.lerp(partialTicks, icon.previousLookedAt(), icon.lookedAt()) / 2f;
        float xOffset;
        float yOffset;
        int r;
        int g;
        int b;
        if(lookedAt > 0) {
            xOffset = 0;
            yOffset = 0;
            r = 0xBB;
            g = 0xBB;
            b = 0xBB;
        } else {
            xOffset = (float) icon.xOffset();
            yOffset = (float) icon.yOffset();
            r = icon.red();
            g = icon.green();
            b = icon.blue();
        }
        return new RenderState(pos.x, pos.y, pos.z, xOffset, yOffset, r, g, b, lookedAt, ambiguous);
    }

    @Override
    public void renderIcon(LevelRenderContext ctx, BufferBuilder buffer, RenderState state) {
        Vec3 cameraPos = ctx.levelState().cameraRenderState.pos;
        float scale = getDistanceScale(cameraPos, state.x, state.y, state.z, Mth.lerp(state.lookedAt, 0.05, 0.0625));
        Vec2[] offsets = new Vec2[] {
                new Vec2(0 + state.xOffset(), 1 + state.yOffset()),
                new Vec2(1 + state.xOffset(), 0 + state.yOffset()),
                new Vec2(0 + state.xOffset(), -1.4f + state.yOffset()),
                new Vec2(-1 + state.xOffset(), 0 + state.yOffset())
        };

        Vector3fc[] vertices = billboardVertices(state.x, state.y, state.z, cameraPos, offsets, scale, 0);

        int color;
        if(state.ambiguous) color = 0x99_99_99_99;
        else {
            int r = Mth.lerpInt(state.lookedAt, state.r, 0xFF) << 16;
            int g = Mth.lerpInt(state.lookedAt, state.g, 0x00) << 8;
            int b = Mth.lerpInt(state.lookedAt, state.b, 0x00);
            color = (0x99 << 24) | r | g | b;
        }
        Matrix4f matrix = ctx.poseStack().last().pose();
        for(int i = 0; i < 4; i++) {
            Vector3fc v = vertices[i];
            buffer.addVertex(matrix, v.x(), v.y(), v.z()).setColor(color);
        }
    }

    public record RenderState(double x, double y, double z, float xOffset, float yOffset, int r, int g, int b, float lookedAt, boolean ambiguous) { }
}
