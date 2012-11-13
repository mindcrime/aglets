package com.ibm.aglets.tahiti;

import java.awt.Component;
import java.util.Locale;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import net.sourceforge.aglets.util.AgletsTranslator;

import com.ibm.aglets.security.PolicyGrant;
import com.ibm.aglets.security.PolicyPermission;

public class PolicyGrantPermissionRenderer extends DefaultListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9076261216157049609L;

	/**
	 * The translator of this class.
	 */
	private static AgletsTranslator translator = null;

	/**
	 * The class name of this object.
	 */
	private final String baseKey = this.getClass().getName();

	/**
	 * Statis strings to accelerate the lookup for translation.
	 */
	private static String codeBaseString = null;
	private static String ownedByString = null;
	private static String signedByString = null;
	private static String counterString = null;
	private static String actionString = null;
	private static String codebaseString = null;
	private static String objectiveString = null;
	private static String signerString = null;

	public PolicyGrantPermissionRenderer() {
		super();

		if (translator == null)
			translator = AgletsTranslator.getInstance("tahiti", Locale.getDefault());

		if (codeBaseString == null)
			codeBaseString = translator.translate(baseKey + ".codeSource");

		if (ownedByString == null)
			ownedByString = translator.translate(baseKey + ".ownedBy");

		if (signedByString == null)
			signedByString = translator.translate(baseKey + ".signedBy");

		if (counterString == null)
			counterString = translator.translate(baseKey + ".counter");

		if (actionString == null)
			actionString = translator.translate(baseKey + ".action");

		if (codebaseString == null)
			codebaseString = translator.translate(baseKey + ".codebase");

		if (objectiveString == null)
			objectiveString = translator.translate(baseKey + ".objective");

		if (signerString == null)
			signerString = translator.translate(baseKey + ".signer");

	}

	@Override
	public Component getListCellRendererComponent(
	                                              final JList list,
	                                              final Object value,
	                                              final int index,
	                                              final boolean isSelected,
	                                              final boolean cellHasFocus) {

		// create a JLabel for the component to show
		final JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		if (value instanceof PolicyGrant) {
			final StringBuffer buffer = new StringBuffer(50);
			final PolicyGrant grant = (PolicyGrant) value;

			buffer.append(codeBaseString);
			buffer.append(" ");
			buffer.append(grant.getCodeBase().toString());
			buffer.append("\t  ");
			buffer.append(ownedByString);
			buffer.append(" ");
			buffer.append(grant.getOwners());
			buffer.append("\t  ");
			buffer.append(signedByString);
			buffer.append(" ");
			buffer.append(grant.getSignerNames());
			buffer.append(" ");
			buffer.append(" ");

			final int permissionCount = grant.getPermissionCount();
			if (permissionCount > 0) {
				buffer.append(counterString);
				buffer.append(" ");
				buffer.append(permissionCount);
			}

			label.setText(buffer.toString());
		} else if (value instanceof PolicyPermission) {
			final PolicyPermission permission = (PolicyPermission) value;
			final StringBuffer buffer = new StringBuffer(100);

			buffer.append(actionString);
			buffer.append(" ");
			buffer.append(permission.getActions());
			buffer.append(" - ");
			buffer.append(objectiveString);
			buffer.append(" ");
			buffer.append(permission.getTargetName());
			buffer.append(" - ");
			buffer.append(objectiveString);
			buffer.append(" ");
			buffer.append(permission.getTargetName());

			// TODO completare qui

		}

		return label;
	}
}
