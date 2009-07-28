package com.ibm.aglets;

/*
 * @(#)AgletImageData.java
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

/*
 * import java.io.ObjectOutput;
 * import java.io.ObjectInput;
 */
import java.net.URL;
import java.io.OutputStream;
import java.io.IOException;
import java.awt.image.ImageProducer;

public class AgletImageData implements com.ibm.aglet.util.ImageData, 
									   java.io.Serializable {

	static final long serialVersionUID = 5393039055419095915L;

	URL url;
	byte buf[];
	String type;

	public AgletImageData(URL u, byte[] b, String t) {
		url = u;
		buf = b;
		type = t;
	}
	public ImageProducer getImageProducer() {
		return new ByteArrayImageSource(buf, type);
	}
	public int hashCode() {
		return url.hashCode();
	}
	/*
	 * To support manual serialization
	 * public AgletImageData() {
	 * }
	 * 
	 * public void writeExternal(ObjectOutput out) throws IOException {
	 * out.writeObject(externalURLForm);
	 * out.writeObject(buf);
	 * out.writeObject(type);
	 * }
	 * 
	 * public void readExternal(ObjectInput in) throws IOException {
	 * try {
	 * externalURLForm = (String)in.readObject();
	 * buf = (byte[])in.readObject();
	 * type = (String)in.readObject();
	 * } catch (ClassNotFoundException ex) {
	 * throw new IOException(ex.getMessage());
	 * }
	 * }
	 * 
	 */

	public void writeTo(OutputStream out) throws IOException {
		out.write(buf);
	}
}
