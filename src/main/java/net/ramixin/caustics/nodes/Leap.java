package net.ramixin.caustics.nodes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.ramixin.caustics.entities.LeapGhost;
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
        node.prioritizeUpdates(sapphirePos, peridotPos);
        if(noLongerValid(network)) {
            Player player = level.getPlayerByUUID(playerUUID);
            if(player == null) return;
            player.stopUsingItem();
            return;
        }
        if(ghostUUID.get() == null) {
            Player player = level.getPlayerByUUID(playerUUID);
            if(player == null) return;
            LeapGhost ghost = new LeapGhost(level);
            ghost.setProfile(player.getProfile());
            updateEntityPos(ghost);
            level.addFreshEntity(ghost);
            ghostUUID.setValue(ghost.getUUID());
        }
        Entity entity = level.getEntity(ghostUUID.get());
        if(entity == null) return;
        updateEntityPos(entity);
    }

    public boolean noLongerValid(CrystalNetwork network) {
        Optional<CrystalNode> maybeNetNode = network.getNodeAt(sapphirePos);
        if(maybeNetNode.isEmpty()) return true;
        if(maybeNetNode.get() != node) return true;
        if(!node.visibleClusterAt(sapphirePos)) return true;
        return node.getDepositingPosAt(peridotPos).map(Optional::isEmpty).orElse(true);
    }

    private void updateEntityPos(Entity entity) {
        Optional<BlockPos> maybePos = getLeapPos();
        if(maybePos.isEmpty()) return;
        BlockPos pos = maybePos.get();
        entity.setPos(pos.getX()+0.5, pos.getY(), pos.getZ()+0.5);
    }


    public Optional<BlockPos> getLeapPos() {
        Optional<Optional<BlockPos>> pos = node.getDepositingPosAt(peridotPos);
        return pos.orElseGet(Optional::empty);
    }

    public void cleanup(ServerLevel level) {
        if(ghostUUID.get() == null) return;
        Entity entity = level.getEntity(ghostUUID.get());
        if(entity == null) return;
       entity.remove(Entity.RemovalReason.DISCARDED);
    }
}
