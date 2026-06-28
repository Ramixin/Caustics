package net.ramixin.caustics.entities;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.ramixin.caustics.Caustics;

public class ModEntities {

    public static final EntityType<LeapGhost> LEAP_GHOST = register("leap_ghost", LeapGhost::create);

    @SuppressWarnings("DataFlowIssue")
    public static void onInitialize() {
        FabricDefaultAttributeRegistry.register(LEAP_GHOST, LeapGhost.createAttributes());
    }

    private static <T extends Entity> EntityType<T> register(String name, EntityType.EntityFactory<T> constructor) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Caustics.id(name));
        EntityType<T> type = EntityType.Builder.of(constructor, MobCategory.MISC).noSave().build(key);
        Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
        return type;
    }
}
