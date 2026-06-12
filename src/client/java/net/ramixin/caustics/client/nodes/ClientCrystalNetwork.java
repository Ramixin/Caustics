package net.ramixin.caustics.client.nodes;

import net.minecraft.core.BlockPos;
import net.ramixin.caustics.nodes.NodeSyncData;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ClientCrystalNetwork {

    private static final List<ClientCrystalNode> nodes = new ArrayList<>();
    private static final HashMap<BlockPos, ClientCrystalNode> sapphireToNode = new HashMap<>();
    private static final HashMap<BlockPos, ClientCrystalNode> topazToNode = new HashMap<>();

    private static final MutableInt scrollPos = new MutableInt();
    private static final Mutable<BlockPos> lastLookingAt = new MutableObject<>();

    public static void onSync(List<NodeSyncData> syncData) {
        nodes.clear();
        sapphireToNode.clear();
        topazToNode.clear();

        for(NodeSyncData data : syncData) {
            nodes.add(ClientCrystalNode.fromSyncData(data));
            for(BlockPos pos : data.sapphirePositions()) sapphireToNode.put(pos, ClientCrystalNode.fromSyncData(data));
            for(BlockPos pos : data.topazPositions()) topazToNode.put(pos, ClientCrystalNode.fromSyncData(data));
        }
    }

    public static Optional<ClientCrystalNode> getTargetableNodeAt(BlockPos pos) {
        return Optional.ofNullable(sapphireToNode.get(pos));
    }

    public static Optional<ClientCrystalNode> getRouterNodeAt(BlockPos pos) {
        return Optional.ofNullable(topazToNode.get(pos));
    }

    public static BlockPos[] getTargetablePositions() {
        return sapphireToNode.keySet().toArray(BlockPos[]::new);
    }

    public static void clearScrollPos() {
        scrollPos.setValue(0);
    }

    public static void deltaScrollPos(double dy) {
        if(dy < 0) {
            if(scrollPos.intValue() > 0) scrollPos.decrement();
        } else {
            BlockPos lookingAt = lastLookingAt.get();
            if(lookingAt == null) return;
            ClientCrystalNode node = sapphireToNode.get(lookingAt);
            if(scrollPos.intValue() < node.getDepositNames().size()-1) scrollPos.increment();
        }

    }

    public static int getScrollPos() {
        return scrollPos.intValue();
    }

    public static void setLastLookingAt(BlockPos pos) {
        if(!pos.equals(lastLookingAt.get())) clearScrollPos();
        lastLookingAt.setValue(pos);
    }

}
