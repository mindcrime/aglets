package com.ibm.awb.misc;

import java.io.*;
import java.util.Date;
import java.text.DateFormat;

/*
 * @(#)LogStream.java
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

public class LogStream extends PrintStream {
	private String _name;
	private OutputStream _logStream;
	private OutputStreamWriter _logWriter;
	private ByteArrayOutputStream _bufOut;
	private DateFormat _date_format = 
		DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

	/*
	 * public static class test extends Thread {
	 * String _name;
	 * 
	 * public test(String name) {
	 * _name = name;
	 * }
	 * 
	 * public void run() {
	 * int i = 0;
	 * while (true) {
	 * System.out.println(_name + "=" + (i++) + "hoge! hoge! Yahoo!!");
	 * try {
	 * throwError();
	 * } catch (Exception ex) {
	 * ex.printStackTrace();
	 * }
	 * }
	 * }
	 * 
	 * public static void throwError() throws Exception {
	 * throw new Exception("Test Exception");
	 * }
	 * 
	 * public static void main(String[] args) throws Exception {
	 * LogStream log;
	 * log = new LogStream("out", System.out);
	 * System.setOut(log);
	 * log = new LogStream("err", System.err);
	 * System.setErr(log);
	 * 
	 * test[] t = new test[100];
	 * for (int i = 0; i < 100; i++) {
	 * t[i] = new test("test" + i);
	 * }
	 * for (int i = 0; i < 100; i++) {
	 * t[i].start();
	 * }
	 * }
	 * }
	 */
	public LogStream(String name, OutputStream out) {
		super(new ByteArrayOutputStream());
		_bufOut = (ByteArrayOutputStream)super.out;

		_name = name;
		_logStream = out;
		_logWriter = new OutputStreamWriter(_logStream);
	}
	public void write(byte b[], int off, int len) {
		if (len < 0) {
			throw new ArrayIndexOutOfBoundsException(len);
		} 
		for (int i = 0; i < len; ++i) {
			write(b[off + i]);
		}
	}
	public void write(int b) {
		if (b == '\n') {
			synchronized (this.getClass()) {
				StringBuffer sb = new StringBuffer();

				sb.append(_date_format.format(new Date()));
				sb.append('|');
				sb.append(_name);
				sb.append('|');
				sb.append(Thread.currentThread().getName());
				sb.append("| ");
				try {
					_logWriter.write(sb.toString());
					_logWriter.flush();
					_bufOut.writeTo(_logStream);
					_logStream.write(b);
					_logStream.flush();
				} catch (IOException e) {
					setError();
				} 
				finally {
					_bufOut.reset();
				} 
			} 
		} else {
			super.write(b);
		} 
	}
}
