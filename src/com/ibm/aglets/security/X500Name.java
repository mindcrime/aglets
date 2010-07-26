package com.ibm.aglets.security;

/**
 * @author: Hideki Tai
 */
public class X500Name {
    private java.lang.String CN = null;
    private java.lang.String OU = null;
    private java.lang.String O = null;
    private java.lang.String L = null;
    private java.lang.String ST = null;
    private java.lang.String C = null;

    /**
     * @param cert
     *            java.security.cert.Certificate
     */
    public X500Name(java.security.cert.X509Certificate cert) {
	String name = cert.getSubjectDN().getName();
	java.util.StringTokenizer st = new java.util.StringTokenizer(name, ",");

	while (st.hasMoreTokens()) {
	    String token = st.nextToken().trim();
	    int idx = token.indexOf("=");

	    if (idx >= 0) {
		String label = token.substring(0, idx);
		String value = token.substring(idx + 1);

		if ("CN".equals(label)) {
		    this.CN = value;
		} else if ("OU".equals(label)) {
		    this.OU = value;
		} else if ("O".equals(label)) {
		    this.O = value;
		} else if ("C".equals(label)) {
		    this.C = value;
		} else if ("L".equals(label)) {
		    this.L = value;
		} else if ("ST".equals(label)) {
		    this.ST = value;
		}
	    }
	}
    }

    /**
     * Insert the method's description here. Creation date: (00/01/13 ???O
     * 02:25:55)
     * 
     * @return java.lang.String
     */
    public java.lang.String getC() {
	return this.C;
    }

    /**
     * Insert the method's description here. Creation date: (00/01/13 ???O
     * 02:23:11)
     * 
     * @return java.lang.String
     */
    public java.lang.String getCN() {
	return this.CN;
    }

    /**
     * Insert the method's description here. Creation date: (00/01/13 ???O
     * 02:25:27)
     * 
     * @return java.lang.String
     */
    public java.lang.String getL() {
	return this.L;
    }

    /**
     * Insert the method's description here. Creation date: (00/01/13 ???O
     * 02:24:47)
     * 
     * @return java.lang.String
     */
    public java.lang.String getO() {
	return this.O;
    }

    /**
     * Insert the method's description here. Creation date: (00/01/13 ???O
     * 02:23:59)
     * 
     * @return java.lang.String
     */
    public java.lang.String getOU() {
	return this.OU;
    }

    /**
     * Insert the method's description here. Creation date: (00/01/13 ???O
     * 02:25:41)
     * 
     * @return java.lang.String
     */
    public java.lang.String getST() {
	return this.ST;
    }
}
