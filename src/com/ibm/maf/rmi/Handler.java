package com.ibm.maf.rmi;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.Hashtable;

import com.ibm.aglet.Ticket;
import com.ibm.maf.AgentSystemHandler;
import com.ibm.maf.AgentSystemInfo;
import com.ibm.maf.FinderNotFound;
import com.ibm.maf.MAFAgentSystem;
import com.ibm.maf.MAFExtendedException;
import com.ibm.maf.MAFFinder;

public class Handler implements AgentSystemHandler {

	private static final int DEFAULT_PORT = 4434;

	static Hashtable rmi_agentsystems = new Hashtable();

	static boolean initialized = false;

	static private int MAX_RETRY = 3;

	/* package synchronized */
	static MAFAgentSystem_RMI rebind(final MAFAgentSystem_RMI rmi) {
		if (rmi_agentsystems.contains(rmi) == false) {
			return null;
		}

		final Enumeration e = rmi_agentsystems.keys();

		while (e.hasMoreElements()) {
			final String address = (String) e.nextElement();
			final Object obj = rmi_agentsystems.get(address);

			if (obj == rmi) {
				System.out.println("rebind found.. " + address);
				final String name = "//" + address + "/aglets";
				int num_retry = 1;

				while (num_retry++ < MAX_RETRY) {
					try {
						final MAFAgentSystem_RMI new_rmi = (MAFAgentSystem_RMI) Naming.lookup(name);

						rmi_agentsystems.put(address, new_rmi);
						return new_rmi;
					} catch (final Exception ex) {
						try {
							Thread.currentThread();
							Thread.sleep(1);
						} catch (final InterruptedException exx) {
							return null;
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public MAFAgentSystem getMAFAgentSystem(String address)
	throws java.net.UnknownHostException {

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

		return getMAFAgentSystem0(address);
	}

	@Override
	public MAFAgentSystem getMAFAgentSystem(final Ticket ticket)
	throws java.net.UnknownHostException {

		// tentative
		final java.net.URL u = ticket.getDestination();
		String address = u.getHost();

		address += (u.getPort() == -1 ? "" : (":" + u.getPort()));

		return getMAFAgentSystem0(address);
	}

	private MAFAgentSystem getMAFAgentSystem0(final String address)
	throws java.net.UnknownHostException {
		final MAFAgentSystem local = MAFAgentSystem_RMIImpl.getLocalAgentSystem(address);

		if (local != null) {
			return local;
		}
		synchronized (rmi_agentsystems) {
			MAFAgentSystem_RMI rmi = (MAFAgentSystem_RMI) rmi_agentsystems.get(address);

			if (rmi == null) {
				try {
					final String fAddress = address;

					rmi = (MAFAgentSystem_RMI) AccessController.doPrivileged(new PrivilegedExceptionAction() {
						@Override
						public Object run()
						throws UnknownHostException,
						MalformedURLException,
						NotBoundException,
						RemoteException {
							return Naming.lookup("//" + fAddress + "/aglets");
						}
					});
					rmi_agentsystems.put(address, rmi);
				} catch (final PrivilegedActionException ex) {
					final Exception e = ex.getException();

					if (e instanceof UnknownHostException) {
						throw (UnknownHostException) e;
					} else if (e instanceof MalformedURLException) {
						throw new UnknownHostException(e.getMessage());
					} else if (e instanceof NotBoundException) {
						e.printStackTrace();
						return null;
					} else if (e instanceof RemoteException) {
						e.printStackTrace();
						return null;
					} else {
						ex.printStackTrace();
					}
				} catch (final Exception ex) {
					ex.printStackTrace();
				}
			}
			return MAFAgentSystem_RMIClient.find_maf_agentsystem(rmi, address);
		}
	}

	@Override
	synchronized public void initMAFAgentSystem(final MAFAgentSystem local)
	throws MAFExtendedException {
		try {
			if (initialized) {
				return;
			}
			initialized = true;

			final String hostname = InetAddress.getLocalHost().getHostName();

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

			// Resource res = Resource.getResourceFor("aglets");
			// int default_port = res.getInteger("aglets.defaultport",
			// DEFAULT_PORT);
			// int port = res.getInteger("aglets.port", default_port);
			local.setAddress("rmi://" + hostname + ":" + port);
		} catch (final Exception ex) {
			throw new MAFExtendedException(ex.toString());
		}
	}

	@Override
	public void startMAFAgentSystem(final MAFAgentSystem local)
	throws MAFExtendedException {
		try {
			final MAFAgentSystem_RMIImpl impl = new MAFAgentSystem_RMIImpl(local);

			final URL url = new URL(local.getAddress());
			final Registry reg = LocateRegistry.createRegistry(url.getPort());

			reg.bind("aglets", impl);

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
