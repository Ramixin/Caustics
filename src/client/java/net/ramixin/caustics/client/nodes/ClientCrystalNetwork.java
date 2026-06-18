package net.ramixin.caustics.client.nodes;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.ramixin.caustics.client.CausticsClient;
import net.ramixin.caustics.items.components.Frequency;
import net.ramixin.caustics.nodes.Network;
import net.ramixin.caustics.nodes.NodeSyncData;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;

public class ClientCrystalNetwork implements Network {

    private static final ClientCrystalNetwork INSTANCE = new ClientCrystalNetwork();

    private final List<ClientCrystalNode> nodes = new ArrayList<>();
    private final HashMap<BlockPos, ClientCrystalNode> sapphireToNode = new HashMap<>();

    private final MutableInt scrollPos = new MutableInt();
    private final Mutable<BlockPos> lastLookingAt = new MutableObject<>();

    private final Map<BlockPos, Frequency> frequencies = new HashMap<>();
    private final Map<Frequency, String> frequencyNames = new HashMap<>();

    public static ClientCrystalNetwork getInstance() {
        return INSTANCE;
    }

    public void onNodeSync(List<NodeSyncData> syncData) {
        nodes.clear();
        sapphireToNode.clear();

        for(NodeSyncData data : syncData) {
            nodes.add(ClientCrystalNode.fromSyncData(data));
            for(BlockPos pos : data.sapphirePositions()) sapphireToNode.put(pos, ClientCrystalNode.fromSyncData(data));
        }
    }

    public void onFrequencySync(Map<BlockPos, Frequency> frequencies, Map<Frequency, String> frequencyNames) {
        this.frequencies.clear();
        this.frequencyNames.clear();

        this.frequencies.putAll(frequencies);
        this.frequencyNames.putAll(frequencyNames);
    }

    public Optional<ClientCrystalNode> getTargetableNodeAt(BlockPos pos) {
        return Optional.ofNullable(sapphireToNode.get(pos));
    }

    public BlockPos[] getTargetablePositions() {
        List<BlockPos> visiblePositions = new ArrayList<>();
        for(BlockPos pos : sapphireToNode.keySet()) {
            if(Minecraft.getInstance().player == null) throw new IllegalStateException("Player is null");
            if(pos.distToCenterSqr(Minecraft.getInstance().player.position()) < CausticsClient.MAX_SIGNAL_RANGE)
                visiblePositions.add(pos);
        }
        return visiblePositions.toArray(BlockPos[]::new);
    }

    public void clearScrollPos() {
        scrollPos.setValue(0);
    }

    public void deltaScrollPos(double dy) {
        if(dy < 0) {
            if(scrollPos.intValue() > 0) scrollPos.decrement();
        } else {
            BlockPos lookingAt = lastLookingAt.get();
            if(lookingAt == null) return;
            ClientCrystalNode node = sapphireToNode.get(lookingAt);
            if(scrollPos.intValue() < node.peridotPositions().size()-1) scrollPos.increment();
        }

    }

    public int getScrollPos() {
        return scrollPos.intValue();
    }

    public void setLastLookingAt(BlockPos pos) {
        if(!pos.equals(lastLookingAt.get())) clearScrollPos();
        lastLookingAt.setValue(pos);
    }

    public Optional<Frequency> getFrequencyAt(BlockPos pos) {
        return Optional.ofNullable(frequencies.get(pos));
    }

    @Override
    public Optional<String> getFrequencyName(Frequency frequency) {
        return Optional.ofNullable(frequencyNames.get(frequency));
    }
}
