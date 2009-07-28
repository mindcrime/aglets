package com.ibm.aglets.tahiti;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.aglets.util.gui.GUICommandStrings;
import org.aglets.util.gui.JComponentBuilder;
import org.aglets.util.*;

import java.util.*;

/**
 * A panel that contains a couple of buttons for the generic ok/cancel operations.
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 *
 * 03/ott/07
 */
public class OkCancelButtonPanel extends JPanel {

    private static AgletsTranslator translator = AgletsTranslator.getInstance("tahiti", Locale.getDefault());
    
    private JButton okButton = null;
    private JButton cancelButton = null;
    
    public OkCancelButtonPanel(String okKey, String cancelKey, ActionListener listener){
	super();
	this.setLayout(new FlowLayout(FlowLayout.RIGHT));
	
	if( okKey == null || okKey.length() == 0 || okKey.equals(translator.translate(okKey)) ) 
	    okKey = JComponentBuilder.OK_BUTTON_KEY;
	
	if( cancelKey == null || cancelKey.length() == 0 || cancelKey.equals(translator.translate(cancelKey)) )
	    cancelKey = JComponentBuilder.CANCEL_BUTTON_KEY;
	
	okButton = JComponentBuilder.createJButton(okKey, GUICommandStrings.OK_COMMAND, listener);
	cancelButton = JComponentBuilder.createJButton(cancelKey, GUICommandStrings.CANCEL_COMMAND, listener);
	
	this.add(okButton);
	this.add(cancelButton);

    }

    /**
     * Gets back the cancelButton.
     * @return the cancelButton
     */
    public final JButton getCancelButton() {
        return cancelButton;
    }

    /**
     * Gets back the okButton.
     * @return the okButton
     */
    public final JButton getOkButton() {
        return okButton;
    }
    
    
}
