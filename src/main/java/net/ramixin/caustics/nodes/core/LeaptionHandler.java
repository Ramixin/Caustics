package net.ramixin.caustics.nodes.core;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.ramixin.caustics.nodes.Node;
import net.ramixin.caustics.nodes.leaption.Leap;
import net.ramixin.caustics.nodes.leaption.Selection;
import net.ramixin.caustics.nodes.routing.NodeMappedRoute;
import net.ramixin.caustics.nodes.routing.Route;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class LeaptionHandler {

    private final HashMap<UUID, Leap> activeLeaps = new HashMap<>();
    private final HashMap<UUID, Selection> selections = new HashMap<>();

    protected void tick(ServerLevel level, CrystalNetwork network) {
        activeLeaps.keySet().removeIf(uuid -> {
            Player player = level.getPlayerByUUID(uuid);
            if(player == null) return true;
            if(player.isUsingItem()) return false;
            Leap leap = activeLeaps.get(uuid);
            leap.cleanup(level);
            return true;
        });
        for(Leap leap : activeLeaps.values()) {
            leap.tick(level, network);
        }

        selections.keySet().removeIf(uuid -> {
            Selection selection = selections.get(uuid);
            if(selection == null) return true;
            Optional<Node> maybeSapphire = network.nodeIndex().getNodeAt(selection.sapphirePos(), NodeIndex.Type.SAPPHIRE);
            Optional<Node> maybePeridot = network.nodeIndex().getNodeAt(selection.peridotPos(), NodeIndex.Type.PERIDOT);
            return maybeSapphire.isEmpty() || maybePeridot.isEmpty();
        });
    }

    public Optional<String> activateLeap(ServerLevel level, UUID uuid, InteractionHand hand) {
        Selection selection = selections.get(uuid);
        if(selection == null) return Optional.of("No node selected");
        Player player = level.getPlayerByUUID(uuid);

        if(player == null) return Optional.of("Player not found");
        CrystalNetwork network = CrystalNetwork.get(level);
        Optional<Node> maybeNode = network.nodeIndex().getNodeAt(selection.sapphirePos(), NodeIndex.Type.SAPPHIRE);
        if(maybeNode.isEmpty()) return Optional.of("Sapphire cluster not found");
        Node node = maybeNode.get();
        if(!node.data().peridotClusters().contains(selection.peridotPos())) return Optional.of("Peridot cluster not found");
        Optional<Route> maybeRoute = network.routingManager().findRoute(network, player.blockPosition(), selection.sapphirePos());
        if(maybeRoute.isEmpty()) return Optional.of("No valid route to node");
        Optional<NodeMappedRoute> maybeMappedRoute = maybeRoute.get().nodeMapped(network);
        if(maybeMappedRoute.isEmpty()) return Optional.of("route contained invalid nodes");

        activeLeaps.put(uuid, Leap.create(level.getServer(), player.getItemInHand(hand), uuid, node, maybeMappedRoute.get(), selection.sapphirePos(), selection.peridotPos()));
        return Optional.empty();
    }

    public void setSelection(UUID uuid, BlockPos sapphirePos, BlockPos peridotPos) {
        selections.put(uuid, new Selection(sapphirePos, peridotPos));
    }

    public Selection getSelection(UUID uuid) {
        return selections.get(uuid);
    }

    public void clear() {
       activeLeaps.clear();
    }

    public Optional<BlockPos> getLeapPos(UUID uuid) {
        Leap leap = activeLeaps.get(uuid);
        if(leap == null) return Optional.empty();
        return leap.getLeapPos();
    }

    public void markLeapCompleted(UUID uuid) {
        Leap leap = activeLeaps.get(uuid);
        if(leap == null) return;
        leap.markCompleted();
    }
}
