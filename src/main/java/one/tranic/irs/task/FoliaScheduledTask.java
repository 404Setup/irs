package one.tranic.irs.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;

public class FoliaScheduledTask implements TaskImpl<Plugin> {
    private final ScheduledTask foliaTask;

    public FoliaScheduledTask(ScheduledTask foliaTask) {
        this.foliaTask = foliaTask;
    }

    @Override
    public void cancel() {
        foliaTask.cancel();
    }

    @Override
    public boolean isCancelled() {
        return foliaTask.isCancelled();
    }

    @Override
    public Plugin getOwner() {
        return foliaTask.getOwningPlugin();
    }

    @Override
    public boolean isRepeatingTask() {
        return foliaTask.isRepeatingTask();
    }

    @Override
    public boolean isSynchronized() {
        return false;
    }
}
