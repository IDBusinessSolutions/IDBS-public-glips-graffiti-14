/*
 * Created on 7 avr. 2004
 *
 =============================================
                   GNU LESSER GENERAL PUBLIC LICENSE Version 2.1
 =============================================
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
package fr.itris.glips.svgeditor.selection;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.menutool.*;
import fr.itris.glips.svgeditor.resources.*;
import fr.itris.glips.svgeditor.undoredo.*;

/**
 * @author Jordi SUC
 * the class managing the selections over the canvas
 */
public class SVGSelection extends SVGModuleAdapter{

    /**
     * the ids of the module
     */
    final private String idselection="Selection", idselectall="SelectAll", iddeselectall="DeselectAll", idlock="Lock",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
									idunlock="UnLock", idregularmode="RegularMode", idgroupmenu="GroupMenu", idgroupenter="EnterGroup",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
									idgroupexit="ExitGroup"; //$NON-NLS-1$

    /**
     * the labels
     */
    protected String labelselection="", labelselect="", labelselectall="", labeldeselectall="", labellock="",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
								labelunlock="", labelregularmode="", labelgroup="", labelgroupenter="", labelgroupexit=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    /**
     * the undo/redo labels
     */
    protected String undoredoselect="", undoredodeselect="", undoredodeselectall="", undoredoselectall="",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
								undoredolock="", undoredounlock="", undoredogroupenter="", undoredogroupexit=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    /**
     * the menu items that will be inserted into the menubar
     */
    private JMenuItem selectAll, deselectAll, lock, unlock, groupEnter, groupExit;
    
    /**
     * the listeners to the menu items
     */
    private ActionListener selectAllListener, deselectAllListener, lockListener, unlockListener, groupEnterListener, groupExitListener;
    
    /**
     * the group menu
     */
    private JMenu groupMenu;

    /**
     * the tool button corresponding to a regular selection
     */
    private JToggleButton regularModeTool=null;
    
    /**
     * the listener to the regular mode tool button
     */
    private ActionListener regularModeToolListener=null;

    /**
     * a reference to the current object of this class
     */
    private final SVGSelection selection=this;
    
    /**
     * whether the selection is enabled or not
     */
    private boolean isSelectionEnabled=true;
    
    /**
     * the map associating a frame to its selection manager
     */
    private Map selectionManagers=Collections.synchronizedMap(new Hashtable());
    
    /**
     * the listeners that are registered by other modules to be notified when a selection modification occurs
     */
    private LinkedList selectionListeners=new LinkedList();

    /**
     * the color for drawing the selected area
     */
    protected final Color LINE_SELECTION_COLOR=new Color(75, 100, 200),
    									LINE_SELECTION_COLOR_HIGHLIGHT=new Color(255,255,255);
    
	/**
	 * the stroke for the selection outlines
	 */
	protected static final BasicStroke lineStroke=new BasicStroke(	1, BasicStroke.CAP_BUTT, 
	        																										BasicStroke.JOIN_BEVEL, 
	        																										0, new float[]{5, 5}, 0);
    
    /**
     * the lastly selected nodes
     */
    private final java.util.List lastSelectedNodes=Collections.synchronizedList(new LinkedList());

    /**
     * the editor
     */
    private SVGEditor editor;

