package com.ibm.aglets;

/*
 * @(#)AgletAudioClip.java
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

import java.applet.AudioClip;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import sun.audio.AudioData;
import sun.audio.AudioDataStream;
import sun.audio.AudioPlayer;
import sun.audio.ContinuousAudioDataStream;

public class AgletAudioClip implements AudioClip, java.io.Serializable {

    static final long serialVersionUID = -694436981030784266L;

    URL url;
    AudioData data;
    transient InputStream stream;

    public AgletAudioClip(URL u, AudioData d) {
	this.url = u;
	this.data = d;
    }

    @Override
    public int hashCode() {
	return this.url.hashCode();
    }

    public synchronized void loop() {
	this.stop();
	if (this.data != null) {
	    this.stream = new ContinuousAudioDataStream(this.data);
	    AudioPlayer.player.start(this.stream);
	}
    }

    public synchronized void play() {
	this.stop();
	if (this.data != null) {
	    this.stream = new AudioDataStream(this.data);
	    AudioPlayer.player.start(this.stream);
	}
    }

    public synchronized void stop() {
	if (this.stream != null) {
	    try {
		AudioPlayer.player.stop(this.stream);
		this.stream.close();
	    } catch (IOException e) {
	    }
	}
	return;
    }

    @Override
    public String toString() {
	return this.getClass().toString() + "[" + this.url + "]";
    }
}
