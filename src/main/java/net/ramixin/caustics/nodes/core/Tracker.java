package net.ramixin.caustics.nodes.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Tracker {

    private final Set<Task> tasks = new HashSet<>();
    private final Set<Task> lazyTasks = new HashSet<>();

    protected Tracker() {}

    protected void begin() {
        if(!tasks.isEmpty()) throw new IllegalStateException("Tracker has incomplete tasks: " + tasks);
        tasks.addAll(lazyTasks);
        lazyTasks.clear();
    }

    public void push(Task... task) {
        Collections.addAll(tasks, task);
    }

    protected void lazyPush(Task task) {
        lazyTasks.add(task);
    }

    protected boolean consume(Task task) {
        return tasks.remove(task);
    }

    public enum Task {
        NODE_SYNC,
        FREQUENCY_SYNC,
        ROUTING_SYNC,
        REBUILD_ROUTING,
        DIRTY
    }
}
