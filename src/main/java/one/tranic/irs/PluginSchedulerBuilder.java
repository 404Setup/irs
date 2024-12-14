package one.tranic.irs;

import io.papermc.paper.threadedregions.scheduler.*;
import one.tranic.irs.platform.Platform;
import one.tranic.irs.task.FoliaScheduledTask;
import one.tranic.irs.task.SpigotScheduledTask;
import one.tranic.irs.task.TaskImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Builder class for scheduling tasks in a Bukkit or Folia environment.
 * Supports synchronous and asynchronous execution with various scheduling options.
 *
 * <p>Usage example:
 * <pre>
 * {@code
 * PluginSchedulerBuilder.builder(plugin)
 *     .sync()
 *     .task(() -> Bukkit.getLogger().info("Task executed!"))
 *     .delayTicks(20L)
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
    private Consumer<? super TaskImpl<Plugin>> taskConsumer;
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
        this.sync = true;
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

    public void cancelTasks() {
        if (folia) {
            Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
            Bukkit.getAsyncScheduler().cancelTasks(plugin);
        } else Bukkit.getScheduler().cancelTasks(plugin);
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
    public PluginSchedulerBuilder task(@NotNull Runnable runnable) {
        this.task = runnable;
        this.taskConsumer = null;
        return this;
    }

    /**
     * Sets the task to be executed.
     *
     * @param task the task to execute
     * @return this builder instance for method chaining
     */
    public PluginSchedulerBuilder task(@NotNull java.util.function.Consumer<? super TaskImpl<Plugin>> task) {
        this.taskConsumer = task;
        this.task = null;
        return this;
    }

    /**
     * Sets the initial delay in ticks before the task is executed.
     *
     * @param delayTicks the number of ticks to delay
     * @return this builder instance for method chaining
     */
    public PluginSchedulerBuilder delayTicks(long delayTicks) {
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
    public PluginSchedulerBuilder period(long period) {
        if (this.folia && period < 1L) this.period = 1L;
        else this.period = period;
        return this;
    }

    /**
     * Builds and schedules the task based on the configured parameters.
     *
     * @return a {@code TaskImpl} representing the scheduled task. <strong>Null</strong> if using Consumer on non-Folia.
     * @throws UnsupportedOperationException if the task is not set
     */
    public @Nullable TaskImpl<Plugin> run() {
        if (this.task == null && this.taskConsumer == null)
            throw new UnsupportedOperationException("It seems that the task has not been set.");
        return folia ? new FoliaScheduledTask(runFoliaTask()) : runBukkitTask();
    }

    private void accept(ScheduledTask scheduledTask) {
        if (this.taskConsumer != null)
            this.taskConsumer.accept(new FoliaScheduledTask(scheduledTask));
        else this.task.run();
    }

    private void accept(BukkitTask bukkitTask) {
        this.taskConsumer.accept(new SpigotScheduledTask(bukkitTask));
    }

    private @Nullable ScheduledTask runFoliaTask() {
        if (sync) {
            if (this.entity != null) return newFoliaEntityTask();
            if (this.location != null) return newFoliaRegionTask();
            return newFoliaGlobalRegionTask();
        }
        return newFoliaAsyncTask();
    }

    private @Nullable TaskImpl<Plugin> runBukkitTask() {
        return sync ? newBukkitSyncTask() : newBukkitAsyncTask();
    }

    private @Nullable ScheduledTask newFoliaEntityTask() {
        @NotNull EntityScheduler scheduler = this.entity.getScheduler();
        if (this.delayTicks != 0L) return this.period != 0L
                ? scheduler.runAtFixedRate(plugin, this::accept, null, delayTicks, period)
                : scheduler.runDelayed(plugin, this::accept, null, delayTicks);
        return scheduler.run(plugin, this::accept, null);
    }

    private @NotNull ScheduledTask newFoliaRegionTask() {
        @NotNull RegionScheduler scheduler = Bukkit.getRegionScheduler();
        if (this.delayTicks != 0L) return this.period != 0L
                ? scheduler.runAtFixedRate(plugin, location, this::accept, delayTicks, period)
                : scheduler.runDelayed(plugin, location, this::accept, delayTicks);
        return scheduler.run(plugin, location, this::accept);
    }

    private @NotNull ScheduledTask newFoliaGlobalRegionTask() {
        @NotNull GlobalRegionScheduler scheduler = Bukkit.getGlobalRegionScheduler();
        if (this.delayTicks != 0L) return this.period != 0L
                ? scheduler.runAtFixedRate(plugin, this::accept, delayTicks, period)
                : scheduler.runDelayed(plugin, this::accept, delayTicks);
        return scheduler.run(plugin, this::accept);
    }

    private @NotNull ScheduledTask newFoliaAsyncTask() {
        @NotNull AsyncScheduler scheduler = Bukkit.getAsyncScheduler();
        if (this.delayTicks != 0L) return this.period != 0L
                ? scheduler.runAtFixedRate(plugin, this::accept, delayTicks * 50, period * 50, TimeUnit.MILLISECONDS)
                : scheduler.runDelayed(plugin, this::accept, delayTicks * 50, TimeUnit.MILLISECONDS);
        return scheduler.runNow(plugin, this::accept);
    }

    private @Nullable TaskImpl<Plugin> newBukkitSyncTask() {
        @NotNull BukkitScheduler scheduler = Bukkit.getScheduler();
        if (this.delayTicks != 0L) {
            if (this.period != 0L)
                if (this.taskConsumer != null) {
                    scheduler.runTaskTimer(plugin, this::accept, delayTicks, period);
                    return null;
                } else return new SpigotScheduledTask(scheduler.runTaskTimer(plugin, task, delayTicks, period));
            else {
                if (this.taskConsumer != null) {
                    scheduler.runTaskLater(plugin, this::accept, delayTicks);
                    return null;
                } else return new SpigotScheduledTask(scheduler.runTaskLater(plugin, task, delayTicks));
            }
        }
        if (this.taskConsumer != null) {
            scheduler.runTask(plugin, this::accept);
            return null;
        } else return new SpigotScheduledTask(scheduler.runTask(plugin, task));
    }

    private @Nullable TaskImpl<Plugin> newBukkitAsyncTask() {
        @NotNull BukkitScheduler scheduler = Bukkit.getScheduler();
        if (this.delayTicks != 0L) {
            if (this.period != 0L)
                if (this.taskConsumer != null) {
                    scheduler.runTaskTimerAsynchronously(plugin, this::accept, delayTicks, period);
                    return null;
                } else
                    return new SpigotScheduledTask(scheduler.runTaskTimerAsynchronously(plugin, task, delayTicks, period));
            else {
                if (this.taskConsumer != null) {
                    scheduler.runTaskLaterAsynchronously(plugin, this::accept, delayTicks);
                    return null;
                } else return new SpigotScheduledTask(scheduler.runTaskLaterAsynchronously(plugin, task, delayTicks));
            }
        }
        if (this.taskConsumer != null) {
            scheduler.runTaskAsynchronously(plugin, this::accept);
            return null;
        } else return new SpigotScheduledTask(scheduler.runTaskAsynchronously(plugin, task));
    }
}
