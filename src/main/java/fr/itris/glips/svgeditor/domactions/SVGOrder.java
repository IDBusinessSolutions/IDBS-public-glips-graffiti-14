/*
 * Created on 23 avr. 2004
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
 * the class allowing to put a node over or behind another, or at the top or the bottom of the document
 */
public class SVGOrder extends SVGModuleAdapter{

	/**
	 * the constants
	 */
	private final int TO_TOP=0;
	private final int TO_UP=1;
	private final int TO_DOWN=2;
	private final int TO_BOTTOM=3;
	
	/**
	 * the id of the module
	 */
	final private String idorder="Order", idordertop="OrderTop", idorderup="OrderUp", idorderdown="OrderDown", idorderbottom="OrderBottom"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	
	/**
	 * the labels
	 */
	private String labelorder="", labelordertop="", labelorderup="", labelorderdown="",  labelorderbottom=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	
	/**
	 * the undo/redo labels
	 */
	private String undoredoordertop="", undoredoorderup="", undoredoorderdown="",  undoredoorderbottom=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	/**
	 * the menu items that will be contained in the menu
	 */
	private JMenuItem toTop, toUp, toDown, toBottom;
	
	/**
	 * the listeners to the menu items
	 */
	private ActionListener toTopListener, toUpListener, toDownListener, toBottomListener;
	
