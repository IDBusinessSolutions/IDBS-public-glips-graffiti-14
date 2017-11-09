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
 * the class allowing to rotate a node 
 */
public class SVGRotate extends SVGModuleAdapter{

	/**
	 * the id of the module
	 */
	final private String idrotate="Rotate", idrotate90="Rotate90", idrotatem90="Rotatem90", idrotate180="Rotate180"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	
	/**
	 * the labels
	 */
	private String labelrotate="", labelrotate90="", labelrotatem90="", labelrotate180=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	
	/**
	 * the undo/redo labels
	 */
	private String undoredorotate=""; //$NON-NLS-1$
	
	/**
	 * the constant defining the 90 degrees rotation
	 */
	final private int ROTATE_90=0;
	
	/**
	 * the constant defining the -90 degrees rotation
	 */
	final private int ROTATE_MINUS_90=1;
	
	/**
	 * the constant defining the 180 degrees rotation
	 */
	final private int ROTATE_180=2;

	/**
	 * the menu items that will be contained in the menu
	 */
	private JMenuItem rotate90, rotatem90, rotate180;
	
	/**
	 * the listeners to the menu items
	 */
	private ActionListener rotate90Listener, rotatem90Listener, rotate180Listener;
	
	/**
	 * the menu in which the menu items will be inserted
	 */
	private JMenu rotate;
	
	/**
	 * the editor
	 */
	private SVGEditor editor=null;
	
