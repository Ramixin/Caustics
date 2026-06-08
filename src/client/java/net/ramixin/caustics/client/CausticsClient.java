package net.ramixin.caustics.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.client.screens.TuningForkScreen;
import net.ramixin.caustics.menus.ModMenus;
import net.ramixin.caustics.networking.SetFrequencyPayload;

public class CausticsClient implements ClientModInitializer {

    public static final Identifier ALIDADE_GUI_TEXTURE = Caustics.id("textures/misc/alidade_scope.png");

    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenus.TUNING_FORK_MENU_TYPE, TuningForkScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(SetFrequencyPayload.PACKET_ID, CausticsClient::handleSetFrequency);
    }

    private static void handleSetFrequency(SetFrequencyPayload payload, ClientPlayNetworking.Context ctx) {
        Screen screen = Minecraft.getInstance().screen;
        if(!(screen instanceof TuningForkScreen tuningForkScreen)) return;
        tuningForkScreen.setFrequency(payload.network(), payload.node());
    }
}
