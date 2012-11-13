package com.ibm.aglets.tahiti;

import java.io.File;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import net.sourceforge.aglets.util.AgletsTranslator;

public class TahitiBrowser extends TahitiDialog implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3146407531945018490L;
	/**
	 * The main page to display
	 */
	private URL mainPage = null;

	/**
	 * Creates a mini browser instance.
	 * 
	 * @param mainWindow
	 *            the window owner of this dialog
	 * @param fileName
	 *            the file name to set for the browser
	 */
	public TahitiBrowser(final TahitiWindow mainWindow, final String fileName) {

		super(mainWindow);
		try {
			mainPage = new File(fileName).toURI().toURL();
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(this, translator.translate(baseKey
					+ ".URLError.title"), translator.translate(baseKey
							+ ".URLError"), JOptionPane.ERROR_MESSAGE);
			logger.error("Cannot load Documentation file " + fileName);
		}
	}

	/**
	 * Initializes the browser with an url.
	 * 
	 * @param mainWindow
	 *            the owner of this dialog
	 * @param webPage
	 *            the main web page from which to start
	 */
	public TahitiBrowser(final TahitiWindow mainWindow, final URL webPage) {
		super(mainWindow);
		this.setSize(200, 200);
		mainPage = webPage;
	}

	/**
	 * Show the main page of this browser.
	 * 
	 */
	@Override
	public void run() {
		showPage();
	}

	/**
	 * Method to read a page
	 */
	protected void showPage() {

		try {

			// declare the JEditorPane and associate to it the HTMLEditor
			// use it as final variable in order to make it accessible to the
			// hyperlink listener
			final JEditorPane editorPanel = new JEditorPane();
			final HTMLEditorKit kit = new HTMLEditorKit();
			editorPanel.setContentType("text/html"); // show only text/html
			// content type
			editorPanel.setEditorKit(kit); // associate the editor panel and the
			// HTML editor
			editorPanel.setPage(mainPage); // show the file
			editorPanel.setEditable(false); // do not allow the content to be
			// changed by the user

			// associate a new link listener
			editorPanel.addHyperlinkListener(new HyperlinkListener() {
				@Override
				public void hyperlinkUpdate(final HyperlinkEvent event) {
					try {
						if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
							// change the displayed page
							editorPanel.setPage(new java.net.URL(event.getURL().toExternalForm()));

					} catch (final Exception ez) {
						final AgletsTranslator translator = TahitiBrowser.this.getAgletsTranslator();
						JOptionPane.showMessageDialog(editorPanel, translator.translate(TahitiBrowser.this.baseKey
								+ ".URLError.title"), translator.translate(TahitiBrowser.this.baseKey
										+ ".URLError"), JOptionPane.ERROR_MESSAGE);
						TahitiBrowser.this.logger.error("Cannot load the page "
								+ event.getURL());

					}
				}

			}

			);

			// use a jscroll pane for the editor
			final JScrollPane sp = new JScrollPane();
			sp.getViewport().add(editorPanel);

			getContentPane().add(sp);
			editorPanel.setVisible(true);
			/* set the title of the window to the page visualized */
			setTitle(getTitle() + " - "
					+ editorPanel.getPage().toExternalForm());
			setVisible(true);
		} catch (final Exception ez) {
			JOptionPane.showMessageDialog(this, translator.translate(baseKey
					+ ".URLError.title"), translator.translate(baseKey
							+ ".URLError"), JOptionPane.ERROR_MESSAGE);
		}

	}

}
