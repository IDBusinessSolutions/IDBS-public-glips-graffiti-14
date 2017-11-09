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

import javax.swing.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.canvas.*;

/**
 * @author Jordi SUC
 * the class listening to mouse events for selecting nodes on the canvas
 */
public class SVGSelectionListener extends MouseAdapter implements MouseMotionListener, KeyListener{

    /**
     * the constants of this class
     */
    private static  final int NO_ACTION=-1;
    private static  final int REGULAR=0;
    private static  final int SELECTION_SQUARE=1;
    private static  final int SELECT=2;
    private static  final int HAS_DRAGGED=3;
    private static  final int TRANSLATION=4;
    
    /**
     * the state of the selection manager
     */
    private int selectionState=NO_ACTION;
    
    /**
     * the current node
     */
    private Node currentNode=null;
    
    /**
     * whether the current node was already selected or not
     */
    private boolean isSelectedNode=false;
    
    /**
     * the current selection square
     */
    private SVGSelectionSquare selectionSquare=null;

    /**
     * the points used to record the drag area
     */
    private Point2D.Double opoint=null;
    /**
     * the two points used for the selection
     */
    private Point2D.Double point1=null, point2=null;
    
    /**
     * the selection manager linked with this listener
     */
    private SVGSelectionManager selectionManager=null;
    
    /**
     * the start point for the constrained dragging
     */
    protected Point2D start=null;
    
    /**
     * the end point for the constrained dragging
     */
    protected Point2D.Double end=null;
    
    /**
     * whether the drag action is constrained
     */
    boolean constrained=false;
    
    /**
     * the frame
     */
    private SVGFrame frame=null;
    
    /**
     * the selection
     */
    private SVGSelection selection=null;
    
	/**
	 * the last time stamp
	 */
	private long lastTimeStamp=-1;

    /**
     * the constructor of the class
     * @param selectionManager the selection manager
     */
    public SVGSelectionListener(SVGSelectionManager selectionManager){
        
        this.selectionManager=selectionManager;
        this.frame=selectionManager.getSVGFrame();
        this.selection=selectionManager.getSVGSelection();
        
        //creating a paint listener
        CanvasPaintListener paintListener=new CanvasPaintListener(){

            public void paintToBeDone(Graphics g) {

                if(selectionState==SELECT && point1!=null && point2!=null){

                    Rectangle2D.Double rect=selection.getSVGEditor().getSVGToolkit().getComputedRectangle(point1, point2);
                    Rectangle2D.Double bounds=frame.getScaledRectangle(rect, false);

                    selection.drawSelectionGhost(g, bounds);
                }
            }
        };
        
        frame.getScrollPane().getSVGCanvas().addLayerPaintListener(SVGCanvas.DRAW_LAYER, paintListener, false);
    }
    
    /**
     * @return whether an action is currently being done or not
     */
    public synchronized boolean isActing(){
        
        return (	selectionState==REGULAR || selectionState==SELECTION_SQUARE || selectionState==SELECT ||
                		selectionState==HAS_DRAGGED || selectionState==TRANSLATION);
    }

    /**
     * @return Returns the selectionManager.
     */
    protected SVGSelectionManager getSelectionManager() {
        return selectionManager;
    }
    
    /**
     * sets the state of the selection manager
     * @param selectionState the state
     */
    protected void setSelectionState(int selectionState){
        
        this.selectionState=selectionState;
    }

