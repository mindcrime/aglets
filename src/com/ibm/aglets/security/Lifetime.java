package com.ibm.aglets.security;

/*
 * @(#)Lifetime.java
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

// - import com.ibm.awb.misc.DateString;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * The <tt>Lifetime</tt> class shows life-time of an aglet.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public final class Lifetime implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4337804352929841734L;
	/**
	 * Infinite lifetime.
	 */
	public final static Date UNLIMITED = null;
	public final static String UNLIMITED_LABEL = "unlimited";

	private final static String FORMAT_DATE = "yyyy.MM.dd-HH:mm:ss.SSS(z)";
	private final static String RELATIVE = "+";

	/**
	 * Returns whether the time is over the limit of lifetime.
	 * 
	 * @return true if the time is over the limit of lifetime, false otherwise
	 */
	public static Date currentDate() {
		final Calendar cal = Calendar.getInstance();

		return cal.getTime();
	}
	/**
	 * Returns whether the lifetime is limited.
	 * 
	 * @param limit
	 *            date limit
	 * @return true if the lifetime is limited, false otherwise
	 */
	public static boolean isLimited(final Date limit) {
		return limit != UNLIMITED;
	}
	/**
	 * Returns whether the lifetime is limited.
	 * 
	 * @param lifetime
	 *            lifetime
	 * @return true if the lifetime is limited, false otherwise
	 */
	public static boolean isLimited(final Lifetime lifetime) {
		return isLimited(lifetime.getLimit());
	}

	/**
	 * Returns whether the lifetime is limited.
	 * 
	 * @param lifetime
	 *            lifetime
	 * @return true if the lifetime is limited, false otherwise
	 */
	public static boolean isLimited(final String lifetime) {
		if (lifetime == null) {
			return true;
		}
		return !lifetime.equalsIgnoreCase(UNLIMITED_LABEL);
	}

	/**
	 * Returns whether the time is over the limit of lifetime.
	 * 
	 * @return true if the time is over the limit of lifetime, false otherwise
	 */
	public static boolean isOver(final Date limit) {
		if (!isLimited(limit)) {
			return false;
		}
		return currentDate().after(limit);
	}

	/**
	 * For test.
	 */
	public static void main(final String[] arg) {
		final Lifetime l = new Lifetime("+36000000");

		System.out.println("Life : " + l.toString());
		for (final String element : arg) {
			final Lifetime life = new Lifetime(element);

			System.out.println(element + " : " + life.toString());
			l.limit(life);
			System.out.println("Life : " + l.toString());
		}
	}

	private boolean _relative = true;

	private long _life = 0;

	private Date _limit = null;

	/**
	 * Creates lifetime.
	 */
	public Lifetime() {
		this(UNLIMITED);
	}

	/**
	 * Creates lifetime.
	 */
	public Lifetime(final Date limit) {
		this.setAbsoluteLifetime(limit);
	}

	/**
	 * Creates lifetime as a copy.
	 */
	public Lifetime(final Lifetime lifetime) {
		this.setLifetime(lifetime);
	}

	/**
	 * Creates lifetime.
	 */
	public Lifetime(final String lifetime) throws NumberFormatException {
		this.setLifetime(lifetime);
	}

	/**
	 * Returns a clone of the lifetime.
	 * 
	 * @return a clone of the lifetime
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new Lifetime(this);
	}

	/**
	 * Returns whether the lifetime equal to lifetime.
	 * 
	 * @return true if the lifetime equal to lifetime, false otherwise
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof Lifetime) {
			final Lifetime lifetime = (Lifetime) obj;
			final boolean l1 = this.isLimited();
			final boolean l2 = lifetime.isLimited();

			if (l1 && l2) {
				return _limit.equals(lifetime.getLimit());
			} else if (!l1 && !l2) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * Gets the available lifetime.
	 * 
	 * @return available lifetime
	 */
	public Date getLimit() {
		return _limit;
	}

	/**
	 * Returns whether the life time is absolute.
	 * 
	 * @return ture if the life time is absolute, false otherwise
	 */
	public boolean isAbsolute() {
		return !isRelative();
	}

	/**
	 * Returns whether the lifetime is greater than lifetime.
	 * 
	 * @param date
	 *            absolute life time
	 * @return true if the lifetime is greater than lifetime, false otherwise
	 */
	public boolean isGreaterThan(final Date date) {
		if (date == null) {
			return true;
		}
		if (!this.isLimited()) {
			return true;
		}

		// both are finite
		return _limit.after(date);
	}

	/**
	 * Returns whether the lifetime is greater than lifetime.
	 * 
	 * @param lifetime
	 *            absolute life time
	 * @return true if the lifetime is greater than lifetime, false otherwise
	 */
	public boolean isGreaterThan(final Lifetime lifetime) {
		if (lifetime == null) {
			return true;
		}
		if (!this.isLimited()) {
			return true;
		}

		// both are finite
		return _limit.after(lifetime.getLimit());
	}

	/**
	 * Returns whether the lifetime is greater than lifetime.
	 * 
	 * @param life
	 *            absolute life time [milliseconds]
	 * @return true if the lifetime is greater than lifetime, false otherwise
	 */
	public boolean isGreaterThan(final long life) {
		if (!this.isLimited()) {
			return true;
		}

		// both are finite
		return _limit.getTime() > life;
	}

	/**
	 * Returns whether the lifetime is limited.
	 * 
	 * @return true if the lifetime is limited, false otherwise
	 */
	public boolean isLimited() {
		return isLimited(_limit);
	}

	/**
	 * Returns whether the time is over the limit of lifetime.
	 * 
	 * @return true if the time is over the limit of lifetime, false otherwise
	 */
	public boolean isOver() {
		return isOver(_limit);
	}

	/**
	 * Returns whether the life time is relative.
	 * 
	 * @return ture if the life time is relative, false otherwise
	 */
	public boolean isRelative() {
		return _relative;
	}

	/**
	 * Limits the lifetime.
	 * 
	 * @param date
	 *            absolute time
	 */
	public void limit(final Date date) {
		if (date == null) {
			return;
		}
		if (this.isGreaterThan(date)) {
			if (isRelative()) {
				subtractLifetime(_limit.getTime() - date.getTime());
			} else {
				this.setAbsoluteLifetime(date);
			}
		}
	}

	/**
	 * Limits the lifetime.
	 * 
	 * @param lifetime
	 *            lifetime
	 */
	public void limit(final Lifetime lifetime) {
		if (lifetime == null) {
			return;
		}
		this.limit(lifetime.getLimit());
	}

	/**
	 * Limits the lifetime.
	 * 
	 * @param life
	 *            absolute life time [milliseconds]
	 */
	public void limit(final long life) {
		if (this.isGreaterThan(life)) {
			if (isRelative()) {
				subtractLifetime(_limit.getTime() - life);
			} else {
				this.setAbsoluteLifetime(life);
			}
		}
	}

	/**
	 * Sets life as absolute time.
	 * 
	 * @param date
	 *            absolute life time
	 */
	private void setAbsoluteLifetime(final Date date) {
		_relative = false;
		_life = 0;
		setLimit(date);
	}

	/**
	 * Sets life as absolute time.
	 * 
	 * @param life
	 *            absolute life time [milliseconds]
	 */
	private void setAbsoluteLifetime(final long life) {
		this.setAbsoluteLifetime(new Date(life));
	}

	/**
	 * Sets the lifetime.
	 * 
	 * @param lifetime
	 *            lifetime
	 */
	public void setLifetime(final Date lifetime) {
		if (lifetime != null) {
			this.setAbsoluteLifetime(lifetime);
		}
	}

	/**
	 * Sets the lifetime.
	 * 
	 * @param lifetime
	 *            lifetime
	 */
	public void setLifetime(final Lifetime lifetime) {
		if (lifetime != null) {
			_relative = lifetime._relative;
			_life = lifetime._life;
			_limit = lifetime._limit;
		}
	}

	/**
	 * Sets the lifetime.
	 * 
	 * @param lifetime
	 *            lifetime
	 */
	public void setLifetime(final String lifetime) throws NumberFormatException {
		if (lifetime == null) {
			throw new NumberFormatException("Lifetime description is null.");
		}
		if (!isLimited(lifetime)) {
			this.setAbsoluteLifetime(UNLIMITED);
		} else {
			if (lifetime.startsWith(RELATIVE)) {

				// relative time : +nnnnnn [milliseconds]
				final long relativeMilliseconds = Long.parseLong(lifetime.substring(1));

				if (relativeMilliseconds > 0) {
					setRelativeLifetime(relativeMilliseconds);
				} else {
					throw new NumberFormatException("invalid lifetime description : "
							+ lifetime);
				}
			} else {

				// absolute time
				final Date date = DateString.string2date(lifetime, FORMAT_DATE);

				if (date != null) {

					// absolute time : yyyy.MM.dd HH:mm:ss.SSS(z)
					this.setAbsoluteLifetime(date);
				} else {

					// absolute time : nnnnnn [milliseconds]
					final long absoluteMilliseconds = Long.parseLong(lifetime);

					if (absoluteMilliseconds > 0) {
						this.setAbsoluteLifetime(absoluteMilliseconds);
					} else {
						throw new NumberFormatException("invalid lifetime description : "
								+ lifetime);
					}
				}
			}
		}
	}

	/**
	 * Sets limit of the lifetime.
	 * 
	 * @param limit
	 *            limit
	 */
	private void setLimit(final Date limit) {
		if (isLimited(limit)) {
			_limit = limit;
		} else {

			// unlimited value
			_limit = UNLIMITED;
		}
	}

	/**
	 * Sets life as relative time.
	 * 
	 * @param life
	 *            relative life time [milliseconds]
	 */
	private void setRelativeLifetime(final long life) {
		_relative = true;
		_life = life;
		setLimit(new Date(currentDate().getTime() + life));
	}

	/**
	 * Subtracts lifetime.
	 * 
	 * @param life
	 *            life time [milliseconds]
	 */
	public void subtractLifetime(final long life) {
		if ((life > 0) && this.isLimited()) {
			long delta = life;

			if (isRelative()) {

				// relative time
				if (_life < life) {
					delta = _life;
				}
				_life -= delta;
			}
			setLimit(new Date(_limit.getTime() - delta));
		}
	}

	/**
	 * Returns a string of the lifetime.
	 * 
	 * @return a string of the lifetime
	 */
	@Override
	public String toString() {
		return this.toString(isRelative());
	}

	/**
	 * Returns a string of the lifetime.
	 * 
	 * @param relative
	 *            in relative format
	 * @return a string of the lifetime
	 */
	public String toString(final boolean relative) {
		return this.toString(relative, FORMAT_DATE);
	}

	/**
	 * Returns a string of the lifetime.
	 * 
	 * @param relative
	 *            in relative format
	 * @param format
	 *            format of date string
	 * @return a string of the lifetime
	 */
	public String toString(final boolean relative, final String format) {
		if (!this.isLimited()) {
			return UNLIMITED_LABEL;
		} else {
			if (relative) {
				return RELATIVE + _life;
			} else {
				if (format != null) {
					return DateString.date2string(_limit, format);
				} else {
					return _limit.toString();
				}
			}
		}
	}
}
