package net.ramixin.caustics.client.screens;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.ramixin.caustics.menus.TuningForkMenu;
import net.ramixin.caustics.networking.SetFrequencyPayload;
import org.jspecify.annotations.NonNull;

public class TuningForkScreen extends AbstractContainerScreen<TuningForkMenu> {

    private final EditBox networkName;
    private final EditBox nodeName;

    public TuningForkScreen(TuningForkMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        networkName = new EditBox(font, 10, 10, 100, 10, Component.empty());
        nodeName = new EditBox(font, 10, 20, 100, 10, Component.empty());

        addRenderableWidget(networkName);
        addRenderableWidget(nodeName);
    }

    @Override
    public void onClose() {
        String networkName = this.networkName.getValue();
        String nodeName = this.nodeName.getValue();
        if(!networkName.isEmpty() && !nodeName.isEmpty())
            ClientPlayNetworking.send(new SetFrequencyPayload(networkName, nodeName));
        super.onClose();
    }

    @Override
    public boolean keyPressed(final @NonNull KeyEvent event) {
        if(this.minecraft.player == null) return false;
        if (event.isEscape()) {
            onClose();
            return true;
        } else {
            return this.networkName.keyPressed(event)
                    || this.networkName.canConsumeInput()
                    || this.nodeName.keyPressed(event)
                    || this.nodeName.canConsumeInput()
                    || super.keyPressed(event);
        }
    }

    public void setFrequency(String network, String node) {
        this.networkName.setValue(network);
        this.nodeName.setValue(node);
    }
}
