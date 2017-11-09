/*
 * Created on 30 avr. 2004
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
 * Sets the nodes size to the same size as the first selected node size
 */
public class SVGSame extends SVGModuleAdapter{
	
	/**
	 * the id of the module
	 */
	final private String idsame="Same", idsamewidth="SameWidth", idsameheight="SameHeight", idsamesize="SameSize"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	
	/**
	 * the labels
	 */
	private String labelsame="", labelsamewidth="", labelsameheight="", labelsamesize=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	
	/**
	 * the undo/redo labels
	 */
	private String undoredosamewidth="", undoredosameheight="", undoredosamesize=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	/**
	 * the constant defining the same width action
	 */
	final private int SAME_WIDTH=0;
	
	/**
	 * the constant defining the same height action
	 */
	final private int SAME_HEIGHT=1;
	
	/**
	 * the constant defining the same size action
	 */
	final private int SAME_SIZE=2;

	/**
	 * the menu items that will be contained in the menu
	 */
	private JMenuItem samewidth, sameheight, samesize;
	
	/**
	 * the listeners to the menu items
	 */
	private ActionListener sameWidthListener, sameHeightListener, sameSizeListener;
	
	/**
	 * the menu in which the menu items will be inserted
	 */
	private JMenu same;
	
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
	public SVGSame(SVGEditor editor) {
		
		this.editor=editor;
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labelsame=bundle.getString("labelsame"); //$NON-NLS-1$
				labelsamewidth=bundle.getString("labelsamewidth"); //$NON-NLS-1$
				labelsameheight=bundle.getString("labelsameheight"); //$NON-NLS-1$
				labelsamesize=bundle.getString("labelsamesize"); //$NON-NLS-1$
				undoredosamewidth=bundle.getString("undoredosamewidth"); //$NON-NLS-1$
				undoredosameheight=bundle.getString("undoredosameheight"); //$NON-NLS-1$
				undoredosamesize=bundle.getString("undoredosamesize"); //$NON-NLS-1$
			}catch (Exception ex){}
		}
		
		//getting the icons
		ImageIcon sameWidthIcon=SVGResource.getIcon("SameWidth", false), //$NON-NLS-1$
						dsameWidthIcon=SVGResource.getIcon("SameWidth", true), //$NON-NLS-1$
						sameHeightIcon=SVGResource.getIcon("SameHeight", false), //$NON-NLS-1$
						dsameHeightIcon=SVGResource.getIcon("SameHeight", true), //$NON-NLS-1$
						sameSizeIcon=SVGResource.getIcon("SameSize", false), //$NON-NLS-1$
						dsameSizeIcon=SVGResource.getIcon("SameSize", true); //$NON-NLS-1$
		
		//creates the menu items, sets the keyboard shortcuts
		samewidth=new JMenuItem(labelsamewidth, sameWidthIcon);
		samewidth.setDisabledIcon(dsameWidthIcon);
		samewidth.setAccelerator(KeyStroke.getKeyStroke("")); //$NON-NLS-1$
		samewidth.setEnabled(false);
		
		sameheight=new JMenuItem(labelsameheight, sameHeightIcon);
		sameheight.setDisabledIcon(dsameHeightIcon);
		sameheight.setAccelerator(KeyStroke.getKeyStroke("")); //$NON-NLS-1$
		sameheight.setEnabled(false);
		
		samesize=new JMenuItem(labelsamesize, sameSizeIcon);
		samesize.setDisabledIcon(dsameSizeIcon);
		samesize.setAccelerator(KeyStroke.getKeyStroke("")); //$NON-NLS-1$
		samesize.setEnabled(false);
		
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
				samewidth.setEnabled(false);
				sameheight.setEnabled(false);
				samesize.setEnabled(false);
				
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

						public void actionPerformed(ActionEvent evt) {
						    
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
				samewidth.setEnabled(false);
				sameheight.setEnabled(false);
				samesize.setEnabled(false);
				
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
				    
					samewidth.setEnabled(true);
					sameheight.setEnabled(true);
					samesize.setEnabled(true);	
				}								
			}
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);

		//adds the listeners
		sameWidthListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>1){
					    
						same(selectednodes,SAME_WIDTH);
						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
			    }
			}
		};
		
		samewidth.addActionListener(sameWidthListener);

		sameHeightListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>1){
					    
						same(selectednodes,SAME_HEIGHT);
						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
			    }
			}
		};
		
		sameheight.addActionListener(sameHeightListener);
		
		sameSizeListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>1){
					    
						same(selectednodes,SAME_SIZE);
						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
			    }
			}
		};
		
		samesize.addActionListener(sameSizeListener);	
		
		//adds the menu items to the menu
		same=new JMenu(labelsame);
		same.add(samewidth);
		same.add(sameheight);
		same.add(samesize);
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
		menuItems.put(idsame, same);
		
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
		
		SVGPopupSubMenu subMenu=new SVGPopupSubMenu(getSVGEditor(), idsame, labelsame, ""); //$NON-NLS-1$
		
		popupItems.add(subMenu);
		
		//creating the same width popup item
		SVGPopupItem sameWidthItem=new SVGPopupItem(getSVGEditor(), idsamewidth, labelsamewidth, "SameWidth"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>1){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(sameWidthListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the same height popup item
		SVGPopupItem sameHeightItem=new SVGPopupItem(getSVGEditor(), idsameheight, labelsameheight, "SameHeight"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>1){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(sameHeightListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the same size popup item
		SVGPopupItem sameSizeItem=new SVGPopupItem(getSVGEditor(), idsamesize, labelsamesize, "SameSize"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>1){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(sameSizeListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//adding the popup items to the sub menu
		subMenu.addPopupItem(sameWidthItem);
		subMenu.addPopupItem(sameHeightItem);
		subMenu.addPopupItem(sameSizeItem);
		
		return popupItems;
	}
	
	/**
	 * allows to set the nodes given in the list to the same width, height or size of the first node in the list
	 * @param list the nodes 
	 * @param type the type of the resizement
	 */
	protected void same(LinkedList list, int type){
		
		final SVGFrame frame=editor.getFrameManager().getCurrentFrame();
		
		if(list!=null && list.size()>1 && frame!=null){
			
			final LinkedList snodes=new LinkedList(list);
			final int ftype=type;
			Node reference=null;
			
			try{
				reference=(Node)snodes.getFirst();
			}catch (Exception e){reference=null;}
			
			//gets the bounds of the first node in the selected list, that will be used to resize the other nodes
			final Rectangle2D refrect=frame.getNodeGeometryBounds((Element)reference);
			
			if(reference!=null && refrect!=null){
				
				//the map associating a node to a transform map
				final Hashtable transformMap=new Hashtable();

				//for each node of the list, the scale and translators factors are 
				//computed and the transform matrix is modified
				Runnable runnable=new Runnable(){
				    
					public void run(){
					    
						Iterator it=snodes.iterator();
						
						//the first node of the list is not handled
						if(it.hasNext()){
						    
						    it.next();
						}
	
						SVGTransformMatrix matrix=null;
						AffineTransform af=null;
						Rectangle2D rect=null;
						Node current=null;
						double a=1, d=1, tx=0, ty=0;
								
						while(it.hasNext()){
						    
							try{
								current=(Node)it.next();
							}catch (Exception ex){current=null;}
						
							if(current!=null){
								
								//computes the bounds of the current node
								rect=frame.getNodeGeometryBounds((Element)current);
								a=1; d=1;
								
								//computes the transform factors
								if(rect!=null){
								    
									a=refrect.getWidth()/rect.getWidth();
									d=refrect.getHeight()/rect.getHeight();
									tx=rect.getX()*(1-a);
									ty=rect.getY()*(1-d);
								}
							
								if(ftype==SAME_WIDTH){
								    
									d=1;
									ty=0;
									
								}else if(ftype==SAME_HEIGHT){
								    
									a=1;
									tx=0;
								}
								
								//the transform
								af=AffineTransform.getScaleInstance(a, d);
								af.preConcatenate(AffineTransform.getTranslateInstance(tx, ty));
								
								transformMap.put(current, af);
							
								//gets, modifies and sets the transformation matrix
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
				};
				
				frame.enqueue(runnable);

				//sets the name of the undo/redo action
				String actionName=""; //$NON-NLS-1$
				
				if(type==SAME_WIDTH){
				    
					actionName=undoredosamewidth;
					
				}else if(type==SAME_HEIGHT){
				    
					actionName=undoredosameheight;
					
				}else if(type==SAME_SIZE){
				    
					actionName=undoredosamesize;
				}
		
				//creates the undo/redo action and insert it into the undo/redo stack
				if(editor.getUndoRedo()!=null){

					SVGUndoRedoAction action=new SVGUndoRedoAction(actionName){

						@Override
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
						
										if(af!=null){
										    
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

						@Override
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
						
										if(af!=null){
										    
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
	
	/**
	 * gets the name of the module
	 * @return the name of the module
	 */
	public String getName(){
		return idsame;
	}
}
