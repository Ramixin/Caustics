package net.ramixin.caustics.nodes.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.ramixin.caustics.CodecUtils;
import net.ramixin.caustics.items.components.Frequency;
import net.ramixin.caustics.networking.clientbound.FrequencySyncPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FrequencyRegistry {

    protected static final Codec<FrequencyRegistry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(CodecUtils.STRINGABLE_BLOCK_POS_CODEC, Frequency.CODEC).fieldOf("frequencies").forGetter(FrequencyRegistry::getFrequencies),
            Codec.unboundedMap(Frequency.STRINGABLE_CODEC, Codec.STRING).fieldOf("frequencyNames").forGetter(FrequencyRegistry::getFrequencyNames)
    ).apply(instance, FrequencyRegistry::new));

    private final Map<BlockPos, Frequency> frequencies = new HashMap<>();
    private final Map<Frequency, String> frequencyNames = new HashMap<>();
    private boolean pushTracker = false;

    private FrequencyRegistry(Map<BlockPos, Frequency> frequencies, Map<Frequency, String> frequencyNames) {
        this.frequencies.putAll(frequencies);
        this.frequencyNames.putAll(frequencyNames);
    }

    protected FrequencyRegistry() {
    }

    protected void tick(CrystalNetwork network) {
        if(pushTracker) {
            network.getTracker().push(Tracker.Item.FREQUENCY_SYNC, Tracker.Item.DIRTY);
            pushTracker = false;
        }
    }

    public void register(BlockPos pos, Frequency frequency) {
        frequencies.put(pos, frequency);
        pushTracker = true;
    }

    public void register(Frequency frequency, String name) {
        frequencyNames.put(frequency, name);
        pushTracker = true;
    }

    public Optional<Frequency> getFrequencyAt(BlockPos pos) {
        return Optional.ofNullable(frequencies.get(pos));
    }

    public Optional<String> getFrequencyName(Frequency frequency) {
        return Optional.ofNullable(frequencyNames.get(frequency));
    }

    public FrequencySyncPayload createSyncPayload() {
        return new FrequencySyncPayload(frequencies, frequencyNames);
    }

    private Map<BlockPos, Frequency> getFrequencies() {
        return frequencies;
    }

    private Map<Frequency, String> getFrequencyNames() {
        return frequencyNames;
    }

    protected void clear() {
        frequencies.clear();
        frequencyNames.clear();
    }

}
