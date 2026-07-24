package net.ramixin.caustics.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.ramixin.caustics.Caustics;
import org.lwjgl.glfw.GLFW;

public class ModKeys {

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Caustics.id("caustics_keys")
    );

    public static final KeyMapping renameFreq = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.caustics.rename_node",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_C,
                    CATEGORY
            ));

    public static void onInitialize() {

    }

}
