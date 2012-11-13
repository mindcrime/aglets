package net.sourceforge.aglets.itinerary.helpers;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

import net.sourceforge.aglets.itinerary.Task;
import net.sourceforge.aglets.itinerary.TaskItinerary;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.event.MobilityEvent;
import com.ibm.aglet.event.MobilityListener;

/**
 * This class represents the base for the construction of a task-based
 * itinerary, that is an itinerary with task associated. Use this class to build
 * more complex task-based itinerary logics. This class keeps distinctions
 * between DEFAULT and non-default tasks. While the default tasks
 * (EXECUTE_ON_DEFAULT) must be executed always, indipendently from the
 * destination, the other tasks must be associated and executed depending on the
 * destination of the itinerary. The EXECUTE_ON_DEFAULT tasks have the priority
 * over other tasks, that means they are executed before the execution of the
 * destination associated tasks. <BR>
 * <B>Attention:</B><BR>
 * the execution policy imposes that default tasks are <U>ALWAYS</U> executed
 * before task associated to a specific destination.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * @version 1.0 13-set-2005
 */
public class BaseTaskItinerary extends BaseItinerary implements TaskItinerary,
Serializable, MobilityListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2552387718482640723L;

	/**
	 * This hashthable contains the task list for each destination. The key of
	 * the table is the URI string representing the destination, the value of
	 * the table is a Task[] that represent each single task that must be
	 * associated with such destination.
	 */
	protected Hashtable taskList = null;

	/**
	 * This vector contains all default tasks that must be executed
	 * indipendently from the destination.
	 */
	protected Vector defaultTasks = null;

	/**
	 * Indicates if the default tasks must be executed on the arrival to a
	 * destination.
	 */
	private boolean executeDefaultTaskOnArrival = true;

	/**
	 * Indicates if the default tasks must be executed on the dispatch to a
	 * destination.
	 */
	private boolean executeDefaultTaskOnDispatch = false;

	/**
	 * Indicates if the default tasks must be executed on the revertering.
	 */
	private boolean executedDefaultTaskOnReverting = false;

	/**
	 * Constructs the itinerary for the agent specified as owner. The size of
	 * the data structure are fixed to 10 elements.
	 * 
	 * @param owner
	 *            the agent owner of this itinerary.
	 */
	public BaseTaskItinerary(final Aglet owner) {
		super(owner);
		taskList = new Hashtable();
		defaultTasks = new Vector(10);
	}

	/**
	 * Adds a default task to the itinerary. A default task is executed on each
	 * destination of the itinerary, <b>only on arrival</b> of an agent.
	 * <B>Please note that the task will not be added to the list if it is
	 * already contained.</B>
	 * 
	 * @param toExecute
	 *            the task to execute on the arrival event
	 * @return true if the task has been added to the task list, false otherwise
	 */
	@Override
	public final boolean addDefaultTask(final Task toExecute) {
		// check params
		if ((toExecute == null)
				|| (toExecute.getExecutionType() != Task.EXECUTE_ON_DEFAULT)
				|| defaultTasks.contains(toExecute))
			return false;

		// add the task to the default task list
		defaultTasks.add(toExecute);
		return true;
	}

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
	@Override
	public boolean addNextDestination(final String destinationURI, final Task toExecute) {
		// the first step is to add the destination URI to the destination list,
		// if the
		// destination can be added then the task must be added to the task list

		if (this.addNextDestination(destinationURI) == true) {
			// destination added, add also the task to the task list
			return addTaskToDestination(destinationURI, toExecute);
		} else
			return false;
	}

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
	@Override
	public boolean addNextDestination(final String destinationURI, final Task[] toExecute) {
		// the first step is to add the destination to the destination list,
		// then if it has been added
		// each task is added to the task list for that destination

		if (this.addNextDestination(destinationURI)) {
			// destination added, adds also each task
			for (int i = 0; (toExecute != null) && (i < toExecute.length); i++)
				addTaskToDestination(destinationURI, toExecute[i]);

			return true;
		} else
			return false;
	}

	/**
	 * Adds another task to the specified destination.
	 * 
	 * @param destinationURI
	 *            the destination the task must be associated to
	 * @param toExecute
	 *            the task to execute
	 * @return true if the association is done, false otherwise
	 */
	@Override
	public boolean addTaskForDestination(final String destinationURI, final Task toExecute) {
		// check params (the internal method already does this check)
		if ((destinationURI == null) || destinationURI.equals("")
				|| (toExecute == null))
			return false;

		// add the task
		return addTaskToDestination(destinationURI, toExecute);
	}

	/**
	 * A service method used to add a task to the task list. This method checks
	 * if the task is a default task, in such case it adds the task to the
	 * default task list. Otherwise, it extract the array of tasks into the
	 * hashtable for such destination and adds the new task (append it) to the
	 * array.
	 * 
	 * @param destinationURI
	 *            the destination the task must be associated with.
	 * @param toExecute
	 *            the task to add for the above destination.
	 * @return true if the task is added, false otherwise.
	 */
	protected final boolean addTaskToDestination(
	                                             final String destinationURI,
	                                             final Task toExecute) {
		// check params
		if ((destinationURI == null) || destinationURI.equals("")
				|| (toExecute == null)) {
			return false;
		}

		// is the task a default one? Then add it to the default task list
		if (toExecute.getExecutionType() == Task.EXECUTE_ON_DEFAULT) {
			return addDefaultTask(toExecute);
		} else {
			// get the task array for the key
			final Task[] oldTasks = (Task[]) taskList.get(destinationURI);
			Task[] newTasks = null;

			if ((oldTasks == null) || (oldTasks.length == 0)) {
				// there are no tasks for this destination, add it
				newTasks = new Task[1];
				newTasks[0] = toExecute;
			} else {
				newTasks = new Task[oldTasks.length + 1];
				System.arraycopy(oldTasks, 0, newTasks, 0, oldTasks.length);
				newTasks[newTasks.length - 1] = toExecute;
			}

			// add the task list
			taskList.remove(destinationURI);
			taskList.put(destinationURI, newTasks);
			return true;
		}
	}

	/**
	 * The status of the execution of such tasks signed as EXECUTE_ON_DEFAULT
	 * for the onArrival event.
	 * 
	 * @return true if the task will be executed due to the onArrival event,
	 *         false otherwise.
	 */
	@Override
	public final boolean defaultTaskExecutionOnArrival() {
		return executeDefaultTaskOnArrival;
	}

	/**
	 * The status of the execution of such tasks signed as EXECUTE_ON_DEFAULT
	 * for the onDispatch event.
	 * 
	 * @return true if the stats must be executed on the onDispatch event, false
	 *         otherwise.
	 */
	@Override
	public final boolean defaultTaskExecutionOnDispatch() {
		return executeDefaultTaskOnDispatch;
	}

	/**
	 * The status of the task execution of such taska signed as
	 * EXECUTE_ON_DEFAULT for the onRevertering event.
	 * 
	 * @return true if the tasks must be executed on the onRevertering event,
	 *         false otherwise.
	 */
	@Override
	public final boolean defaultTaskExecutionOnRevertering() {
		return executedDefaultTaskOnReverting;
	}

	/**
	 * Sets the execution of tasks matched as EXECUTE_ON_DEFAULT on arrival.
	 * 
	 * @param enable
	 *            true if the tasks must be executed on the onArrival event,
	 *            false otherwise.
	 */
	@Override
	public final void enableDefaultTaskExecutionOnArrival(final boolean enable) {
		executeDefaultTaskOnArrival = enable;
	}

	/**
	 * Sets the execution of tasks matched as EXECUTE_ON_DEFAULT on dispatching.
	 * 
	 * @param enable
	 *            true if the tasks must be executed on the onDispatching event,
	 *            false otherwise.
	 */
	@Override
	public final void enableDefaultTaskExecutionOnDispatch(final boolean enable) {
		executeDefaultTaskOnDispatch = enable;
	}

	/**
	 * Sets the execution of tasks matched as EXECUTE_ON_DEFAULT on rectract.
	 * 
	 * @param enable
	 *            true if the tasks must be executed on the onRevertering event,
	 *            false otherwise.
	 */
	@Override
	public final void enableDefaultTaskExecutionOnRevertering(final boolean enable) {
		executedDefaultTaskOnReverting = enable;
	}

	/**
	 * A service method to execute all the tasks associated to a specific
	 * destination.
	 * 
	 * @param destinationURI
	 *            the destination to execute task for.
	 */
	protected void executeTaskForDestination(final String destinationURI) {
		final Task toExecute[] = (Task[]) taskList.get(destinationURI);

		for (int i = 0; (toExecute != null) && (i < toExecute.length); i++) {
			toExecute[i].execute();
		}
	}

	/**
	 * Provides the array of tasks associated with the specified destination.
	 * 
	 * @param destinationURI
	 *            the destination to check
	 * @return the array of tasks associated with such destination.
	 */
	@Override
	public Task[] getTasksForDestination(final String destinationURI) {
		// check params
		if ((destinationURI == null) || destinationURI.equals("")
				|| (taskList.containsKey(destinationURI) == false))
			return null;

		return (Task[]) taskList.get(destinationURI);
	}

	/**
	 * Method invoked when arriving to a destination. Executes all the required
	 * tasks.
	 */
	@Override
	public void onArrival(final MobilityEvent event) {
		// get the location I've arrived to
		final String currentDestination = getCurrentLocation();

		// check if I need to execute the default tasks for the onArrival
		if (defaultTaskExecutionOnArrival()) {
			// executes all tasks for the next destination

			for (int i = 0; i < defaultTasks.size(); i++) {
				final Task toExecute = (Task) defaultTasks.get(i);
				toExecute.execute();
			}
		}

		// now execute tasks for the destination
		executeTaskForDestination(currentDestination);
	}

	/**
	 * INvoked when the aglet is moving to a destination. Forces the execution
	 * of all tasks for that destination.
	 */
	@Override
	public void onDispatching(final MobilityEvent event) {
		// get the location I'm moving to
		final String currentDestination = getCurrentLocation();

		// check if I need to execute the default tasks for the onDispatching
		if (defaultTaskExecutionOnDispatch()) {
			// executes all tasks for the next destination
			for (int i = 0; i < defaultTasks.size(); i++) {
				final Task toExecute = (Task) defaultTasks.get(i);
				toExecute.execute();
			}
		}

		// now execute tasks for the destination
		executeTaskForDestination(currentDestination);

	}

	/**
	 * Invoked when the agent is coming back.
	 */
	@Override
	public void onReverting(final MobilityEvent event) {
		// get the location I'm moving to
		final String currentDestination = getCurrentLocation();

		// check if I need to execute the default tasks for the onDispatching
		if (defaultTaskExecutionOnRevertering()) {
			// executes all tasks for the next destination
			for (int i = 0; i < defaultTasks.size(); i++) {
				final Task toExecute = (Task) defaultTasks.get(i);
				toExecute.execute();
			}
		}

		// now execute tasks for the destination
		executeTaskForDestination(currentDestination);

	}

	/**
	 * Removes all tasks to be executed on a specified destination.
	 * 
	 * @param destinationURI
	 *            the destination to clean up
	 * @return true if all the tasks have been removed, false otherwise.
	 */
	@Override
	public boolean removeAllTaskForDestination(final String destinationURI) {
		// check params
		if ((destinationURI == null) || destinationURI.equals("")
				|| (taskList.containsKey(destinationURI) == false)) {
			return false;
		}

		// remove the key and the content from the task list
		taskList.remove(destinationURI);
		return true;
	}

	/**
	 * Removes a default task from the default task set.
	 * 
	 * @param toExecute
	 *            the task to be removed
	 * @return true if the task is removed, false otherwise (e.g., the task is
	 *         not contained in the list).
	 */
	@Override
	public final boolean removeDefaultTask(final Task toExecute) {
		// check params
		if ((toExecute == null)
				|| (toExecute.getExecutionType() != Task.EXECUTE_ON_DEFAULT)
				|| defaultTasks.isEmpty()
				|| (defaultTasks.contains(toExecute) == false))
			return false;

		// now remove the task from the task list
		defaultTasks.remove(toExecute);
		return true;
	}

	/**
	 * Removes a task from a destination.
	 * 
	 * @param destinationURI
	 *            the destination the task must be de-associated.
	 * @param toExecute
	 *            the task to remove
	 * @return true if the task has been removed, false otherwise.
	 */
	@Override
	public boolean removeTaskForDestination(
	                                        final String destinationURI,
	                                        final Task toExecute) {
		// check params
		if ((destinationURI == null) || destinationURI.equals("")
				|| (toExecute == null))
			return false;

		// remove the task
		return removeTaskFromDestination(destinationURI, toExecute);
	}

	/**
	 * A service method exploited to remove a task from the task array
	 * associated to a destination. The method checks if the task is a default
	 * one, in such case the task is removed from the task list of default
	 * tasks. Otherwise the task is removed from the destination list.
	 * 
	 * @param destinationURI
	 *            the destination from which the task must be removed.
	 * @param toExecute
	 *            the task to remove.
	 * @return true if the task has been removed, false otherwise.
	 */
	protected boolean removeTaskFromDestination(
	                                            final String destinationURI,
	                                            final Task toExecute) {
		// check params
		if ((destinationURI == null) || destinationURI.equals("")
				|| (taskList.containsKey(destinationURI) == false)
				|| (toExecute == null))
			return false;

		// if the task is a default one remove it from the default task list,
		// but this should never happen since
		// the destination URI is valid (i.e., contained in the tasklist).
		if (toExecute.getExecutionType() == Task.EXECUTE_ON_DEFAULT)
			return removeDefaultTask(toExecute);
		else {
			// remove the task from the task list
			final Task[] oldTasks = (Task[]) taskList.get(destinationURI);
			Task[] newTasks = null;

			// Warning: if the oldTasks has length == 1 I must to simply removed
			// the destination from
			// the list
			if ((oldTasks.length == 1) && oldTasks[0].equals(toExecute)) {
				taskList.remove(destinationURI);
				return true;
			} else if ((oldTasks.length == 1)
					&& (oldTasks[0].equals(toExecute) == false)) {
				return false;
			}

			if ((oldTasks == null) || (oldTasks.length == 0))
				return false;
			else {
				// iterate over each task in order to check if the task is
				// contained in the array, then
				// remove it
				newTasks = new Task[oldTasks.length - 1];

				int j = 0;
				boolean found = false; // indicates if the task is in the array

				for (int i = 0; i < oldTasks.length; i++) {

					if (!oldTasks[i].equals(toExecute)) {
						// this task is not the same that must be removed, copy
						// it
						if (j < newTasks.length) {
							newTasks[j] = oldTasks[i];
							j++;
						}
					} else
						found = true;
				}

				// now if the task has been found then I can substitute the task
				// array, otherwise no
				if (found) {
					taskList.remove(destinationURI);
					taskList.put(destinationURI, newTasks);
					return true;
				} else
					return false;

			}
		}
	}

	/**
	 * Display information about this itinerary.
	 */
	@Override
	public String toString() {
		// build a string with all the destinations
		final StringBuffer head = new StringBuffer();
		head.append(this.getClass().getName() + " owned by " + agletOwner);
		head.append(" - " + destinations.size() + " destinations - ");
		head.append("" + getRemainingDestinationCount()
				+ " destinations remaining.");

		final StringBuffer ret = new StringBuffer();
		ret.append(head);
		ret.append("\n");

		// now append each task for each destination
		for (int i = 0; i < destinations.size(); i++) {
			final String currentDestination = (String) destinations.get(i);
			final Task[] tasks = (Task[]) taskList.get(currentDestination);
			ret.append((i + 1) + ") " + currentDestination + "\n");

			for (int j = 0; (tasks != null) && (j < tasks.length); j++) {
				ret.append("\t" + tasks[j] + "\n");
			}
		}

		// now append the default tasks
		for (int i = 0; i < defaultTasks.size(); i++) {
			ret.append("\n(default task) " + defaultTasks.get(i));
		}

		// now show the execution policy
		ret.append("\n\nExecution policy for default tasks:\n");
		ret.append("\texecute on arrival       = "
				+ executeDefaultTaskOnArrival);
		ret.append("\n\texecute on dispatch    = "
				+ executeDefaultTaskOnDispatch);
		ret.append("\n\texecute on revertering = "
				+ executedDefaultTaskOnReverting);

		return ret.toString();
	}

}