	/**
	 * the nodes that are currently selected
	 */
	private LinkedList selectedNodes=new LinkedList();
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGRotate(SVGEditor editor){
		
		this.editor=editor;
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labelrotate=bundle.getString("labelrotate"); //$NON-NLS-1$
				labelrotate90=bundle.getString("labelrotate90"); //$NON-NLS-1$
				labelrotatem90=bundle.getString("labelrotatem90"); //$NON-NLS-1$
				labelrotate180=bundle.getString("labelrotate180"); //$NON-NLS-1$
				undoredorotate=bundle.getString("undoredorotate"); //$NON-NLS-1$
			}catch (Exception ex){}
		}
		
		//getting the icons
		ImageIcon rotate90Icon=SVGResource.getIcon("Rotate90", false), //$NON-NLS-1$
						drotate90Icon=SVGResource.getIcon("Rotate90", true), //$NON-NLS-1$
						rotatem90Icon=SVGResource.getIcon("Rotatem90", false), //$NON-NLS-1$
						drotatem90Icon=SVGResource.getIcon("Rotatem90", true), //$NON-NLS-1$
						rotate180Icon=SVGResource.getIcon("Rotate180", false), //$NON-NLS-1$
						drotate180Icon=SVGResource.getIcon("Rotate180", true); //$NON-NLS-1$
		
		//creates the menu items, sets the keyboard shortcuts
		rotate90=new JMenuItem(labelrotate90, rotate90Icon);
		rotate90.setDisabledIcon(drotate90Icon);
		rotate90.setAccelerator(KeyStroke.getKeyStroke("")); //$NON-NLS-1$
		rotate90.setEnabled(false);
		
		rotatem90=new JMenuItem(labelrotatem90, rotatem90Icon);
		rotatem90.setDisabledIcon(drotatem90Icon);
		rotatem90.setAccelerator(KeyStroke.getKeyStroke("")); //$NON-NLS-1$
		rotatem90.setEnabled(false);
		
		rotate180=new JMenuItem(labelrotate180, rotate180Icon);
		rotate180.setDisabledIcon(drotate180Icon);
		rotate180.setAccelerator(KeyStroke.getKeyStroke("")); //$NON-NLS-1$
		rotate180.setEnabled(false);
		
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
				selectedNodes.clear();
				
				//disables the menuitems
				rotate90.setEnabled(false);
				rotatem90.setEnabled(false);
				rotate180.setEnabled(false);

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
				rotate90.setEnabled(false);
				rotatem90.setEnabled(false);
				rotate180.setEnabled(false);
				
				LinkedList list=null;
				
				//gets the currently selected nodes list 
				if(selection!=null){
				    
					list=selection.getCurrentSelection(getSVGEditor().getFrameManager().getCurrentFrame());
				}
				
				selectedNodes.clear();
				
				//refresh the selected nodes list
				if(list!=null){
				    
				    selectedNodes.addAll(list);
				}
				
				if(selectedNodes.size()>0){
				    
					rotate90.setEnabled(true);
					rotatem90.setEnabled(true);
					rotate180.setEnabled(true);
				}								
			}
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);

		//adds the listeners
		rotate90Listener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectedNodes.size()>0){

						rotate(selectedNodes,ROTATE_90);
					}
			    }
			}
		};
		
		rotate90.addActionListener(rotate90Listener);

		rotatem90Listener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectedNodes.size()>0){

						rotate(selectedNodes,ROTATE_MINUS_90);
					}
			    }
			}
		};
		
		rotatem90.addActionListener(rotatem90Listener);
		
		rotate180Listener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectedNodes.size()>0){

						rotate(selectedNodes,ROTATE_180);
					}
			    }
			}
		};
		
		rotate180.addActionListener(rotate180Listener);

		//adds the menu items to the menu
		rotate=new JMenu(labelrotate);
		rotate.add(rotate90);
		rotate.add(rotatem90);
		rotate.add(rotate180);
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
		menuItems.put(idrotate, rotate);
		
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
		
		SVGPopupSubMenu subMenu=new SVGPopupSubMenu(getSVGEditor(), idrotate, labelrotate, ""); //$NON-NLS-1$
		
		popupItems.add(subMenu);
		
		//creating the rotate90 popup item
		SVGPopupItem rotate90Item=new SVGPopupItem(getSVGEditor(), idrotate90, labelrotate90, "Rotatem90"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(rotate90Listener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the rotatem90 popup item
		SVGPopupItem rotatem90Item=new SVGPopupItem(getSVGEditor(), idrotatem90, labelrotatem90, "Rotatem90"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(rotatem90Listener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the rotatem180 popup item
		SVGPopupItem rotatem180Item=new SVGPopupItem(getSVGEditor(), idrotate180, labelrotate180, "Rotate180"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(rotate180Listener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//adding the popup items to the sub menu
		subMenu.addPopupItem(rotate90Item);
		subMenu.addPopupItem(rotatem90Item);
		subMenu.addPopupItem(rotatem180Item);
		
		return popupItems;
	}
	
	/**
	 * used to horizontally or vertically flip a the nodes contained in the given list
	 * @param list the list of nodes
	 * @param type the type of the flip (HORIZONTAL_FLIP or VERTICAL_FLIP)
	 */
	protected void rotate(LinkedList list, int type){
		
		final SVGFrame frame=editor.getFrameManager().getCurrentFrame();
		
		if(list!=null && list.size()>0 && frame!=null){
			
			final LinkedList snodes=new LinkedList(list);
			final int ftype=type;

			Runnable runnable=new Runnable(){
			    
				public void run(){
				    
					//the map associating a node to its old affine transform
					final Hashtable oldTransformMap=new Hashtable();
				    
					//the map associating a node to its new affine transform
					final Hashtable transformMap=new Hashtable();
							
					Node current=null;
					SVGTransformMatrix matrix=null;
					AffineTransform af=null;
					Rectangle2D rect=null;
					double cx=0, cy=0;
					
					//gets or creates the undo/redo list
					SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList(undoredorotate);
				
					//for each selected node
					for(Iterator it=snodes.iterator(); it.hasNext();){
					    
						try{current=(Node)it.next();}catch(Exception ex){current=null;}
						
						if(current!=null){
						
							rect=frame.getNodeGeometryBounds((Element)current);
							
							if(rect!=null){
							    
								Point2D.Double centerpoint=new Point2D.Double(rect.getX()+rect.getWidth()/2, 
								        																	rect.getY()+rect.getHeight()/2);
								
								//getting the matrix and the affine transform associated with the current node
								matrix=editor.getSVGToolkit().getTransformMatrix(current);
								af=matrix.getTransform();
								oldTransformMap.put(current, new AffineTransform(af));
								
								af.preConcatenate(AffineTransform.getTranslateInstance(-centerpoint.x, -centerpoint.y));
								
								if(ftype==ROTATE_90){
								    
								    af.preConcatenate(AffineTransform.getRotateInstance(Math.PI/2));
									
								}else if(ftype==ROTATE_MINUS_90){
								    
								    af.preConcatenate(AffineTransform.getRotateInstance(-Math.PI/2));
									
								}else if(ftype==ROTATE_180){
								    
								    af.preConcatenate(AffineTransform.getRotateInstance(Math.PI));
								}
								
								af.preConcatenate(AffineTransform.getTranslateInstance(centerpoint.x, centerpoint.y));

								transformMap.put(current, af);
								
								//creating the new transform matrix
								matrix=new SVGTransformMatrix(1, 0, 0, 1, 0, 0);
								matrix.concatenateTransform(af);
								
								if(matrix.isMatrixCorrect()){
								    
									editor.getSVGToolkit().setTransformMatrix(current, matrix);	
									
									//creates the undo/redo action and insert it into the undo/redo stack
									if(editor.getUndoRedo()!=null){

										SVGUndoRedoAction action=new SVGUndoRedoAction(undoredorotate){

											public void undo(){
											    
												//sets the nodes transformation matrix
												if(transformMap.size()>0){

													Node current=null;
													SVGTransformMatrix matrix=null;
													AffineTransform af=null;
													
													for(Iterator it=oldTransformMap.keySet().iterator(); it.hasNext();){
													    
														try{current=(Node)it.next();}catch (Exception ex){current=null;}
														
														if(current!=null){
														    
														    //getting the old affine transform associated with this node 
															try{af=(AffineTransform)oldTransformMap.get(current);}catch (Exception ex){af=null;}
												
															if(af!=null){
															    
															    matrix=new SVGTransformMatrix(1, 0, 0, 1, 0, 0);
															    matrix.concatenateTransform(af);
															    
																//sets the old matrix
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
													    
														try{current=(Node)it.next();}catch (Exception ex){current=null;}
														
														if(current!=null){
														    
														    //getting the new affine transform associated with this node 
															try{af=(AffineTransform)transformMap.get(current);}catch (Exception ex){af=null;}
												
															if(af!=null){
															    
															    matrix=new SVGTransformMatrix(1, 0, 0, 1, 0, 0);
															    matrix.concatenateTransform(af);
															    
																//sets the new matrix
																editor.getSVGToolkit().setTransformMatrix(current, matrix);	
															}
														}
													}
													
												    frame.getScrollPane().getSVGCanvas().delayedRepaint();
												}
											}
										};
									
										//adds the undo/redo action into the action list
										actionlist.add(action);
									}
								}
							}
						}
					}
					
					//adding the action list to the undo/redo module
					editor.getUndoRedo().addActionList(frame, actionlist);
					
				    frame.getScrollPane().getSVGCanvas().delayedRepaint();
				    
					//sets that the svg has been modified
					getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
				}
			};
			
			frame.enqueue(runnable);
		}		
	}
	
	/**
	 * gets the name of the module
	 * @return the name of the module
	 */
	public String getName(){
		return idrotate;
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

