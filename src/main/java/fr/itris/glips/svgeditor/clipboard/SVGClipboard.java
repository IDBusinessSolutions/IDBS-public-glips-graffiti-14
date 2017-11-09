/*
 * Created on 15 avr. 2004
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
package fr.itris.glips.svgeditor.clipboard;

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
 * the class managing all the copy, paste, cut, delete actions
 */
public class SVGClipboard extends SVGModuleAdapter{
    
	/**
	 * the ids of the module
	 */
	final private String idclipboard="Clipboard", idcopy="Copy", idpaste="Paste", idcut="Cut", iddelete="Delete"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	
	/**
	 * the labels
	 */
	private String labelcopy="", labelpaste="", labelcut="", labeldelete=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	
	/**
	 * the undo/redo labels
	 */
	private String undoredopaste="", undoredocut="", undoredodelete=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	/**
	 * the editor
	 */
	private SVGEditor editor=null;
	
	/**
	 * the menu items that will be added to the menubar
	 */
	private JMenuItem copy=null, paste=null, cut=null, delete=null;

	/**
	 * the listeners the the copy, paste, cut and delete menu items
	 */
	private ActionListener copyListener=null, pasteListener=null, cutListener=null, deleteListener=null;
	
	/**
	 * the nodes that are added to the clipboard
	 */
	private final LinkedList<Element> clipboardContent=new LinkedList<Element>();
	
