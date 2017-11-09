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
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.shape.*;
import fr.itris.glips.svgeditor.undoredo.*;

/**
 * @author jordi
 * the class handling the selections within a canvas
 * 
 * @author Jordi SUC
 */
public class SVGSelectionManager{
    
    /**
     * the selection module
     */
    private SVGSelection selection=null;
    
    /**
     * the colors used to draw the area out of a parent group node
     */
    private final Color outOfParentAreaColor=new Color(100, 100, 100, 50), outOfParentAreaBorderColor=new Color(100, 100, 100);
    
    /**
     * the frame linked with this selection manager
     */
    private SVGFrame frame=null;
    
    /**
     * the undo/redo list that will be added to the undo/redo stack
     */
    private SVGUndoRedoActionList undoRedoList=null;
    
    /**
     * the map associating the name of a module to a linked list of nodes that are handled by the module
     */
    private Map selectedItemsByModule=new Hashtable();
    
    /**
     * the map of the selected nodes associating a node to its type
     */
    private Map currentSelectionType=new LinkedHashMap();
    
    /**
     * the map associating a node to the linked list of the selection square objects
     */
    private final Map selectionSquares=new Hashtable();
    
    /**
     * the list of the locked nodes
     */
    private LinkedList lockedNodes=new LinkedList();
    
    /**
     * the boolean used to refresh the selection
     */
    private boolean shouldRefresh=false;
    
    /**
     * the current cursor
     */
    private Cursor currentCursor=null;

    /**
     * the selection listener
     */
    private SVGSelectionListener selectionListener=null;
    
    /**
     * the parent of the nodes that could be selected
     */
    private Element parentElement=null;
    
    /**
     * an instance of this class
     */
    private SVGSelectionManager selectionManager=this;
    
    /**
     * the refresh manager
     */
    private Thread refreshManager=null;
    
    /**
     * the paint listener for the selection
     */
    private CanvasPaintListener paintListener=null;
    
    /**
     * whether the refresh manager should keep on running
     */
    private boolean keepRunning=true;
    
    /**
     * the constructor of the class
     * @param selection the selection module
     * @param frame a frame
     */
    public SVGSelectionManager(SVGSelection selection, SVGFrame frame){

        this.selection=selection;
        this.frame=frame;
        
        //the parent
        Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
        
        if(doc!=null){
            
            parentElement=doc.getDocumentElement();
        }
        
        //adding the paint listener that will paint the area 
        paintListener=new CanvasPaintListener(){

            public void paintToBeDone(Graphics g) {

                Graphics2D g2=(Graphics2D)g;
                
                if(getParentElement()!=null && getParentElement().getNodeName().equals("g")){ //$NON-NLS-1$
                    
                    //the bounds of the parent element
                    Rectangle2D parentBounds=selectionManager.frame.getNodeGeometryBounds(getParentElement());
                    Rectangle2D.Double dparentBounds=new Rectangle2D.Double(parentBounds.getX(), parentBounds.getY(), parentBounds.getWidth(), parentBounds.getHeight()),
                    								scaledParentBounds=getSVGFrame().getScaledRectangle(dparentBounds, false);
                    
                    //the size of the canvas
                    Dimension canvasSize=selectionManager.frame.getScrollPane().getSVGCanvas().getScaledCanvasSize();
                    
                    //the area to be painted
                    Area area=new Area(new Rectangle(0, 0, canvasSize.width, canvasSize.height));
                    area.subtract(new Area(new Rectangle((int)scaledParentBounds.x, (int)scaledParentBounds.y, (int)scaledParentBounds.width, (int)scaledParentBounds.height)));
                    
                    //painting the area
                    g.setColor(outOfParentAreaColor);
                    g2.fill(area);
                    
                    g.setColor(outOfParentAreaBorderColor);
                    g2.draw(area);
                }
            }
        };
        
        frame.getScrollPane().getSVGCanvas().addLayerPaintListener(SVGCanvas.BOTTOM_LAYER, paintListener, false);
        
        //the mouse and key listeners
        selectionListener=new SVGSelectionListener(this);
        
        frame.getScrollPane().getSVGCanvas().addMouseListener(selectionListener);
        frame.getScrollPane().getSVGCanvas().addMouseMotionListener(selectionListener);
        frame.getScrollPane().getSVGCanvas().addKeyListener(selectionListener);
        
        //the refresh manager
        refreshManager=new Thread(){

        	@Override
            public void run() {

                while(keepRunning){

                    try{sleep(200);}catch (Exception ex){}

                    if(shouldRefresh){

					    SwingUtilities.invokeLater(new Runnable(){

	                        public void run() {

	    					    redrawSelectionMethod();
	                        }  
					    });

                        synchronized(this){shouldRefresh=false;}
                    }
                }
            }
        };

        refreshManager.start();
    }
    
    /**
     * disposes this selection manager
     */
    protected void dispose(){
        
        if(selectionListener!=null){

        	synchronized(this){
        		
        		keepRunning=false;
        	}

            frame.getScrollPane().getSVGCanvas().removeMouseListener(selectionListener);
            frame.getScrollPane().getSVGCanvas().removeMouseMotionListener(selectionListener);
            frame.getScrollPane().getSVGCanvas().removeKeyListener(selectionListener);
            
            selectedItemsByModule.clear();
            currentSelectionType.clear();
            selectionSquares.clear();
            lockedNodes.clear();
            
            if(undoRedoList!=null){
            	
            	undoRedoList.clear();
            }
        }
    }
    
