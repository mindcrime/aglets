package com.ibm.aglets.tahiti;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.sourceforge.aglets.util.AgletsTranslator;
import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

/**
 * A panel that contains a couple of buttons for the generic ok/cancel
 * operations.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 *         03/ott/07
 */
public class OkCancelButtonPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 315108948949142575L;

	private static AgletsTranslator translator = AgletsTranslator.getInstance("tahiti", Locale.getDefault());

	private JButton okButton = null;
	private JButton cancelButton = null;

	public OkCancelButtonPanel(String okKey, String cancelKey,
	                           final ActionListener listener) {
		super();
		setLayout(new FlowLayout(FlowLayout.RIGHT));

		if ((okKey == null) || (okKey.length() == 0)
				|| okKey.equals(translator.translate(okKey)))
			okKey = JComponentBuilder.OK_BUTTON_KEY;

		if ((cancelKey == null) || (cancelKey.length() == 0)
				|| cancelKey.equals(translator.translate(cancelKey)))
			cancelKey = JComponentBuilder.CANCEL_BUTTON_KEY;

		okButton = JComponentBuilder.createJButton(okKey, GUICommandStrings.OK_COMMAND, listener);
		cancelButton = JComponentBuilder.createJButton(cancelKey, GUICommandStrings.CANCEL_COMMAND, listener);

		this.add(okButton);
		this.add(cancelButton);

	}

	/**
	 * Gets back the cancelButton.
	 * 
	 * @return the cancelButton
	 */
	public final JButton getCancelButton() {
		return cancelButton;
	}

	/**
	 * Gets back the okButton.
	 * 
	 * @return the okButton
	 */
	public final JButton getOkButton() {
		return okButton;
	}

}
