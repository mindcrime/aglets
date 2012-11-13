package com.ibm.awb.misc;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.GregorianCalendar;

/*
 * @(#)LogFileOutputStream.java
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

/**
 * Write a log to a file. If the file does not exist, it will create it. If a
 * directory of the file does not exist, ir will create the directory too.
 * 
 * @author GakuYamamoto
 */
public class LogFileOutputStream extends OutputStream {
	String _fileName = null;
	RandomAccessFile _file = null;
	long _maxFileSize = 0;

	/**
	 * Create an instance of this class. If the file does not exist, it will
	 * create the file. If a directory of the file does not exist, it will
	 * create the directory too.
	 * 
	 * @param filename
	 *            log file name
	 * @param maxFileSize
	 *            maximum file size of the log. If the size of the log file
	 *            exceeds this value, the log file will be renamed and a new log
	 *            file will be created.
	 * @exception IOException
	 *                if fail to create or access the file.
	 */
	public LogFileOutputStream(final String filename, final long maxFileSize)
	throws IOException {
		checkFile(filename);
		_file = new RandomAccessFile(filename, "rw");
		_file.seek(_file.length());
		_maxFileSize = maxFileSize;
		_fileName = filename;
	}

	private RandomAccessFile changeFile(RandomAccessFile file, final String filename)
	throws Exception {
		final String dstName = filename + ".arc";

		file.close();
		final File src = new File(filename);
		final File dst = new File(dstName);

		if (dst.exists()) {
			final Calendar date = new GregorianCalendar();
			final String bakName = dstName + date.get(Calendar.MINUTE)
			+ date.get(Calendar.HOUR_OF_DAY) + date.get(Calendar.DATE)
			+ date.get(Calendar.MONTH) + date.get(Calendar.YEAR);
			final File bak = new File(bakName);

			if (bak.exists() == false) {
				dst.renameTo(bak);
			}
		}
		src.renameTo(dst);
		file = new RandomAccessFile(filename, "rw");
		return file;
	}

	private void checkFile(final String filename) throws IOException {

		// System.out.println(filename);
		final File file = new File(filename);
		final File dir = new File(file.getParent());

		if ((dir.exists() == false) && (dir.mkdir() == false)) {
			throw new IOException("cannot create the directory" + dir);
		}
		if (file.exists() && (file.canWrite() == false)) {
			throw new IOException("The file " + file + " is not writable");
		}
	}

	/**
	 * Close this.
	 */
	@Override
	public void close() throws IOException {
		_file.close();
		super.close();
	}

	/**
	 * Write data to the log file. If the size of the file exceeds maximum size
	 * of the file, this stream will rename the file to filename + ".arc" and
	 * create a new log file. If the file whose name is "filename + ".arc""
	 * already exists, the existing file will be renamed to "filename + ".arc" +
	 * date.getMinutes() + date.getHours() + date.getDate() + date.getMonth() +
	 * date.getYear()", where date is current date.
	 * 
	 * @param b
	 *            data.
	 * @exception IOException
	 *                if fail to write the data.
	 */
	@Override
	public synchronized void write(final byte[] b) throws IOException {
		final long pointer = _file.getFilePointer();

		if (pointer > _maxFileSize) {
			try {
				_file = changeFile(_file, _fileName);
			} catch (final Exception e) {

				// e.printStackTrace();
				throw new IOException("can't create archive file."
						+ e.toString());
			}
		}
		_file.write(b);
	}

	/**
	 * Write data to the log file. If the size of the file exceeds maximum size
	 * of the file, this stream will rename the file to filename + ".arc" and
	 * create a new log file. If the file whose name is "filename + ".arc""
	 * already exists, the existing file will be renamed to "filename + ".arc" +
	 * date.getMinutes() + date.getHours() + date.getDate() + date.getMonth() +
	 * date.getYear()", where date is current date.
	 * 
	 * @param b
	 *            data.
	 * @param off
	 *            offset of the data.
	 * @param len
	 *            length of the data written to the file.
	 * @exception IOException
	 *                if fail to write the data.
	 */
	@Override
	public synchronized void write(final byte[] b, final int off, final int len)
	throws IOException {
		final long pointer = _file.getFilePointer();

		if (pointer > _maxFileSize) {
			try {
				_file = changeFile(_file, _fileName);
			} catch (final Exception e) {

				// e.printStackTrace();
				throw new IOException("can't create archive file."
						+ e.toString());
			}
		}
		_file.write(b, off, len);
	}

	/**
	 * Write data to the log file. If the size of the file exceeds maximum size
	 * of the file, this stream will rename the file to filename + ".arc" and
	 * create a new log file. If the file whose name is "filename + ".arc""
	 * already exists, the existing file will be renamed to "filename + ".arc" +
	 * date.getMinutes() + date.getHours() + date.getDate() + date.getMonth() +
	 * date.getYear()", where date is current date.
	 * 
	 * @param b
	 *            integer.
	 * @exception IOException
	 *                if fail to write the data.
	 */
	@Override
	public synchronized void write(final int b) throws IOException {
		final long pointer = _file.getFilePointer();

		if (pointer > _maxFileSize) {
			try {
				_file = changeFile(_file, _fileName);
			} catch (final Exception e) {

				// e.printStackTrace();
				throw new IOException("can't create archive file."
						+ e.toString());
			}
		}
		_file.write(b);
	}
}
