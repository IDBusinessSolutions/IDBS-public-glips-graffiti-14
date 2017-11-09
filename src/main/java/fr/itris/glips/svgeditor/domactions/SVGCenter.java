/*
 * Created on 29 avr. 2004
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
package fr.itris.glips.svgeditor.domactions;

import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.menutool.*;
import fr.itris.glips.svgeditor.resources.*;
import fr.itris.glips.svgeditor.selection.*;
import fr.itris.glips.svgeditor.undoredo.*;

/**
 * @author Jordi SUC
 *
 * the class used to center nodes horizontally or vertically
 */
public class SVGCenter extends SVGModuleAdapter{
	
	/**
	 * the ids of the module
	 */
	final private String idCenter="Center", idCenterHorizontal="HorizontalCenter", idCenterVertical="VerticalCenter"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	/**
	 * the labels
	 */
	private String labelcenter="", labelcenterhorizontal="", labelcentervertical=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	/**
	 * the undo/redo labels
	 */
	private String undoredocenterhorizontal="", undoredocentervertical=""; //$NON-NLS-1$ //$NON-NLS-2$
	
	/**
	 * the listeners to the menu items
	 */
	private ActionListener centerHorizontalListener, centerVerticalListener;
	
	/**
	 * the constant defining the horizontal center
	 */
	final private int HORIZONTAL_CENTER=0;
	
	/**
	 * the constant defining the vertical center
	 */
	final private int VERTICAL_CENTER=1;

	/**
	 * the menu items that will be contained in the menu
	 */
	private JMenuItem centerh, centerv;
	
	/**
	 * the menu in which the menu items will be inserted
	 */
	private JMenu center;
	
	/**
	 * the editor
	 */
	private SVGEditor editor=null;
	