    /**
     * @return whether an action is currently being done or not
     */
    public boolean isActing(){
        
        return selectionListener.isActing();
    }
    
    /**
     * @return the SVGFrame containing the canvas
     */
    protected SVGFrame getSVGFrame(){
        return frame;
    }

    /**
     * @return Returns the parentElement.
     */
    public Element getParentElement() {
        return parentElement;
    }

    /**
     * @return Returns the selection.
     */
    protected SVGSelection getSVGSelection() {
        return selection;
    }
    
    /**
     * sets the parent element
     * @param parentElement a parent element
     */
    public synchronized void setParentElement(Element parentElement){
        
        this.parentElement=parentElement;
    }
    
    /**
     * enters the group that is the single g selected node
     */
    public void enterGroup(){
        
        LinkedList currentSelection=new LinkedList(currentSelectionType.keySet());

        if(currentSelection.size()==1){
            
            final Element lastParent=getParentElement();
            
            //getting the selected g node
            Element currentParent=null;
            
            try{
                currentParent=(Element)currentSelection.getFirst();
            }catch (Exception ex){currentParent=null;}
            
            if(currentParent!=null){

                //sets the new parent
                setParentElement(currentParent);
                
                final Element newParentElement=currentParent;
                
                //adding the undo/redo action
                if(getSVGSelection().getSVGEditor().getUndoRedo()!=null){
                    
                    if(undoRedoList==null){
                        
                        undoRedoList=new SVGUndoRedoActionList(getSVGSelection().undoredogroupenter);
                    }
                    
                    //adding the undo/redo action
                    undoRedoList.addLast(new SVGUndoRedoAction(getSVGSelection().undoredogroupenter){

        	            public void undo() {
        	                
        	                setParentElement(lastParent);
        	                redrawSelection();
        	            }

                        public void redo() {
                            
                            setParentElement(newParentElement);
                            redrawSelection();
                        }
                    });
                    
                    undoRedoList.setName(getSVGSelection().undoredogroupenter);
                }

                //deselects all the nodes
                deselectAll(false, false);
                
                refreshSelection();
            }
        }
    }
    
    /**
     * enters the group that is the single g selected node
     */
    public void exitGroup(){

        final Element lastParent=getParentElement();
        
        if(lastParent!=null){
            
            //getting the parent element of the last parent element
            Element currentParent=null;
            
            try{
                currentParent=(Element)lastParent.getParentNode();
            }catch (Exception ex){}

            if(currentParent!=null){

                //sets the new parent
                setParentElement(currentParent);
                
                final Element newParentElement=currentParent;
                
                //adding the undo/redo action
                if(getSVGSelection().getSVGEditor().getUndoRedo()!=null){
                    
                    if(undoRedoList==null){
                        
                        undoRedoList=new SVGUndoRedoActionList(getSVGSelection().undoredogroupexit);
                    }
                    
                    //adding the undo/redo action
                    undoRedoList.addLast(new SVGUndoRedoAction(getSVGSelection().undoredogroupexit){

        	            public void undo() {
        	                
        	                setParentElement(lastParent);
        	                redrawSelection();
        	            }

                        public void redo() {
                            
                            setParentElement(newParentElement);
                            redrawSelection();
                        }
                    });
                    
                    undoRedoList.setName(getSVGSelection().undoredogroupexit);
                }
                
                //deselects all the nodes
                deselectAll(false, false);
                

                refreshSelection();
            }
        }
    }
    
    /**
     * sets the cursor to be displayed on the canvas
     * @param point a point on the canvas
     */
    protected void setCursor(Point2D.Double point){

        SVGSelectionSquare sq=getSelectionSquare(point);
        SVGCanvas canvas=frame.getScrollPane().getSVGCanvas();
        
        if(sq!=null){

            //if the selection square is not null, sets the canvas cursor with the cursor associated with the square
            currentCursor=sq.getCursor();
            canvas.setSVGCursor(currentCursor);

        }else{
            
            //if the selection square is null, 
            //if the node (on which the mouse event has been done) is selected, 
            //sets the new cursor, otherwise sets the default cursor
            Node node=frame.getNodeAt(parentElement, point);

            if(node!=null && isSelected(node) && ! isLocked(node)){

                currentCursor=getSVGSelection().getSVGEditor().getCursors().getCursor("translate"); //$NON-NLS-1$

            }else{
                
                currentCursor=getSVGSelection().getSVGEditor().getCursors().getCursor("default"); //$NON-NLS-1$
            }
            
            canvas.setSVGCursor(currentCursor);
        }
    }

    /**
     * adds the selection squares contained in the map to the selection squares
     * @param table the map associating a node to selection squares
     */
    public void addSelectionSquares(Hashtable table){

        if(table!=null){
            
            selectionSquares.putAll(table);
        }
    }

