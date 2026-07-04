package net.ramixin.caustics.nodes.core;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.ramixin.caustics.nodes.Node;
import net.ramixin.caustics.nodes.Leap;
import net.ramixin.caustics.nodes.PlayerAccess;
import net.ramixin.caustics.nodes.routing.NodeMappedRoute;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

public class LeaptionHandler {

    private final HashMap<UUID, Leap> activeLeaps = new HashMap<>();
    private final PlayerAccess playerAccess = new PlayerAccess();

    protected void tick(ServerLevel level, CrystalNetwork network) {
        playerAccess.attach(level);
        final Iterator<UUID> each = activeLeaps.keySet().iterator();
        while (each.hasNext()) {
            UUID next = each.next();
            if(playerAccess.fromUUID(next).map(Player::isUsingItem).orElse(false)) continue;
            activeLeaps.get(next).cleanup(level);
            each.remove();
        }
        for(Leap leap : activeLeaps.values()) {
            leap.tick(level, network);
        }

    }

    protected void startLeap(UUID uuid, Node node, NodeMappedRoute route, BlockPos sapphirePos, BlockPos peridotPos) {
        activeLeaps.put(uuid, new Leap(uuid, node, route, sapphirePos, peridotPos));
    }

    public void clear() {
       activeLeaps.clear();
    }

    protected Optional<BlockPos> getLeapPos(UUID uuid) {
        Leap leap = activeLeaps.get(uuid);
        if(leap == null) return Optional.empty();
        return leap.getLeapPos();
    }

    protected void markLeapCompleted(UUID uuid) {
        Leap leap = activeLeaps.get(uuid);
        if(leap == null) return;
        leap.markCompleted();
    }
}
