/*
 * Created on 26 août 2004
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
package fr.itris.glips.svgeditor.visualresources;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.menutool.*;
import fr.itris.glips.svgeditor.resources.*;

/**
 * the class used to manipulate svg resources like gradients, patterns and markers
 * @author Jordi SUC
 */
public class SVGVisualResources extends SVGModuleAdapter{

    /**
     * the ids of the module
     */
    final private String idvisualresources="VisualResources"; //$NON-NLS-1$
    
    /**
     * the labels
     */
    protected String labelresources=""; //$NON-NLS-1$
    
    /**
     * the undo/redo labels
     */
    protected String undoredoresources="", undoredoresourcesnew="", undoredoresourcesremove=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
    /**
     * the editor
     */
    private SVGEditor editor=null;
    
    /**
     * the document used for the resources
     */
    private Document docResources=null;
    
    /**
     * the document storing the visual resources
     */
    private Document visualResourceStore=null;

    /**
     * the font
     */
    private final Font theFont=new Font("theFont", Font.ROMAN_BASELINE, 10); //$NON-NLS-1$
    
    /**
     * the bundle used to get labels
     */
    private ResourceBundle bundle=SVGEditor.getBundle();
    
    /**
     * used to convert numbers into a string
     */
    private DecimalFormat format;

	/**
	 * the panel in which the widget panel will be inserted
	 */
	private JPanel visualResourcesPanel=new JPanel();
    
    /**
     * the panel displaying the lists of the resources
     */
    private SVGVisualResourceListsPanel listsPanel=null;
    
    /**
     * the map associating a frame to a resource state object
     */
    private Hashtable stateMap=new Hashtable();
    
    /**
     * the toolkit of this module
     */
    private SVGVisualResourceToolkit visualResourcesToolkit=null;
    
	/**
	 * the bounds of the tool frame
	 */
	private Rectangle frameBounds=null;
	
	/**
	 * the frame into which the resources panel will be inserted
	 */
	private SVGToolFrame visualResourcesFrame;
    
    /**
     * the constructor of the class
     * @param editor the editor
     */
    public SVGVisualResources(SVGEditor editor){

        this.editor=editor;
        this.visualResourcesToolkit=new SVGVisualResourceToolkit(this);
        
        if(bundle!=null){
            
            try{
                labelresources=bundle.getString("label_visualresources"); //$NON-NLS-1$
                undoredoresources=bundle.getString("undoredoresources"); //$NON-NLS-1$
                undoredoresourcesnew=bundle.getString("undoredoresourcesnew"); //$NON-NLS-1$
                undoredoresourcesremove=bundle.getString("undoredoresourcesremove"); //$NON-NLS-1$
            }catch (Exception ex){}
        }
        
        //sets the format used to convert numbers into a string
        DecimalFormatSymbols symbols=new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        format=new DecimalFormat("######.#",symbols); //$NON-NLS-1$
        
        //a listener that listens to the changes of the SVGFrames
        final ActionListener svgframeListener=new ActionListener(){

            public void actionPerformed(ActionEvent e) {
            	
            	//removes all the components of the panel of the resources
            	visualResourcesPanel.removeAll();
            	
                if(listsPanel!=null){
                    
                	listsPanel.dispose();
                	listsPanel=null;
                }
                
                final SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
                Collection frames=getSVGEditor().getFrameManager().getFrames();

                //checks the defs map and the resource state map consistency
                SVGFrame f=null;
                
                for(Iterator it=new LinkedList(stateMap.keySet()).iterator(); it.hasNext();){
                    
                    try{
                        f=(SVGFrame)it.next();
                    }catch (Exception ex){f=null;}
                    
                    if(f!=null && ! frames.contains(f)){
    
                        stateMap.remove(f);
                    }
                }

                //if a new frame has been created, the defs element is added, and a new resource state object is added to the stateMap
                if(frame!=null && ! stateMap.containsKey(frame)){
                    
                    SVGVisualResourceState resourceState=new SVGVisualResourceState();
                    stateMap.put(frame, resourceState);
                }
                
                if(visualResourcesPanel.isVisible() || frame==null){

                    handleVisualResources(frame);
                }
            }
        };
        
        //adds the SVGFrame change listener
        editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);
		
		//setting the layout for the visual resources panel
		visualResourcesPanel.setLayout(new BoxLayout(visualResourcesPanel, BoxLayout.Y_AXIS));
		
		//getting the preferred bounds
		frameBounds=editor.getPreferredWidgetBounds("visualresources"); //$NON-NLS-1$
		
		//creating the internal frame containing the properties panel
		visualResourcesFrame=new SVGToolFrame(editor, idvisualresources, labelresources, visualResourcesPanel);
		
		//setting the visibility changes handler
		Runnable visibilityRunnable=new Runnable(){
			
			public void run() {
				
    			if(getSVGEditor().getFrameManager().getCurrentFrame()!=null){
    			    
    				handleVisualResources(getSVGEditor().getFrameManager().getCurrentFrame());
    				
    			}else{
    			    
    				handleVisualResources(null);
    			}
			}
		};
		
