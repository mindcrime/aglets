/*
 * Created on Oct 1, 2004
 *
 * @author Luca Ferrari, fluca1978@virgilio.it
 */
package com.ibm.aglets.tahiti;
import java.util.ResourceBundle;

import javax.swing.*;
import com.ibm.aglets.tahiti.utils.*;

/**
 * A window to show the memory amount.
 * @author Luca Ferrari <A HREF="mailto:cat4hire@users.sourceforge.net">cat4hire@users.sourceforge.net</A>
 */
public class MemoryWindow extends JFrame{
    private MemoryPanel mem;
    
    public MemoryWindow(){
        super();
        this.mem = new MemoryPanel(250,250,false,true);
        // use the resource bundle of the memory panel
        ResourceBundle bundle = MemoryPanel.bundle;
        this.setTitle(bundle.getString("window.memory.title"));
        this.getContentPane().add(this.mem);
        this.pack();
        this.mem.startThread();
        this.setVisible(true);

    }
    
    
}
