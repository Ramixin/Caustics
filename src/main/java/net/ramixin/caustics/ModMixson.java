package net.ramixin.caustics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.Identifier;
import net.ramixin.mixson.Mixson;
import net.ramixin.mixson.enums.ErrorPolicy;
import net.ramixin.mixson.enums.Lifetime;
import net.ramixin.mixson.util.Index;

import java.util.Set;
import java.util.function.Consumer;

public class ModMixson {



    public static void onInitialize() {

        registerGeode("sapphire");
        registerGeode("beryl");
        registerGeode("peridot");
        registerGeode("topaz");
        registerGeode("sunstone");
        registerGeode("selenite");
        registerGeode("tourmaline");
    }

    private static void registerGeode(String crystal) {
        Mixson.registerEvent(
                Mixson.DEFAULT_PRIORITY,
                Lifetime.PERSISTENT,
                ErrorPolicy.THROW,
                "caustics:create_"+crystal+"_configuration",
                idx -> isAmethystGeode(idx, "configured_feature"),
                ctx -> {
                    JsonObject object = ctx.getFile().getAsJsonObject().deepCopy();
                    traverseConfiguration(object, crystal, _ -> {throw new IllegalStateException();});

                    ctx.createResource(new Index(Caustics.id("worldgen/configured_feature/"+crystal+"_geode")), object);
                }
        );

        Mixson.registerEvent(
                Mixson.DEFAULT_PRIORITY,
                Lifetime.PERSISTENT,
                ErrorPolicy.THROW,
                "caustics:create_"+crystal+"_placed",
                idx -> isAmethystGeode(idx, "placed_feature"),
                ctx -> {
                    JsonObject original = ctx.getFile().getAsJsonObject();
                    JsonArray placement = original.getAsJsonArray("placement");
                    for(JsonElement e : placement) {
                        JsonObject obj = e.getAsJsonObject();
                        if(obj.has("chance")) {
                            obj.addProperty("chance", 192);
                            break;
                        }
                    }

                    JsonObject object = original.deepCopy();
                    object.addProperty("feature", "caustics:"+crystal+"_geode");
                    ctx.createResource(new Index(Caustics.id("worldgen/placed_feature/"+crystal+"_geode")), object);
                }
        );
    }

    private static boolean isAmethystGeode(Index idx, String subFolder) {
        Identifier id = idx.id();
        if(!id.getNamespace().equals("minecraft")) return false;
        String path = String.format("worldgen/%s/amethyst_geode", subFolder);
        return id.getPath().equals(path);
    }

    private static void traverseConfiguration(JsonElement element, String replacement, Consumer<String> setter) {
        switch(element) {
            case JsonObject obj -> {
                for(String key : Set.copyOf(obj.keySet())) {
                    traverseConfiguration(obj.get(key), replacement, str -> obj.addProperty(key, str));
                }
            }
            case JsonArray arr -> {
                for(int i = 0; i < arr.size(); i++) {
                    int finalI = i;
                    traverseConfiguration(arr.get(i), replacement, str -> arr.set(finalI, new JsonPrimitive(str)));
                }
            }
            case JsonPrimitive primitive -> {
                if(!primitive.isString()) return;
                String value = primitive.getAsString();
                if(!value.contains("amethyst")) return;
                String newValue = value.replaceAll("amethyst", replacement).replace("minecraft", Caustics.MOD_ID);
                setter.accept(newValue);
            }
            default -> throw new IllegalStateException("Unexpected value: " + element);
        }
    }
}
