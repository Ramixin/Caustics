package net.ramixin.caustics.nodes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.ramixin.caustics.nodes.core.CrystalNetwork;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Optional;
import java.util.UUID;

public final class Leap {
    private final UUID playerUUID;
    private final CrystalNode node;
    private final BlockPos sapphirePos;
    private final BlockPos peridotPos;
    private final Mutable<UUID> ghostUUID = new MutableObject<>();

    public Leap(UUID playerUUID, CrystalNode node, BlockPos sapphirePos, BlockPos peridotPos) {
        this.playerUUID = playerUUID;
        this.node = node;
        this.sapphirePos = sapphirePos;
        this.peridotPos = peridotPos;
    }

    public void tick(ServerLevel level, CrystalNetwork network) {
        block: {
            Optional<CrystalNode> maybeNetNode = network.getNodeAt(sapphirePos);
            if(maybeNetNode.isEmpty()) break block;
            if(maybeNetNode.get() != node) break block;
            if(!node.visibleClusterAt(sapphirePos)) break block;
            if(!node.getDepositingPosAt(peridotPos).map(Optional::isPresent).orElse(false)) break block;
            return;
        }
        Player player = level.getPlayerByUUID(playerUUID);
        if(player == null) return;
        player.stopUsingItem();
    }


    public Optional<BlockPos> getLeapPos() {
        Optional<Optional<BlockPos>> pos = node.getDepositingPosAt(peridotPos);
        return pos.orElseGet(Optional::empty);
    }
}
