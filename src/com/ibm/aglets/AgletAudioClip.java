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

import sun.audio.*;
import java.net.URL;
import java.applet.AudioClip;
import java.io.IOException;
import java.io.InputStream;

public class AgletAudioClip implements AudioClip, java.io.Serializable {

	static final long serialVersionUID = -694436981030784266L;

	URL url;
	AudioData data;
	transient InputStream stream;

	public AgletAudioClip(URL u, AudioData d) {
		url = u;
		data = d;
	}
	public int hashCode() {
		return url.hashCode();
	}
	public synchronized void loop() {
		stop();
		if (data != null) {
			stream = new ContinuousAudioDataStream(data);
			AudioPlayer.player.start(stream);
		} 
	}
	public synchronized void play() {
		stop();
		if (data != null) {
			stream = new AudioDataStream(data);
			AudioPlayer.player.start(stream);
		} 
	}
	public synchronized void stop() {
		if (stream != null) {
			try {
				AudioPlayer.player.stop(stream);
				stream.close();
			} catch (IOException e) {}
		} 
		return;
	}
	public String toString() {
		return getClass().toString() + "[" + url + "]";
	}
}
