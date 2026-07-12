package net.ramixin.caustics;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public interface ModTags {

    interface Blocks {
        TagKey<Block> CRYSTAL = TagKey.create(Registries.BLOCK, Caustics.id("crystal"));
        TagKey<Block> CLUSTER = TagKey.create(Registries.BLOCK, Caustics.id("cluster"));
        TagKey<Block> NETWORK_CLUSTER = TagKey.create(Registries.BLOCK, Caustics.id("network_cluster"));
        TagKey<Block> TUNABLE_CLUSTER = TagKey.create(Registries.BLOCK, Caustics.id("tunable_cluster"));
    }

    interface Items {
        TagKey<Item> LENS = TagKey.create(Registries.ITEM, Caustics.id("lens"));
        TagKey<Item> ALIDADE_LENS = TagKey.create(Registries.ITEM, Caustics.id("alidade_lens"));
        TagKey<Item> DOWSER_LENS = TagKey.create(Registries.ITEM, Caustics.id("dowser_lens"));
        TagKey<Item> TELESCOPE_LENS = TagKey.create(Registries.ITEM, Caustics.id("telescope_lens"));
    }
}
