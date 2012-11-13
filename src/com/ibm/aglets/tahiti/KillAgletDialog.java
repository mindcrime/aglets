/*
 * Created on Oct 16, 2004
 *
 * @author Luca Ferrari, <a href="mailto:cat4hire@users.sourceforge.net">cat4hire@users.sourceforge.net</a>
 */
package com.ibm.aglets.tahiti;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import net.sourceforge.aglets.util.gui.JComponentBuilder;

import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.system.AgletRuntime;
import com.ibm.aglets.tahiti.utils.IconRepository;
import com.ibm.aglets.tahiti.utils.TahitiCommandStrings;

/**
 * 
 */
public class KillAgletDialog extends TahitiDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -310269321700342540L;
	/**
	 * The proxy to kill.
	 */
	protected AgletProxy _proxies[] = null;

	/**
	 * Default constructor.
	 * 
	 * @param parent
	 *            the main window of this dialog
	 * @param proxies
	 *            an array of proxies to kill
	 */
	public KillAgletDialog(final MainWindow parent, final AgletProxy[] proxies) {
		super(parent);

		if ((proxies == null) || (proxies.length == 0)) {
			JOptionPane.showMessageDialog(this, translator.translate("dialog.kill.error.proxy"), translator.translate("dialog.kill.title"), JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("proxy"));
			return;
		}

		final String msg[] = new String[proxies.length];

		final JTextArea area = new JTextArea(msg.length, 100);
		for (int i = 0; i < proxies.length; i++) {
			msg[i] = getAgletName(proxies[i]);
			area.append(msg[i]);
			area.append("\n");
		}

		getContentPane().add("North", JComponentBuilder.createJLabel("dialog.kill.message"));

		getContentPane().add("Center", area);

		// add buttons
		final JButton okButton = JComponentBuilder.createJButton("dialog.kill.button.ok", TahitiCommandStrings.OK_COMMAND, this);
		final JButton cancelButton = JComponentBuilder.createJButton("dialog.kill.button.cancel", TahitiCommandStrings.CANCEL_COMMAND, this);
		getContentPane().add(okButton);
		getContentPane().add(cancelButton);

		_proxies = proxies;
	}

	/**
	 * Manage events from buttons.
	 * 
	 * @param event
	 *            the event to deal with
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		final String command = event.getActionCommand();

		if (command.equals(TahitiCommandStrings.OK_COMMAND)
				&& (_proxies != null) && (_proxies.length > 0)) {
			try {
				for (final AgletProxy _proxie : _proxies) {
					AgletRuntime.getAgletRuntime().killAglet(_proxie);
				}
			} catch (final Exception ex) {

				logger.error("Exception caught while killing agents", ex);

				getMainWindow().showException(ex);

			}

		}

		setVisible(false);
		dispose();
	}

	private String getAgletName(final AgletProxy agletProxy) {
		final StringBuffer agletName = new StringBuffer(100);
		try {
			final AgletInfo info = agletProxy.getAgletInfo();
			agletName.append("Classname:");
			agletName.append(info.getAgletClassName());
			agletName.append(" - Owner:");
			agletName.append(info.getAuthorityName());
			agletName.append(" - From:");
			agletName.append(info.getOrigin());
			agletName.append(" - Address:");
			agletName.append(info.getAddress());
		} catch (final InvalidAgletException e) {
			logger.error("Cannot get the name of the aglet", e);
		} finally {
			return agletName.toString();
		}
	}
}
