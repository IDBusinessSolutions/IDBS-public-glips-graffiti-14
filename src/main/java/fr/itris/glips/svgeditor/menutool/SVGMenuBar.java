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
package fr.itris.glips.svgeditor.menutool;

import java.util.*;

import javax.swing.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.resources.*;

/**
 * @author Jordi SUC
 * the class of the menu bar
 */
public class SVGMenuBar extends JMenuBar {

	/**
	 * the editor
	 */
	private SVGEditor editor;
	
	/**
	 * the document describing the menu bar
	 */
	private Document docMenu=null;
	
	/**
	 * the map associating the name of a menu to this menu
	 */
	private LinkedHashMap menus=new LinkedHashMap();
	
	/**
	 * the Hashtable associating a name to a JMenuItem
	 */
	private LinkedHashMap menuItems=new LinkedHashMap();
	
	/**
	 * the hashtable associating the name of a menu to a list of menu items
	 */
	private LinkedHashMap unknownMenuItems=new LinkedHashMap();
	
	/**
	 * the bundle
	 */
	private ResourceBundle bundle=null;

	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGMenuBar(SVGEditor editor) {
		
		super();
		this.editor=editor;
		
		//gets the bundle
        bundle=SVGEditor.getBundle();
	}
	
	/**
	 * initializes the menu bar
	 */
	public void init(){
		
		docMenu=SVGResource.getXMLDocument("menu.xml");		 //$NON-NLS-1$
		parseXMLMenu();
		findModuleMenuItems();
		build();
	}
	
	/**
	 * @return editor the editor
	 */
	public SVGEditor getSVGEditor(){
		
		return editor;
	}
	
	/**
	 * gets all the menu items from the modules
	 */
	protected void findModuleMenuItems(){
		
		Collection modules=getSVGEditor().getSVGModuleLoader().getModules();
		SVGModule module=null;
		
		for(Iterator it=modules.iterator(); it.hasNext();){
		    
			try{
				module=(SVGModule)it.next();
				
				if(module!=null && module.getMenuItems()!=null){
				    
				    menuItems.putAll(module.getMenuItems());
				}
			}catch (Exception ex){}
		}
	}
	
	/**
	 * adds a menu item to the list of menu items
	 * @param id the id of the menu item
	 * @param menuItem the menu item
	 */
	public void addMenuItem(String id, JMenuItem menuItem){
	    
	    if(id!=null && ! id.equals("") && menuItem!=null){ //$NON-NLS-1$
	        
	        menuItems.put(id, menuItem);
	    }
	}
	
	/**
	 * returns a menu given its name
	 * @param menuName the name of a menu
	 * @return a menu
	 */
	public JMenu getMenu(String menuName){
		
		if(menuName!=null && ! menuName.equals("")){ //$NON-NLS-1$

			return (JMenu)menus.get(menuName);
		}
		
		return null;
	}
	
	/**
	 * adds a menu item that is not listed in the xml menu file
	 * @param menuName the name of a menu
	 * @param menuItem the menu item
	 */
	public void addUnknownMenuItem(String menuName, JMenuItem menuItem){
		
		if(menuName!=null && ! menuName.equals("") && menuItem!=null){ //$NON-NLS-1$
			
			LinkedList list=null;
			
			if(unknownMenuItems.containsKey(menuName)){
				
				list=(LinkedList)unknownMenuItems.get(menuName);
				list.add(menuItem);
				
			}else{
				
				list=new LinkedList();
				list.add(menuItem);
				unknownMenuItems.put(menuName, list);
			}
		}
	}
	
	/**
	 * removes a menu item that is not listed in the xml menu file
	 * @param menuName the name of a menu
	 * @param menuItem the menu item
	 */
	public void removeUnknownMenuItem(String menuName, JMenuItem menuItem){
		
		if(menuName!=null && ! menuName.equals("") && menuItem!=null){ //$NON-NLS-1$
			
			LinkedList list=null;
			
			if(unknownMenuItems.containsKey(menuName)){
				
				list=(LinkedList)unknownMenuItems.get(menuName);
				
				if(list!=null){
				    
				    list.remove(menuItem);
				}
			}
		}
	}

