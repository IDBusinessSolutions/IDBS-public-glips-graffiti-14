/*
 * Created on 10 déc. 2004
 * =============================================
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

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.apache.batik.util.gui.*;

/**
 * @author Jordi SUC
 *
 * the module displaying a memory monitor
 */
public class SVGMemoryMonitor extends SVGModuleAdapter{
    
	/**
	 * the id
	 */
	private String idMemoryMonitor="MemoryMonitor"; //$NON-NLS-1$

	/**
	 * the labels
	 */
	private String labelMemoryMonitor=""; //$NON-NLS-1$
	
	/**
	 * the menu item
	 */
	private final JMenuItem memoryMonitorMenuitem=new JMenuItem();
	
	/**
	 * the resource bundle
	 */
	private ResourceBundle bundle=null;
	
	/**
	 * the editor
	 */
	private SVGEditor editor;
    
    /**
     * the constructor of the class
	 * @param editor the editor
     */
    public SVGMemoryMonitor(SVGEditor theEditor) {

		this.editor = theEditor;
		
		//the resource bundle
        bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
		    try{
		        labelMemoryMonitor=bundle.getString("labelmemorymonitor"); //$NON-NLS-1$
		    }catch (Exception ex){}
		}
		
		//the menuitem
		memoryMonitorMenuitem.setText(labelMemoryMonitor);
		
		//the dialog
		final MemoryMonitor memoryMonitorDialog=new MemoryMonitor();
		
		//the listener to the menu item
		memoryMonitorMenuitem.addActionListener(
		        
			new ActionListener(){
			    
				public void actionPerformed(ActionEvent arg0) {
					
					if(! memoryMonitorDialog.isVisible()){
						
						//sets the location of the dialog box
						int 	x=(int)(editor.getParent().getLocationOnScreen().getX()+editor.getParent().getWidth()/2-memoryMonitorDialog.getSize().getWidth()/2), 
								y=(int)(editor.getParent().getLocationOnScreen().getY()+editor.getParent().getHeight()/2-memoryMonitorDialog.getSize().getHeight()/2);
						
						memoryMonitorDialog.setLocation(x,y);
						memoryMonitorDialog.setVisible(true);
					}
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
    
	@Override
	public Hashtable<String, JMenuItem> getMenuItems(){
		
		Hashtable<String, JMenuItem> menuItems=new Hashtable<String, JMenuItem>();
		menuItems.put(idMemoryMonitor, memoryMonitorMenuitem);
		
		return menuItems;
	}
}
