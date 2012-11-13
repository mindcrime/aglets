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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Constructor;

public final class MessageEx extends MAFExtendedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1470099716483531666L;
	static public Throwable deserialize(final DataInput in) throws IOException {
		try {
			final String classname = in.readUTF();
			final String msg = in.readUTF();

			final Class cls = Class.forName(classname);
			final java.lang.reflect.Constructor[] c = cls.getConstructors();

			Object result = null;

			for (final Constructor element : c) {
				final Class params[] = element.getParameterTypes();

				if (params.length == 1) {
					final Object args[] = new Object[1];

					args[0] = msg;
					result = element.newInstance(args);
				}
			}
			if (result instanceof Throwable) {
				return (Throwable) result;
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		return new Exception("");
	}

	static public MessageEx read(final DataInput in) throws IOException {
		final String str = in.readUTF();

		return new MessageEx(str, deserialize(in));
	}

	// constructor
	transient Throwable _exception;

	public MessageEx() {
		super();
	}

	public MessageEx(final String msg, final Throwable ex) {
		super(msg);
		_exception = ex;
	}

	public MessageEx(final Throwable ex) {
		this(ex.getMessage(), ex);
	}

	public Throwable getException() {
		return _exception;
	}

	@Override
	public String toString() {
		return super.toString() + ": " + _exception.getClass().getName()
		+ "," + _exception.getMessage();
	}

	public void write(final DataOutput out) throws IOException {
		out.writeUTF(getMessage());
		out.writeUTF(_exception.getClass().getName());
		out.writeUTF(_exception.getMessage());
	}
}
