/*
 * Created on 23 mars 2004
 *
 =============================================
 GNU LESSER GENERAL PUBLIC LICENSE Version 2.1
 =============================================
 GLIPS Graffiti Editor, a SVG Editor
 Copyright (C) 2003 Jordi SUC, Philippe Gil, SARL ITRIS
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 Contact : jordi.suc@itris.fr; philippe.gil@itris.fr
 
 =============================================
 */
package fr.itris.glips.svgeditor.svgfile;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.zip.*;

import javax.swing.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.resources.*;

/**
 * @author Jordi SUC, Maciej Wojtkiewicz
 * the class that creates the static menu item exit in the menu bar
 */
public class SVGSaveClose extends SVGModuleAdapter{
    
    /**
     * the ids of the module
     */
    final private String	idsaveclose="SaveClose", idsave="Save", idsaveas="SaveAs",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    idclose="Close", idcloseall="CloseAll"; //$NON-NLS-1$ //$NON-NLS-2$
    
    /**
     * the labels
     */
    private String labelsave="", labelsaveas="", labelsaveall="", labelclose="", labelcloseall=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    
    /**
     * labels
     */
    private String 	savecloseconfirmtitle="", savecloseconfirmmessage="", savewarningmessage="", savewarningtitle="", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    closeallwarningtitle="", closeallwarningmessage="", savecloseerasetitle="", savecloseerasemessage=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    
    /**
     * the editor
     */
    private SVGEditor editor;
    
    /**
     * the menu item that will be added to the menubar
     */
    private JMenuItem save=null, saveAs=null, saveAll=null, close=null, closeAll=null;
    
    /**
     * the constructor of the class
     * @param editor the editor
     */
    public SVGSaveClose(SVGEditor theEditor)
    {        
        this.editor = theEditor;
        
        //gets the labels from the resources
        ResourceBundle bundle=SVGEditor.getBundle();
        
        if(bundle!=null){
            
            try{
                labelsave=bundle.getString("labelsave"); //$NON-NLS-1$
                labelsaveas=bundle.getString("labelsaveas"); //$NON-NLS-1$
                labelsaveall=bundle.getString("labelsaveall");				 //$NON-NLS-1$
                labelclose=bundle.getString("labelclose"); //$NON-NLS-1$
                labelcloseall=bundle.getString("labelcloseall"); //$NON-NLS-1$
                savecloseconfirmtitle=bundle.getString("savecloseconfirmtitle"); //$NON-NLS-1$
                savecloseconfirmmessage=bundle.getString("savecloseconfirmmessage"); //$NON-NLS-1$
                savewarningmessage=bundle.getString("savewarningmessage"); //$NON-NLS-1$
                savewarningtitle=bundle.getString("savewarningtitle"); //$NON-NLS-1$
                closeallwarningtitle=bundle.getString("closeallwarningtitle"); //$NON-NLS-1$
                closeallwarningmessage=bundle.getString("closeallwarningmessage"); //$NON-NLS-1$
                savecloseerasetitle=bundle.getString("erasefiletitle"); //$NON-NLS-1$
                savecloseerasemessage=bundle.getString("erasefilemessage"); //$NON-NLS-1$
            }catch (Exception ex){}
        }
        
        //a listener that listens to the changes of the SVGFrames
        final ActionListener svgframeListener=new ActionListener(){
            
            public void actionPerformed(ActionEvent e) {
                
                final SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
                
                if(frame!=null){
                    
                    //enables the menuitems
                    save.setEnabled(true);
                    saveAs.setEnabled(true);
                    saveAll.setEnabled(true);
                    close.setEnabled(true);
                    closeAll.setEnabled(true);
                    
                }else{
                    
                    //disables the menuitems
                    save.setEnabled(false);
                    saveAs.setEnabled(false);
                    saveAll.setEnabled(false);
                    close.setEnabled(false);
                    closeAll.setEnabled(false);
                }
            }	
        };
        
        //adds the SVGFrame change listener
        editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);
        
