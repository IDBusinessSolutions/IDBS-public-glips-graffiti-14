/*
 * Created on 24 mars 2004
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

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.resources.*;

/**
 * @author Jordi SUC
 * the class that creates the static menu item exit in the menu bar
 */
public class SVGExit extends SVGModuleAdapter{

	/**
	 * the id of the module
	 */
	final private String idexit="Exit"; //$NON-NLS-1$
	
	/**
	 * the labels
	 */
	private String labelexit=""; //$NON-NLS-1$

	/**
	 * the menuItems
	 */
	private JMenuItem exit;
	
	/**
	 * the editor 
	 */
	private SVGEditor editor;
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGExit(SVGEditor editor){
		this.editor=editor;
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labelexit=bundle.getString("labelexit"); //$NON-NLS-1$
			}catch (Exception ex){}
		}
		
		//getting the icons
		ImageIcon exitIcon=SVGResource.getIcon("Exit", false), //$NON-NLS-1$
						dexitIcon=SVGResource.getIcon("Exit", true); //$NON-NLS-1$
		
		//handling the menu item
		exit=new JMenuItem(labelexit, exitIcon);
		exit.setDisabledIcon(dexitIcon);
		exit.setAccelerator(KeyStroke.getKeyStroke("ESCAPE")); //$NON-NLS-1$
			
		//adds a listener to the menu item
		exit.addActionListener(
			new ActionListener(){
			    
				public void actionPerformed(ActionEvent e){
				    
					getSVGEditor().exit();
				}
			}
		);
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
		menuItems.put(idexit, exit);	
		
		return menuItems;
	}
	
	/**
	 * @return a map associating a tool item id to its tool item object
	 */
	public Hashtable getToolItems(){
		
		return null;
	}
	
	/**
	 * Returns the collection of the popup items
	 * @return the collection of the popup items
	 */
	public Collection getPopupItems(){
		
		return null;
	}
	
	/**
	 * gets the module's name
	 * @return the module's name
	 */
	public String getName(){
		return idexit;
	}
	
	/**
	 * cancels all the actions that could be running
	 */
	public void cancelActions(){
	}

	/**
	 * layout some elements in the module
	 */
	public void initialize(){
		

	}
}
