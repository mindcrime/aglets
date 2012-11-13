package com.ibm.aglets.security;

/*
 * @(#)DateString.java
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

// - package com.ibm.awb.misc;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * The <tt>DateString</tt> class is a converter between Date and String.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
final public class DateString {
	/**
	 * Date string format
	 */
	final private static String FORMAT_DATE = "yyyy.MM.dd HH:mm:ss.SSS z";

	/**
	 * Gets string from date.
	 * 
	 * @param date
	 *            date
	 * @return date string
	 */
	public static String date2string(final Date date) {
		return date2string(date, FORMAT_DATE);
	}

	/**
	 * Gets string from date.
	 * 
	 * @param date
	 *            date
	 * @param format
	 *            format string
	 * @return date string
	 */
	public static String date2string(final Date date, final String format) {
		if (date == null) {
			return null;
		}
		final SimpleDateFormat dateFormat = getDateFormatInstance(format);

		return dateFormat.format(date);
	}

	/**
	 * 
	 */
	public static Date getCurrentTime() {
		final Calendar cal = Calendar.getInstance();

		if (cal == null) {
			return null;
		}
		return cal.getTime();
	}

	private static SimpleDateFormat getDateFormatInstance(final String format) {
		return new SimpleDateFormat(format);
	}

	public static void main(final String arg[]) {
		final Date curTime = DateString.getCurrentTime();

		System.out.println("current date=" + curTime.toString());
		System.out.println(DateString.date2string(curTime));
		final Date date = DateString.string2date(arg[0]);

		System.out.println(date.toString());
		final DateString dtstr = new DateString();

		System.out.println(dtstr.getTime().toString());
		System.out.println(dtstr.getTimeString());
	}

	/**
	 * Gets date from string.
	 * 
	 * @param date
	 *            date string
	 * @return date
	 */
	public static Date string2date(final String date) {
		return string2date(date, FORMAT_DATE);
	}

	/**
	 * Gets date from string.
	 * 
	 * @param date
	 *            date string
	 * @param format
	 *            format string
	 * @return date
	 */
	public static Date string2date(final String date, final String format) {
		if (date == null) {
			return null;
		}
		final SimpleDateFormat dateFormat = getDateFormatInstance(format);
		Date dt = null;

		try {
			dt = dateFormat.parse(date);
		} catch (final ParseException excpt) {
			return null;
		}
		return dt;
	}

	Date _date = null;

	/**
	 * Constructor.
	 */
	public DateString() {
		_date = getCurrentTime();
	}

	public Date getTime() {
		return _date;
	}

	public String getTimeString() {
		return date2string(_date);
	}
}
