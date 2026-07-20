package net.ramixin.caustics.client.nodes;

import net.minecraft.core.BlockPos;
import net.ramixin.caustics.client.nodes.cache.AbstractIconCache;
import net.ramixin.caustics.client.nodes.icons.AlidadeIcon;
import net.ramixin.caustics.client.nodes.icons.CollimatorIcon;
import net.ramixin.caustics.client.nodes.icons.DowserIcon;
import net.ramixin.caustics.client.nodes.icons.NodeIcon;
import net.ramixin.caustics.nodes.NodeSyncData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ClientNode {

    private final List<AlidadeIcon> sapphires;
    private final List<BlockPos> topazes;
    private final List<BlockPos> peridots;
    private final List<CollimatorIcon> sunstones;
    private final List<DowserIcon> tourmaline;
    private final boolean visible;

    public ClientNode(NodeSyncData syncData, IconCaches caches) {
        this.sapphires = updateIcons(caches.alidade(), syncData.sapphirePositions());
        this.topazes = syncData.topazPositions();
        this.peridots = syncData.peridotPositions();
        this.sunstones = updateIcons(caches.collimator(), syncData.sunstonePositions(), syncData.sapphirePositions(), syncData.topazPositions(), syncData.peridotPositions());
        this.tourmaline = updateIcons(caches.dowserCache(), syncData.tourmalinePositions());
        this.visible = syncData.visible();
    }

    @SafeVarargs
    private <T extends NodeIcon> List<T> updateIcons(AbstractIconCache<T> cache, List<BlockPos>... lists) {
        List<T> icons = new ArrayList<>();
        for(List<BlockPos> list : lists) {
            for(BlockPos pos : list) {
                T cached = cache.get(pos);
                cache.associateNode(pos, this);
                icons.add(cached);
            }
        }
        return icons;
    }

    public List<BlockPos> peridot() {
        return peridots;
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
