package com.ibm.aglets.tahiti;



import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;

import org.aglets.util.AgletsTranslator;
import org.aglets.log.*;

import com.ibm.aglets.thread.AgletThreadPool;

/**
 * A panel with a progress memoryBar showing the amount of memory in the VM.
 * Since version 0.2 it changes the color of the progress memoryBar depending on the memory usage. The calculation of the
 * used memory has also changed, since before it was calculating the wrong value.
 * @author Luca Ferrari <A HREF="mailto:cat4hire@users.sourceforge.net">cat4hire@users.sourceforge.net</A>
 * @version 0.2
 */
public class MemoryPanel extends JPanel implements ActionListener
{
    
    	/**
     * The logger of this class.
     */
    private AgletsLogger logger = AgletsLogger.getLogger( this.getClass().getName() );
    /**
     * The tranlator of this object.
     */
    private AgletsTranslator translator = AgletsTranslator.getInstance("tahiti", Locale.getDefault());
    /**
     * The name of this class.
     */
    private String baseKey = this.getClass().getName();
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
         * @param width the size of the memoryBar
         * @param height the height of the memoryBar
         * @param startThread true if the thread must be started.
         * @param showDescription if true shows a description of the memory usage under the progress memoryBar
         */
        public MemoryPanel(int width,int height,boolean startThread, boolean showDescription)
        {
            super();
            this.showDescription=showDescription;
            this.description = new JLabel();
            this.setLayout( new FlowLayout(FlowLayout.RIGHT));
            this.createAndPlaceProgressBars(width,height);
            this.setSize(width,height);
            
            // get the tooltip string
            this.memoryString = this.translator.translate(this.baseKey + ".memoryString");
            this.threadString = this.translator.translate(this.baseKey + ".threadString");

            // create a swing time
            Timer timer = new Timer(this.updateTime, this);
            timer.setRepeats(true);
            timer.start();
        
        }
    
        /**
         * A string that describes the memory usage.
         * @param currentValue the current value for the memory
         * @param maxValue the max value
         * @return the string that describes the memory usage
         */
        protected String getCurrentMemoryStringDescription(long currentValue, long maxValue) {
            return String.format(this.memoryString, new Object[] {currentValue, maxValue, (float) ((float)currentValue/(float)maxValue)});
        }
        
        /**
         * Provides the information for the thread progress bar.
         * @param currentValue the current number of busy threads
         * @param maxValue the max number of available threads
         * @return the string with the description
         */
        protected String getCurrentThreadStringDescription(long currentValue, long maxValue){
            return String.format(this.threadString, new Object[] {currentValue, maxValue });
        }
        
        protected  long getMaxValue() {
            /* the total memory should not change during the execution */
            return  this.currentRuntime.totalMemory();
        
        }


        protected long getCurrentValue() {
            return this.currentRuntime.freeMemory();
        }

	/**
	 * Method to update the progress memoryBar.
	 * @param newLevel the new value to set
	 * @param bar the progress bar on which to work
	 */
	protected void updateProgressBar(JProgressBar bar, long newLevel) {
	    if(newLevel<0)   return;
	    
	    int used = (int) (newLevel *  this.scaling);
	
	    if(bar!=null)
	        bar.setValue(used);
	    
	    // change the color of the progress memoryBar
	    if( used < 50 )
	        bar.setForeground(Color.GREEN);
	    else
	    if( used < 80 )
	        bar.setForeground(Color.YELLOW);
	    else
	    if( used > 80 )
	        bar.setForeground(Color.RED);
	  }

	/**
	 * Method to init the size of the memoryBar.
	 * The size is calculated automatically to the size of the panel.
	 * @param x the size of the memoryBar
	 * @param y the size of the memoryBar
	 */
	protected void createAndPlaceProgressBars(int x, int y) {
	
	    this.memoryBar=new JProgressBar(SwingConstants.CENTER);
	    this.memoryBar.setStringPainted(true);
	    this.memoryBar.setForeground(Color.GREEN);
	    this.memoryBar.setBackground(Color.darkGray);
	    this.memoryBar.setSize(x/4,y);
	    this.memoryBar.setVisible(true);
	    this.memoryBar.setMinimum(0);
	    this.memoryBar.setMaximum(100);
	    this.add(new JLabel( this.translator.translate( this.baseKey + ".memoryDescription")));
	    this.add(this.memoryBar);
	    if(this.showDescription)
	        this.add(this.description);
	    
	    this.threadBar = new JProgressBar(SwingConstants.CENTER);
	    this.threadBar.setStringPainted(true);
	    this.threadBar.setForeground(Color.GREEN);
	    this.threadBar.setBackground(Color.darkGray);
	    this.threadBar.setSize(x/4,y);
	    this.threadBar.setVisible(true);
	    this.threadBar.setMinimum(0);
	    this.threadBar.setMaximum(100);
	    this.add( new JLabel( this.translator.translate( this.baseKey + ".threadDescription")) );
	    this.add( this.threadBar );
	    	
	
	}

	/**
	 * Method to set the update time.
	 * @param time interval time in milliseconds
	 */
	public void setUpdateTime(int millis) {
	    if(millis>0)
	    {
	        this.updateTime=millis;
	    }
	}



	
	public void actionPerformed(ActionEvent e) {
	    // update the ui
	    Runtime rt=Runtime.getRuntime();

	    /* the total memory should not change during the execution */
	    this.total = this.getMaxValue();

	    // set the max value for the
	    // thread bar
	    this.totalThreads = this.pool.getMaxPoolSize();
	    this.threadBar.setMaximum( this.totalThreads );

	    /* set the scaling factor */
	    this.scaling=(float)100/(float)this.total;

	    /* get the local memory */
	    /* get the local memory */
	    this.used= this.total - this.getCurrentValue();
	    this.threadUsed = this.pool.getBusyThreadsNumber();
	    this.updateProgressBar(this.memoryBar, this.used);
	    this.updateProgressBar(this.threadBar, this.threadUsed );
	    this.memoryBar.setToolTipText( this.getCurrentMemoryStringDescription( this.used, this.total) );
	    this.threadBar.setToolTipText( this.getCurrentThreadStringDescription( this.threadUsed, this.totalThreads) );
	    
	}

    
}