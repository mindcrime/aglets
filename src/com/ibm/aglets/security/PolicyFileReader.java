package com.ibm.aglets.security;

/*
 * @(#)PolicyFileReader.java
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;

import net.sourceforge.aglets.log.AgletsLogger;

import com.ibm.awb.misc.FileUtils;
import com.ibm.awb.misc.MalformedURIPatternException;

/**
 * The <tt>PolicyFileReader</tt> class accesses Java policy database file and
 * returns PolicyDB object.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public class PolicyFileReader {
    private static final String PROPERTY_JAVAHOME = "java.home";
    private static final String PROPERTY_POLICYFILE = "java.policy";
    private static final String FILENAME_JAVAPOLICY = "java.policy";

    private final static String SEP = File.separator;
    static final String CRLF = "\r\n";

    static final int TT_EOF = StreamTokenizer.TT_EOF;
    static final int TT_EOL = StreamTokenizer.TT_EOL;
    static final int TT_WORD = StreamTokenizer.TT_WORD;
    static final int TT_NUMBER = StreamTokenizer.TT_NUMBER;

    static final char CHAR_A_LOWERCASE = 'a';
    static final char CHAR_Z_LOWERCASE = 'z';
    static final char CHAR_A_UPPERCASE = 'A';
    static final char CHAR_Z_UPPERCASE = 'Z';
    static final char CHAR_START = (char) 0;
    static final char CHAR_SPACE = ' ';
    static final char CHAR_8BITS_START = (char) (128 + 32);
    static final char CHAR_8BITS_END = (char) 255;
    static final char CHAR_BACKSLASH = '\\';
    static final char CHAR_SINGLE_QUOTE = '\'';
    static final char CHAR_DOUBLE_QUOTE = '"';
    static final char CHAR_CROSSHATCH = '#';
    static final int TT_CROSSHATCH = CHAR_CROSSHATCH;
    static final char CHAR_COMMENT = CHAR_CROSSHATCH;
    static final int TT_COMMENT = CHAR_COMMENT;
    static final char CHAR_COMMA = ',';
    static final int TT_COMMA = CHAR_COMMA;
    static final char CHAR_STRING_QUOTE = CHAR_DOUBLE_QUOTE;
    static final int TT_QUOTED_STRING = CHAR_STRING_QUOTE;
    static final char CHAR_SEMICOLON = ';';
    static final int TT_SEMICOLON = CHAR_SEMICOLON;
    static final char CHAR_TERMINATOR = CHAR_SEMICOLON;
    static final int TT_TERMINATOR = CHAR_TERMINATOR;
    static final char CHAR_OPEN_BRACKET = '{';
    static final int TT_OPEN_BRACKET = CHAR_OPEN_BRACKET;
    static final char CHAR_BEGIN_BLOCK = CHAR_OPEN_BRACKET;
    static final int TT_BEGIN_BLOCK = CHAR_BEGIN_BLOCK;
    static final char CHAR_CLOSE_BRACKET = '}';
    static final int TT_CLOSE_BRACKET = CHAR_CLOSE_BRACKET;
    static final char CHAR_END_BLOCK = CHAR_CLOSE_BRACKET;
    static final int TT_END_BLOCK = CHAR_END_BLOCK;

    static final char CHAR_UNDERSCORE = '_';
    static final int TT_UNDERSCORE = CHAR_UNDERSCORE;

    static final String WORD_GRANT = "grant";
    static final String WORD_SIGNEDBY = "signedBy";
    static final String WORD_CODEBASE = "codeBase";
    static final String WORD_OWNEDBY = "ownedBy";
    static final String WORD_PERMISSION = "permission";
    static final String WORD_PROTECTION = "protection";

    // # static final String WORD_ALLOWANCE = "allowance";

    private String _filename = null;
    private StreamTokenizer _st = null;
    private PolicyDB _db = null;

    private static AgletsLogger logger = AgletsLogger.getLogger(PolicyFileReader.class.getName());

    public PolicyFileReader(String policyFilename) {
	try {
	    this.readPolicyFile(policyFilename);
	} catch (FileNotFoundException excpt) {

	    // do nothing
	}
    }

    public synchronized static PolicyDB getAllPolicyDB() {
	return getAllPolicyDB(getUserPolicyFilename());
    }

    public synchronized static PolicyDB getAllPolicyDB(String userPolicyFilename) {
	return getAllPolicyDB(getSystemPolicyFilename(), userPolicyFilename);
    }

    protected synchronized static PolicyDB getAllPolicyDB(
                                                          String systemPolicyFilename,
                                                          String userPolicyFilename) {
	PolicyFileReader reader = new PolicyFileReader(systemPolicyFilename);

	try {
	    reader.readPolicyFile(userPolicyFilename);
	} catch (FileNotFoundException excpt) {

	    // do nothing
	}
	return reader.getPolicyDB();
    }

    PolicyFileParsingException getParsingException(String msg) {
	return new PolicyFileParsingException(this._filename + "("
		+ this.lineno() + ") " + msg);
    }

    public PolicyDB getPolicyDB() {
	return this._db;
    }

    protected synchronized PolicyPermission getPolicyPermission()
    throws PolicyFileParsingException {
	if (!this.isPolicyPermission()) {

	    // # throw
	    // getParsingException("The reserved word '"+WORD_PERMISSION+"' or '"+WORD_PROTECTION+"' or '"+WORD_ALLOWANCE+"' is expected, not '"+token()+"'.");
	    throw this.getParsingException("The reserved word '"
		    + WORD_PERMISSION + "' or '" + WORD_PROTECTION
		    + "' is expected, not '" + this.token() + "'.");
	}
	String type = this.string();

	this.nextToken();

	PolicyPermission permission;

	if (this.isWord()) {
	    try {
		permission = new PolicyPermission(this, type, this.string());
	    } catch (ClassNotFoundException excpt) {
		throw this.getParsingException(excpt.toString());
	    }
	} else {
	    throw this.getParsingException("Not permission class name '"
		    + this.token() + "'.");
	}
	this.nextToken();

	if (this.isQuotedString()) {

	    // target name
	    permission.setTargetName(this.string());
	    this.nextToken();
	}

	boolean optional = false;

	if (this.isComma()) {
	    optional = true;
	    this.nextToken();

	    // action or signed by
	    if (this.isQuotedString()) {

		// actions
		permission.setActions(this.string());
		optional = false;
		this.nextToken();
		if (this.isComma()) {
		    this.nextToken();
		    if (this.isSignedBy()) {

			// signed by
			optional = true;
		    } else {
			throw this.getParsingException("The reserved word '"
				+ this.token() + "'.");
		    }
		}
	    } else if (this.isSignedBy()) {

		// signed by
		optional = true;
	    } else {
		throw this.getParsingException("Unknown token '" + this.token()
			+ "'.");
	    }
	}

	if (this.isSignedBy()) {
	    if (optional == false) {
		throw this.getParsingException("A character '" + CHAR_COMMA
			+ "' is needed before '" + WORD_SIGNEDBY + "' in "
			+ WORD_PERMISSION + " clause.");
	    }

	    // signed by
	    this.nextToken();
	    if (this.isQuotedString()) {

		// signer names
		try {
		    permission.setSignerNames(this.string());
		} catch (SecurityException excpt) {
		    throw this.getParsingException(excpt.toString());
		}
		this.nextToken();
	    } else {
		throw this.getParsingException("Signer name(s) should be a quoted string. '"
			+ this.token() + "'.");
	    }
	}

	if (this.isTerminator()) {

	    // create a permission
	    permission.create();

	    // terminate the parsing
	    this.nextToken();
	} else {
	    throw this.getParsingException("Unknown token '" + this.token()
		    + "'.");
	}

	return permission;
    }

    public static String getSystemPolicyFilename() {
	final String javahome = PolicyImpl.getSystemProperty(PROPERTY_JAVAHOME);

	if (javahome == null) {
	    return null;
	}
	return javahome + SEP + "lib" + SEP + "security" + SEP
	+ FILENAME_JAVAPOLICY;
    }

    public static String getUserPolicyFilename() {

	// if the property "java.policy" is specified, use it as policy
	// filename.
	String filename = PolicyImpl.getSystemProperty(PROPERTY_POLICYFILE);

	if (filename != null) {

	    // System.out.println("the property \"java.policy\" specified : "+filename);
	    return filename;
	}

	// use {user.home}/.java.policy as policy filename.
	final String userhome = FileUtils.getUserHome();

	if (userhome == null) {
	    return null;
	}
	filename = userhome + SEP + "." + FILENAME_JAVAPOLICY;

	// System.out.println("{user.home}/.java.policy used : "+filename);
	return filename;
    }

    protected void initialize() {
	if (this._st == null) {
	    return;
	}
	this._st.resetSyntax();
	this._st.wordChars(CHAR_A_LOWERCASE, CHAR_Z_LOWERCASE);
	this._st.wordChars(CHAR_A_UPPERCASE, CHAR_Z_UPPERCASE);
	this._st.wordChars(CHAR_8BITS_START, CHAR_8BITS_END);
	this._st.whitespaceChars(CHAR_START, CHAR_SPACE);
	this._st.quoteChar(CHAR_STRING_QUOTE);
	this._st.parseNumbers();
	this._st.wordChars(TT_UNDERSCORE, TT_UNDERSCORE);
	this._st.slashStarComments(true);
	this._st.slashSlashComments(true);
	this._st.lowerCaseMode(false);
    }

    private boolean isBeginBlock() {
	return isBeginBlock(this.tokenType());
    }

    private static boolean isBeginBlock(int ttype) {
	return ttype == TT_BEGIN_BLOCK;
    }

    private boolean isCodeBase() {
	return this.isWord() && this.string().equals(WORD_CODEBASE);
    }

    private boolean isComma() {
	return isComma(this.tokenType());
    }

    private static boolean isComma(int ttype) {
	return ttype == TT_COMMA;
    }

    private boolean isEndBlock() {
	return isEndBlock(this.tokenType());
    }

    private static boolean isEndBlock(int ttype) {
	return ttype == TT_END_BLOCK;
    }

    // # private boolean isAllowance() {
    // # return isWord() && isAllowance(string());
    // # }
    // #
    // # private boolean isAllowance(String word) {
    // # return WORD_ALLOWANCE.equals(word);
    // # }

    private boolean isEndOfFile() {
	return isEndOfFile(this.tokenType());
    }

    private static boolean isEndOfFile(int ttype) {
	return ttype == TT_EOF;
    }

    private boolean isGrant() {
	return this.isWord() && this.string().equals(WORD_GRANT);
    }

    private boolean isOwnedBy() {
	return this.isWord() && this.string().equals(WORD_OWNEDBY);
    }

    private boolean isPermission() {
	return this.isWord() && this.isPermission(this.string());
    }

    private boolean isPermission(String word) {
	return WORD_PERMISSION.equals(word);
    }

    private boolean isPolicyPermission() {

	// # return isPermission() || isProtection() || isAllowance();
	return this.isPermission() || this.isProtection();
    }

    private boolean isProtection() {
	return this.isWord() && this.isProtection(this.string());
    }

    private boolean isProtection(String word) {
	return WORD_PROTECTION.equals(word);
    }

    private boolean isQuotedString() {
	return isQuotedString(this.tokenType());
    }

    private static boolean isQuotedString(int ttype) {
	return ttype == TT_QUOTED_STRING;
    }

    private boolean isSignedBy() {
	return this.isWord() && this.string().equals(WORD_SIGNEDBY);
    }

    private boolean isTerminator() {
	return isTerminator(this.tokenType());
    }

    private static boolean isTerminator(int ttype) {
	return ttype == TT_TERMINATOR;
    }

    private boolean isWord() {
	return isWord(this.tokenType());
    }

    private static boolean isWord(int ttype) {
	return ttype == TT_WORD;
    }

    private int lineno() {
	return this._st.lineno();
    }

    // for test
    static public void main(String arg[]) {
	PolicyDB db = null;

	if (arg.length == 0) {
	    db = getAllPolicyDB();
	} else {
	    PolicyFileReader reader = new PolicyFileReader(arg[0]);

	    db = reader.getPolicyDB();
	}
	if (db != null) {
	    System.out.print(db.toString());
	} else {
	    System.out.println("Policy file does not exist.");
	}
    }

    private int nextToken() throws PolicyFileParsingException {
	int ttype;

	try {
	    ttype = this._st.nextToken();
	} catch (IOException excpt) {
	    throw this.getParsingException(excpt.toString());
	}
	return ttype;
    }

    private double number() {
	return this._st.nval;
    }

    protected synchronized void readPolicyDB()
    throws PolicyFileParsingException {
	this.nextToken();

	// at least one grant clause is needed
	if (!this.isGrant()) {
	    throw this.getParsingException("The reserved word '" + WORD_GRANT
		    + "' is expected, not '" + this.token() + "'.");
	}

	if (this._db == null) {
	    this._db = new PolicyDB();
	}

	while (this.isGrant()) {
	    this.readPolicyGrant();
	}

	if (!this.isEndOfFile()) {
	    throw this.getParsingException("The reserved word '" + WORD_GRANT
		    + "' is expected, not '" + this.token() + "'.");
	}
    }

    protected synchronized void readPolicyFile(String filename)
    throws FileNotFoundException {
	logger.info("Reading security policy file: " + filename);
	if (filename == null) {
	    throw new FileNotFoundException("Policy filename is null.");
	}
	this._filename = filename;
	FileReader reader = new FileReader(filename);

	this._st = new StreamTokenizer(new BufferedReader(reader));
	this.initialize();
	try {
	    this.readPolicyDB();
	    reader.close();
	    logger.debug("Policy file read complete");
	} catch (PolicyFileParsingException excpt) {
	    logger.error("Error parsing policy file: ", excpt);
	} catch (IOException excpt) {
	    logger.error("Error parsing policy file: ", excpt);
	}
    }

    protected synchronized void readPolicyGrant()
    throws PolicyFileParsingException {
	if (!this.isGrant()) {
	    throw this.getParsingException("The reserved word '" + WORD_GRANT
		    + "' is expected, not '" + this.token() + "'.");
	}
	this.nextToken();

	PolicyGrant grant = new PolicyGrant();

	boolean signedBy = false;
	boolean codeBase = false;
	boolean ownedBy = false;

	while (!this.isBeginBlock()) {
	    if (this.isSignedBy()) {

		// signed by
		if (signedBy) {

		    // duplicated
		    throw this.getParsingException(WORD_SIGNEDBY
			    + " phrase duplicated.");
		}
		signedBy = true;
		this.nextToken();

		// should be signer names
		if (this.isQuotedString()) {

		    // signer names
		    grant.setSignerNames(this.string());
		    this.nextToken();
		    if (!this.isBeginBlock()) {

			// optional
			if (this.isComma()) {
			    this.nextToken();
			} else {
			    throw this.getParsingException("A character '"
				    + CHAR_COMMA + "' is expected before '"
				    + this.token() + "'.");
			}
		    }
		} else {
		    throw this.getParsingException("Signer name(s) should be a quoted string. '"
			    + this.token() + "'.");
		}
	    } else if (this.isCodeBase()) {

		// code base
		if (codeBase) {

		    // duplicated
		    throw this.getParsingException(WORD_CODEBASE
			    + " phrase duplicated.");
		}
		codeBase = true;
		this.nextToken();

		// should be URI pattern as code base
		if (this.isQuotedString()) {

		    // URI pattern as code base
		    try {
			grant.setCodeBase(this.string());
		    } catch (MalformedURIPatternException excpt) {
			throw this.getParsingException(excpt.toString());
		    }
		    this.nextToken();
		    if (!this.isBeginBlock()) {

			// optional
			if (this.isComma()) {
			    this.nextToken();
			} else {
			    throw this.getParsingException("A character '"
				    + CHAR_COMMA + "' is expected before '"
				    + this.token() + "'.");
			}
		    }
		} else {
		    throw this.getParsingException("An URI pattern as code base should be a quoted string. '"
			    + this.token() + "'.");
		}
	    } else if (this.isOwnedBy()) {

		// owned by
		if (ownedBy) {

		    // duplicated
		    throw this.getParsingException(WORD_OWNEDBY
			    + " phrase duplicated.");
		}
		ownedBy = true;
		this.nextToken();

		// should be owner names
		if (this.isQuotedString()) {

		    // owner names
		    grant.setOwnerNames(this.string());
		    this.nextToken();
		    if (!this.isBeginBlock()) {

			// optional
			if (this.isComma()) {
			    this.nextToken();
			} else {
			    throw this.getParsingException("Begin block character '"
				    + CHAR_BEGIN_BLOCK
				    + "' is expected, not '"
				    + this.token() + "'.");
			}
		    }
		} else {
		    throw this.getParsingException("An owner name should be a quoted string. '"
			    + this.token() + "'.");
		}
	    } else {
		throw this.getParsingException("Begin block character '"
			+ CHAR_BEGIN_BLOCK + "' is expected, not '"
			+ this.token() + "'.");
	    }
	}

	// should be begin block
	if (this.isBeginBlock()) {
	    this.nextToken();

	    // at least one permission clause is needed in the block.
	    if (!this.isPolicyPermission()) {

		// # throw
		// getParsingException("The reserved word '"+WORD_PERMISSION+"' or '"+WORD_PROTECTION+"' or '"+WORD_ALLOWANCE+"' is expected, not '"+token()+"'.");
		throw this.getParsingException("The reserved word '"
			+ WORD_PERMISSION + "' or '" + WORD_PROTECTION
			+ "' is expected, not '" + this.token() + "'.");
	    }
	    while (this.isPolicyPermission()) {
		grant.addPermission(this.getPolicyPermission());
	    }

	    // should be end block
	    if (this.isEndBlock()) {
		this.nextToken();

		// should be terminator
		if (this.isTerminator()) {
		    this.nextToken();
		} else {
		    throw this.getParsingException("The termination character '"
			    + CHAR_TERMINATOR
			    + "' is expected, not '"
			    + this.token() + "'.");
		}
	    } else {
		throw this.getParsingException("End block character '"
			+ CHAR_END_BLOCK + "' is expected, not '"
			+ this.token() + "'.");
	    }
	} else {
	    throw this.getParsingException("Begin block character '"
		    + CHAR_BEGIN_BLOCK + "' is expected, not '" + this.token()
		    + "'.");
	}

	this._db.addGrant(grant);
    }

    private String string() {
	return this._st.sval;
    }

    private String token() {
	String t;
	final int type = this.tokenType();

	switch (type) {
	case TT_WORD:
	case TT_QUOTED_STRING:
	    t = this.string();
	    break;
	case TT_NUMBER:
	    t = Double.toString(this.number());
	    break;
	default:
	    t = String.valueOf((char) type);
	}
	return t;
    }

    private int tokenType() {
	return this._st.ttype;
    }
}
