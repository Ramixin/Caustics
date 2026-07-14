package net.ramixin.caustics.client.nodes;

import net.ramixin.caustics.client.cache.AlidadeIconCache;
import net.ramixin.caustics.client.cache.SimpleIconCache;
import net.ramixin.caustics.client.nodes.icons.CollimatorIcon;
import net.ramixin.caustics.client.nodes.icons.DowserIcon;

public class IconIndex {

    private final AlidadeIconCache alidadeCache = new AlidadeIconCache();
    private final SimpleIconCache<CollimatorIcon> collimatorCache = new SimpleIconCache<>(CollimatorIcon[]::new, CollimatorIcon::new);
    private final SimpleIconCache<DowserIcon> dowserCache = new SimpleIconCache<>(DowserIcon[]::new, DowserIcon::new);

    protected IconIndex() {}

    public void tick() {
        alidadeCache.tick();
        collimatorCache.tick();
        dowserCache.tick();
    }

    public AlidadeIconCache alidadeCache() {
        return alidadeCache;
    }

    public SimpleIconCache<CollimatorIcon> collimatorCache() {
        return collimatorCache;
    }

    public SimpleIconCache<DowserIcon> dowserCache() {
        return dowserCache;
    }

    public void wipe() {
        alidadeCache.wipe();
        collimatorCache.wipe();
        dowserCache.wipe();
    }

    public void clear() {
        alidadeCache.clear();
        collimatorCache.clear();
        dowserCache.clear();
    }

}
