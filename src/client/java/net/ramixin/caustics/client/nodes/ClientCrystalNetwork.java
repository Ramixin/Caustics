package net.ramixin.caustics.client.nodes;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.ramixin.caustics.client.CausticsClient;
import net.ramixin.caustics.items.components.Frequency;
import net.ramixin.caustics.nodes.NodeSyncData;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;

public class ClientCrystalNetwork {

    private static final List<ClientCrystalNode> nodes = new ArrayList<>();
    private static final HashMap<BlockPos, ClientCrystalNode> sapphireToNode = new HashMap<>();

    private static final MutableInt scrollPos = new MutableInt();
    private static final Mutable<BlockPos> lastLookingAt = new MutableObject<>();

    private static final Map<BlockPos, Frequency> frequencies = new HashMap<>();

    public static void onSync(List<NodeSyncData> syncData, Map<BlockPos, Frequency> freqs) {
        nodes.clear();
        sapphireToNode.clear();
        frequencies.clear();

        for(NodeSyncData data : syncData) {
            nodes.add(ClientCrystalNode.fromSyncData(data));
            for(BlockPos pos : data.sapphirePositions()) sapphireToNode.put(pos, ClientCrystalNode.fromSyncData(data));
        }
        frequencies.putAll(freqs);
    }

    public static Optional<ClientCrystalNode> getTargetableNodeAt(BlockPos pos) {
        return Optional.ofNullable(sapphireToNode.get(pos));
    }

    public static List<Frequency> getNodeFrequencies(ClientCrystalNode node) {
        List<Frequency> freqs = new ArrayList<>();
        for(BlockPos pos : node.sunstonePositions()) {
            Frequency freq = frequencies.get(pos);
            if(freq != null) freqs.add(freq);
        }
        return freqs;
    }

    public static BlockPos[] getTargetablePositions() {
        List<BlockPos> visiblePositions = new ArrayList<>();
        for(BlockPos pos : sapphireToNode.keySet()) {
            if(Minecraft.getInstance().player == null) throw new IllegalStateException("Player is null");
            if(pos.distToCenterSqr(Minecraft.getInstance().player.position()) < CausticsClient.MAX_SIGNAL_RANGE)
                visiblePositions.add(pos);
        }
        return visiblePositions.toArray(BlockPos[]::new);
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
            if(scrollPos.intValue() < node.peridotPositions().size()-1) scrollPos.increment();
        }

    }

    public static int getScrollPos() {
        return scrollPos.intValue();
    }

    public static void setLastLookingAt(BlockPos pos) {
        if(!pos.equals(lastLookingAt.get())) clearScrollPos();
        lastLookingAt.setValue(pos);
    }

    public static Optional<Frequency> getFrequencyAt(BlockPos pos) {
        return Optional.ofNullable(frequencies.get(pos));
    }

}