	/**
	 * the nodes that are currently selected
	 */
	private final LinkedList<Element> selectedNodes=new LinkedList<Element>();
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGClipboard(SVGEditor editor){
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labelcopy=bundle.getString("labelcopy"); //$NON-NLS-1$
				labelpaste=bundle.getString("labelpaste"); //$NON-NLS-1$
				labelcut=bundle.getString("labelcut"); //$NON-NLS-1$
				labeldelete=bundle.getString("labeldelete"); //$NON-NLS-1$
				undoredopaste=bundle.getString("undoredopaste"); //$NON-NLS-1$
				undoredocut=bundle.getString("undoredocut"); //$NON-NLS-1$
				undoredodelete=bundle.getString("undoredodelete"); //$NON-NLS-1$
			}catch (Exception ex){}
		}
		
		this.editor=editor;
		
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
				
				//clearing the selected nodes
				selectedNodes.clear();
				
				//disables the menuitems
				copy.setEnabled(false);
				cut.setEnabled(false);
				delete.setEnabled(false);
				
				final SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
				
				//if a selection listener is already registered on a selection module, it is removed	
				if(selection!=null && selectionListener!=null){
				    
					selection.removeSelectionListener(selectionListener);
				}

				//gets the current selection module	
				if(frame!=null){
				    
					selection=getSVGEditor().getSVGSelection();
					
					if(clipboardContent.size()>0){
					    
					    paste.setEnabled(true);
					}
					
				}else{
				    
					paste.setEnabled(false);
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
				copy.setEnabled(false);
				cut.setEnabled(false);
				delete.setEnabled(false);
				LinkedList<Element> list=null;
				
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
				    
					copy.setEnabled(true);
					cut.setEnabled(true);
					delete.setEnabled(true);
				}								
			}
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);
		
		//getting the icons
		ImageIcon copyIcon=SVGResource.getIcon("Copy", false), //$NON-NLS-1$
						dcopyIcon=SVGResource.getIcon("Copy", true), //$NON-NLS-1$
						pasteIcon=SVGResource.getIcon("Paste", false), //$NON-NLS-1$
						dpasteIcon=SVGResource.getIcon("Paste", true), //$NON-NLS-1$
						cutIcon=SVGResource.getIcon("Cut", false), //$NON-NLS-1$
						dcutIcon=SVGResource.getIcon("Cut", true), //$NON-NLS-1$
						deleteIcon=SVGResource.getIcon("Delete", false), //$NON-NLS-1$
						ddeleteIcon=SVGResource.getIcon("Delete", true); //$NON-NLS-1$
						
		
		//initializes the menuitems, the popup items and adds the listeners on them//
		
		copy=new JMenuItem(labelcopy, copyIcon);
		copy.setDisabledIcon(dcopyIcon);
		copy.setAccelerator(KeyStroke.getKeyStroke("ctrl C")); //$NON-NLS-1$
		copy.setEnabled(false);
		
		copyListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e){
			    
				getSVGEditor().cancelActions(true);
				copy();
				paste.setEnabled(true);
			}
		};
		
		copy.addActionListener(copyListener);
		
		paste=new JMenuItem(labelpaste, pasteIcon);
		paste.setDisabledIcon(dpasteIcon);
		paste.setAccelerator(KeyStroke.getKeyStroke("ctrl V")); //$NON-NLS-1$
		paste.setEnabled(false);
		
		pasteListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e){
			    
				getSVGEditor().cancelActions(true);
				
				//sets that the svg document has been modified
				getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
				
				paste();
			}
		};
		
		paste.addActionListener(pasteListener);
		
		cut=new JMenuItem(labelcut, cutIcon);
		cut.setDisabledIcon(dcutIcon);
		cut.setAccelerator(KeyStroke.getKeyStroke("ctrl X")); //$NON-NLS-1$
		cut.setEnabled(false);
		
		cutListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e){
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					//sets that the svg document has been modified
					getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					
					cut();
					paste.setEnabled(true);
			    }
			}
		};
		
		cut.addActionListener(cutListener);
		
		delete=new JMenuItem(labeldelete, deleteIcon);
		delete.setDisabledIcon(ddeleteIcon);
		delete.setAccelerator(KeyStroke.getKeyStroke("DELETE")); //$NON-NLS-1$
		delete.setEnabled(false);
		
		deleteListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e){
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					//sets that the svg document has been modified
					getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					
					delete(true);
			    }
			}
		};
		
		delete.addActionListener(deleteListener);
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
		menuItems.put(idcopy,copy);
		menuItems.put(idpaste,paste);
		menuItems.put(idcut,cut);
		menuItems.put(iddelete,delete);
		
		return menuItems;
	}
	
	@Override
	public Collection<SVGPopupItem> getPopupItems() {

		LinkedList<SVGPopupItem> popupItems=new LinkedList<SVGPopupItem>();
		
		//creating the copy popup item
		SVGPopupItem item=new SVGPopupItem(getSVGEditor(), idcopy, labelcopy, "Copy"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {
				
				if(nodes!=null && nodes.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					menuItem.addActionListener(copyListener);
					
				}else{
					
					menuItem.setEnabled(false);
				}

				return super.getPopupItem(nodes);
			}
		};
		
		popupItems.add(item);
		
		//creating the paste popup item
		item=new SVGPopupItem(getSVGEditor(), idpaste, labelpaste, "Paste"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {
				
				if(clipboardContent.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					menuItem.addActionListener(pasteListener);
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		popupItems.add(item);
		
		//creating the cut popup item
		item=new SVGPopupItem(getSVGEditor(), idcut, labelcut, "Cut"){ //$NON-NLS-1$
			
			public JMenuItem getPopupItem(LinkedList nodes) {
				
				if(nodes!=null && nodes.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					menuItem.addActionListener(cutListener);
					
				}else{
					
					menuItem.setEnabled(false);
				}

				return super.getPopupItem(nodes);
			}
		};
		
		popupItems.add(item);
		
		//creating the delete popup item
		item=new SVGPopupItem(getSVGEditor(), iddelete, labeldelete, "Delete"){ //$NON-NLS-1$
			
			public JMenuItem getPopupItem(LinkedList nodes) {
				
				if(nodes!=null && nodes.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					menuItem.addActionListener(deleteListener);
					
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
	 * @return a map associating a tool item id to its tool item object
	 */
	public Hashtable getToolItems(){
		
		return null;
	}
	
	/**
	 * copies the selected nodes of the current frame into the clipboard
	 */
	public synchronized void copy(){
	    
	    clipboardContent.clear();
	    Node clonedNode=null;
		
		//orders the nodes in the list and clones them
		for(Element cur : new LinkedList<Element>(selectedNodes)){

			if(cur!=null){
			    
			    clonedNode=getSVGEditor().getSVGToolkit().getClonedNodeWithoutUseNodes(cur);
			    
			    if(clonedNode!=null){
			        
				    clipboardContent.add((Element)clonedNode);
			    }
			}
		}
	}
	
	/**
	 * pastes the copied nodes
	 */
	public void paste(){
		
		final SVGFrame frame=editor.getFrameManager().getCurrentFrame();
		
		if(clipboardContent.size()>0 && frame!=null){
			
			final SVGSelection selection=editor.getSVGSelection();
			Element parent=null;

			if(selection!=null){
			    
			    parent=selection.getCurrentParentElement(frame);
			}

			if(parent!=null){
			    
			    final Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
				
			    //the list of the pasted nodes
			    final LinkedList pastedNodes=new LinkedList();
			    
				//clones the nodes
				final LinkedList pasteList=new LinkedList(clipboardContent);
				Node current=null;
				String sid=""; //$NON-NLS-1$
				
				for(Iterator it=pasteList.iterator(); it.hasNext();){
				    
					current=(Node)it.next();
					
					if(current!=null && current instanceof Element){
					    
						sid=frame.getId(""); //$NON-NLS-1$
						((Element)current).setAttribute("id", sid); //$NON-NLS-1$
					}
				}
				
				final Element fparent=parent;

				//the runnable
				Runnable runnable=new Runnable(){
				    
					public void run(){
					    
                        //the defs node
                        final Element defs=frame.getDefsElement();
                        
					    //the list of the resources used by the pasted nodes
					    LinkedList resNodes=new LinkedList();
					    
					    //the list of the resources imported into the current document
					    final LinkedList usedResourceNodes=new LinkedList();

						Iterator it, it2, it3;
						Element current=null;
						LinkedList resourceNodes=null;
			            Element res=null;
			            Node node=null;

						for(it=pasteList.iterator(); it.hasNext();){
						    
							try{
								current=(Element)it.next();
							}catch (Exception ex){current=null;}

							if(current!=null){
							    
						        //getting all the resource nodes used by this node
						        resourceNodes=getSVGEditor().getSVGToolkit().getResourcesUsedByNode((Element)current, true);

							    //if the copied node does not belong to this svg document
							    if(! current.getOwnerDocument().equals(doc)){

						            //for each resource node, check if it is contained in the list of the resources used by the copied nodes
						            for(it2=resourceNodes.iterator(); it2.hasNext();){
						                
						                try{res=(Element)it2.next();}catch (Exception ex){res=null;}
						                
						                if(res!=null && ! resNodes.contains(res)){
						                    
						                    resNodes.add(res);
						                }
						            }
							    }
							    
								current=(Element)doc.importNode(current, true);
								pastedNodes.add(current);
							}
						}
						
						String resId="", newId="", style=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						
						//adds the resource nodes to the defs element
						for(it=resNodes.iterator(); it.hasNext();){
						    
						    try{
						        res=(Element)doc.importNode((Element)it.next(), true);
						        resId=res.getAttribute("id"); //$NON-NLS-1$
						    }catch (Exception ex){res=null; resId=null;}
						    
						    if(res!=null && resId!=null){

						        if(! frame.checkId(resId)){
						            
						            //creating the new id
						            newId=frame.getId(resId);
						            
						            //modifying the id of the resource
						            res.setAttribute("id", newId); //$NON-NLS-1$
						            
						            //for each pasted node, modifies the name of the resource they use
						            for(it2=pastedNodes.iterator(); it2.hasNext();){
						                
						                try{
						                    current=(Element)it2.next();
						                }catch (Exception ex){current=null;}
						                
						                if(current!=null){
						                    
						                    style=current.getAttribute("style"); //$NON-NLS-1$

						                    if(style!=null && style.indexOf("#".concat(resId))!=-1){ //$NON-NLS-1$
						                        
						                        style=style.replaceAll("#".concat(resId)+"[)]", "#".concat(newId)+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						                        current.setAttribute("style", style); //$NON-NLS-1$
						                        frame.addNodeUsingResource(newId, current);
						                    }
						                    
						                    //modifies the nodes of the subtree under the pasted node
						                    for(it3=new NodeIterator(current); it3.hasNext();){
						                    	
						                    	node=(Node)it3.next();
						                    	
						                    	if(    node!=null && ! node.equals(current) && 
						                    	        node instanceof Element && ((Element)node).hasAttribute("style")){ //$NON-NLS-1$
						                    		
								                    style=((Element)node).getAttribute("style"); //$NON-NLS-1$

								                    if(style!=null && style.indexOf("#".concat(resId))!=-1){ //$NON-NLS-1$
								                        
								                        style=style.replaceAll("#".concat(resId)+"[)]", "#".concat(newId)+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
								                        ((Element)node).setAttribute("style", style); //$NON-NLS-1$
								                        frame.addNodeUsingResource(newId, node);
								                    }
						                    	}
						                    }
						                }
						            }
						        }
						        
						        //adding the resource node to the defs element and the list of the resource nodes
						        defs.appendChild(res);
						        usedResourceNodes.add(res);
						    }
						}
						
						//appends the nodes to their parent and registers those which use resources
						//checks if the sub tree of the pasted nodes require a specific name space
						Node cur=null;
						NodeIterator nit=null;
						String prefix="", nsp=""; //$NON-NLS-1$ //$NON-NLS-2$
						int pos=0;
						HashMap<String, String> nameSpaces=new HashMap<String, String>(SVGEditor.requiredNameSpaces);

						for(it=pastedNodes.iterator(); it.hasNext();){
						    
						    try{
						        current=(Element)it.next();
						    }catch (Exception ex){current=null;}
						    
						    if(current!=null){
						        
								fparent.appendChild(current);
						        frame.registerUsedResource(current);
						        
						        nit=new NodeIterator(current);

						        //checks if the nodes under the pasted nodes use a specific name space
						        do{

						        	if(nit.hasNext()){
						        		
						        		cur=nit.next();
						        		
						        	}else{
						        		
								        cur=current;
						        	}
						        	
						        	if(cur!=null && cur instanceof Element){

						        		pos=cur.getNodeName().indexOf(":"); //$NON-NLS-1$
						        		
						        		if(pos!=-1){
						        			
						        			prefix=cur.getNodeName().substring(0, pos);
						        			nsp=nameSpaces.get(prefix);
						        			
						        			if(nsp!=null){
						        				
						        				SVGToolkit.checkXmlns(cur.getOwnerDocument(), prefix, nsp);
						        				nameSpaces.remove(prefix);
						        			}
						        		}
						        	}
						        	
						        }while(nit.hasNext());
						    }
						}
						
						//adds the undo/redo action list
						if(editor.getUndoRedo()!=null){
						    
							final SVGFrame frm=frame;
						
							SVGUndoRedoAction action=new SVGUndoRedoAction(undoredopaste){

								public void undo(){
								    
									Iterator it;
									Node current=null;
									
									//removes the added children from the parent node
									for(it=pastedNodes.iterator(); it.hasNext();){
									    
										try{current=(Node)it.next();}catch (Exception e){current=null;}
										
										if(current!=null){
										    
										    fparent.removeChild(current);
										    
											//unregister the current node to the used resources map if it uses a resource
											frame.unregisterAllUsedResource(current);
										}
									}
									
									//removes the added resources
									for(it=usedResourceNodes.iterator(); it.hasNext();){
									    
										try{current=(Node)it.next();}catch (Exception e){current=null;}
										
										if(current!=null){
										    
										    defs.removeChild(current);
										}
									}
									
									//refreshing the properties and the resources frame
									getSVGEditor().getSVGToolkit().forceReselection();
									getSVGEditor().getFrameManager().frameChanged();
								}

								public void redo(){
								    
									Iterator it;
									Node current=null;
								    
									//appends the resources
									for(it=usedResourceNodes.iterator(); it.hasNext();){
									    
										try{current=(Node)it.next();}catch (Exception e){current=null;}
										
										if(current!=null){
										    
										    defs.appendChild(current);
										}
									}
									
									//appends the children
									for(it=pastedNodes.iterator(); it.hasNext();){
									    
										try{current=(Node)it.next();}catch (Exception e){current=null;}
										
										if(current!=null){
										    
										    fparent.appendChild(current);
										    
											//registers the current node to the used resources map if it uses a resource
											frame.registerUsedResource(current);
										}
									}
									
									//refreshing the properties and the resources frame
									getSVGEditor().getSVGToolkit().forceReselection();
									getSVGEditor().getFrameManager().frameChanged();
								}
							};
						
							if(selection!=null && ! selection.isActing()){

							    selection.deselectAll(frame, false, true);  
								selection.addUndoRedoAction(frame, action);
								
								for(it=pastedNodes.iterator(); it.hasNext();){
								    
								    try{node=(Node)it.next();}catch (Exception ex){node=null;}
									
								    if(node!=null && node instanceof Element){
								        
								        selection.handleNodeSelection(frame, (Element)node);	
								    }			
								}
								
								selection.addUndoRedoAction(frame, new SVGUndoRedoAction(undoredopaste){});
								selection.refreshSelection(frame);
							
							}else{
							    
								//creates the undo/redo list and adds the action to it					
								SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList(undoredopaste);
								actionlist.add(action);
								editor.getUndoRedo().addActionList(frame, actionlist);
							}
						}
						
						//refreshing the resources frame
						getSVGEditor().getFrameManager().frameChanged();
					}
				};
				
				//appends the nodes to the root element with the correct id
				frame.enqueue(runnable);
			}
		}
	}
	
	/**
	 * cuts the current selection
	 */
	public void cut(){
	    
		//copies the nodes
		copy();
		
		//removes the nodes
		delete(false);
	}
	
	/**
	 * deletes the selected nodes
	 * @param isDelete true if it is used in the delete action
	 * 								false if it is used in the cut action
	 */
	public void delete(boolean isDelete){
		
		final SVGFrame frame=editor.getFrameManager().getCurrentFrame();
		
		if(frame!=null && selectedNodes.size()>0){
			
			final LinkedList deletedlist=new LinkedList(selectedNodes);
			
			SVGSelection selection=editor.getSVGSelection();
			Node parent=null;
			
			if(selection!=null){
			    
				parent=selection.getCurrentParentElement(frame);
			}
			
			Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
			final Node fparent=parent;
			
			if(fparent!=null){

				Node current=null;
				Runnable runnable=new Runnable(){
				    
					public void run(){
						
						Node current=null;

						for(Iterator it=deletedlist.iterator(); it.hasNext();){
						    
							try{
								current=(Node)it.next();
							}catch (Exception e){current=null;}
							
							if(current!=null && current.getParentNode()!=null && current.getParentNode().equals(fparent)){
							    
							    fparent.removeChild(current);
								
								//unregister the current node to the used resources map if it uses a resource
								frame.unregisterAllUsedResource(current);
							}
						}
					}
				};
				
				frame.enqueue(runnable);

				//adds the undo/redo action list			
				if(editor.getUndoRedo()!=null){
			
					final SVGFrame frm=frame;
			
					String undoredo=""; //$NON-NLS-1$
					
					if(isDelete){
					    
					    undoredo=undoredodelete;
					    
					}else{
					    
					    undoredo=undoredocut;
					}

					SVGUndoRedoAction action=new SVGUndoRedoAction(undoredo){

						public void undo(){
						    
							//appends the children to the root element						
							Node current=null;
							
							for(Iterator it=deletedlist.iterator(); it.hasNext();){
							    
								try{current=(Node)it.next();}catch (Exception e){current=null;}
								
								if(current!=null){
								    
								    fparent.appendChild(current);
								    
									//unregisters the current node to the used resources map if it uses a resource
									frame.registerUsedResource(current);
								}
							}
						}

						public void redo(){
						    
							//removes the children from the root element						
							Node current=null;
							
							for(Iterator it=deletedlist.iterator(); it.hasNext();){
							    
								try{current=(Node)it.next();}catch (Exception e){current=null;}
								
								if(current!=null && current.getParentNode()!=null && current.getParentNode().equals(fparent)){
								    
								    fparent.removeChild(current);
									
									//register the current node to the used resources map if it uses a resource
									frame.unregisterAllUsedResource(current);
								}
							}
						}
					};
			
					if(selection!=null){
					    
					    selection.deselectAll(frame, false, true);
						selection.addUndoRedoAction(frame, action);
						selection.addUndoRedoAction(frame, new SVGUndoRedoAction(undoredo){});
						selection.refreshSelection(frame);

					}else{
					    
						//creates the undo/redo list and adds the action to it					
						SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList(undoredo);
						actionlist.add(action);
						editor.getUndoRedo().addActionList(frame, actionlist);
					}
				}
			}
		}
	}

	/**
	 * gets the name of the module
	 * @return the name of the module
	 */
	public String getName(){
	    
		return idclipboard;
	}

}
