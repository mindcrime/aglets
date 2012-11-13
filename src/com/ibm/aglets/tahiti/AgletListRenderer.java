/**
 * 
 */
package com.ibm.aglets.tahiti;

import java.awt.Color;
import java.awt.Component;
import java.security.cert.X509Certificate;
import java.util.Locale;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;

import net.sourceforge.aglets.util.AgletsTranslator;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglets.AgletProxyImpl;

/**
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 *         26/set/07
 */
public class AgletListRenderer extends DefaultListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5494221603098422328L;

	/**
	 * The translator of this class object.
	 */
	private AgletsTranslator translator = null;

	/**
	 * The base key used for the translating.
	 */
	private final String baseKey = this.getClass().getName();

	/**
	 * Strings used in the visualization of a row. They are statically set each
	 * time a constructor is called, thus to not recall the translate method
	 * each time the line must be repainted.
	 */
	private static String agletIDString = null;
	private static String creationTimeString = null;
	private static String certificateString = null;
	private static Icon activeIcon = null;
	private static Icon normalIcon = null;
	private static Icon deactivatedIcon = null;

	public AgletListRenderer(final AgletListPanel panel) {
		super();
		translator = AgletsTranslator.getInstance("tahiti", Locale.getDefault());
		// localize strings
		agletIDString = translator.translate(baseKey + ".agletID");
		certificateString = translator.translate(baseKey
				+ ".certificate");
		creationTimeString = translator.translate(baseKey
				+ ".creationTime");
		activeIcon = JComponentBuilder.getIcon(baseKey + ".running");
		normalIcon = JComponentBuilder.getIcon(baseKey);
		deactivatedIcon = JComponentBuilder.getIcon(baseKey
				+ ".deactivated");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax
	 * .swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(
	                                              final JList list,
	                                              final Object value,
	                                              final int index,
	                                              final boolean isSelected,
	                                              final boolean cellHasFocus) {

		// create a JLabel for the component to show
		final JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		try {
			// if this is an aglet proxy extract the information from it
			if (value instanceof AgletProxy) {

				// information about the aglet
				final AgletProxy proxy = (AgletProxy) value;
				final AgletInfo info = proxy.getAgletInfo();

				// get the information about the proxy
				final StringBuffer buffer = new StringBuffer(500);
				buffer.append(proxy.getAgletClassName());
				buffer.append(" - ");
				buffer.append(agletIDString);
				buffer.append(proxy.getAgletID());
				buffer.append(" - ");
				buffer.append(creationTimeString);
				buffer.append(info.getCreationTime());
				buffer.append(" - ");
				buffer.append(certificateString);
				buffer.append(((X509Certificate) info.getAuthorityCertificate()).getSubjectDN().getName());

				// set the text of the label
				label.setText(buffer.toString());

				// set the color
				if (!isSelected) {
					label.setForeground(Color.BLUE);
					label.setBackground(Color.WHITE);
				} else {
					// the item is selected
					label.setForeground(Color.GREEN);
					label.setBackground(Color.BLACK);
				}

				// get the state of the agent and select an appropriate icon for
				// it
				Icon icon = null;
				if (proxy.isActive())
					icon = activeIcon;
				else
					icon = normalIcon;

				if (icon != null)
					setIcon(icon);

				// set the tooltip
				if (proxy instanceof AgletProxyImpl)
					label.setToolTipText(((AgletProxyImpl) proxy).toHTMLString());
			}

		} catch (final AgletException e) {
			label.setText("!!NO INFORMATION!!");
		}

		// all done
		return label;

	}

}