    /**
     * the constructor of the class
     * @param editor the editor
     */
    public SVGSelection(SVGEditor editor){

        this.editor=editor;

        //gets the labels from the resources
        ResourceBundle bundle=SVGEditor.getBundle();

        if(bundle!=null){
            
            try{
                labelselection=bundle.getString("labelselection"); //$NON-NLS-1$
                labelselectall=bundle.getString("labelselectall"); //$NON-NLS-1$
                labeldeselectall=bundle.getString("labeldeselectall"); //$NON-NLS-1$
                labellock=bundle.getString("labellock"); //$NON-NLS-1$
                labelunlock=bundle.getString("labelunlock"); //$NON-NLS-1$
                labelgroup=bundle.getString("labelgroup"); //$NON-NLS-1$
                labelgroupenter=bundle.getString("labelgroupenter"); //$NON-NLS-1$
                labelgroupexit=bundle.getString("labelgroupexit"); //$NON-NLS-1$
                labelregularmode=bundle.getString("labelregularmode"); //$NON-NLS-1$
                undoredoselect=bundle.getString("undoredoselect"); //$NON-NLS-1$
                undoredodeselect=bundle.getString("undoredodeselect"); //$NON-NLS-1$
                undoredodeselectall=bundle.getString("undoredodeselectall"); //$NON-NLS-1$
                undoredoselectall=bundle.getString("undoredoselectall"); //$NON-NLS-1$
                undoredolock=bundle.getString("undoredolock"); //$NON-NLS-1$
                undoredounlock=bundle.getString("undoredounlock"); //$NON-NLS-1$
                undoredogroupenter=bundle.getString("undoredogroupenter"); //$NON-NLS-1$
                undoredogroupexit=bundle.getString("undoredogroupexit"); //$NON-NLS-1$
            }catch (Exception ex){}
        }

        //the icons
        final ImageIcon 	regularModeIcon=SVGResource.getIcon(idregularmode, false),
        							regularModeDisabledIcon=SVGResource.getIcon(idregularmode, true);

		//getting the icons
		ImageIcon selectAllIcon=SVGResource.getIcon("SelectAll", false), //$NON-NLS-1$
						dselectAllIcon=SVGResource.getIcon("SelectAll", true), //$NON-NLS-1$
						deselectAllIcon=SVGResource.getIcon("DeselectAll", false), //$NON-NLS-1$
						ddeselectAllIcon=SVGResource.getIcon("DeselectAll", true), //$NON-NLS-1$
						lockIcon=SVGResource.getIcon("Lock", false), //$NON-NLS-1$
						dlockIcon=SVGResource.getIcon("Lock", true), //$NON-NLS-1$
						unlockIcon=SVGResource.getIcon("Unlock", false), //$NON-NLS-1$
						dunlockIcon=SVGResource.getIcon("Unlock", true), //$NON-NLS-1$
						groupEnterIcon=SVGResource.getIcon("GroupEnter", false), //$NON-NLS-1$
						dgroupEnterIcon=SVGResource.getIcon("GroupEnter", true), //$NON-NLS-1$
						groupExitIcon=SVGResource.getIcon("GroupExit", false), //$NON-NLS-1$
						dgroupExitIcon=SVGResource.getIcon("GroupExit", true); //$NON-NLS-1$
						
        
        //creates the menu items
        selectAll=new JMenuItem(labelselectall, selectAllIcon);
        selectAll.setDisabledIcon(dselectAllIcon);
        selectAll.setAccelerator(KeyStroke.getKeyStroke("ctrl A")); //$NON-NLS-1$
        selectAll.setEnabled(false);
        
        deselectAll=new JMenuItem(labeldeselectall, deselectAllIcon);
        deselectAll.setDisabledIcon(ddeselectAllIcon);
        deselectAll.setAccelerator(KeyStroke.getKeyStroke("ctrl D")); //$NON-NLS-1$
        deselectAll.setEnabled(false);

        lock=new JMenuItem(labellock, lockIcon);
        lock.setDisabledIcon(dlockIcon);
        lock.setEnabled(false);
        
        unlock=new JMenuItem(labelunlock, unlockIcon);
        unlock.setDisabledIcon(dunlockIcon);
        unlock.setEnabled(false);
        
        groupEnter=new JMenuItem(labelgroupenter, groupEnterIcon);
        groupEnter.setDisabledIcon(dgroupEnterIcon);
        groupEnter.setAccelerator(KeyStroke.getKeyStroke("shift ctrl E")); //$NON-NLS-1$
        groupEnter.setEnabled(false);
        
        groupExit=new JMenuItem(labelgroupexit, groupExitIcon);
        groupExit.setDisabledIcon(dgroupExitIcon);
        groupExit.setAccelerator(KeyStroke.getKeyStroke("shift ctrl X")); //$NON-NLS-1$
        groupExit.setEnabled(false);
        
        //the group menu
        groupMenu=new JMenu(labelgroup);
        groupMenu.add(groupEnter);
        groupMenu.add(groupExit);

        //creates the tool items
        regularModeTool=new JToggleButton(regularModeDisabledIcon);
        regularModeTool.setEnabled(false);
        regularModeTool.setSelected(true);
        regularModeTool.setToolTipText(labelregularmode);
        
        //a listener that listens to the changes of the SVGFrames
        final ActionListener svgframeListener=new ActionListener(){

            /**
             * a listener on the selection changes
             */
            private ActionListener selectionListener=null;

            public void actionPerformed(ActionEvent e) {

            	//clears the last selection
            	lastSelectedNodes.clear();
            	
                //deals with the state of the menu items
                if(getSVGEditor().getFrameManager().getFrameNumber()>0){

                    selectAll.setEnabled(true);
                    deselectAll.setEnabled(true);
                    regularModeTool.setEnabled(true);
                    regularModeTool.setIcon(regularModeIcon);

                }else{

                    selectAll.setEnabled(false);
                    deselectAll.setEnabled(false);
                    regularModeTool.setEnabled(false);
                    regularModeTool.setIcon(regularModeDisabledIcon);
                }

                //disables the menuitems
                lock.setEnabled(false);
                unlock.setEnabled(false);
                groupEnter.setEnabled(false);
                groupExit.setEnabled(false);

                SVGSelectionManager selectionManager=null;
                Collection frames=getSVGEditor().getFrameManager().getFrames();
                
                for(Iterator it=new LinkedList(selectionManagers.values()).iterator(); it.hasNext();){
                    
                    try{selectionManager=(SVGSelectionManager)it.next();}catch (Exception ex){selectionManager=null;}

                    if(selectionManager!=null && ! frames.contains(selectionManager.getSVGFrame())){

                        selectionManagers.remove(selectionManager.getSVGFrame());
                        selectionManager.dispose();
                    }
                }

                SVGFrame frame=null;
                
                //adds the new mouse motion and key listeners
                for(Iterator it=frames.iterator(); it.hasNext();){

                    try{frame=(SVGFrame)it.next();}catch (Exception ex){frame=null;}

                    if(frame!=null && ! selectionManagers.containsKey(frame)){
                        
                        selectionManagers.put(frame, new SVGSelectionManager(selection, frame));
                    }
                }

                frame=getSVGEditor().getFrameManager().getCurrentFrame();

                if(frame!=null){

                    manageSelection();

                    //the listener to the selection changes
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

                SVGFrame currentFrame=getSVGEditor().getFrameManager().getCurrentFrame();
                
                //disables the menuitems
                deselectAll.setEnabled(false);
                lock.setEnabled(false);
                unlock.setEnabled(false);
                groupEnter.setEnabled(false);

                //getting the current parent node for the current frame
                Element parent=getCurrentParentElement(currentFrame);
                
                //handling the enablement of the groupExit menuItem
                if(parent!=null && parent.getNodeName().equals("g")){ //$NON-NLS-1$
                    
                    groupExit.setEnabled(true);
                    
                }else{
                    
                    groupExit.setEnabled(false);
                }

                Node current=null;
                String type=""; //$NON-NLS-1$

                //gets the currently selected nodes list
                Map selectedNodes=getSelectionMap(currentFrame);

                if(selectedNodes.size()>0){

                    lock.setEnabled(true);
                    deselectAll.setEnabled(true);
                    
                    //if the node is already locked
                    if(selectedNodes.size()==1){
                        
                        try{
                            current=(Node)selectedNodes.keySet().iterator().next();
                            type=(String)selectedNodes.get(current);
                            
                            if(type.equals("lock")){ //$NON-NLS-1$
                                
                                lock.setEnabled(false);
                                
                            }else if(current.getNodeName().equals("g")){ //$NON-NLS-1$
                                
                                groupEnter.setEnabled(true);
                            }
                        }catch (Exception ex){}
                    }

                    for(Iterator it=selectedNodes.keySet().iterator(); it.hasNext();){

                        try{current=(Node)it.next();}catch (Exception ex){current=null;}

                        if(current!=null){

                            type=(String)selectedNodes.get(current);

                            if(type!=null && type.equals("lock")){ //$NON-NLS-1$

                                unlock.setEnabled(true);
                                break;
                            }
                        }
                    }
                }
            }
        };

        //adds the SVGFrame change listener
        editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);

        //adds the listener on the menu items and tool items
        regularModeToolListener=new ActionListener(){

            public void actionPerformed(ActionEvent e) {

                getSVGEditor().cancelActions(true);
                
                if(e.getSource() instanceof JToggleButton){
                    
                    regularModeTool.setSelected(true);
                }
            }
        };
        
        regularModeTool.addActionListener(regularModeToolListener);

        selectAllListener=new ActionListener(){

            public void actionPerformed(ActionEvent e) {

                if(! isActing()){
                    
                    getSVGEditor().getFrameManager().getCurrentFrame().getScrollPane().getSVGCanvas().displayWaitCursor();
                    getSVGEditor().cancelActions(true);
                    selectAll(getSVGEditor().getFrameManager().getCurrentFrame(), true);
                    getSVGEditor().getFrameManager().getCurrentFrame().getScrollPane().getSVGCanvas().returnToLastCursor();
                }
            }
        };
        
        selectAll.addActionListener(selectAllListener);
        
        deselectAllListener=new ActionListener(){

            public void actionPerformed(ActionEvent e) {

                if(! isActing()){
                    
                    getSVGEditor().getFrameManager().getCurrentFrame().getScrollPane().getSVGCanvas().displayWaitCursor();
                    getSVGEditor().cancelActions(true);
                    deselectAll(getSVGEditor().getFrameManager().getCurrentFrame(), true, false);
                    getSVGEditor().getFrameManager().getCurrentFrame().getScrollPane().getSVGCanvas().returnToLastCursor();
                }
            }
        };
        
        deselectAll.addActionListener(deselectAllListener);

        lockListener=new ActionListener(){

            public void actionPerformed(ActionEvent e) {

                if(! isActing()){
                    
                    getSVGEditor().getFrameManager().getCurrentFrame().getScrollPane().getSVGCanvas().displayWaitCursor();
                    getSVGEditor().cancelActions(true);
                    lock(getSVGEditor().getFrameManager().getCurrentFrame());
                    getSVGEditor().getFrameManager().getCurrentFrame().getScrollPane().getSVGCanvas().returnToLastCursor();
                }
            }
        };
        
        lock.addActionListener(lockListener);

        unlockListener=new ActionListener(){

            public void actionPerformed(ActionEvent e) {

                if(! isActing()){
                    
                    getSVGEditor().getFrameManager().getCurrentFrame().getScrollPane().getSVGCanvas().displayWaitCursor();
                    getSVGEditor().cancelActions(true);
                    unlock(getSVGEditor().getFrameManager().getCurrentFrame());
                    getSVGEditor().getFrameManager().getCurrentFrame().getScrollPane().getSVGCanvas().returnToLastCursor();
                }
            }
        };
        
        unlock.addActionListener(unlockListener);
        
        groupEnterListener=new ActionListener(){

            public void actionPerformed(ActionEvent e) {

                if(! isActing()){
                    
                    getSVGEditor().getFrameManager().getCurrentFrame().getScrollPane().getSVGCanvas().displayWaitCursor();
                    getSVGEditor().cancelActions(true);
                    enterGroup(getSVGEditor().getFrameManager().getCurrentFrame());
                    getSVGEditor().getFrameManager().getCurrentFrame().getScrollPane().getSVGCanvas().returnToLastCursor();
                }
            }
        };
        
        groupEnter.addActionListener(groupEnterListener);
        
        groupExitListener=new ActionListener(){

            public void actionPerformed(ActionEvent e) {

                if(! isActing()){
                    
                    getSVGEditor().getFrameManager().getCurrentFrame().getScrollPane().getSVGCanvas().displayWaitCursor();
                    getSVGEditor().cancelActions(true);
                    exitGroup(getSVGEditor().getFrameManager().getCurrentFrame());
                    getSVGEditor().getFrameManager().getCurrentFrame().getScrollPane().getSVGCanvas().returnToLastCursor();
                }
            }
        };
        
        groupExit.addActionListener(groupExitListener);
    }

