package net.ramixin.caustics.nodes.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Tracker {

    private final Set<Item> items = new HashSet<>();
    private final Set<Item> lazyItems = new HashSet<>();

    protected Tracker() {}

    protected void begin() {
        if(!items.isEmpty()) throw new IllegalStateException("Tracker has incomplete items: " + items);
        items.addAll(lazyItems);
        lazyItems.clear();
    }

    public void push(Item... item) {
        Collections.addAll(items, item);
    }

    protected void lazyPush(Item item) {
        lazyItems.add(item);
    }

    protected boolean consume(Item item) {
        return items.remove(item);
    }

    public enum Item {
        NODE_SYNC,
        FREQUENCY_SYNC,
        ROUTING_SYNC,
        REBUILD_ROUTING,
        DIRTY
    }
}
