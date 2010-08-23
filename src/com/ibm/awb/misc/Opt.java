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

    static Hashtable opts = new Hashtable();
    static Vector v = new Vector();

    static public interface Proc {
	public boolean exec(String a) throws Exception;
    }

    private String name;
    private String message;

    static class VOpt extends Opt {
	private String prop_name;

	VOpt(String n, String p, String m) {
	    super(n, m);
	    this.prop_name = p;
	}

	@Override
	public boolean match(String val) {
	    if (val == null) {
		return false;
	    }
	    System.getProperties().put(this.prop_name, val);
	    return true;
	}
    };

    static class SOpt extends Opt {
	String prop_name;
	String svalue;

	SOpt(String n, String p, String v, String m) {
	    super(n, m);
	    this.prop_name = p;
	    this.svalue = v;
	}

	@Override
	public boolean match(String val) {
	    if (val != null) {
		return false;
	    }
	    System.getProperties().put(this.prop_name, this.svalue);
	    return true;
	}
    };

    static class POpt extends Opt {
	Proc proc;

	POpt(String n, Proc r, String m) {
	    super(n, m);
	    this.proc = r;
	}

	@Override
	public boolean match(String val) {
	    try {
		return this.proc.exec(val);
	    } catch (Exception ex) {
		ex.printStackTrace();
		return false;
	    }
	}
    };

    static class DOpt extends Opt {

	DOpt(String n) {
	    super(n, null);
	}

	@Override
	public boolean match(String val) {
	    return true;
	}
    };

    static Opt o[] = {
	Opt.Entry("-help", new Proc() {
	    @Override
	    public boolean exec(String a) {
		message();
		return true;
	    }
	}, " -help        print this message"),
	Opt.Entry("-verbose", "verbose", "true", " -verbose     turn on verbose mode"),
	Opt.Entry("-port", "port", " -port <port> set the port number"), };

    Opt(String n, String m) {
	this.name = n;
	this.message = m;
    }

    static public boolean checkopt(String args[]) {
	if (args == null) {
	    return true;
	}
	for (String arg : args) {
	    if (arg != null) {
		message();
		return false;
	    }
	}
	return true;
    }

    static public Opt Entry(String n) {
	return new DOpt(n);
    }

    static public Opt Entry(String n, Proc r, String m) {
	return new POpt(n, r, m);
    }

    static public Opt Entry(String n, String p, String m) {
	return new VOpt(n, p, m);
    }

    static public Opt Entry(String n, String p, String val, String m) {
	return new SOpt(n, p, val, m);
    }

    static public void getopt(String args[]) {
	if (args == null) {
	    return;
	}
	System.getProperties();

	for (int i = 0; i < args.length; i++) {
	    if (args[i] == null) {
		continue;
	    }
	    Opt r = (Opt) opts.get(args[i]);

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

    static public String getopt(String val, String args[], String def_value) {
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

    static public void main(String args[]) {
	Opt.getopt(args);
	Opt.checkopt(args);
	if (Boolean.getBoolean("help")) {
	    Opt.message();
	    System.exit(0);
	}
	System.out.println(Integer.getInteger("port", -99));
	System.out.println(Boolean.getBoolean("verbose"));
    }

    abstract boolean match(String arg);

    static public void message() {
	Enumeration e = v.elements();
	String name = System.getProperty("program-name", "java com.ibm.aglets.tahiti.Main");

	System.err.println("usage: " + name + " [-options] \n"
		+ "where options include: \n");
	while (e.hasMoreElements()) {
	    Opt r = (Opt) e.nextElement();

	    if (r.message != null) {
		System.err.println(r.message);
	    }
	}
    }

    static public void setopt(Opt[] options) {
	for (Opt option : options) {
	    opts.put(option.name, option);
	    v.addElement(option);
	}
    }
}
