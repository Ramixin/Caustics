package net.ramixin.caustics.client.nodes;

import net.minecraft.core.BlockPos;
import net.ramixin.caustics.client.nodes.icons.AlidadeIcon;
import net.ramixin.caustics.client.nodes.icons.DowserIcon;
import net.ramixin.caustics.nodes.NodeSyncData;

import java.util.List;
import java.util.Objects;

public final class ClientNode {

    private final List<AlidadeIcon> sapphires;
    private final List<BlockPos> topazes;
    private final List<BlockPos> peridots;
    private final List<BlockPos> sunstones;
    private final List<DowserIcon> tourmaline;
    private final boolean visible;

    public ClientNode(NodeSyncData syncData, IconIndex index) {
        this.sapphires = syncData.sapphirePositions().stream().map(index.alidadeCache()::get).toList();
        this.topazes = syncData.topazPositions();
        this.peridots = syncData.peridotPositions();
        this.sunstones = syncData.sunstonePositions();
        this.tourmaline = syncData.tourmalinePositions().stream().map(index.dowserCache()::get).toList();
        this.visible = syncData.visible();
    }

    public List<AlidadeIcon> sapphire() {
        return sapphires;
    }

    public List<BlockPos> topaz() {
        return topazes;
    }

    public List<BlockPos> peridot() {
        return peridots;
    }

    public List<BlockPos> sunstones() {
        return sunstones;
    }

    public List<DowserIcon> tourmaline() {
        return tourmaline;
    }

    public boolean visible() {
        return visible;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sapphires, topazes, peridots, sunstones, tourmaline, visible);
    }

    @Override
    public String toString() {
        return "ClientNode[" +
                "sapphire=" + sapphires + ", " +
                "topaz=" + topazes + ", " +
                "peridot=" + peridots + ", " +
                "sunstones=" + sunstones + ", " +
                "tourmaline=" + tourmaline + ", " +
                "visible=" + visible;
    }


}
