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

import com.ibm.awb.misc.FileUtils;
import com.ibm.awb.misc.MalformedURIPatternException;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.StreamTokenizer;
import java.io.IOException;
import java.io.FileNotFoundException;
import org.aglets.log.*;

/**
 * The <tt>PolicyFileReader</tt> class accesses Java policy database file
 * and returns PolicyDB object.
 * 
 * @version     1.00    $Date: 2002/02/20 22:17:18 $
 * @author      ONO Kouichi
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
	static final char CHAR_START = (char)0;
	static final char CHAR_SPACE = ' ';
	static final char CHAR_8BITS_START = (char)(128 + 32);
	static final char CHAR_8BITS_END = (char)255;
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

	// #   static final String WORD_ALLOWANCE    = "allowance";

	private String _filename = null;
	private StreamTokenizer _st = null;
	private PolicyDB _db = null;
    
    private static final LogCategory log 
            = LogInitializer.getCategory(PolicyFileReader.class.getName() );
    
	public PolicyFileReader(String policyFilename) {
		try {
			readPolicyFile(policyFilename);
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
	protected synchronized static PolicyDB getAllPolicyDB(String systemPolicyFilename, 
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
		return new PolicyFileParsingException(_filename + "(" + lineno() 
											  + ") " + msg);
	}
	public PolicyDB getPolicyDB() {
		return _db;
	}
	protected synchronized PolicyPermission getPolicyPermission() 
			throws PolicyFileParsingException {
		if (!isPolicyPermission()) {

			// #       throw getParsingException("The reserved word '"+WORD_PERMISSION+"' or '"+WORD_PROTECTION+"' or '"+WORD_ALLOWANCE+"' is expected, not '"+token()+"'.");
			throw getParsingException("The reserved word '" + WORD_PERMISSION 
									  + "' or '" + WORD_PROTECTION 
									  + "' is expected, not '" + token() 
									  + "'.");
		} 
		String type = string();

		nextToken();

		PolicyPermission permission;

		if (isWord()) {
			try {
				permission = new PolicyPermission(this, type, string());
			} catch (ClassNotFoundException excpt) {
				throw getParsingException(excpt.toString());
			} 
		} else {
			throw getParsingException("Not permission class name '" + token() 
									  + "'.");
		} 
		nextToken();

		if (isQuotedString()) {

			// target name
			permission.setTargetName(string());
			nextToken();
		} 

		boolean optional = false;

		if (isComma()) {
			optional = true;
			nextToken();

			// action or signed by
			if (isQuotedString()) {

				// actions
				permission.setActions(string());
				optional = false;
				nextToken();
				if (isComma()) {
					nextToken();
					if (isSignedBy()) {

						// signed by
						optional = true;
					} else {
						throw getParsingException("The reserved word '" 
												  + token() + "'.");
					} 
				} 
			} else if (isSignedBy()) {

				// signed by
				optional = true;
			} else {
				throw getParsingException("Unknown token '" + token() + "'.");
			} 
		} 

		if (isSignedBy()) {
			if (optional == false) {
				throw getParsingException("A character '" + CHAR_COMMA 
										  + "' is needed before '" 
										  + WORD_SIGNEDBY + "' in " 
										  + WORD_PERMISSION + " clause.");
			} 

			// signed by
			nextToken();
			if (isQuotedString()) {

				// signer names
				try {
					permission.setSignerNames(string());
				} catch (SecurityException excpt) {
					throw getParsingException(excpt.toString());
				} 
				nextToken();
			} else {
				throw getParsingException("Signer name(s) should be a quoted string. '" 
										  + token() + "'.");
			} 
		} 

		if (isTerminator()) {

			// create a permission
			permission.create();

			// terminate the parsing
			nextToken();
		} else {
			throw getParsingException("Unknown token '" + token() + "'.");
		} 

		return permission;
	}
	public static String getSystemPolicyFilename() {
		final String javahome = 
			PolicyImpl.getSystemProperty(PROPERTY_JAVAHOME);

		if (javahome == null) {
			return null;
		} 
		return javahome + SEP + "lib" + SEP + "security" + SEP 
			   + FILENAME_JAVAPOLICY;
	}
	public static String getUserPolicyFilename() {

		// if the property "java.policy" is specified, use it as policy filename.
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
		if (_st == null) {
			return;
		} 
		_st.resetSyntax();
		_st.wordChars(CHAR_A_LOWERCASE, CHAR_Z_LOWERCASE);
		_st.wordChars(CHAR_A_UPPERCASE, CHAR_Z_UPPERCASE);
		_st.wordChars(CHAR_8BITS_START, CHAR_8BITS_END);
		_st.whitespaceChars(CHAR_START, CHAR_SPACE);
		_st.quoteChar(CHAR_STRING_QUOTE);
		_st.parseNumbers();
		_st.wordChars(TT_UNDERSCORE, TT_UNDERSCORE);
		_st.slashStarComments(true);
		_st.slashSlashComments(true);
		_st.lowerCaseMode(false);
	}
	private boolean isBeginBlock() {
		return isBeginBlock(tokenType());
	}
	private static boolean isBeginBlock(int ttype) {
		return ttype == TT_BEGIN_BLOCK;
	}
	private boolean isCodeBase() {
		return isWord() && string().equals(WORD_CODEBASE);
	}
	private boolean isComma() {
		return isComma(tokenType());
	}
	private static boolean isComma(int ttype) {
		return ttype == TT_COMMA;
	}
	private boolean isEndBlock() {
		return isEndBlock(tokenType());
	}
	private static boolean isEndBlock(int ttype) {
		return ttype == TT_END_BLOCK;
	}
	// #   private boolean isAllowance() {
	// #     return isWord() && isAllowance(string());
	// #   }
	// #
	// #   private boolean isAllowance(String word) {
	// #     return WORD_ALLOWANCE.equals(word);
	// #   }

	private boolean isEndOfFile() {
		return isEndOfFile(tokenType());
	}
	private static boolean isEndOfFile(int ttype) {
		return ttype == TT_EOF;
	}
	private boolean isEndOfLine() {
		return isEndOfLine(tokenType());
	}
	private static boolean isEndOfLine(int ttype) {
		return ttype == TT_EOL;
	}
	private boolean isGrant() {
		return isWord() && string().equals(WORD_GRANT);
	}
	private boolean isNumber() {
		return isNumber(tokenType());
	}
	private static boolean isNumber(int ttype) {
		return ttype == TT_NUMBER;
	}
	private boolean isOwnedBy() {
		return isWord() && string().equals(WORD_OWNEDBY);
	}
	private boolean isPermission() {
		return isWord() && isPermission(string());
	}
	private boolean isPermission(String word) {
		return WORD_PERMISSION.equals(word);
	}
	private boolean isPolicyPermission() {

		// #     return isPermission() || isProtection() || isAllowance();
		return isPermission() || isProtection();
	}
	private boolean isProtection() {
		return isWord() && isProtection(string());
	}
	private boolean isProtection(String word) {
		return WORD_PROTECTION.equals(word);
	}
	private boolean isQuotedString() {
		return isQuotedString(tokenType());
	}
	private static boolean isQuotedString(int ttype) {
		return ttype == TT_QUOTED_STRING;
	}
	private boolean isSignedBy() {
		return isWord() && string().equals(WORD_SIGNEDBY);
	}
	private boolean isTerminator() {
		return isTerminator(tokenType());
	}
	private static boolean isTerminator(int ttype) {
		return ttype == TT_TERMINATOR;
	}
	private boolean isWord() {
		return isWord(tokenType());
	}
	private static boolean isWord(int ttype) {
		return ttype == TT_WORD;
	}
	private int lineno() {
		return _st.lineno();
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
			ttype = _st.nextToken();
		} catch (IOException excpt) {
			throw getParsingException(excpt.toString());
		} 
		return ttype;
	}
	private double number() {
		return _st.nval;
	}
	protected synchronized void readPolicyDB() 
			throws PolicyFileParsingException {
		nextToken();

		// at least one grant clause is needed
		if (!isGrant()) {
			throw getParsingException("The reserved word '" + WORD_GRANT 
									  + "' is expected, not '" + token() 
									  + "'.");
		} 

		if (_db == null) {
			_db = new PolicyDB();
		} 

		while (isGrant()) {
			readPolicyGrant();
		} 

		if (!isEndOfFile()) {
			throw getParsingException("The reserved word '" + WORD_GRANT 
									  + "' is expected, not '" + token() 
									  + "'.");
		} 
	}
	protected synchronized void readPolicyFile(String filename) 
			throws FileNotFoundException {
		log.info("Reading security policy file: " + filename);
		if (filename == null) {
			throw new FileNotFoundException("Policy filename is null.");
		} 
		_filename = filename;
		FileReader reader = new FileReader(filename);

		_st = new StreamTokenizer(new BufferedReader(reader));
		initialize();
		try {
			readPolicyDB();
			reader.close();
			log.debug("Policy file read complete");
		} catch (PolicyFileParsingException excpt) {
			log.error("Error parsing policy file: ",excpt);
		} catch (IOException excpt) {
			log.error("Error parsing policy file: ",excpt);
		} 
	}
	protected synchronized void readPolicyGrant() 
			throws PolicyFileParsingException {
		if (!isGrant()) {
			throw getParsingException("The reserved word '" + WORD_GRANT 
									  + "' is expected, not '" + token() 
									  + "'.");
		} 
		nextToken();

		PolicyGrant grant = new PolicyGrant();

		boolean signedBy = false;
		boolean codeBase = false;
		boolean ownedBy = false;

		while (!isBeginBlock()) {
			if (isSignedBy()) {

				// signed by
				if (signedBy) {

					// duplicated
					throw getParsingException(WORD_SIGNEDBY 
											  + " phrase duplicated.");
				} 
				signedBy = true;
				nextToken();

				// should be signer names
				if (isQuotedString()) {

					// signer names
					grant.setSignerNames(string());
					nextToken();
					if (!isBeginBlock()) {

						// optional
						if (isComma()) {
							nextToken();
						} else {
							throw getParsingException("A character '" 
													  + CHAR_COMMA 
													  + "' is expected before '" 
													  + token() + "'.");
						} 
					} 
				} else {
					throw getParsingException("Signer name(s) should be a quoted string. '" 
											  + token() + "'.");
				} 
			} else if (isCodeBase()) {

				// code base
				if (codeBase) {

					// duplicated
					throw getParsingException(WORD_CODEBASE 
											  + " phrase duplicated.");
				} 
				codeBase = true;
				nextToken();

				// should be URI pattern as code base
				if (isQuotedString()) {

					// URI pattern as code base
					try {
						grant.setCodeBase(string());
					} catch (MalformedURIPatternException excpt) {
						throw getParsingException(excpt.toString());
					} 
					nextToken();
					if (!isBeginBlock()) {

						// optional
						if (isComma()) {
							nextToken();
						} else {
							throw getParsingException("A character '" 
													  + CHAR_COMMA 
													  + "' is expected before '" 
													  + token() + "'.");
						} 
					} 
				} else {
					throw getParsingException("An URI pattern as code base should be a quoted string. '" 
											  + token() + "'.");
				} 
			} else if (isOwnedBy()) {

				// owned by
				if (ownedBy) {

					// duplicated
					throw getParsingException(WORD_OWNEDBY 
											  + " phrase duplicated.");
				} 
				ownedBy = true;
				nextToken();

				// should be owner names
				if (isQuotedString()) {

					// owner names
					grant.setOwnerNames(string());
					nextToken();
					if (!isBeginBlock()) {

						// optional
						if (isComma()) {
							nextToken();
						} else {
							throw getParsingException("Begin block character '" 
													  + CHAR_BEGIN_BLOCK 
													  + "' is expected, not '" 
													  + token() + "'.");
						} 
					} 
				} else {
					throw getParsingException("An owner name should be a quoted string. '" 
											  + token() + "'.");
				} 
			} else {
				throw getParsingException("Begin block character '" 
										  + CHAR_BEGIN_BLOCK 
										  + "' is expected, not '" + token() 
										  + "'.");
			} 
		} 

		// should be begin block
		if (isBeginBlock()) {
			nextToken();

			// at least one permission clause is needed in the block.
			if (!isPolicyPermission()) {

				// # 	throw getParsingException("The reserved word '"+WORD_PERMISSION+"' or '"+WORD_PROTECTION+"' or '"+WORD_ALLOWANCE+"' is expected, not '"+token()+"'.");
				throw getParsingException("The reserved word '" 
										  + WORD_PERMISSION + "' or '" 
										  + WORD_PROTECTION 
										  + "' is expected, not '" + token() 
										  + "'.");
			} 
			while (isPolicyPermission()) {
				grant.addPermission(getPolicyPermission());
			} 

			// should be end block
			if (isEndBlock()) {
				nextToken();

				// should be terminator
				if (isTerminator()) {
					nextToken();
				} else {
					throw getParsingException("The termination character '" 
											  + CHAR_TERMINATOR 
											  + "' is expected, not '" 
											  + token() + "'.");
				} 
			} else {
				throw getParsingException("End block character '" 
										  + CHAR_END_BLOCK 
										  + "' is expected, not '" + token() 
										  + "'.");
			} 
		} else {
			throw getParsingException("Begin block character '" 
									  + CHAR_BEGIN_BLOCK 
									  + "' is expected, not '" + token() 
									  + "'.");
		} 

		_db.addGrant(grant);
	}
	private String string() {
		return _st.sval;
	}
	private String token() {
		String t;
		final int type = tokenType();

		switch (type) {
		case TT_WORD:
		case TT_QUOTED_STRING:
			t = string();
			break;
		case TT_NUMBER:
			t = Double.toString(number());
			break;
		default:
			t = String.valueOf((char)type);
		}
		return t;
	}
	private int tokenType() {
		return _st.ttype;
	}
}