	/**
	 * the nodes that are currently selected
	 */
	private LinkedList selectednodes=new LinkedList();
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGCenter(SVGEditor editor) {
		
		this.editor=editor;
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labelcenter=bundle.getString("labelcenter"); //$NON-NLS-1$
				labelcenterhorizontal=bundle.getString("labelcenterhorizontal"); //$NON-NLS-1$
				labelcentervertical=bundle.getString("labelcentervertical"); //$NON-NLS-1$
				undoredocenterhorizontal=bundle.getString("undoredocenterhorizontal"); //$NON-NLS-1$
				undoredocentervertical=bundle.getString("undoredocentervertical"); //$NON-NLS-1$
			}catch (Exception ex){}
		}
		
		//getting the icons
		ImageIcon 	horizontalCenterIcon=SVGResource.getIcon("HorizontalCenter", false), //$NON-NLS-1$
						dhorizontalCenterIcon=SVGResource.getIcon("HorizontalCenter", true), //$NON-NLS-1$
						verticalCenterIcon=SVGResource.getIcon("VerticalCenter", false), //$NON-NLS-1$
						dverticalCenterIcon=SVGResource.getIcon("VerticalCenter", true); //$NON-NLS-1$
		
		//creates the menu items, sets the keyboard shortcuts
		centerh=new JMenuItem(labelcenterhorizontal, horizontalCenterIcon);
		centerh.setDisabledIcon(dhorizontalCenterIcon);
		centerh.setAccelerator(KeyStroke.getKeyStroke("")); //$NON-NLS-1$
		centerh.setEnabled(false);
		
		centerv=new JMenuItem(labelcentervertical, verticalCenterIcon);
		centerv.setDisabledIcon(dverticalCenterIcon);
		centerv.setAccelerator(KeyStroke.getKeyStroke("")); //$NON-NLS-1$
		centerv.setEnabled(false);
		
		//a listener that listens to the changes of the SVGFrames
		final ActionListener svgframeListener=new ActionListener(){
			
			/**
			 * a listener on the selection changes
			 */
			private ActionListener selectionListener=null;
			
			/**
			 * the current selection module
			 */
			private SVGSelection selection=null;

			public void actionPerformed(ActionEvent e) {
				
				//clears the list of the selected items
				selectednodes.clear();
				
				//disables the menuitems
				centerh.setEnabled(false);
				centerv.setEnabled(false);
				
				final SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
				
				//if a selection listener is already registered on a selection module, it is removed	
				if(selection!=null && selectionListener!=null){
				    
					selection.removeSelectionListener(selectionListener);
				}

				//gets the current selection module	
				if(frame!=null){
				    
					selection=getSVGEditor().getSVGSelection();
				}
				
				if(frame!=null && selection!=null){
				    
					manageSelection();
					
					//the listener of the selection changes
					selectionListener=new ActionListener(){

						public void actionPerformed(ActionEvent e) {
						    
							manageSelection();
						}
					};
					
					//adds the selection listener
					if(selectionListener!=null){
					    
						selection.addSelectionListener(selectionListener);
					}
				}
			}	
			
			/**
			 * updates the selected items and the state of the menu items
			 */
			protected void manageSelection(){
			    
				//disables the menuitems							
				centerh.setEnabled(false);
				centerv.setEnabled(false);
				
				LinkedList list=null;
				
				//gets the currently selected nodes list 
				if(selection!=null){
				    
					list=selection.getCurrentSelection(getSVGEditor().getFrameManager().getCurrentFrame());
				}
				selectednodes.clear();
				
				//refresh the selected nodes list
				if(list!=null){
				    
				    selectednodes.addAll(list);
				}
				
				if(selectednodes.size()>0){
				    
					centerh.setEnabled(true);
					centerv.setEnabled(true);			
				}								
			}
			
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);
		
		//adds the listeners
		
		centerHorizontalListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>0){

						center(selectednodes,HORIZONTAL_CENTER);

						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
			    }
			}
		};
		
		centerh.addActionListener(centerHorizontalListener);
		
		centerVerticalListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>0){
						
						center(selectednodes, VERTICAL_CENTER);
						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
			    }
			}
		};

		centerv.addActionListener(centerVerticalListener);		
		
		//adds the menu items to the menu
		center=new JMenu(labelcenter);
		center.add(centerh);
		center.add(centerv);
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
		menuItems.put(idCenter, center);
		
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
		
		LinkedList popupItems=new LinkedList();
		
		SVGPopupSubMenu subMenu=new SVGPopupSubMenu(getSVGEditor(), idCenter, labelcenter, ""); //$NON-NLS-1$
		
		popupItems.add(subMenu);
		
		//creating the horizontal center popup item
		SVGPopupItem horizontalCenterItem=new SVGPopupItem(getSVGEditor(), idCenterHorizontal, labelcenterhorizontal, "HorizontalCenter"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(centerHorizontalListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the vertical center popup item
		SVGPopupItem verticalCenterItem=new SVGPopupItem(getSVGEditor(), idCenterVertical, labelcentervertical, "VerticalCenter"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(centerVerticalListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//adding the popup items to the sub menu
		subMenu.addPopupItem(horizontalCenterItem);
		subMenu.addPopupItem(verticalCenterItem);
		
		return popupItems;
	}
	
	/**
	 * allows to center a list of nodes vertically or horizontally
	 * @param list the nodes to be centered
	 * @param type the type of the center
	 */
	protected void center(LinkedList list, int type){
		
		final SVGFrame frame=editor.getFrameManager().getCurrentFrame();
		
		if(list!=null && list.size()>0 && frame!=null){
			
			final LinkedList snodes=new LinkedList(list);
			final int ftype=type;
			final Point2D.Double canvasSize=frame.getScrollPane().getSVGCanvas().getGeometryCanvasSize();
			
			if(canvasSize!=null){
			    
				//the map associating a node to its affine transform for this action
				final Hashtable transformMap=new Hashtable();
				
				Runnable runnable=new Runnable(){
				    
					public void run(){
								
						SVGTransformMatrix matrix=null;
						AffineTransform af=null;
						Rectangle2D rect=null;
						Node current=null;
						double e=0, f=0;
								
						for(Iterator it=snodes.iterator(); it.hasNext();){
						    
							try{
								current=(Node)it.next();
							}catch (Exception ex){current=null;}
						
							if(current!=null){
							    
								rect=frame.getNodeBounds((Element)current);
								
								if(rect!=null){
								    
									e=0; f=0;
									
									if(ftype==HORIZONTAL_CENTER){
									    
										e=(canvasSize.getX()-rect.getWidth())/2-rect.getX();
										
									}else if(ftype==VERTICAL_CENTER){
									    
										f=(canvasSize.getY()-rect.getHeight())/2-rect.getY();
									}
									
									af=AffineTransform.getTranslateInstance(e, f);
									transformMap.put(current, af);
									
									//sets the transformation matrix
									if(af!=null && ! af.isIdentity()){
									    
									    matrix=editor.getSVGToolkit().getTransformMatrix(current);
									    matrix.concatenateTransform(af);
									    editor.getSVGToolkit().setTransformMatrix(current, matrix);
									}
								}
							}
						}
						
					    frame.getScrollPane().getSVGCanvas().delayedRepaint();
					}
				};
				
				frame.enqueue(runnable);

				//sets the name of the undo/redo action
				String actionName=""; //$NON-NLS-1$
				
				if(type==HORIZONTAL_CENTER){
				    
					actionName=undoredocenterhorizontal;
					
				}else if(type==VERTICAL_CENTER){
				    
					actionName=undoredocentervertical;
				}
		
				//creates the undo/redo action and insert it into the undo/redo stack
				if(editor.getUndoRedo()!=null){
				    
					SVGUndoRedoAction action=new SVGUndoRedoAction(actionName){

						public void undo(){
						    
							//sets the nodes transformation matrix
							if(transformMap.size()>0){

								Node current=null;
								SVGTransformMatrix matrix=null;
								AffineTransform af=null;
								
								for(Iterator it=transformMap.keySet().iterator(); it.hasNext();){
								    
									try{current=(Node)it.next();}catch (Exception ex){current=null;}
									
									if(current!=null){
									    
										try{
											af=(AffineTransform)transformMap.get(current);
										}catch (Exception ex){af=null;}
						
										if(af!=null){

										    matrix=editor.getSVGToolkit().getTransformMatrix(current);
										    try {matrix.concatenateTransform(af.createInverse());}catch (Exception ex){}
										    editor.getSVGToolkit().setTransformMatrix(current, matrix);
										}
									}
								}
								
							    frame.getScrollPane().getSVGCanvas().delayedRepaint();
							}
						}

						public void redo(){
						    
							//sets the nodes transformation matrix					
							if(transformMap.size()>0){

								Node current=null;
								SVGTransformMatrix matrix=null;
								AffineTransform af=null;
								
								for(Iterator it=transformMap.keySet().iterator(); it.hasNext();){
								    
									try{
										current=(Node)it.next();
									}catch (Exception ex){current=null;}
									
									if(current!=null){
									    
										try{
											af=(AffineTransform)transformMap.get(current);
										}catch (Exception ex){af=null;}
						
										if(af!=null && ! af.isIdentity()){
										    
										    matrix=editor.getSVGToolkit().getTransformMatrix(current);
										    matrix.concatenateTransform(af);
										    editor.getSVGToolkit().setTransformMatrix(current, matrix);
										}
									}
								}
								
							    frame.getScrollPane().getSVGCanvas().delayedRepaint();
							}							
						}
					};
			
					//gets or creates the undo/redo list and adds the action into it
					SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList(actionName);
					actionlist.add(action);
					editor.getUndoRedo().addActionList(frame, actionlist);
				}
			}
		}		
	}
	
	/**
	 * gets the name of the module
	 * @return the name of the module
	 */
	public String getName(){
		return idCenter;
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
