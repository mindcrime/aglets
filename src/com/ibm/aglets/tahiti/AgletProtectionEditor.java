package com.ibm.aglets.tahiti;

/*
 * @(#)SecurityConfigDialog.java
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

// - class ThreadPermissionEditor extends GeneralPermissionEditor {
// -   // currently action should be null
// -   ThreadPermissionEditor() {
// -     super();
// -   }
// - }

// # class ActivityPermissionEditor extends PermissionEditor {
// #   // possible actions are "cloning=xxx", "hops=xxx" and "lifetime=xxxxx"
// #   private static final String LABEL_NAME        = "Name";
// #   private static final int LENGTH_NAME          = 5;
// #   private static final String LABEL_CLONING     = "Cloning";
// #   private static final int LENGTH_CLONING       = 5;
// #   private static final String LABEL_HOPS        = "Hops";
// #   private static final int LENGTH_HOPS          = 5;
// #   private static final String LABEL_LIFETIME    = "Lifetime";
// #   private static final int LENGTH_LIFETIME      = 10;
// #
// #   private static final String CLONING           = "cloning";
// #   private static final String HOPS              = "hops";
// #   private static final String LIFETIME          = "lifetime";
// #   private static final String ASSIGNMENT                = "=";
// #   private static final String ASSIGNMENT_DELIMITER      = ",";
// #   private static final String ASSIGNMENT_DELIMITERS     = ";,";
// #
// #   private static final char CHAR_EQUAL          = '=';
// #   private static final char CHAR_ASSIGNMENT     = CHAR_EQUAL;
// #
// #   private TextField name        = new TextField(LENGTH_NAME);
// #   private TextField cloning     = new TextField(LENGTH_CLONING);
// #   private TextField hops        = new TextField(LENGTH_HOPS);
// #   private TextField lifetime    = new TextField(LENGTH_LIFETIME);
// #
// #   ActivityPermissionEditor() {
// #     GridBagLayout grid = new GridBagLayout();
// #     setLayout(grid);
// #
// #     GridBagConstraints cns = new GridBagConstraints();
// #     cns.weighty = 0.0;
// #     cns.fill = GridBagConstraints.HORIZONTAL;
// #     cns.ipadx = cns.ipady = 5;
// #
// #     Label label = null;
// #
// #     // name
// #     label = new Label(LABEL_NAME);
// #     add(label);
// #     cns.weightx = 0.2;
// #     grid.setConstraints(label, cns);
// #
// #     add(name);
// #     cns.weightx = 1.0;
// #     grid.setConstraints(name, cns);
// #
// #     // cloning
// #     label = new Label(LABEL_CLONING);
// #     add(label);
// #     cns.weightx = 0.2;
// #     grid.setConstraints(label, cns);
// #
// #     add(cloning);
// #     cns.weightx = 0.5;
// #     grid.setConstraints(cloning, cns);
// #
// #     // hops
// #     label = new Label(LABEL_HOPS);
// #     add(label);
// #     cns.weightx = 0.2;
// #     grid.setConstraints(label, cns);
// #
// #     add(hops);
// #     cns.weightx = 0.5;
// #     grid.setConstraints(hops, cns);
// #
// #     // lifetime
// #     label = new Label(LABEL_LIFETIME);
// #     add(label);
// #     cns.weightx = 0.2;
// #     grid.setConstraints(label, cns);
// #
// #     add(lifetime);
// #     cns.weightx = 0.5;
// #     grid.setConstraints(lifetime, cns);
// #   }
// #
// #   public void setText(String text) {
// #     parseText(text);
// #     final String nam  = getArg(0);
// #     final String acts = getArg(1);
// #     if(nam!=null) {
// #       name.setText(nam);
// #     } else {
// #       name.setText("");
// #     }
// #     if(acts!=null) {
// #       StringTokenizer st = new StringTokenizer(acts, ASSIGNMENT_DELIMITERS);
// #       while(st.hasMoreTokens()) {
// # 	final String elem = st.nextToken();
// # 	final int ind = elem.indexOf(ASSIGNMENT);
// # 	if(ind>0) {
// # 	  final String label = elem.substring(0,ind).trim();
// # 	  final String value = elem.substring(ind+1).trim();
// # 	  if(label.equalsIgnoreCase(CLONING)) {
// # 	    cloning.setText(value);
// # 	  } else if(label.equalsIgnoreCase(HOPS)) {
// # 	    hops.setText(value);
// # 	  } else if(label.equalsIgnoreCase(LIFETIME)) {
// # 	    lifetime.setText(value);
// # 	  }
// # 	}
// #       }
// #     } else {
// #       cloning.setText("");
// #       hops.setText("");
// #       lifetime.setText("");
// #     }
// #   }
// #
// #   private final String getActions() {
// #     return getActions(cloning.getText(), hops.getText(), lifetime.getText());
// #   }
// #
// #   private static final String getActions(String cloning, String hops, String lifetime) {
// #     String acts = null;
// #     final boolean cl = cloning!=null  && !cloning.equals("");
// #     final boolean hp = hops!=null     && !hops.equals("");
// #     final boolean lt = lifetime!=null && !lifetime.equals("");
// #     if(cl) {
// #       final String assignment = CLONING+CHAR_ASSIGNMENT+cloning;
// #       acts = assignment;
// #     }
// #     if(hp) {
// #       final String assignment = HOPS+CHAR_ASSIGNMENT+hops;
// #       if(acts!=null) {
// # 	acts += ASSIGNMENT_DELIMITER+assignment;
// #       } else {
// # 	acts = assignment;
// #       }
// #     }
// #     if(lt) {
// #       final String assignment = LIFETIME+CHAR_ASSIGNMENT+lifetime;
// #       if(acts!=null) {
// # 	acts += ASSIGNMENT_DELIMITER+assignment;
// #       } else {
// # 	acts = assignment;
// #       }
// #     }
// #     return acts;
// #   }
// #
// #   public String getText() {
// #     Vector args = new Vector();
// #     final String nam  = name.getText();
// #     final String acts = getActions();
// #     final boolean n = nam!=null  && !nam.equals("");
// #     final boolean a = acts!=null && !acts.equals("");
// #     if(n || a) {
// #       args.addElement(nam);
// #     }
// #     if(a) {
// #       args.addElement(acts);
// #     }
// #     return toText(args);
// #   }
// # }

class AgletProtectionEditor extends GeneralPermissionEditor {

    // possible actions are "dispatch", "dispose", "deactivate", "activate",
    // "clone", and "retract"
    AgletProtectionEditor() {
	super();
    }
}
