package net.ramixin.caustics.nodes.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.ramixin.caustics.nodes.Node;
import net.ramixin.caustics.nodes.core.CrystalNetwork;
import net.ramixin.caustics.nodes.core.NodeIndex;

import java.util.*;

public class Route {
    
    public static final StreamCodec<FriendlyByteBuf, Route> STREAM_CODEC = StreamCodec.ofMember(Route::write, Route::new);

    protected final List<BlockPos> path;
    protected final BlockPos sapphirePos;

    private Route(FriendlyByteBuf friendlyByteBuf) {
        this(new ArrayList<>(friendlyByteBuf.readList(BlockPos.STREAM_CODEC)), friendlyByteBuf.readBlockPos());
    }

    protected Route(BlockPos pos, Map<BlockPos, BlockPos> searchTree) {
        List<BlockPos> path = new ArrayList<>();
        BlockPos last = searchTree.get(pos);
        while(last != null) {
            path.add(last);
            last = searchTree.get(last);
        }
        Collections.reverse(path);
        this.path = path;
        this.sapphirePos = path.getLast();
    }

    public Route(List<BlockPos> path, BlockPos sapphirePos) {
        this.path = path;
        this.sapphirePos = sapphirePos;
    }

    public BlockPos sapphirePos() {
        return sapphirePos;
    }


    private void write(FriendlyByteBuf buf) {
        buf.writeCollection(path, BlockPos.STREAM_CODEC);
        buf.writeBlockPos(sapphirePos);
    }

    public List<BlockPos> immutablePath() {
        return List.copyOf(path);
    }

    public Optional<NodeMappedRoute> nodeMapped(CrystalNetwork network) {
        Optional<Node> maybeSapphire = network.getNodeAt(sapphirePos, NodeIndex.Type.SAPPHIRE);
        if(maybeSapphire.isEmpty()) return Optional.empty();
        List<Node> nodes = new ArrayList<>(path.size());
        for(BlockPos pos : path) {
            Optional<Node> maybeNode = network.getNodeAt(pos, NodeIndex.Type.TOPAZ);
            if(maybeNode.isEmpty()) return Optional.empty();
            nodes.add(maybeNode.get());
        }
        return Optional.of(new NodeMappedRoute(path, sapphirePos, nodes, maybeSapphire.get()));
    }

    public int length() {
        return path.size();
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return "Route[" +
                "path=" + path + ']';
    }


}
