package com.ibm.aglets.tahiti;

/*
 * @(#)DeactivateAgletDialog.java
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

import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.AgletProxy;

import com.ibm.aglets.*;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class DeactivateAgletDialog is the dialog for deactivating an Aglet.
 * 
 * @version     1.02    $Date: 2001/07/28 06:32:32 $
 * @author	Mitsuru Oshima
 */

final class DeactivateAgletDialog extends TahitiDialog 
	implements ActionListener {

	/*
	 * The proxy to be deactivated
	 */
	private AgletProxy proxy = null;

	/*
	 * GUI components
	 */
	private TextField _time = new TextField(10);

	/*
	 * Constructs a new Aglet dispatch dialog.
	 */
	DeactivateAgletDialog(MainWindow parent, AgletProxy proxy) {
		super(parent, "Deactivate", false);
		this.proxy = proxy;

		add("Center", makePanel());

		addButton("Deactivate", this);
		addCloseButton("Cancel");
		pack();
	}
	/*
	 * Creates an Aglet dispatch dialog.
	 */
	public void actionPerformed(ActionEvent ev) {
		if (proxy == null) {
			return;
		} 
		if (!"".equals(_time.getText())) {
			long time = Integer.parseInt(_time.getText());

			getMainWindow().deactivateAglet(proxy, time);
			dispose();
			return;
		} 
		System.out.print("\007");
		System.out.flush();
	}
	/*
	 * Layouts all Components
	 */
	protected GridBagPanel makePanel() {
		GridBagPanel p = new GridBagPanel();

		GridBagConstraints cns = new GridBagConstraints();

		cns.insets = new Insets(5, 5, 5, 5);
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.weightx = 1.0;

		/*
		 * Aglet name
		 */
		String agletname = "Invalid Aglet";

		try {
			agletname = (proxy == null ? "No Aglet" 
						 : proxy.getAgletClassName());
		} catch (InvalidAgletException ex) {}
		p.add(new Label(agletname, Label.CENTER), cns);

		/*
		 * Time to sleep
		 */
		cns.gridwidth = 1;
		cns.weightx = 0.0;
		p.add(new Label("Time to sleep (seconds)"));

		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.weightx = 1.0;
		p.add(_time);

		_time.addActionListener(this);
		_time.setText("0");

		return p;
	}
}
