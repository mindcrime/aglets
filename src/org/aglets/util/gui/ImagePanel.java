package org.aglets.util.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;

import javax.swing.JPanel;

public class ImagePanel extends JPanel implements ImageObserver
{
    
    
    
    /**
     * The image to display
     */
    protected Image image=null;

    /**
     * This flag indicates if the image is complete or not.
     */
    protected boolean isComplete=false;

    /**
     * The dimension of the panel.
     */
    protected int width=0,height=0;

    /**
     * This is the message to display during loading.
     */
    protected String message="Loading...";

    /**
     * Default constructor.
     * @param image the image object.
     */
    public ImagePanel(Image image)
    {
		super();
		this.image=image;
    }

    /**
     * Overloaded constructor. It try to load the image itself. You should use
     * this to avoid image flicking problems. This method use all the
     * capabilities of the ImageObserver.
     * @param image the image file name
     */
    public ImagePanel(String image) {
		super();
	
	
		/* now load the image */
		Toolkit tk=Toolkit.getDefaultToolkit();
	
		this.image=tk.getImage(image);
	
		/* use the media tracker to wait untill the image is not loaded */
		try{
		    MediaTracker tracker=new MediaTracker(this);
		    tracker.addImage(this.image,0);
		    tracker.waitForID(0);
		    this.isComplete=true;
	
		    /* now that the image is fully loaded I need to resize the panel to
		    the size of the image */
		    this.width=this.image.getWidth(this);
		    this.height=this.image.getHeight(this);
		    this.setSize(this.width,this.height);
		    this.setMinimumSize(this.getSize());
		    this.setMaximumSize(this.getSize());
		    this.setVisible(true);
		}
		catch(InterruptedException e){
		    this.message="Exception during loading process!";
		    this.isComplete=false;
		}

    }

    /**
     * Draw the image.
     */
    @Override
    public void paint(Graphics device)   {
		super.paint(device);
	
		if(this.isComplete==true)	{
		    device.drawImage(this.image,0,0,this.width,this.height,this);
		}
		else	{
		    device.setColor(Color.RED);
		    device.drawString(this.message,20,20);
		}

    }

    /**
     * The update image method. This method is called for every update and or
     * error.
     */
    @Override
    public boolean imageUpdate(Image image,int infoFlags, int x, int y,
				int width, int height)
    {
		if((infoFlags & ImageObserver.ALLBITS)==0)
		{
		    /* the image is complete */
		    this.isComplete=true;
		    this.repaint();
		    this.setVisible(true);
		    return false;
		}
		else
		if((infoFlags & ImageObserver.ERROR)==0 || (infoFlags & ImageObserver.ABORT)==0 )
		{
		    /* error or abort */
		    this.isComplete=false;
		    this.message="Error during load process (or abort)";
		    this.repaint();
		    return true;
		}
		else
		if((infoFlags & ImageObserver.SOMEBITS)==0)
		{
		    /* some other data loaded, show the loading process percent */
		    int originalWidth=this.image.getWidth(this);
		    int originalHeight=this.image.getHeight(this);
		    int currentWidth=image.getWidth(this);
		    int currentHeight=image.getHeight(this);
	
		    /* now calculate the total of pixels */
		    long originalTotal=originalWidth*originalHeight;
		    long currentTotal=currentWidth*currentHeight;
	
		    /* now calculate the percent */
		    float percent=(float)currentTotal/(float)originalTotal *100;
	
		    /* set the string */
		    this.message="Loading progress: "+(int)percent+" % done";
		    this.repaint();
		    return true;
		}
	
		return true;
    }
    
    /**
     * Defines the dimension of the panel depending on the size of the image.
     */
    @Override
    public Dimension getPreferredSize(){
	return new Dimension( this.image.getWidth(this), this.image.getHeight(this));
    }
   
    
  
}