package net.sourceforge.aglets.itinerary.helpers;

import java.io.Serializable;

import net.sourceforge.aglets.itinerary.Task;

/**
 * This class represents a base implementation for the Task interface, it's a
 * kind of <i>Task adapter</i>.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net 1-ott-2005
 * @version 1.0
 */
public class BaseTask implements Task, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1304922928844500937L;

	/**
	 * The execution policy for this task.
	 */
	private final int executionPolicy;

	/**
	 * A description of the task.
	 */
	private String description = null;

	/**
	 * Constructs the task with the specified policy, that is a value of the
	 * execution as specified in the Task interface.
	 * 
	 * @param policy
	 *            the policy to adopt.
	 */
	public BaseTask(final int policy) {
		super();
		executionPolicy = policy;
		description = "(No description available)";
	}

	/**
	 * Constructs the task with the specified policy and the description.
	 * 
	 * @param policy
	 *            the policy for the task execution.
	 * @param description
	 *            the description of this task.
	 */
	public BaseTask(final int policy, final String description) {
		this(policy);
		this.description = description;
	}

	/**
	 * Executes nothing!
	 */
	@Override
	public void execute() {

	}

	/**
	 * Returns the execution type for this task.
	 * 
	 * @return the execution type as specified in the Task interface.
	 */
	@Override
	public int getExecutionType() {
		return executionPolicy;
	}

	@Override
	public String toString() {
		return description + " (execution policy = "
		+ executionPolicy + ")";
	}
}