    /**
     * @return a map associating a menu item id to its menu item object
     */
    public Hashtable getMenuItems(){

        Hashtable menuItems=new Hashtable();
        menuItems.put(idselectall,selectAll);
        menuItems.put(iddeselectall,deselectAll);
        menuItems.put(idlock,lock);
        menuItems.put(idunlock, unlock);
        menuItems.put(idgroupmenu, groupMenu);

        return menuItems;
    }

    /**
     * @return a map associating a tool item id to its tool item object
     */
    public Hashtable getToolItems(){

        Hashtable toolItems=new Hashtable();
        toolItems.put(idregularmode, regularModeTool);

        return toolItems;
    }
    
	/**
	 * Returns the collection of the popup items
	 * @return the collection of the popup items
	 */
	public Collection getPopupItems(){
		
		LinkedList popupItems=new LinkedList();
		
		SVGPopupSubMenu subMenu=new SVGPopupSubMenu(getSVGEditor(), idgroupmenu, labelgroup, ""); //$NON-NLS-1$
		
		popupItems.add(subMenu);
		
		//creating the group enter popup item
		SVGPopupItem groupEnterItem=new SVGPopupItem(getSVGEditor(), idgroupenter, labelgroupenter, "GroupEnter"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {
				
				menuItem.setEnabled(false);
				menuItem.setAccelerator(KeyStroke.getKeyStroke("shift ctrl E")); //$NON-NLS-1$
				
				if(nodes!=null && nodes.size()==1){

					Node current=(Node)nodes.iterator().next();
                    
                    if(current!=null && current.getNodeName().equals("g")){ //$NON-NLS-1$
                        
                    	menuItem.setEnabled(true);
                    }
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(groupEnterListener);
					}
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the group exit popup item
		SVGPopupItem groupExitItem=new SVGPopupItem(getSVGEditor(), idgroupexit, labelgroupexit, "GroupExit"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes){
				
				menuItem.setEnabled(false);
				menuItem.setAccelerator(KeyStroke.getKeyStroke("shift ctrl X")); //$NON-NLS-1$
					
                SVGFrame currentFrame=getSVGEditor().getFrameManager().getCurrentFrame();
                
                //getting the current parent node for the current frame
                Element parent=getCurrentParentElement(currentFrame);
                
                //handling the enablement of the groupExit menuItem
                if(parent!=null && parent.getNodeName().equals("g")){ //$NON-NLS-1$
                    
                	menuItem.setEnabled(true);
                }
				
				//adds the action listeners
				if(menuItem.isEnabled()){
					
					menuItem.addActionListener(groupExitListener);
				}

				return super.getPopupItem(nodes);
			}
		};
		
		//adding the popup items to the sub menu
		subMenu.addPopupItem(groupEnterItem);
		subMenu.addPopupItem(groupExitItem);
		
		//creating the select all popup item
		/*SVGPopupItem selectAllItem=new SVGPopupItem(getSVGEditor(), idselectall, labelselectall, "SelectAll"){
		
			public JMenuItem getPopupItem(LinkedList nodes){

				if(getSVGEditor().getFrameManager().getFrameNumber()>0){
					
	                menuItem.setEnabled(true);

					//adds the action listeners
					menuItem.addActionListener(selectAllListener);
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		popupItems.add(selectAllItem);
		
		//creating the deselect all popup item
		SVGPopupItem deselectAllItem=new SVGPopupItem(getSVGEditor(), iddeselectall, labeldeselectall, "DeselectAll"){
		
			public JMenuItem getPopupItem(LinkedList nodes){

				if(nodes!=null && nodes.size()>0){
					
	                menuItem.setEnabled(true);

					//adds the action listeners
					menuItem.addActionListener(deselectAllListener);
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		popupItems.add(deselectAllItem);
		
		//creating the lock popup item
		SVGPopupItem lockItem=new SVGPopupItem(getSVGEditor(), idlock, labellock, "Lock"){
	
			public JMenuItem getPopupItem(LinkedList nodes){

                SVGFrame currentFrame=getSVGEditor().getFrameManager().getCurrentFrame();
                
                //gets the currently selected nodes list
                Map selectedNodes=getSelectionMap(currentFrame);

				if(selectedNodes!=null && selectedNodes.size()>0){
					
                    menuItem.setEnabled(true);

                    //if the node is already locked
                    if(selectedNodes.size()==1){

                        Node current=(Node)selectedNodes.keySet().iterator().next();
                        
                        if(current!=null){
                        	
                            String type=(String)selectedNodes.get(current);
                            
                            if(type!=null && type.equals("lock")){
                                
                                menuItem.setEnabled(false);
                            }
                        }
                    }

					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(lockListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		popupItems.add(lockItem);
		
		//creating the unlock popup item
		SVGPopupItem unlockItem=new SVGPopupItem(getSVGEditor(), idunlock, labelunlock, "Unlock"){
	
			public JMenuItem getPopupItem(LinkedList nodes){
				
                SVGFrame currentFrame=getSVGEditor().getFrameManager().getCurrentFrame();
				
                //gets the currently selected nodes list
                Map selectedNodes=new HashMap(getSelectionMap(currentFrame));
				
				if(selectedNodes!=null && selectedNodes.size()>0){
					
                    menuItem.setEnabled(false);

	                Node current=null;
	                String type="";

                    for(Iterator it=selectedNodes.keySet().iterator(); it.hasNext();){

                        try{current=(Node)it.next();}catch (Exception ex){current=null;}

                        if(current!=null){

                            type=(String)selectedNodes.get(current);

                            if(type!=null && type.equals("lock")){

                                menuItem.setEnabled(true);
                                break;
                            }
                        }
                    }

					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(unlockListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		popupItems.add(unlockItem);*/
		
		return popupItems;
	}

    /**
     * @return the editor
     */
    public SVGEditor getSVGEditor(){
        return editor;
    }

    /**
     * adds a listener listening to a selection change
     * @param listener a listener listening to a selection change
     */
    public void addSelectionListener(ActionListener listener){
        
    	if(! selectionListeners.contains(listener)){
    		
            selectionListeners.add(listener);
    	}
    }

    /**
     * removes a listener listening to a selection change
     * @param listener a listener listening to a selection change
     */
    public void removeSelectionListener(ActionListener listener){
        
        selectionListeners.remove(listener);
    }

    /**
     * notifies the listeners that the selection has changed
     * @param forceToRefresh whether the selection should be refreshed immediatly or not
     */
    public synchronized void selectionChanged(boolean forceToRefresh){

		//gets the currently selected nodes list 
        LinkedList list=getCurrentSelection(getSVGEditor().getFrameManager().getCurrentFrame());
		
		if(! lastSelectedNodes.equals(list) || forceToRefresh){
		    
		    lastSelectedNodes.clear();
		    lastSelectedNodes.addAll(list);
		    
	        for(Iterator it=new LinkedList(selectionListeners).iterator(); it.hasNext();){

	            ((ActionListener)it.next()).actionPerformed(null);
	        }
		}
    }

    /**
     * adds selection squares to the map
     * @param frame a frame
     * @param map the map of the selection squares
     */
    public void addSelectionSquares(SVGFrame frame, Hashtable map){

        if(frame!=null && map!=null){
            
            SVGSelectionManager selectionManager=null;
            
            try{
                selectionManager=(SVGSelectionManager)selectionManagers.get(frame);
            }catch (Exception ex){selectionManager=null;}

            if(selectionManager!=null){
                
                selectionManager.addSelectionSquares(map);
            }
        }
    }
    
    /**
     * @return whether an action is currently being done or not on any of the canvases
     */
    public boolean isActing(){
        
        boolean isActing=false;
        SVGSelectionManager selectionManager=null;
        
        for(Iterator it=selectionManagers.values().iterator(); it.hasNext();){
            
            try{
                selectionManager=(SVGSelectionManager)it.next();
            }catch (Exception ex){selectionManager=null;}

            if(selectionManager!=null && selectionManager.isActing()){
                
                isActing=true;
                break;
            }
        }

        return isActing;
    }

    /**
     * refreshes the selection
     * @param frame a frame
     */
    public void refreshSelection(SVGFrame frame){

        if(frame!=null){
            
            SVGSelectionManager selectionManager=null;
            
            try{
                selectionManager=(SVGSelectionManager)selectionManagers.get(frame);
            }catch (Exception ex){selectionManager=null;}

            if(selectionManager!=null){
                
                selectionManager.refreshSelection();
            }
        }
    }

    /** adds or removes a node from the selected nodes
     * @param frame the current SVGFrame
     * @param element the element that will be handled
     */
    public void handleNodeSelection(SVGFrame frame, Element element){

        if(frame!=null){
            
            SVGSelectionManager selectionManager=null;
            
            try{
                selectionManager=(SVGSelectionManager)selectionManagers.get(frame);
            }catch (Exception ex){selectionManager=null;}

            if(selectionManager!=null){
                
                selectionManager.handleNodeSelection(element, false, true, false);
            }
        }
    }

    /**
     * adds an undo/redo action to the action list
     * @param frame the current SVGFrame
     * @param action the action to be added
     */
    public void addUndoRedoAction(SVGFrame frame, SVGUndoRedoAction action){

        if(frame!=null && action!=null){
            
            SVGSelectionManager selectionManager=null;
            
            try{
                selectionManager=(SVGSelectionManager)selectionManagers.get(frame);
            }catch (Exception ex){selectionManager=null;}

            if(selectionManager!=null){
                
                selectionManager.addUndoRedoAction(action);
            }
        }
    }

    /**
     * gets the list of the currently selected nodes
     * @param frame the current SVGFrame
     * @return the list of the currently selected nodes
     */
    public LinkedList<Element> getCurrentSelection(SVGFrame frame){

        if(frame!=null){
            
            SVGSelectionManager selectionManager=null;
            
            try{
                selectionManager=(SVGSelectionManager)selectionManagers.get(frame);
            }catch (Exception ex){selectionManager=null;}

            if(selectionManager!=null){
                
                return selectionManager.getCurrentSelection();
            }
        }
        
        return null;
    }

    /**
     * gets the list of the currently selected nodes
     * @param frame the current SVGFrame
     * @return the list of the currently selected nodes
     */
    protected Map getSelectionMap(SVGFrame frame){

        Map map=new HashMap();
        
        if(frame!=null){
            
            SVGSelectionManager selectionManager=null;
            
            try{
                selectionManager=(SVGSelectionManager)selectionManagers.get(frame);
            }catch (Exception ex){selectionManager=null;}

            if(selectionManager!=null){
                
                map=selectionManager.getCurrentSelectionTypeMap();
            }
        }

        return map;
    }
    
    /**
     * returns the current parent element for the given frame (the root element of a svg document, or a g node)
     * @param frame
     * @return the current parent element for the given frame (the root element of a svg document, or a g node)
     */
    public Element getCurrentParentElement(SVGFrame frame){
        
        if(frame!=null){
            
            SVGSelectionManager selectionManager=null;
            
            try{
                selectionManager=(SVGSelectionManager)selectionManagers.get(frame);
            }catch (Exception ex){selectionManager=null;}

            if(selectionManager!=null){
                
                return selectionManager.getParentElement();
            }
        }

        return null;
    }
    
    /**
     * enters a group if the selection contains a single g node
     * @param frame
     */
    public void enterGroup(SVGFrame frame){
        
        if(frame!=null){
            
            SVGSelectionManager selectionManager=null;
            
            try{
                selectionManager=(SVGSelectionManager)selectionManagers.get(frame);
            }catch (Exception ex){selectionManager=null;}

            if(selectionManager!=null){
                
                selectionManager.enterGroup();
            }
        }
    }
    
    /**
     * exits the current edited group
     * @param frame
     */
    public void exitGroup(SVGFrame frame){
        
        if(frame!=null){
            
            SVGSelectionManager selectionManager=null;
            
            try{
                selectionManager=(SVGSelectionManager)selectionManagers.get(frame);
            }catch (Exception ex){selectionManager=null;}

            if(selectionManager!=null){
                
                selectionManager.exitGroup();
            }
        }
    }

    /**
     * deselects all the nodes contained in the svg document of the given frame
     * @param frame
     * @param pushUndoRedoAction whether an undo/redo action should be added to the stack*
     * @param executeWhenNoNodesSelected whether to execute this method even if no node is selected
     */
    public void deselectAll(SVGFrame frame, boolean pushUndoRedoAction, boolean executeWhenNoNodesSelected){
        
        if(frame!=null){
            
            SVGSelectionManager selectionManager=null;
            
            try{
                selectionManager=(SVGSelectionManager)selectionManagers.get(frame);
            }catch (Exception ex){selectionManager=null;}

            if(selectionManager!=null){
                
                selectionManager.deselectAll(pushUndoRedoAction, executeWhenNoNodesSelected);
            }
        }
    }

    /**
     * selects all the children of the root element
     * @param frame the current SVGFrame
     * @param pushUndoRedoAction whether an undo/redo action should be added to the stack
     */
    public void selectAll(SVGFrame frame, boolean pushUndoRedoAction){

        if(frame!=null){
            
            SVGSelectionManager selectionManager=null;
            
            try{
                selectionManager=(SVGSelectionManager)selectionManagers.get(frame);
            }catch (Exception ex){selectionManager=null;}

            if(selectionManager!=null){
                
                selectionManager.selectAll(pushUndoRedoAction);
            }
        }
    }

    /**
     * draws a rectangle representing the selected area while dragging
     * @param gr a graphics element
     * @param bounds the bounds of the rectangle to be drawned
     */
    public void drawSelectionGhost(Graphics gr, Rectangle2D.Double bounds){
        
        Graphics2D g=(Graphics2D)gr.create();

        if(g!=null && bounds!=null){

            //draws the new awt rectangle to be displayed
		    g=(Graphics2D)g.create();

			g.setColor(LINE_SELECTION_COLOR);
			g.setXORMode(Color.white);
			//g.setStroke(lineStroke);
			g.drawRect((int)bounds.x, (int)bounds.y, (int)bounds.width, (int)bounds.height);
			g.dispose();
        }
    }

    /**
     * locks the node of the current selection
     * @param frame the current SVGFrame
     */
    public void lock(SVGFrame frame){

        if(frame!=null){
            
            SVGSelectionManager selectionManager=null;
            
            try{
                selectionManager=(SVGSelectionManager)selectionManagers.get(frame);
            }catch (Exception ex){selectionManager=null;}

            if(selectionManager!=null){
                
                selectionManager.lock();
            }
        }
    }

    /**
     * unlocks the locked nodes of the current selection
     * @param frame the current SVGFrame
     */
    public void unlock(SVGFrame frame){

        if(frame!=null){
            
            SVGSelectionManager selectionManager=null;
            
            try{
                selectionManager=(SVGSelectionManager)selectionManagers.get(frame);
            }catch (Exception ex){selectionManager=null;}

            if(selectionManager!=null){
                
                selectionManager.unlock();
            }
        }
    }

    @Override
    public void cancelActions(){

    	if(getSVGEditor().getFrameManager().getCurrentFrame()!=null){
    		
            //resets the help information displayed
            getSVGEditor().getFrameManager().getCurrentFrame().getStateBar().setSVGInfos(""); //$NON-NLS-1$
            getSVGEditor().getFrameManager().getCurrentFrame().getStateBar().setSVGW(""); //$NON-NLS-1$
            getSVGEditor().getFrameManager().getCurrentFrame().getStateBar().setSVGH(""); //$NON-NLS-1$

            getSVGEditor().getFrameManager().getCurrentFrame().getScrollPane().getSVGCanvas().setEnableWaitCursor(true);
            
            regularModeTool.removeActionListener(regularModeToolListener);
            regularModeTool.setSelected(false);
            regularModeTool.addActionListener(regularModeToolListener);
    	}
    }

    /**
     * enables or disables the selection actions
     * @param isSelectionEnabled true to enable the selection actions
     */
    public synchronized void setSelectionEnabled(boolean isSelectionEnabled){

        this.isSelectionEnabled=isSelectionEnabled;
        regularModeTool.setSelected(isSelectionEnabled);
    }
    
    /**
     * @return true if the selection is enabled
     */
    public boolean isSelectionEnabled(){
        
        return isSelectionEnabled;
    }

    /**
     * gets the name of the module
     * @return the name of the module
     */
    public String getName(){
        return idselection;
    }
}