    /**
     * adds an undo/redo action to the action list
     * @param action the action to be added
     */
    public void addUndoRedoAction(SVGUndoRedoAction action){

        if(action!=null && getSVGSelection().getSVGEditor().getUndoRedo()!=null){

            if(undoRedoList==null){
                
                undoRedoList=new SVGUndoRedoActionList(action.getName());
            }

            undoRedoList.addLast(action);
            undoRedoList.setName(action.getName());
        }
    }
    
    /**
     * selects the given element that is not already selected, does not handle undo/redos
     * @param element a non selected element
     */
    protected void select(Element element){
    	
    }

    /** adds or removes a node from the selected nodes
     * @param element the element that will be treated
     * @param isMultiSelectionEnabled true if the multi selection is enabled
     * @param enableUndoRedoAction the boolean telling if an undo/redo action should be added for this method
     * @param alwaysSelect whether to always select the given node
     */
    public void handleNodeSelection(Element element, boolean isMultiSelectionEnabled, boolean enableUndoRedoAction, boolean alwaysSelect){

        if(		element!=null && ! element.getNodeName().equals("svg") &&  //$NON-NLS-1$
                ! element.equals(element.getOwnerDocument().getDocumentElement())){

            final Element felement=element;
            
            //gets the list of the selected nodes managed by one module
            LinkedList list=(LinkedList)selectedItemsByModule.get(getAssociatedModuleName(element));
            
            if(list==null){
                
                list=new LinkedList();
            }

            //if the list does not contain a node, the node is added, otherwise, it is removed
            if(alwaysSelect || ! isMultiSelectionEnabled || (isMultiSelectionEnabled && ! list.contains(element))){

                //sets the selection level
                String type="default"; //$NON-NLS-1$
                
                if(! list.contains(element)){
                    
                    list.add(element);
                    
                }else{
                    
                    type=(String)currentSelectionType.get(element);
                }
                
                selectedItemsByModule.put(getAssociatedModuleName(element), list);

                //gets the default first selection level for the node
                SVGShape shape=getShapeModule(getAssociatedModuleName(element));

                if(shape!=null && ! isLocked(element)){

                    type=shape.getNextLevel(type);
                    
                }else if(isLocked(element)){
                    
                    type="lock"; //$NON-NLS-1$
                }

                currentSelectionType.put(element, type);

                //create the undo/redo action and insert it into the undo/redo stack
                if(enableUndoRedoAction && getSVGSelection().getSVGEditor().getUndoRedo()!=null){

                    final String ftype=type;
                    
                    SVGUndoRedoAction action=new SVGUndoRedoAction(getSVGSelection().undoredoselect){

                        public void undo(){
                            
                            //gets the list of the selected nodes managed by one module
                            LinkedList list=(LinkedList)selectedItemsByModule.get(getAssociatedModuleName(felement));
                            
                            if(list==null){
                                
                                list=new LinkedList();
                            }

                            //removes the node from the list
                            list.remove(felement);
                            
                            //removes the list from the map if it is empty
                            if(list.size()==0){
                                
                                selectedItemsByModule.remove(getAssociatedModuleName(felement));
                            }
                            
                            //removes the node from the map of the types
                            currentSelectionType.remove(felement);
                        }

                        public void redo(){
                            
                            //gets the list of the selected nodes managed by one module
                            LinkedList list=(LinkedList)selectedItemsByModule.get(getAssociatedModuleName(felement));
                            
                            if(list==null){
                                
                                list=new LinkedList();
                            }

                            //removes the node from the list
                            list.add(felement);
                            
                            //removes the list from the map if it is empty
                            if(list.size()==1){
                                
                                selectedItemsByModule.put(getAssociatedModuleName(felement), list);
                            }
                            
                            //removes the node from the map of the types
                            currentSelectionType.put(felement, ftype);
                        }
                    };
                    
                    //creates a undoredo list into which the action will be inserted
                    if(undoRedoList==null){
                        
                        undoRedoList=new SVGUndoRedoActionList(getSVGSelection().undoredoselect);
                    }
                    
                    undoRedoList.add(action);
                    undoRedoList.setName(getSVGSelection().undoredoselect);
                }

            }else if(isMultiSelectionEnabled && list.contains(element)){

                list.remove(element);
                
                //removes this list from the map if it is empty
                if(list.size()==0){
                    
                    selectedItemsByModule.remove(getAssociatedModuleName(element));
                }
                
                //getting the type associated with this node
                final String type=(String)currentSelectionType.get(element);
                
                currentSelectionType.remove(element);

                //create the undo/redo action and insert it into the undo/redo stack
                if(enableUndoRedoAction && getSVGSelection().getSVGEditor().getUndoRedo()!=null){

                    SVGUndoRedoAction action=new SVGUndoRedoAction(getSVGSelection().undoredodeselect){

                        public void undo() {
                            
                            //gets the list of the selected nodes managed by one module
                            LinkedList list=(LinkedList)selectedItemsByModule.get(getAssociatedModuleName(felement));
                            
                            if(list==null){
                                
                                list=new LinkedList();
                            }

                            //removes the node from the list
                            list.add(felement);
                            
                            //removes the list from the map if it is empty
                            if(list.size()==1){
                                
                                selectedItemsByModule.put(getAssociatedModuleName(felement), list);
                            }
                            
                            //removes the node from the map of the types
                            currentSelectionType.put(felement, type);
                        }

                        public void redo(){
                            
                            //gets the list of the selected nodes managed by one module
                            LinkedList list=(LinkedList)selectedItemsByModule.get(getAssociatedModuleName(felement));
                            
                            if(list==null){
                                
                                list=new LinkedList();
                            }

                            //removes the node from the list
                            list.remove(felement);
                            
                            //removes the list from the map if it is empty
                            if(list.size()==0){
                                
                                selectedItemsByModule.remove(getAssociatedModuleName(felement));
                            }
                            
                            //removes the node from the map of the types
                            currentSelectionType.remove(felement);
                        }
                    };

                    //creates a undoredo list into which the action will be inserted
                    if(undoRedoList==null){
                        
                        undoRedoList=new SVGUndoRedoActionList(getSVGSelection().undoredodeselect);
                    }
                    
                    undoRedoList.addLast(action);
                    undoRedoList.setName(getSVGSelection().undoredodeselect);
                }
            }
        }
    }

