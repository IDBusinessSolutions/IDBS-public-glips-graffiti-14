/*
 * Created on 4 mai 2004
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
 * the class allowing to flip a node 
 */
public class SVGFlip extends SVGModuleAdapter{
	
	/**
	 * the id of the module
	 */
	final private String idflip="Flip", idfliphorizontal="HorizontalFlip", idflipVertical="VerticalFlip"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	/**
	 * the labels
	 */
	private String labelflip="", labelfliphorizontal="", labelflipvertical=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	/**
	 * the undo/redo labels
	 */
	private String undoredofliphorizontal="", undoredoflipvertical=""; //$NON-NLS-1$ //$NON-NLS-2$
	
	/**
	 * the listeners for the menu items
	 */
	private ActionListener horizontalFlipListener, verticalFlipListener;
	
	/**
	 * the constant defining the horizontal center
	 */
	final private int HORIZONTAL_FLIP=0;
	
	/**
	 * the constant defining the vertical center
	 */
	final private int VERTICAL_FLIP=1;

	/**
	 * the menu items that will be contained in the menu
	 */
	private JMenuItem fliph, flipv;
	
	/**
	 * the menu in which the menu items will be inserted
	 */
	private JMenu flip;

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
	public SVGFlip(SVGEditor editor) {
		
		this.editor=editor;
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labelflip=bundle.getString("labelflip"); //$NON-NLS-1$
				labelfliphorizontal=bundle.getString("labelfliphorizontal"); //$NON-NLS-1$
				labelflipvertical=bundle.getString("labelflipvertical"); //$NON-NLS-1$
				undoredofliphorizontal=bundle.getString("undoredofliphorizontal"); //$NON-NLS-1$
				undoredoflipvertical=bundle.getString("undoredoflipvertical"); //$NON-NLS-1$
			}catch (Exception ex){}
		}
		
		//getting the icons
		ImageIcon fliphIcon=SVGResource.getIcon("HorizontalFlip", false), //$NON-NLS-1$
						dfliphIcon=SVGResource.getIcon("HorizontalFlip", true), //$NON-NLS-1$
						flipvIcon=SVGResource.getIcon("VerticalFlip", false), //$NON-NLS-1$
						dflipvIcon=SVGResource.getIcon("VerticalFlip", true); //$NON-NLS-1$
		
		//creates the menu items, sets the keyboard shortcuts
		fliph=new JMenuItem(labelfliphorizontal, fliphIcon);
		fliph.setDisabledIcon(dfliphIcon);
		fliph.setAccelerator(KeyStroke.getKeyStroke("")); //$NON-NLS-1$
		fliph.setEnabled(false);
		
		flipv=new JMenuItem(labelflipvertical, flipvIcon);
		flipv.setDisabledIcon(dflipvIcon);
		flipv.setAccelerator(KeyStroke.getKeyStroke("")); //$NON-NLS-1$
		flipv.setEnabled(false);
		
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
				fliph.setEnabled(false);
				flipv.setEnabled(false);
				
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
				fliph.setEnabled(false);
				flipv.setEnabled(false);
				
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
				    
					fliph.setEnabled(true);
					flipv.setEnabled(true);			
				}								
			}
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);

		//adds the listeners
		horizontalFlipListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>0){
						
						flip(selectednodes,HORIZONTAL_FLIP);
						
						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
			    }
			}
		};
		
		fliph.addActionListener(horizontalFlipListener);

		verticalFlipListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>0){

						flip(selectednodes, VERTICAL_FLIP);
						
						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
			    }
			}
		};
		
		flipv.addActionListener(verticalFlipListener);		
		
		//adds the menu items to the menu
		flip=new JMenu(labelflip);
		flip.add(fliph);
		flip.add(flipv);
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
		menuItems.put(idflip, flip);
		
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
		
		SVGPopupSubMenu subMenu=new SVGPopupSubMenu(getSVGEditor(), idflip, labelflip, ""); //$NON-NLS-1$
		
		popupItems.add(subMenu);
		
		//creating the horizontal flip popup item
		SVGPopupItem horizontalFlipItem=new SVGPopupItem(getSVGEditor(), idfliphorizontal, labelfliphorizontal, "HorizontalFlip"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(horizontalFlipListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the vertical flip popup item
		SVGPopupItem verticalFlipItem=new SVGPopupItem(getSVGEditor(), idflipVertical, labelflipvertical, "VerticalFlip"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(verticalFlipListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//adding the popup items to the sub menu
		subMenu.addPopupItem(horizontalFlipItem);
		subMenu.addPopupItem(verticalFlipItem);
		
		return popupItems;
	}
	
	/**
	 * used to horizontally or vertically flip a the nodes contained in the given list
	 * @param list the list of nodes
	 * @param type the type of the flip (HORIZONTAL_FLIP or VERTICAL_FLIP)
	 */
	protected void flip(LinkedList list, int type){
		
		final SVGFrame frame=editor.getFrameManager().getCurrentFrame();
		
		if(list!=null && list.size()>0 && frame!=null){

			final LinkedList snodes=new LinkedList(list);
			final int ftype=type;
			
			//the map associating a node to an affine transform
			final Hashtable transformMap=new Hashtable();

			Runnable runnable=new Runnable(){
			    
				public void run(){
				
					Node current=null;
					SVGTransformMatrix matrix=null;
					AffineTransform af=null;
					Rectangle2D rect=null;
					double cx=0, cy=0;
				
					//for each selected node
					for(Iterator it=snodes.iterator(); it.hasNext();){
					    
						try{current=(Node)it.next();}catch(Exception ex){current=null;}
						
						if(current!=null){
					
							rect=frame.getNodeGeometryBounds((Element)current);
						
							//computes the translation values
							cx=0;
							cy=0;
							
							if(rect!=null){
							    
								if(ftype==HORIZONTAL_FLIP){
								    
									cx=2*rect.getX()+rect.getWidth();
						
								}else if(ftype==VERTICAL_FLIP){
								    
									cy=2*rect.getY()+rect.getHeight();
								}
							}
	
							//the affine transform
							if(ftype==HORIZONTAL_FLIP){
							    
							    af=AffineTransform.getScaleInstance(-1, 1);
							    
							}else if(ftype==VERTICAL_FLIP){
							    
							    af=AffineTransform.getScaleInstance(1, -1);
							}

							af.preConcatenate(AffineTransform.getTranslateInstance(cx, cy));
							transformMap.put(current, af);
						
							//getting, modifying and setting the transform matrix
							matrix=editor.getSVGToolkit().getTransformMatrix(current);
							matrix.concatenateTransform(af);
							editor.getSVGToolkit().setTransformMatrix(current, matrix);	
						}
					}
					
				    frame.getScrollPane().getSVGCanvas().delayedRepaint();
				}
			};

			frame.enqueue(runnable);
			
			//creates the undo/redo action and insert it into the undo/redo stack
			if(editor.getUndoRedo()!=null){
						
				//sets the name of the undo/redo action
				String actionName=""; //$NON-NLS-1$
				
				if(type==HORIZONTAL_FLIP){
				    
					actionName=undoredofliphorizontal;
					
				}else if(type==VERTICAL_FLIP){
				    
					actionName=undoredoflipvertical;
				}

				SVGUndoRedoAction action=new SVGUndoRedoAction(actionName){

					public void undo(){
					    
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
									    
										//getting, modifying and setting the transform matrix
										matrix=editor.getSVGToolkit().getTransformMatrix(current);
										try{matrix.concatenateTransform(af.createInverse());}catch (Exception ex){}
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
									    
										//getting, modifying and setting the transform matrix
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
	
	/**
	 * gets the name of the module
	 * @return the name of the module
	 */
	public String getName(){
		return idflip;
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
