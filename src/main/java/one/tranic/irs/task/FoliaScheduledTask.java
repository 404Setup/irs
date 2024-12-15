package one.tranic.irs.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;

public class FoliaScheduledTask implements TaskImpl<Plugin> {
    private final ScheduledTask foliaTask;
    private final boolean isSynchronized;

    public FoliaScheduledTask(ScheduledTask foliaTask) {
        this(foliaTask, true);
    }

    public FoliaScheduledTask(ScheduledTask foliaTask, boolean isSynchronized) {
        this.foliaTask = foliaTask;
        this.isSynchronized = isSynchronized;
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
        return isSynchronized;
    }
}