    /**
     * refreshes the hashtable of the selected items, and draws the selections
     */
    public void refreshSelection(){

        //adds the current undo/redo list into the undo/redo stack
        if(getSVGSelection().getSVGEditor().getUndoRedo()!=null && undoRedoList!=null && undoRedoList.size()>0){

            SVGUndoRedoAction actionFirst=new SVGUndoRedoAction(""){ //$NON-NLS-1$

                public void undo() {

                    redrawSelection();
                    getSVGSelection().selectionChanged(false);
                }

                public void redo(){

                }
            };
            
            SVGUndoRedoAction actionLast=new SVGUndoRedoAction(""){ //$NON-NLS-1$

                public void undo() {

                }

                public void redo(){

                    redrawSelection();
                    getSVGSelection().selectionChanged(false);
                }
            };

            undoRedoList.addFirst(actionFirst);
            undoRedoList.addLast(actionLast);
            getSVGSelection().getSVGEditor().getUndoRedo().addActionList(frame, undoRedoList);
            undoRedoList=null;
        }

        //notifies that the selection has changed
        getSVGSelection().selectionChanged(false);
        
        //redraws the selections of the selected nodes
        redrawSelection();
    }

    /**
     * notifies the refresh manager that a refresh should be done
     */
    protected synchronized void redrawSelection(){

        shouldRefresh=true;
    }

    /**
     * redraws the selections over the nodes that have been selected
     */
    protected void redrawSelectionMethod(){
        
        //stopping refreshing the canvas
        frame.getScrollPane().getSVGCanvas().setRepaintEnabled(false);

        //deselects all the shape modules
    	SVGShape sh=null;

    	for(Iterator it=getSVGSelection().getSVGEditor().getShapeModules().iterator(); it.hasNext();){

    		try{sh=(SVGShape)it.next();}catch (Exception ex){sh=null;}

    		if(sh!=null){
    		    
    		    sh.deselectAll(frame, false);
    		}
    	}

        clearSelectionSquares();

        Hashtable dSelectedItemsByModule=new Hashtable(selectedItemsByModule), selectedItems=null, map;
        LinkedHashMap dCurrentSelectionType=new LinkedHashMap(currentSelectionType);
        Iterator it2;
        LinkedList list=null;
        String current=null;
		Object obj=null;

        //invokes on each module the "select" method passing the linked list containing the nodes to be selected for each module as an argument
        for(Iterator it=dSelectedItemsByModule.keySet().iterator(); it.hasNext();){

            try{
                current=(String)it.next();
                list=(LinkedList)dSelectedItemsByModule.get(current);
            }catch (Exception ex){list=null;}

            if(list!=null && list.size()>0){

                selectedItems=new Hashtable();

                for(it2=list.iterator(); it2.hasNext();){

                    obj=it2.next();
                    
                    if(obj!=null){

                        String type=(String)dCurrentSelectionType.get(obj);

                        if(type!=null){

                            selectedItems.put(obj, type);
                        }
                    }
                }

                //getting the shape module
                sh=getShapeModule(current);

                if(sh!=null){
                    
                    //selecting the items
                    sh.select(frame, selectedItems);

                    //getting the selection squares
                    map=sh.getSelectionSquares(frame);

                    if(map!=null){

                        selectionSquares.putAll(map);
                    }
                }
            }
        }

        //enabling refreshing the canvas
        frame.getScrollPane().getSVGCanvas().setRepaintEnabled(true);
    }

    /**
     * selects all the nodes included in the given rectangle
     * @param rect a rectangle
     * @param parent the parent element into which the selection will be computed
     * @param isMultiSelectionEnabled true if the mutli selection is enabled
     */
    protected void select(Rectangle2D.Double rect, Element parent, boolean isMultiSelectionEnabled){

        if(! isMultiSelectionEnabled){

            deselectAll(false, false);
        }

        if(rect!=null && parent!=null){
            
            Rectangle2D r2=null;

            for(Node current=parent.getFirstChild(); current!=null; current=current.getNextSibling()){
                
                if(current instanceof Element && SVGToolkit.isElementAShape((Element)current)){
                    
                    r2=frame.getNodeGeometryBounds((Element)current);

                    if(r2!=null){

                        Rectangle2D.Double r3=new Rectangle2D.Double(r2.getX(), r2.getY(), r2.getWidth()+1, r2.getHeight()+1);

                        //if the node is contained in the rectangle, it is selected
                        if(r3!=null && rect.contains(r3)){

                            handleNodeSelection((Element)current, isMultiSelectionEnabled, true, true);
                        }
                    }
                }
            }

            refreshSelection();
        }
    }

