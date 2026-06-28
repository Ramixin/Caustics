package net.ramixin.caustics.nodes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.ramixin.caustics.nodes.core.Tracker;
import net.ramixin.caustics.nodes.steppers.DepositChecker;
import net.ramixin.caustics.nodes.steppers.VisibilityChecker;
import org.jspecify.annotations.NonNull;

import java.util.*;

public final class CrystalNode {

    public static final Codec<CrystalNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NodeData.CODEC.fieldOf("data").forGetter(CrystalNode::data)
    ).apply(instance, CrystalNode::new));

    private final NodeData data;
    private final Map<BlockPos, VisibilityChecker> visibilityCheckers;
    private final Map<BlockPos, DepositChecker> depositCheckers;
    private int delay = 0;

    public CrystalNode(NodeData data) {
        this.data = data;
        this.visibilityCheckers = new HashMap<>();
        addVisibilityCheckersForEach(data.sapphireClusters(), Map.of());
        addVisibilityCheckersForEach(data.topazClusters(), Map.of());
        addVisibilityCheckersForEach(data.tourmalineClusters(), Map.of());

        this.depositCheckers = new HashMap<>();
        for(BlockPos pos : data.peridotClusters())
            depositCheckers.put(pos, new DepositChecker(pos));
    }

    public CrystalNode(NodeData data, Map<BlockPos, VisibilityChecker> visibilityCheckers, Map<BlockPos, DepositChecker> depositCheckers) {
        this.data = data;

        this.visibilityCheckers = new HashMap<>();
        addVisibilityCheckersForEach(data.sapphireClusters(), visibilityCheckers);
        addVisibilityCheckersForEach(data.topazClusters(), visibilityCheckers);
        addVisibilityCheckersForEach(data.tourmalineClusters(), visibilityCheckers);

        this.depositCheckers = new HashMap<>();
        for(BlockPos pos : data.peridotClusters())
            if(depositCheckers.containsKey(pos))
                this.depositCheckers.put(pos, depositCheckers.get(pos));
            else
                this.depositCheckers.put(pos, new DepositChecker(pos));
    }

    private void addVisibilityCheckersForEach(Set<BlockPos> set, Map<BlockPos, VisibilityChecker> oldCheckers) {
        for(BlockPos pos : set)
            if(!oldCheckers.containsKey(pos))
                visibilityCheckers.put(pos, new VisibilityChecker(pos));
            else
                visibilityCheckers.put(pos, oldCheckers.get(pos));
    }

    public void tick(ServerLevel level, Tracker tracker) {
        if(delay > 0) {
            delay--;
            return;
        }

        if(!isLoaded(level)) {
            delay = 100;
            return;
        }

        for(VisibilityChecker checker : visibilityCheckers.values()) {
            checker.tick(level, tracker);
        }
        for(DepositChecker checker : depositCheckers.values()) {
            checker.tick(level, tracker);
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

    public CrystalNode withData(NodeData newData) {
        return new CrystalNode(newData, this.visibilityCheckers, this.depositCheckers);
    }

    public Optional<NodeSyncData> createSyncData() {
        List<BlockPos> sapphires = new ArrayList<>();
        for(BlockPos pos : data.sapphireClusters())
            if(visibleClusterAt(pos)) sapphires.add(pos);

        List<BlockPos> topazes = new ArrayList<>();
        for(BlockPos pos : data.topazClusters())
            if(visibleClusterAt(pos)) topazes.add(pos);

        List<BlockPos> tourmalines = new ArrayList<>();
        for(BlockPos pos : data.tourmalineClusters())
            if(visibleClusterAt(pos)) tourmalines.add(pos);


        if(sapphires.isEmpty() && topazes.isEmpty() && tourmalines.isEmpty()) return Optional.empty();
        List<BlockPos> peridots = new ArrayList<>();
        for(BlockPos pos : data.peridotClusters()) {
            if(getDepositingPosAt(pos).map(Optional::isPresent).orElse(false)) peridots.add(pos);
        }
        peridots.sort(BlockPos::compareTo);
        return Optional.of(new NodeSyncData(sapphires, topazes, peridots, List.copyOf(data.sunstoneClusters())));
    }

    @Override
    public @NonNull String toString() {
        return String.format("CrystalNode Report:\n - Sapphire Clusters: %s\n - Tourmaline Clusters: %s\n - Topaz Clusters: %s", data.sapphireClusters(), data.tourmalineClusters(), data.topazClusters());
    }

    public NodeData data() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(!(obj instanceof CrystalNode that)) return false;
        return Objects.equals(this.data, that.data);
    }

    public void prioritizeUpdates(BlockPos sapphirePos, BlockPos peridotPos) {
        VisibilityChecker checker = visibilityCheckers.get(sapphirePos);
        if(checker != null) {
            checker.setPauseTicks(0);
        }
        DepositChecker depositChecker = depositCheckers.get(peridotPos);
        if(depositChecker != null) {
            depositChecker.setPauseTicks(0);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