    /**
     * called when a mouse pressed event on the canvas has been received
     * @param evt the event
     */
    public void mousePressed(MouseEvent evt){

    	//whether this event is a popup event
    	boolean isPopupEvent=(evt.isPopupTrigger() || SwingUtilities.isRightMouseButton(evt));
    	
        if(selection.isSelectionEnabled() && ! isPopupEvent){

            Point2D.Double point=frame.getAlignedWithRulersPoint(evt.getPoint());
            start=point;
            opoint=point;

            //checks if the clicked point corresponds to a selection square
            SVGSelectionSquare selSquare=getSelectionManager().getSelectionSquare(point);

            if(selSquare!=null){
            	
            	//sets the state of the action
                setSelectionState(SELECTION_SQUARE);
                selectionSquare=selSquare;

            }else{

                getSelectionManager().setCursor(point);
                Node node=frame.getNodeAt(selectionManager.getParentElement(), point);

                if(! (evt.isControlDown() && evt.isShiftDown()) && node!=null && node instanceof Element){

                    //whether the node is selected or not
                    isSelectedNode=getSelectionManager().isSelected(node);

                    //if the multiple selection is not activated, and the node is not selected, all the selected nodes are deselected
                    if(! isSelectedNode && ! evt.isShiftDown()){

                        getSelectionManager().deselectAll(false, false);
                    }

                    //adds or removes the node
                    if(! isSelectedNode || (isSelectedNode && evt.isShiftDown())){
                    	
                    	getSelectionManager().handleNodeSelection((Element)node, evt.isShiftDown(), true, false);
                    }

                    //sets the state of the action
                    setSelectionState(REGULAR);

                    
                    //sets the current node
                    currentNode=node;

                }else{
                	
                    getSelectionManager().deselectAll(false, false);
                    
                    //sets the state
                    setSelectionState(SELECT);
                    
                    //set the value of the first point
                    point1=point;
                    
                    getSelectionManager().setCursor(point);
                }
            }
        }
        
        if(selection.isSelectionEnabled() && isPopupEvent){

        	//shows a popup
        	selection.getSVGEditor().getPopupManager().showPopup(frame, evt.getPoint());
        }
    }

    /**
     * called when a mouse released event on the canvas has been received
     * @param evt the event
     */
    public void mouseReleased(MouseEvent evt){

    	//whether this event is a popup event
    	boolean isPopupEvent=(evt.isPopupTrigger() || SwingUtilities.isRightMouseButton(evt));
    	
        if(selection.isSelectionEnabled() /*&& hasBeenPressed*/){
            
            Point2D.Double point=frame.getAlignedWithRulersPoint(evt.getPoint());

            if(constrained){
                
                point=end;
            }
            
        	if(selectionState==REGULAR && ! isPopupEvent){
        		
    			if(isSelectedNode && ! evt.isShiftDown()){
    				
        			getSelectionManager().handleNodeSelection((Element)currentNode, evt.isShiftDown(), true, false);
    			}
    			
                //refreshes the hashtable of the selected items, and draws the selections
                getSelectionManager().refreshSelection();

                //reinitializing the used variables
                opoint=null;
                currentNode=null;
                
        	}else if(selectionState==TRANSLATION){
        	    
                //computing the values of the translation and validating the translation
                Point2D.Double translationValues=new Point2D.Double(point.x-opoint.x, point.y-opoint.y);
                getSelectionManager().validateTranslateSelectedNodes(translationValues);

        	}else if(selectionState==SELECT){
        		
                //the point corresponding to the point returned by the event for a 1.0 scale index
                Point2D.Double baseScPoint=frame.getScaledPoint(new Point2D.Double(evt.getPoint().x, evt.getPoint().y), true);
        		
                //selects all the nodes that lies in the rectangle drawn by the mouse
        		point2=baseScPoint;
                frame.getScrollPane().getSVGCanvas().displayWaitCursor();
                getSelectionManager().select(selection.getSVGEditor().getSVGToolkit().getComputedRectangle(new Point2D.Double(point1.x, point1.y), 
                											new Point2D.Double(point.x, point.y)), getSelectionManager().getParentElement(), evt.isShiftDown());

                //reinitializing the used variables
                point1=null;
                point2=null;
                frame.getStateBar().setSVGW(""); //$NON-NLS-1$
                frame.getStateBar().setSVGH(""); //$NON-NLS-1$
                frame.getScrollPane().getSVGCanvas().delayedRepaint();
                frame.getScrollPane().getSVGCanvas().returnToLastCursor();
        		
        	}else if(selectionState==SELECTION_SQUARE){
        		
        		getSelectionManager().validateDoActions(selectionSquare, opoint, point, opoint, point);
        		
                //reinitializing the used variables
                opoint=null;
                selectionSquare=null;
        	}
        	
        	setSelectionState(NO_ACTION);
        }
    }

    /**
     * called when a mouse dragged event on the canvas has been received
     * @param evt the event
     */
    public void mouseDragged(MouseEvent evt){
    	
		long currentTime=System.currentTimeMillis();
		
		if(lastTimeStamp==-1){
			
			doMouseDraggedAction(evt);
			lastTimeStamp=currentTime;
			
		}else{
			
			if(currentTime-lastTimeStamp>50){
				
				doMouseDraggedAction(evt);
				lastTimeStamp=currentTime;
			}
		}
    }
    
