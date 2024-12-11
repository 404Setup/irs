package one.tranic.irs.task;

public interface TaskImpl<C> {
    void cancel();

    boolean isCancelled();

    C getOwner();

    /**
     * Returns whether this task executes on a fixed period, as opposed to executing only once.
     *
     * @return whether this task executes on a fixed period, as opposed to executing only once.
     */
    boolean isRepeatingTask();

    /**
     * Returns true if the Task is a sync task.
     * <p>
     * In folia, it will always be false
     *
     * @return true if the task is run by main thread
     */
    boolean isSynchronized();
}
