package net.ramixin.caustics.client.rendering;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.items.components.Frequency;
import net.ramixin.caustics.nodes.core.FrequencyRegistry;
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

    static MutableComponent getFrequencyName(BlockPos pos, Component unnamedDefault) {
        return getFrequencyName(pos, unnamedDefault, true);
    }

    static MutableComponent getFrequencyName(BlockPos pos, Component unnamedDefault, boolean unknownIsUnnamed) {
        FrequencyRegistry registry = ClientCrystalNetwork.getInstance().frequencyRegistry();
        Optional<Frequency> maybeFreq = registry.getFrequencyAt(pos);
        if(maybeFreq.isEmpty()) return unknownIsUnnamed ? unnamedDefault.copy() : Component.translatable("caustics.frequency.unknown");
        Optional<String> maybeDepositName = registry.getFrequencyName(maybeFreq.get());
        return maybeDepositName.map(Component::literal).orElse(unnamedDefault.copy());
    }

}
