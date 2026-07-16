package net.ramixin.caustics.client.rendering.renderers;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.ramixin.caustics.ModTags;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.nodes.icons.AlidadeIcon;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

import java.util.Set;

import static net.ramixin.caustics.client.rendering.RenderUtil.*;

public class AlidadeRenderer extends NodeRenderer<AlidadeIcon, AlidadeRenderer.RenderState> {

    public AlidadeRenderer() {
        super(ClientCrystalNetwork.getInstance().caches().alidade(), ModTags.Items.ALIDADE_LENS);
    }

    @Override
    public RenderState extractIcon(AlidadeIcon icon, Set<Integer> ambiguities, int i, float partialTicks) {
        Vec3 pos = icon.getCenterPos();
        boolean ambiguous = ambiguities.contains(i);
        double angle = Mth.lerp(partialTicks, icon.previousAngle(), icon.angle());
        float lookedAt = Mth.lerp(partialTicks, icon.previousLookedAt(), icon.lookedAt()) / 2f;
        return new RenderState(pos.x, pos.y, pos.z, angle, lookedAt, ambiguous);
    }

    @Override
    public void renderIcon(LevelRenderContext ctx, BufferBuilder buffer, RenderState state) {
        Vec3 cameraPos = ctx.levelState().cameraRenderState.pos;
        float scale = getDistanceScale(cameraPos, state.x, state.y, state.z, Mth.lerp(state.lookedAt, 0.05, 0.0625));
        Vec2[] offsets = new Vec2[] {
                new Vec2(0, 1),
                new Vec2(1, 0),
                new Vec2(0, -1),
                new Vec2(-1, 0)
        };

        Vector3fc[] vertices = billboardVertices(state.x, state.y, state.z, cameraPos, offsets, scale, state.angle / Math.PI / 32);

        int color;
        if(state.ambiguous) color = 0x99_99_99_99;
        else color = lerpColor(state.lookedAt, 0xFB_f8_40, 0xFB_40_46, 0x99);

        Matrix4f matrix = ctx.poseStack().last().pose();
        for(int i = 0; i < 4; i++) {
            Vector3fc v = vertices[i];
            buffer.addVertex(matrix, v.x(), v.y(), v.z()).setColor(color);
        }
    }

    public record RenderState(double x, double y, double z, double angle, float lookedAt, boolean ambiguous) {}
}
