package com.ibm.aglets.tahiti;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import net.sourceforge.aglets.log.AgletsLogger;
import net.sourceforge.aglets.util.AgletsTranslator;

import com.ibm.aglets.thread.AgletThreadPool;

/**
 * A panel with a progress memoryBar showing the amount of memory in the VM.
 * Since version 0.2 it changes the color of the progress memoryBar depending on
 * the memory usage. The calculation of the used memory has also changed, since
 * before it was calculating the wrong value.
 * 
 * @author Luca Ferrari <A
 *         HREF="mailto:cat4hire@users.sourceforge.net">cat4hire@users.
 *         sourceforge.net</A>
 * @version 0.2
 */
public class MemoryPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2429665474224569992L;
	/**
	 * The logger of this class.
	 */
	private final AgletsLogger logger = AgletsLogger.getLogger(this.getClass().getName());
	/**
	 * The tranlator of this object.
	 */
	private final AgletsTranslator translator = AgletsTranslator.getInstance("tahiti", Locale.getDefault());
	/**
	 * The name of this class.
	 */
	private final String baseKey = this.getClass().getName();
	/**
	 * The current runtime for this progress memoryBar.
	 */
	protected Runtime currentRuntime = Runtime.getRuntime();
	/**
	 * Memory ammount.
	 */
	protected long total = 0;
	/**
	 * Memory ammount.
	 */
	protected long used = 0;
	/**
	 * Size of the memoryBar.
	 */
	protected int height = 0;
	/**
	 * Size of the memoryBar.
	 */
	protected int length = 0;
	/**
	 * The progress memoryBar.
	 */
	protected JProgressBar memoryBar = null;

	/**
	 * A progress bar for the threads
	 */
	protected JProgressBar threadBar = null;

	/**
	 * The update period.
	 */
	protected int updateTime = 1000;
	/**
	 * My thread.
	 */
	protected Thread myThread = null;
	/**
	 * The scaling factor.
	 */
	protected float scaling = 1;
	/**
	 * A label to show below the progress memoryBar
	 * 
	 */
	protected JLabel description;
	/**
	 * Indicates if the description must be shown.
	 */
	protected boolean showDescription = false;

	/**
	 * The threadpool to monitor.
	 */
	protected AgletThreadPool pool = AgletThreadPool.getInstance();

	/**
	 * The number of threads that can be contained in the pool.
	 */
	private int totalThreads = 0;

	/**
	 * The current number of busy threads.
	 */
	private int threadUsed;

	/**
	 * The tooltip text to use (must be formatted).
	 */
	private String memoryString = null;

	/**
	 * The thread tooltip (must be formatted).
	 */
	private String threadString = null;

	/**
	 * Default constructor.
	 * 
	 * @param width
	 *            the size of the memoryBar
	 * @param height
	 *            the height of the memoryBar
	 * @param startThread
	 *            true if the thread must be started.
	 * @param showDescription
	 *            if true shows a description of the memory usage under the
	 *            progress memoryBar
	 */
	public MemoryPanel(final int width, final int height, final boolean startThread,
	                   final boolean showDescription) {
		super();
		this.showDescription = showDescription;
		description = new JLabel();
		setLayout(new FlowLayout(FlowLayout.RIGHT));
		createAndPlaceProgressBars(width, height);
		this.setSize(width, height);

		// get the tooltip string
		memoryString = translator.translate(baseKey
				+ ".memoryString");
		threadString = translator.translate(baseKey
				+ ".threadString");

		// create a swing time
		final Timer timer = new Timer(updateTime, this);
		timer.setRepeats(true);
		timer.start();

	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		Runtime.getRuntime();

		/* the total memory should not change during the execution */
		total = getMaxValue();

		// set the max value for the
		// thread bar
		totalThreads = pool.getMaxPoolSize();
		threadBar.setMaximum(totalThreads);

		/* set the scaling factor */
		scaling = (float) 100 / (float) total;

		/* get the local memory */
		/* get the local memory */
		used = total - getCurrentValue();
		threadUsed = pool.getBusyThreadsNumber();
		updateProgressBar(memoryBar, used);
		updateProgressBar(threadBar, threadUsed);
		memoryBar.setToolTipText(getCurrentMemoryStringDescription(used, total));
		threadBar.setToolTipText(getCurrentThreadStringDescription(threadUsed, totalThreads));

	}

	/**
	 * Method to init the size of the memoryBar. The size is calculated
	 * automatically to the size of the panel.
	 * 
	 * @param x
	 *            the size of the memoryBar
	 * @param y
	 *            the size of the memoryBar
	 */
	protected void createAndPlaceProgressBars(final int x, final int y) {

		memoryBar = new JProgressBar(SwingConstants.CENTER);
		memoryBar.setStringPainted(true);
		memoryBar.setForeground(Color.GREEN);
		memoryBar.setBackground(Color.darkGray);
		memoryBar.setSize(x / 4, y);
		memoryBar.setVisible(true);
		memoryBar.setMinimum(0);
		memoryBar.setMaximum(100);
		this.add(new JLabel(translator.translate(baseKey
				+ ".memoryDescription")));
		this.add(memoryBar);
		if (showDescription)
			this.add(description);

		threadBar = new JProgressBar(SwingConstants.CENTER);
		threadBar.setStringPainted(true);
		threadBar.setForeground(Color.GREEN);
		threadBar.setBackground(Color.darkGray);
		threadBar.setSize(x / 4, y);
		threadBar.setVisible(true);
		threadBar.setMinimum(0);
		threadBar.setMaximum(100);
		this.add(new JLabel(translator.translate(baseKey
				+ ".threadDescription")));
		this.add(threadBar);

	}

	/**
	 * A string that describes the memory usage.
	 * 
	 * @param currentValue
	 *            the current value for the memory
	 * @param maxValue
	 *            the max value
	 * @return the string that describes the memory usage
	 */
	protected String getCurrentMemoryStringDescription(
	                                                   final long currentValue,
	                                                   final long maxValue) {
		return String.format(memoryString, new Object[] { currentValue,
				maxValue, (float) ((float) currentValue / (float) maxValue) });
	}

	/**
	 * Provides the information for the thread progress bar.
	 * 
	 * @param currentValue
	 *            the current number of busy threads
	 * @param maxValue
	 *            the max number of available threads
	 * @return the string with the description
	 */
	protected String getCurrentThreadStringDescription(
	                                                   final long currentValue,
	                                                   final long maxValue) {
		return String.format(threadString, new Object[] { currentValue,
				maxValue });
	}

	protected long getCurrentValue() {
		return currentRuntime.freeMemory();
	}

	protected long getMaxValue() {
		/* the total memory should not change during the execution */
		return currentRuntime.totalMemory();

	}

	/**
	 * Method to set the update time.
	 * 
	 * @param millis
	 *            interval time in milliseconds
	 */
	public void setUpdateTime(final int millis) {
		if (millis > 0) {
			updateTime = millis;
		}
	}

	/**
	 * Method to update the progress memoryBar.
	 * 
	 * @param newLevel
	 *            the new value to set
	 * @param bar
	 *            the progress bar on which to work
	 */
	protected void updateProgressBar(final JProgressBar bar, final long newLevel) {
		if (newLevel < 0)
			return;

		final int used = (int) (newLevel * scaling);

		if (bar != null)
			bar.setValue(used);

		// change the color of the progress memoryBar
		if (used < 50)
			bar.setForeground(Color.GREEN);
		else if (used < 80)
			bar.setForeground(Color.YELLOW);
		else if (used > 80)
			bar.setForeground(Color.RED);
	}

}