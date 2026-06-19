package net.ramixin.caustics.nodes.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;

public class Route {
    
    public static final StreamCodec<FriendlyByteBuf, Route> STREAM_CODEC = StreamCodec.ofMember(Route::write, Route::new);

    private final List<BlockPos> path;

    private Route(FriendlyByteBuf friendlyByteBuf) {
        this(new ArrayList<>(friendlyByteBuf.readList(BlockPos.STREAM_CODEC)));
    }

    protected Route(BlockPos pos, Map<BlockPos, BlockPos> searchTree) {
        List<BlockPos> path = new ArrayList<>();
        path.add(pos);
        BlockPos last = searchTree.get(pos);
        while(last != null) {
            path.add(last);
            last = searchTree.get(last);
        }
        Collections.reverse(path);
        this.path = path;
    }

    public Route(List<BlockPos> path) {
        this.path = path;
    }

    public Route() {
        this(new ArrayList<>());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeCollection(path, BlockPos.STREAM_CODEC);
    }

    public List<BlockPos> immutablePath() {
        return List.copyOf(path);
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