        //getting the icons
        ImageIcon saveIcon = SVGResource.getIcon("Save", false), //$NON-NLS-1$
        dsaveIcon=SVGResource.getIcon("Save", true), //$NON-NLS-1$
        saveAsIcon=SVGResource.getIcon("SaveAs", false), //$NON-NLS-1$
        dsaveAsIcon=SVGResource.getIcon("SaveAs", true), //$NON-NLS-1$
        closeIcon=SVGResource.getIcon("Close", false), //$NON-NLS-1$
        dcloseIcon=SVGResource.getIcon("Close", true), //$NON-NLS-1$
        closeAllIcon=SVGResource.getIcon("CloseAll", false), //$NON-NLS-1$
        dcloseAllIcon=SVGResource.getIcon("CloseAll", true); //$NON-NLS-1$
        
        //handling the menu items
        save=new JMenuItem(labelsave, saveIcon);
        save.setDisabledIcon(dsaveIcon);
        save.setAccelerator(KeyStroke.getKeyStroke("ctrl S")); //$NON-NLS-1$
        save.setEnabled(false);
        
        saveAs=new JMenuItem(labelsaveas, saveAsIcon);
        saveAs.setDisabledIcon(dsaveAsIcon);
        saveAs.setAccelerator(KeyStroke.getKeyStroke("alt ctrl S")); //$NON-NLS-1$
        saveAs.setEnabled(false);
        
        saveAll=new JMenuItem(labelsaveall);
        saveAll.setEnabled(false);
        
        close=new JMenuItem(labelclose, closeIcon);
        close.setDisabledIcon(dcloseIcon);
        close.setAccelerator(KeyStroke.getKeyStroke("ctrl Q")); //$NON-NLS-1$
        close.setEnabled(false);
        
        closeAll=new JMenuItem(labelcloseall, closeAllIcon);
        closeAll.setEnabled(false);
        closeAll.setDisabledIcon(dcloseAllIcon);
        
        //adds a listener to the menu item
        save.addActionListener(new ActionListener(){
            
            public void actionPerformed(ActionEvent e){
                
                SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
                
                if(frame!=null){
                    
                    saveAction(frame, false, false);
                }
            }
        });
        
        //adds a listener to the menu item
        saveAs.addActionListener(new ActionListener(){
                    
            public void actionPerformed(ActionEvent e){
                
                SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
                
                if(frame!=null){
                    
                    saveAction(frame, true, false);
                }
            }
        });			
        
        close.addActionListener(
                
                new ActionListener(){
                    
                    public void actionPerformed(ActionEvent e){
                        
                        SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
                        
                        if(frame!=null){
                            
                            closeAction(frame);
                        }  
                    }
                }
        );
        
        final String fcloseallwarningtitle=closeallwarningtitle;
        final String fcloseallwarningmessage=closeallwarningmessage;
        
