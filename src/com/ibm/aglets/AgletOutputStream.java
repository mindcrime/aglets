package com.ibm.aglets;

/*
 * @(#)AgletOutputStream.java
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

import java.io.*;

// import com.ibm.awb.misc.DigestTable;
import java.util.Vector;
import java.util.Enumeration;
import com.ibm.maf.ClassName;

/**
 * An instance of this class writes objects and class data into an output
 * stream. This aglet output stream writes objects, class data of these objects
 * and class data of all super classes of these classes. Data written into the
 * output stream must be read by an instance of the AgletInputStream.
 * 
 * This aglet output stream writes the name of the class, the URL of its origin
 * , length of class data and class data. If the class is common whose package
 * is "java", "atp" or "aglets", the class data will not be written.
 * 
 * @see AgletInputStream
 * @version     1.00    96/06/28
 * @author      Gaku Yamamoto
 */

final class AgletOutputStream extends ObjectOutputStream {

	private Vector classes = new Vector();

	/*
	 * Write annotated classes to DataOutput object.
	 * private void writeTo(DataOutput dout, ResourceManager rm) throws IOException {
	 * int num = classes.size();
	 * 
	 * dout.writeInt(num);
	 * Enumeration e = classes.elements();
	 * 
	 * while(e.hasMoreElements()) {
	 * Class cls = (Class)e.nextElement();
	 * 
	 * dout.writeUTF(cls.getName());
	 * byte bytecode[] = rm.getByteCode(cls);
	 * if (bytecode != null) {
	 * dout.writeInt(bytecode.length);
	 * dout.write(bytecode);
	 * } else {
	 * dout.writeInt(0);
	 * }
	 * }
	 * }
	 */
	/**
	 * Create a new instance of this class with version given.
	 * @param out an output stream where data are written into.
	 * @exception IOException if can not write into the output stream.
	 */
	AgletOutputStream(OutputStream out) throws IOException {
		super(out);
	}
	/**
	 * Write the class data into the output stream. Class data of all super
	 * classes of the class will be written together.
	 * @param cls {@link Class} to annotate
	 * @exception IOException if can not write into the output stream.
	 */
	synchronized public void annotateClass(Class cls) throws IOException {

		// annotate interfaces
		Class interfaces[] = cls.getInterfaces();

		for (int i = 0; i < interfaces.length; i++) {
			annotateClass(interfaces[i]);
		} 

		// annotate class if it's not in the class cache.
		if (classes.contains(cls) == false) {
			classes.addElement(cls);

			// 
			// REMIND: may not need in RMIprebeta2 or JDK1.1
			// 
			Class super_class = cls.getSuperclass();

			if (super_class != null) {
				annotateClass(super_class);
			} 
		} 
	}
	/*
	 * Creates class table for classes
	 */
	private Class[] getClasses() {
		Class[] ret = new Class[classes.size()];

		classes.copyInto(ret);
		return ret;
	}
	/*
	 * 
	 */
	/* package */
	ClassName[] getClassNames(ResourceManager rm) {
		return rm.getClassNames(getClasses());
	}
	/*
	 * 
	 */
	protected void writeStreamHeader() throws IOException {
		writeInt(AgletRuntime.AGLET_MAGIC);
		writeByte(AgletRuntime.AGLET_STREAM_VERSION);
	}
}
