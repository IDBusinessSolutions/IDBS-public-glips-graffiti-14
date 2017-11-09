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
public class SVGSpacing extends SVGModuleAdapter{
	
	/**
	 * the id of the module
	 */
	final private String idspacing="Spacing", idspacinghorizontal="HorizontalSpacing", idspacingvertical="VerticalSpacing"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	/**
	 * the labels
	 */
	private String labelspacing="", labelspacinghorizontal="", labelspacingvertical=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	/**
	 * the undo/redo labels
	 */
	private String undoredospacinghorizontal="", undoredospacingvertical=""; //$NON-NLS-1$ //$NON-NLS-2$
	
	/**
	 * the constant defining the horizontal center
	 */
	final private int HORIZONTAL_SPACING=0;
	
	/**
	 * the constant defining the vertical center
	 */
	final private int VERTICAL_SPACING=1;

	/**
	 * the menu items that will be contained in the menu
	 */
	private JMenuItem spacingh, spacingv;
	
	/**
	 * the listeners to the menu items
	 */
	private ActionListener horizontalSpacingListener, verticalSpacingListener;
	
	/**
	 * the menu in which the menu items will be inserted
	 */
	private JMenu spacing;
	
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
	public SVGSpacing(SVGEditor editor) {
		
		this.editor=editor;
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labelspacing=bundle.getString("labelspacing"); //$NON-NLS-1$
				labelspacinghorizontal=bundle.getString("labelspacinghorizontal"); //$NON-NLS-1$
				labelspacingvertical=bundle.getString("labelspacingvertical"); //$NON-NLS-1$
				undoredospacinghorizontal=bundle.getString("undoredospacinghorizontal"); //$NON-NLS-1$
				undoredospacingvertical=bundle.getString("undoredospacingvertical"); //$NON-NLS-1$
			}catch (Exception ex){}
		}
		
		//getting the icons
		ImageIcon horizontalSpacingIcon=SVGResource.getIcon("HorizontalSpacing", false), //$NON-NLS-1$
						dhorizontalSpacingIcon=SVGResource.getIcon("HorizontalSpacing", true), //$NON-NLS-1$
						verticalSpacingIcon=SVGResource.getIcon("VerticalSpacing", false), //$NON-NLS-1$
						dverticalSpacingIcon=SVGResource.getIcon("VerticalSpacing", true); //$NON-NLS-1$
		
		//creates the menu items, sets the keyboard shortcuts
		spacingh=new JMenuItem(labelspacinghorizontal, horizontalSpacingIcon);
		spacingh.setDisabledIcon(dhorizontalSpacingIcon);
		spacingh.setAccelerator(KeyStroke.getKeyStroke("")); //$NON-NLS-1$
		spacingh.setEnabled(false);
		
		spacingv=new JMenuItem(labelspacingvertical, verticalSpacingIcon);
		spacingv.setDisabledIcon(dverticalSpacingIcon);
		spacingv.setAccelerator(KeyStroke.getKeyStroke("")); //$NON-NLS-1$
		spacingv.setEnabled(false);
		
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
				spacingh.setEnabled(false);
				spacingv.setEnabled(false);
				
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
				spacingh.setEnabled(false);
				spacingv.setEnabled(false);
				
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
				
				if(selectednodes.size()>1){
				    
					spacingh.setEnabled(true);
					spacingv.setEnabled(true);		
				}								
			}
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);
		
		//adds the listeners
		horizontalSpacingListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>1){
						
						spacing(selectednodes,HORIZONTAL_SPACING);

						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
			    }
			}
		};
		
		spacingh.addActionListener(horizontalSpacingListener);

		verticalSpacingListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>1){

						spacing(selectednodes,VERTICAL_SPACING);

						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
			    }
			}
		};
		
		spacingv.addActionListener(verticalSpacingListener);
		
		//adds the menu items to the menu
		spacing=new JMenu(labelspacing);
		spacing.add(spacingh);
		spacing.add(spacingv);
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
		menuItems.put(idspacing, spacing);
		
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
		
		SVGPopupSubMenu subMenu=new SVGPopupSubMenu(getSVGEditor(), idspacing, labelspacing, ""); //$NON-NLS-1$
		
		popupItems.add(subMenu);
		
		//creating the horizontal spacing popup item
		SVGPopupItem horizontalSpacingItem=new SVGPopupItem(getSVGEditor(), idspacinghorizontal, labelspacinghorizontal, "HorizontalSpacing"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>1){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(horizontalSpacingListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the vertical spacing popup item
		SVGPopupItem verticalSpacingItem=new SVGPopupItem(getSVGEditor(), idspacingvertical, labelspacingvertical, "VerticalSpacing"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>1){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(verticalSpacingListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//adding the popup items to the sub menu
		subMenu.addPopupItem(horizontalSpacingItem);
		subMenu.addPopupItem(verticalSpacingItem);
		
		return popupItems;
	}
	
	/**
	 * allows to put equal spaces vertically or horizontally between the different nodes of a list
	 * @param list the nodes 
	 * @param type the type of the distribution of spaces
	 */
	protected void spacing(LinkedList list, int type){
		
		final SVGFrame frame=editor.getFrameManager().getCurrentFrame();
		
		if(list!=null && list.size()>0 && frame!=null){
			
			final LinkedList snodes=new LinkedList(list);
			final int ftype=type;

			//the map associating a node to an affine transform
			final Hashtable transformMap=new Hashtable();
			final LinkedList ordered=new LinkedList();
			Element current=null, ocurrent=null;
			Rectangle2D rect=null;
			double wsum=0, hsum=0;
			int i=0;
			
			//orders the nodes given their position
			for(Iterator it=list.iterator(); it.hasNext();){
			    
				try{
					current=(Element)it.next();
				}catch (Exception e){current=null;}
				
				if(current!=null){
				    
					rect=frame.getNodeGeometryBounds(current);
					
					if(rect!=null){
					    
						wsum+=rect.getWidth();
						hsum+=rect.getHeight();
					}

					if(ordered.size()>0){
						
						for(i=0;i<ordered.size();i++){
						    
							try{
								ocurrent=(Element)ordered.get(i);	
							}catch(Exception ex){ocurrent=null;}
							
							if(ocurrent!=null){
							    
								if(isGreaterThan(ocurrent,current,type)){
								    
									if(i>0){
									    
										ordered.add(i,current);
										
									}else {
									    
										ordered.addFirst(current);
									} 
									
									break;
									
								}else if(i==ordered.size()-1){
								    
									ordered.addLast(current);
									break;
								}
							}
						}
						
					}else{
					    
						ordered.add(current);
					}	
				}
			}
			
			final Rectangle2D frect=rect;
			final double fwsum=wsum;
			final double fhsum=hsum;
			
			Runnable runnable=new Runnable(){
			    
				public void run() {

					double wtotal=0, htotal=0, wsum=fwsum, hsum=fhsum;
					Node current=null;
					Rectangle2D rect1=null, rect2=null, rect=frect;
					
					if(ordered.size()>1 && ordered.get(0)!=null && ordered.get(ordered.size()-1)!=null){
					    
						try{
							rect1=frame.getNodeGeometryBounds((Element)ordered.get(0));
							rect2=frame.getNodeGeometryBounds((Element)ordered.get(ordered.size()-1));
						}catch (Exception ex){rect1=null; rect2=null;}
						
						if(rect1!=null && rect2!=null){
						    
							//computes the space between the first and the last node
							wtotal=rect2.getWidth()+rect2.getX()-rect1.getX();
							htotal=rect.getHeight()+rect2.getY()-rect1.getY();
							
							if(wtotal>0 && htotal>0){
							    
								//computes the space that will be set between each node
								double spacew=(wtotal-wsum)/(ordered.size()-1), spaceh=(htotal-hsum)/(ordered.size()-1), e=0, f=0;
								Node lastnode=null;
								Rectangle2D lrect=null;
								SVGTransformMatrix matrix=null;
								AffineTransform af=null;
								
								//computes the new matrix for each node
								for(Iterator it=ordered.iterator(); it.hasNext();){
								    
									try{
										current=(Node)it.next();
									}catch (Exception ex){current=null;}
									
									if(current!=null){

										rect=frame.getNodeGeometryBounds((Element)current);
										
										if(lastnode!=null){
										    
										    lrect=frame.getNodeGeometryBounds((Element)lastnode);
										    
										}else{
										    
										    lrect=null;
										}
										
										e=0; f=0;

										//computes the translation values
										if(rect!=null && lrect!=null){
										    
											if(ftype==HORIZONTAL_SPACING){
											    
												e=(lrect==null?0:-rect.getX()+lrect.getX()+lrect.getWidth()+spacew);
												
											}else if(ftype==VERTICAL_SPACING){
											    
												f=(lrect==null?0:-rect.getY()+lrect.getY()+lrect.getHeight()+spaceh);
											}

											af=AffineTransform.getTranslateInstance(e, f);
											transformMap.put(current, af);
											
											//sets the transformation matrix
											if(! af.isIdentity()){
											    
											    //gets, modifies and sets the transform matrix
												matrix=editor.getSVGToolkit().getTransformMatrix(current);
												matrix.concatenateTransform(af);
												editor.getSVGToolkit().setTransformMatrix(current, matrix);		
											}
										}
										
										lastnode=current;
									}
								}
								
							    frame.getScrollPane().getSVGCanvas().delayedRepaint();
						
								//creates the undo/redo action and insert it into the undo/redo stack
								if(editor.getUndoRedo()!=null){
							
									//sets the name of the undo/redo action
									String actionName=""; //$NON-NLS-1$
									
									if(ftype==HORIZONTAL_SPACING){
									    
										actionName=undoredospacinghorizontal;
										
									}else if(ftype==VERTICAL_SPACING){
									    
										actionName=undoredospacingvertical;
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
														    
															//gets, modifies and sets the transform matrix
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
														    
															//gets, modifies and sets the transform matrix
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
				}
			};
			
			frame.enqueue(runnable);
		}
	}
	
	/**
	 * tells if node1 is greater than node2 given the type of the comparison
	 * @param node1 the first node
	 * @param node2 the second node
	 * @param type the type of the comparison
	 * @return true if node1 is greater than node2
	 */
	protected boolean isGreaterThan(Element node1, Element node2, int type){
		
		final SVGFrame frame=editor.getFrameManager().getCurrentFrame();		
		Rectangle2D rect1=frame.getNodeGeometryBounds(node1), 
							rect2=frame.getNodeGeometryBounds(node2);
		
		if(rect1!=null && rect2!=null){
		    
			if(type==HORIZONTAL_SPACING && rect1.getX()>=rect2.getX()){

					return true;

			}else if(type==VERTICAL_SPACING && rect1.getY()>=rect2.getY()){
			    
			    return true;
			}
		}

		return false;
	}
	
	/**
	 * gets the name of the module
	 * @return the name of the module
	 */
	public String getName(){
		return idspacing;
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
