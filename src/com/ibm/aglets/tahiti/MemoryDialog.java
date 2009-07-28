package com.ibm.aglets.tahiti;

import java.awt.BorderLayout;
import java.security.acl.Owner;

/**
 * A dialog that shows the memory usage for the current platform.
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 *
 * 19/nov/07
 */
public class MemoryDialog extends TahitiDialog {

    /**
     * The memory panel for this dialog.
     */
    private MemoryPanel memory = null;
    
   
    public MemoryDialog(MainWindow owner){
	super(owner);
	
	this.memory = new MemoryPanel(200,100,true, false);
	this.contentPanel.add( this.memory, BorderLayout.CENTER);
	
	this.pack();
    }
}
