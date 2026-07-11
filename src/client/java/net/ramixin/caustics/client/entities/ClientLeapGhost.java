package net.ramixin.caustics.client.entities;

import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.level.Level;
import net.ramixin.caustics.Caustics;
import net.ramixin.caustics.client.nodes.ClientCrystalNetwork;
import net.ramixin.caustics.entities.LeapGhost;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ClientLeapGhost extends LeapGhost implements ClientAvatarEntity {

    private static final PlayerSkin DEFAULT_SKIN = DefaultPlayerSkin.get(Mannequin.DEFAULT_PROFILE.partialProfile());
    public final ClientAvatarState state = new ClientAvatarState();
    private @Nullable CompletableFuture<Optional<PlayerSkin>> skinLookup;
    private PlayerSkin skin;
    private final PlayerSkinRenderCache skinRenderCache;

    protected ClientLeapGhost(Level level, final PlayerSkinRenderCache skinRenderCache) {
        super(level);
        this.skin = DEFAULT_SKIN;
        this.skinRenderCache = skinRenderCache;
    }

    public static void overrideFactory(PlayerSkinRenderCache cache) {
        EntityType.EntityFactory<LeapGhost> current = ClientLeapGhost.FACTORY;
        ClientLeapGhost.FACTORY = (type, level) -> level instanceof ClientLevel ? new ClientLeapGhost(level, cache) : current.create(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        state.tick(this.position(), this.getDeltaMovement());
        if (this.skinLookup != null && this.skinLookup.isDone()) {
            try {
                this.skinLookup.get().ifPresent(this::setSkin);
                this.skinLookup = null;
            } catch (Exception e) {
                Caustics.LOGGER.error("Error when trying to look up skin", e);
            }
        }
        ClientCrystalNetwork.getInstance().particleEngine().addParticle(this);
    }

    @Override
    public void onSyncedDataUpdated(@NonNull EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if(accessor.equals(LeapGhost.DATA_PROFILE)) {
            this.updateSkin();
        }
    }

    @Override
    public @NonNull ClientAvatarState avatarState() {
        return state;
    }

    @Override
    public @NonNull PlayerSkin getSkin() {
        return skin;
    }

    private void setSkin(PlayerSkin skin) {
        this.skin = skin;
    }

    private void updateSkin() {
        if (this.skinLookup != null) {
            CompletableFuture<Optional<PlayerSkin>> future = this.skinLookup;
            this.skinLookup = null;
            future.cancel(false);
        }

        this.skinLookup = this.skinRenderCache.lookup(this.getProfile()).thenApply((info) -> info.map(PlayerSkinRenderCache.RenderInfo::playerSkin));
    }

    @Override
    public Parrot.@Nullable Variant getParrotVariantOnShoulder(boolean left) {
        return null;
    }

    @Override
    public boolean showExtraEars() {
        return false;
    }
}
