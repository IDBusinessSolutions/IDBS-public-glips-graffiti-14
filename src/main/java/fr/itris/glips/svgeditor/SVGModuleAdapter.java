/*
 * Created on 1 juil. 2004
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
package fr.itris.glips.svgeditor;

import java.util.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.menutool.*;

/**
 * the adapter of the module interface
 * @author Jordi SUC
 */
public abstract class SVGModuleAdapter implements SVGModule{
	
	/**
	 * @return a map associating a menu item id to its menu item object
	 */
	public Hashtable<String, JMenuItem> getMenuItems(){
		
		return null;
	}
	
	/**
	 * Returns the list of the popup items
	 * @return the list of the popup items
	 */
	public Collection<SVGPopupItem> getPopupItems(){
		
		return null;
	}
	
	/**
	 * @return a map associating a tool item id to its tool item object
	 */
	public Hashtable<String, JToggleButton> getToolItems(){
		
		return null;
	}
	
	/**
	 * @return a map associating a tool item id to its tool item object
	 */
	public SVGToolFrame getToolFrame(){
		
		return null;
	}
	
	/**
	 * cancels all the actions that could be running
	 */
	public void cancelActions(){}
	
	/**
	 * initializes the module
	 */
	public void initialize(){}
	
}
