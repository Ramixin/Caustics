package net.ramixin.caustics.client.rendering;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.client.nodes.ClientNode;
import net.ramixin.caustics.items.components.Frequency;
import net.ramixin.caustics.nodes.routing.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface RenderUtil {

    static List<Component> extractRoute(Route route) {
        List<Component> routeStrings = new ArrayList<>();
        List<BlockPos> path = route.immutablePath();
        for(BlockPos pos : path) {
            routeStrings.add(extractNodeName(pos));
        }
        return routeStrings;
    }

    static Component extractNodeName(BlockPos pos) {
        Optional<Frequency> maybeFreq = ClientCrystalNetwork.getInstance().frequencyRegistry().getFrequencyAt(pos);
        if(maybeFreq.isEmpty()) return Component.translatable("caustics.node.unknown_travel");
        Optional<String> maybeNodeName = ClientCrystalNetwork.getInstance().frequencyRegistry().getFrequencyName(maybeFreq.get());
        return maybeNodeName.map(Component::literal).orElseGet(() -> Component.translatable("caustics.node.unnamed_travel"));
    }

    static Component extractDepositName(ClientNode node, int scrollPos) {
        if(scrollPos >= node.peridotPositions().size() || scrollPos < 0) return Component.translatable("caustics.node.scroll_oob");
        BlockPos pos = node.peridotPositions().get(scrollPos);
        Optional<Frequency> maybeFreq = ClientCrystalNetwork.getInstance().frequencyRegistry().getFrequencyAt(pos);
        if(maybeFreq.isEmpty()) return Component.translatable("caustics.node.unknown_deposit");
        Optional<String> maybeDepositName = ClientCrystalNetwork.getInstance().frequencyRegistry().getFrequencyName(maybeFreq.get());
        return maybeDepositName.map(Component::literal).orElseGet(() -> Component.translatable("caustics.node.unnamed_deposit"));
    }

}
