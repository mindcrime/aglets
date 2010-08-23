package com.ibm.aglets.tahiti;

import java.io.File;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import org.aglets.util.AgletsTranslator;

public class TahitiBrowser extends TahitiDialog implements Runnable {

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
    public TahitiBrowser(TahitiWindow mainWindow, String fileName) {

	super(mainWindow);
	try {
	    this.mainPage = new File(fileName).toURI().toURL();
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
		    + ".URLError.title"), this.translator.translate(this.baseKey
			    + ".URLError"), JOptionPane.ERROR_MESSAGE);
	    this.logger.error("Cannot load Documentation file " + fileName);
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
    public TahitiBrowser(TahitiWindow mainWindow, URL webPage) {
	super(mainWindow);
	this.setSize(200, 200);
	this.mainPage = webPage;
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
	    HTMLEditorKit kit = new HTMLEditorKit();
	    editorPanel.setContentType("text/html"); // show only text/html
	    // content type
	    editorPanel.setEditorKit(kit); // associate the editor panel and the
	    // HTML editor
	    editorPanel.setPage(this.mainPage); // show the file
	    editorPanel.setEditable(false); // do not allow the content to be
	    // changed by the user

	    // associate a new link listener
	    editorPanel.addHyperlinkListener(new HyperlinkListener() {
		@Override
		public void hyperlinkUpdate(HyperlinkEvent event) {
		    try {
			if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
			    // change the displayed page
			    editorPanel.setPage(new java.net.URL(event.getURL().toExternalForm()));

		    } catch (Exception ez) {
			AgletsTranslator translator = TahitiBrowser.this.getAgletsTranslator();
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
	    JScrollPane sp = new JScrollPane();
	    sp.getViewport().add(editorPanel);

	    this.getContentPane().add(sp);
	    editorPanel.setVisible(true);
	    /* set the title of the window to the page visualized */
	    this.setTitle(this.getTitle() + " - "
		    + editorPanel.getPage().toExternalForm());
	    this.setVisible(true);
	} catch (Exception ez) {
	    JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
		    + ".URLError.title"), this.translator.translate(this.baseKey
			    + ".URLError"), JOptionPane.ERROR_MESSAGE);
	}

    }

    /**
     * Show the main page of this browser.
     * 
     */
    @Override
    public void run() {
	this.showPage();
    }

}
