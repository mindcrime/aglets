package com.ibm.aglets.tahiti;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.aglets.util.gui.GUICommandStrings;
import org.aglets.util.gui.ImagePanel;
import org.aglets.util.gui.JComponentBuilder;

import com.ibm.aglets.AgletRuntime;

/**
 * A dialog that shows the general information about the project: the logo, the
 * name, the version, the credits and the licence.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 *         14/nov/07
 */
public class AboutDialog extends BaseAgletsDialog {

    /**
     * The panel that will contain the image with the logo.
     */
    private ImagePanel image = null;

    /**
     * A text area that will contain the credits.
     */
    private JTextArea creditArea = null;

    /**
     * A text area with the licence.
     */
    private JTextArea licenceArea = null;

    /**
     * An instance to myself.
     */
    private static AboutDialog mySelf = null;

    protected AboutDialog(JFrame parentFrame) {
	super(parentFrame);

	// create the logo
	this.image = JComponentBuilder.createLogoPanel();

	// set the layout
	this.setLayout(new BorderLayout(10, 10));
	JPanel northPanel = new JPanel();
	northPanel.setLayout(new GridLayout(0, 1));

	JLabel label = JComponentBuilder.createJLabel("Aglets");
	northPanel.add(label);
	label = JComponentBuilder.createJLabel(this.baseKey + ".version");
	label.setText(label.getText() + " " + AgletRuntime.getVersion());
	northPanel.add(label);
	this.add(northPanel, BorderLayout.NORTH);

	JPanel southPanel = new JPanel();
	southPanel.setLayout(new BorderLayout());
	label = JComponentBuilder.createJLabel(this.baseKey + ".webPage");
	southPanel.add(label, BorderLayout.NORTH);

	this.creditArea = JComponentBuilder.createJTextArea(this.translator.translate(this.baseKey + ".creditsFile"));
	this.creditArea.setEditable(false);
	JTabbedPane tabbedPanel = new JTabbedPane();

	JPanel creditPanel = new JPanel();
	creditPanel.add(this.creditArea);

	JPanel licencePanel = new JPanel();
	this.licenceArea = JComponentBuilder.createJTextArea(this.translator.translate(this.baseKey
		+ ".licenceFile"));
	licencePanel.add(this.licenceArea);

	tabbedPanel.add(this.translator.translate(this.baseKey + ".credits"), creditPanel);
	tabbedPanel.add(this.translator.translate(this.baseKey + ".licence"), licencePanel);
	southPanel.add(tabbedPanel, BorderLayout.CENTER);

	JButton closeButton = JComponentBuilder.createJButton(this.baseKey
		+ ".okButton", GUICommandStrings.OK_COMMAND, this);
	southPanel.add(closeButton, BorderLayout.SOUTH);

	this.add(northPanel, BorderLayout.NORTH);
	this.add(this.image, BorderLayout.CENTER);
	this.add(southPanel, BorderLayout.SOUTH);

	this.pack();

    }

    /**
     * Provides an instance of this dialog window.
     * 
     * @param parentFrame
     *            the frame parent of this dialog
     * @return the instance of the dialog
     */
    public synchronized static AboutDialog getInstance(JFrame parentFrame) {
	if (mySelf == null)
	    mySelf = new AboutDialog(parentFrame);

	return mySelf;
    }
}
