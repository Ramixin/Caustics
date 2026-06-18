package net.ramixin.caustics.nodes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerAccess {

    ServerLevel level;

    public PlayerAccess() {}

    public void attach(ServerLevel level) {
        this.level = level;
    }

    public Optional<ServerPlayer> fromUUID(UUID uuid) {
        if(level == null) throw new IllegalStateException("PlayerAccess not attached to level");
        Player player = level.getPlayerByUUID(uuid);
        if(!(player instanceof ServerPlayer serverPlayer)) return Optional.empty();
        return Optional.of(serverPlayer);
    }

    public List<ServerPlayer> getAll() {
        if(level == null) throw new IllegalStateException("PlayerAccess not attached to level");
        return level.getServer().getPlayerList().getPlayers();
    }

}
