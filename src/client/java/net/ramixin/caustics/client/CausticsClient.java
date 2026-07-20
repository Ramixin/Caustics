package net.ramixin.caustics.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientHotbarScrollEvents;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.rendering.huds.AbstractHud;
import net.ramixin.caustics.client.rendering.huds.AlidadeHud;
import net.ramixin.caustics.client.rendering.huds.CollimatorHud;
import net.ramixin.caustics.client.rendering.huds.ListeningHud;
import net.ramixin.caustics.client.rendering.pipelines.LeapParticleRenderPipeline;
import net.ramixin.caustics.client.rendering.pipelines.NodeRenderPipeline;
import net.ramixin.caustics.items.components.SpyglassLens;

public class CausticsClient implements ClientModInitializer {

    public static final Identifier ALIDADE_GUI_TEXTURE = Caustics.id("textures/misc/alidade_scope.png");
    public static final Identifier DOWSER_GUI_TEXTURE = Caustics.id("textures/misc/dowser_scope.png");
    public static final Identifier COLLIMATOR_GUI_TEXTURE = Caustics.id("textures/misc/collimator_scope.png");

    public static int MAX_SIGNAL_RANGE = 256 * 256;

    public static final RenderStateDataKey<Double> OPACITY_KEY = RenderStateDataKey.create(() -> "caustics:opacity");
    public static final RenderStateDataKey<Double> OPACITY_DEFAULT_KEY = RenderStateDataKey.create(() -> "caustics:opacity_default");

    public static AbstractHud<?, ?, ?> hud = null;

    @Override
    public void onInitializeClient() {
        NodeRenderPipeline.getInstance().onInitialize();
        LeapParticleRenderPipeline.getInstance().onInitialize();
        AbstractHud.onInitialize();
        ModClientNetworking.onInitialize();
        ModMixsonClient.onInitialize();

        ClientHotbarScrollEvents.ALLOW.register((_, _, _, _, dy) -> {
            if(CausticsClient.hud instanceof ListeningHud listener)
                return !listener.mouseScrolled(dy);
            return true;
        });

        LevelRenderEvents.END_MAIN.register(_ -> ClientCrystalNetwork.getInstance().caches().wipeAll());
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> ClientCrystalNetwork.getInstance().nuke());
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            if(minecraft.level == null) return;
            if(minecraft.level.tickRateManager().isFrozen()) return;
            ClientCrystalNetwork.getInstance().tick();
        });

        ClientTickEvents.START_CLIENT_TICK.register(minecraft -> {
            if(minecraft.player == null) return;
            if(hud == null) return;
            if(!hud.isCloseable()) return;
            if(minecraft.player.isUsingItem()) return;
            hud = null;
            Caustics.LOGGER.info("HUD disabled");
        });


        ItemEvents.USE.register((level, player, hand) -> {
            if(!level.isClientSide()) return null;
            ItemStack stack = player.getItemInHand(hand);
            if(!stack.is(Items.SPYGLASS)) return null;
            if(SpyglassLens.isAlidade(stack)) {
                hud = new AlidadeHud();
                Caustics.LOGGER.info("Alidade HUD enabled");
            } else if(SpyglassLens.isCollimator(stack)) {
                hud = new CollimatorHud();
                Caustics.LOGGER.info("Collimator HUD enabled");
            }
            return null;
        });
    }
}
