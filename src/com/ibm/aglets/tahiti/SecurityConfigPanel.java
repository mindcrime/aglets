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

import java.awt.CardLayout;
import java.awt.Choice;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;
import java.util.Vector;

import com.ibm.aglets.security.PolicyGrant;
import com.ibm.aglets.security.PolicyPermission;

class SecurityConfigPanel extends GridBagPanel implements ItemListener {
    private static final String LABEL_FILE_PERMISSION = "FileSystem";
    private static final String LABEL_SOCKET_PERMISSION = "Socket";
    private static final String LABEL_AWT_PERMISSION = "Window";
    private static final String LABEL_PROPERTY_PERMISSION = "Property";
    private static final String LABEL_RUNTIME_PERMISSION = "Runtime";
    private static final String LABEL_SECURITY_PERMISSION = "Security";
    private static final String LABEL_ALL_PERMISSION = "All";

    // for aglets
    private static final String LABEL_AGLET_PERMISSION = "Aglet";
    private static final String LABEL_MESSAGE_PERMISSION = "Message";
    private static final String LABEL_CONTEXT_PERMISSION = "Context";

    // - private static final String LABEL_THREAD_PERMISSION = "Thread";
    // # private static final String LABEL_ACTIVITY_PERMISSION = "Activity";
    private static final String LABEL_AGLET_PROTECTION = "Protection (Aglet)";
    private static final String LABEL_MESSAGE_PROTECTION = "Protection (Message)";

    private static final String CLASSNAME_FILE_PERMISSION = "java.io.FilePermission";
    private static final String CLASSNAME_SOCKET_PERMISSION = "java.net.SocketPermission";
    private static final String CLASSNAME_AWT_PERMISSION = "java.awt.AWTPermission";
    private static final String CLASSNAME_PROPERTY_PERMISSION = "java.util.PropertyPermission";
    private static final String CLASSNAME_RUNTIME_PERMISSION = "java.lang.RuntimePermission";
    private static final String CLASSNAME_SECURITY_PERMISSION = "java.security.SecurityPermission";
    private static final String CLASSNAME_ALL_PERMISSION = "java.security.AllPermission";

    // for aglets
    private static final String CLASSNAME_AGLET_PERMISSION = "com.ibm.aglets.security.AgletPermission";
    private static final String CLASSNAME_MESSAGE_PERMISSION = "com.ibm.aglets.security.MessagePermission";
    private static final String CLASSNAME_CONTEXT_PERMISSION = "com.ibm.aglets.security.ContextPermission";

    // - private static final String CLASSNAME_THREAD_PERMISSION =
    // "com.ibm.awb.security.ThreadPermission";
    // # private static final String CLASSNAME_ACTIVITY_PERMISSION =
    // "com.ibm.aglets.security.ActivityPermission";
    private static final String CLASSNAME_AGLET_PROTECTION = "com.ibm.aglet.security.AgletProtection";
    private static final String CLASSNAME_MESSAGE_PROTECTION = "com.ibm.aglet.security.MessageProtection";

    private PolicyGrant grant;

    CardLayout layout = new CardLayout();
    Panel setting_panel = new Panel();

    private List _filePermList = new List(5, false);
    private FilePermissionEditor _filePermEditor = new FilePermissionEditor();

    private List _socketPermList = new List(3, false);
    private SocketPermissionEditor _socketPermEditor = new SocketPermissionEditor();

    private List _awtPermList = new List(3, false);
    private AWTPermissionEditor _awtPermEditor = new AWTPermissionEditor();

    // + private List _netPermList = new List(3, false);
    // + private NetPermissionEditor _netPermEditor = new NetPermissionEditor();

    private List _propertyPermList = new List(3, false);
    private PropertyPermissionEditor _propertyPermEditor = new PropertyPermissionEditor();

    // + private List _reflectPermList = new List(3, false);
    // + private ReflectPermissionEditor _reflectPermEditor = new
    // ReflectPermissionEditor();

    private List _runtimePermList = new List(3, false);
    private RuntimePermissionEditor _runtimePermEditor = new RuntimePermissionEditor();

