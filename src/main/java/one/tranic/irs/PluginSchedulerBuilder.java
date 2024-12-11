package one.tranic.irs;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import one.tranic.irs.platform.Platform;
import one.tranic.irs.task.FoliaScheduledTask;
import one.tranic.irs.task.SpigotScheduledTask;
import one.tranic.irs.task.TaskImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * Builder class for scheduling tasks in a Bukkit or Folia environment.
 * Supports synchronous and asynchronous execution with various scheduling options.
 *
 * <p>Usage example:
 * <pre>
 * {@code
 * PluginSchedulerBuilder.builder(plugin)
 *     .sync()
 *     .setTask(() -> Bukkit.getLogger().info("Task executed!"))
 *     .setDelayTicks(20L)
 *     .run();
 * }
 * </pre>
 */
public class PluginSchedulerBuilder {
    private final Plugin plugin;
    private boolean folia = false;
    private boolean sync;
    private long delayTicks;
    private long period;
    private Runnable task;
    private Location location;
    private Entity entity;

    /**
     * Constructs a new {@code PluginSchedulerBuilder} for the given plugin.
     * Automatically detects the platform to adjust behavior for Folia or Spigot.
     *
     * @param plugin the plugin instance to associate with this scheduler builder
     */
    public PluginSchedulerBuilder(Plugin plugin) {
        this.folia = Platform.isMultithreading();
        this.plugin = plugin;
    }

    /**
     * Creates a new instance of {@code PluginSchedulerBuilder}.
     *
     * @param plugin the plugin instance to associate with this scheduler builder
     * @return a new {@code PluginSchedulerBuilder} instance
     */
    public static PluginSchedulerBuilder builder(Plugin plugin) {
        return new PluginSchedulerBuilder(plugin);
    }

    /**
     * Configures the task to run synchronously.
     *
     * @return this builder instance for method chaining
     */
    public PluginSchedulerBuilder sync() {
        if (!this.sync) this.sync = true;
        return this;
    }

    /**
     * Configures the task to run synchronously at the specified location.
     *
     * @param location the location where the task will execute synchronously
     * @return this builder instance for method chaining
     */
    public PluginSchedulerBuilder sync(@NotNull Location location) {
        if (this.location == null) {
            this.sync = true;
            this.location = location;
        }
        return this;
    }

    /**
     * Configures the task to run synchronously on the specified entity.
     *
     * @param entity the entity where the task will execute synchronously
     * @return this builder instance for method chaining
     */
    public PluginSchedulerBuilder sync(@NotNull Entity entity) {
        if (this.entity == null) {
            this.sync = true;
            this.entity = entity;
        }
        return this;
    }

    /**
     * Configures the task to run asynchronously.
     *
     * @return this builder instance for method chaining
     */
    public PluginSchedulerBuilder async() {
        if (this.sync && (!folia && this.location == null && this.entity == null)) this.sync = false;
        return this;
    }

    /**
     * Sets the task to be executed.
     *
     * @param runnable the task to execute
     * @return this builder instance for method chaining
     */
    public PluginSchedulerBuilder setTask(@NotNull Runnable runnable) {
        if (this.task == null) this.task = runnable;
        return this;
    }

    /**
     * Sets the initial delay in ticks before the task is executed.
     *
     * @param delayTicks the number of ticks to delay
     * @return this builder instance for method chaining
     */
    public PluginSchedulerBuilder setDelayTicks(long delayTicks) {
        if (this.folia && delayTicks < 1L) this.delayTicks = 1L;
        else this.delayTicks = delayTicks;
        return this;
    }

    /**
     * Sets the period in ticks between consecutive executions of the task.
     *
     * @param period the number of ticks between executions
     * @return this builder instance for method chaining
     */
    public PluginSchedulerBuilder setPeriod(long period) {
        if (this.folia && period < 1L) this.period = 1L;
        else this.period = period;
        return this;
    }

    /**
     * Builds and schedules the task based on the configured parameters.
     *
     * @return a {@code TaskImpl} representing the scheduled task
     * @throws UnsupportedOperationException if the task is not set
     */
    public TaskImpl<Plugin> run() {
        if (this.task == null) throw new UnsupportedOperationException("It seems that the task has not been set.");
        if (folia) return runFoliaTask();
        return runBukkitTask();
    }

