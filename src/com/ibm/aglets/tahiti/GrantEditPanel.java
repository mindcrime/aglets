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

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.Font;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;

import java.io.File;
import java.io.IOException;

import java.security.Policy;
import com.ibm.aglets.security.PolicyDB;
import com.ibm.aglets.security.PolicyFileReader;
import com.ibm.aglets.security.PolicyFileWriter;
import com.ibm.aglets.security.PolicyGrant;
import com.ibm.aglets.security.PolicyPermission;
import com.ibm.aglets.security.PolicyFileParsingException;

import com.ibm.awb.misc.URIPattern;
import com.ibm.awb.misc.MalformedURIPatternException;
import com.ibm.awb.misc.Resource;
import com.ibm.aglets.tahiti.utils.*;
import javax.swing.*;

/**
 * Converted from AWT to SWING
 *
 */
class GrantEditPanel extends EditListPanel {
	SecurityConfigDialog _dialog = null;

	GrantEditPanel(SecurityConfigDialog dialog, AgentListPanel list, Editor editor) {
		super(null, list, editor);
		_dialog = dialog;
	}
	
	protected void addItemIntoList(String item) {
		_dialog.addGrantPanel(item);
		selectItem(item);
		_dialog.showGrantPanel(item);
	}
	
	protected void removeItemFromList() {
		final int idx = getSelectedIndex();
		final String item = getSelectedItem();

		super.removeItemFromList();
		_dialog.removeGrantPanel(idx, item);
		_dialog.showGrantPanel(item);
	}
}