    /**
     * executes the mouse dragged action
     * @param evt an event
     */
    protected void doMouseDraggedAction(MouseEvent evt){
    	
       	//whether this event is a popup event
    	boolean isPopupEvent=(evt.isPopupTrigger() || SwingUtilities.isRightMouseButton(evt));
        
        if(selection.isSelectionEnabled() && ! isPopupEvent){

            Point2D.Double point=frame.getAlignedWithRulersPoint(evt.getPoint());
            constrained=evt.isControlDown() && evt.isAltDown();
            
            if(constrained){
                
                double totDx=point.x-start.getX(), totDy=point.y-start.getY();
                int dx=0, dy=0;
               
                if (Math.abs(totDx)>Math.abs(totDy)){
                    
                    dx=0;
                    dy=(int)totDy;
                    
                } else {
                    
                    dx=(int)totDx;
                    dy=0;
                }
                
                point.x=point.x-dx;
                point.y=point.y-dy;
                end=new Point2D.Double(point.x,point.y);
            }
        	
        	if((selectionState==REGULAR && ! point.equals(opoint)) || selectionState==TRANSLATION){
        		
                //translating the selected nodes
                getSelectionManager().translateSelectedNodes(new Point2D.Double(point.x-opoint.x, point.y-opoint.y));
        		
                setSelectionState(TRANSLATION);
                
        	}else if(selectionState==SELECT){
        		
                //the point corresponding to the point returned by the event for a 1.0 scale index
                Point2D.Double baseScPoint=frame.getScaledPoint(new Point2D.Double(evt.getPoint().x, evt.getPoint().y), true);
        		
                point2=baseScPoint;
                frame.getScrollPane().getSVGCanvas().delayedRepaint();
        		
        	}else if(selectionState==SELECTION_SQUARE){

        		getSelectionManager().doActions(selectionSquare, opoint, point, opoint, point);
        	}
        }
    }

    /**
     * called when a mouse moved event on the canvas has been received
     * @param evt the event
     */
    public void mouseMoved(MouseEvent evt) {

        if(selection.isSelectionEnabled() && selectionState!=SELECT){

    		long currentTime=System.currentTimeMillis();
    		
    		if(lastTimeStamp==-1){
    			
    			doMouseMovedAction(evt);
    			lastTimeStamp=currentTime;
    			
    		}else{
    			
    			if(currentTime-lastTimeStamp>100){
    				
    				doMouseMovedAction(evt);
    				lastTimeStamp=currentTime;
    			}
    		}
        }
    }
    
    /**
     * executes the mouse moved action
     * @param evt an event
     */
    protected void doMouseMovedAction(MouseEvent evt){
    	
        Point2D.Double baseScPoint=frame.getScaledPoint(new Point2D.Double(evt.getPoint().x, evt.getPoint().y), true);
        getSelectionManager().setCursor(baseScPoint);
    }

    @Override
    public void mouseEntered(MouseEvent evt) {
    }

    @Override
    public void mouseExited(MouseEvent evt) {
    }

    /**
     * @param evt the event
     */
    public void keyTyped(KeyEvent evt){
    }

    /**
     * @param evt the event
     */
    public void keyReleased(KeyEvent evt){
    }

    /**
     * @param evt the event
     */
    public void keyPressed(KeyEvent evt){
        
        if(selection.isSelectionEnabled()){
            
            /*Point range=frame.getScrollPane().getRulersRange();
            Point2D.Double translationValues=null;

            if(range!=null && evt.getModifiers()==0){

                if(evt.getKeyCode()==KeyEvent.VK_UP){

                    translationValues=new Point2D.Double(0, -range.y);

                }else if(evt.getKeyCode()==KeyEvent.VK_DOWN){

                    translationValues=new Point2D.Double(0, range.y);

                }else if(evt.getKeyCode()==KeyEvent.VK_LEFT){

                    translationValues=new Point2D.Double(-range.x, 0);

                }else if(evt.getKeyCode()==KeyEvent.VK_RIGHT){

                    translationValues=new Point2D.Double(range.x, 0);
                }

                if(translationValues!=null){

                    getSelectionManager().validateTranslateSelectedNodes(translationValues);
                }
            }*/
        }
    }

}
