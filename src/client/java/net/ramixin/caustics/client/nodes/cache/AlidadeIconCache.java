package net.ramixin.caustics.client.nodes.cache;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.nodes.icons.AlidadeIcon;
import net.ramixin.caustics.nodes.routing.Route;

public class AlidadeIconCache extends AbstractIconCache<AlidadeIcon> {

    private BlockPos[] positions;
    private Route[] routes;

    public AlidadeIconCache() {
        super(AlidadeIcon[]::new, AlidadeIcon::new);
    }

    @Override
    public BlockPos[] getPositions() {
        if(positions != null) return positions;
        Pair<BlockPos[], Route[]> positionsAndRoutes = ClientCrystalNetwork.getInstance().getTargetablePositions();
        positions = positionsAndRoutes.getFirst();
        routes = positionsAndRoutes.getSecond();
        return positions;
    }

    public Route[] getRoutes() {
        if(routes != null) return routes;
        Pair<BlockPos[], Route[]> positionsAndRoutes = ClientCrystalNetwork.getInstance().getTargetablePositions();
        positions = positionsAndRoutes.getFirst();
        routes = positionsAndRoutes.getSecond();
        return routes;
    }

    @Override
    public void wipe() {
        super.wipe();
        positions = null;
        routes = null;
    }
}
