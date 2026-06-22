package net.ramixin.caustics.nodes.core;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.ramixin.caustics.nodes.CrystalNode;
import net.ramixin.caustics.nodes.Leap;
import net.ramixin.caustics.nodes.PlayerAccess;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class LeaptionHandler {

    private final HashMap<UUID, Leap> activeLeaps = new HashMap<>();
    private final PlayerAccess playerAccess = new PlayerAccess();

    protected void tick(ServerLevel level, CrystalNetwork network) {
        playerAccess.attach(level);
        activeLeaps.entrySet().removeIf(entry -> playerAccess.fromUUID(entry.getKey()).map(Player::isUsingItem).orElse(false));
        for(Leap leap : activeLeaps.values()) {
            leap.tick(level, network);
        }

    }

    protected void startLeap(UUID uuid, CrystalNode node, BlockPos sapphirePos, BlockPos peridotPos) {
        activeLeaps.put(uuid, new Leap(uuid, node, sapphirePos, peridotPos));
    }

    public void clear() {
       activeLeaps.clear();
    }

    protected Optional<BlockPos> getLeapPos(UUID uuid) {
        Leap leap = activeLeaps.get(uuid);
        if(leap == null) return Optional.empty();
        return leap.getLeapPos();
    }
}
