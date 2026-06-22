package net.ramixin.caustics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.Identifier;
import net.ramixin.caustics.utils.MixsonUtil;
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

        Mixson.registerEvent(
                Mixson.DEFAULT_PRIORITY,
                Lifetime.PERSISTENT,
                ErrorPolicy.THROW,
                "caustics:create_leaper_recipes",
                idx -> idx.id().toString().equals("caustics:recipe/mirror"),
                ctx -> {
                    Set<Identifier> filteredHandles = MixsonUtil.getViableHandles();
                    Set<Identifier> filteredDecorations = MixsonUtil.getViableDecorations();

                    int counter = 0;
                    for(int i = 0; i < 2; i++) {
                        boolean hasCore = i == 0;
                        for(Identifier handle : filteredHandles)
                            for(Identifier decoration : filteredDecorations) {
                                JsonObject recipe = createRecipe(handle, decoration, hasCore);
                                Identifier recipeId = MixsonUtil.createId("recipes/", handle, decoration, hasCore);
                                ctx.createResource(new Index(recipeId), recipe);
                                counter++;
                            }
                    }
                    Caustics.LOGGER.info("Created {} leaper recipes", counter);
                }
        );
    }

    @SuppressWarnings("ExtractMethodRecommender")
    private static JsonObject createRecipe(Identifier handle, Identifier decoration, boolean hasCore) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "equipment");
        recipe.addProperty("group", "leaper_"+handle);
        JsonObject key = new JsonObject();
        key.addProperty("S", "caustics:sapphire_shard");
        key.addProperty("D", decoration.toString());
        key.addProperty("H", handle.toString());
        if(hasCore)
            key.addProperty("C", "caustics:selenite_shard");
        recipe.add("key", key);

        JsonArray pattern = new JsonArray();
        pattern.add(" DS");
        if(hasCore) {
            pattern.add("DHD");
            pattern.add("CD ");
        } else {
            pattern.add(" HD");
        }
        recipe.add("pattern", pattern);

        JsonObject result = new JsonObject();
        result.addProperty("count", 1);
        result.addProperty("id", "caustics:leaper");
        JsonObject components = new JsonObject();
        JsonObject leaperMaterial = new JsonObject();
        leaperMaterial.addProperty("handle", handle.toString());
        leaperMaterial.addProperty("decoration", decoration.toString());
        leaperMaterial.addProperty("has_core", hasCore);
        components.add("caustics:leaper_material", leaperMaterial);
        result.add("components", components);
        recipe.add("result", result);

        return recipe;
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