    /**
     * tells whether a node is selected or not
     * @param node
     * @return true if the given node is selected
     */
    protected boolean isSelected(Node node){

        return currentSelectionType.containsKey(node);
    }

    /**
     * tells if one or more nodes are selected
     * @return true if one or more nodes are selected
     */
    protected boolean isSmthSelected(){

        if(currentSelectionType.size()>0){

            return true;
        }
        
        return false;
    }

    /**
     * deselects all the items on the canvas
     * @param validateAction true to validate the action
     * @param executeWhenNoNodesSelected whether to execute this method even if no node is selected
     */
    public void deselectAll(boolean validateAction, boolean executeWhenNoNodesSelected){

        //creates the undo/redo action
        if(getSVGSelection().getSVGEditor().getUndoRedo()!=null && (isSmthSelected() || executeWhenNoNodesSelected)){
            
            //stopping all the actions on the canvas//
            SVGCanvas canvas=frame.getScrollPane().getSVGCanvas();

            //creates a copy of the list of the selected items
            final LinkedList oldSelection=new LinkedList(currentSelectionType.keySet());

            SVGUndoRedoAction action=new SVGUndoRedoAction(getSVGSelection().undoredodeselectall){

                public void undo(){

                    selectedItemsByModule.clear();
                    currentSelectionType.clear();
                    selectionSquares.clear();
                    
                    //reselects all the nodes that have been deselected
                    Node current=null;

                    for(Iterator it=oldSelection.iterator(); it.hasNext();){
                        
                        try{current=(Node)it.next();}catch (Exception e){current=null;}

                        if(current!=null && current instanceof Element){
                            
                            handleNodeSelection((Element)current, false, false, false);
                        }
                    }
                    
                    redrawSelection();
                    getSVGSelection().selectionChanged(false);
                }

                public void redo(){
                    
                    selectedItemsByModule.clear();
                    currentSelectionType.clear();
                    selectionSquares.clear();

                    //deselects all the shape modules
                	SVGShape sh=null;

                	for(Iterator it=getSVGSelection().getSVGEditor().getShapeModules().iterator(); it.hasNext();){

                		try{sh=(SVGShape)it.next();}catch (Exception ex){sh=null;}

                		if(sh!=null){
                		    
                		    sh.deselectAll(frame, false);
                		}
                	}
                	
                	frame.getScrollPane().getSVGCanvas().delayedRepaint();
                }
            };
            
            //creates or gets the current undo/redo list and adds the new action into it
            if(undoRedoList==null){
                
                undoRedoList=new SVGUndoRedoActionList(getSVGSelection().undoredodeselectall);
            }
            
            undoRedoList.addLast(action);
            undoRedoList.setName(getSVGSelection().undoredodeselectall);
        }
        
        //removing all the nodes from the selection 
        selectedItemsByModule.clear();
        currentSelectionType.clear();
        selectionSquares.clear();

        //deselects all the shape modules
    	SVGShape sh=null;

    	for(Iterator it=getSVGSelection().getSVGEditor().getShapeModules().iterator(); it.hasNext();){

    		try{sh=(SVGShape)it.next();}catch (Exception ex){sh=null;}

    		if(sh!=null){
    		    
    		    sh.deselectAll(frame, true);
    		}
    	}

        //refreshes the selection
        if(validateAction){
            
            refreshSelection();
        }
    }

    /**
     * selects all the nodes of the dom
     * @param validateAction true to validate the action
     */
    protected void selectAll(boolean validateAction){

        final LinkedList nodesThatWereSelected=new LinkedList(currentSelectionType.keySet());

        //deselects all the selected nodes
        deselectAll(false, false);

        final LinkedList nodesToBeSelected=new LinkedList();

        if(getParentElement()!=null){

            for(Node current=getParentElement().getFirstChild(); current!=null; current=current.getNextSibling()){

                if(current instanceof Element && ! current.getNodeName().equals("defs")){ //$NON-NLS-1$
                    
                    handleNodeSelection((Element)current, false, true, false);
                    nodesToBeSelected.add(current);
                }
            }
        }
        
        if(undoRedoList==null){
            
            undoRedoList=new SVGUndoRedoActionList(getSVGSelection().undoredodeselectall);
        }
        
        //adding the undo/redo action
        undoRedoList.addLast(new SVGUndoRedoAction(getSVGSelection().undoredoselectall){

            public void undo() {
                
                redrawSelection();
            }

            public void redo() {
                
                redrawSelection();
            }
        });
        
        undoRedoList.setName(getSVGSelection().undoredoselectall);

        //refreshes the selection
        if(validateAction){
            
            refreshSelection();
        }
    }

