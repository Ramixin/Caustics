package net.ramixin.caustics.nodes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.ramixin.caustics.CodecUtils;
import net.ramixin.caustics.items.components.NetworkFrequency;
import net.ramixin.caustics.nodes.steppers.DepositChecker;
import net.ramixin.caustics.nodes.steppers.VisibilityChecker;
import org.jspecify.annotations.NonNull;

import java.util.*;

public final class CrystalNode {

    public static final Codec<CrystalNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NodeData.CODEC.fieldOf("data").forGetter(CrystalNode::data),
            Codec.unboundedMap(CodecUtils.STRINGABLE_BLOCK_POS_CODEC, NetworkFrequency.CODEC).fieldOf("networks").forGetter(CrystalNode::networks),
            Codec.unboundedMap(CodecUtils.STRINGABLE_BLOCK_POS_CODEC, Codec.STRING).fieldOf("clusterNames").forGetter(CrystalNode::clusterNames)
    ).apply(instance, CrystalNode::new));

    private final NodeData data;
    private final Map<BlockPos, NetworkFrequency> networks;
    private final Map<BlockPos, String> clusterNames;
    private final Map<BlockPos, VisibilityChecker> visibilityCheckers;
    private final Map<BlockPos, DepositChecker> depositCheckers;
    private boolean syncingDirty = false;
    private int delay = 0;

    private CrystalNode(NodeData data, Map<BlockPos, NetworkFrequency> networks, Map<BlockPos, String> clusterNames) {
        this(data, networks, Map.of(), Map.of(), clusterNames);
    }

    public CrystalNode(NodeData data) {
        this.data = data;
        this.visibilityCheckers = new HashMap<>();
        addCheckersForEach(data.sapphireClusters(), Map.of());
        addCheckersForEach(data.topazClusters(), Map.of());
        addCheckersForEach(data.tourmalineClusters(), Map.of());

        this.depositCheckers = new HashMap<>();
        for(BlockPos pos : data.peridotClusters())
            depositCheckers.put(pos, new DepositChecker(pos));

        this.clusterNames = new HashMap<>();
        this.networks = new HashMap<>();
    }

    public CrystalNode(NodeData data, Map<BlockPos, NetworkFrequency> networks, Map<BlockPos, VisibilityChecker> visibilityCheckers, Map<BlockPos, DepositChecker> depositCheckers, Map<BlockPos, String> clusterNames) {
        this.data = data;

        this.visibilityCheckers = new HashMap<>();
        addCheckersForEach(data.sapphireClusters(), visibilityCheckers);
        addCheckersForEach(data.topazClusters(), visibilityCheckers);
        addCheckersForEach(data.tourmalineClusters(), visibilityCheckers);

        this.depositCheckers = new HashMap<>();
        for(BlockPos pos : data.peridotClusters())
            if(depositCheckers.containsKey(pos))
                this.depositCheckers.put(pos, depositCheckers.get(pos));
            else
                this.depositCheckers.put(pos, new DepositChecker(pos));

        this.networks = new HashMap<>();

        Set<BlockPos> sunstoneClusters = data.sunstoneClusters();
        for(BlockPos pos : sunstoneClusters)
            if(networks.containsKey(pos))
                this.networks.put(pos, networks.get(pos));

        this.clusterNames = new HashMap<>();
        Set<BlockPos> peridotClusters = data.peridotClusters();
        for(BlockPos pos : peridotClusters)
            if(clusterNames.containsKey(pos))
                this.clusterNames.put(pos, clusterNames.get(pos));
            else
                this.clusterNames.put(pos, UUID.randomUUID().toString());
    }

    private void addCheckersForEach(Set<BlockPos> set, Map<BlockPos, VisibilityChecker> oldCheckers) {
        for(BlockPos pos : set)
            if(!oldCheckers.containsKey(pos))
                visibilityCheckers.put(pos, new VisibilityChecker(pos));
            else
                visibilityCheckers.put(pos, oldCheckers.get(pos));
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

        for(VisibilityChecker checker : visibilityCheckers.values()) {
            checker.tick(level);
            if(checker.consumeSyncingDirty())
                syncingDirty = true;
        }
        for(DepositChecker checker : depositCheckers.values()) {
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
        if(!visibilityCheckers.containsKey(pos)) return false;
        return visibilityCheckers.get(pos).getValue();
    }

    public Optional<Optional<BlockPos>> getDepositingPosAt(BlockPos pos) {
        if(!data.peridotClusters().contains(pos)) return Optional.empty();
        if(!depositCheckers.containsKey(pos)) return Optional.empty();
        return Optional.of(depositCheckers.get(pos).getValue());
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
        return new CrystalNode(newData, this.networks, this.visibilityCheckers, this.depositCheckers, this.clusterNames);
    }

    public Optional<NodeSyncData> createSyncData() {
        List<BlockPos> sapphires = new ArrayList<>();
        for(BlockPos pos : data.sapphireClusters())
            if(visibleClusterAt(pos)) sapphires.add(pos);

        List<BlockPos> topazes = new ArrayList<>();
        for(BlockPos pos : data.topazClusters())
            if(visibleClusterAt(pos)) topazes.add(pos);
        if(sapphires.isEmpty() && topazes.isEmpty()) return Optional.empty();
        return Optional.of(new NodeSyncData(sapphires, topazes, networks.values().stream().toList(), clusterNames));
    }

    @Override
    public @NonNull String toString() {
        return String.format("CrystalNode Report:\n - Sapphire Clusters: %s\n - Networks: %s\n - Names: %s\n - Tourmaline Clusters: %s\n - Topaz Clusters: %s", data.sapphireClusters(), networks, clusterNames, data.tourmalineClusters(), data.topazClusters());
    }

    public NodeData data() {
        return data;
    }

    private Map<BlockPos, NetworkFrequency> networks() {
        return networks;
    }

    private Map<BlockPos, String> clusterNames() {
        return clusterNames;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if(!(obj instanceof CrystalNode that)) return false;
        return Objects.equals(this.data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }


}
