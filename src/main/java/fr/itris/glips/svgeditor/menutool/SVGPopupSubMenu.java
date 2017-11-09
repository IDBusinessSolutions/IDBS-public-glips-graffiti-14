/*
 * Created on 20 févr. 2005
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

import fr.itris.glips.svgeditor.*;

/**
 * the class of the pop up sub menus
 * 
 * @author Jordi SUC
 */
public class SVGPopupSubMenu extends SVGPopupItem{

	/**
	 * the collection of the popup items that are contained in this popup submenu
	 */
	protected LinkedList subPopupItem=new LinkedList();
	
	/**
	 * whether the popup items are enabled or not
	 */
	protected boolean isEnabled=false;
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 * @param id the id of this popup item
	 * @param label the label for the popup item
	 * @param iconName the name of an icon
	 */
	public SVGPopupSubMenu(SVGEditor editor, String id, String label, String iconName){

		super(editor, id, label, iconName);
		
		menuItem=new JMenu(label);
		//menuItem.setFont(theFont);
		
		if(icon!=null){
			
			menuItem.setIcon(icon);
		}
		
		if(disabledIcon!=null){
			
			menuItem.setDisabledIcon(disabledIcon);
		}
	}
	
	/**
	 * returns the popup item that will be displayed
	 * @param nodes the nodes onto which the popup item will act
	 * @return the menu item
	 */
	public JMenuItem getPopupItem(LinkedList nodes){

		//removes all the items  from the menu
		((JMenu)menuItem).removeAll();
		
		//adding all the menu items that lie in this sub menu
		SVGPopupItem item=null;
		JMenuItem popupItem=null;
		isEnabled=false;
		
		for(Iterator it=subPopupItem.iterator(); it.hasNext();){
			
			item=(SVGPopupItem)it.next();
			
			if(item!=null){
				
				popupItem=item.getPopupItem(nodes);
				
				if(popupItem!=null){

					((JMenu)menuItem).add(popupItem);
					isEnabled=isEnabled || popupItem.isEnabled();
				}
			}
		}
		
		return menuItem;
	}
	
	/**
	 * adds a popup item to this popup sub menu item
	 * @param item a popup item
	 */
	public void addPopupItem(SVGPopupItem item){
		
		if(item!=null){
			
			subPopupItem.add(item);
		}
	}
	
	/**
	 * restores the popup items initial state
	 */
	public void setToInitialState() {

		SVGPopupItem item=null;
		
		for(Iterator it=subPopupItem.iterator(); it.hasNext();){
			
			item=(SVGPopupItem)it.next();
			
			if(item!=null){
				
				item.setToInitialState();
			}
		}
	}
	
	/**
	 * @return whether this popup item is enabled or not
	 */
	public boolean isEnabled(){
		
		return isEnabled;
	}
}
