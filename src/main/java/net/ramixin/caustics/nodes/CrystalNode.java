package net.ramixin.caustics.nodes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.ramixin.caustics.items.components.NetworkFrequency;
import net.ramixin.caustics.nodes.steppers.NodeBuilder;
import net.ramixin.caustics.nodes.steppers.VisibilityChecker;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Function;

public record CrystalNode(NodeData data, Map<BlockPos, NetworkFrequency> networks, NodeBuilder builder, HashMap<BlockPos, VisibilityChecker> checkers) {

    public static final Codec<CrystalNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NodeData.CODEC.fieldOf("data").forGetter(CrystalNode::data),
            Codec.unboundedMap(Codec.STRING, BlockPos.CODEC).xmap(map -> invertMap(map, NetworkFrequency::new, Function.identity()), map -> invertMap(map, Function.identity(), NetworkFrequency::idAsString)).fieldOf("networks").forGetter(CrystalNode::networks)
    ).apply(instance, CrystalNode::new));

    public CrystalNode(NodeData data, Map<BlockPos, NetworkFrequency> networks) {
        HashMap<BlockPos, VisibilityChecker> deducers = new HashMap<>();
        for(BlockPos pos : data.sapphireClusters()) {
            deducers.put(pos, new VisibilityChecker(pos));
        }

        this(data, networks, new NodeBuilder(data.sapphireClusters()), deducers);
    }

    public CrystalNode(NodeData data, Map<BlockPos, NetworkFrequency> networks, NodeBuilder builder, HashMap<BlockPos, VisibilityChecker> checkers) {
        this.data = data;
        this.builder = builder;
        HashMap<BlockPos, VisibilityChecker> newCheckers = new HashMap<>();
        for(BlockPos pos : data.sapphireClusters()) {
            if(checkers.containsKey(pos)) {
                newCheckers.put(pos, checkers.get(pos));
            } else
                newCheckers.put(pos, new VisibilityChecker(pos));
        }
        List<BlockPos> sunstoneClusters = data.sunstoneClusters();
        for(BlockPos pos : Set.copyOf(networks.keySet())) {
            if(!sunstoneClusters.contains(pos))
                newCheckers.remove(pos);
        }

        this.checkers = newCheckers;
        this.networks = new HashMap<>(networks);
    }

    public void tick(ServerLevel level) {
        builder.tick(level);
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

    @Override
    public @NonNull String toString() {
        return data.sapphireClusters() + " freqs: " + networks;
    }

    private static <K1, V1, K2, V2> Map<V2, K2> invertMap(Map<K1, V1> map, Function<K1, K2> keyMapper, Function<V1, V2> valueMapper) {
        Map<V2, K2> inverted = new HashMap<>();
        for(Map.Entry<K1, V1> entry : map.entrySet()) {
            inverted.put(valueMapper.apply(entry.getValue()), keyMapper.apply(entry.getKey()));
        }
        return inverted;
    }

}
