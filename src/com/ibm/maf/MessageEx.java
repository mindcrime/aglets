package com.ibm.maf;

/*
 * @(#)MessageEx.java
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

public final class MessageEx extends MAFExtendedException {

	// constructor
	transient Throwable _exception;

	public MessageEx() {
		super();
	}
	public MessageEx(String msg, Throwable ex) {
		super(msg);
		_exception = ex;
	}
	public MessageEx(Throwable ex) {
		this(ex.getMessage(), ex);
	}
	static public Throwable deserialize(DataInput in) throws IOException {
		try {
			String classname = in.readUTF();
			String msg = in.readUTF();

			Class cls = Class.forName(classname);
			java.lang.reflect.Constructor[] c = cls.getConstructors();

			Object result = null;

			for (int i = 0; i < c.length; i++) {
				Class params[] = c[i].getParameterTypes();

				if (params.length == 1) {
					Object args[] = new Object[1];

					args[0] = msg;
					result = c[i].newInstance(args);
				} 
			} 
			if (result instanceof Throwable) {
				return (Throwable)result;
			} 
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
		return new Exception("");
	}
	public Throwable getException() {
		return _exception;
	}
	static public MessageEx read(DataInput in) throws IOException {
		String str = in.readUTF();

		return new MessageEx(str, deserialize(in));
	}
	public String toString() {
		return super.toString() + ": " + _exception.getClass().getName() 
			   + "," + _exception.getMessage();
	}
	public void write(DataOutput out) throws IOException {
		out.writeUTF(getMessage());
		out.writeUTF(_exception.getClass().getName());
		out.writeUTF(_exception.getMessage());
	}
}
