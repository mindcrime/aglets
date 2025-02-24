package com.ibm.aglets.security;

/*
 * @(#)PolicyPermission.java
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.Permission;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import net.sourceforge.aglets.log.AgletsLogger;

import com.ibm.aglet.security.Protection;

/**
 * The <tt>PolicyPermission</tt> class represents a permission in a grant of
 * Java policy database.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public class PolicyPermission {
	private static AgletsLogger logger = AgletsLogger.getLogger(PolicyPermission.class.getName());
	private static final String CLASSNAME_PERMISSION = "java.security.Permission";
	private static final String CLASSNAME_BASIC_PERMISSION = "java.security.BasicPermission";
	private static final String CLASSNAME_FILE_PERMISSION = "java.io.FilePermission";
	private static final String CLASSNAME_SOCKET_PERMISSION = "java.net.SocketPermission";
	private static final String CLASSNAME_AWT_PERMISSION = "java.awt.AWTPermission";
	private static final String CLASSNAME_NET_PERMISSION = "java.net.NetPermission";
	private static final String CLASSNAME_PROPERTY_PERMISSION = "java.util.PropertyPermission";
	private static final String CLASSNAME_REFLECT_PERMISSION = "java.lang.reflect.ReflectPermission";
	private static final String CLASSNAME_RUNTIME_PERMISSION = "java.lang.RuntimePermission";
	private static final String CLASSNAME_SECURITY_PERMISSION = "java.security.SecurityPermission";
	private static final String CLASSNAME_SERIALIZABLE_PERMISSION = "java.io.SerializablePermission";
	private static final String CLASSNAME_UNRESOLVED_PERMISSION = "java.security.UnresolvedPermission";
	private static final String CLASSNAME_ALL_PERMISSION = "java.security.AllPermission";

	// for aglets
	private static final String CLASSNAME_AGLET_PERMISSION = "com.ibm.aglets.security.AgletPermission";
	private static final String CLASSNAME_MESSAGE_PERMISSION = "com.ibm.aglets.security.MessagePermission";
	private static final String CLASSNAME_CONTEXT_PERMISSION = "com.ibm.aglets.security.ContextPermission";

	private static final String CLASSNAME_AGLET_PROTECTION = "com.ibm.aglet.security.AgletProtection";
	private static final String CLASSNAME_MESSAGE_PROTECTION = "com.ibm.aglet.security.MessageProtection";
	public static boolean equalsSigners(final Vector signersA, final Vector signersB) {
		return includesSigners(signersA, signersB)
		&& includesSigners(signersB, signersA);
	}
	private static final String escapeBackslash(final String str) {
		return escapeChar(str, CHAR_BACKSLASH);
	}
	private static final String escapeChar(final String str, final char c) {
		if (str == null) {
			return null;
		}
		final StringBuffer buf = new StringBuffer(str);
		int len = buf.length();
		int idx = 0;

		while (idx < len) {
			if (buf.charAt(idx) == c) {
				buf.insert(idx, CHAR_ESCAPE);
				idx++;
				len = buf.length();
			}
			idx++;
		}
		return buf.toString();
	}
	private static String getType(final String className) {
		if (className == null) {
			return null;
		}

		String type = null;

		if (className.equals(CLASSNAME_PERMISSION)
				|| className.equals(CLASSNAME_BASIC_PERMISSION)
				|| className.equals(CLASSNAME_FILE_PERMISSION)
				|| className.equals(CLASSNAME_SOCKET_PERMISSION)
				|| className.equals(CLASSNAME_AWT_PERMISSION)
				|| className.equals(CLASSNAME_NET_PERMISSION)
				|| className.equals(CLASSNAME_PROPERTY_PERMISSION)
				|| className.equals(CLASSNAME_REFLECT_PERMISSION)
				|| className.equals(CLASSNAME_RUNTIME_PERMISSION)
				|| className.equals(CLASSNAME_SECURITY_PERMISSION)
				|| className.equals(CLASSNAME_SERIALIZABLE_PERMISSION)
				|| className.equals(CLASSNAME_UNRESOLVED_PERMISSION)
				|| className.equals(CLASSNAME_ALL_PERMISSION)
				|| className.equals(CLASSNAME_AGLET_PERMISSION)
				|| className.equals(CLASSNAME_MESSAGE_PERMISSION)
				|| className.equals(CLASSNAME_CONTEXT_PERMISSION)

				// - || className.equals(CLASSNAME_THREAD_PERMISSION)
		) {
			type = PolicyFileReader.WORD_PERMISSION;
		} else if (className.equals(CLASSNAME_AGLET_PROTECTION)
				|| className.equals(CLASSNAME_MESSAGE_PROTECTION)) {
			type = PolicyFileReader.WORD_PROTECTION;

			// # } else if(className.equals(CLASSNAME_ACTIVITY_PERMISSION)) {
			// # type = PolicyFileReader.WORD_ALLOWANCE;
		}

		return type;
	}
	protected static boolean includesSigners(final Vector names, final Vector signers) {
		if (names == null) {

			// nobody
			return true;
		}
		if (signers == null) {

			// empty
			return false;
		}

		final int num = names.size();
		int i;

		for (i = 0; i < num; i++) {
			final Object obj = names.elementAt(i);

			if (obj instanceof String) {
				final String name = (String) obj;

				if (!isSigner(name, signers)) {
					return false;
				}
			}
		}

		return true;
	}
	protected static boolean isSigner(final String name, final Vector signers) {
		if (name == null) {

			// nobody
			return true;
		}
		if (signers == null) {

			// empty
			return false;
		}

		final int num = signers.size();
		int i;

		for (i = 0; i < num; i++) {
			final Object obj = signers.elementAt(i);

			if (obj instanceof String) {
				final String signer = (String) obj;

				if (name.equals(signer)) {
					return true;
				}
			}
		}

		return false;
	}
	private String _originalClassName = null;
	private String _className = null;
	private Class _class = null;
	private String _targetName = null;
	private String _actions = null;
	private String _signerNames = null;
	private static final String QUOTE = String.valueOf(PolicyFileReader.CHAR_STRING_QUOTE);
	private static final String COMMA = String.valueOf(PolicyFileReader.CHAR_COMMA);
	private static final String TERMINATOR = String.valueOf(PolicyFileReader.CHAR_TERMINATOR);

	private static final String NAME_SEPARATOR = COMMA;
	private static final char CHAR_BACKSLASH = '\\';
	private static final char CHAR_ESCAPE = CHAR_BACKSLASH;
	private static final int NO_TYPE = 0;

	private static final int TYPE_PERMISSION = 1;

	private static final int TYPE_PROTECTION = 2;

	// # private static final int TYPE_ALLOWANCE = 3;
	private int _type = NO_TYPE;

	private Vector _signers = null;

	private Permission _permission = null;

	private PolicyFileReader _reader = null;

	public PolicyPermission(final PolicyFileReader reader, final String type,
	                        final String className) throws ClassNotFoundException {
		_reader = reader;
		setType(type);
		setClassName(className);
	}

	public PolicyPermission(final String className) throws ClassNotFoundException {
		setType(getType(className));
		setClassName(className);
	}

	protected void checkSigner(final String signer) throws SecurityException {

		// check the permission class is signed by the signer.

		if (!this.isSignedBy(signer)) {
			throw new SecurityException("The permission class '"
					+ _class.getName() + "' is not signed by '" + signer
					+ "'.");
		}

		return;
	}

	protected void checkSigners(final Vector signers) throws SecurityException {

		// check the permission class is signed by signers.

		if (signers == null) {

			// regard as anybody
			return;
		}

		final int num = signers.size();
		int i;

		for (i = 0; i < num; i++) {
			final Object obj = signers.elementAt(i);

			if (obj instanceof String) {
				final String signer = (String) obj;

				checkSigner(signer);
			}
		}

		return;
	}

	public Permission create()
	throws PolicyFileParsingException,
	SecurityException {
		if (_class == null) {
			throw getParsingException("No permission class.");
		}

		int numArgs = 0;

		if (_targetName != null) {
			numArgs++;
		}
		if (_actions != null) {
			numArgs++;
		}
		final Class[] classes = new Class[numArgs];
		int i = 0;

		try {
			if (_targetName != null) {
				classes[i] = Class.forName("java.lang.String");
				i++;
			}
			if (_actions != null) {
				classes[i] = Class.forName("java.lang.String");
				i++;
			}
		} catch (final ClassNotFoundException excpt) {
			throw getParsingException(excpt.toString());
		}
		Constructor constructor;

		try {
			constructor = _class.getConstructor(classes);
		} catch (final NoSuchMethodException excpt) {
			throw getParsingException(excpt.toString() + " : "
			                          + _class.getName());
		}
		if (constructor == null) {
			throw getParsingException("No constructor.");
		}
		final String[] args = new String[numArgs];

		i = 0;
		if (_targetName != null) {
			args[i] = _targetName;
			i++;
		}
		if (_actions != null) {
			args[i] = _actions;
			i++;
		}
		Object obj;

		try {
			obj = constructor.newInstance(args);
		} catch (final InstantiationException excpt) {
			throw getParsingException(excpt.toString());
		} catch (final IllegalAccessException excpt) {
			throw getParsingException(excpt.toString());
		} catch (final IllegalArgumentException excpt) {
			throw getParsingException(excpt.toString());
		} catch (final InvocationTargetException excpt) {
			throw getParsingException(excpt.getTargetException().toString());
		}
		if (!(obj instanceof Permission)) {
			_permission = null;
			throw getParsingException("Non-permission class cannot be specified.");
		}
		if (_type == TYPE_PROTECTION) {
			if (!(obj instanceof Protection)) {
				_permission = null;
				throw getParsingException("Protection class is excepted.");
			}
		}
		_permission = (Permission) obj;
		logger.debug("Created permission: " + _permission);
		return _permission;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof PolicyPermission) {
			final PolicyPermission perm = (PolicyPermission) obj;

			return this.equals(perm);
		}
		return false;
	}

	public boolean equals(final PolicyPermission permission) {
		if (permission == null) {
			return false;
		}
		if (!equalsClassName(permission.getClassName())) {
			return false;
		}
		if (!this.equalsSigners(permission._signers)) {
			return false;
		}
		if (!equalsTargetName(permission.getTargetName())) {
			return false;
		}
		if (!equalsActions(permission.getActions())) {
			return false;
		}
		return true;
	}

	public boolean equalsActions(final String actions) {
		if ((_actions == null) || (actions == null)) {
			return false;
		}
		return _actions.equals(actions);
	}

	public boolean equalsClassName(final String className) {
		if (className == null) {
			return false;
		}

		// return _className.equals(convertClassName(className));
		return _className.equals(className);
	}

	public boolean equalsSigners(final Vector signers) {
		return equalsSigners(signers, _signers);
	}

	public boolean equalsTargetName(final String target) {
		if ((_targetName == null) || (target == null)) {
			return false;
		}
		return _targetName.equals(target);
	}

	public String getActions() {
		return _actions;
	}

	public String getClassName() {
		return _className;
	}

	private PolicyFileParsingException getParsingException(final String msg) {
		if (_reader != null) {
			return _reader.getParsingException(msg);
		} else {
			return new PolicyFileParsingException(msg);
		}
	}

	public Permission getPermission() {
		return _permission;
	}

	public String getSignerNames() {
		return _signerNames;
	}

	public Enumeration getSigners() {
		if (_signers != null) {
			return _signers.elements();
		}
		return null;
	}

	public String getTargetName() {
		return _targetName;
	}

	private String getType() {
		String type = null;

		switch (_type) {
			case TYPE_PERMISSION:
				type = PolicyFileReader.WORD_PERMISSION;
				break;
			case TYPE_PROTECTION:
				type = PolicyFileReader.WORD_PROTECTION;
				break;

				// # case TYPE_ALLOWANCE:
					// # type = PolicyFileReader.WORD_ALLOWANCE;
				// # break;
		}
		return type;
	}

	protected boolean isSignedBy(final String signer) {

		// verify the permission class is signed by the signer.

		if (signer.equals("*")) {

			// regard as anybody
			return true;
		}

		// tentative
		return true;
	}

	protected boolean isSignedBy(final Vector signers) {

		// verify the permission class is signed by signers.

		if (signers == null) {

			// regard as anybody
			return true;
		}

		final int num = signers.size();
		int i;

		for (i = 0; i < num; i++) {
			final Object obj = signers.elementAt(i);

			if (obj instanceof String) {
				final String signer = (String) obj;

				if (!this.isSignedBy(signer)) {
					return false;
				}
			}
		}

		return true;
	}

	public void setActions(final String actions) {
		_actions = actions;
	}

	protected void setClassName(final String name) throws ClassNotFoundException {

		// final String className = convertClassName(name);
		final String className = name;

		_class = Class.forName(className);

		_originalClassName = name;
		_className = className;
	}

	public void setSignerNames(final String signerNames) throws SecurityException {
		Vector signers = null;

		if (signerNames != null) {
			signers = new Vector();
			final StringTokenizer st = new StringTokenizer(signerNames, NAME_SEPARATOR);

			while (st.hasMoreTokens()) {
				signers.addElement(st.nextToken().trim());
			}
		}
		checkSigners(signers);
		_signers = signers;
		_signerNames = signerNames;
	}

	public void setTargetName(final String targetName) {
		_targetName = targetName;
	}

	private void setType(final String type) {
		if (type == null) {
			_type = NO_TYPE;
			return;
		}
		if (type.equals(PolicyFileReader.WORD_PERMISSION)) {
			_type = TYPE_PERMISSION;
		} else if (type.equals(PolicyFileReader.WORD_PROTECTION)) {
			_type = TYPE_PROTECTION;

			// # } else if(type.equals(PolicyFileReader.WORD_ALLOWANCE)) {
			// # _type = TYPE_ALLOWANCE;
		} else {
			_type = NO_TYPE;
		}
	}

	@Override
	public String toString() {
		String str = this.getType();

		if (_class != null) {

			// str += " "+_class.getName();
			str += " " + _originalClassName;
		}

		if (_targetName != null) {
			str += " " + QUOTE + escapeBackslash(_targetName) + QUOTE;
		}

		if (_actions != null) {
			str += COMMA + " " + QUOTE + escapeBackslash(_actions) + QUOTE;
		}

		if (_signers != null) {
			final int num = _signers.size();

			if (num > 0) {
				String names = QUOTE;
				int i;

				for (i = 0; i < num; i++) {
					if (i > 0) {
						names += NAME_SEPARATOR;
					}
					names += _signers.elementAt(i).toString();
				}
				names += QUOTE;
				str += COMMA + " " + PolicyFileReader.WORD_SIGNEDBY + " "
				+ names;
			}
		}

		str += TERMINATOR;

		return str;
	}
}
