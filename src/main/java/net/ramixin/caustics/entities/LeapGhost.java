package net.ramixin.caustics.entities;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class LeapGhost extends Avatar {

    protected static final EntityDataAccessor<ResolvableProfile> DATA_PROFILE = SynchedEntityData.defineId(LeapGhost.class, EntityDataSerializers.RESOLVABLE_PROFILE);
    protected static EntityType.EntityFactory<LeapGhost> FACTORY = LeapGhost::new;

    protected LeapGhost(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    public LeapGhost(Level level) {
        super(ModEntities.LEAP_GHOST, level);
    }

    public static LeapGhost create(EntityType<LeapGhost> type, Level level) {
        return FACTORY.create(type, level);
    }

    public static AttributeSupplier createAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.GRAVITY, 0).build();
    }

    @Override
    public boolean canBeAffected(@NonNull MobEffectInstance newEffect) {
        return false;
    }

    @Override
    public boolean canCollideWith(@NonNull Entity entity) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_PROFILE, Mannequin.DEFAULT_PROFILE);
    }

    @Override
    public void tick() {
        super.tick();
        Player player = level().getPlayerByUUID(getProfileId());
        if(player == null) return;
        float health = player.getHealth();
        if(getHealth() != health && !player.isCreative()) {
            setHealth(health);
        }
        float rotY = player.getYRot();
        if(getYRot() != rotY) {
            setYRot(rotY);
        }
        float rotX = player.getXRot();
        if(getXRot() != rotX) {
            setXRot(rotX);
        }
        float headRotY = player.getYHeadRot();
        if(getYHeadRot() != headRotY) {
            setYHeadRot(headRotY);
        }
        setMainArm(player.getMainArm());
        setItemInHand(InteractionHand.MAIN_HAND, player.getItemInHand(InteractionHand.MAIN_HAND));
        setItemInHand(InteractionHand.OFF_HAND, player.getItemInHand(InteractionHand.OFF_HAND));
        if(player.isUsingItem())
            startUsingItem(player.getUsedItemHand());
        else
            stopUsingItem();
        setPose(player.getPose());
        equipment.set(EquipmentSlot.HEAD, player.getItemBySlot(EquipmentSlot.HEAD));
        equipment.set(EquipmentSlot.CHEST, player.getItemBySlot(EquipmentSlot.CHEST));
        equipment.set(EquipmentSlot.LEGS, player.getItemBySlot(EquipmentSlot.LEGS));
        equipment.set(EquipmentSlot.FEET, player.getItemBySlot(EquipmentSlot.FEET));
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource source, float damage) {
        boolean original = super.hurtServer(level, source, damage);
        Player player = level().getPlayerByUUID(getProfileId());
        if(player == null) return original;
        player.hurtServer(level, source, damage);
        return original;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public @NonNull PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public @NonNull ResolvableProfile getProfile() {
        return getEntityData().get(DATA_PROFILE);
    }

    public UUID getProfileId() {
        return getProfile().partialProfile().id();
    }

    public void setProfile(ResolvableProfile profile) {
        getEntityData().set(DATA_PROFILE, profile);
    }
}
