package net.ramixin.caustics.nodes.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.items.components.Frequency;
import net.ramixin.caustics.nodes.Network;
import net.ramixin.caustics.nodes.Node;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CrystalNetwork extends SavedData implements Network {

    private static final Codec<CrystalNetwork> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NodeWorker.CODEC.fieldOf("worker").forGetter(CrystalNetwork::nodeWorker),
            FrequencyRegistry.CODEC.fieldOf("registry").forGetter(CrystalNetwork::frequencyRegistry)
    ).apply(instance, CrystalNetwork::new));

    @SuppressWarnings("DataFlowIssue") // doesn't want null datafixer
    private static final SavedDataType<CrystalNetwork> TYPE = new SavedDataType<>(
            Caustics.id("crystal_network"),
            CrystalNetwork::new,
            CODEC,
            null
    );

    private final NodeIndex index;
    private final NodeWorker worker;
    private final FrequencyRegistry registry;
    private final RoutingManager manager = new RoutingManager();
    private final NetworkSynchronizer synchronizer = new NetworkSynchronizer();
    private final LeaptionHandler handler = new LeaptionHandler();

    private final Tracker tracker = new Tracker();

    private CrystalNetwork() {
        this.worker = new NodeWorker();
        this.index = new NodeIndex();
        this.registry = new FrequencyRegistry();
    }

    private CrystalNetwork(NodeWorker worker, FrequencyRegistry registry) {
        this.worker = worker;
        this.index = new NodeIndex(worker);
        this.registry = registry;
        tracker.lazyPush(Tracker.Task.REBUILD_ROUTING);
    }

    public static CrystalNetwork get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void tick(ServerLevel level) {
        tracker.begin();

        worker.tick(level, this);
        registry.tick(this);
        manager.tick(level, this);
        handler.tick(level, this);
        synchronizer.tick(level, this);

        if(tracker.consume(Tracker.Task.DIRTY))
            setDirty();
    }

    public NodeIndex nodeIndex() {
        return index;
    }

    public NodeWorker nodeWorker() {
        return worker;
    }

    public FrequencyRegistry frequencyRegistry() {
        return registry;
    }

    public RoutingManager routingManager() {
        return manager;
    }

    public NetworkSynchronizer synchronizer() {
        return synchronizer;
    }

    public LeaptionHandler leaptionHandler() {
        return handler;
    }

    protected Tracker getTracker() {
        return tracker;
    }

    public void generateNodeAt(BlockPos pos) {
        this.worker.generateNodeAt(pos);
        setDirty();
    }

    public Set<Frequency> getNetworks(BlockPos pos) {
        Optional<Node> maybeNode = index.getNodeAt(pos);
        if(maybeNode.isEmpty()) return Set.of();
        Node node = maybeNode.get();
        Set<BlockPos> sunstones = node.data().sunstoneClusters();

        Set<Frequency> networks = new HashSet<>();
        for(BlockPos sunstone : sunstones) {
            Optional<Frequency> maybeFreq = registry.getFrequencyAt(sunstone);
            if(maybeFreq.isEmpty()) continue;
            networks.add(maybeFreq.get());
        }
        return networks;
    }

    public void nuke() {
        index.clear();
        worker.clear();
        registry.clear();
        manager.clear();
        handler.clear();
        setDirty();
    }
}