    private List _securityPermList = new List(3, false);
    private SecurityPermissionEditor _securityPermEditor = new SecurityPermissionEditor();

    // + private List _serializablePermList = new List(3, false);
    // + private SerializablePermissionEditor _serializablePermEditor = new
    // SerializablePermissionEditor();

    private List _allPermList = new List(3, false);
    private AllPermissionEditor _allPermEditor = new AllPermissionEditor();

    private List _agletPermList = new List(3, false);
    private AgletPermissionEditor _agletPermEditor = new AgletPermissionEditor();

    private List _messagePermList = new List(3, false);
    private MessagePermissionEditor _messagePermEditor = new MessagePermissionEditor();

    private List _contextPermList = new List(3, false);
    private ContextPermissionEditor _contextPermEditor = new ContextPermissionEditor();

    // - private List _threadPermList = new List(3, false);
    // - private ThreadPermissionEditor _threadPermEditor = new
    // ThreadPermissionEditor();

    // # private List _activityPermList = new List(3, false);
    // # private ActivityPermissionEditor _activityPermEditor = new
    // ActivityPermissionEditor();

    private List _agletProtList = new List(3, false);
    private AgletProtectionEditor _agletProtEditor = new AgletProtectionEditor();

    private List _messageProtList = new List(3, false);
    private MessageProtectionEditor _messageProtEditor = new MessageProtectionEditor();

    // - /*
    // - * window
    // - */
    // - private Checkbox _winWarning = new
    // Checkbox("Warning Message on Aglet Windows", null, true);
    // - private Checkbox _winOpen = new Checkbox("Enable to Open Windows",
    // null, false);
    // -
    // - /*
    // - * JDBC
    // - */
    // - private Checkbox _enableJDBC = new Checkbox("Enable JDBC");
    // -
    // - /*
    // - * RMI
    // - */
    // - private Checkbox _enableRMIClient = new Checkbox("Enable RMI Client");
    // - private Checkbox _enableRMIServer = new Checkbox("Enable RMI Server");

    SecurityConfigPanel(String name, PolicyGrant grant) {
	this.grant = grant;
    }

    /*
     * add permissions into list
     */
    void addPermissions(PolicyGrant grant) {
	if (grant == null) {
	    return;
	}
	this.addPermissions(grant, this._filePermList, CLASSNAME_FILE_PERMISSION);
	this.addPermissions(grant, this._socketPermList, CLASSNAME_SOCKET_PERMISSION);
	this.addPermissions(grant, this._awtPermList, CLASSNAME_AWT_PERMISSION);

	// + addPermissions(grant, _netPermList, CLASSNAME_NET_PERMISSION);
	this.addPermissions(grant, this._propertyPermList, CLASSNAME_PROPERTY_PERMISSION);

	// + addPermissions(grant, _reflectPermList,
	// CLASSNAME_REFLECT_PERMISSION);
	this.addPermissions(grant, this._runtimePermList, CLASSNAME_RUNTIME_PERMISSION);
	this.addPermissions(grant, this._securityPermList, CLASSNAME_SECURITY_PERMISSION);

	// + addPermissions(grant, _serializablePermList,
	// CLASSNAME_SERIALIZABLE_PERMISSION);
	this.addPermissions(grant, this._allPermList, CLASSNAME_ALL_PERMISSION);
	this.addPermissions(grant, this._agletPermList, CLASSNAME_AGLET_PERMISSION);
	this.addPermissions(grant, this._messagePermList, CLASSNAME_MESSAGE_PERMISSION);
	this.addPermissions(grant, this._contextPermList, CLASSNAME_CONTEXT_PERMISSION);

	// - addPermissions(grant, _threadPermList,
	// CLASSNAME_THREAD_PERMISSION);
	// # addPermissions(grant, _activityPermList,
	// CLASSNAME_ACTIVITY_PERMISSION);
	this.addPermissions(grant, this._agletProtList, CLASSNAME_AGLET_PROTECTION);
	this.addPermissions(grant, this._messageProtList, CLASSNAME_MESSAGE_PROTECTION);
    }

