/*
 * Created on Oct 1, 2004
 *
 * @author Luca Ferrari
 */
package com.ibm.aglets.tahiti.utils;

import java.awt.Color;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

/**
 * A panel with a progress bar showing the amount of memory in the VM.
 * 
 * @author Luca Ferrari <A
 *         HREF="mailto:cat4hire@users.sourceforge.net">cat4hire@users.
 *         sourceforge.net</A>
 */
public class MemoryPanel extends JPanel implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8514585622436310579L;

	// The resource bundle component
	public static ResourceBundle bundle = null;
	// loading resources from the bundle
	static {
		bundle = ResourceBundle.getBundle("tahiti");
	}

	/**
	 * Memory amount.
	 */
	protected long total = 0, used = 0;

	/**
	 * Size of the bar.
	 */
	protected int height = 0, length = 0;
	/**
	 * The progress bar.
	 */
	protected JProgressBar bar = null;

	/**
	 * The update period.
	 */
	protected long updateTime = 1000;

	/**
	 * My thread.
	 */
	protected Thread myThread = null;

	/**
	 * The scaling factor.
	 */
	protected float scaling = 1;

	/**
	 * A label to show below the progress bar
	 * 
	 */
	protected JLabel description;

	/**
	 * Indicates if the description must be shown.
	 */
	protected boolean showDescription = false;

	/**
	 * Default constructor.
	 * 
	 * @param width
	 *            the size of the bar
	 * @param height
	 *            the height of the bar
	 * @param startThread
	 *            true if the thread must be started.
	 * @param showDescription
	 *            if true shows a description of the memory usage under the
	 *            progress bar
	 */
	public MemoryPanel(final int width, final int height, final boolean startThread,
	                   final boolean showDescription) {
		super();
		this.showDescription = showDescription;
		// this.setLayout(new GridLayout(2,2));
		calculateBarSize(width, height);
		this.setSize(width, height);

		if (startThread == true) {
			final Thread t = new Thread(this, "Memory check thread");
			t.start();
		}
	}

	/**
	 * Method to init the size of the bar. The size is calculated automatically
	 * to the size of the panel.
	 * 
	 * @param x
	 *            the size of the bar
	 * @param y
	 *            the size of the bar
	 */
	protected void calculateBarSize(final int x, final int y) {

		bar = new JProgressBar(SwingConstants.CENTER);
		bar.setForeground(Color.GREEN);
		bar.setBackground(Color.darkGray);
		bar.setSize(x / 4, y);
		bar.setVisible(true);
		bar.setMinimum(0);
		bar.setMaximum(100);
		this.add(new JLabel(bundle.getString("memorypanel.label")));
		this.add(bar);
		description = new JLabel(bundle.getString("memorypanel.label")
				+ ": ");
		if (showDescription) {

			this.add(description);
			this.add(new JLabel(bundle.getString("memorypanel.update")
					+ updateTime));
		}

	}

	/**
	 * Method to run the memory thread. The thread calucaltes the memory spaces
	 * and display the progress bar.
	 */
	@Override
	public void run() {

		final Runtime rt = Runtime.getRuntime();
		myThread = Thread.currentThread();
		if ((rt == null) || (myThread == null)) {
			return;
		}

		/* the total memory should not change during the execution */
		total = rt.totalMemory();

		/* set the scaling factor */
		scaling = (float) 100 / (float) total;

		while (true) {
			try {
				/* get the local memory */
				/* get the local memory */
				used = rt.freeMemory();
				updateProgressBar(used);
				bar.setToolTipText(bundle.getString("memorypanel.memory")
						+ used + "/" + total + " bytes ("
						+ (((float) used / (float) total) * 100)
						+ "%)");
				description.setText(bar.getToolTipText());
				Thread.sleep(updateTime);
			} catch (final Exception e) {
				System.err.println("[memory-panel]" + e);
			}
		}
	}

	/**
	 * Method to set the update time.
	 * 
	 * @param millis
	 *            interval time in milliseconds
	 */
	public void setUpdateTime(final long millis) {
		if (millis > 0) {
			updateTime = millis;
		}
	}

	/**
	 * Method to start the thread.
	 */
	public void startThread() {
		if (myThread == null) {
			final Thread t = new Thread(this, "Memory check thread");
			t.start();
		}
	}

	/**
	 * Method to update the progress bar.
	 * 
	 * @param newLevel
	 *            the new value to set
	 */
	protected void updateProgressBar(final long newLevel) {
		if (newLevel < 0) {
			return;
		}

		if (bar != null)
			bar.setValue((int) (newLevel * scaling));
	}

}
