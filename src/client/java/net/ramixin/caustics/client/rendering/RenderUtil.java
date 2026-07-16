package net.ramixin.caustics.client.rendering;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.items.components.Frequency;
import net.ramixin.caustics.nodes.core.FrequencyRegistry;
import net.ramixin.caustics.nodes.routing.Route;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface RenderUtil {

    static List<Component> extractRoute(Route route) {
        List<Component> routeStrings = new ArrayList<>();
        List<BlockPos> path = route.immutablePath();
        for(BlockPos pos : path) {
            routeStrings.add(extractNodeName(pos));
        }
        return routeStrings;
    }

    static Component extractNodeName(BlockPos pos) {
        Optional<Frequency> maybeFreq = ClientCrystalNetwork.getInstance().frequencyRegistry().getFrequencyAt(pos);
        if(maybeFreq.isEmpty()) return Component.translatable("caustics.node.unknown_travel");
        Optional<String> maybeNodeName = ClientCrystalNetwork.getInstance().frequencyRegistry().getFrequencyName(maybeFreq.get());
        return maybeNodeName.map(Component::literal).orElseGet(() -> Component.translatable("caustics.node.unnamed_travel"));
    }

    static MutableComponent getFrequencyName(BlockPos pos, Component unnamedDefault) {
        return getFrequencyName(pos, unnamedDefault, true);
    }

    static MutableComponent getFrequencyName(BlockPos pos, Component unnamedDefault, boolean unknownIsUnnamed) {
        FrequencyRegistry registry = ClientCrystalNetwork.getInstance().frequencyRegistry();
        Optional<Frequency> maybeFreq = registry.getFrequencyAt(pos);
        if(maybeFreq.isEmpty()) return unknownIsUnnamed ? unnamedDefault.copy() : Component.translatable("caustics.frequency.unknown");
        Optional<String> maybeDepositName = registry.getFrequencyName(maybeFreq.get());
        return maybeDepositName.map(Component::literal).orElse(unnamedDefault.copy());
    }

    static float getDistanceScale(Vec3 one, double x2, double y2, double z2, double modifier) {
        double dx = one.x - x2;
        double dy = one.y - y2;
        double dz = one.z - z2;
        double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
        return (float) (dist * modifier);
    }

    static Vector3fc[] billboardVertices(double x1, double y1, double z1, Vec3 cameraPos, Vec2[] offsets, float scale, double theta) {
        Camera camera = Minecraft.getInstance()
                .gameRenderer
                .getMainCamera();

        Vector3f right = camera.leftVector().negate(new Vector3f());
        Vector3f up = camera.upVector().negate(new Vector3f());

        float cx = (float)(x1 - cameraPos.x);
        float cy = (float)(y1 - cameraPos.y);
        float cz = (float)(z1 - cameraPos.z);

        float cos = Mth.cos(theta);
        float sin = Mth.sin(theta);

        Vector3fc[] vertices = new Vector3fc[4];
        for(int i = 0; i < 4; i++) {
            Vec2 offset = offsets[i];
            float x = offset.x * cos - offset.y * sin;
            float y = offset.x * sin + offset.y * cos;
            vertices[i] = new Vector3f(
                    cx + (right.x * x + up.x * y) * scale,
                    cy + (right.y * x + up.y * y) * scale,
                    cz + (right.z * x + up.z * y) * scale
            );
        }
        return vertices;
    }

    static int lerpColor(float t, int color1, int color2, int alpha) {
        int r = Mth.lerpInt(t, (color1 >> 16) & 0xFF, (color2 >> 16) & 0xFF);
        int g = Mth.lerpInt(t, (color1 >> 8) & 0xFF, (color2 >> 8) & 0xFF);
        int b = Mth.lerpInt(t, color1 & 0xFF, color2 & 0xFF);
        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }

}
