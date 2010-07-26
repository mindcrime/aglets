package com.ibm.aglets;

/*
 * @(#)ByteArrayImageSource.java
 * 
 * IBM Confidential-Restricted
 * 
 * OCO Source Materials
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import sun.awt.image.FileImageSource;
import sun.awt.image.GifImageDecoder;
import sun.awt.image.ImageDecoder;
import sun.awt.image.JPEGImageDecoder;
import sun.awt.image.XbmImageDecoder;

public class ByteArrayImageSource extends FileImageSource {
    byte buf[];
    String type;

    public ByteArrayImageSource(byte b[], String t) {
	super(null);
	this.buf = b;
	this.type = t;
    }

    @Override
    protected ImageDecoder getDecoder() {
	InputStream inputStream = new ByteArrayInputStream(this.buf);

	if (this.type != null) {
	    if (this.type.equals("gif")) {
		return new GifImageDecoder(this, inputStream);
	    }
	    if (this.type.equals("jpeg") || this.type.equals("jpg")
		    || this.type.equals("jpe") || this.type.equals("jfif")) {
		return new JPEGImageDecoder(this, inputStream);

	    }
	    if (this.type.equals("xbm")) {
		return new XbmImageDecoder(this, inputStream);
	    }
	}

	return super.getDecoder(inputStream);
    }
}
