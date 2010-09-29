package com.ibm.agletx.util;

/*
 * @(#)Task.java
 * 
 * IBM Confidential-Restricted
 * 
 * OCO Source Materials
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */

import java.io.Serializable;

/**
 * A class which defines a task to be performed during a sequential itinerary.
 * 
 * @version 1.20 $Date: 2009/07/28 07:04:53 $
 * @author Yariv Aridor
 * @see SeqItinerary
 */

public abstract class Task implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -4016885502125593520L;

    abstract public void execute(SeqItinerary itin) throws Exception;
}
