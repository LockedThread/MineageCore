package rip.simpleness.mineagecore.task;

import rip.simpleness.mineagecore.objs.Generation;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GenerationTask implements Runnable {

    private ConcurrentHashMap<UUID, Generation> generations;

    public GenerationTask() {
        generations = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        if (!generations.isEmpty()) {
            generations.entrySet()
                    .stream()
                    .filter(entry -> !entry.getValue().generate())
                    .forEach(entry -> generations.remove(entry.getKey()));
        }
    }

    public ConcurrentHashMap<UUID, Generation> getGenerations() {
        return generations;
    }
}
