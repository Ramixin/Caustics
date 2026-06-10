package net.ramixin.caustics.nodes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.ramixin.caustics.CodecUtils;
import net.ramixin.caustics.items.components.NetworkFrequency;
import net.ramixin.caustics.nodes.steppers.VisibilityChecker;
import org.jspecify.annotations.NonNull;

import java.util.*;

public record CrystalNode(NodeData data, Map<BlockPos, NetworkFrequency> networks, Map<BlockPos, VisibilityChecker> checkers) {

    public static final Codec<CrystalNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NodeData.CODEC.fieldOf("data").forGetter(CrystalNode::data),
            CodecUtils.invertMap(Codec.unboundedMap(NetworkFrequency.STRINGABLE_CODEC, BlockPos.CODEC)).fieldOf("networks").forGetter(CrystalNode::networks)
    ).apply(instance, CrystalNode::new));

    public CrystalNode(NodeData data, Map<BlockPos, NetworkFrequency> networks) {
        HashMap<BlockPos, VisibilityChecker> deducers = new HashMap<>();
        for(BlockPos pos : data.sapphireClusters()) {
            deducers.put(pos, new VisibilityChecker(pos));
        }

        this(data, networks, deducers);
    }

    public CrystalNode(NodeData data, Map<BlockPos, NetworkFrequency> networks, Map<BlockPos, VisibilityChecker> checkers) {
        this.data = data;
        HashMap<BlockPos, VisibilityChecker> newCheckers = new HashMap<>();
        for(BlockPos pos : data.sapphireClusters()) {
            if(checkers.containsKey(pos)) {
                newCheckers.put(pos, checkers.get(pos));
            } else
                newCheckers.put(pos, new VisibilityChecker(pos));
        }
        Set<BlockPos> sunstoneClusters = data.sunstoneClusters();
        for(BlockPos pos : Set.copyOf(networks.keySet())) {
            if(!sunstoneClusters.contains(pos))
                networks.remove(pos);
        }

        this.checkers = newCheckers;
        this.networks = new HashMap<>(networks);
    }

    public void tick(ServerLevel level) {
        for(VisibilityChecker deducer : checkers.values()) {
            deducer.tick(level);
        }
    }

    public boolean visibleClusterAt(BlockPos pos) {
        if(!checkers.containsKey(pos)) return false;
        return checkers.get(pos).isVisible();
    }

    public Optional<NetworkFrequency> networkFrequencyAt(BlockPos pos) {
        if(!data.sunstoneClusters().contains(pos)) return Optional.empty();
        if(!networks.containsKey(pos)) {
            networks.put(pos, new NetworkFrequency(UUID.randomUUID()));
        }
        return Optional.of(networks.get(pos));
    }

    public void setFrequency(BlockPos pos, NetworkFrequency freq) {
        if(data.sunstoneClusters().contains(pos))
            networks.put(pos, freq);
    }

    public CrystalNode withData(NodeData newData) {
        return new CrystalNode(newData.toImmutable(), this.networks, this.checkers);
    }

    public NodeSyncData createSyncData() {
        return new NodeSyncData(data.sapphireList(), networks.values().stream().toList());
    }

    @Override
    public @NonNull String toString() {
        return data.sapphireClusters() + " freqs: " + networks;
    }

}