        closeAll.addActionListener(new ActionListener(){
            
            public void actionPerformed(ActionEvent arg0) {
                
                boolean displayDialog=false;
                Collection<SVGFrame> frames=new LinkedList<SVGFrame>(getSVGEditor().getFrameManager().getFrames());
                
                if(frames!=null && frames.size()>0){
                    
                    SVGFrame frame=null;
                    
                    for(Iterator it=frames.iterator(); it.hasNext();){
                        
                        try{
                            frame=(SVGFrame)it.next();
                        }catch (Exception ex){frame=null;}
                        
                        if(frame!=null && frame.isModified()){
                            
                            displayDialog=true;
                            break;
                        }
                    }   
                }
                
                int returnVal=-1;
                
                if(displayDialog){
                    
                    //asks the user if the frames should be deleted
                    returnVal=JOptionPane.showConfirmDialog(    editor.getParent(), 
                            fcloseallwarningmessage, 
                            fcloseallwarningtitle, 
                            JOptionPane.YES_NO_OPTION); 
                }
                
                //closes all the frames
                if(returnVal!=JOptionPane.NO_OPTION) {
                    
                    SVGFrame frame=null;
                    
                    for(Iterator it=frames.iterator(); it.hasNext();){
                        
                        try{
                            frame=(SVGFrame)it.next();
                        }catch (Exception ex){frame=null;}
                        
                        if(frame!=null){
                            
                            final SVGFrame fframe=frame;
                            
                            SwingUtilities.invokeLater(new Runnable() {
                                
                                public void run() {

                                    close(fframe);                                    
                                }
                            });
                        }
                    }   
                }
            }
        });
    }
    
    /**
     * @return the editor
     */
    public SVGEditor getSVGEditor()
    {
        return editor;
    }

    @Override
    public Hashtable<String, JMenuItem> getMenuItems() 
    {
        Hashtable<String, JMenuItem> menuItems=new Hashtable<String, JMenuItem>();
        menuItems.put(idsave,save);
        menuItems.put(idsaveas,saveAs);
        menuItems.put(idclose,close);
        menuItems.put(idcloseall,closeAll);
        
        return menuItems;
    }
    
    /**
     * closes all open frames
     */
    public void closeAllAction()
    {
        this.closeAllAction(null);
    }    
        
    /**
     * closes all open frames, and runs the code provided on successful completion of save
     */
    public void closeAllAction(final Runnable runOnSave)
    {
        final HashSet<SVGFrame> frames=new HashSet<SVGFrame>(getSVGEditor().getFrameManager().getFrames());
        
        SwingUtilities.invokeLater(new Runnable() {
            
            public void run() {
                
                for(SVGFrame frame : frames) {
                    
                    closeActionMethod(frame, runOnSave);
                }
            }
        });
    }
    
    /**
     * closes the frame of the given name
     * @param frame a svg frame
     */
    public void closeAction(final SVGFrame frame)
    {
        SwingUtilities.invokeLater(new Runnable() 
        {            
            public void run() 
            {                
                closeActionMethod(frame, null);
            }
        });
    }
    
    /**
     * the method that closes the given frame
     * @param frame a frame
     */
    protected void closeActionMethod(final SVGFrame frame, final Runnable runOnSave) {
        
        if(frame.isModified())
        {            
            //if the file has been modified, a dialog to confirm the close action is shown
            int returnVal=JOptionPane.showConfirmDialog(    editor.getParent(), 
										                    savecloseconfirmmessage, 
										                    savecloseconfirmtitle, 
										                    JOptionPane.YES_NO_CANCEL_OPTION);
            
            if (returnVal == JOptionPane.YES_OPTION)
            {                
                saveAction(frame, false, true, runOnSave);               
            }
            else if (returnVal == JOptionPane.NO_OPTION)
            {                
                //closes the frame
                close(frame);
            }            
        }
        else 
        {            
            //closes the frame
            close(frame);
        }
    }
    
    /**
     * saves the content of the given frame and closes it if necessary
     * @param frame a svg frame
     * @param isSaveAs whether it is a save as action or not
     * @param isCloseAction 
     */
    public void saveAction(final SVGFrame frame, final boolean isSaveAs, final boolean isCloseAction)
    {
        saveAction(frame,isSaveAs,isCloseAction,null);
    }    
    
    /**
     * saves the content of the given frame and closes it if necessary
     * @param frame a svg frame
     * @param isSaveAs whether it is a save as action or not
     * @param isCloseAction whether or not to close the frame after saving
     * @param runOnSave code to be executed upon successful completion of the save process, 
     *                  or <code>null</code> if nothing required
     */   
    public void saveAction(final SVGFrame frame, final boolean isSaveAs, final boolean isCloseAction, final Runnable runOnSave){

        SwingUtilities.invokeLater(new Runnable() {
            
            public void run() {
 
                boolean allowCloseAction=true;
                
                if(frame.isModified() || isSaveAs){
                    
                    File file=null;
                    
                    try{file=new File(new URI(frame.getName()));}catch (Exception ex){file=null;}
                    
                    if(file!=null && ! file.exists()){
                        
                        file=null;
                    }
                    
                    //if the file has never been saved or a save as action is being done
                    if(file==null || isSaveAs){
                        
                        //opens a file chooser
                        JFileChooser fileChooser=new JFileChooser();
                        
                        if(getSVGEditor().getResource().getCurrentDirectory()!=null){
                            
                            fileChooser.setCurrentDirectory(getSVGEditor().getResource().getCurrentDirectory());
                        }
                        
                        fileChooser.setFileFilter(new SVGFileFilter());
                        fileChooser.setMultiSelectionEnabled(false); 
                        
                        File selectedFile=null;
                        int rVal=-1;
                        int returnVal=-1;
                        
                        do{
                            
                            //showing the file chooser
                            returnVal=fileChooser.showSaveDialog(editor.getParent());

                            if(returnVal==JFileChooser.APPROVE_OPTION){
                                
                                getSVGEditor().getResource().setCurrentDirectory(fileChooser.getCurrentDirectory());
                                selectedFile=fileChooser.getSelectedFile();
                                
                                //normalizes the selected file
                                String uri=selectedFile.toURI().toString();
                                
                                if(	! uri.endsWith(SVGToolkit.SVG_FILE_EXTENSION) && 
                                	! uri.endsWith(SVGToolkit.SVGZ_FILE_EXTENSION)){
                                    
                                	uri=uri.concat(SVGToolkit.SVG_FILE_EXTENSION);
                                }
                                
                                try{
                                	selectedFile=new File(new URI(uri));
                                }catch (Exception ex){}

                                if(selectedFile!=null && selectedFile.exists()){
                                    
                                    //if the file exist prompts a dialog to confirm that the file will be erased
                                    rVal=JOptionPane.showConfirmDialog( 	editor.getParent(), 
													                                            savecloseerasemessage, 
													                                            savecloseerasetitle, 
													                                            JOptionPane.YES_NO_OPTION);
                                    
                                    //if the file should be erased
                                    if(rVal==JOptionPane.YES_OPTION){
                                        
                                        save(frame, selectedFile, runOnSave);
                                        break;
                                    }
                                    
                                }else if(selectedFile!=null){
                                    
                                    //if the file does not already exists
                                    save(frame, selectedFile, runOnSave);
                                    break;
                                }
                                
                            }else{
                                
                                //the cancel button from the button chooser has been clicked, therefore, 
                                //nothing is done
                                allowCloseAction=false;
                                
                                break;
                            }
                            
                        }while(selectedFile!=null && selectedFile.exists());
                        
                    }else{
                        
                        //if the file has already been saved or has been open before its changes, it saves the file
                        save(frame, file, runOnSave);
                    }
                }
                
                if(isCloseAction && allowCloseAction) {
                    
                    close(frame);
                }
            }
        });
    }
    
    /**
     * saves the SVG picture to a SVG file given a URI
     * @param frame the related svg frame
     * @param newFile the file of the next save action
     */
    protected void save(final SVGFrame frame, final File newFile, final Runnable runOnSave){
        
        if(frame!=null && newFile!=null){
            
            String newSVGPath=newFile.toURI().toString();
            
            if(newSVGPath!=null){
                
                final Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
                final String fnewSVGPath=newSVGPath;
                
                if(doc!=null){
                    
                    try
                    {
                        Runnable runnable=new Runnable()
                        {                            
                            public void run(){
                                
                                try
                                {
                                	getSVGEditor().getSVGToolkit().removeUselessAttributes(doc.getDocumentElement());
                                	printXML(editor.getParent(), doc, new File(new URI(fnewSVGPath)), fnewSVGPath, runOnSave, frame);
                                }
                                catch(Exception ex)
                                {
                                    throw new RuntimeException("Error whilst saving frame", ex); //$NON-NLS-1$
                                }   
                            }
                        };
                        
                        frame.enqueue(runnable);
                        
                    }catch (Exception ex){
                        
                        JOptionPane.showMessageDialog(editor.getParent(), savewarningmessage, savewarningtitle, JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    //refreshes the name of the SVGFrame
                    if(frame.getName()!=null && newSVGPath!=null && ! frame.getName().equals(newSVGPath)){
                        
                        getSVGEditor().getFrameManager().changeName(frame.getName(), newSVGPath);
                        
                        //adding the file name to the list of the recent files
                        getSVGEditor().getResource().addRecentFile(newSVGPath);

                        //re-building the open recent menu
                        Object obj=getSVGEditor().getSVGModuleLoader().getModule("NewOpen"); //$NON-NLS-1$
                        
                        if(obj!=null){
                            
                            try{
                                obj.getClass().getMethod("buildOpenRecentMenu", (Class[])null).invoke(obj, (Object[])null); //$NON-NLS-1$
                            }catch (Exception ex){}
                        }
                    }
                    
                    frame.setModified(false);
                }			
            }
        }
    }
    
    /**
     * prints the given document into the given file
     * @param parentContainer
     * @param doc
     * @param file
     * @param newSVGPath
     */
    protected void printXML(Container parentContainer, final Document doc, final File file, String newSVGPath, final Runnable runOnSave, final SVGFrame frame){
    	
        File newPath=null;
        
        if(newSVGPath!=null && ! newSVGPath.equals("")){ //$NON-NLS-1$
            
            try{
                newPath=new File(new URI(newSVGPath));
            }catch (Exception ex){}
        }

        if(newPath!=null){
        	
            //creating the dialog displaying a progress bar
            SVGProgressBarDialog pb=null;
            
            if(parentContainer instanceof JFrame){
            	
            	pb=new SVGProgressBarDialog((JFrame)parentContainer, labelsave);
            	
            }else{
            	
            	pb=new SVGProgressBarDialog(new JFrame(""), labelsave); //$NON-NLS-1$
            }
            
            final SVGProgressBarDialog progressBarDialog=pb;
            progressBarDialog.setVisible(true);

            progressBarDialog.setIndeterminate(false, true);
            //getting the number of nodes in the tree
            int nodesNumber=0;
            for(NodeIterator it=new NodeIterator(doc.getDocumentElement()); it.hasNext();){
            	
            	it.next();
            	nodesNumber++;
            }
            
            progressBarDialog.setMax(nodesNumber);
            
            Thread thread=new Thread(){
            	
            	@Override
            	public void run() {

                    //writing the writer
                    try
                    {
                    	
                    	boolean isSVGFile=file.getName().endsWith(SVGToolkit.SVG_FILE_EXTENSION);
                    	
                    	//converts the svg document into xml strings
                        StringBuffer buffer=new StringBuffer(""); //$NON-NLS-1$

                        for (Node node=doc.getFirstChild(); node != null; node=node.getNextSibling()) {
                        	
                        	writeNode(node, buffer, progressBarDialog, 0, isSVGFile);
                        }
                    	
                		ByteBuffer byteBuffer=ByteBuffer.wrap(buffer.toString().getBytes("UTF-8")); //$NON-NLS-1$
                        FileOutputStream out=new FileOutputStream(file);
                        
                        if(! isSVGFile){
                        	
                        	//compressing the svg content
                        	GZIPOutputStream zout=new GZIPOutputStream(out);
                        	
                        	zout.write(byteBuffer.array());
                        	zout.flush();
                        	zout.close();
                        	
                        }else{
                        	
                        	//writing the svg content without compression
                            FileChannel channel=out.getChannel();
                            channel.write(byteBuffer);
                            channel.close();
                        }
                        
                        out.close();
                        
                    }                    
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                    finally
                    {
                        if (runOnSave != null) 
                        { 
                            frame.enqueue(runOnSave); 
                        }
                    }
                    
                    progressBarDialog.setVisible(false);
            	}
            };
            
            thread.start();
        }
    }

    /**
     * writes the string representation of the given node in the given string buffer
     * @param node a node
     * @param buffer the string buffer
     * @param progressBarDialog the progress bar dialog used to show the save action progress
     * @param indent the indent
     * @param format whether the xml should be formatted
     */
    public static void writeNode(Node node, StringBuffer buffer, final SVGProgressBarDialog progressBarDialog, 
    												int indent, boolean format){
    	
    	if(node!=null){

            switch (node.getNodeType()) {
            
            case Node.ELEMENT_NODE:
            	
                buffer.append("<"); //$NON-NLS-1$
                buffer.append(node.getNodeName());

                if (node.hasAttributes()) {
                	
                    NamedNodeMap attr = node.getAttributes();
                    int len = attr.getLength();
                    
                    for (int i = 0; i < len; i++) {
                    	
                        Attr a = (Attr)attr.item(i);
                        buffer.append(" "); //$NON-NLS-1$
                        buffer.append(a.getNodeName());
                        buffer.append("=\""); //$NON-NLS-1$
                        buffer.append(contentToString(a.getNodeValue()));
                        buffer.append("\""); //$NON-NLS-1$
                    }
                }

                Node c = node.getFirstChild();
                
                if (c != null) {
                	
                    buffer.append(">"); //$NON-NLS-1$
                    
                    if(format){
                    	// this was putting a new line character within the text tag
                        // this is not desirable
                    	//buffer.append("\n");
                    }
                    
                    for (; c != null; c = c.getNextSibling()) {
                    	
                        writeNode(c, buffer, progressBarDialog, indent+1, format);
                    }

                    buffer.append("</"); //$NON-NLS-1$
                    buffer.append(node.getNodeName());
                    buffer.append(">"); //$NON-NLS-1$
                    
                } else {
                	
                    buffer.append("/>"); //$NON-NLS-1$
                }
                
                if(format){
                	
                	buffer.append("\n"); //$NON-NLS-1$
                }
                
                break;
                
            case Node.TEXT_NODE:
            	
                buffer.append(contentToString(node.getNodeValue()));
                break;
                
            case Node.CDATA_SECTION_NODE:
            	
                buffer.append("<![CDATA["); //$NON-NLS-1$
                buffer.append(node.getNodeValue());
                buffer.append("]]>"); //$NON-NLS-1$
                break;
                
            case Node.ENTITY_REFERENCE_NODE:
            	
                buffer.append("&"); //$NON-NLS-1$
                buffer.append(node.getNodeName());
                buffer.append(";"); //$NON-NLS-1$
                break;
                
            case Node.PROCESSING_INSTRUCTION_NODE:
            	
                buffer.append("<?"); //$NON-NLS-1$
                buffer.append(node.getNodeName());
                buffer.append(" "); //$NON-NLS-1$
                buffer.append(node.getNodeValue());
                buffer.append("?>"); //$NON-NLS-1$
                break;
                
            case Node.COMMENT_NODE:
            	
                buffer.append("<!--"); //$NON-NLS-1$
                buffer.append(node.getNodeValue());
                buffer.append("-->"); //$NON-NLS-1$
                break;
                
            case Node.DOCUMENT_TYPE_NODE: 
            	
                DocumentType dt = (DocumentType)node;
                buffer.append ("<!DOCTYPE ");  //$NON-NLS-1$
                buffer.append (node.getOwnerDocument().getDocumentElement().getNodeName());
                String pubID = dt.getPublicId();
                
                if (pubID != null) {
                	
                    buffer.append (" PUBLIC \"" + dt.getNodeName() + "\" \"" + //$NON-NLS-1$ //$NON-NLS-2$
                               pubID + "\">"); //$NON-NLS-1$
                    
                } else {
                	
                    String sysID = dt.getSystemId();
                    
                    if (sysID != null){
                    	
                        buffer.append (" SYSTEM \"" + sysID + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }

                break;
            }
            
            SwingUtilities.invokeLater(new Runnable(){
            	
            	public void run() {

                    progressBarDialog.incrementProgressBarValue();
            	}
            });
    	}
    }
    
    /**
     * @param s the string to be modified
     * @return the given content value transformed to replace invalid
     * characters with entities.
     */
    public static String contentToString(String s) {
    	
        StringBuffer result = new StringBuffer();
        
        s=s.replaceAll("\\n+", ""); //$NON-NLS-1$ //$NON-NLS-2$
        s=s.replaceAll("\\r+", ""); //$NON-NLS-1$ //$NON-NLS-2$
        s=s.replaceAll("\\t+", ""); //$NON-NLS-1$ //$NON-NLS-2$

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            switch (c) {
            case '<':
                result.append("&lt;"); //$NON-NLS-1$
                break;
            case '>':
                result.append("&gt;"); //$NON-NLS-1$
                break;
            case '&':
                result.append("&amp;"); //$NON-NLS-1$
                break;
            case '"':
                result.append("&quot;"); //$NON-NLS-1$
                break;
            case '\'':
                result.append("&apos;"); //$NON-NLS-1$
                break;
            default:
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    /**
     * closes the current SVG picture
     * @param frame a frame
     */
    public void close(SVGFrame frame)
    {       
        if (frame != null)
        {           
            getSVGEditor().getFrameManager().removeFrame(frame.getName());
            if (getSVGEditor().getParent() instanceof JFrame)
            {
                ((JFrame)getSVGEditor().getParent()).dispose();
            }
        }
    }
    
    /**
     * gets the module's name
     * @return the module's name
     */
    public String getName()
    {
        return idsaveclose;
    }
    
}
