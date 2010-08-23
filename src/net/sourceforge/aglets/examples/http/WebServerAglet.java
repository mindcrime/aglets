package net.sourceforge.aglets.examples.http;

/*
 * @(#)WebServerAglet.java
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The program is provided "as is" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 */

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.RequestRefusedException;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageManager;
import com.ibm.awb.misc.Encoding;

/**
 * WebServerAglet is an aglet which behaves like WebServer. Please enable the
 * HTTP messaging feature in the configuration panel Options -> Network
 * Cofiguration -> Others. If the aglet is successfully created, please try
 * 
 * <pre>
 * http://aglet.server:434/aglets/default/test/index.html
 * </pre>
 * 
 * in your web browser. The port number have to be same number on which the
 * aglet server is running. (434 by default)
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:54 $
 * @author Mitsuru Oshima
 */
public class WebServerAglet extends Aglet {
    static private final Encoding ENCODING = Encoding.getDefault();
    static private final String ENCODING_JAVA = ENCODING.getJavaEncoding();
    static private final String CHARSET_PAGE = ENCODING.getHTMLCharset();
    static private String META_TAG = null;
    static {
	META_TAG = "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html;";
	if (CHARSET_PAGE != null) {
	    META_TAG += " charset=" + CHARSET_PAGE;
	}
	META_TAG += "\">";
    }

    String indexPage = null;

    //
    // If it's http request.
    //
    boolean handleHttpRequest(Message m, PrintWriter p) {
	System.out.println(m);
	if (m.sameKind("index.html") || m.sameKind("")) {
	    p.println(this.indexPage);
	    p.flush();
	} else if (m.sameKind("next.html")) {
	    p.println("<HTML>");
	    p.println("<HEAD>");
	    p.println(META_TAG);
	    p.println("<TITLE>");
	    p.println("NEXT");
	    p.println("</TITLE>");
	    p.println("</HEAD>");
	    p.println("<BODY>");
	    p.println("<H1> Here is a list of proxies in <TT>"
		    + this.getAgletContext().getHostingURL() + "</TT></H1>");
	    Enumeration e = this.getAgletContext().getAgletProxies(ACTIVE
		    | INACTIVE);

	    p.println("<PRE>");
	    while (e.hasMoreElements()) {
		AgletProxy proxy = (AgletProxy) e.nextElement();

		try {
		    p.println(proxy.getAgletInfo().toString());
		} catch (Exception ex) {
		    ex.printStackTrace(p);
		}
	    }
	    p.println("</PRE>");
	    p.println("</BODY>");
	    p.println("</HTML>");
	    p.flush();
	    m.sendReply("text/html");
	} else if (m.sameKind("go")) {
	    System.out.println((String) m.getArg("location"));
	    String l = (String) m.getArg("location");

	    if (l.startsWith("atp:")) {
		l = l.substring(4);
		if (l.indexOf(':') < 0) {
		    p.println("<HTML>");
		    p.println("<HEAD>");
		    p.println(META_TAG);
		    p.println("<TITLE>");
		    p.println("ILLEGAL INPUT");
		    p.println("</TITLE>");
		    p.println("</HEAD>");
		    p.println("<BODY>");
		    p.println("<H1>");
		    p.println("Please specify port number. default = 434");
		    p.println("</H1>");
		    p.println("</BODY>");
		    p.println("</HTML>");
		    p.flush();
		    m.sendReply("text/html");
		    return true;
		}
	    }

	    try {
		p.println("<HTML>");
		p.println("<HEAD>");
		p.println(META_TAG);
		p.println("<TITLE>");
		p.println("MOVING TO");
		p.println("</TITLE>");
		p.println("</HEAD>");
		p.println("<BODY>");
		p.println("<H1> Moving to...! </H1>");
		String contextName = this.getAgletContext().getName();

		if (contextName.equals("")) {
		    contextName = "default";
		}

		p.println("<a href= \"http:" + l + "/aglets/" + contextName
			+ "/" + this.getAgletID()
			+ "/index.html\" TARGET=_top> atp:" + l + " </a>");
		p.println("Click above link to trace me! <BR>");
		p.println("</BODY>");
		p.println("</HTML>");
		p.flush();
		m.sendReply("text/html");

		this.dispatch(new java.net.URL("atp:" + l));

	    } catch (IOException ex) {
		ex.printStackTrace();
	    } catch (RequestRefusedException ex) {
		ex.printStackTrace();
	    }
	} else {
	    return false;
	}
	return true;
    }

    @Override
    public boolean handleMessage(Message m) {

	//
	// This is just a temporary solution.
	//
	Object o = m.getArg("cgi-response");
	if ((o != null) && (o instanceof OutputStream)) {
	    OutputStream os = (OutputStream) o;
	    OutputStreamWriter osw = null;

	    try {
		osw = new OutputStreamWriter(os, ENCODING_JAVA);
	    } catch (UnsupportedEncodingException excpt) {
		osw = new OutputStreamWriter(os);
	    }
	    PrintWriter pw = new PrintWriter(osw);

	    return this.handleHttpRequest(m, pw);
	}
	return true;
    }

    /*
     * index page
     */
    void indexPage() {
	StringBuffer b = new StringBuffer();

	b.append("<HTML>");
	b.append("<HEAD>");
	b.append(META_TAG);
	b.append("<TITLE>");
	b.append("CGI TEST");
	b.append("</TITLE>");
	b.append("</HEAD>");
	b.append("<BODY>");
	b.append("<H1> Welcome to WebServerAglet! </H1>");
	b.append("<a href=next.html> List Proxies </a> <BR>");
	b.append("<FORM METHOD=GET ACTION=go>");
	b.append("<INPUT NAME=location VALUE=\"atp://your.host\">");
	b.append("<INPUT TYPE=submit VALUE=GO!> <BR>");
	b.append("<P><FONT color=#FF0000>note:</FONT> Check box ");
	b.append("for \"Accept HTTP Request as a message\"<BR>");
	b.append("in \"Network Preference\" of target server <B>must be checked</B>.");
	b.append("</BODY>");
	b.append("</HTML>");
	this.indexPage = b.toString();
    }

    @Override
    public void onCreation(Object init) {

	//
	// this is just a convension.
	//
	this.getAgletContext().setProperty("name.test", this.getAgletID());

	//
	// Creating pages in advance.
	//
	this.indexPage();

	//
	// Accepts http requests concurrently.
	//
	this.getMessageManager().setPriority("index.html", MessageManager.NOT_QUEUED);
	this.getMessageManager().setPriority("next.html", MessageManager.NOT_QUEUED);
    }
}
