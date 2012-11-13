package com.ibm.aglets.tahiti;
/*
 * $Id: TahitiDaemonClient.java,v 1.4 2009/07/28 07:04:53 cat4hire Exp $
 * 
 * @(#)TahitiDaemonClient.java
 *
 * @author     Lary Spector
 * @created    July 22, 2001
 *
 * TahitiDaemonClient is a java application used to communicate with
 * and control a running TahitiDaemon.
 *
 * TahitiDaemon implements a Tahiti service which listens on
 * a configurable control port for commands.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class TahitiDaemonClient {

	private static boolean _verbose = false;
	private static boolean _dont_connect = false;
	private static int _control_port_num = 5545;
	private static String hostname;
	private static boolean _banner_version_OK = false;
	private static int _socket_timeout = 10000; // 10 seconds

	/*
	 * Banner and version Strings
	 */
	private static String _banner_string = "TahitiDaemon";
	private static String _version_string = "1.0";

	private static String helpMsg = "help                    Display this message. \n"
		+ "quit                    Disconnect from the server and quit. \n"
		+ "shutdown                Shutdown the server and quit. \n"
		+ "reboot                  Reboot the server and quit. \n"
		+ "list                    List all aglets in the server. \n"
		+ "msg on|off              Turns message printing on/off, default is off. \n"
		+ "debug on|off            Debug output on/off, default is off. \n"
		+ "create [codeBase] name  Create new aglet. \n"
		+ "<aglet> dispatch URL    Dispatch the aglet to the URL. \n"
		+ "<aglet> clone           Clone the aglet. \n"
		+ "<aglet> dispose         Dispose the aglet. \n"
		+ "<aglet> dialog          Request a dialog to interact with.\n"
		+ "<aglet> property        Display properties of the aglet.\n"
		+ "Note: <aglet> is a left most string listed in the result of list command. ";

	/*
	 * Main method Args: -help -verbose -controlport -host
	 */

	public static void main(final String[] args) throws IOException {

		final String prompt = ">";
		Socket clientSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;

		parseArgs(args);

		// bail out if the user only asked for help.
		if (_dont_connect)
			System.exit(1);

		try {
			clientSocket = new Socket(hostname, _control_port_num);
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			clientSocket.setSoTimeout(_socket_timeout);
		} catch (final UnknownHostException e) {
			System.err.println("\nUnknown host: " + hostname);
			System.exit(1);
		} catch (final IOException e) {
			System.err.println("\nCouldn't get I/O for the connection to: "
					+ hostname);
			System.exit(1);
		}

		final BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String fromServer = "start";
		String fromUser;

		Thread.currentThread().setPriority(1);

		// Try reading from the Daemon
		try {
			fromServer = in.readLine();
		} catch (final IOException ex) {
			System.err.println("\nFailure reading from: " + hostname + ":"
					+ _control_port_num);
			if (_verbose) {
				ex.printStackTrace();
			}
			System.exit(1);
		}

		/*
		 * Handshake with the Daemon, check banner and version
		 */
		try {
			if (fromServer != null) {
				if (_verbose) {
					System.out.println("Server banner: " + fromServer);
				}
				if (fromServer.equals(_banner_string)) {
					if (_verbose) {
						System.out.println("Banner check OK.");
					}
					if ((fromServer = in.readLine()) != null) {
						if (_verbose) {
							System.out.println("Server version: " + fromServer);
						}
						if (fromServer.equals(_version_string)) {
							if (_verbose) {
								System.out.println("Version check OK.");
							}
							_banner_version_OK = true;
						} else {
							System.out.println("Version mismatch.");
							System.out.println("Client version is: "
									+ _version_string);
							System.out.println("Server version is: "
									+ fromServer);
						}
					}
				} else {
					System.out.println("Banner check failed.");
					System.out.println("Client banner is: " + _banner_string);
					System.out.println("Server banner is: " + fromServer);
				}
			}
		} catch (final IOException ex) {
			System.err.println("\nFailure reading from: " + hostname + ":"
					+ _control_port_num);
			if (_verbose) {
				ex.printStackTrace();
			}
			System.exit(1);
		}

		/*
		 * If the banner and version strings are OK, start handling commands.
		 */
		if (_banner_version_OK) {
			try {
				// read the connection info
				if ((fromServer = in.readLine()) != null) {
					// display it if verbose
					if (_verbose) {
						System.out.println("Server: " + fromServer);
					}
				}
			} catch (final IOException ex) {
				System.err.println("\nFailure reading from: " + hostname + ":"
						+ _control_port_num);
				if (_verbose) {
					ex.printStackTrace();
				}
				System.exit(1);
			}
			while (true) {
				try {
					// Display the Prompt
					System.out.print(prompt + " ");
					System.out.flush();

					// Handle client side input
					fromUser = stdIn.readLine();
					if (fromUser != null) {
						if (_verbose)
							System.out.println("Client: " + fromUser);

						// Special cases for help and quit
						if ("help".equalsIgnoreCase(fromUser)) {
							System.out.println(helpMsg);
							System.out.flush();
						}
						if ("quit".equalsIgnoreCase(fromUser)) {
							System.out.println("Closing client connection");
							System.out.flush();
							break;
						}
						// Send command to server
						out.println(fromUser);
					}

					// Read reply from the server
					fromServer = in.readLine();
					// Special cases for shutdown and reboot
					if (_verbose) {
						System.out.println("Server: " + fromServer);
					}
					if (fromServer.equals("shutting down")) {
						System.out.println("Server shutting down, closing client connection.");
						break;
					} else if (fromServer.equals("rebooting")) {
						System.out.println("Server rebooting, closing client connection.");
						break;
					} else { // handle everything else.
						System.out.println(fromServer);
						while (!((fromServer = in.readLine()).equals("done."))) {
							System.out.println(fromServer);
						}
					}
				} catch (final IOException ex) {
					System.err.println("\nFailure reading from: " + hostname
							+ ":" + _control_port_num);
					if (_verbose) {
						ex.printStackTrace();
					}
					break;
				} catch (final Throwable ex) {
					if (_verbose) {
						ex.printStackTrace();
					}
					break;
				}
			}
		}

		out.close();
		in.close();
		stdIn.close();
		clientSocket.close();
		System.exit(1);
	}

	private static void parseArgs(final String[] args) {

		if (_verbose)
			System.out.println("Parsing Args...\n");
		if (args.length <= 0)
			return;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-help")) {
				usage();
				break;
			} else if (args[i].equalsIgnoreCase("-verbose")) {
				_verbose = true;
			} else if (args[i].equalsIgnoreCase("-controlport")) {
				if (i + 1 >= args.length) {
					usage();
					break;
				}
				i++;
				try {
					_control_port_num = Integer.parseInt(args[i]);
				} catch (final NumberFormatException ex) {
					System.err.println("\nError! controlport <" + args[i]
					                                                   + "> is invalid, it  must be an integer ");
					_dont_connect = true;
					break;
				}
			} else if (args[i].equalsIgnoreCase("-timeout")) {
				if (i + 1 >= args.length) {
					usage();
					break;
				}
				i++;
				try {
					_socket_timeout = Integer.parseInt(args[i]);
				} catch (final NumberFormatException ex) {
					System.err.println("\nError! timeout <" + args[i]
					                                               + "> is invalid, it  must be an integer ");
					_dont_connect = true;
					break;
				}
			} else if (args[i].equalsIgnoreCase("-host")) {
				if (i + 1 >= args.length) {
					System.err.println("\nError! hostname was not specified after -host argument");
					usage();
					break;
				}
				i++;
				hostname = args[i];
			} else {
				System.err.println("\nUnknown argument: " + args[i] + "\n");
				usage();
				break;
			}
		}
	}

	private static void usage() {
		System.err.println("\nTahitiDaemonClient [-verbose] [-host <hostname>] [-controlport <port>] [-help]\n");
		System.err.println("Connects to a Tahiti Daemon and provides an interface to control it.\n");
		System.err.println("options:");
		System.err.println("    -verbose         verbose output");
		System.err.println("    -host <hostname>, default is localhost");
		System.err.println("    -controlport <port number>, default is port "
				+ _control_port_num);
		System.err.println("    -timeout <timeout>, in milliseconds, 0 (zero) is infinite, default is "
				+ _socket_timeout);
		System.err.println("    -help            prints this message");

		// signal that the program
		// should not continue the connection process.
		_dont_connect = true;
	}
}
