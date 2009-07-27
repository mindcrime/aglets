package com.ibm.aglets.tahiti;

/*
 * @(#)NetworkConfigDialog.java
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

import com.ibm.atp.auth.SharedSecret;
import com.ibm.atp.auth.SharedSecrets;

// import com.ibm.atp.auth.Challenge;
// import com.ibm.atp.auth.Randoms;

// import com.ibm.aglets.security.User;
// import com.ibm.aglets.security.UserAuthenticator;
// import com.ibm.aglets.security.UserAdministrator;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.Graphics;
import java.awt.TextField;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.io.File;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.URL;

import java.security.cert.Certificate;

import java.util.Hashtable;
import java.util.Enumeration;

import com.ibm.aglet.system.AgletRuntime;
import com.ibm.awb.misc.Resource;
import com.ibm.awb.misc.FileUtils;
import javax.swing.*;
import com.ibm.aglets.tahiti.utils.*;

/**
 * Class NetworkConfigDialog represents the dialog for
 * 
 * @version     1.03    96/04/15
 * @author      Danny B. Lange
 * @modified  97/02/17 Yariv Aridor : add subsription support
 */

final class NetworkConfigDialog extends TahitiDialog implements ActionListener{

    private static final String ACTION_RESTORE_DEFAULTS = "Restore Defaults";

    /*
     * Proxy Configuration
     */
    private JCheckBox _useProxy = new JCheckBox(bundle.getString("dialog.netprefs.button.useproxy"));

    private JTextField _proxyHost = new JTextField(30);

    private JTextField _proxyPort = new JTextField(5);

    private JTextField _noProxy = new JTextField(35);

    /*
     *  
     */
    private JCheckBox _httpTunneling = new JCheckBox(bundle.getString("dialog.netprefs.button.http"));

    private JCheckBox _httpMessaging = new JCheckBox(bundle.getString("dialog.netprefs.button.httpmessaging"));

    /* subscription panel */
    private static final int UNDEFINED = 0;

    private static final int YES = 1;

    private static final int NO = 2;

    private static final String ACTION_OK = "OK";

    private static final String ACTION_SUBSCRIBE = "subscribe";

    private static final String ACTION_UNSUBSCRIBE = "unsubscribe";

    protected JButton _subscribe = new JButton("Subscribe");

    protected JButton _unsubscribe = new JButton("Unsubscribe");

    private JTextField _boxHost = new JTextField(35);

    private JTextField _boxUserid = new JTextField(35);

    private JPasswordField _boxPasswd = new JPasswordField(35);

    private Choice _updateChoice = new Choice();

    private int _boxSubscribe = UNDEFINED;

    /* authentication panel */
    private JCheckBox _authenticationMode = new JCheckBox(
            "Do Authentication on ATP Connection");

    private JCheckBox _secureRandomSeed = new JCheckBox(
            "Use Secure Random Seed");

    private static final String CREATE_SHARED_SECRET = "Create a new shared secret";

    private JButton _createSharedSecret = new JButton(CREATE_SHARED_SECRET);

    private static final String REMOVE_SHARED_SECRET = "Remove a shared secret";

    private JButton _removeSharedSecret = new JButton(REMOVE_SHARED_SECRET);

    private static final String IMPORT_SHARED_SECRET = "Import a shared secret";

    private JButton _importSharedSecret = new JButton(IMPORT_SHARED_SECRET);

    private static final String EXPORT_SHARED_SECRET = "Export a shared secret";

    private JButton _exportSharedSecret = new JButton(EXPORT_SHARED_SECRET);

    /*
     * Singleton instance reference.
     */
    private static NetworkConfigDialog _instance = null;

    private int boxUpdateValues[] = { 0, 15, 30, 60, 60 * 5, 60 * 15, 60 * 60 };

    /*
     * Constructs a new Aglet creation dialog.
     */
    private NetworkConfigDialog(MainWindow parent) {
        super(parent, bundle.getString("dialog.netprefs.title"), true);

        makePanel();

        // add buttons
        this.addJButton(bundle.getString("dialog.netprefs.button.ok"),TahitiCommandStrings.OK_COMMAND,IconRepository.getIcon("ok"),this);
        this.addJButton(bundle.getString("dialog.netprefs.button.cancel"),TahitiCommandStrings.CANCEL_COMMAND,IconRepository.getIcon("cancel"),this);
        this.addJButton(bundle.getString("dialog.netprefs.button.default"),TahitiCommandStrings.OK_COMMAND,IconRepository.getIcon("default"),this);
        
    }