		this.visualResourcesFrame.setVisibilityChangedRunnable(visibilityRunnable);

        //loading the documents
        docResources=SVGResource.getXMLDocument("visualResources.xml"); //$NON-NLS-1$
    }
    
    /**
     * @return the editor
     */
    public SVGEditor getSVGEditor(){
        return editor;
    }
    
    /**
     * @return a map associating a menu item id to its menu item object
     */
    public Hashtable getMenuItems(){

        Hashtable menuItems=new Hashtable();
        
        menuItems.put("ToolFrame_"+this.idvisualresources, visualResourcesFrame.getMenuItem()); //$NON-NLS-1$
        
        return menuItems;
    }
    
	@Override
	public SVGToolFrame getToolFrame() {
		return visualResourcesFrame;
	}

    /**
     * @return Returns the bundle.
     */
    protected ResourceBundle getBundle() {
        return bundle;
    }
    
    /**
     * @return Returns the format.
     */
    protected DecimalFormat getFormat() {
        return format;
    }

    /**
     * @return Returns the visualResourcesToolkit.
     */
    protected SVGVisualResourceToolkit getVisualResourcesToolkit() {
        return visualResourcesToolkit;
    }
    
    /**
     * returns the defs element corresponding to the given frame
     * @param frame a frame
     * @return the defs element corresponding to the given frame
     */
    protected Element getDefs(SVGFrame frame){
        
        Element defs=null;
        
        if(frame!=null){
            
            try{
                
                defs=frame.getDefsElement();
            }catch(Exception ex){defs=null;}
        }

        return defs;
    }

    /**
     * @return Returns the docResources.
     */
    protected Document getDocResources() {
        return docResources;
    }
    
    /**
     * @return Returns the visualResourceStore.
     */
    protected Document getVisualResourceStore() {
        return visualResourceStore;
    }
    
    /**
     * @return Returns the listState
     * @param frame a frame
     */
    protected SVGVisualResourceState getResourceState(SVGFrame frame) {
    	
    	SVGVisualResourceState resourceState=null;
    	
    	if(frame!=null){
    		
    		resourceState=(SVGVisualResourceState)stateMap.get(frame);
    	}
    	
        return resourceState;
    }
    
    /**
     * manages the display of the resource panel
     * @param frame the current frame
     */
    protected void handleVisualResources(SVGFrame frame){
    	
    	//removes all the components of the panel of the resources
    	visualResourcesPanel.removeAll();
    	
        if(listsPanel!=null){
            
        	listsPanel.dispose();
        	listsPanel=null;
        }
    	
        if(frame!=null){
            
            //getting the resource store
            visualResourceStore=editor.getResource().getResourceStore();
            
            //creates the list of the visual resource items
            LinkedList models=getVisualResourceModels(frame);

            //creates the panel
            if(models!=null){

                listsPanel=new SVGVisualResourceListsPanel(this, models);
            }
            
            //adds the resource panel into the container and displays it
            visualResourcesPanel.add(listsPanel);
    		visualResourcesPanel.setPreferredSize(new Dimension(frameBounds.width, frameBounds.height));
		    visualResourcesFrame.revalidate();
            
            return;
        }
        
        if(bundle!=null){
            
            //initializes the value of the last selected tab
            String message=""; //$NON-NLS-1$
            
            try{
                message=bundle.getString("visualresources_empty_dialog_noframe"); //$NON-NLS-1$
            }catch (Exception ex){}

            JLabel label=new JLabel(message);
            label.setBorder(new EmptyBorder(5, 5, 5, 5));
            label.setFont(theFont);

            visualResourcesPanel.add(label);
            visualResourcesPanel.setPreferredSize(null);
        }
        
	    visualResourcesFrame.revalidate();
    }
    
    /**
     * creates the resource items
     * @param frame the current frame
     * @return the list of the resource items
     */
    protected LinkedList getVisualResourceModels(SVGFrame frame){
        
        LinkedList items=new LinkedList();
        
        if(frame!=null && docResources!=null){
            
            Element root=docResources.getDocumentElement();
            
            if(root!=null){

                //creates the visual resource items
                SVGVisualResourceModel model=null;
                
                for(Node cur=root.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
                    
                    if(cur instanceof Element){
                        
                        model=new SVGVisualResourceModel(this, (Element)cur);
                        items.add(model);
                    }
                }
            }
        }
        
        return items;
    }
    
    /**
     * transforms a string taken from the xml document into the accurate string
     * @param value a string
     * @return the absolute string
     */
    protected String getAbsoluteString(String value){
        
        String str=new String(value);
        str="vresource_".concat(str); //$NON-NLS-1$
        
        return str;
    }
    
    /**
     * transforms a string taken from the xml document into the accurate string
     * @param value a string
     * @return the normalized string
     */
    protected String getNormalizedString(String value){
        
        String str=new String(value);
        
        if(value.length()>10){
            
            str=str.substring(10, str.length());
        }
        
        return str;
    }
}
