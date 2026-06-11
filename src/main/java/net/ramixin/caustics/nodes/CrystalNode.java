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

public final class CrystalNode {

    public static final Codec<CrystalNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NodeData.CODEC.fieldOf("data").forGetter(CrystalNode::data),
            Codec.unboundedMap(CodecUtils.STRINGABLE_BLOCK_POS_CODEC, NetworkFrequency.CODEC).fieldOf("networks").forGetter(CrystalNode::networks)
    ).apply(instance, CrystalNode::new));

    private final NodeData data;
    private final Map<BlockPos, NetworkFrequency> networks;
    private final Map<BlockPos, VisibilityChecker> checkers;
    private boolean syncingDirty = false;
    private final String name = "Jeff";
    private int delay = 0;


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
        for(BlockPos pos : Set.copyOf(networks.keySet()))
            if(!sunstoneClusters.contains(pos))
                networks.remove(pos);

        this.checkers = newCheckers;
        this.networks = new HashMap<>(networks);
    }

    public boolean consumeSyncingDirty() {
        boolean val = syncingDirty;
        syncingDirty = false;
        return val;
    }

    public void tick(ServerLevel level) {
        if(delay > 0) {
            delay--;
            return;
        }

        if(!isLoaded(level)) {
            delay = 100;
            return;
        }

        for(VisibilityChecker checker : checkers.values()) {
            checker.tick(level);
            if(checker.consumeSyncingDirty())
                syncingDirty = true;
        }
    }

    private boolean isLoaded(ServerLevel level) {
        for(BlockPos pos : data.sapphireClusters()) {
            if(level.isLoaded(pos))
                return true;
        }
        return false;
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

    public Optional<NodeSyncData> createSyncData() {
        List<BlockPos> sapphires = new ArrayList<>();
        for(BlockPos pos : data.sapphireClusters())
            if(visibleClusterAt(pos)) sapphires.add(pos);

        if(sapphires.isEmpty()) return Optional.empty();
        return Optional.of(new NodeSyncData(sapphires, networks.values().stream().toList(), Optional.of(name)));
    }

    @Override
    public @NonNull String toString() {
        return data.sapphireClusters() + " freqs: " + networks;
    }

    public NodeData data() {
        return data;
    }

    public Map<BlockPos, NetworkFrequency> networks() {
        return networks;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if(!(obj instanceof CrystalNode that)) return false;
        return Objects.equals(this.data, that.data) &&
                Objects.equals(this.networks, that.networks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, networks);
    }


}