    /**
     * Manage button events.
     * @param event the event to manage
     */
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
System.out.println("command "+command);
        
        if(command.equals(TahitiCommandStrings.OK_COMMAND)){
            this.dispose();
            JOptionPane.showMessageDialog(this,bundle.getString("dialog.netprefs.reboot.message"),bundle.getString("dialog.netprefs.reboot.title"),JOptionPane.WARNING_MESSAGE,IconRepository.getIcon("reboot"));
        }
        else
        if(command.equals(TahitiCommandStrings.CREATE_COMMAND)){
            // create a shared secret
            this.createSharedSecret();
            return;
        }
        else
        if(command.equals(TahitiCommandStrings.REMOVE_COMMAND)){
            // remove a shared secret
            this.removeSharedSecret();
            return;
        }
        else
        if(command.equals(TahitiCommandStrings.EXPORT_COMMAND)){
            // export a shared secret
            this.exportSharedSecret();
            return;
        }
        else
        if(command.equals(TahitiCommandStrings.IMPORT_COMMAND)){
            // import a shared secret
            this.importSharedSecret();
            return;
        }
        
        
        
            
        this.setVisible(false);
        this.dispose();
    }

    /**
     * Now using a swing dialog
     *
     */
    private void createSharedSecret() {
        TahitiDialog d = new CreateSharedSecretDialog(getMainWindow());

        d.popupAtCenterOfParent();
    }

    private void exportSharedSecret() {
        SharedSecrets secrets = SharedSecrets.getSharedSecrets();

        if (secrets == null) {
            JOptionPane.showMessageDialog(this,bundle.getString("dialog.netprefs.exportsharedsecret.error"),bundle.getString("dialog.netprefs.exportsharedsecret.error.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("secret"));
            return;
        }

        TahitiDialog d = new ExportSharedSecret(getMainWindow(), secrets,this);

        d.popupAtCenterOfParent();
    }

    /*
     * Singletion method to get the instnace
     */
    static NetworkConfigDialog getInstance(MainWindow parent) {
        if (_instance == null) {
            _instance = new NetworkConfigDialog(parent);
        } else {
            _instance.updateValues();
        }
        return _instance;
    }

    protected static String getOwnerName() {
        com.ibm.aglet.system.AgletRuntime runtime = com.ibm.aglet.system.AgletRuntime
                .getAgletRuntime();

        if (runtime == null) { return null; }
        return runtime.getOwnerName();
    }

    /*
     * Subscribe
     */
    private int getStatusCode(Hashtable headers) {
        int defValue = -1;

        try {
            String ret = (String) (headers.get("status-code"));

            return ret == null ? defValue : Integer.parseInt(ret);
        } catch (Exception e) {
        }
        return defValue;
    }

    // TEMPRARY
    private int getSubscribeStatus(String s) {
        if (s.equalsIgnoreCase("yes")) {
            return YES;
        } else if (s.equalsIgnoreCase("no")) {
            return NO;
        } else if (s.equalsIgnoreCase("undefined")) { return UNDEFINED; }
        return UNDEFINED;
    }

    private void importSharedSecret() {
        TahitiDialog d = new ImportSharedSecret(getMainWindow(),this);
        d.popupAtCenterOfParent();
    }


    public void itemStateChanged(ItemEvent ev) {
        updateGUIState();
    }

    /*
     * Layouts all components
     */
    void makePanel() {
        GridBagPanel p = new GridBagPanel();

        this.getContentPane().add("Center", p);

        GridBagConstraints cns = new GridBagConstraints();

        cns.anchor = GridBagConstraints.WEST;
        cns.fill = GridBagConstraints.BOTH;
        cns.weightx = 1.0;
        cns.weighty = 1.0;
        cns.insets = new Insets(5, 5, 5, 5);

        p.setConstraints(cns);

        BorderPanel panel = new BorderPanel(bundle.getString("dialog.netprefs.borderpanel.http"));

        p.add(panel, GridBagConstraints.REMAINDER);
        setupHttpTunnelingPanel(panel);


        panel = new BorderPanel(bundle.getString("dialog.netprefs.borderpanel.authentication"));
        p.add(panel, GridBagConstraints.REMAINDER);
        setupAuthenticationPanel(panel);

        panel = new BorderPanel(bundle.getString("dialog.netprefs.borderpanel.others"));
        p.add(panel, GridBagConstraints.REMAINDER);
        setupOthersPanel(panel);

        /*_updateChoice.addItem("None");
        _updateChoice.addItem("At most every 15sec");
        _updateChoice.addItem("At most every 30sec");
        _updateChoice.addItem("At most every 1 min");
        _updateChoice.addItem("At most every 5 min");
        _updateChoice.addItem("At most every 15min");
        _updateChoice.addItem("At most once an hour");
        */
        updateValues();
        
    }

    protected Panel makeSubscribeButtonPanel() {
        Panel p = new Panel();

        p.setLayout(new FlowLayout(FlowLayout.RIGHT));
        p.add(_subscribe);
        p.add(_unsubscribe);
        _subscribe.setActionCommand(ACTION_SUBSCRIBE);
        _subscribe.addActionListener(this);

        _unsubscribe.setActionCommand(ACTION_UNSUBSCRIBE);
        _unsubscribe.addActionListener(this);

        return p;
    }

    private void removeSharedSecret() {


        SharedSecrets secrets = SharedSecrets.getSharedSecrets();

        if (secrets == null) {
            TahitiDialog.alert(getMainWindow(), "No shared secrets")
                    .popupAtCenterOfParent();
            return;
        }
        TahitiDialog d = new RemoveSharedSecret(getMainWindow(), secrets,this);

        d.popupAtCenterOfParent();
    }

    void restoreDefaults() {
        _updateChoice.select(0);
        _httpTunneling.setSelected(false);
        _httpMessaging.setSelected(false);
    }

    /*
     * private boolean check () { int min = -1; PopupMessageWindow mw = null;
     * 
     * try { min = Integer.parseInt(_boxUpdate.getText().trim()); }
     * catch(Exception e) { min = -1; }
     * 
     * if (min <0) { (mw=new PopupMessageWindow(_parent,"ERROR!!","invalid
     * update setting")).popup(_parent); return false; } return true; }
     */

    private boolean save() {
        boolean changed = false;
        String value;
        Resource system_res = Resource.getResourceFor("system");
        Resource atp_res = Resource.getResourceFor("atp");

        boolean use = _useProxy.isSelected();

        if (use != atp_res.getBoolean("atp.useHttpProxy", false)) {
            changed = true;
        }
        atp_res.setResource("atp.useHttpProxy", String.valueOf(use));

        value = _proxyHost.getText().trim();
        if (value.equals(atp_res.getString("atp.http.proxyHost")) == false) {
            changed = true;
        }
        atp_res.setResource("atp.http.proxyHost", value);
        system_res.setResource("proxyHost", use ? value : "");
        system_res.setResource("http.proxyHost", use ? value : "");

        value = _proxyPort.getText().trim();
        if (value.equals(atp_res.getString("atp.http.proxyPort")) == false) {
            changed = true;
        }
        atp_res.setResource("atp.http.proxyPort", value);
        system_res.setResource("proxyPort", use ? value : "");
        system_res.setResource("http.proxyPort", use ? value : "");

        value = _noProxy.getText().trim();
        if (value.equals(atp_res.getString("atp.noProxy")) == false) {
            changed = true;
        }
        atp_res.setResource("atp.noProxy", value);
        system_res.setResource("http.nonProxyHosts", value);

        /*
         * allow/disallow http tunneling/messaging
         */
        atp_res.setResource("atp.http.tunneling", String.valueOf(_httpTunneling
                .isSelected()));
        atp_res.setResource("atp.http.messaging", String.valueOf(_httpMessaging
                .isSelected()));

        // com.ibm.atp.daemon.Daemon.update();

        /*
         * authentication
         */
        final boolean auth = _authenticationMode.isSelected();

        if (auth != atp_res.getBoolean("atp.authentication", false)) {
            changed = true;
        }
        atp_res.setResource("atp.authentication", String.valueOf(auth));
        if (auth) {
            System.out.println("AUTHENTICATION MODE ON.");

            // SharedSecrets.getSharedSecrets();
        } else {
            System.out.println("AUTHENTICATION MODE OFF.");
        }
        final boolean secureseed = _secureRandomSeed.isSelected();

        atp_res.setResource("atp.secureseed", String.valueOf(secureseed));
        if (secureseed) {
            System.out.println("USE SECURE RANDOM SEED.");
        } else {
            System.out.println("USE UNSECURE PSEUDO RANDOM SEED.");
        }

        // Randoms.setUseSecureRandomSeed(secureseed);
        if (auth) {
            System.out
                    .print("[Generating random seed ... wait for a while ... ");
            if (auth) {

                // Randoms.getRandomGenerator(Challenge.LENGTH);
            }
            System.out.println("done.]");
        }

        Resource aglets_res = Resource.getResourceFor("aglets");

        if (Tahiti.enableBox) {
            aglets_res.setResource("aglets.box.update.sec", String
                    .valueOf(updateValue(_updateChoice.getSelectedIndex())));
            aglets_res.setResource("aglets.box.update", String
                    .valueOf(_updateChoice.getSelectedIndex()));

            aglets_res
                    .setResource("aglets.box.host", _boxHost.getText().trim());
            aglets_res.setResource("aglets.box.passwd", new String(_boxPasswd.getText()
                    .trim()));
            aglets_res.setResource("aglets.box.userid", _boxUserid.getText()
                    .trim());
            aglets_res.setResource("aglets.box.subscribe",
                    setSubscribeStatus(_boxSubscribe));
        }
        aglets_res.save("Tahiti");
        atp_res.save("Tahiti");

        // REMIND: needs update
        // com.ibm.atp.protocol.http.HttpProxy.update();
        return changed;
    }

    private String setSubscribeStatus(int i) {
        switch (i) {
        case YES:
            return "yes";
        case NO:
            return "no";
        case UNDEFINED:
            return "undefined";
        default:
            return "undefined";
        }
    }

    /*
     * Make the authentication panel
     */
    void setupAuthenticationPanel(BorderPanel authPanel) {
        authPanel.setLayout(new java.awt.GridLayout(2,2));
        
        // creation of a new secret
        this._createSharedSecret = new JButton(bundle.getString("dialog.netprefs.button.createsharedsecret"));
        this._createSharedSecret.setIcon(IconRepository.getIcon("create"));
        this._createSharedSecret.setActionCommand(TahitiCommandStrings.CREATE_COMMAND);
        this._createSharedSecret.addActionListener(this);
        authPanel.add(_createSharedSecret);
        
        // remove a shared secret
        this._removeSharedSecret = new JButton(bundle.getString("dialog.netprefs.button.removesharedsecret"));
        this._removeSharedSecret.setIcon(IconRepository.getIcon("remove"));
        this._removeSharedSecret.setActionCommand(TahitiCommandStrings.REMOVE_COMMAND);
        this._removeSharedSecret.addActionListener(this);
        authPanel.add(this._removeSharedSecret);
        
        // import a shared secret
        this._importSharedSecret = new JButton(bundle.getString("dialog.netprefs.button.importsharedsecret"));
        this._importSharedSecret.setIcon(IconRepository.getIcon("import"));
        this._importSharedSecret.setActionCommand(TahitiCommandStrings.IMPORT_COMMAND);
        this._importSharedSecret.addActionListener(this);
        authPanel.add(_importSharedSecret);
        
        
        // export a shared secret
        this._exportSharedSecret = new JButton(bundle.getString("dialog.netprefs.button.exportsharedsecret"));
        this._exportSharedSecret.setIcon(IconRepository.getIcon("export"));
        this._exportSharedSecret.setActionCommand(TahitiCommandStrings.EXPORT_COMMAND);
        this._exportSharedSecret.addActionListener(this);
        authPanel.add(_exportSharedSecret);
    }

    /*
     * Make the GUI for setting http proxy
     */
    void setupHttpTunnelingPanel(BorderPanel proxyPanel) {
        GridBagConstraints cns = new GridBagConstraints();

        cns.anchor = GridBagConstraints.WEST;
        cns.fill = GridBagConstraints.NONE;
        cns.weighty = 1.0;
        cns.insets = proxyPanel.topInsets();
        cns.insets.bottom = proxyPanel.bottomInsets().bottom;

        proxyPanel.setConstraints(cns);

        proxyPanel.add(_httpTunneling, GridBagConstraints.REMAINDER, 1.0);
        proxyPanel.add(_useProxy, GridBagConstraints.REMAINDER, 1.0);

        cns.fill = GridBagConstraints.HORIZONTAL;
        cns.insets = proxyPanel.bottomInsets();

        proxyPanel.add(new JLabel(bundle.getString("dialog.netprefs.label.proxy")), 1, 0.1);
        proxyPanel.add(_proxyHost, 1, 1.0);
        proxyPanel.add(new JLabel(bundle.getString("dialog.netprefs.label.port")), 1, 0.1);
        proxyPanel.add(_proxyPort, GridBagConstraints.REMAINDER, 0.4);

        proxyPanel.add(new JLabel(bundle.getString("dialog.netprefs.label.noproxy")), GridBagConstraints.REMAINDER, 1.0);
        proxyPanel.add(_noProxy, GridBagConstraints.REMAINDER, 1.0);

        
    }

    /*
     *  
     */
    void setupOthersPanel(BorderPanel othersPanel) {
        GridBagConstraints cns = new GridBagConstraints();

        cns.anchor = GridBagConstraints.WEST;
        cns.fill = GridBagConstraints.NONE;
        cns.weighty = 1.0;

        othersPanel.setConstraints(cns);

        cns.insets = othersPanel.topInsets();
        cns.insets.bottom = othersPanel.bottomInsets().bottom;
        othersPanel.add(_httpMessaging, GridBagConstraints.REMAINDER, 1.0);
    }

    
    void updateGUIState() {
        _boxUserid.setEnabled(false);
        boolean b = _useProxy.isSelected();

        _proxyHost.setEnabled(b);
        _proxyPort.setEnabled(b);
        _noProxy.setEnabled(b);

        _boxUserid.setEnabled(false);

    }

    private int updateIndex(int value) {
        int i = -1;

        for (int j = 0; j < boxUpdateValues.length; j++) {
            if (value == boxUpdateValues[j]) {
                i = j;
                break;
            }
        }
        return i;
    }


    private int updateValue(int index) {
        return (index < boxUpdateValues.length) ? boxUpdateValues[index] : 0;
    }

    /*
     * Setting values
     */
    private void updateValues() {

        Resource atp_res = Resource.getResourceFor("atp");

        _proxyHost.setText(atp_res.getString("atp.http.proxyHost", ""));
        _proxyPort.setText(atp_res.getString("atp.http.proxyPort", ""));

        _noProxy.setText(atp_res.getString("atp.noProxy", ""));
        _useProxy.setSelected(atp_res.getBoolean("atp.useHttpProxy", false));

        Resource res = Resource.getResourceFor("aglets");

        /*
         * allow/disallow http tunneling/messaging
         */
        _httpTunneling.setSelected(atp_res.getBoolean("atp.http.tunneling",
                false));
        _httpMessaging.setSelected(atp_res.getBoolean("atp.http.messaging",
                false));

        /*
         * Authentication
         */
        _authenticationMode.setSelected(atp_res.getBoolean(
                "atp.authentication", false));
        _secureRandomSeed.setSelected(atp_res.getBoolean("atp.secureseed",
                false));

        /*
         * BOX
         */
        String email = res.getString("aglets.box.userid", "");

        // _useBox.setState( res.getBoolean("aglets.box.enabled", false) );
        _boxUserid.setText(res.getString("aglets.box.userid", email));
        _boxPasswd.setText(res.getString("aglets.box.passwd"));

        // _useUpdate.setState( res.getBoolean("aglets.box.update.enabled",
        // false));
        //_updateChoice.select(updateIndex(res.getInteger(
        //        "aglets.box.update.sec", 0)));
       // _updateChoice.select(res.getInteger("aglets.box.update", 0));

        _boxHost.setText(res.getString("aglets.box.host"));
        String tmp = res.getString("aglets.box.subscribe", "undefined");

        _boxSubscribe = (_boxSubscribe == UNDEFINED) ? getSubscribeStatus(tmp)
                : _boxSubscribe;

        updateGUIState();
    }
}