package net.ramixin.caustics.nodes.leaption;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.ramixin.caustics.entities.LeapGhost;
import net.ramixin.caustics.nodes.Node;
import net.ramixin.caustics.nodes.core.CrystalNetwork;
import net.ramixin.caustics.nodes.routing.NodeMappedRoute;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Optional;
import java.util.UUID;

public final class Leap {
    private final UUID playerUUID;
    private final Node node;
    private final NodeMappedRoute route;
    private final BlockPos sapphirePos;
    private final BlockPos peridotPos;
    private boolean completed = false;
    private final Mutable<UUID> ghostUUID = new MutableObject<>();

    public Leap(UUID playerUUID, Node node, NodeMappedRoute route, BlockPos sapphirePos, BlockPos peridotPos) {
        this.playerUUID = playerUUID;
        this.node = node;
        this.route = route;
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
        Optional<Node> maybeNetNode = network.nodeIndex().getNodeAt(sapphirePos);
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
        if(!completed) return;

        Player player = level.getPlayerByUUID(playerUUID);
        if(!(player instanceof ServerPlayer serverPlayer)) return;
        int lightLost = route.calculateLightLost();
        serverPlayer.hurtServer(level, level.damageSources().generic(), lightLost / 4f);
    }

    public void markCompleted() {
        completed = true;
    }
}
