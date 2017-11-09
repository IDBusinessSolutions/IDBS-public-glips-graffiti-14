/*
 * Created on 2 avr. 2004
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
package fr.itris.glips.svgeditor.undoredo;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.menutool.*;
import fr.itris.glips.svgeditor.resources.*;

/**
 * @author Jordi SUC
 *
* the class that manages the undo/redo stack that contains the actions to call when undoing or redoing
 */
public class SVGUndoRedo extends SVGModuleAdapter{

	/**
	 * the ids of the module
	 */
	final private String idundoredo="UndoRedo", idundo="Undo", idredo="Redo"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	/**
	 * the labels
	 */
	private String labelundo="", labelredo=""; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * the map associating a frame to a stack of the undo action lists for the frame
	 */
	private Map undoMap=Collections.synchronizedMap(new Hashtable());
	
	/**
	 *  the map associating a frame to a stack of the redo action lists for the frame
	 */
	private Map redoMap=Collections.synchronizedMap(new Hashtable());
	
	/**
	 * the amount of action lists the undo/redo stacks can contain
	 */
	private int stackDepth=30;

	/**
	 * the editor
	 */
	private SVGEditor editor;
	
	/**
	 * the menuitems of the undo and redo actions
	 */
	private JMenuItem undo, redo;
	
	/**
	 * the undo and redo listeners
	 */
	private ActionListener undoListener=null, redoListener=null;
	
