package com.ibm.aglets.tahiti;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.JFrame;

import net.sourceforge.aglets.log.AgletsLogger;
import net.sourceforge.aglets.util.AgletsTranslator;
import net.sourceforge.aglets.util.gui.GUICommandStrings;

public class BaseAgletsDialog extends JDialog implements ActionListener {

    /**
     * The aglet translator for this window.
     */
    protected AgletsTranslator translator = AgletsTranslator.getInstance("tahiti", Locale.getDefault());
    /**
     * The base key for this dialog window.
     */
    protected String baseKey = this.getClass().getName();
    /**
     * The logger for this class.
     */
    protected AgletsLogger logger = AgletsLogger.getLogger(this.getClass().getName());

    public BaseAgletsDialog() {
	super();
    }

    /**
     * Gets back the baseKey.
     * 
     * @return the baseKey
     */
    public final synchronized String getBaseKey() {
	return this.baseKey;
    }

    /**
     * Manages the events from the buttons. At the moment it manages only the
     * cancel event. You should override this method in order to get advantage
     * of your own method and window control.
     */
    @Override
    public void actionPerformed(ActionEvent event) {
	// check params
	if (event == null)
	    return;

	String command = event.getActionCommand();

	if (GUICommandStrings.CANCEL_COMMAND.equals(command)
		|| GUICommandStrings.CLOSE_COMMAND.equals(command)) {
	    // cancel
	    this.setVisible(false);
	    this.dispose();
	} else if (GUICommandStrings.OK_COMMAND.equals(command)) {
	    // ok pressed, for not do nothing
	    this.setVisible(true);
	    this.dispose();
	}

    }

    public BaseAgletsDialog(JFrame parentFrame) {
	super(parentFrame);
    }

    /**
     * Provides the translator used for this Tahiti object.
     * 
     * @return the Aglets Translator object
     */
    protected final AgletsTranslator getAgletsTranslator() {
	return this.translator;
    }

}