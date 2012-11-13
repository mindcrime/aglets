package com.ibm.maf.atp;

/*
 * @(#)HttpProxyConnection.java
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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Hashtable;

import com.ibm.atp.ContentOutputStream;
import com.ibm.awb.misc.Resource;

/**
 * @version 1.00 $Date :$
 * @author Mitsuru Oshima
 */
class HttpProxyConnection implements Connection {

	private static final String CRLF = "\r\n";

	private static String noProxies[];
	private static String proxyHost;
	private static int proxyPort;

	// private static String proxyService;

	private static boolean enableProxy = false;

	static {
		final Resource atp_res = Resource.getResourceFor("atp");

		enableProxy = atp_res.getBoolean("atp.useHttpProxy", false);

		final Resource system_res = Resource.getResourceFor("system");

		try {
			proxyPort = system_res.getInteger("http.proxyPort", -1);
			proxyHost = system_res.getString("http.proxyHost", null);
			noProxies = system_res.getStringArray("http.nonProxyHosts", " \t,");
			verboseOut("proxy = " + proxyHost + ':' + proxyPort);
		} catch (final Throwable ex) {
			proxyHost = null;
		}
	}

	/*
	 * 
	 */
	static boolean useHttpProxy(final URL url) {
		boolean useProxy = false;

		if (enableProxy && (proxyHost != null)) {
			final String host = url.getHost();

			useProxy = true;
			for (final String noProxie : noProxies) {
				if (host.endsWith(noProxie) || host.startsWith(noProxie)) {
					useProxy = false;
					break;
				}
			}
		}
		if (useProxy) {
			verboseOut("use Proxy for " + url);
		} else {
			verboseOut("no proxy for " + url);
		}
		return useProxy;
	}
	static final private void verboseOut(final String msg) {
		Daemon.verboseOut(msg);
	}
	OutputStream out;
	Socket _socket;

	private boolean established = false;

	ContentOutputStream _outputStream;

	InputStream _inputStream;

	HttpProxyConnection(final URL url, final int defaultPort) throws IOException {
		_socket = new Socket(proxyHost, proxyPort);
		try {
			final Socket fSocket = _socket;

			out = (OutputStream) AccessController.doPrivileged(new PrivilegedExceptionAction() {
				@Override
				public Object run() throws IOException {
					return fSocket.getOutputStream();
				}
			});
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		final PrintStream p = new PrintStream(out);
		final int port = url.getPort();
		final URL referer = new URL("http", url.getHost(), (port == -1 ? defaultPort
				: port), url.getFile());

		p.print("POST " + referer + " HTTP/1.0" + CRLF);
		p.print("Referer: " + referer + CRLF);
		p.print("User-Agent: Java1.0.2(ATP)" + CRLF);
		p.print("Accept: application/x-atp" + CRLF);
		p.print("Content-Type: application/x-atp" + CRLF);

		// connection is established
		established = true;
	}

	@Override
	public void close() throws IOException {
		if (_socket != null) {
			_socket.close();
		}

		// connection is NOT established
		established = false;
	}

	@Override
	public String getAuthenticatedSecurityDomain() {
		return null;
	}

	@Override
	synchronized public InputStream getInputStream() throws IOException {
		if (_inputStream == null) {
			_inputStream = _socket.getInputStream();
			readHeaders();
		}
		return _inputStream;
	}

	@Override
	public String getMessage() {
		return null;
	}

	@Override
	synchronized public OutputStream getOutputStream() throws IOException {
		if (_outputStream == null) {
			_outputStream = new ContentOutputStream(out, true);
		}
		return _outputStream;
	}

	@Override
	public boolean isEstablished() {
		return established;
	}

	private void readHeaders() throws IOException {
		final DataInputStream in = new DataInputStream(_inputStream);

		// BufferedReader in = new BufferedReader(new
		// InputStreamReader(_inputStream));
		final String statusline = in.readLine();

		//
		verboseOut("statusline = " + statusline);

		//
		if (statusline == null) {
			throw new IOException("No response.");
		}

		//
		int index = statusline.indexOf(' ');
		final String string1 = statusline.substring(0, index);
		final int index2 = statusline.indexOf(' ', index + 1);
		final String string2 = statusline.substring(index + 1, index2);
		final String string3 = statusline.substring(index2 + 1);

		if (string1.startsWith("HTTP/1.") == false) {
			throw new IOException("Invalid response.");
		}

		//
		if (Integer.parseInt(string2) != 200) {
			throw new IOException(string3);
		}

		//
		final Hashtable headers = new Hashtable();

		while (true) {
			final String field = in.readLine();

			verboseOut(field);
			try {
				if (field.length() == 0) {
					break;
				}
				index = field.indexOf(':');
				String key = field.substring(0, index);
				String value = field.substring(index + 1);

				key = key.toLowerCase().trim();
				value = value.toLowerCase().trim();
				headers.put(key, value);
			} catch (final Exception e) {
				throw new IOException(e.getMessage());
			}
		}
		final String type = (String) headers.get("content-type");

		if ("application/x-atp".equalsIgnoreCase(type) == false) {
			throw new IOException("Unknown Content-Type:" + type);
		}
	}

	@Override
	public void sendRequest() throws IOException {
		_outputStream.sendContent();

		// getOutputStream().flush();
	}
}
