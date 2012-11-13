/*
 * Created on Oct 5, 2004
 *
 * @author Luca Ferrari, <a href="mailto:cat4hire@users.sourceforge.net">cat4hire@users.sourceforge.net</a>
 */
package com.ibm.aglets.tahiti.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;

import javax.swing.JPanel;

/**
 * A panel able to load and show an image. This class is not localizable!
 */
public class ImagePanel extends JPanel implements ImageObserver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8849059915023050623L;

	/**
	 * The image to display
	 */
	protected Image image = null;

	/**
	 * This flag indicates if the image is complete or not.
	 */
	protected boolean isComplete = false;

	/**
	 * The dimension of the panel.
	 */
	protected int width = 0, height = 0;

	/**
	 * This is the message to display during loading.
	 */
	protected String message = "Loading...";

	/**
	 * Default constructor.
	 * 
	 * @param image
	 *            the image object.
	 */
	public ImagePanel(final Image image) {
		super();
		this.image = image;
	}

	/**
	 * Overloaded constructor. It try to load the image itself. You should use
	 * this to avoid image flicking problems. This method use all the
	 * capabilities of the ImageObserver.
	 * 
	 * @param image
	 *            the image file name
	 */
	public ImagePanel(final String image) {
		super();

		/* now load the image */
		final Toolkit tk = Toolkit.getDefaultToolkit();

		this.image = tk.getImage(image);

		/* use the media tracker to wait untill the image is not loaded */
		try {
			final MediaTracker tracker = new MediaTracker(this);
			tracker.addImage(this.image, 0);
			tracker.waitForID(0);
			isComplete = true;

			/*
			 * now that the image is fully loaded I need to resize the panel to
			 * the size of the image
			 */
			width = this.image.getWidth(this);
			height = this.image.getHeight(this);
			this.setSize(width, height);
			setVisible(true);
		} catch (final InterruptedException e) {
			message = "Exception during loading process!";
			isComplete = false;
		}

	}

	/**
	 * The update image method. This method is called for every update and or
	 * error.
	 */
	@Override
	public boolean imageUpdate(
	                           final Image image,
	                           final int infoFlags,
	                           final int x,
	                           final int y,
	                           final int width,
	                           final int height) {
		if ((infoFlags & ALLBITS) == 0) {
			/* the image is complete */
			isComplete = true;
			this.repaint();
			setVisible(true);
			return false;
		} else if (((infoFlags & ERROR) == 0) || ((infoFlags & ABORT) == 0)) {
			/* error or abort */
			isComplete = false;
			message = "Error during load process (or abort)";
			this.repaint();
			return true;
		} else if ((infoFlags & SOMEBITS) == 0) {
			/* some other data loaded, show the loading process percent */
			final int originalWidth = this.image.getWidth(this);
			final int originalHeight = this.image.getHeight(this);
			final int currentWidth = image.getWidth(this);
			final int currentHeight = image.getHeight(this);

			/* now calculate the total of pixels */
			final long originalTotal = originalWidth * originalHeight;
			final long currentTotal = currentWidth * currentHeight;

			/* now calculate the percent */
			final float percent = (float) currentTotal / (float) originalTotal * 100;

			/* set the string */
			message = "Loading progress: " + (int) percent + " % done";
			this.repaint();
			return true;
		}

		return true;
	}

	/**
	 * Draw the image.
	 */
	@Override
	public void paint(final Graphics device) {
		super.paint(device);

		if (isComplete == true) {
			device.drawImage(image, 0, 0, width, height, this);
		} else {
			device.setColor(Color.RED);
			device.drawString(message, 20, 20);
		}

	}
}