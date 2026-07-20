package net.ramixin.caustics.client.rendering.renderers;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.ramixin.caustics.ModTags;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.nodes.icons.CollimatorIcon;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

import java.util.Set;

import static net.ramixin.caustics.client.rendering.RenderUtil.*;

public class CollimatorRenderer extends NodeRenderer<CollimatorIcon, CollimatorRenderer.RenderState> {

    private static final float SIZE = 1 / Mth.sqrt(2f);

    public CollimatorRenderer() {
        super(ClientCrystalNetwork.getInstance().caches().collimator(), ModTags.Items.COLLIMATOR_LENS);
    }

    @Override
    public RenderState extractIcon(CollimatorIcon icon, Set<Integer> ambiguities, int i, float partialTicks) {
        Vec3 pos = icon.getCenterPos();
        float lookedAt = Mth.lerp(partialTicks, icon.previousLookedAt(), icon.lookedAt()) / 2f;
        return new RenderState(pos.x, pos.y, pos.z, lookedAt, ambiguities.contains(i));
    }

    @Override
    public void renderIcon(LevelRenderContext ctx, BufferBuilder buffer, RenderState state) {
        Vec3 cameraPos = ctx.levelState().cameraRenderState.pos;
        float scale = getDistanceScale(cameraPos, state.x, state.y, state.z, Mth.lerp(state.lookedAt, 0.05, 0.0625));
        Vec2[] offsets = new Vec2[] {
                new Vec2(-SIZE, SIZE),
                new Vec2(SIZE, SIZE),
                new Vec2(SIZE, -SIZE),
                new Vec2(-SIZE, -SIZE)
        };

        Vector3fc[] vertices = billboardVertices(state.x, state.y, state.z, cameraPos, offsets, scale, 0);

        int color;
        if(state.ambiguous) color = 0x99_99_99_99;
        else color = lerpColor(state.lookedAt, 0xCA_25_EF, 0x4A_EF_25, 0x99);
        Matrix4f matrix = ctx.poseStack().last().pose();
        for(int i = 0; i < 4; i++) {
            Vector3fc v = vertices[i];
            buffer.addVertex(matrix, v.x(), v.y(), v.z()).setColor(color);
        }
    }

    public record RenderState(double x, double y, double z, float lookedAt, boolean ambiguous) { }
}
