package com.ibm.aglets.tahiti;

/*
 * @(#)SecurityConfigDialog.java
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

abstract class PermissionEditor extends PermissionPanel implements Editor {
    /**
     * 
     */
    private static final long serialVersionUID = -3594477053931804574L;

    @Override
    abstract public String getText();

    @Override
    abstract public void setText(String text);
}
