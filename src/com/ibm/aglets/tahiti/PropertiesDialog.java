package com.ibm.aglets.tahiti;

/*
 * @(#)PropertiesDialog.java
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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.List;
import java.security.cert.X509Certificate;
import java.util.Date;

import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;

/**
 * Class PropertiesDialog is used to view the aglet properties.
 * 
 * @version 1.02 96/07/2
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 */

final class PropertiesDialog extends TahitiDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3282190391091423057L;
	/*
	 * Labels to display the properties
	 */
	private final Label _identity = new Label("");
	private final Label _date = new Label("");
	private final Label _name = new Label("");
	private final Label _owner = new Label("");

	// # private Label _cloning = new Label("");
	// # private Label _hops = new Label("");
	// # private Label _lifetime = new Label("");

	private final Label _codebase = new Label("");
	private final Label _version = new Label("");
	private final List _from = new List(3, false);

	// - private String subtitle = "";

	/*
	 * Constructs a new Aglet dispatch dialog.
	 */
	public PropertiesDialog(final MainWindow parent, final AgletProxy proxy) {
		super(parent, "Aglet Info", false);
		try {
			final AgletInfo info = proxy.getAgletInfo();

			_identity.setText(info.getAgletID().toString());
			_date.setText(new Date(info.getCreationTime()).toString());
			_name.setText(info.getAgletClassName());
			_codebase.setText((info.getCodeBase() == null) ? "Local host"
					: info.getCodeBase().toString());
			final X509Certificate ownerCert = (X509Certificate) info.getAuthorityCertificate();
			String ownerName;

			if (ownerCert == null) {
				ownerName = "(Unknown Owner)";
			} else {
				ownerName = ownerCert.getSubjectDN().getName();
			}
			_owner.setText(ownerName);

			// # Allowance allowance = proxy.getAllowance();
			// # _cloning.setText(allowance.getRoomCloningString());
			// # _hops.setText(allowance.getRoomHopsString());
			// # _lifetime.setText(allowance.getLifeTimeString(false, null));
			_version.setText(info.getAPIMajorVersion() + "."
					+ info.getAPIMinorVersion());

			// - subtitle = info.getPrivilegeName();
		} catch (final InvalidAgletException ae) {
			_identity.setText("Invalid Aglet");

			// - subtitle = "Invalid Aglet";
		}
		makePanel();
		addCloseButton(null);
	}

	/*
	 * Creates this panel
	 */
	void makePanel() {
		final GridBagPanel p = new GridBagPanel();

		this.add("Center", p);

		final GridBagConstraints cns = new GridBagConstraints();

		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.fill = GridBagConstraints.BOTH;
		cns.weightx = 1.0;
		cns.weighty = 1.0;
		cns.ipadx = cns.ipady = 5;
		cns.insets = new Insets(5, 5, 5, 5);

		MyPanel my_panel =

			// - new MyPanel("Aglet Instance Information:" + subtitle);
			new MyPanel("Aglet Instance Information:");

		p.add(my_panel, cns);

		// Instance
		my_panel.makeLabeledComponent("Identity", _identity);
		my_panel.makeLabeledComponent("Owner Id", _owner);
		my_panel.makeLabeledComponent("Creation Date", _date);

		// # my_panel.makeLabeledComponent("Available Room of Cloning",
		// _cloning);
		// # my_panel.makeLabeledComponent("Available Room of Hops", _hops);
		// # my_panel.makeLabeledComponent("Lifetime", _lifetime);

		my_panel = new MyPanel("Aglet Class Information:");
		p.add(my_panel, cns);

		// Class Information
		my_panel.makeLabeledComponent("Class Name", _name);
		my_panel.makeLabeledComponent("Code Base", _codebase);
		my_panel.makeLabeledComponent("Version", _version);
	}
}
