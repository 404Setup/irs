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
 * // Folia: In GlobalRegion
 * // Spigot/Paper: In MainThread Scheduler
 * PluginSchedulerBuilder.builder(plugin)
 *     .sync()
 *     .task(() -> Bukkit.getLogger().info("Task executed!"))
 *     .delayTicks(20L)
 *     .run();
 * }
 * </pre>
 */
public class PluginSchedulerBuilder {
    private static final long MINIMUM_TICK = 1L;


    private final Plugin plugin;
    private boolean isFoliaDispatcher;
    private boolean isSynchronous;
    private Long initialDelayTicks;
    private Long periodTicks;
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
        this.isFoliaDispatcher = Platform.isMultithreading();
        this.plugin = plugin;
        this.isSynchronous = true;
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
        if (isFoliaDispatcher) {
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
        this.isSynchronous = true;
        return this;
    }

    /**
     * Configures the task to run synchronously at the specified location.
     *
     * @param location the location where the task will execute synchronously
     * @return this builder instance for method chaining
     */
    public PluginSchedulerBuilder sync(@NotNull Location location) {
        this.isSynchronous = true;
        this.location = location;
        this.entity = null;
        return this;
    }

    /**
     * Configures the task to run synchronously on the specified entity.
     *
     * @param entity the entity where the task will execute synchronously
     * @return this builder instance for method chaining
     */
    public PluginSchedulerBuilder sync(@NotNull Entity entity) {
        this.isSynchronous = true;
        this.entity = entity;
        this.location = null;
        return this;
    }

    /**
     * Configures the task to run asynchronously.
     * <p>
     * <strong>Processing entities and worlds in asynchronous tasks is not allowed.</strong>
     *
     * @return this builder instance for method chaining
     */
    public PluginSchedulerBuilder async() {
        this.isSynchronous = false;
        this.location = null;
        this.entity = null;
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
        this.initialDelayTicks = normalizeTick(delayTicks);
        return this;

    }

    /**
     * Sets the period in ticks between consecutive executions of the task.
     *
     * @param period the number of ticks between executions
     * @return this builder instance for method chaining
     */
    public PluginSchedulerBuilder period(long period) {
        this.periodTicks = normalizeTick(periodTicks);
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
        return isFoliaDispatcher ? runFoliaTask() : runBukkitTask();
    }

    private boolean hasInitialDelay() {
        return this.initialDelayTicks != null;
    }

    private boolean hasPeriod() {
        return this.periodTicks != null;
    }

    private long normalizeTick(long tick) {
        return (isFoliaDispatcher && tick < MINIMUM_TICK) ? MINIMUM_TICK : tick;
    }

    private void processScheduledTask(ScheduledTask scheduledTask) {
        if (this.taskConsumer != null)
            this.taskConsumer.accept(new FoliaScheduledTask(scheduledTask));
        else this.task.run();
    }

    private void processBukkitTask(BukkitTask bukkitTask) {
        this.taskConsumer.accept(new SpigotScheduledTask(bukkitTask));
    }

    private @Nullable TaskImpl<Plugin> runBukkitTask() {
        @NotNull BukkitScheduler scheduler = Bukkit.getScheduler();
        return isSynchronous ? newBukkitSyncTask(scheduler) : newBukkitAsyncTask(scheduler);
    }

    private @Nullable TaskImpl<Plugin> runFoliaTask() {
        if (isSynchronous) {
            @Nullable ScheduledTask tasks = runFoliaSyncTask();
            return tasks != null ? new FoliaScheduledTask(tasks) : null;
        }
        return new FoliaScheduledTask(newFoliaAsyncTask(), false);
    }

    private @Nullable ScheduledTask runFoliaSyncTask() {
        if (this.entity != null) return newFoliaEntityTask();
        if (this.location != null) return newFoliaRegionTask();
        return newFoliaGlobalRegionTask();
    }

