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

    Date _date = null;

    /**
     * Constructor.
     */
    public DateString() {
	this._date = getCurrentTime();
    }

    /**
     * Gets string from date.
     * 
     * @param date
     *            date
     * @return date string
     */
    public static String date2string(Date date) {
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
    public static String date2string(Date date, String format) {
	if (date == null) {
	    return null;
	}
	SimpleDateFormat dateFormat = getDateFormatInstance(format);

	return dateFormat.format(date);
    }

    /**
     * 
     */
    public static Date getCurrentTime() {
	Calendar cal = Calendar.getInstance();

	if (cal == null) {
	    return null;
	}
	return cal.getTime();
    }

    private static SimpleDateFormat getDateFormatInstance(String format) {
	return new SimpleDateFormat(format);
    }

    public Date getTime() {
	return this._date;
    }

    public String getTimeString() {
	return date2string(this._date);
    }

    public static void main(String arg[]) {
	Date curTime = DateString.getCurrentTime();

	System.out.println("current date=" + curTime.toString());
	System.out.println(DateString.date2string(curTime));
	Date date = DateString.string2date(arg[0]);

	System.out.println(date.toString());
	DateString dtstr = new DateString();

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
    public static Date string2date(String date) {
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
    public static Date string2date(String date, String format) {
	if (date == null) {
	    return null;
	}
	SimpleDateFormat dateFormat = getDateFormatInstance(format);
	Date dt = null;

	try {
	    dt = dateFormat.parse(date);
	} catch (ParseException excpt) {
	    return null;
	}
	return dt;
    }
}
