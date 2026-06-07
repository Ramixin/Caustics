package net.ramixin.caustics.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resources.Identifier;
import net.ramixin.caustics.Caustics;

public class CausticsClient implements ClientModInitializer {

    public static final Identifier ALIDADE_GUI_TEXTURE = Caustics.id("textures/misc/alidade_scope.png");

    @Override
    public void onInitializeClient() {
    }
}
