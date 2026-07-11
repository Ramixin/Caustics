package net.ramixin.caustics.client.rendering.node;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.ramixin.caustics.client.CausticsClient;
import net.ramixin.caustics.utils.LookUtil;

import java.util.HashMap;
import java.util.Optional;

public class IconHolder {

    private final HashMap<BlockPos, NodeIcon> icons = new HashMap<>();

    public void tick() {
        double[] angles = CausticsClient.LOOK_MANAGER.getAngles();
        Optional<Integer> closest = LookUtil.calculateClosestLooking(angles);
        NodeIcon[] icons = CausticsClient.LOOK_MANAGER.getIcons();
        int closestIndex = closest.orElse(-1);
        for(int i = 0; i < icons.length; i++) {
            NodeIcon icon = icons[i];
            icon.tick(i == closestIndex);
        }
    }

    public void clear() {
        icons.clear();
    }

    public void add(BlockPos pos, RandomSource random) {
        if(icons.containsKey(pos)) return;
        icons.put(pos, new NodeIcon(random));
    }

    public boolean has(BlockPos pos) {
        return icons.containsKey(pos);
    }

    public Optional<NodeIcon> get(BlockPos pos) {
        return Optional.ofNullable(icons.get(pos));
    }

}
