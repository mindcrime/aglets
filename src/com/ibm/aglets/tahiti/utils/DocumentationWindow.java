/*
 * Created on Oct 5, 2004
 *
 * @author Luca Ferrari, <a href="mailto:cat4hire@users.sourceforge.net">cat4hire@users.sourceforge.net</a>
 */
package com.ibm.aglets.tahiti.utils;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.ResourceBundle;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.event.*;


/**
 * A window that is able to show both text and html files, and can be used to show the javadoc documentation
 * or web pages available on internet.
 */
public class DocumentationWindow extends JFrame implements Runnable {
    // The resource bundle component
    static ResourceBundle bundle = null;
    // loading resources from the bundle
    static {
        bundle = ResourceBundle.getBundle("tahiti");
    }
    
    
    
    /**
     * The file to read
     */
    protected URL page=null;

    /**
     * Default constructor. It try to load the index.html file from
     * the docs default directory specified into the properties. If no directory
     * is founded then it ask to the user.
     * @param width the dimension of the window
     * @param height the dimension
     * @param fileToRead a file to display. It can be a text file or an html
     * file. If null the reader will work as a javadoc reader, else
     * it will display the asked file.
     * @param title the title of the window
     */
    public DocumentationWindow(int width,int height,File fileToRead, String title)
    {
        super(title);

        // set the icon of this window
        this.setIconImage(IconRepository.getImage("documentation"));
        
        // set the size of the window
        this.setSize(width,height);
        
        try{
            
        
	        // if the file to read is null, ask the user which file to open
	        if(fileToRead==null || fileToRead.exists()==false){
	    		JFileChooser chooser=new JFileChooser();
	    		int response=chooser.showOpenDialog(this);
	
	    		if(response==JFileChooser.CANCEL_OPTION){
	    		    this.setVisible(false);
	    		    return;
	    		}
	    		else	{
	    		    this.page=chooser.getSelectedFile().toURL();
	    		}
	            
	        }
	        else{
	            this.page=fileToRead.toURL();
	        }
        
        }catch(Exception e){
            JOptionPane.showMessageDialog(this,bundle.getString("documentationwindow.error.message"),bundle.getString("documentationwindow.error.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("error"));
            e.printStackTrace();
            this.setVisible(false);
            return;
        }

		Thread t=new Thread(this,"Documentation Thread");
		t.start();

    }

    
    
    
    /**
     * Default constructor. It try to load the index.html file from
     * the docs default directory specified into the properties. If no directory
     * is founded then it ask to the user.
     * @param width the dimension of the window
     * @param height the dimension
     * @param url the page to display. It can be a text file or an html
     * file. If null the reader will work as a javadoc reader, else
     * it will display the asked file.
     * @param title the title of the window
     */
    public DocumentationWindow(int width,int height,URL url, String title)
    {
        super(title);

        // set the icon of this window
        this.setIconImage(IconRepository.getImage("documentation"));
        
        // set the size of the window
        this.setSize(width,height);
        
        try{
            
        
	        // if the file to read is null, ask the user which file to open
	        if(url==null ){
	    		JFileChooser chooser=new JFileChooser();
	    		int response=chooser.showOpenDialog(this);
	
	    		if(response==JFileChooser.CANCEL_OPTION){
	    		    this.setVisible(false);
	    		    return;
	    		}
	    		else	{
	    		    this.page=chooser.getSelectedFile().toURL();
	    		}
	            
	        }
	        else{
	            this.page=url;
	        }
        
        }catch(Exception e){
            JOptionPane.showMessageDialog(this,bundle.getString("documentationwindow.error.message"),bundle.getString("documentationwindow.error.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("error"));
            e.printStackTrace();
            this.setVisible(false);
            return;
        }

		Thread t=new Thread(this,"Documentation Thread");
		t.start();

    }
    
    
    
    /**
     * Method to read a page
     */
     public void run()
     {
         /* check if there is a file to read */
		if(this.page==null)
		{
		    JOptionPane.showMessageDialog(this,bundle.getString("documentationwindow.error.null"),bundle.getString("documentationwindow.error.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("error"));
		    return;
		}

	  try
	  {

	    /* declare the text editor final so I can use the hyperlink defined
	    into this code */
	    final JEditorPane e=new JEditorPane();
	    HTMLEditorKit kit=new HTMLEditorKit();
	    e.setContentType("text/html");
	    e.setEditorKit(kit);

	    e.setPage(this.page);
	    e.setEditable(false);

	    /* add a listener for linked pages */
	    e.addHyperlinkListener(
				    new  HyperlinkListener()
					{
					    public void hyperlinkUpdate(HyperlinkEvent event) {
							try{
							    if(event.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
								/* change the page */
								e.setPage(new java.net.URL(event.getURL().toExternalForm()));
							    }
							}
							catch(Exception ez){
							    JOptionPane.showMessageDialog(null,bundle.getString("documentationwindow.error.pagechange"),bundle.getString("documentationwindow.error.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("error"));
							    ez.printStackTrace();
							}
					    }
					}

				    );



	    JScrollPane sp=new JScrollPane();
	    sp.getViewport().add(e);

	    this.getContentPane().add(sp);
	    e.setVisible(true);
	    /* set the title of the window to the page visualized */
	    this.setTitle(this.getTitle()+" - "+e.getPage().toExternalForm());
	    this.show();
	   }
	   catch(Exception ez) {
		    JOptionPane.showMessageDialog(null,bundle.getString("documentationwindow.error.pagechange"),bundle.getString("documentationwindow.error.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("error"));
		    ez.printStackTrace();
	   }

     }

}