	/**
	 * the menu in which the menu items will be inserted
	 */
	private JMenu order;
	
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
	public SVGOrder(SVGEditor editor) {
		
		this.editor=editor;
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labelorder=bundle.getString("labelorder"); //$NON-NLS-1$
				labelordertop=bundle.getString("labelordertop"); //$NON-NLS-1$
				labelorderup=bundle.getString("labelorderup"); //$NON-NLS-1$
				labelorderdown=bundle.getString("labelorderdown"); //$NON-NLS-1$
				labelorderbottom=bundle.getString("labelorderbottom"); //$NON-NLS-1$
				undoredoordertop=bundle.getString("undoredoordertop"); //$NON-NLS-1$
				undoredoorderup=bundle.getString("undoredoorderup"); //$NON-NLS-1$
				undoredoorderdown=bundle.getString("undoredoorderdown"); //$NON-NLS-1$
				undoredoorderbottom=bundle.getString("undoredoorderbottom"); //$NON-NLS-1$
			}catch (Exception ex){}
		}
		
		//getting the icons
		ImageIcon orderTopIcon=SVGResource.getIcon("OrderTop", false), //$NON-NLS-1$
						dorderTopIcon=SVGResource.getIcon("OrderTop", true), //$NON-NLS-1$
						orderUpIcon=SVGResource.getIcon("OrderUp", false), //$NON-NLS-1$
						dorderUpIcon=SVGResource.getIcon("OrderUp", true), //$NON-NLS-1$
						orderDownIcon=SVGResource.getIcon("OrderDown", false), //$NON-NLS-1$
						dorderDownIcon=SVGResource.getIcon("OrderDown", true), //$NON-NLS-1$
						orderBottomIcon=SVGResource.getIcon("OrderBottom", false), //$NON-NLS-1$
						dorderBottomIcon=SVGResource.getIcon("OrderBottom", true); //$NON-NLS-1$

		toTop=new JMenuItem(labelordertop, orderTopIcon);
		toTop.setDisabledIcon(dorderTopIcon);
		toTop.setAccelerator(KeyStroke.getKeyStroke("ctrl shift UP")); //$NON-NLS-1$
		toTop.setEnabled(false);
		
		toUp=new JMenuItem(labelorderup, orderUpIcon);
		toUp.setDisabledIcon(dorderUpIcon);
		toUp.setAccelerator(KeyStroke.getKeyStroke("ctrl UP")); //$NON-NLS-1$
		toUp.setEnabled(false);
		
		toDown=new JMenuItem(labelorderdown, orderDownIcon);
		toDown.setDisabledIcon(dorderDownIcon);
		toDown.setAccelerator(KeyStroke.getKeyStroke("ctrl DOWN")); //$NON-NLS-1$
		toDown.setEnabled(false);
		
		toBottom=new JMenuItem(labelorderbottom, orderBottomIcon);
		toBottom.setDisabledIcon(dorderBottomIcon);
		toBottom.setAccelerator(KeyStroke.getKeyStroke("ctrl shift DOWN")); //$NON-NLS-1$
		toBottom.setEnabled(false);

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
				toTop.setEnabled(false);
				toUp.setEnabled(false);
				toDown.setEnabled(false);
				toBottom.setEnabled(false);
				
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
				toTop.setEnabled(false);
				toUp.setEnabled(false);
				toDown.setEnabled(false);
				toBottom.setEnabled(false);
				
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
				    
					toTop.setEnabled(true);
					toUp.setEnabled(true);
					toDown.setEnabled(true);
					toBottom.setEnabled(true);
				}								
			}
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);

		//adds the listeners
		toTopListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>0){
					    
						order(selectednodes, TO_TOP);
					}
			    }
			}
		};
		
		toTop.addActionListener(toTopListener);
		
		toUpListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>0){
					    
						order(selectednodes, TO_UP);
					}
			    }
			}
		};
		
		toUp.addActionListener(toUpListener);
		
		toDownListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>0){
					    
						order(selectednodes, TO_DOWN);
					}
			    }
			}
		};
		
		toDown.addActionListener(toDownListener);
		
		toBottomListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>0){
					    
						order(selectednodes, TO_BOTTOM);
					}
			    }
			}
		};
		
		toBottom.addActionListener(toBottomListener);		
		
		//adds the menu items to the menu
		order=new JMenu(labelorder);
		order.add(toTop);
		order.add(toUp);
		order.add(toDown);
		order.add(toBottom);
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
		menuItems.put(idorder, order);
		
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
		
		SVGPopupSubMenu subMenu=new SVGPopupSubMenu(getSVGEditor(), idorder, labelorder, ""); //$NON-NLS-1$
		
		popupItems.add(subMenu);
		
		//creating the top popup item
		SVGPopupItem toTopItem=new SVGPopupItem(getSVGEditor(), idordertop, labelordertop, "OrderTop"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(toTopListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the up popup item
		SVGPopupItem toUpItem=new SVGPopupItem(getSVGEditor(), idorderup, labelorderup, "OrderUp"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(toUpListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the down popup item
		SVGPopupItem toDownItem=new SVGPopupItem(getSVGEditor(), idorderdown, labelorderdown, "OrderDown"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(toDownListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the bottom popup item
		SVGPopupItem toBottomItem=new SVGPopupItem(getSVGEditor(), idorderbottom, labelorderbottom, "OrderBottom"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(toBottomListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//adding the popup items to the sub menu
		subMenu.addPopupItem(toTopItem);
		subMenu.addPopupItem(toUpItem);
		subMenu.addPopupItem(toDownItem);
		subMenu.addPopupItem(toBottomItem);
		
		return popupItems;
	}
	
	/**
	 * sets the nodes contained in the linked list at the top of the document
	 * @param list the list of the selected nodes
	 * @param type the type of the order action
	 */
	protected void order(LinkedList list, int type){
		
		final SVGFrame frame=editor.getFrameManager().getCurrentFrame();
		
		if(list!=null && list.size()>0 && frame!=null){
			
			final LinkedList snodes=new LinkedList(list);
			final int ftype=type;
			final Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
			
			//getting the parent node
			Node p=null;
			
			try{p=((Node)snodes.getFirst()).getParentNode();}catch (Exception ex){p=null;}
			
			final Node parent=p;
			
			if(doc!=null && parent!=null){

				NodeList allChildren=parent.getChildNodes();
				
				//the list of the shape elements in the svg document
				final LinkedList childrenList=new LinkedList();
				final LinkedList orderedSelectedNodes=new LinkedList();

				Node current=null;
				
				//orders the selected nodes according to their place in the dom
				for(int i=0;i<allChildren.getLength();i++){
				    
					current=allChildren.item(i);
					
					if(current!=null && current instanceof Element && ! current.getNodeName().equals("defs")){ //$NON-NLS-1$
					    
						childrenList.add(current);
						
						if(snodes.contains(current)){
						    
						    orderedSelectedNodes.add(current);
						}
					}
				}
				
				//tests whether the order action should be done or not
				if(isOrderActionNeeded(type, orderedSelectedNodes, childrenList)){
				    
					//put the selected nodes in the proper place
					final Runnable orderAction=new Runnable(){
					    
						public void run(){
						    
							Node current=null;
							Iterator it;
						    
							if(ftype==TO_BOTTOM){

							    //getting the first child element in the dom that is not selected
								Element firstChild=null;
								
								for(it=childrenList.iterator(); it.hasNext();){
								    
								    current=(Element)it.next();
								    
								    if(current!=null && ! orderedSelectedNodes.contains(current)){
								        
								        firstChild=(Element)current;
								        break;
								    }
								}

								if(firstChild!=null){
								    
								    //inserts each selected element at the beginning of the dom
									for(it=orderedSelectedNodes.iterator(); it.hasNext();){
									    
										try{current=(Node)it.next();}catch (Exception ex){current=null;}
					
										if(current!=null){

										    parent.insertBefore(current, firstChild);
										}
									}
								}
								
							}else if(ftype==TO_DOWN){
							    
							    int ind=0, i;
							    Element previousElement=null;
							    
							    //puts each selected element to the previous step
								for(it=orderedSelectedNodes.iterator(); it.hasNext();){
								    
									try{current=(Node)it.next();}catch (Exception ex){current=null;}
				
									if(current!=null){

									    //getting the index of the current element in the children list
									    ind=childrenList.indexOf(current);
									    
									    if(ind>0){
									        
									        //getting the node that lays before the current node in the list and that is not selected
									        //and inserting the current element before this node
									        for(i=ind-1; i>=0; i--){
									            
									            previousElement=(Element)childrenList.get(i);
									            
									            if(previousElement!=null && ! orderedSelectedNodes.contains(previousElement)){
									                
												    parent.insertBefore(current, previousElement);
												    break;
									            }
									        }
									    }
									}
								}

							}else if(ftype==TO_UP){
							    
								//reverses the list of the ordered selected nodes
								LinkedList reversedSelectedNodes=new LinkedList(orderedSelectedNodes);
								Collections.reverse(reversedSelectedNodes);

							    int ind=0, i;
							    Element nextElement=null;
							    
							    //puts each selected element to the previous step
								for(it=reversedSelectedNodes.iterator(); it.hasNext();){
								    
									try{current=(Node)it.next();}catch (Exception ex){current=null;}
				
									if(current!=null){

									    //getting the index of the current element in the children list
									    ind=childrenList.indexOf(current);
									    
									    if(ind>=0 && ind<childrenList.size()-1){
									        
									        //getting the node that lays after the current node in the list and that is not selected
									        //and inserting the current element after this node
									        for(i=ind+1; i<childrenList.size(); i++){
									            
									            nextElement=(Element)childrenList.get(i);
									            
									            if(nextElement!=null && ! orderedSelectedNodes.contains(nextElement)){
									                
									                if(nextElement.getNextSibling()!=null){
									                    
									                    parent.insertBefore(current, nextElement.getNextSibling());
									                    
									                }else{
									                    
									                    parent.removeChild(current);
									                    parent.appendChild(current);
									                }

												    break;
									            }
									        }
									    }
									}
								}

							}else if(ftype==TO_TOP){

								//reverses the list of the ordered selected nodes
								LinkedList reversedSelectedNodes=new LinkedList(orderedSelectedNodes);
								Collections.reverse(reversedSelectedNodes);
							    
							    //getting the last child element in the dom that is not selected
								Element lastElement=null;
								
								for(int i=childrenList.size()-1; i>=0; i--){
								    
								    current=(Element)childrenList.get(i);
								    
								    if(current!=null && ! orderedSelectedNodes.contains(current)){
								        
								        lastElement=(Element)current;
								        break;
								    }
								}

								if(lastElement!=null){
								    
								    //inserts each selected element at the beginning of the dom
									for(it=reversedSelectedNodes.iterator(); it.hasNext();){
									    
										try{current=(Node)it.next();}catch (Exception ex){current=null;}
					
										if(current!=null){

							                if(lastElement.getNextSibling()!=null){
							                    
							                    parent.insertBefore(current, lastElement.getNextSibling());
							                    
							                }else{
							                    
							                    parent.removeChild(current);
							                    parent.appendChild(current);
							                }
										}
									}
								}
							}
						}
					};
					
					frame.enqueue(orderAction);
					
					//sets the label for the undo/redo action
					String undoredo=""; //$NON-NLS-1$
					
					if(type==TO_TOP){
					    
						undoredo=undoredoordertop;

					}else if(type==TO_UP){
					    
						undoredo=undoredoorderup;

					}else if(type==TO_DOWN){
					    
						undoredo=undoredoorderdown;
					
					}else if(type==TO_BOTTOM){
					    
						undoredo=undoredoorderbottom;
					}
					
					//creates the undo/redo action and insert it into the undo/redo stack
					if(editor.getUndoRedo()!=null){

						SVGUndoRedoAction action=new SVGUndoRedoAction(undoredo){

							public void undo(){
								
								NodeList newChildren=parent.getChildNodes();
								Node current=null;
								int i;
								
								for(i=0;i<newChildren.getLength();i++){
								    
									try{
										current=newChildren.item(i);
										
										if(current!=null){
										    
										    parent.removeChild(current);
										}
									}catch (Exception ex){current=null;}
								}
								
								for(i=0;i<childrenList.size();i++){
								    
									try{
										current=(Node)childrenList.get(i);
									}catch (Exception ex){current=null;}
									
									if(current!=null){
									    
									    parent.appendChild(current);
									}
								}
							}

							public void redo(){

							    orderAction.run();
							}
						};
						
						//gets or creates the undo/redo list and adds the action into it
						SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList(undoredo);
						actionlist.add(action);
						editor.getUndoRedo().addActionList(frame, actionlist);
						
						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
				}
			}
		}
	}
	
	/**
	 * checks whether an order action should be done or not
	 * @param type the type of the action
	 * @param nodesToBeModified the list of the nodes to be modified
	 * @param children the list of the children of the parent node
	 * @return whether an order action should be done or not
	 */
	protected boolean isOrderActionNeeded(int type, LinkedList nodesToBeModified, LinkedList children){
	    
	    boolean isOrderActionNeeded=false;
	    
	    if(nodesToBeModified!=null && nodesToBeModified.size()>0 && children!=null && children.size()>0){
	        
	        //for each order action type, checks if this action is needed
			if(type==TO_TOP || type==TO_UP){
			    
			    //getting the list of the nodes that lay at the end of the children list, the list having the size of the list
			    //of the nodes to be modified
			    List endList=children.subList(children.size()-nodesToBeModified.size(), children.size());
			    
			    if(! endList.containsAll(nodesToBeModified)){
			        
			        isOrderActionNeeded=true;
			    }

			}else if(type==TO_BOTTOM || type==TO_DOWN){
			    
			    //getting the list of the nodes that lay at the beginning of the children list, the list having the size of the list
			    //of the nodes to be modified
			    List beginningList=children.subList(0, nodesToBeModified.size());
			    
			    if(! beginningList.containsAll(nodesToBeModified)){
			        
			        isOrderActionNeeded=true;
			    }
			}
	    }
	    
	    return isOrderActionNeeded;
	}
	
	/**
	 * gets the name of the module
	 * @return the name of the module
	 */
	public String getName(){
		return idorder;
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
