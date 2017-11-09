/*
 * Created on 13 janv. 2005
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
package fr.itris.glips.svgeditor.menutool;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.resources.*;
import fr.itris.glips.svgeditor.selection.*;

/**
 * the class handling the popups that appear when clicking on a node
 * 
 * @author Jordi SUC
 */
public class SVGPopup {

	/**
	 * the editor
	 */
	private SVGEditor editor;
	
	/**
	 * the id of the module
	 */
	private final String idtoolbar="Popup"; //$NON-NLS-1$
	
	/**
	 * the map (the description of the popup menu) associating the name of a group of popup items to the list of the popup items names
	 */
	private final LinkedHashMap popupItemsDescription=new LinkedHashMap();
	
	/**
	 * the map associating the id of a popup item to this popup item
	 */
	private final HashMap popupItems=new HashMap();
	
	/**
	 * the popup menu
	 */
	private JPopupMenu popupMenu=new JPopupMenu();
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGPopup(SVGEditor editor){
	    
		this.editor=editor;
		
		//adding a frame listener
		editor.getFrameManager().addSVGFrameChangedListener(new ActionListener(){

	        public void actionPerformed(ActionEvent evt) {
	            
	            SVGPopupItem popupItem=null;
	            
	            //restores the initial state of each popup item
	            for(Iterator it=popupItems.values().iterator(); it.hasNext();){
	            	
	            	popupItem=(SVGPopupItem)it.next();
	            	
	            	if(popupItem!=null){
	            		
	            		popupItem.setToInitialState();
	            	}
	            }
	        }
		});
		
		//creating the listener to the popup menu
		popupMenu.addPopupMenuListener(new PopupMenuListener(){

			public void popupMenuCanceled(PopupMenuEvent evt) {

				hidePopup();
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
			}
		});

		//gets the order of the popup items from a xml file
		parseXMLPopupMenuItems();
		
		//gets the popup items from each module
		retrieveModulePopupItems();
	}
	
	/**
	 * @return editor the editor
	 */
	public SVGEditor getSVGEditor(){
		return editor;
	}
	
	/**
	 * retrieves all the popup items from the modules
	 */
	protected void retrieveModulePopupItems(){

		//getting all the loaded modules
		Collection modules=getSVGEditor().getSVGModuleLoader().getModules();
		
		SVGModule module=null;
		Collection itemsList=null;
		SVGPopupItem item=null;
		Iterator it, it2;
		
		//for each module, gets the popup items linked with the given list of nodes
		for(it=modules.iterator(); it.hasNext();){

			module=(SVGModule)it.next();
			
			if(module!=null){
			    
				//getting the list of the popup items of each module
				itemsList=module.getPopupItems();

			    if(itemsList!=null){
			    	
			    	for(it2=itemsList.iterator(); it2.hasNext();){
			    		
			    		item=(SVGPopupItem)it2.next();
			    		
			    		if(item!=null){
			    			
			    			popupItems.put(item.getId(), item);
			    		}
			    	}
			    }
			}
		}
	}

