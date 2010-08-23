package com.ibm.aglets.tahiti;

import java.awt.Component;
import java.util.Locale;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import org.aglets.util.AgletsTranslator;

import com.ibm.aglets.security.PolicyGrant;
import com.ibm.aglets.security.PolicyPermission;

public class PolicyGrantPermissionRenderer extends DefaultListCellRenderer {

    /**
     * The translator of this class.
     */
    private static AgletsTranslator translator = null;

    /**
     * The class name of this object.
     */
    private String baseKey = this.getClass().getName();

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
	    codeBaseString = translator.translate(this.baseKey + ".codeSource");

	if (ownedByString == null)
	    ownedByString = translator.translate(this.baseKey + ".ownedBy");

	if (signedByString == null)
	    signedByString = translator.translate(this.baseKey + ".signedBy");

	if (counterString == null)
	    counterString = translator.translate(this.baseKey + ".counter");

	if (actionString == null)
	    actionString = translator.translate(this.baseKey + ".action");

	if (codebaseString == null)
	    codebaseString = translator.translate(this.baseKey + ".codebase");

	if (objectiveString == null)
	    objectiveString = translator.translate(this.baseKey + ".objective");

	if (signerString == null)
	    signerString = translator.translate(this.baseKey + ".signer");

    }

    @Override
    public Component getListCellRendererComponent(
                                                  JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {

	// create a JLabel for the component to show
	JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

	if (value instanceof PolicyGrant) {
	    StringBuffer buffer = new StringBuffer(50);
	    PolicyGrant grant = (PolicyGrant) value;

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

	    int permissionCount = grant.getPermissionCount();
	    if (permissionCount > 0) {
		buffer.append(counterString);
		buffer.append(" ");
		buffer.append(permissionCount);
	    }

	    label.setText(buffer.toString());
	} else if (value instanceof PolicyPermission) {
	    PolicyPermission permission = (PolicyPermission) value;
	    StringBuffer buffer = new StringBuffer(100);

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