    private TaskImpl<Plugin> runFoliaTask() {
        if (sync) {
            if (this.entity != null) return newFoliaEntityTask();
            if (this.location != null) return newFoliaRegionTask();
            return newFoliaGlobalRegionTask();
        }
        return newFoliaAsyncTask();
    }

    private TaskImpl<Plugin> runBukkitTask() {
        if (sync) return newBukkitSyncTask();
        return newBukkitAsyncTask();
    }

    private TaskImpl<Plugin> newFoliaEntityTask() {
        @NotNull EntityScheduler scheduler = this.entity.getScheduler();
        if (this.delayTicks != 0L) {
            if (this.period != 0L)
                return new FoliaScheduledTask(scheduler.runAtFixedRate(plugin, (e) -> task.run(), null, delayTicks, period));
            else return new FoliaScheduledTask(scheduler.runDelayed(plugin, (e) -> task.run(), null, delayTicks));
        }
        return new FoliaScheduledTask(scheduler.run(plugin, (e) -> task.run(), null));
    }

    private TaskImpl<Plugin> newFoliaRegionTask() {
        @NotNull RegionScheduler scheduler = Bukkit.getRegionScheduler();
        if (this.delayTicks != 0L) {
            if (this.period != 0L)
                return new FoliaScheduledTask(scheduler.runAtFixedRate(plugin, location, (e) -> task.run(), delayTicks, period));
            else
                return new FoliaScheduledTask(scheduler.runDelayed(plugin, location, (e) -> task.run(), delayTicks));
        }
        return new FoliaScheduledTask(scheduler.run(plugin, location, (e) -> task.run()));
    }

    private TaskImpl<Plugin> newFoliaGlobalRegionTask() {
        @NotNull GlobalRegionScheduler scheduler = Bukkit.getGlobalRegionScheduler();
        if (this.delayTicks != 0L) {
            if (this.period != 0L)
                return new FoliaScheduledTask(scheduler.runAtFixedRate(plugin, (e) -> task.run(), delayTicks, period));
            else return new FoliaScheduledTask(scheduler.runDelayed(plugin, (e) -> task.run(), delayTicks));
        }
        return new FoliaScheduledTask(scheduler.run(plugin, (e) -> task.run()));
    }

    private TaskImpl<Plugin> newFoliaAsyncTask() {
        @NotNull AsyncScheduler scheduler = Bukkit.getAsyncScheduler();
        if (this.delayTicks != 0L) {
            if (this.period != 0L)
                return new FoliaScheduledTask(scheduler.runAtFixedRate(plugin, (e) -> task.run(), delayTicks * 50, period * 50, TimeUnit.MILLISECONDS));
            else
                return new FoliaScheduledTask(scheduler.runDelayed(plugin, (e) -> task.run(), delayTicks * 50, TimeUnit.MILLISECONDS));
        }
        return new FoliaScheduledTask(scheduler.runNow(plugin, (e) -> task.run()));
    }

    private TaskImpl<Plugin> newBukkitSyncTask() {
        @NotNull BukkitScheduler scheduler = Bukkit.getScheduler();
        if (this.delayTicks != 0L) {
            if (this.period != 0L)
                return new SpigotScheduledTask(scheduler.runTaskTimer(plugin, task, delayTicks, period));
            else
                return new SpigotScheduledTask(scheduler.runTaskLater(plugin, task, delayTicks));
        }
        return new SpigotScheduledTask(scheduler.runTask(plugin, task));
    }

    private TaskImpl<Plugin> newBukkitAsyncTask() {
        @NotNull BukkitScheduler scheduler = Bukkit.getScheduler();
        if (this.delayTicks != 0L) {
            if (this.period != 0L)
                return new SpigotScheduledTask(scheduler.runTaskTimerAsynchronously(plugin, task, delayTicks, period));
            else
                return new SpigotScheduledTask(scheduler.runTaskLaterAsynchronously(plugin, task, delayTicks));
        }
        return new SpigotScheduledTask(scheduler.runTaskAsynchronously(plugin, task));
    }
}
