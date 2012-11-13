package com.ibm.aglets.tahiti;

/*
 * @(#)DigestTable.java
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

import java.util.Date;

import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.awb.misc.Resource;

/*
 * An object which is associated to a list item in the Tahiti main window.
 * 
 * @author Yoshiaki Mima
 */

final public class TahitiItem {
	final static int KEY_LASTUPDATE = 0;
	final static int KEY_TIMESTAMP = 1;
	final static int KEY_CLASSNAME = 2;

	static int _keyOrder = KEY_LASTUPDATE;
	static boolean _isAscentOrder = true;
	static boolean _isPrecise = true;
	static boolean _needUpdate = false; // true;
	static boolean _fInit = false;

	static boolean getPrecision() {
		return _isAscentOrder;
	}
	/*
	 * Class methods
	 */
	static void init() {
		if (_fInit) {
			return;
		}
		_fInit = true;

		final Resource tahiti_res = Resource.getResourceFor("tahiti");
		final String key = tahiti_res.getString("tahiti.itemkey", "event order");
		final String order = tahiti_res.getString("tahiti.itemorder", "ascent");
		final String precision = tahiti_res.getString("tahiti.itemprecision", "complete");

		// tahiti items view control
		if (key.equals("event order")) {
			setKeyItem(TahitiItem.KEY_LASTUPDATE);
		} else if (key.equals("creation time")) {
			setKeyItem(TahitiItem.KEY_TIMESTAMP);
		} else if (key.equals("class name")) {
			setKeyItem(TahitiItem.KEY_CLASSNAME);
		}
		;

		if (order.equals("ascent")) {
			setAscentOrder();
		} else if (order.equals("descent")) {
			setDescentOrder();
		}
		;

		if (precision.equals("complete")) {
			setPrecision(true);
		} else if (precision.equals("compact")) {
			setPrecision(false);
		}
		;

		// System.out.println("TahitiItem: key: " + key + " order: " + order +
		// " precision: " + precision);
	}
	static boolean isAscentOrder() {
		return _isAscentOrder;
	}
	static boolean isNeedUpdate() {
		final boolean retval = _needUpdate;

		_needUpdate = false;
		return retval;
	}
	static void setAscentOrder() {
		if (_isAscentOrder) {
			return;
		}
		_isAscentOrder = true;
		_needUpdate = true;
	}
	static void setDescentOrder() {
		if (!_isAscentOrder) {
			return;
		}
		_isAscentOrder = false;
		_needUpdate = true;
	}
	static void setKeyItem(final int key) {
		if (_keyOrder == key) {
			return;
		}
		_keyOrder = key;
		_needUpdate = true;
	}

	static void setPrecision(final boolean precision) {
		if (_isPrecise == precision) {
			return;
		}
		_isPrecise = precision;
		_needUpdate = true;
	}

	AgletProxy _proxy;

	Date _date;

	String _timestamp;

	String _timestampSimple;

	String _classname;

	String _classnameSimple;

	boolean _isValid = false;

	/*
	 * Constractor
	 */
	 public TahitiItem() {
		 _keyOrder = KEY_LASTUPDATE;
		 _needUpdate = true;
	 }

	 public TahitiItem(final AgletProxy proxy) {
		 try {
			 if (_isValid = proxy.isValid()) {
				 _proxy = proxy;
				 final AgletInfo info = proxy.getAgletInfo();

				 _date = new Date(info.getCreationTime());
				 _classname = info.getAgletClassName();
			 }
		 } catch (final InvalidAgletException ex) {
		 } catch (final RuntimeException ex) {
			 ex.printStackTrace();
		 } finally {
		 }
	 }

	 boolean checkProxy(final AgletProxy proxy) {
		 return (_proxy == proxy);
	 }

	 int compareTo(final TahitiItem tahitiItem) {
		 int cmp = 0;

		 switch (_keyOrder) {
			 case KEY_LASTUPDATE:
				 cmp = -1;
				 break;
			 case KEY_TIMESTAMP:
				 if (_date.after(tahitiItem._date)) {
					 cmp = 1;
				 } else if (_date.before(tahitiItem._date)) {
					 cmp = -1;
				 } else {
					 cmp = _classname.compareTo(tahitiItem._classname);
				 }
				 break;
			 case KEY_CLASSNAME:
				 cmp = _classname.compareTo(tahitiItem._classname);
				 if (cmp == 0) {
					 if (_date.after(tahitiItem._date)) {
						 cmp = 1;
					 } else if (_date.before(tahitiItem._date)) {
						 cmp = -1;
					 } else {
						 cmp = 0;
					 }
				 }
				 break;
			 default:
				 break;
		 }

		 if (_isAscentOrder == false) {
			 cmp = -cmp;
		 }

		 return cmp;
	 }

	 AgletProxy getAgletProxy() {
		 return _proxy;
	 }

	 /*
	  * Instance methods
	  */
	 String getClassName() {
		 if (_isPrecise) {
			 return _classname;
		 } else {
			 int len = 0;

			 len = _classname.length();
			 if (len > 20) {

				 // replace last string "Aglet" with ".."
				 if (_classname.endsWith("Aglet")) {
					 _classnameSimple = _classname.substring(0, len - 5)
					 + "..";
				 } else {
					 _classnameSimple = new String(_classname);
				 }

				 // replace left part of classname with ".."
				 len = _classnameSimple.length();
				 if (len > 20) {
					 _classnameSimple = ".."
						 + _classnameSimple.substring(len - 18, len);
				 } else {
					 _classnameSimple = (_classnameSimple + "   ").substring(0, 20);
				 }
			 } else {
				 _classnameSimple = (_classname + "                    ").substring(0, 20);
			 }
			 return _classnameSimple;
		 }
	 }

	 String getText() {
		 String text;

		 switch (_keyOrder) {
			 case KEY_LASTUPDATE:
				 text = getClassName() + " : " + getTimeStamp();
				 break;
			 case KEY_TIMESTAMP:
				 text = getTimeStamp() + " : " + getClassName();
				 break;
			 case KEY_CLASSNAME:
				 text = getClassName() + " : " + getTimeStamp();
				 break;
			 default:
				 text = getTimeStamp() + " : " + getClassName();
				 break;
		 }

		 return text;
	 }

	 String getTimeStamp() {
		 _timestamp = _date.toString();
		 if (_isPrecise) {
			 return _timestamp;
		 } else {
			 _timestampSimple = _timestamp.substring(11, 19);

			 return _timestampSimple;
		 }
	 }
}
