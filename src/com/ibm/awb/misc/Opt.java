package com.ibm.awb.misc;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/*
 * @(#)Opt.java
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

abstract public class Opt {

	static class DOpt extends Opt {

		DOpt(final String n) {
			super(n, null);
		}

		@Override
		public boolean match(final String val) {
			return true;
		}
	}
	static class POpt extends Opt {
		Proc proc;

		POpt(final String n, final Proc r, final String m) {
			super(n, m);
			proc = r;
		}

		@Override
		public boolean match(final String val) {
			try {
				return proc.exec(val);
			} catch (final Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}
	}

	static public interface Proc {
		public boolean exec(String a) throws Exception;
	}

	static class SOpt extends Opt {
		String prop_name;
		String svalue;

		SOpt(final String n, final String p, final String v, final String m) {
			super(n, m);
			prop_name = p;
			svalue = v;
		}

		@Override
		public boolean match(final String val) {
			if (val != null) {
				return false;
			}
			System.getProperties().put(prop_name, svalue);
			return true;
		}
	}
	static class VOpt extends Opt {
		private final String prop_name;

		VOpt(final String n, final String p, final String m) {
			super(n, m);
			prop_name = p;
		}

		@Override
		public boolean match(final String val) {
			if (val == null) {
				return false;
			}
			System.getProperties().put(prop_name, val);
			return true;
		}
	}

	static Hashtable opts = new Hashtable();;

	static Vector v = new Vector();;

	static public boolean checkopt(final String args[]) {
		if (args == null) {
			return true;
		}
		for (final String arg : args) {
			if (arg != null) {
				message();
				return false;
			}
		}
		return true;
	};

	static public Opt Entry(final String n) {
		return new DOpt(n);
	};

	static public Opt Entry(final String n, final Proc r, final String m) {
		return new POpt(n, r, m);
	}

	static public Opt Entry(final String n, final String p, final String m) {
		return new VOpt(n, p, m);
	}

	static public Opt Entry(final String n, final String p, final String val, final String m) {
		return new SOpt(n, p, val, m);
	}

	static public void getopt(final String args[]) {
		if (args == null) {
			return;
		}
		System.getProperties();

		for (int i = 0; i < args.length; i++) {
			if (args[i] == null) {
				continue;
			}
			final Opt r = (Opt) opts.get(args[i]);

			if (r == null) {
				return;
			}
			args[i] = null;
			String v = null;

			if (((i + 1) < args.length) && (args[i + 1] != null)
					&& (args[i + 1].charAt(0) != '-')) {
				v = args[i + 1];
			}
			if (r.match(v)) {
				args[i] = null;
				if (v != null) {
					i++;
					args[i] = null;
				}
			}
		}
	}

	static public String getopt(final String val, final String args[], final String def_value) {
		if (args == null) {
			return def_value;
		}
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith(val)) {
				if (((i + 1) < args.length) && (args[i + 1].charAt(0) != '-')) {
					return args[i + 1];
				}
			}
		}
		return def_value;
	}

	static public void main(final String args[]) {
		Opt.getopt(args);
		Opt.checkopt(args);
		if (Boolean.getBoolean("help")) {
			Opt.message();
			System.exit(0);
		}
		System.out.println(Integer.getInteger("port", -99));
		System.out.println(Boolean.getBoolean("verbose"));
	}

	static public void message() {
		final Enumeration e = v.elements();
		final String name = System.getProperty("program-name", "java com.ibm.aglets.tahiti.Main");

		System.err.println("usage: " + name + " [-options] \n"
				+ "where options include: \n");
		while (e.hasMoreElements()) {
			final Opt r = (Opt) e.nextElement();

			if (r.message != null) {
				System.err.println(r.message);
			}
		}
	}

	static public void setopt(final Opt[] options) {
		for (final Opt option : options) {
			opts.put(option.name, option);
			v.addElement(option);
		}
	}

	private final String name;

	private final String message;

	static Opt o[] = {
		Opt.Entry("-help", new Proc() {
			@Override
			public boolean exec(final String a) {
				message();
				return true;
			}
		}, " -help        print this message"),
		Opt.Entry("-verbose", "verbose", "true", " -verbose     turn on verbose mode"),
		Opt.Entry("-port", "port", " -port <port> set the port number"), };

	Opt(final String n, final String m) {
		name = n;
		message = m;
	}

	abstract boolean match(String arg);
}