	/**
	 * builds the menubar given the order of menu items specified in the XML file
	 */
	public void build(){
		
		removeAll();
		
		if(docMenu!=null){
			
			Element root=docMenu.getDocumentElement();
			
			if(root!=null){

				Iterator it2;
				Node current=null, parent=null;
				String name=null, att=null, attParent=null;
				JMenu menu=null, parentMenu=null;
				JMenuItem menuItem=null;
				LinkedList list=null;
				
				for(NodeIterator it=new NodeIterator(root); it.hasNext();){
					
					current=(Node)it.next();
					
					if(current!=null && current instanceof Element){
						
						name=current.getNodeName();
						
						if(name!=null){
							
							if(name.equals("menu")){ //$NON-NLS-1$
								
								//finds and adds the menu to the menu bar
								att=((Element)current).getAttribute("name"); //$NON-NLS-1$

								if(att!=null && ! att.equals("")){ //$NON-NLS-1$

									menu=(JMenu)menus.get(att);
									
									if(menu!=null){
										
										menu.removeAll();
										parent=current.getParentNode();
										
										if(parent!=null && parent instanceof Element){
											
											attParent=((Element)parent).getAttribute("name"); //$NON-NLS-1$
											parentMenu=null;
											
											try{
												parentMenu=(JMenu)menus.get(attParent);
											}catch (Exception ex){parentMenu=null;}
										
											if(parentMenu!=null){
											    
												parentMenu.add(menu);
												
											}else{
												
												add(menu);
											}
										}
										
										//adds the non xml specified menu items
										list=(LinkedList)unknownMenuItems.get(att);
										
										if(list!=null){

											for(it2=list.iterator(); it2.hasNext();){
												
												try{
													menuItem=(JMenuItem)it2.next();
												}catch (Exception ex){menuItem=null;}
												
												if(menuItem!=null){
												    
													menu.add(menuItem);
												}
											}
										}
									}
								}
									
							}else if(name.equals("menuitem")){ //$NON-NLS-1$
								
								//finds and adds the menu item to the current menu
								att=((Element)current).getAttribute("name"); //$NON-NLS-1$

								if(att!=null && ! att.equals("")){ //$NON-NLS-1$
									
									menuItem=(JMenuItem)menuItems.get(att);
									parent=current.getParentNode();
									
									if(menuItem!=null && parent!=null && parent instanceof Element){
										
										attParent=((Element)parent).getAttribute("name"); //$NON-NLS-1$
										parentMenu=null;
										
										try{
											parentMenu=(JMenu)menus.get(attParent);
										}catch (Exception ex){parentMenu=null;}
									
										if(parentMenu!=null){
										    
											parentMenu.add(menuItem);
											
										}else{
											
											menu.add(menuItem);
										}
									}
								}
								
							}else if(name.equals("separator")){ //$NON-NLS-1$
							    
								//adds a separator to the menu
								parent=current.getParentNode();
								
								if(parent!=null && parent instanceof Element){
									
									attParent=((Element)parent).getAttribute("name"); //$NON-NLS-1$
									parentMenu=null;
									
									try{
										parentMenu=(JMenu)menus.get(attParent);
									}catch (Exception ex){parentMenu=null;}
								
									if(parentMenu!=null){
									    
										parentMenu.add(new JSeparator());
									}
								}
							}
						}
					}
				}
			}
		}
	
	}
	
	/**
	 *parses the document to get the menu and menu items specified in the "menu.xml" file
	 */
	protected void parseXMLMenu(){

		if(docMenu!=null){
			
			Element root=docMenu.getDocumentElement();
			
			if(root!=null){

				Node current=null;
				String name=null, att=null, label=""; //$NON-NLS-1$
				JMenu menu=null;
				
				for(NodeIterator it=new NodeIterator(root); it.hasNext();){
					
					current=(Node)it.next();
					
					if(current!=null && current instanceof Element){	
						
						name=current.getNodeName();
						
						if(name!=null && name.equals("menu")){ //$NON-NLS-1$

							att=((Element)current).getAttribute("name"); //$NON-NLS-1$

							if(att!=null && ! att.equals("")){ //$NON-NLS-1$
								
								label=att;
								
								if(bundle!=null){
									
									try{label=bundle.getString(att);}catch (Exception ex){}
								}
								
								menu=new JMenu(label);
								menus.put(att, menu);
							}
						}
					}
				}
			}
		}
	}
	
}