    /**
     * gets the list of the selected nodes
     * @return the list of the selected nodes
     */
    protected LinkedList<Element> getCurrentSelection(){

        LinkedList<Element> list=new LinkedList();
        Element current=null;
        
        for(Iterator it=currentSelectionType.keySet().iterator(); it.hasNext();){
            
            try{current=(Element)it.next();}catch (Exception ex){current=null;}
            
            if(current!=null){
                
                String type=(String)currentSelectionType.get(current);
                
                if(type!=null && ! type.equals("lock")){ //$NON-NLS-1$
                    
                    list.add(current);
                }
            }
        }
        
        return list;
    }

    /**
     * @return the map of the current selection
     */
    protected Map getCurrentSelectionTypeMap(){

        return new Hashtable(currentSelectionType);
    }

    /**
     * adds the squares associated to a node in the selectionsquares hashtable
     * @param node a node
     * @param squares a list of selection squares
     */
    protected void addSelectionSquares(Node node, LinkedList squares){

        if(selectionSquares.containsKey(node)){
            
            selectionSquares.remove(node);
        }
        
        selectionSquares.put(node, squares);
    }

    /**
     * removes the squares associated to a node in the selectionsquares hashtable
     * @param node a node
     * @param squares a list of selection squares
     */
    protected void removeSelectionSquares(Node node, LinkedList squares){

        if(selectionSquares.containsKey(node)){
            
            selectionSquares.remove(node);
        }
    }

    /**
     * clears the selection squares map
     */
    protected void clearSelectionSquares(){

        LinkedList currentList=null;
        
        for(Iterator it=selectionSquares.keySet().iterator(); it.hasNext();){
            
            currentList=(LinkedList)selectionSquares.get(it.next());
            
            if(currentList!=null){
                
                currentList.clear();
            }
        }
        
        selectionSquares.clear();
    }

