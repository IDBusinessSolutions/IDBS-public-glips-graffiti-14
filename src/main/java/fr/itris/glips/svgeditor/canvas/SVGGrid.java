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
package fr.itris.glips.svgeditor.canvas;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.resources.*;

/**
 * the class used to display the grid on the canvas
 * 
 * @author Jordi SUC
 */
public class SVGGrid extends SVGModuleAdapter{
	
	/**
	 * the id
	 */
	private String idgrid="Grid"; //$NON-NLS-1$
	
	/**
	 * the labels
	 */
	private String /*	labelframegrid="", */labelframegridhidden="", labelframegridshown=""; //$NON-NLS-1$ //$NON-NLS-2$
	
	/**
	 * the menu items used to display the grid
	 */
	private final JMenuItem gridDisplay=new JMenuItem();
	
	/**
	 * the editor
	 */
	private SVGEditor editor;

	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGGrid(SVGEditor editor){
	    
		this.editor=editor;
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				//labelframegrid=bundle.getString("labelgrid");
				labelframegridhidden=bundle.getString("labelgridhidden"); //$NON-NLS-1$
				labelframegridshown=bundle.getString("labelgridshown"); //$NON-NLS-1$
			}catch (Exception ex){}
		}
		
		//a listener that listens to the changes of the SVGFrames
		final ActionListener svgframeListener=new ActionListener(){
			
			/**
			 * called when an event occurs
			 */
			public void actionPerformed(ActionEvent e) {
				
				final SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
				
				if(frame!=null){
				    
					//enables the menuitems
					gridDisplay.setEnabled(true);
					
					if(frame.getScrollPane().getGrid().isEnableGrid()){
					    
						gridDisplay.setText(labelframegridshown);
	
					}else{
					    
						gridDisplay.setText(labelframegridhidden);
					}
					
				}else{
				    
					//disables the menuitems
					gridDisplay.setEnabled(false);
				}
			}	
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);
		
		//the menu item for the grid display
		gridDisplay.setText(labelframegridhidden);
		gridDisplay.setEnabled(false);
		
		//adds the listener
		gridDisplay.addActionListener(new ActionListener(){

			/**
			 * the method called when the action is done
			 */
			public void actionPerformed(ActionEvent evt) {
			    
				SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
				
				if(frame!=null){
				    
					if(! frame.getScrollPane().getGrid().isEnableGrid()){
					    
						frame.getScrollPane().getGrid().setEnableGrid(true);
						gridDisplay.setText(labelframegridshown);
	
					}else{
					    
						frame.getScrollPane().getGrid().setEnableGrid(false);
						gridDisplay.setText(labelframegridhidden);
					}
				}
			}
		});
		
		//getting the icons
		ImageIcon icon=SVGResource.getIcon("Grid", false), //$NON-NLS-1$
						disabledIcon=SVGResource.getIcon("Grid", true); //$NON-NLS-1$
		
		//the grid menu
		gridDisplay.setIcon(icon);
		gridDisplay.setDisabledIcon(disabledIcon);
	}
	
	@Override
	public Hashtable<String, JMenuItem> getMenuItems() {

		Hashtable<String, JMenuItem> menuItems=new Hashtable<String, JMenuItem>();
		menuItems.put(idgrid,gridDisplay);
		
		return menuItems;
	}
	
	/**
	 * @return the editor
	 */
	public SVGEditor getSVGEditor(){
		return editor;
	}
}
