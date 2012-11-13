package com.ibm.aglets.tahiti;

import java.awt.BorderLayout;

/**
 * A dialog that shows the memory usage for the current platform.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 *         19/nov/07
 */
public class MemoryDialog extends TahitiDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4618287515135257471L;
	/**
	 * The memory panel for this dialog.
	 */
	private MemoryPanel memory = null;

	public MemoryDialog(final MainWindow owner) {
		super(owner);

		memory = new MemoryPanel(200, 100, true, false);
		contentPanel.add(memory, BorderLayout.CENTER);

		pack();
	}
}
