package com.ibm.maf.atp;

/*
 * @(#)Handler.java
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

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.sourceforge.aglets.log.AgletsLogger;

import com.ibm.aglet.Ticket;
import com.ibm.aglet.system.AgletRuntime;
import com.ibm.awb.misc.Resource;
import com.ibm.maf.AgentSystemHandler;
import com.ibm.maf.AgentSystemInfo;
import com.ibm.maf.FinderNotFound;
import com.ibm.maf.MAFAgentSystem;
import com.ibm.maf.MAFExtendedException;
import com.ibm.maf.MAFFinder;

public class Handler implements AgentSystemHandler {
	private static AgletsLogger logger = AgletsLogger.getLogger(Handler.class.getName());
	static final int DEFAULT_PORT = 4434;

	static boolean initialized = false;

	// static private int MAX_RETRY = 3;

	private static String getFullyQualifiedHostName()
	throws UnknownHostException {
		final Resource res = Resource.getResourceFor("atp");

		if (res.getBoolean("atp.offline", false)) {
			return "localhost";
		}

		final InetAddress host = InetAddress.getLocalHost();

		if (host == null) {
			throw new UnknownHostException("Illegal local host.");
		}

		final String ipaddr = host.getHostAddress();

		if ((ipaddr == null) || ipaddr.equals("")) {
			throw new UnknownHostException("IP address of local host does not exist.");
		}

		if (res.getBoolean("atp.useip", false)) {
			logger.debug("Hostname: " + ipaddr);
			return ipaddr;
		}

		String hostname = null;

		if (res.getBoolean("atp.resolve", false)) {
			final InetAddress canonhost = InetAddress.getByName(ipaddr);

			if (canonhost != null) {
				hostname = canonhost.getHostName();
			} else {
				hostname = host.getHostName();
			}
			if ((hostname == null) || hostname.equals("")) {
				throw new UnknownHostException("No host name.");
			}
		} else {
			hostname = InetAddress.getLocalHost().getHostName();
		}

		final String domain = res.getString("atp.domain");

		if (domain != null) {
			if (hostname.indexOf('.') < 0) {

				// you may not have any domain name
				hostname = hostname + "." + domain;
				InetAddress.getByName(hostname);
			} else {
				System.out.println("You cannot set domain name");
			}
		} else if (hostname.indexOf('.') < 0) {
			System.out.println("[Warning: The hostname seems not having domain name.");
			System.out.println(" Please try -resolve option to resolve the fully qualified hostname");
			System.out.println(" or use -domain option to manually specify the domain name.]");
		}

		return hostname;
	}

	@Override
	public MAFAgentSystem getMAFAgentSystem(String address)
	throws UnknownHostException {

		int to = -1;

		if (address.startsWith("//") == true) {
			address = address.substring(2);
			to = address.indexOf('/');

		} else if (address.startsWith("/")) {
			address = address.substring(1);
			to = address.indexOf('/');

		} else {
			to = address.indexOf('/');

		}

		if (to >= 0) {
			address = address.substring(0, to);
		}

		// System.out.println("get..." + address);

		final MAFAgentSystem local = Daemon.getLocalAgentSystem(address);

		if (local != null) {
			return local;
		}

		return new MAFAgentSystem_ATPClient(address);
	}

	@Override
	public MAFAgentSystem getMAFAgentSystem(final Ticket ticket)
	throws UnknownHostException {
		final MAFAgentSystem local = Daemon.getLocalAgentSystem(ticket);

		if (local != null) {
			return local;
		}
		return new MAFAgentSystem_ATPClient(ticket);
	}

	@Override
	synchronized public void initMAFAgentSystem(final MAFAgentSystem local)
	throws MAFExtendedException {
		try {
			if (initialized) {
				return;
			}
			initialized = true;

			/*
			 * initialize default resource default values installed within the
			 * code like here will never be maintained in the property file.
			 */
			final java.util.Properties sys_props = System.getProperties();
			final String s_port = sys_props.getProperty("maf.port", String.valueOf(DEFAULT_PORT));
			int port;

			try {
				port = Integer.parseInt(s_port);
			} catch (final NumberFormatException ex) {
				port = DEFAULT_PORT;
				System.err.println("maf.port must be a number, use "
						+ DEFAULT_PORT);
			}
			sys_props.put("maf.port", String.valueOf(port));

			// Resource sys_res = Resource.getResourceFor("system");
			// int port = sys_res.getInteger("maf.port", DEFAULT_PORT);

			// res = Resource.getResourceFor("aglets");
			// int default_port = res.getInteger("aglets.defaultport",
			// DEFAULT_PORT);
			// int port = res.getInteger("aglets.port", default_port);

			final Resource res = Resource.getResourceFor("atp");

			res.setDefaultResource("atp.protocols", "atp");
			res.setDefaultResource("atp.maxHandlerThread", "32");

			/*
			 * 
			 */
			final String hostname = getFullyQualifiedHostName();

			local.setAddress("atp://" + hostname + ":" + port);

			final Resource system_res = Resource.getResourceFor("system");

			/**
			 * Proxy settings
			 */
			if (res.getBoolean("atp.useHttpProxy", false)) {
				system_res.setResource("http.proxyHost", res.getString("atp.http.proxyHost", ""));
				system_res.setResource("http.proxyPort", res.getString("atp.http.proxyPort", ""));
				system_res.setResource("proxyHost", res.getString("atp.http.proxyHost", ""));
				system_res.setResource("proxyPort", res.getString("atp.http.proxyPort", ""));
			}
			system_res.setResource("http.nonProxyHosts", res.getString("atp.noProxy", ""));
		} catch (final Exception ex) {
			ex.printStackTrace();
			throw new MAFExtendedException(ex.toString());
		}
	}

	@Override
	public void startMAFAgentSystem(final MAFAgentSystem local)
	throws MAFExtendedException {
		try {
			final Daemon daemon = new Daemon(local);
			final AgletRuntime runtime = AgletRuntime.getAgletRuntime();

			if (runtime != null) {
				daemon.setUser(runtime.getOwnerName(), runtime.getOwnerCertificate());
			}
			daemon.start();
			Daemon.update();

			// Register "local"(MAFAgentSystem) to MAFFinder server.
			try {
				final MAFFinder finder = local.get_MAFFinder();

				if (finder != null) {
					try {
						final AgentSystemInfo asi = local.get_agent_system_info();
						final String addr = local.getAddress();

						finder.register_agent_system(asi.agent_system_name, addr, asi);
					} catch (final Exception ex) {
						ex.printStackTrace();
					}
				}
			} catch (final FinderNotFound ex) {
			}
		} catch (final Exception ex) {
			throw new MAFExtendedException(ex.toString());
		}
	}
}
