package net.ramixin.caustics.nodes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.ramixin.caustics.blocks.ChargeClusterBlock;
import net.ramixin.caustics.blocks.ModBlocks;
import net.ramixin.caustics.nodes.core.Tracker;
import net.ramixin.caustics.nodes.steppers.DepositChecker;
import net.ramixin.caustics.nodes.steppers.VisibilityChecker;
import org.jspecify.annotations.NonNull;

import java.util.*;

public final class Node {

    public static final Codec<Node> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NodeData.CODEC.fieldOf("data").forGetter(Node::data)
    ).apply(instance, Node::new));

    private final NodeData data;
    private final Map<BlockPos, VisibilityChecker> visibilityCheckers;
    private final Map<BlockPos, DepositChecker> depositCheckers;
    private int delay = 0;
    private final ChargeHolder holder = new ChargeHolder();
    private boolean cachedVisible = false;

    public Node(NodeData data) {
        this.data = data;
        this.visibilityCheckers = new HashMap<>();
        addVisibilityCheckersForEach(data.sapphireClusters(), Map.of());
        addVisibilityCheckersForEach(data.topazClusters(), Map.of());
        addVisibilityCheckersForEach(data.tourmalineClusters(), Map.of());

        this.depositCheckers = new HashMap<>();
        for(BlockPos pos : data.peridotClusters())
            depositCheckers.put(pos, new DepositChecker(pos));
    }

    public Node(NodeData data, Map<BlockPos, VisibilityChecker> visibilityCheckers, Map<BlockPos, DepositChecker> depositCheckers) {
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

        for(VisibilityChecker checker : visibilityCheckers.values())
            checker.tick(level, tracker);

        for(DepositChecker checker : depositCheckers.values())
            checker.tick(level, tracker);

        holder.tick(level, tracker);

        boolean newVisible = visibleAtNight();
        if(newVisible != cachedVisible) {
            tracker.push(Tracker.Task.NODE_SYNC);
        }
        cachedVisible = newVisible;
    }

    private boolean isLoaded(ServerLevel level) {
        for(BlockPos pos : data.sapphireClusters()) {
            if(level.isLoaded(pos))
                return true;
        }
        for(BlockPos pos : data.topazClusters()) {
            if(level.isLoaded(pos))
                return true;
        }
        for(BlockPos pos : data.tourmalineClusters()) {
            if(level.isLoaded(pos))
                return true;
        }
        return false;
    }

    public boolean visibleClusterAt(BlockPos pos) {
        if(!visibilityCheckers.containsKey(pos)) return false;
        return visibilityCheckers.get(pos).getValue();
    }

    private boolean visibleAtNight() {
        int lowestLight = Integer.MAX_VALUE;
        for(BlockPos pos : data.sapphireClusters()) {
            lowestLight = Math.min(lowestLight, visibilityCheckers.get(pos).skyBrightness());
        }
        for(BlockPos pos : data.topazClusters()) {
            lowestLight = Math.min(lowestLight, visibilityCheckers.get(pos).skyBrightness());
        }
        for(BlockPos pos : data.tourmalineClusters()) {
            lowestLight = Math.min(lowestLight, visibilityCheckers.get(pos).skyBrightness());
        }
        if(lowestLight == Integer.MAX_VALUE) return false;
        if(lowestLight > 10) return true;
        return holder.hasCharge();
    }

    public Optional<Optional<BlockPos>> getDepositingPosAt(BlockPos pos) {
        if(!data.peridotClusters().contains(pos)) return Optional.empty();
        if(!depositCheckers.containsKey(pos)) return Optional.empty();
        return Optional.of(depositCheckers.get(pos).getValue());
    }

    public Node withData(NodeData newData) {
        return new Node(newData, this.visibilityCheckers, this.depositCheckers);
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
        return Optional.of(new NodeSyncData(sapphires, topazes, peridots, List.copyOf(data.sunstoneClusters()), tourmalines, cachedVisible));
    }

    @Override
    public @NonNull String toString() {
        return String.format("CrystalNode Report:\n - Sapphire Clusters: %s\n - Tourmaline Clusters: %s\n - Topaz Clusters: %s", data.sapphireClusters(), data.tourmalineClusters(), data.topazClusters());
    }

    public NodeData data() {
        return data;
    }

    public Optional<Integer> getLightLevelAt(BlockPos pos) {
        VisibilityChecker checker = visibilityCheckers.get(pos);
        if(checker == null) return Optional.empty();
        return Optional.of(checker.skyBrightness());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(!(obj instanceof Node that)) return false;
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

    private class ChargeHolder {

        private final Set<BlockPos> chargedPositions = new HashSet<>();

        protected ChargeHolder() {}

        protected void tick(ServerLevel level, Tracker tracker) {
            Set<BlockPos> temp = new HashSet<>();
            for(BlockPos pos : data.seleniteClusters()) {
                BlockState state = level.getBlockState(pos);
                if(!state.is(ModBlocks.SELENITE_GROUP.cluster())) continue;
                if(state.getValue(ChargeClusterBlock.CHARGED)) temp.add(pos);
            }
            if(temp.equals(chargedPositions)) return;
            tracker.push(Tracker.Task.NODE_SYNC);
            chargedPositions.clear();
            chargedPositions.addAll(temp);
        }

        protected boolean hasCharge() {
            return !chargedPositions.isEmpty();
        }

        protected Optional<BlockPos> popCharge() {
            if(chargedPositions.isEmpty()) return Optional.empty();
            BlockPos next = chargedPositions.iterator().next();
            chargedPositions.remove(next);
            return Optional.of(next);
        }

    }
}
