package net.sourceforge.aglets.itinerary.helpers;


import java.io.Serializable;

import net.sourceforge.aglets.itinerary.Task;

/**
 * This class represents a base implementation for the Task interface, it's a kind of <i>Task adapter</i>.
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 1-ott-2005
 * @varsion 1.0
 */
public class BaseTask implements Task,Serializable {
	
	/**
	 * The execution policy for this task.
	 */
	private int executionPolicy;
	
	/**
	 * A description of the task.
	 */
	private String description = null;
	
	/**
	 * Constructs the task with the specified policy, that is a value of the execution as specified in
	 * the Task interface.
	 * @param policy the policy to adopt.
	 * @see #net.sourceforge.aglets.itinerary.Task
	 */
	public BaseTask(int policy){
		super();
		this.executionPolicy = policy;
		this.description = "(No description available)";
	}
	
	
	/**
	 * Constructs the task with the specified policy and the description.
	 * @param policy the policy for the task execution.
	 * @param description the description of this task.
	 */
	public BaseTask(int policy, String description){
		this(policy);
		this.description = description;
	}
	
	/**
	 * Executes nothing!
	 */
	public void execute(){
		
	}
	
	/**
	 * Returns the execution type for this task.
	 * @return the execution type as specified in the Task interface.
	 */
	public int getExecutionType(){
		return this.executionPolicy;
	}

	
	
	public String toString(){
		return this.description+" (execution policy = "+this.executionPolicy+")";
	}
}
