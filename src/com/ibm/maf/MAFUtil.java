package com.ibm.maf;

import com.ibm.aglet.AgletID;

/*
 * @(#)MAFUtil.java
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

// package
final public class MAFUtil {
	private static Name null_name = new Name(new byte[1], new byte[1], (short) 0);

	static String[] agent_systems = { "NonAgentSystem", "Aglets", "MOA",
	"AgentTcl" };
	static String[] languages = { "LanguageNotSpecified", "Java", "Tcl",
		"Scheme", "Perl" };

	public static final short AGENT_SYSTEM_TYPE_AGLETS = (short) 1;

	public static Name decodeName(final byte b[]) {
		if (b.length < 4) {
			return null_name;
		}
		final short s1 = b[0];
		final short s2 = b[1];
		final short type = (short) ((s1 << 8) + (s2 << 0));
		final short s3 = b[2];
		final short s4 = b[3];
		final short len = (short) ((s3 << 8) + (s4 << 0));
		final byte ident[] = new byte[len];
		final byte auth[] = new byte[b.length - len - 4];

		System.arraycopy(b, 4, ident, 0, len);
		System.arraycopy(b, 4 + len, auth, 0, auth.length);
		return new Name(auth, ident, type);
	}

	public static String decodeString(final byte[] n) {
		final StringBuffer buf = new StringBuffer();

		for (final byte b : n) {
			buf.append(Character.forDigit((b >>> 4) & 0xF, 16));
			buf.append(Character.forDigit(b & 0xF, 16));
		}
		return buf.toString();
	}

	public static byte[] encodeName(final Name name) {
		final int length = name.identity.length + name.authority.length + 2 + 2;
		final byte b[] = new byte[length];

		b[0] = (byte) (name.agent_system_type >>> 8 & 0xFF);
		b[1] = (byte) (name.agent_system_type >>> 0 & 0xFF);
		final short len = (short) name.identity.length;

		b[2] = (byte) (len >>> 8 & 0xFF);
		b[3] = (byte) (len >>> 0 & 0xFF);
		System.arraycopy(name.identity, 0, b, 4, len);
		System.arraycopy(name.authority, 0, b, 4 + len, name.authority.length);
		return b;
	}

	public static byte[] encodeString(final String str) {
		final int len = str.length();
		final byte[] b = new byte[len / 2];

		for (int i = 0, j = 0; j < len; i++, j++) {
			b[i] = (byte) (Character.digit(str.charAt(j++), 16) << 4);
			b[i] += (byte) Character.digit(str.charAt(j), 16);
		}
		return b;
	}

	public static String toAgentSystem(final short type) {
		if ((type >= 0) && (type < agent_systems.length)) {
			return agent_systems[type];
		}
		return null;
	}

	public static short toAgentSystemType(final String n) {
		for (short i = 0; i < agent_systems.length; i++) {
			if (agent_systems[i].equalsIgnoreCase(n)) {
				return i;
			}
		}
		return 0;
	}

	public static AgletID toAgletID(final Name name) {
		return new AgletID(name.identity);
	}

	public static String toLanguage(final short id) {
		if ((id >= 0) && (id < languages.length)) {
			return languages[id];
		}
		return null;
	}

	public static short toLanguageID(final String n) {
		for (short i = 0; i < languages.length; i++) {
			if (languages[i].equalsIgnoreCase(n)) {
				return i;
			}
		}
		return 0;
	}

	public static Name toName(final AgletID aid, final java.security.cert.Certificate cert) {
		byte[] auth;

		try {
			if (cert != null) {
				auth = cert.getEncoded();
			} else {
				auth = new byte[0];
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
			auth = new byte[0];
		}
		return new Name(auth, aid.toByteArray(), MAFUtil.AGENT_SYSTEM_TYPE_AGLETS);
	}
}