    void addPermissions(PolicyGrant grant, List list, String className) {
	if (grant == null) {
	    return;
	}
	final int num = list.getItemCount();
	int idx = 0;

	for (idx = 0; idx < num; idx++) {
	    final String item = list.getItem(idx);
	    PolicyPermission permission = PermissionPanel.toPermission(className, item);

	    if (permission != null) {
		grant.addPermission(permission);
	    }
	}
    }

    /*
     * add permissions into list
     */
    void addPermissions(EditListPanel elp, String className) {
	if (this.grant == null) {
	    return;
	}

	Enumeration permissions = this.grant.getPermissions(className);

	while (permissions.hasMoreElements()) {
	    Object obj = permissions.nextElement();

	    if (obj instanceof PolicyPermission) {
		PolicyPermission permission = (PolicyPermission) obj;
		Vector args = new Vector();
		final String target = permission.getTargetName();
		final String actions = permission.getActions();
		final boolean t = (target != null) && !target.equals("");
		final boolean a = (actions != null) && !actions.equals("");

		if (t || a) {
		    args.addElement(target);
		}
		if (a) {
		    args.addElement(actions);
		}
		elp.addItemIntoList(args);
	    }
	}
    }

    private BorderPanel getBorderPanel(String label) {
	return new BorderPanel(" " + label + " ");
    }

    /*
     * Handles the events
     */
    public void itemStateChanged(ItemEvent ev) {
	this.layout.show(this.setting_panel, (String) ev.getItemSelectable().getSelectedObjects()[0]);
    }

    /*
     * setup General Permission Panel
     */
    void setupGeneralPermissionPanel(
				     BorderPanel panel,
				     List list,
				     PermissionEditor editor,
				     String className) {
	GridBagConstraints cns = new GridBagConstraints();

	cns.weightx = 1.0;
	cns.weighty = 1.0;
	cns.fill = GridBagConstraints.BOTH;
	cns.ipadx = cns.ipady = 3;
	cns.insets = panel.topInsets();
	cns.insets.bottom = panel.bottomInsets().bottom;

	panel.setConstraints(cns);

	cns.weighty = 0.1;
	EditListPanel elp = new EditListPanel(null, list, editor);

	panel.add(elp, GridBagConstraints.REMAINDER, 1.0);

	cns.insets.top = 0;
	cns.fill = GridBagConstraints.HORIZONTAL;
	cns.weighty = 0.0;
	panel.add(editor, GridBagConstraints.REMAINDER, 0.1);

	this.addPermissions(elp, className);
    }

