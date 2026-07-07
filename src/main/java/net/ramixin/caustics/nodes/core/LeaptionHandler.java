package net.ramixin.caustics.nodes.core;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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

    public boolean activateLeap(ServerLevel level, UUID uuid) {
        Selection selection = selections.get(uuid);
        if(selection == null) return false;
        Player player = level.getPlayerByUUID(uuid);
        if(player == null) return false;
        CrystalNetwork network = CrystalNetwork.get(level);
        Optional<Node> maybeNode = network.nodeIndex().getNodeAt(selection.sapphirePos(), NodeIndex.Type.SAPPHIRE);
        if(maybeNode.isEmpty()) return false;
        Node node = maybeNode.get();
        if(!node.data().peridotClusters().contains(selection.peridotPos())) return false;
        Optional<Route> maybeRoute = network.routingManager().findRoute(network, player.blockPosition(), selection.sapphirePos());
        if(maybeRoute.isEmpty()) return false;
        Optional<NodeMappedRoute> maybeMappedRoute = maybeRoute.get().nodeMapped(network);
        if(maybeMappedRoute.isEmpty()) return false;

        activeLeaps.put(uuid, new Leap(uuid, node, maybeMappedRoute.get(), selection.sapphirePos(), selection.peridotPos()));
        return true;
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
