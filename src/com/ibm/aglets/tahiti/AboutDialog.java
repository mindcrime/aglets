package com.ibm.aglets.tahiti;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.ImagePanel;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

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
	 * 
	 */
	private static final long serialVersionUID = 6317104289078288226L;

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

	/**
	 * Provides an instance of this dialog window.
	 * 
	 * @param parentFrame
	 *            the frame parent of this dialog
	 * @return the instance of the dialog
	 */
	public synchronized static AboutDialog getInstance(final JFrame parentFrame) {
		if (mySelf == null)
			mySelf = new AboutDialog(parentFrame);

		return mySelf;
	}

	protected AboutDialog(final JFrame parentFrame) {
		super(parentFrame);

		// create the logo
		image = JComponentBuilder.createLogoPanel();

		// set the layout
		setLayout(new BorderLayout(10, 10));
		final JPanel northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(0, 1));

		JLabel label = JComponentBuilder.createJLabel("Aglets");
		northPanel.add(label);
		label = JComponentBuilder.createJLabel(baseKey + ".version");
		label.setText(label.getText() + " " + AgletRuntime.getVersion());
		northPanel.add(label);
		this.add(northPanel, BorderLayout.NORTH);

		final JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		label = JComponentBuilder.createJLabel(baseKey + ".webPage");
		southPanel.add(label, BorderLayout.NORTH);

		creditArea = JComponentBuilder.createJTextArea(translator.translate(baseKey
				+ ".creditsFile"));
		creditArea.setEditable(false);
		final JTabbedPane tabbedPanel = new JTabbedPane();

		final JPanel creditPanel = new JPanel();
		creditPanel.add(creditArea);

		final JPanel licencePanel = new JPanel();
		licenceArea = JComponentBuilder.createJTextArea(translator.translate(baseKey
				+ ".licenceFile"));
		licencePanel.add(licenceArea);

		tabbedPanel.add(translator.translate(baseKey + ".credits"), creditPanel);
		tabbedPanel.add(translator.translate(baseKey + ".licence"), licencePanel);
		southPanel.add(tabbedPanel, BorderLayout.CENTER);

		final JButton closeButton = JComponentBuilder.createJButton(baseKey
				+ ".okButton", GUICommandStrings.OK_COMMAND, this);
		southPanel.add(closeButton, BorderLayout.SOUTH);

		this.add(northPanel, BorderLayout.NORTH);
		this.add(image, BorderLayout.CENTER);
		this.add(southPanel, BorderLayout.SOUTH);

		pack();

	}
}