    public void setupPanels() {
	GridBagConstraints cns = new GridBagConstraints();

	cns.weightx = 0.0;
	cns.weighty = 0.0;
	cns.fill = GridBagConstraints.NONE;
	cns.anchor = GridBagConstraints.WEST;
	cns.ipadx = cns.ipady = 3;
	cns.insets = new Insets(10, 5, 5, 5);

	this.setConstraints(cns);

	Choice choice = new Choice();

	choice.addItem(LABEL_FILE_PERMISSION);
	choice.addItem(LABEL_SOCKET_PERMISSION);
	choice.addItem(LABEL_AWT_PERMISSION);

	// + choice.addItem(LABEL_NET_PERMISSION);
	choice.addItem(LABEL_PROPERTY_PERMISSION);

	// + choice.addItem(LABEL_REFLECT_PERMISSION);
	choice.addItem(LABEL_RUNTIME_PERMISSION);
	choice.addItem(LABEL_SECURITY_PERMISSION);

	// + choice.addItem(LABEL_SERIALIZABLE_PERMISSION);
	choice.addItem(LABEL_ALL_PERMISSION);
	choice.addItem(LABEL_AGLET_PERMISSION);
	choice.addItem(LABEL_MESSAGE_PERMISSION);
	choice.addItem(LABEL_CONTEXT_PERMISSION);

	// - choice.addItem(LABEL_THREAD_PERMISSION);
	// # choice.addItem(LABEL_ACTIVITY_PERMISSION);
	choice.addItem(LABEL_AGLET_PROTECTION);
	choice.addItem(LABEL_MESSAGE_PROTECTION);

	choice.select(LABEL_FILE_PERMISSION);
	choice.addItemListener(this);

	this.add(choice, GridBagConstraints.REMAINDER, 0.0);

	cns.weightx = 1.0;
	cns.weighty = 1.0;
	cns.fill = GridBagConstraints.BOTH;
	cns.anchor = GridBagConstraints.CENTER;
	this.add(this.setting_panel, GridBagConstraints.REMAINDER, 1.0);

	this.setting_panel.setLayout(this.layout);

	BorderPanel filePermPanel = this.getBorderPanel(LABEL_FILE_PERMISSION);

	this.setting_panel.add(LABEL_FILE_PERMISSION, filePermPanel);
	BorderPanel socketPermPanel = this.getBorderPanel(LABEL_SOCKET_PERMISSION);

	this.setting_panel.add(LABEL_SOCKET_PERMISSION, socketPermPanel);
	BorderPanel awtPermPanel = this.getBorderPanel(LABEL_AWT_PERMISSION);

	this.setting_panel.add(LABEL_AWT_PERMISSION, awtPermPanel);

	// + BorderPanel netPermPanel = getBorderPanel(LABEL_NET_PERMISSION);
	// + setting_panel.add(LABEL_NET_PERMISSION, netPermPanel);
	BorderPanel propertyPermPanel = this.getBorderPanel(LABEL_PROPERTY_PERMISSION);

	this.setting_panel.add(LABEL_PROPERTY_PERMISSION, propertyPermPanel);

	// + BorderPanel reflectPermPanel =
	// getBorderPanel(LABEL_REFLECT_PERMISSION);
	// + setting_panel.add(LABEL_REFLECT_PERMISSION, reflectPermPanel);
	BorderPanel runtimePermPanel = this.getBorderPanel(LABEL_RUNTIME_PERMISSION);

	this.setting_panel.add(LABEL_RUNTIME_PERMISSION, runtimePermPanel);
	BorderPanel securityPermPanel = this.getBorderPanel(LABEL_SECURITY_PERMISSION);

	this.setting_panel.add(LABEL_SECURITY_PERMISSION, securityPermPanel);

	// + BorderPanel serializablePermPanel =
	// getBorderPanel(LABEL_SERIALIZABLE_PERMISSION);
	// + setting_panel.add(LABEL_SERIALIZABLE_PERMISSION,
	// serializablePermPanel);
	BorderPanel allPermPanel = this.getBorderPanel(LABEL_ALL_PERMISSION);

	this.setting_panel.add(LABEL_ALL_PERMISSION, allPermPanel);
	BorderPanel agletPermPanel = this.getBorderPanel(LABEL_AGLET_PERMISSION);

	this.setting_panel.add(LABEL_AGLET_PERMISSION, agletPermPanel);
	BorderPanel messagePermPanel = this.getBorderPanel(LABEL_MESSAGE_PERMISSION);

	this.setting_panel.add(LABEL_MESSAGE_PERMISSION, messagePermPanel);
	BorderPanel contextPermPanel = this.getBorderPanel(LABEL_CONTEXT_PERMISSION);

	this.setting_panel.add(LABEL_CONTEXT_PERMISSION, contextPermPanel);

	// - BorderPanel threadPermPanel =
	// getBorderPanel(LABEL_THREAD_PERMISSION);
	// - setting_panel.add(LABEL_THREAD_PERMISSION, threadPermPanel);
	// # BorderPanel activityPermPanel =
	// getBorderPanel(LABEL_ACTIVITY_PERMISSION);
	// # setting_panel.add(LABEL_ACTIVITY_PERMISSION, activityPermPanel);
	BorderPanel agletProtPanel = this.getBorderPanel(LABEL_AGLET_PROTECTION);

	this.setting_panel.add(LABEL_AGLET_PROTECTION, agletProtPanel);
	BorderPanel messageProtPanel = this.getBorderPanel(LABEL_MESSAGE_PROTECTION);

	this.setting_panel.add(LABEL_MESSAGE_PROTECTION, messageProtPanel);

	// - setupFilePermissionPanel(filePermPanel);
	// - setupSocketPermissionPanel(socketPermPanel);
	// - setupAWTPermissionPanel(awtPermPanel);
	// - setupNetPermissionPanel(netPermPanel);
	// - setupPropertyPermissionPanel(propertyPermPanel);
	// - setupReflectPermissionPanel(reflectPermPanel);
	// - setupRuntimePermissionPanel(runtimePermPanel);
	// - setupSecurityPermissionPanel(securityPermPanel);
	// - setupSerializablePermissionPanel(serializablePermPanel);
	// - setupSerializablePermissionPanel(allPermPanel);
	// - setupAgletPermissionPanel(agletPermPanel);
	// - setupMessagePermissionPanel(messagePermPanel);
	// - setupContextPermissionPanel(contextPermPanel);
	// - setupThreadPermissionPanel(threadPermPanel);
	// # //- setupActivityPermissionPanel(activityPermPanel);
	// - setupAgletProtectionPanel(agletProtPanel);
	// - setupMessageProtectionPanel(messageProtPanel);

	this.setupGeneralPermissionPanel(filePermPanel, this._filePermList, this._filePermEditor, CLASSNAME_FILE_PERMISSION);
	this.setupGeneralPermissionPanel(socketPermPanel, this._socketPermList, this._socketPermEditor, CLASSNAME_SOCKET_PERMISSION);
	this.setupGeneralPermissionPanel(awtPermPanel, this._awtPermList, this._awtPermEditor, CLASSNAME_AWT_PERMISSION);

	// + setupGeneralPermissionPanel(netPermPanel, _netPermList,
	// _netPermEditor, CLASSNAME_NET_PERMISSION);
	this.setupGeneralPermissionPanel(propertyPermPanel, this._propertyPermList, this._propertyPermEditor, CLASSNAME_PROPERTY_PERMISSION);

	// + setupGeneralPermissionPanel(reflectPermPanel, _reflectPermList,
	// _reflectPermEditor, CLASSNAME_REFLECT_PERMISSION);
	this.setupGeneralPermissionPanel(runtimePermPanel, this._runtimePermList, this._runtimePermEditor, CLASSNAME_RUNTIME_PERMISSION);
	this.setupGeneralPermissionPanel(securityPermPanel, this._securityPermList, this._securityPermEditor, CLASSNAME_SECURITY_PERMISSION);

	// + setupGeneralPermissionPanel(serializablePermPanel,
	// _serializablePermList, _serializablePermEditor,
	// CLASSNAME_SERIALIZABLE_PERMISSION);
	this.setupGeneralPermissionPanel(allPermPanel, this._allPermList, this._allPermEditor, CLASSNAME_ALL_PERMISSION);
	this.setupGeneralPermissionPanel(agletPermPanel, this._agletPermList, this._agletPermEditor, CLASSNAME_AGLET_PERMISSION);
	this.setupGeneralPermissionPanel(messagePermPanel, this._messagePermList, this._messagePermEditor, CLASSNAME_MESSAGE_PERMISSION);
	this.setupGeneralPermissionPanel(contextPermPanel, this._contextPermList, this._contextPermEditor, CLASSNAME_CONTEXT_PERMISSION);

	// - setupGeneralPermissionPanel(threadPermPanel, _threadPermList,
	// _threadPermEditor, CLASSNAME_THREAD_PERMISSION);
	// # setupGeneralPermissionPanel(activityPermPanel, _activityPermList,
	// _activityPermEditor, CLASSNAME_ACTIVITY_PERMISSION);
	this.setupGeneralPermissionPanel(agletProtPanel, this._agletProtList, this._agletProtEditor, CLASSNAME_AGLET_PROTECTION);
	this.setupGeneralPermissionPanel(messageProtPanel, this._messageProtList, this._messageProtEditor, CLASSNAME_MESSAGE_PROTECTION);
    }

