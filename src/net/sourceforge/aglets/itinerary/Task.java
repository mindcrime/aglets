package net.sourceforge.aglets.itinerary;

/**
 * This interface defines a single task. A task is a code unit that must be
 * executed depending on the destination.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * @version 1.0 13-set-2005
 */
public interface Task {

    /**
     * Identify a task to be executed on the aglet arrival.
     */
    public int EXECUTE_ON_ARRIVAL = 1;

    /**
     * Identify a task as to be executed on the aglet migration (dispatch).
     */
    public int EXECUTE_ON_DISPATCH = 2;

    /**
     * Identify a task as to be executed on retracting of an aglet.
     */
    public int EXECUTE_ON_REVERTERING = 4;

    /**
     * Identify a task as to be executed on default, that means always.
     */
    public int EXECUTE_ON_DEFAULT = 6;

    /**
     * The main method of a task. Place your code to be executed here, this
     * method will be invoked automatically at the right moment.
     */
    public void execute();

    /**
     * Indicates the type of execution task, that is the policy this task should
     * be executed.
     * 
     * @return the type of task.
     */
    public int getExecutionType();
}
