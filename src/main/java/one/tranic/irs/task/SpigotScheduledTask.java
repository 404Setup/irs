package one.tranic.irs.task;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class SpigotScheduledTask implements TaskImpl<Plugin> {
    private final BukkitTask bukkitTask;

    public SpigotScheduledTask(BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    @Override
    public void cancel() {
        bukkitTask.cancel();
    }

    @Override
    public boolean isCancelled() {
        return bukkitTask.isCancelled();
    }

    @Override
    public Plugin getOwner() {
        return bukkitTask.getOwner();
    }

    @Override
    public boolean isRepeatingTask() {
        return false;
    }

    @Override
    public boolean isSynchronized() {
        return bukkitTask.isSync();
    }
}
