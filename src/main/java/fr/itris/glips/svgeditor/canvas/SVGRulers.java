/*
 * Created on 1 juil. 2004
 *
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
 * the class used to display the rulers
 * 
 * @author Jordi SUC
 *
 */
public class SVGRulers extends SVGModuleAdapter{
	
	/**
	 * the id
	 */
	private String idrulers="Rulers"; //$NON-NLS-1$
	
	/**
	 * the labels
	 */
	private String 	labelrulers="", labelrulershidden="", labelrulersshown="",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							labelalignwithrulersenabled="", labelalignwithrulersdisabled=""; //$NON-NLS-1$ //$NON-NLS-2$
	
	/**
	 * the menu items used for the rulers
	 */
	private final JMenuItem displayRulers=new JMenuItem(), alignWithRulers=new JMenuItem();
	
	/**
	 * the menu of the rulers
	 */
	private JMenu rulers=new JMenu();
	
	/**
	 * the editor
	 */
	private SVGEditor editor;
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGRulers(SVGEditor editor){
		this.editor=editor;
		
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labelrulers=bundle.getString("labelrulers"); //$NON-NLS-1$
				labelrulershidden=bundle.getString("labelrulershidden"); //$NON-NLS-1$
				labelrulersshown=bundle.getString("labelrulersshown"); //$NON-NLS-1$
				labelalignwithrulersenabled=bundle.getString("labelalignwithrulersenabled"); //$NON-NLS-1$
				labelalignwithrulersdisabled=bundle.getString("labelalignwithrulersdisabled"); //$NON-NLS-1$
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
					displayRulers.setEnabled(true);
					alignWithRulers.setEnabled(true);
					
				}else{
				    
					//disables the menuitems
					displayRulers.setEnabled(false);
					alignWithRulers.setEnabled(false);
					alignWithRulers.setText(labelalignwithrulersdisabled);
				}
			}	
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);
		
		//the menuitems for the rulers
		displayRulers.setText(labelrulersshown);
		displayRulers.setEnabled(false);
		
		alignWithRulers.setText(labelalignwithrulersdisabled);
		alignWithRulers.setEnabled(false);
		
		//adds the listeners
		displayRulers.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent evt) {
				
				SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
				
				if(frame!=null){
				    
					if(! frame.getScrollPane().isRulersVisible()){
					    
						frame.getScrollPane().setRulersVisible(true);
						displayRulers.setText(labelrulersshown);
						alignWithRulers.setEnabled(true);
	
					}else{
					    
						frame.getScrollPane().setRulersVisible(false);
						displayRulers.setText(labelrulershidden);
						alignWithRulers.setEnabled(false);
						frame.getScrollPane().setAlignWithRulers(false);
						alignWithRulers.setText(labelalignwithrulersdisabled);
					}
				}
			}
		});
		
		alignWithRulers.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent evt) {
					
				SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
					
				if(frame!=null){
					
					if(frame.getScrollPane().isRulersVisible()){
					    
						if(frame.getScrollPane().isAlignWithRulers()){
							
							frame.getScrollPane().setAlignWithRulers(false);
							alignWithRulers.setText(labelalignwithrulersdisabled);
							
						}else{
						    
							frame.getScrollPane().setAlignWithRulers(true);
							alignWithRulers.setText(labelalignwithrulersenabled);
						}
					}
				}
			}
		});
		
		//getting the icons
		ImageIcon icon=SVGResource.getIcon("Rulers", false), //$NON-NLS-1$
						disabledIcon=SVGResource.getIcon("Rulers", true); //$NON-NLS-1$
		
		//builds the menu
		rulers.setIcon(icon);
		rulers.setDisabledIcon(disabledIcon);
		rulers.setText(labelrulers);
		rulers.add(displayRulers);
		rulers.add(alignWithRulers);
	}
	
	@Override
	public Hashtable<String, JMenuItem> getMenuItems() {

		Hashtable<String, JMenuItem> menuItems=new Hashtable<String, JMenuItem>();
		menuItems.put(idrulers, rulers);
		
		return menuItems;
	}
	
	/**
	 * @return the editor
	 */
	public SVGEditor getSVGEditor(){
		return editor;
	}
}
