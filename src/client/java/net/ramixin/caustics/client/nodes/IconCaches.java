package net.ramixin.caustics.client.nodes;

import net.ramixin.caustics.client.nodes.cache.AlidadeIconCache;
import net.ramixin.caustics.client.nodes.cache.DowserIconCache;
import net.ramixin.caustics.client.nodes.cache.SimpleIconCache;
import net.ramixin.caustics.client.nodes.icons.CollimatorIcon;

public class IconCaches {

    private final AlidadeIconCache alidadeCache = new AlidadeIconCache();
    private final SimpleIconCache<CollimatorIcon> collimatorCache = new SimpleIconCache<>(CollimatorIcon[]::new, CollimatorIcon::new);
    private final DowserIconCache dowserCache = new DowserIconCache();

    protected IconCaches() {}

    public void tick() {
        alidadeCache.tick();
        collimatorCache.tick();
        dowserCache.tick();
    }

    public AlidadeIconCache alidade() {
        return alidadeCache;
    }

    public SimpleIconCache<CollimatorIcon> collimatorCache() {
        return collimatorCache;
    }

    public DowserIconCache dowserCache() {
        return dowserCache;
    }

    public void wipeAll() {
        alidadeCache.wipe();
        collimatorCache.wipe();
        dowserCache.wipe();
    }

    public void clearAll() {
        alidadeCache.clear();
        collimatorCache.clear();
        dowserCache.clear();
    }

}
