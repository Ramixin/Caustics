package net.ramixin.caustics.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class CausticsDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(CausticsModelProvider::new);
        pack.addProvider(CausticsLangProvider::new);
        pack.addProvider(CausticsLootProvider::new);
        pack.addProvider(CausticsRecipeProvider::new);
    }
}