	/**
	 * shows a popup on the given frame at the given location
	 * @param frame a frame
	 * @param location the location of the mouse click wher the popup should appear
	 */
	public void showPopup(SVGFrame frame, Point location){
	    
	    if(frame!=null && location!=null && popupMenu!=null){

	        SVGSelection selection=getSVGEditor().getSVGSelection();
	        
	        if(selection!=null){

	            LinkedList selectedNodes=selection.getCurrentSelection(frame);

	            if(popupItems!=null && popupItems.size()>0){

                    JMenuItem menuItem=null;
                    SVGPopupItem popupItem=null;
                    String itemName=""; //$NON-NLS-1$
                    Collection itemNames=null;
                    boolean hasAddedAnItem=false;
                    boolean isGroupEnabled=false;
                    LinkedList popupItemsToAdd=null;
                    Iterator it2=null;
                    
                    //for each group of menu items, fills the pop up menu and put popup separators between each group of them
                    for(Iterator it=popupItemsDescription.values().iterator(); it.hasNext();){
                    	
                        try{
                            itemNames=(Collection)it.next();
                        }catch (Exception ex){itemNames=null;}
                        
                        if(itemNames!=null){
                            
                            hasAddedAnItem=false;
                            isGroupEnabled=false;
                            popupItemsToAdd=new LinkedList();
                            
                            for(it2=itemNames.iterator(); it2.hasNext();){
                                
                                try{
                                    itemName=(String)it2.next();
                                }catch (Exception ex){itemName=null;}
                                
                                //if the name of the menuitem of the popup model is contained 
                                //in the list of the names of the menuitems that should be displayed
                                if(itemName!=null && popupItems.containsKey(itemName)){
                                	
                                	popupItem=(SVGPopupItem)popupItems.get(itemName);
                                	
                                	if(popupItem!=null){
                                		
                                		menuItem=popupItem.getPopupItem(selectedNodes);
                                		
                                        if(menuItem!=null){
                                            
                                            //the menu item is added to a list
    	                                    popupItemsToAdd.add(menuItem);
    	                                    hasAddedAnItem=true;
    	                                    isGroupEnabled=isGroupEnabled || popupItem.isEnabled();
                                        }
                                	}
                                }
                            }
                            
                            if(isGroupEnabled){
                            	
                                for(it2=popupItemsToAdd.iterator(); it2.hasNext();){
                                    
                                    try{
                                        menuItem=(JMenuItem)it2.next();
                                    }catch (Exception ex){menuItem=null;}
                                    
                                    if(menuItem!=null){
                                    	
                                    	//the menu item is added to the popup menu
                                    	popupMenu.add(menuItem);
                                    }
                                }
                                
                                popupItemsToAdd.clear();
                                
                                if(hasAddedAnItem && it.hasNext()){
                                    
                                    //if other groups of menu items could be added and if menu items 
                                    //from the previous group have been added, a new separator is added
                                    popupMenu.addSeparator();
                                }
                            }
                        }
                    }
                    
                    popupMenu.show(frame.getScrollPane().getSVGCanvas(), location.x, location.y);
	            }
	        }
	    }
	}
	
	/**
	 * hides the popup
	 */
	public void hidePopup(){
		
        popupMenu.removeAll();
        restorePopupItemsInitialState();
	}
	
	/**
	 * restores the initial state of all the popup items
	 */
	protected void restorePopupItemsInitialState(){
		
		SVGPopupItem item=null;
		
    	for(Iterator it=popupItems.values().iterator(); it.hasNext();){
    		
    		item=(SVGPopupItem)it.next();
    		
    		if(item!=null){
    			
    			item.setToInitialState();
    		}
    	}
	}
	
	/**
	 *parses the document to get the menu items specified in the "popup.xml" file
	 */
	protected void parseXMLPopupMenuItems(){

		Document doc=SVGResource.getXMLDocument("popup.xml"); //$NON-NLS-1$
		
		if(doc!=null){
		    
		    Element root=doc.getDocumentElement();
		    
		    if(root!=null){
		        
				String groupName="", itemName=""; //$NON-NLS-1$ //$NON-NLS-2$
				LinkedList itemList=null;
				Node item=null;
				
				//for each group of menu items
				for(Node current=root.getFirstChild(); current!=null; current=current.getNextSibling()){
					
					if(current instanceof Element && current.getNodeName().equals("group")){ //$NON-NLS-1$

						groupName=((Element)current).getAttribute("name"); //$NON-NLS-1$
							
						if(groupName!=null && ! groupName.equals("")){ //$NON-NLS-1$
							
						    itemList=new LinkedList();

						    //for each menuitem, gets its name
						    for(item=current.getFirstChild(); item!=null; item=item.getNextSibling()){
						        
						    	if(item instanceof Element){
						    		
							        itemName=((Element)item).getAttribute("name"); //$NON-NLS-1$
							        
							        if(itemName!=null && ! itemName.equals("")){ //$NON-NLS-1$
							            
							            itemList.add(itemName);
							        }
						    	}
						    }
						    
						    if(itemList.size()>0){
						        
						    	popupItemsDescription.put(groupName, itemList);
						    }
						}
					}
				}
		    }
		}
	}
	
}
