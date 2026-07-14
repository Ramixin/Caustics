package net.ramixin.caustics.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.utils.MixsonUtil;
import net.ramixin.mixson.Mixson;
import net.ramixin.mixson.enums.DebugOption;
import net.ramixin.mixson.enums.ErrorPolicy;
import net.ramixin.mixson.enums.Lifetime;
import net.ramixin.mixson.util.Index;

import java.util.Set;

public class ModMixsonClient {

    public static void onInitialize() {

        Mixson.enableDebugOption(DebugOption.EXPORT_PATCHED_FILE);

        Mixson.registerEvent(
                Mixson.DEFAULT_PRIORITY,
                Lifetime.PERSISTENT,
                ErrorPolicy.THROW,
                "caustics:generate_leaper_models",
                idx -> idx.id().toString().equals("caustics:models/item/leaper_template"),
                ctx -> {
                    Set<Identifier> filteredHandles = MixsonUtil.getViableHandles();
                    Set<Identifier> filteredDecorations = MixsonUtil.getViableDecorations();

                    int counter = 0;
                    for(int i = 0; i < 2; i++) {
                        boolean hasCore = i == 0;
                        for(Identifier handle : filteredHandles)
                            for(Identifier decoration : filteredDecorations) {
                                JsonObject model = createModel(handle, decoration, hasCore);
                                Identifier id = MixsonUtil.createId("models/item/generated/", handle, decoration, hasCore);
                                ctx.createResource(new Index(id), model);
                                counter++;
                            }
                    }
                    Caustics.LOGGER.info("Created {} leaper models", counter);
                }
        );

        Mixson.registerEvent(
                Mixson.DEFAULT_PRIORITY,
                Lifetime.PERSISTENT,
                ErrorPolicy.THROW,
                "caustics:generate_leaper_item",
                idx -> idx.id().toString().equals("caustics:items/mirror"),
                ctx -> {
                    Set<Identifier> filteredHandles = MixsonUtil.getViableHandles();
                    Set<Identifier> filteredDecorations = MixsonUtil.getViableDecorations();

                    JsonObject item = new JsonObject();
                    JsonObject model = new JsonObject();
                    model.addProperty("type", "select");
                    model.addProperty("property", "minecraft:component");
                    model.addProperty("component", "caustics:leaper_material");

                    JsonArray cases = new JsonArray();
                    for(int i = 0; i < 2; i++) {
                        boolean hasCore = i == 0;
                        for(Identifier handle : filteredHandles)
                            for(Identifier decoration : filteredDecorations) {
                                JsonObject caseObj = new JsonObject();
                                JsonObject when = new JsonObject();
                                when.addProperty("handle", handle.toString());
                                when.addProperty("decoration", decoration.toString());
                                when.addProperty("has_core", hasCore);
                                caseObj.add("when", when);

                                JsonObject caseModel = new JsonObject();
                                caseModel.addProperty("type", "model");
                                caseModel.addProperty("model", MixsonUtil.createId("item/generated/", handle, decoration, hasCore).toString());
                                caseObj.add("model", caseModel);

                                cases.add(caseObj);
                            }
                    }
                    model.add("cases", cases);

                    JsonObject fallback = new JsonObject();
                    fallback.addProperty("type", "model");
                    fallback.addProperty("model", "caustics:item/leaper");
                    model.add("fallback", fallback);

                    item.add("model", model);

                    ctx.createResource(new Index(Caustics.id("items/leaper")), item);
                    ctx.setDebugExport(item);
                    Caustics.LOGGER.info("Created leaper item model");
                }
        );
    }

    private static JsonObject createModel(Identifier handle, Identifier decoration, boolean hasCore) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "caustics:item/leaper_template");
        JsonObject textures = new JsonObject();
        textures.addProperty("crystal", "caustics:item/leaper/crystal/beryl_shard");
        textures.addProperty("decoration", String.format("%s:item/leaper/decoration/%s", decoration.getNamespace(), decoration.getPath()));
        textures.addProperty("handle", String.format("%s:item/leaper/handle/%s", handle.getNamespace(), handle.getPath()));
        String core;
        if(hasCore)
            core = String.format("%s:item/leaper/decoration/core/%s", decoration.getNamespace(), decoration.getPath());
        else
            core = String.format("%s:item/leaper/handle/core/%s", handle.getNamespace(), handle.getPath());
        textures.addProperty("core", core);
        model.add("textures", textures);

        return model;
    }

}
