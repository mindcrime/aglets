package net.sourceforge.aglets.itinerary;

/**
 * This interface defines an itinerary with task associated to each destination.
 * Once the destination is reached the related task is executed.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * @version 1.0 13-set-2005
 */
public interface TaskItinerary extends Itinerary {

	/**
	 * Adds a default task to the itinerary. A default task is executed on each
	 * destination of the itinerary, <b>only on arrival</b> of an agent.
	 * 
	 * @param toExecute
	 *            the task to execute on the arrival event
	 * @return true if the task has been added to the task list, false otherwise
	 */
	public boolean addDefaultTask(Task toExecute);

	/**
	 * Adds a couple <I>destination, task</i>, that means the destination will
	 * be added to the itinerary, and the task will be executed depending on the
	 * specified type
	 * 
	 * @param destinationURI
	 *            the destination of the itinerary
	 * @param toExecute
	 *            the task to execute
	 * @return true if the task has been added, false otherwise
	 */
	public boolean addNextDestination(String destinationURI, Task toExecute);

	/**
	 * Adds a destination to the itinerary and several tasks to execute on such
	 * destination.
	 * 
	 * @param destinationURI
	 *            the destination of the itinerary
	 * @param toExecute
	 *            the array of task to be executed (depending on the policy of
	 *            the tasks).
	 * @return true if the destination is added, false otherwise
	 */
	public boolean addNextDestination(String destinationURI, Task[] toExecute);

	/**
	 * Adds another task to the specified destination.
	 * 
	 * @param destinationURI
	 *            the destination the task must be associated to
	 * @param toExecute
	 *            the task to execute
	 * @return true if the association is done, false otherwise
	 */
	public boolean addTaskForDestination(String destinationURI, Task toExecute);

	/**
	 * The status of the execution of such tasks signed as EXECUTE_ON_DEFAULT
	 * for the onArrival event.
	 * 
	 * @return true if the task will be executed due to the onArrival event,
	 *         false otherwise.
	 */
	public boolean defaultTaskExecutionOnArrival();

	/**
	 * The status of the execution of such tasks signed as EXECUTE_ON_DEFAULT
	 * for the onDispatch event.
	 * 
	 * @return true if the stats must be executed on the onDispatch event, false
	 *         otherwise.
	 */
	public boolean defaultTaskExecutionOnDispatch();

	/**
	 * The status of the task execution of such taska signed as
	 * EXECUTE_ON_DEFAULT for the onRevertering event.
	 * 
	 * @return true if the tasks must be executed on the onRevertering event,
	 *         false otherwise.
	 */
	public boolean defaultTaskExecutionOnRevertering();

	/**
	 * Sets the execution of tasks matched as EXECUTE_ON_DEFAULT on arrival.
	 * 
	 * @param enable
	 *            true if the tasks must be executed on the onArrival event,
	 *            false otherwise.
	 */
	public void enableDefaultTaskExecutionOnArrival(boolean enable);

	/**
	 * Sets the execution of tasks matched as EXECUTE_ON_DEFAULT on dispatching.
	 * 
	 * @param enable
	 *            true if the tasks must be executed on the onDispatching event,
	 *            false otherwise.
	 */
	public void enableDefaultTaskExecutionOnDispatch(boolean enable);

	/**
	 * Sets the execution of tasks matched as EXECUTE_ON_DEFAULT on rectract.
	 * 
	 * @param enable
	 *            true if the tasks must be executed on the onRevertering event,
	 *            false otherwise.
	 */
	public void enableDefaultTaskExecutionOnRevertering(boolean enable);

	/**
	 * Provides the array of tasks associated with the specified destination.
	 * 
	 * @param destinationURI
	 *            the destination to check
	 * @return the array of tasks associated with such destination.
	 */
	public Task[] getTasksForDestination(String destinationURI);

	/**
	 * Removes all tasks to be executed on a specified destination.
	 * 
	 * @param destinationURI
	 *            the destination to clean up
	 * @return true if all the tasks have been removed, false otherwise.
	 */
	public boolean removeAllTaskForDestination(String destinationURI);

	/**
	 * Removes a default task from the default task set.
	 * 
	 * @param toExecute
	 *            the task to be removed
	 * @return true if the task is removed, false otherwise (e.g., the task is
	 *         not contained in the list).
	 */
	public boolean removeDefaultTask(Task toExecute);

	/**
	 * Removes a task from a destination.
	 * 
	 * @param destinationURI
	 *            the destination the task must be de-associated.
	 * @param toExecute
	 *            the task to remove
	 * @return true if the task has been removed, false otherwise.
	 */
	public boolean removeTaskForDestination(
	                                        String destinationURI,
	                                        Task toExecute);
}