	/**
	 * the constructor of the class
	 *@param editor the editor
	 */
	public SVGUndoRedo(SVGEditor editor){
		
		this.editor=editor;
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labelundo=bundle.getString("labelundo"); //$NON-NLS-1$
				labelredo=bundle.getString("labelredo"); //$NON-NLS-1$
			}catch (Exception ex){}
		}
		
		//getting the icons
		ImageIcon undoIcon=SVGResource.getIcon("Undo", false), //$NON-NLS-1$
						dundoIcon=SVGResource.getIcon("Undo", true), //$NON-NLS-1$
						redoIcon=SVGResource.getIcon("Redo", false), //$NON-NLS-1$
						dredoIcon=SVGResource.getIcon("Redo", true); //$NON-NLS-1$
						

		//gets the menu named "Display"
		undo=new JMenuItem(labelundo, undoIcon);
		undo.setDisabledIcon(dundoIcon);
		undo.setAccelerator(KeyStroke.getKeyStroke("ctrl Z")); //$NON-NLS-1$
		undo.setEnabled(false);
		
		redo=new JMenuItem(labelredo, redoIcon);
		redo.setDisabledIcon(dredoIcon);
		redo.setAccelerator(KeyStroke.getKeyStroke("ctrl Y")); //$NON-NLS-1$
		redo.setEnabled(false);
		
		final SVGEditor feditor=editor;
		
		//a listener that listens to the changes of the SVGFrames
		final ActionListener svgframeListener=new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				SVGFrame frame=feditor.getFrameManager().getCurrentFrame();
				Collection undoFrames=new LinkedList(undoMap.keySet());
				Collection availableFrames=new LinkedList(feditor.getFrameManager().getFrames());

				SVGFrame f=null;
				
				for(Iterator it=undoFrames.iterator(); it.hasNext();){
				    
					try{f=(SVGFrame)it.next();}catch (Exception ex){}
					
					if(f!=null && ! availableFrames.contains(f)){

						undoMap.remove(f);
						redoMap.remove(f);
					}
				}

				if(frame!=null){
				    
					//the undo and redo stacks associated with the current frame
					LinkedList undoList=null, redoList=null;
					
					try{
						undoList=(LinkedList)undoMap.get(frame);
						redoList=(LinkedList)redoMap.get(frame);
					}catch (Exception ex){undoList=null; redoList=null;}
			
					if(undoList==null){
					    
						undoList=new LinkedList();
						undoMap.put(frame, undoList);
					}
			
					if(redoList==null){
					    
						redoList=new LinkedList();
						redoMap.put(frame, redoList);
					}
				
					//changes the labels in the menu items according to the current frame
					SVGUndoRedoActionList actionList=null;
				
					if(undoList.size()>0){
					    
						try{
							actionList=(SVGUndoRedoActionList)undoList.getLast();
						}catch (Exception ex){actionList=null;}
					}
					
					//if the undo stack is not empty
					if(actionList!=null){
					    
						undo.setText(labelundo+" "+actionList.getName()); //$NON-NLS-1$
						undo.setEnabled(true);
					
					}else{
					    
						//if the undo stack is empty, the menu item is disabled
						undo.setText(labelundo);
						undo.setEnabled(false);
					}
				
					actionList=null;
				
					if(redoList.size()>0){
					    
						try{
							actionList=(SVGUndoRedoActionList)redoList.getLast();
						}catch (Exception ex){actionList=null;}
					}
					//if the redo stack is not empty
					if(actionList!=null){
					    
						redo.setText(labelredo+" "+actionList.getName()); //$NON-NLS-1$
						redo.setEnabled(true);
					
					}else{
					    
						//if the redo stack is empty, the menu item is disabled
						redo.setText(labelredo);
						redo.setEnabled(false);
					}
					
				}else{
				    //as no frame is displayed, the menu items are disabled
					undo.setText(labelundo);
					undo.setEnabled(false);
					
					redo.setText(labelredo);
					redo.setEnabled(false);
				}
			}
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);

		//creating the undo action listener
		undoListener=new ActionListener(){
		    
			public synchronized void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					final SVGFrame frame=feditor.getFrameManager().getCurrentFrame();
					
					if(frame!=null){
						
						Runnable runnable=new Runnable(){
							
							public void run() {

								undoLastAction(frame);
							}
						};
						
						frame.enqueue(runnable);
					}
			    }
			}
		};
		
		//adds the undo listener to the menuItem
		undo.addActionListener(undoListener);
		
		//creating the redo action listener
		redoListener=new ActionListener(){
		    
			public synchronized void actionPerformed(ActionEvent e) {
				
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					final SVGFrame frame=feditor.getFrameManager().getCurrentFrame();
					
					if(frame!=null){
						
						Runnable runnable=new Runnable(){
							
							public void run() {

								redoLastAction(feditor.getFrameManager().getCurrentFrame());
							}
						};
						
						frame.enqueue(runnable);
					}
			    }
			}
		};
		
		//adds the redo listener to the menuItem
		redo.addActionListener(redoListener);
	}
	
	/**
	 * @return a map associating a menu item id to its menu item object
	 */
	public Hashtable getMenuItems(){
		
		Hashtable menuItems=new Hashtable();
		menuItems.put(idundo,undo);
		menuItems.put(idredo,redo);
		
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
		
		//creating and adding the undo popup item
		SVGPopupItem undoItem=new SVGPopupItem(getSVGEditor(), idundo, labelundo, "Undo"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				//getting the current frame
				SVGFrame frame=editor.getFrameManager().getCurrentFrame();
				
				//the list of the undo actions
				LinkedList undoList=null;
				
				if(frame!=null){
					
					undoList=(LinkedList)undoMap.get(frame);
				}
				
				if(undoList!=null && undoList.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					menuItem.addActionListener(undoListener);
					
					//setting the label for the undo menu item
					menuItem.setText(labelundo+" "+((SVGUndoRedoActionList)undoList.getLast()).getName()); //$NON-NLS-1$
					
				}else{
					
					menuItem.setText(labelundo);
					menuItem.setEnabled(false);
				}

				return super.getPopupItem(nodes);
			}
		};
		
		popupItems.add(undoItem);
		
		//creating and adding the redo popup item
		SVGPopupItem redoItem=new SVGPopupItem(getSVGEditor(), idredo, labelredo, "Redo"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				//getting the current frame
				SVGFrame frame=editor.getFrameManager().getCurrentFrame();
				
				//the list of the redo actions
				LinkedList redoList=null;
				
				if(frame!=null){
					
					redoList=(LinkedList)redoMap.get(frame);
				}
				
				if(redoList!=null && redoList.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					menuItem.addActionListener(redoListener);
					
					//setting the label for the redo menu item
					menuItem.setText(labelredo+" "+((SVGUndoRedoActionList)redoList.getLast()).getName()); //$NON-NLS-1$
					
				}else{
					
					menuItem.setText(labelredo);
					menuItem.setEnabled(false);
				}

				return super.getPopupItem(nodes);
			}
		};
		
		popupItems.add(redoItem);

		return popupItems;
	}
	
	/**
	 * layout some elements in the module
	 */
	public void initialize(){
		

	}
	
	/**
	 * @return the editor
	 */
	public SVGEditor getSVGEditor() {
		return editor;
	}
	
	/**
	 * adds an action to the undo stack
	 * @param frame the current SVGFrame
	 * @param actionList  the actionList that describes what has to be done when calling the undo or redo action
	 */
	public void addActionList(SVGFrame frame, SVGUndoRedoActionList actionList){
	    
		if(frame!=null && actionList!=null){
			
			undo.setText(labelundo+" "+actionList.getName()); //$NON-NLS-1$
			undo.setEnabled(true);
			
			//the undo and redo stacks associated with the current frame
			LinkedList undoList=null, redoList=null;
			
			try{
				undoList=(LinkedList)undoMap.get(frame);
				redoList=(LinkedList)redoMap.get(frame);
			}catch (Exception ex){undoList=null; redoList=null;}
			
			if(undoList==null){
			    
				undoList=new LinkedList();
				undoMap.put(frame, undoList);
			}
			
			if(redoList==null){
			    
				redoList=new LinkedList();
				redoMap.put(frame, redoList);
			}
			
			if(undoList.size()>stackDepth){
			    
			    undoList.removeFirst();
			}
			
			undoList.addLast(actionList);
			redoList.clear();
			redo.setText(labelredo);
			redo.setEnabled(false);
		}
	}
	
	/**
	 * undoes the last action added into the undo stack
	 * @param frame the current SVGFrame
	 */
	public void undoLastAction(SVGFrame frame){
		
		if(frame!=null){
			
			//sets that the document has been modified
			frame.setModified(true);
			
			//the undo and redo stacks associated with the current frame
			LinkedList undoList=null, redoList=null;
			
			try{
				undoList=(LinkedList)undoMap.get(frame);
				redoList=(LinkedList)redoMap.get(frame);
			}catch (Exception ex){undoList=null; redoList=null;}
			
			if(undoList==null){
				
				undoList=new LinkedList();
				undoMap.put(frame, undoList);
			}
			
			if(redoList==null){
			    
				redoList=new LinkedList();
				redoMap.put(frame, redoList);
			}
		
			if(undoList.size()>0){
			    
				//gets the action list to call the undo methods
				SVGUndoRedoActionList actionList=(SVGUndoRedoActionList)undoList.getLast();
				
				//adds the action list to the redo stack
				redoList.addLast(actionList);
				redo.setText(labelredo+" "+actionList.getName()); //$NON-NLS-1$
				
				if(! redo.isEnabled()){
				    
				    redo.setEnabled(true);
				}
				
				//removes the list from the undo stack
				undoList.removeLast();
					
				if(undoList.size()==0){
				    
					undo.setText(labelundo);
					undo.setEnabled(false);
					
				}else{
				    
					undo.setText(labelundo+" "+((SVGUndoRedoActionList)undoList.getLast()).getName()); //$NON-NLS-1$
				}
				
				//calls the undo method on each action of the action list
				Iterator it=actionList.iterator();
				SVGUndoRedoAction current=null;

				for(int i=actionList.size()-1; i>=0; i--){
				    
					try{
						current=(SVGUndoRedoAction)actionList.get(i);
					}catch (Exception ex){}
					
					if(current!=null){
					    
						current.undo();
					}
				}
			}
			
			frame.getScrollPane().getSVGCanvas().delayedRepaint();
		}
	}

	/**
	 * redoes the last action added into the redo stack
	 * @param frame the current SVGFrame
	 */	
	public void redoLastAction(SVGFrame frame){
		
		if(frame!=null){
			
			//sets that the document has been modified
			frame.setModified(true);
			
			//the undo and redo stacks associated with the current frame
			LinkedList undoList=null, redoList=null;
			
			try{
				undoList=(LinkedList)undoMap.get(frame);
				redoList=(LinkedList)redoMap.get(frame);
			}catch (Exception ex){undoList=null; redoList=null;}
				
			if(undoList==null){
			    
				undoList=new LinkedList();
				undoMap.put(frame, undoList);
			}
				
			if(redoList==null){
			    
				redoList=new LinkedList();
				redoMap.put(frame, redoList);
			}
			
			if(redoList.size()>0){
			    
				//gets the action list to call the redo methods
				SVGUndoRedoActionList actionList=(SVGUndoRedoActionList)redoList.getLast();
				//adds the action list to the undo stack
				undoList.addLast(actionList);
				undo.setText(labelundo+" "+actionList.getName()); //$NON-NLS-1$
				
				if(!undo.isEnabled()){
				    
				    undo.setEnabled(true);
				}
				
				//removes the list from the redo stack
				redoList.removeLast();
				
				if(redoList.size()==0){
				    
					redo.setText(labelredo);
					redo.setEnabled(false);
					
				}else{
				    
					redo.setText(labelredo+" "+((SVGUndoRedoActionList)redoList.getLast()).getName()); //$NON-NLS-1$
				}
				
				//calls the redo method on each action of the action list
				SVGUndoRedoAction current=null;
				
				for(Iterator it=actionList.iterator(); it.hasNext();){
				    
					try{
						current=(SVGUndoRedoAction)it.next();
					}catch (Exception ex){}
					
					if(current!=null){
					    
						current.redo();
					}
				}
			}
			
			frame.getScrollPane().getSVGCanvas().delayedRepaint();
		}
	}
	
	/**
	 * gets the name of the module
	 * @return the name of the module
	 */
	public String getName(){
		return idundoredo;
	}

	/**
	 * cancels all the actions that could be running
	 */
	public void cancelActions(){
	}
}