    // - /*
    // - * setup File Permission Panel
    // - */
    // - void setupFilePermissionPanel(BorderPanel panel) {
    // - setupGeneralPermissionPanel(panel, _filePermList, _filePermEditor,
    // CLASSNAME_FILE_PERMISSION);
    // - }
    // -
    // - /*
    // - * setup Socket Permission Panel
    // - */
    // - void setupSocketPermissionPanel(BorderPanel panel) {
    // - setupGeneralPermissionPanel(panel, _socketPermList, _socketPermEditor,
    // CLASSNAME_SOCKET_PERMISSION);
    // - }
    // -
    // - /*
    // - * setup AWT Permission Panel
    // - */
    // - void setupAWTPermissionPanel(BorderPanel panel) {
    // - setupGeneralPermissionPanel(panel, _awtPermList, _awtPermEditor,
    // CLASSNAME_AWT_PERMISSION);
    // - }
    // -
    // - /*
    // - * setup Net Permission Panel
    // - */
    // - void setupNetPermissionPanel(BorderPanel panel) {
    // - setupGeneralPermissionPanel(panel, _netPermList, _netPermEditor,
    // CLASSNAME_NET_PERMISSION);
    // - }
    // -
    // - /*
    // - * setup Property Permission Panel
    // - */
    // - void setupPropertyPermissionPanel(BorderPanel panel) {
    // - setupGeneralPermissionPanel(panel, _propertyPermList,
    // _propertyPermEditor, CLASSNAME_PROPERTY_PERMISSION);
    // - }
    // -
    // - /*
    // - * setup Reflect Permission Panel
    // - */
    // - void setupReflectPermissionPanel(BorderPanel panel) {
    // - setupGeneralPermissionPanel(panel, _reflectPermList,
    // _reflectPermEditor, CLASSNAME_REFLECT_PERMISSION);
    // - }
    // -
    // - /*
    // - * setup Runtime Permission Panel
    // - */
    // - void setupRuntimePermissionPanel(BorderPanel panel) {
    // - setupGeneralPermissionPanel(panel, _runtimePermList,
    // _runtimePermEditor, CLASSNAME_RUNTIME_PERMISSION);
    // - }
    // -
    // - /*
    // - * setup Security Permission Panel
    // - */
    // - void setupSecurityPermissionPanel(BorderPanel panel) {
    // - setupGeneralPermissionPanel(panel, _securityPermList,
    // _securityPermEditor, CLASSNAME_SECURITY_PERMISSION);
    // - }
    // -
    // - /*
    // - * setup Serializable Permission Panel
    // - */
    // - void setupSerializablePermissionPanel(BorderPanel panel) {
    // - setupGeneralPermissionPanel(panel, _serializablePermList,
    // _serializablePermEditor, CLASSNAME_SERIALIZABLE_PERMISSION);
    // - }
    // -
    // - /*
    // - * setup All Other Permission Panel
    // - */
    // - void setupAllPermissionPanel(BorderPanel panel) {
    // - setupGeneralPermissionPanel(panel, _allPermList, _allPermEditor,
    // CLASSNAME_ALL_PERMISSION);
    // - }
    // -
    // - /*
    // - * setup Aglet Permission Panel
    // - */
    // - void setupAgletPermissionPanel(BorderPanel panel) {
    // - setupGeneralPermissionPanel(panel, _agletPermList, _agletPermEditor,
    // CLASSNAME_AGLET_PERMISSION);
    // - }
    // -
    // - /*
    // - * setup Message Permission Panel
    // - */
    // - void setupMessagePermissionPanel(BorderPanel panel) {
    // - setupGeneralPermissionPanel(panel, _messagePermList,
    // _messagePermEditor, CLASSNAME_MESSAGE_PERMISSION);
    // - }
    // -
    // - /*
    // - * setup Aglet Context Permission Panel
    // - */
    // - void setupContextPermissionPanel(BorderPanel panel) {
    // - setupGeneralPermissionPanel(panel, _contextPermList,
    // _contextPermEditor, CLASSNAME_CONTEXT_PERMISSION);
    // - }
    // -
    // - /*
    // - * setup Thread Permission Panel
    // - */
    // - void setupThreadPermissionPanel(BorderPanel panel) {
    // - setupGeneralPermissionPanel(panel, _threadPermList, _threadPermEditor,
    // CLASSNAME_THREAD_PERMISSION);
    // - }
    // -
    // # //- /*
    // # //- * setup Activity Permission Panel
    // # //- */
    // # //- void setupActivityPermissionPanel(BorderPanel panel) {
    // # //- setupGeneralPermissionPanel(panel, _activityPermList,
    // _activityPermEditor, CLASSNAME_ACTIVITY_PERMISSION);
    // # //- }
    // # //-
    // - /*
    // - * setup Aglet Protection Panel
    // - */
    // - void setupAgletProtectionPanel(BorderPanel panel) {
    // - setupGeneralPermissionPanel(panel, _agletProtList, _agletProtEditor,
    // CLASSNAME_AGLET_PROTECTION);
    // - }
    // -
    // - /*
    // - * setup Message Protection Panel
    // - */
    // - void setupMessageProtectionPanel(BorderPanel panel) {
    // - setupGeneralPermissionPanel(panel, _messageProtList,
    // _messageProtEditor, CLASSNAME_MESSAGE_PROTECTION);
    // - }
    // -
    //
    //
    // - void restoreDefaults() {
    // - //- _winWarning.setState(true);
    // - _filePermList.removeAll();
    // - _socketPermList.removeAll();
    // - _awtPermList.removeAll();
    // - _netPermList.removeAll();
    // - _propertyPermList.removeAll();
    // - _reflectPermList.removeAll();
    // - _runtimePermList.removeAll();
    // - _securityPermList.removeAll();
    // - _serializablePermList.removeAll();
    // - _allPermList.removeAll();
    // - _agletPermList.removeAll();
    // - _messagePermList.removeAll();
    // - _contextPermList.removeAll();
    // - _threadPermList.removeAll();
    // - //# _activityPermList.removeAll();
    // - _agletProtList.removeAll();
    // - _messageProtList.removeAll();
    // - }

