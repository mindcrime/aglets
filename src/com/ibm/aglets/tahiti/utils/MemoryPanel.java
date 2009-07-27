/*
 * Created on Oct 1, 2004
 *
 * @author Luca Ferrari
 */
package com.ibm.aglets.tahiti.utils;

import javax.swing.JPanel;
import javax.swing.*;
import java.awt.Color;
import java.awt.*;
import java.util.ResourceBundle;


/**
 * A panel with a progress bar showing the amount of memory in the VM.
 * @author Luca Ferrari <A HREF="mailto:cat4hire@users.sourceforge.net">cat4hire@users.sourceforge.net</A>
 */
public class MemoryPanel extends JPanel implements Runnable
{
    
    // The resource bundle component
    public static ResourceBundle bundle = null;
    // loading resources from the bundle
    static {
        bundle = ResourceBundle.getBundle("tahiti");
    }
    
    
    /**
     * Memory ammount.
     */
    protected long total=0,used=0;

    /**
     * Size of the bar.
     */
    protected int height=0,length=0;
    /**
     * The progress bar.
     */
    protected JProgressBar bar=null;

    /**
     * The update period.
     */
    protected long updateTime=1000;

    /**
     * My thread.
     */
    protected Thread myThread=null;

    /**
     * The scaling factor.
     */
    protected float scaling=1;
    
    /**
     * A label to show below the progress bar
     * 
     */
    protected JLabel description;
    
    /**
     * Indicates if the description must be shown.
     */
    protected boolean showDescription=false;

    /**
     * Method to update the progress bar.
     * @param newLevel the new value to set
     */
    protected void updateProgressBar(long newLevel)
    {
        if(newLevel<0)
        {
            return;
        }

        if(this.bar!=null)
            this.bar.setValue((int)(newLevel*this.scaling));
        }

        /**
         * Method to init the size of the bar.
         * The size is calculated automatically to the size of the panel.
         * @param x the size of the bar
         * @param y the size of the bar
         */
        protected void calculateBarSize(int x, int y)
        {

            this.bar=new JProgressBar(JProgressBar.CENTER);
            this.bar.setForeground(Color.GREEN);
            this.bar.setBackground(Color.darkGray);
            this.bar.setSize(x/4,y);
            this.bar.setVisible(true);
            this.bar.setMinimum(0);
            this.bar.setMaximum(100);
            this.add(new JLabel(bundle.getString("memorypanel.label")));
            this.add(this.bar);
	    this.description=new JLabel(bundle.getString("memorypanel.label")+": ");
            if(this.showDescription){

                    this.add(this.description);
                    this.add(new JLabel(bundle.getString("memorypanel.update")+this.updateTime));
            }
	    


        }

        /**
         * Method to set the update time.
         * @param time interval time in milliseconds
         */
        public void setUpdateTime(long millis)
        {
            if(millis>0)
            {
                this.updateTime=millis;
            }
        }

        /**
         * Method to run the memory thread. The thread calucaltes the memory
         * spaces and display the progress bar.
         */
        public void run()
        {

            Runtime rt=Runtime.getRuntime();
            this.myThread=Thread.currentThread();
            if(rt==null || this.myThread==null)
            {
                return;
            }

            /* the total memory should not change during the execution */
            this.total=rt.totalMemory();

            /* set the scaling factor */
            this.scaling=(float)100/(float)this.total;

            while(true)
            {
                try
                {
                    /* get the local memory */
                    /* get the local memory */
                    this.used=rt.freeMemory();
                    this.updateProgressBar(this.used);
                    this.bar.setToolTipText(bundle.getString("memorypanel.memory")+this.used+"/"+this.total+" bytes ("+(((
    float)this.used/(float)this.total) *100)+"%)");
                    this.description.setText(this.bar.getToolTipText());
                    this.myThread.sleep(this.updateTime);
                }
                catch(Exception e){
                    System.err.println("[memory-panel]"+e);
                }
            }
        }


        /**
         * Default constructor.
         * @param width the size of the bar
         * @param height the height of the bar
         * @param startThread true if the thread must be started.
         * @param showDescription if true shows a description of the memory usage under the progress bar
         */
        public MemoryPanel(int width,int height,boolean startThread, boolean showDescription)
        {
            super();
            this.showDescription=showDescription;
	    //            this.setLayout(new GridLayout(2,2));
            this.calculateBarSize(width,height);
            this.setSize(width,height);



            if(startThread==true)
            {
                Thread t=new Thread(this,"Memory check thread");
                t.start();
            }
        }


    /**
     * Method to start the thread.
     */
    public void startThread()
    {
        if(this.myThread==null)
        {
            Thread t=new Thread(this,"Memory check thread");
            t.start();
        }
    }
    
    
}