    private @Nullable ScheduledTask newFoliaEntityTask() {
        @NotNull EntityScheduler scheduler = this.entity.getScheduler();
        if (hasInitialDelay()) {
            return hasPeriod()
                    ? scheduler.runAtFixedRate(plugin, this::processScheduledTask, null, initialDelayTicks, periodTicks)
                    : scheduler.runDelayed(plugin, this::processScheduledTask, null, initialDelayTicks);
        }
        return scheduler.run(plugin, this::processScheduledTask, null);
    }

    private @NotNull ScheduledTask newFoliaRegionTask() {
        @NotNull RegionScheduler scheduler = Bukkit.getRegionScheduler();
        if (hasInitialDelay()) return hasPeriod()
                ? scheduler.runAtFixedRate(plugin, location, this::processScheduledTask, initialDelayTicks, periodTicks)
                : scheduler.runDelayed(plugin, location, this::processScheduledTask, initialDelayTicks);
        return scheduler.run(plugin, location, this::processScheduledTask);
    }

    private @NotNull ScheduledTask newFoliaGlobalRegionTask() {
        @NotNull GlobalRegionScheduler scheduler = Bukkit.getGlobalRegionScheduler();
        if (hasInitialDelay()) return hasPeriod()
                ? scheduler.runAtFixedRate(plugin, this::processScheduledTask, initialDelayTicks, periodTicks)
                : scheduler.runDelayed(plugin, this::processScheduledTask, initialDelayTicks);
        return scheduler.run(plugin, this::processScheduledTask);
    }

    private @NotNull ScheduledTask newFoliaAsyncTask() {
        @NotNull AsyncScheduler scheduler = Bukkit.getAsyncScheduler();
        if (hasInitialDelay()) return hasPeriod()
                ? scheduler.runAtFixedRate(plugin, this::processScheduledTask, initialDelayTicks * 50, periodTicks * 50, TimeUnit.MILLISECONDS)
                : scheduler.runDelayed(plugin, this::processScheduledTask, initialDelayTicks * 50, TimeUnit.MILLISECONDS);
        return scheduler.runNow(plugin, this::processScheduledTask);
    }

    private @Nullable TaskImpl<Plugin> newBukkitSyncTask(BukkitScheduler scheduler) {
        if (hasInitialDelay()) {
            if (hasPeriod())
                if (this.taskConsumer != null) {
                    scheduler.runTaskTimer(plugin, this::processBukkitTask, initialDelayTicks, periodTicks);
                    return null;
                } else return new SpigotScheduledTask(scheduler.runTaskTimer(plugin, task, initialDelayTicks, periodTicks));
            else {
                if (this.taskConsumer != null) {
                    scheduler.runTaskLater(plugin, this::processBukkitTask, initialDelayTicks);return null;
                } else return new SpigotScheduledTask(scheduler.runTaskLater(plugin, task, initialDelayTicks));
            }
        }
        if (this.taskConsumer != null) {
            scheduler.runTask(plugin, this::processBukkitTask);
            return null;
        } else return new SpigotScheduledTask(scheduler.runTask(plugin, task));
    }

    private @Nullable TaskImpl<Plugin> newBukkitAsyncTask(BukkitScheduler scheduler) {
        if (hasInitialDelay()) {
            if (hasPeriod())
                if (this.taskConsumer != null) {
                    scheduler.runTaskTimerAsynchronously(plugin, this::processBukkitTask, initialDelayTicks, periodTicks);
                    return null;
                } else
                    return new SpigotScheduledTask(scheduler.runTaskTimerAsynchronously(plugin, task, initialDelayTicks, periodTicks));
            else {
                if (this.taskConsumer != null) {
                    scheduler.runTaskLaterAsynchronously(plugin, this::processBukkitTask, initialDelayTicks);
                    return null;
                } else return new SpigotScheduledTask(scheduler.runTaskLaterAsynchronously(plugin, task, initialDelayTicks));
            }
        }
        if (this.taskConsumer != null) {
            scheduler.runTaskAsynchronously(plugin, this::processBukkitTask);
            return null;
        } else return new SpigotScheduledTask(scheduler.runTaskAsynchronously(plugin, task));
    }
}