    /*
	 * 
	 */
    void updateValues() {

	// - Resource privilege_res = Resource.getResourceFor("security." +
	// name);
	// -
	// - Resource res = Resource.getResourceFor("aglets");
	// - /* read */
	// - _readPathList.removeAll();
	// - String readDirs = privilege_res.getString("file.read");
	// - StringTokenizer st =
	// - new StringTokenizer(readDirs == null ? "" : readDirs, " ,;");
	// - while(st.hasMoreTokens()) {
	// - _readPathList.addItem( st.nextToken() );
	// - }
	// -
	// - /* write */
	// - _writePathList.removeAll();
	// - String writeDirs = privilege_res.getString("file.write");
	// - st = new StringTokenizer(writeDirs == null ? "" : writeDirs,
	// " ,;");
	// - while(st.hasMoreTokens()) {
	// - _writePathList.addItem( st.nextToken() );
	// - }
	// -
	// - String s[] =
	// privilege_res.getPersistentResourcesStartsWith("socket.connect.");
	// - _connectHosts.removeAll();
	// - if (s != null && s.length>0) {
	// - for(int i=0; i<s.length; i++) {
	// - _connectHosts.addItem(s[i].substring(15));
	// - }
	// - }
	// -
	// - s =
	// privilege_res.getPersistentResourcesStartsWith("socket.listen.");
	// - _listenPorts.removeAll();
	// -
	// - if (s != null && s.length>0) {
	// - StringBuffer buff = new StringBuffer();
	// - for(int i=0; i<s.length; i++) {
	// - _listenPorts.addItem(s[i].substring(14));
	// - }
	// - }
	// -
	// - /* others */
	// -
	// _winWarning.setState(privilege_res.getBoolean("window.warning",false));
	// - _winOpen.setState(privilege_res.getBoolean("window.open", false));
	// -
	// -
	// _enableJDBC.setState(privilege_res.getBoolean("enable.jdbc",false));
	// -
	// _enableRMIClient.setState(privilege_res.getBoolean("enable.rmiclient",false));
	// -
	// _enableRMIServer.setState(privilege_res.getBoolean("enable.rmiserver",false));
    }
}
