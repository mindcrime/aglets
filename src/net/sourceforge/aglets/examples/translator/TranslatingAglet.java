package net.sourceforge.aglets.examples.translator;

import java.util.Enumeration;

import net.sourceforge.aglets.util.AgletsTranslator;

import com.ibm.aglet.Aglet;

public class TranslatingAglet extends Aglet {

    /**
     * 
     */
    private static final long serialVersionUID = -6400128254712042051L;

    @Override
    public void run() {
	System.out.println("Hello, I'm a translating agent...");
	System.out.println("let me see what translator I've got....");

	// get the translator
	AgletsTranslator translator = this.getTranslator(true);

	if (translator != null) {
	    System.out.println("My translator has been loaded, check it:");
	    System.out.println("\tBasename:" + translator.getResourceBaseName());
	    System.out.println("\tLocale:" + translator.getLocale());
	} else
	    System.out.println("Problem! My translator has not been loaded!");

	System.out.println("Let me show you which translator keys and values I've got...");
	Enumeration<String> keys = translator.getKeys();
	while ((keys != null) && keys.hasMoreElements()) {
	    String currentKey = keys.nextElement();
	    System.out.println("Translation for the key [" + currentKey + "]:"
		    + translator.translate(currentKey));
	}
    }
}
