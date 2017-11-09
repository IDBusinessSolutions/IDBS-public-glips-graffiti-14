/*
 * Created on 28 avr. 2004
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
 * the class allowing  to group or ungroup nodes in the svg document
 */
public class SVGGroupUnGroup extends SVGModuleAdapter{

	/**
	 * the ids of the module
	 */
	final private String idgroupungroup="GroupUnGroup", idgroupug="Group", idgungroup="UnGroup"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	/**
	 * the labels
	 */
	private String labelgroupug="", labelgungroup=""; //$NON-NLS-1$ //$NON-NLS-2$
	
	/**
	 * the undo/redo labels
	 */
	private String undoredogroupug="", undoredogungroup=""; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * the menu items that will be contained in the menu
	 */
	private JMenuItem group, ungroup;
	
	/**
	 * the action listeners
	 */
	private ActionListener groupListener=null, ungroupListener=null;
	
	/**
	 * the editor
	 */
	private SVGEditor editor=null;
	
	/**
	 * the nodes that are currently selected
	 */
	private LinkedList<Element> selectednodes=new LinkedList<Element>();
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGGroupUnGroup(SVGEditor editor) {
		
		this.editor=editor;
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labelgroupug=bundle.getString("labelgroupug"); //$NON-NLS-1$
				labelgungroup=bundle.getString("labelgungroup"); //$NON-NLS-1$
				undoredogroupug=bundle.getString("undoredogroupug"); //$NON-NLS-1$
				undoredogungroup=bundle.getString("undoredogungroup"); //$NON-NLS-1$
			}catch (Exception ex){}
		}
		
		//getting the icons
		ImageIcon groupIcon=SVGResource.getIcon("Group", false), //$NON-NLS-1$
						dgroupIcon=SVGResource.getIcon("Group", true), //$NON-NLS-1$
						ungroupIcon=SVGResource.getIcon("Ungroup", false), //$NON-NLS-1$
						dungroupIcon=SVGResource.getIcon("Ungroup", true); //$NON-NLS-1$

		//creates the menu items, sets the keyboard shortcuts
		group=new JMenuItem(labelgroupug, groupIcon);
		group.setDisabledIcon(dgroupIcon);
		group.setAccelerator(KeyStroke.getKeyStroke("ctrl G")); //$NON-NLS-1$
		group.setEnabled(false);
		
		ungroup=new JMenuItem(labelgungroup, ungroupIcon);
		ungroup.setDisabledIcon(dungroupIcon);
		ungroup.setAccelerator(KeyStroke.getKeyStroke("ctrl U")); //$NON-NLS-1$
		ungroup.setEnabled(false);
		
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
				group.setEnabled(false);
				ungroup.setEnabled(false);
				
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
				group.setEnabled(false);
				ungroup.setEnabled(false);
				
				LinkedList<Element> list=null;
				
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
				    
				    group.setEnabled(true);
				    
				    //checks if all the selected nodes are g elements to ungroup them all
				    LinkedList<Element> snodes=new LinkedList<Element>(selectednodes);
					Node current=null;
					boolean canBeEnabled=true;
					
					for(Iterator it=snodes.iterator(); it.hasNext();){
					    
						try{
							current=(Node)it.next();
						}catch (Exception ex){current=null;}
						
						if(current!=null){
						    
							canBeEnabled=canBeEnabled && current.getNodeName().equals("g"); //$NON-NLS-1$
						}							
					}
					
					if(canBeEnabled){
					    
					    ungroup.setEnabled(true);
					}
				}					
			}
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);

		//creating and adding the group menu item listener
		groupListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>0){
					    
						group(selectednodes);
						
						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
			    }
			}
		};
		
		group.addActionListener(groupListener);
		
		//creating and adding the ungroup menu item listener
		ungroupListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>0){
					    
						ungroup(selectednodes);
						
						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
			    }
			}
		};
		
		ungroup.addActionListener(ungroupListener);
	}
	
	/**
	 * @return the editor
	 */
	public SVGEditor getSVGEditor(){
		return editor;
	}

	@Override
	public Hashtable<String, JMenuItem> getMenuItems() {

		Hashtable<String, JMenuItem> menuItems=new Hashtable<String, JMenuItem>();
		menuItems.put(idgroupug, group);
		menuItems.put(idgungroup, ungroup);
		
		return menuItems;
	}

	@Override
	public Collection<SVGPopupItem> getPopupItems() {

		LinkedList<SVGPopupItem> popupItems=new LinkedList<SVGPopupItem>();
		
		//creating the group popup item
		SVGPopupItem item=new SVGPopupItem(getSVGEditor(), idgroupug, labelgroupug, "Group"){ //$NON-NLS-1$
		
			@Override
			public JMenuItem getPopupItem(LinkedList nodes) {
				
				if(nodes!=null && nodes.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					menuItem.addActionListener(groupListener);
					
				}else{
					
					menuItem.setEnabled(false);
				}

				return super.getPopupItem(nodes);
			}
		};
		
		popupItems.add(item);
		
		//creating the ungroup popup item
		item=new SVGPopupItem(getSVGEditor(), idgungroup, labelgungroup, "Ungroup"){ //$NON-NLS-1$
			
			@Override
			public JMenuItem getPopupItem(LinkedList nodes) {
				
				if(nodes!=null && nodes.size()>0){
					
				    //checks if all the selected nodes are g elements to ungroup them all
					Node current=null;
					boolean canBeEnabled=true;
					
					for(Iterator it=nodes.iterator(); it.hasNext();){
					    
						try{current=(Node)it.next();}catch (Exception ex){current=null;}
						
						if(current!=null){
						    
							canBeEnabled=canBeEnabled && current.getNodeName().equals("g"); //$NON-NLS-1$
						}							
					}
					
					if(canBeEnabled){
					    
						menuItem.setEnabled(true);
						
						//adds the action listeners
						menuItem.addActionListener(ungroupListener);
						
					}else{
						
						menuItem.setEnabled(false);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}

				return super.getPopupItem(nodes);
			}
		};
		
		popupItems.add(item);
		
		return popupItems;
	}

	/**
	 * groups the nodes in the given list
	 * @param list the nodes to be grouped
	 */
	protected void group(LinkedList<Element> list){
		
		final SVGFrame frame=editor.getFrameManager().getCurrentFrame();
		
		if(list!=null && list.size()>0 && frame!=null){
			
			final Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
			
			//the selected nodes
			final LinkedList<Element> snodes=new LinkedList<Element>(list);
			
			//getting the parent node
			Node p=null;
			
			try{p=((Node)snodes.getFirst()).getParentNode();}catch (Exception ex){p=null;}
			final Node parent=p;
			
			if(doc!=null && parent!=null){

				//the list of the nodes to be grouped
				final LinkedList<Node> toBeGrouped=new LinkedList<Node>();
				
				//orders the selected nodes
				Node cur=null;

				for(cur=parent.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
				    
				    if(snodes.contains(cur)){
				        
				        toBeGrouped.add(cur);
				    }
				}

				//creates the g element
				final Element g=doc.createElementNS(doc.getDocumentElement().getNamespaceURI(),"g"); //$NON-NLS-1$
				String id=frame.getId(""); //$NON-NLS-1$
				g.setAttributeNS(null,"id",id); //$NON-NLS-1$
				
				//appends the selected nodes as children of the g element
				Runnable runnable=new Runnable(){
				    
					public void run(){

						Node current=null;
						
						for(Iterator it=toBeGrouped.iterator(); it.hasNext();){
						    
							try{current=(Node)it.next();}catch (Exception ex){current=null;}
							
							if(current!=null){
							    
								try{
									parent.removeChild(current);
								}catch(Exception ex){}
								
								g.appendChild(current);					
							}
						}
						
						//appends the g element to the root element
						parent.appendChild(g);
						
						frame.getScrollPane().getSVGCanvas().delayedRepaint();
					}
				};
				
				frame.enqueue(runnable);
			
				//creates the undo/redo action and insert it into the undo/redo stack
				if(editor.getUndoRedo()!=null){

					SVGUndoRedoAction action=new SVGUndoRedoAction(undoredogroupug){

						@Override
						public void undo(){
						    
							//removes the g element and appends the children of the g element to the root element
							for(Node current : toBeGrouped){

								if(current!=null){
								    
									try{g.removeChild(current);}catch(Exception ex){}		
									parent.appendChild(current);		
								}
							}
							
							parent.removeChild(g);
						}

						@Override
						public void redo(){
						    
							//appends the g element to the root element
							for(Node current : toBeGrouped){

								//removes the selected nodes from the root element and adds them to the g element
								if(current!=null){
								    
									try{parent.removeChild(current);}catch(Exception ex){}
									g.appendChild(current);					
								}
							}
							
							parent.appendChild(g);
						}
					};
			
					SVGSelection selection=editor.getSVGSelection();
			
					if(selection!=null){
					    
						//manages the selections and the undo/redo action list
						selection.deselectAll(frame, false, false);
						selection.addUndoRedoAction(frame, action);
						selection.handleNodeSelection(frame, g);
						selection.addUndoRedoAction(frame, new SVGUndoRedoAction(undoredogroupug){});
						selection.refreshSelection(frame);
				
					}else{
					    
						SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList(undoredogroupug);
						actionlist.add(action);
						editor.getUndoRedo().addActionList(frame, actionlist);
					}
				}
			}
		}
	}
	
	/**
	 * ungroups the nodes in the given list
	 * @param list the nodes to be grouped
	 */
	protected void ungroup(LinkedList<Element> list){
		
		final SVGFrame frame=editor.getFrameManager().getCurrentFrame();
		
		if(list!=null && list.size()>0 && frame!=null){
			
			final Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
			
		    //the selected nodes
			final LinkedList<Element> snodes=new LinkedList<Element>(list);
			
			//getting the parent node
			Node p=null;
			
			try{p=((Node)snodes.getFirst()).getParentNode();}catch (Exception ex){p=null;}
			
			final Node parent=p;
			
			if(doc!=null && parent!=null){

				//the list of the g nodes
				final LinkedList<Node> groupNodes=new LinkedList<Node>();
				
				//orders the selected nodes
				Node cur=null;

				for(cur=parent.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
				    
				    if(snodes.contains(cur)){
				        
				        groupNodes.add(cur);
				    }
				}
				
				//the map associating a group node to the list of its children before the ungroup action
				final LinkedHashMap<Node, Collection<Node>> gOldNodes=new LinkedHashMap<Node, Collection<Node>>();
				//the map assocating a g element to the list of its linked nodes
				final LinkedHashMap<Node, Collection<Node>> gNewNodes=new LinkedHashMap<Node, Collection<Node>>();
				//the action
				SVGUndoRedoAction urAction=null;

				//create the undo/redo action
				if(editor.getUndoRedo()!=null){

					urAction=new SVGUndoRedoAction(undoredogungroup){

						@Override
						public void undo(){
						    
						    Iterator it=null, it2=null;
						    Node g=null, child=null;
						    LinkedList children=null;
						    LinkedList newChildren=null;

						    for(it=gOldNodes.keySet().iterator(); it.hasNext();){
						        
						        try{
						            g=(Node)it.next();
						            children=(LinkedList)gOldNodes.get(g);
						            newChildren=(LinkedList)gNewNodes.get(g);
						        }catch (Exception ex){g=null; children=null; newChildren=null;}
						        
						        if(g!=null && children!=null && newChildren!=null){
						            
						            //removes all the children from the g element
						            while(g.hasChildNodes()){
						                
						                g.removeChild(g.getFirstChild());
						            }
						            
						            //appends all the children to the g element
						            for(it2=newChildren.iterator(); it2.hasNext();){
						                
						                try{
						                    child=(Node)it2.next();
						                }catch (Exception ex){child=null;}
						                
						                if(child!=null){
						                    
						                    parent.removeChild(child);
						                }
						            }
						            
						            //appends all the children to the g element
						            for(it2=children.iterator(); it2.hasNext();){
						                
						                try{
						                    child=(Node)it2.next();
						                }catch (Exception ex){child=null;}
						                
						                if(child!=null){

						                    g.appendChild(child);
						                }
						            }
						            
						            parent.appendChild(g);
						        }
						    }
						}
						
						@Override
						public void redo(){

						    Iterator it=null, it2=null;
						    Node g=null, child=null;
						    LinkedList children=null;

						    for(it=gNewNodes.keySet().iterator(); it.hasNext();){
						        
						        try{
						            g=(Node)it.next();
						            children=(LinkedList)gNewNodes.get(g);
						        }catch (Exception ex){g=null; children=null;}
						        
						        if(g!=null && children!=null){
						            
						            //removes all the children from the g element
						            while(g.hasChildNodes()){
						                
						                g.removeChild(g.getFirstChild());
						            }
						            
						            //appends all the children to the root element
						            for(it2=children.iterator(); it2.hasNext();){
						                
						                try{
						                    child=(Node)it2.next();
						                }catch (Exception ex){child=null;}
						                
						                if(child!=null){
						                    
						                    parent.appendChild(child);
						                }
						            }
						            
						            parent.removeChild(g);
						        }
						    }
						}
					};
				}
				
				final SVGUndoRedoAction action=urAction;
				
				//ungroups the g elements
				Runnable runnable=new Runnable(){
				    
					public void run(){
				
						Iterator it=null, it2=null;
						Node g=null, current=null;
						//the list of the children of the g element
						LinkedList<Node> gchildren=null;
						//the list of the cloned children of the g element
						LinkedList<Node> clonedChildren=null;

						for(it=groupNodes.iterator(); it.hasNext();){
						    
						    try{
						        g=(Node)it.next();
						    }catch (Exception ex){g=null;}
						    
						    if(g!=null && g.getNodeName().equals("g")){ //$NON-NLS-1$

								gchildren=new LinkedList<Node>();
								clonedChildren=new LinkedList<Node>();
						        
								for(current=g.getFirstChild(); current!=null; current=current.getNextSibling()){
								    
									if(	! current.getNodeName().startsWith(SVGToolkit.rtdaPrefix) || 
										current.getNodeName().equals(SVGToolkit.jwidgetTagName)){
										
										gchildren.add(current);
									}

									clonedChildren.add(current.cloneNode(true));
								}
								
								gOldNodes.put(g, clonedChildren);
								gNewNodes.put(g, gchildren);
	
								it2=gchildren.iterator();
								
								while(it2.hasNext()){
								    
									try{
										current=(Node)it2.next();
									}catch (Exception ex){current=null;}
									
									if(current!=null){
									    
										try{
											g.removeChild(current);
											parent.appendChild(current);
										}catch(Exception ex){}	
									}
								}
						    }
						    
						   parent.removeChild(g);
						}
						
						//inserts the undo/redo action into the undo/redo stack
						if(editor.getUndoRedo()!=null && action!=null){
						
							SVGSelection selection=editor.getSVGSelection();

							if(selection!=null){
							    
								//manages the selections and the undo/redo action list
								selection.deselectAll(frame, false, false);
								selection.addUndoRedoAction(frame, action);
								
								//selects the nodes that were before children of the g elements
							    Element child=null;
							    LinkedList children=null;

							    for(it=gNewNodes.keySet().iterator(); it.hasNext();){
							        
							        try{
							            g=(Node)it.next();
							            children=(LinkedList)gNewNodes.get(g);
							        }catch (Exception ex){g=null; children=null;}
							        
							        if(g!=null && children!=null){
							            
							            //selects the nodes
							            for(it2=children.iterator(); it2.hasNext();){
							                
							                try{
							                    child=(Element)it2.next();
							                }catch (Exception ex){child=null;}
							                
							                if(child!=null){
							                    
							                    selection.handleNodeSelection(frame, child);
							                }
							            }
							        }
							    }
													
								selection.addUndoRedoAction(frame, new SVGUndoRedoAction(undoredogungroup){});
								selection.refreshSelection(frame);
		
							}else{
							    
								SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList(undoredogungroup);
								actionlist.add(action);
								editor.getUndoRedo().addActionList(frame, actionlist);
							}
						}
					}
				};

				frame.enqueue(runnable);
			}
		}
	}
	
	/**
	 * gets the name of the module
	 * @return the name of the module
	 */
	public String getName(){
		return idgroupungroup;
	}
}