    /**
     * gets the SelectionSquare object on which a mouse event has been done
     * @param point the point on the canvas with a 1.0 scale
     * @return the SelectionSquare object on which a mouse event has been done
     * 			 null if the mouse event has not been done on the representation of a SelectionSquare object
     */
    public SVGSelectionSquare getSelectionSquare(Point2D.Double point){

        Point2D.Double pt=frame.getScaledPoint(point, false);

        Iterator it2=null;
        LinkedList currentList=null;
        SVGSelectionSquare square=null;
        Rectangle2D.Double rect=null;
        
        for(Iterator it=selectionSquares.values().iterator(); it.hasNext();){
            
            currentList=(LinkedList)it.next();
            
            if(currentList!=null){

                for(it2=currentList.iterator(); it2.hasNext();){
                    
                    square=(SVGSelectionSquare)it2.next();
                    rect=square.getRectangle();
                    
                    if(rect!=null && rect.contains(pt)){
                        
                        return square;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * returns true if the node is locked
     * @param node a node
     * @return true if the node is locked
     */
    protected boolean isLocked(Node node){
        
        if(node!=null && lockedNodes.contains(node)){
            
            return true;
        }
        
        return false;
    }

    /**
     * locks the nodes of the current selection
     */
    protected void lock(){

        //the hashtable of the current selection and the hashtable after the lock action
        final LinkedHashMap oldSelection=new LinkedHashMap(), newSelection=new LinkedHashMap();
        
        //the list of the nodes to be locked
        final LinkedList nodesToBeLocked=new LinkedList();

        //copies the hashtable of the current selection
        LinkedList list=new LinkedList(currentSelectionType.keySet());
        Object obj=null;
        
        for(Iterator it0=currentSelectionType.keySet().iterator(); it0.hasNext();){
            
            obj=it0.next();
            oldSelection.put(obj, currentSelectionType.get(obj));
        }

        //modifies the map of the current selection
        Node current=null;
        currentSelectionType.clear();
        
        for(Iterator it=list.iterator(); it.hasNext();){
            
            try{current=(Node)it.next();}catch (Exception ex){current=null;}
            
            if(current!=null){
                
                currentSelectionType.put(current,"lock"); //$NON-NLS-1$
                newSelection.put(current,"lock"); //$NON-NLS-1$
                
                if(! lockedNodes.contains(current)){
                    
                    lockedNodes.add(current);
                    nodesToBeLocked.add(current);
                }
            }
        }

        SVGUndoRedoAction action=new SVGUndoRedoAction(getSVGSelection().undoredounlock){

            public void undo() {
                
                //restores the old selection
                lockedNodes.removeAll(nodesToBeLocked);
                currentSelectionType.clear();
                currentSelectionType.putAll(oldSelection);
                redrawSelection();
                
                //notifies that the selection has changed
                getSVGSelection().selectionChanged(false);
            }

            public void redo(){
                
                //restores the selection that had been modified
                lockedNodes.addAll(nodesToBeLocked);
                currentSelectionType.clear();
                currentSelectionType.putAll(newSelection);
                redrawSelection();
                
                //notifies that the selection has changed
                getSVGSelection().selectionChanged(false);
            }
        };
        
        //creates or gets the current undo/redo list and adds the new action into it
        if(undoRedoList==null){
            
            undoRedoList=new SVGUndoRedoActionList(getSVGSelection().undoredolock);
        }
        
        undoRedoList.addLast(action);
        undoRedoList.setName(getSVGSelection().undoredolock);
        refreshSelection();
    }

    /**
     * unlocks the nodes of the current selection
     */
    protected void unlock(){

        //the hashtable of the current selection and the hashtable after the lock action
        final LinkedHashMap oldSelection=new LinkedHashMap(), newSelection=new LinkedHashMap();
        LinkedList list=new LinkedList(currentSelectionType.keySet());

        //copies the hashtable of the current selection
        Object obj=null;
        
        for(Iterator it0=currentSelectionType.keySet().iterator(); it0.hasNext();){
            
            obj=it0.next();
            oldSelection.put(obj, currentSelectionType.get(obj));
        }

        //modifies the hastable of the current selection
        Node current=null;
        currentSelectionType.clear();
        
        for(Iterator it=list.iterator(); it.hasNext();){
            
            try{current=(Node)it.next();}catch (Exception ex){current=null;}
            
            if(current!=null){
                
                currentSelectionType.put(current,"level1"); //$NON-NLS-1$
                newSelection.put(current,"level1"); //$NON-NLS-1$
                lockedNodes.remove(current);
            }
        }

        //the list of the nodes to be unlocked
        final LinkedList fnodes=new LinkedList(list);

        SVGUndoRedoAction action=new SVGUndoRedoAction(getSVGSelection().undoredounlock){

            public void undo() {
                
                //restores the old selection
                lockedNodes.addAll(fnodes);
                currentSelectionType.clear();
                currentSelectionType.putAll(oldSelection);
                redrawSelection();
                
                //notifies that the selection has changed
                getSVGSelection().selectionChanged(false);
            }

            public void redo(){
                
                //restores the selection that had been modified
                lockedNodes.removeAll(fnodes);
                currentSelectionType.clear();
                currentSelectionType.putAll(newSelection);
                redrawSelection();
                
                //notifies that the selection has changed
                getSVGSelection().selectionChanged(false);
            }
        };
        
        //creates or gets the current undo/redo list and adds the new action into it
        if(undoRedoList==null){
            
            undoRedoList=new SVGUndoRedoActionList(getSVGSelection().undoredounlock);
        }
        
        undoRedoList.addLast(action);
        undoRedoList.setName(getSVGSelection().undoredounlock);
        refreshSelection();
    }
    
    /**
     * returns the shape module having the given name
     * @param name a name
     * @return a module associated having the given name
     */
    public SVGShape getShapeModule(String name){

        SVGShape shape=null;
        
        if(name!=null){
        	
        	shape=getSVGSelection().getSVGEditor().getShapeModule(name);
        }
        
        if(shape==null){
            
            shape=getSVGSelection().getSVGEditor().getShapeModule("any"); //$NON-NLS-1$
        }
        
        return shape;
    }
    
    /**
     * returns the name of the shape module linked with the given element
     * @param element an element
     * @return the name of the shape module linked with the given element
     */
    public String getAssociatedModuleName(Element element){
        
        String moduleName=""; //$NON-NLS-1$
        
        if(element!=null){
        	
        	if(SVGEditor.isRtdaAnimationsVersion) {
        		
        		NodeList children=element.getChildNodes();
        		
        		if(children!=null && children.getLength()>0) {
        			
            		for(int i=0; i<children.getLength(); i++){
            			
            			if(children.item(i).getNodeName().equals(SVGToolkit.jwidgetTagName)){
            				
            				moduleName="JWidgetManager"; //$NON-NLS-1$
            				break;
            			}
            		}
        		}
        	}
        	
        	if(moduleName==null || (moduleName!=null && moduleName.equals(""))) { //$NON-NLS-1$
        		
        		moduleName=element.getNodeName();
        	}
        	
        }else {
        	
        	moduleName="any"; //$NON-NLS-1$
        }
        
        return moduleName;
    }
    
    /**
     * returns the shape module linked with the given element
     * @param element an element
     * @return a module associated with the given element
     */
    /*public SVGShape getShapeModule(Element element){
        
        SVGShape shape=null;
        
        if(element!=null){
        	
        	if(SVGEditor.isRtdaAnimationsVersion) {
        		
        		NodeList children=element.getElementsByTagName("rtda:jwidget");
        		
        		if(children!=null && children.getLength()>0) {
        			
        			//the element is a jwidget element
        			shape=SVGEditor.getSVGEditor().getShapeModule("JWidgetManager");
        		}
        	}
        	
        	if(shape==null) {
        		
        		shape=getSVGSelection().getSVGEditor().getShapeModule(element.getNodeName());
        	}
        }
        
        if(shape==null){
            
            shape=getSVGSelection().getSVGEditor().getShapeModule("any");
        }
        
        return shape;
    }*/
    
    /**
     * shows an outline of the translation of the given nodes with the given translation values
     * @param translationValues
     */
    protected void translateSelectedNodes(Point2D.Double translationValues){
        
        if(translationValues!=null){
            
            Iterator it2;
            LinkedList list=null, notLockedNodes=null;
            SVGShape shape=null;
            String name=""; //$NON-NLS-1$
            Node node=null;

            for(Iterator it=selectedItemsByModule.keySet().iterator(); it.hasNext();){

                try{name=(String)it.next();}catch (Exception ex){name=null;}

                if(name!=null && ! name.equals("")){ //$NON-NLS-1$

                    shape=getShapeModule(name);
                    
                    if(shape!=null){

                        try{
                            list=(LinkedList)selectedItemsByModule.get(name);
                        }catch (Exception ex){list=null;}

                        if(list!=null && list.size()>0){

                            notLockedNodes=new LinkedList();

                            //creates the list that does not contain the locked nodes
                            for(it2=list.iterator(); it2.hasNext();){

                                try{node=(Node)it2.next();}catch (Exception ex){node=null;}

                                if(node!=null && ! isLocked(node)){
                                    
                                    notLockedNodes.add(node);
                                }
                            }

                            if(notLockedNodes!=null && notLockedNodes.size()>0){

                                Object[] args={notLockedNodes, translationValues};
                                
                                //invokes the "doActions" method on the nodes to translate them
                                shape.doActions(frame, "level1", SVGShape.DO_TRANSLATE_ACTION, args); //$NON-NLS-1$
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * translates the given nodes with the translation values range
     * @param translationValues
     */
    protected void validateTranslateSelectedNodes(Point2D.Double translationValues){
        
        final Point2D.Double ftranslationValues=translationValues;
        
        if(ftranslationValues!=null){

            Runnable runnable=new Runnable(){

                public void run(){

                    Iterator it2;
                    LinkedList list=null, notLockedNodes=null;
                    SVGShape shape=null;
                    String name=""; //$NON-NLS-1$
                    Node node=null;

                    //for each type of modules
                    for(Iterator it=selectedItemsByModule.keySet().iterator(); it.hasNext();){

                        try{name=(String)it.next();}catch (Exception ex){name=null;}

                        if(name!=null && ! name.equals("")){ //$NON-NLS-1$
                            
                            //gets the module
                            shape=getShapeModule(name);

                            if(shape!=null){

                                try{
                                    list=(LinkedList)selectedItemsByModule.get(name);
                                }catch (Exception ex){list=null;}

                                if(list!=null && list.size()>0){

                                    notLockedNodes=new LinkedList();

                                    //creates the list that does not contain the locked nodes
                                    for(it2=list.iterator(); it2.hasNext();){

                                        try{node=(Node)it2.next();}catch (Exception ex){node=null;}

                                        if(node!=null && ! isLocked(node)){

                                            notLockedNodes.add(node);
                                        }
                                    }

                                    if(notLockedNodes.size()>0){

                                        Object[] args={notLockedNodes, ftranslationValues};
                                        
                                        //invokes the "doActions" method on the nodes to translate them
                                        shape.doActions(frame, "level1", SVGShape.VALIDATE_TRANSLATE_ACTION,args); //$NON-NLS-1$
                                    }
                                }
                            }
                        }
                    }

                    //notifies that the selection has changed
                    refreshSelection();
                    getSVGSelection().selectionChanged(true);
                }
            };

            frame.enqueue(runnable);
        }
    }
    
    /**
     * the method for doing an action given the current selection square and the points
     * @param square
     * @param originPoint
     * @param point
     * @param scaledOriginPoint
     * @param scaledPoint
     */
    protected void doActions(SVGSelectionSquare square, Point2D.Double originPoint, Point2D.Double point, Point2D.Double scaledOriginPoint, Point2D.Double scaledPoint){

        if(square!=null && originPoint!=null && point!=null && scaledOriginPoint!=null && scaledPoint!=null){
            
            final String type=(String)currentSelectionType.get(square.getNode());
            final SVGShape shape=getShapeModule(getAssociatedModuleName((Element)square.getNode()));

            if(type!=null && shape!=null && ! type.equals("lock") ){ //$NON-NLS-1$

                final Object[] args={square, originPoint, point, scaledOriginPoint, scaledPoint};

                shape.doActions(frame, type, SVGShape.DO_ACTION, args);
            }
        }
    }

    /**
     * the method for validating an action given the current selection square and the points
     * @param square
     * @param originPoint
     * @param point
     * @param scaledOriginPoint
     * @param scaledPoint
     */
    protected void validateDoActions(SVGSelectionSquare square, Point2D.Double originPoint, Point2D.Double point, Point2D.Double scaledOriginPoint, Point2D.Double scaledPoint){

        if(square!=null && originPoint!=null && point!=null && scaledOriginPoint!=null && scaledPoint!=null){
            
            final String type=(String)currentSelectionType.get(square.getNode());
            final SVGShape shape=getShapeModule(getAssociatedModuleName((Element)square.getNode()));

            if(type!=null && shape!=null && ! type.equals("lock") ){ //$NON-NLS-1$

                final Object[] args={square, originPoint, point, scaledOriginPoint, scaledPoint};

                Runnable runnable=new Runnable(){

                    public void run() {

                        shape.doActions(frame, type, SVGShape.VALIDATE_ACTION, args);
                        getSVGSelection().selectionChanged(true);
                    } 
                };

                frame.enqueue(runnable);
            }
        }
    }
}
