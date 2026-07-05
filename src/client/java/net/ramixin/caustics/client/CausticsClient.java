package net.ramixin.caustics.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientHotbarScrollEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.nodes.ClientNode;
import net.ramixin.caustics.client.nodes.NodesRenderPipeline;
import net.ramixin.caustics.items.ModItems;
import net.ramixin.caustics.networking.clientbound.*;
import net.ramixin.caustics.networking.serverbound.RequestLeaptionPayload;
import net.ramixin.caustics.nodes.routing.Route;
import net.ramixin.caustics.utils.LookUtil;

import java.util.Optional;

public class CausticsClient implements ClientModInitializer {

    public static final Identifier ALIDADE_GUI_TEXTURE = Caustics.id("textures/misc/alidade_scope.png");

    public static final LookManager LOOK_MANAGER = new LookManager();

    public static int MAX_SIGNAL_RANGE = 256;

    public static final RenderStateDataKey<Double> GHOST_PROGRESS_KEY = RenderStateDataKey.create(() -> "caustics:ghost_progress");
    public static final RenderStateDataKey<Double> PLAYER_PROGRESS_KEY = RenderStateDataKey.create(() -> "caustics:player_progress");
    public static final RenderStateDataKey<Double> OPACITY_DEFAULT_KEY = RenderStateDataKey.create(() -> "caustics:opacity_default");

    @Override
    public void onInitializeClient() {
        NodesRenderPipeline.getInstance().onInitialize();

        ClientPlayNetworking.registerGlobalReceiver(NodeSyncPayload.TYPE, (payload, _) -> ClientCrystalNetwork.getInstance().onNodeSync(payload.nodeData()));
        ClientPlayNetworking.registerGlobalReceiver(SignalRangeSyncPayload.TYPE, (payload, _) -> {
            int val = payload.newValue();
            Caustics.LOGGER.info("Signal range changed to {} on client", val);
            MAX_SIGNAL_RANGE = val * val;
        });
        ClientPlayNetworking.registerGlobalReceiver(FrequencySyncPayload.TYPE, (payload, _) -> ClientCrystalNetwork.getInstance().onFrequencySync(payload.frequencies(), payload.frequencyNames()));
        ClientPlayNetworking.registerGlobalReceiver(RoutingSyncPayload.TYPE, (payload, _) -> ClientCrystalNetwork.getInstance().onRoutingSync(payload.routingTables()));

        ClientHotbarScrollEvents.ALLOW.register((inventory, _, _, _, dy) -> {
            Optional<BlockPos> lookingAt = LookUtil.getLookingAt(inventory.player, LOOK_MANAGER.getPositions());
            if(lookingAt.isEmpty()) {
                ClientCrystalNetwork.getInstance().clearScrollPos();
                return true;
            }
            ClientCrystalNetwork.getInstance().deltaScrollPos(-dy);
            return false;
        });

        LevelRenderEvents.END_MAIN.register(_ -> LOOK_MANAGER.wipe());
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> ClientCrystalNetwork.getInstance().nuke());

        ModMixsonClient.onInitialize();

        UseItemCallback.EVENT.register((player, level, hand) -> {
            if(!level.isClientSide()) return InteractionResult.PASS;
            ItemStack stack = player.getItemInHand(hand);
            if(!stack.is(ModItems.LEAPER)) return InteractionResult.PASS;
            if(player.getCooldowns().isOnCooldown(stack)) return InteractionResult.PASS;
            Optional<BlockPos> selectedPosition = ClientCrystalNetwork.getInstance().getSelectedNode();
            if(selectedPosition.isEmpty()) return InteractionResult.PASS;
            BlockPos pos = selectedPosition.get();
            Optional<Route> route = LookUtil.getRouteToPos(LOOK_MANAGER.getPositions(), LOOK_MANAGER.getRoutes(), pos);
            if(route.isEmpty()) return InteractionResult.PASS;
            Optional<ClientNode> maybeNode = ClientCrystalNetwork.getInstance().getTargetableNodeAt(pos);
            if(maybeNode.isEmpty()) return InteractionResult.PASS;
            ClientNode node = maybeNode.get();
            int scrollPos = ClientCrystalNetwork.getInstance().getSelectedScrollPos();
            BlockPos peridotPos = node.peridotPositions().get(scrollPos);
            ClientPlayNetworking.send(new RequestLeaptionPayload(route.get(), peridotPos));
            //ClientCrystalNetwork.getInstance().deselectNode();
            return InteractionResult.SUCCESS;
        });

        ClientPlayNetworking.registerGlobalReceiver(LeapStatusPayload.TYPE, (payload, ctx) -> {
            switch(payload.status()) {
                case FAILURE -> onLeapFailure((LeapStatusPayload.Failure) payload, ctx);
            }
            if(payload.status().stopsLeaps())
                ctx.player().stopUsingItem();
        });
    }

    private static void onLeapFailure(LeapStatusPayload.Failure failure, ClientPlayNetworking.Context ctx) {
        Caustics.LOGGER.error("Leap failed: {}", failure.reason());
    }

    public static void onAlidadeAttack() {
        Optional<Integer> closest = LookUtil.calculateClosestLooking(LOOK_MANAGER.getAngles());
        if(closest.isEmpty()) return;
        ClientCrystalNetwork.getInstance().selectNode(LOOK_MANAGER.getPositions()[closest.get()]);
    }
}
