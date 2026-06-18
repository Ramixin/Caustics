package net.ramixin.caustics.nodes;

import net.ramixin.caustics.items.components.Frequency;

import java.util.Optional;

public interface Network {

    Optional<String> getFrequencyName(Frequency frequency);

}
