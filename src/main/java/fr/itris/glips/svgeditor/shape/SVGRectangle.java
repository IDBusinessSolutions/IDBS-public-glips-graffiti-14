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
package fr.itris.glips.svgeditor.shape;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.*;
import java.util.*;

import javax.swing.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.resources.*;
import fr.itris.glips.svgeditor.selection.*;
import fr.itris.glips.svgeditor.undoredo.*;

/**
 * @author Jordi SUC
 * the class that allows to add, select,modify the properties, delete, transform a rectangle on the canvas
 */
public class SVGRectangle extends SVGShape{
	
	/**
	 * the reference of an object of this class
	 */
	private final SVGRectangle svgRect=this;
	
	/**
	 * used to convert numbers into a string
	 */
	private DecimalFormat format;
	
	/**
	 * the action listener used to draw the rectangle
	 */
	private RectActionListener rectAction;
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGRectangle(SVGEditor editor) {
	    
		super(editor);
		
		ids.put("id","rect"); //$NON-NLS-1$ //$NON-NLS-2$
		ids.put("idmenuitem","Rectangle"); //$NON-NLS-1$ //$NON-NLS-2$
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labels.put("label", bundle.getString("shaperectanglelabel")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredocreate", bundle.getString("shaperectangleundoredocreate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredotranslate", bundle.getString("shapeundoredotranslate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredoresize", bundle.getString("shaperectangleundoredoresize")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredorotate", bundle.getString("shaperectangleundoredorotate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("helpcreate", bundle.getString("shaperectanglehelpcreate")); //$NON-NLS-1$ //$NON-NLS-2$
			}catch (Exception ex){}
		}
		
		DecimalFormatSymbols symbols=new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		format=new DecimalFormat("######.#",symbols); //$NON-NLS-1$
		
		//the icons
		icon=SVGResource.getIcon(ids.get("idmenuitem"), false); //$NON-NLS-1$
		disabledIcon=SVGResource.getIcon(ids.get("idmenuitem"), true); //$NON-NLS-1$
		
		//the menu item
		menuitem=new JMenuItem(labels.get("label"), icon); //$NON-NLS-1$
		menuitem.setDisabledIcon(disabledIcon);
		menuitem.setEnabled(false);
		rectAction=new RectActionListener();
		
		//the toggle button
		toolItem=new JToggleButton(disabledIcon);
		toolItem.setEnabled(false);
		toolItem.setToolTipText(labels.get("label")); //$NON-NLS-1$
		
		//adds a listener to the menu item and the toggle button
		menuitem.addActionListener(rectAction);
		toolItem.addActionListener(rectAction);
		
		//a listener that listens to the changes of the SVGFrames
		final ActionListener svgframeListener=new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				if(getSVGEditor().getFrameManager().getFrameNumber()>0){
				    
					menuitem.setEnabled(true);
					toolItem.setEnabled(true);
					toolItem.setIcon(icon);
					
					rectAction.reset();

				}else{
				    
					menuitem.setEnabled(false);
					toolItem.setEnabled(false);
					toolItem.setIcon(disabledIcon);
				}
			}
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);
	}
	
	/**
	 * @return a map associating a menu item id to its menu item object
	 */
	public Hashtable getMenuItems(){
		
		Hashtable menuItems=new Hashtable();
		menuItems.put(ids.get("idmenuitem"), menuitem); //$NON-NLS-1$
		
		return menuItems;
	}
	
	/**
	 * @return a map associating a tool item id to its tool item object
	 */
	public Hashtable getToolItems(){
		
		Hashtable toolItems=new Hashtable();
		toolItems.put(ids.get("idmenuitem"), toolItem); //$NON-NLS-1$
		
		return toolItems;
	}

	/**
	 * draws a rectangle
	 * @param frame the current SVGFrame
	 * @param bounds the bounds of the element
	 */
	protected void drawRectangle(SVGFrame frame, Rectangle2D.Double bounds){
		
		if(frame!=null && bounds!=null){
		    
			Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
			
			//normalizing the bounds of the element
			if(bounds.width<=0){
			    
			    bounds.width=1;
			}
			
			if(bounds.height<=0){
			    
			    bounds.height=1;
			}
			
			if(getSVGEditor().getSVGSelection()!=null && doc!=null){
			    
				final Element parent=getSVGEditor().getSVGSelection().getCurrentParentElement(frame);
				
				if(parent!=null){

					// creates the rectangle
					final Element rectangle=doc.createElementNS(doc.getDocumentElement().getNamespaceURI(),"rect"); //$NON-NLS-1$
					
					rectangle.setAttributeNS(null,"x", format.format(bounds.x)); //$NON-NLS-1$
					rectangle.setAttributeNS(null,"y", format.format(bounds.y)); //$NON-NLS-1$
					rectangle.setAttributeNS(null,"width", format.format(bounds.width==0?1:bounds.width)); //$NON-NLS-1$
					rectangle.setAttributeNS(null,"height", format.format(bounds.height==0?1:bounds.height)); //$NON-NLS-1$
					String colorString=SVGEditor.getColorChooser().
																		getColorString(SVGColorManager.getCurrentColor());
					rectangle.setAttributeNS(null, "style", "fill:".concat(colorString.concat(";"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
					//sets that the svg has been modified
					frame.setModified(true);

					//attaches the element to the svg parent element	
					parent.appendChild(rectangle);
			
					//create the undo/redo action and insert it into the undo/redo stack
					if(editor.getUndoRedo()!=null){

						SVGUndoRedoAction action=new SVGUndoRedoAction((String)labels.get("undoredocreate")){ //$NON-NLS-1$

							public void undo(){
							    
							    parent.removeChild(rectangle);
							}

							public void redo(){
							    
							    parent.appendChild(rectangle);
							}
						};
				
						SVGSelection selection=editor.getSVGSelection();
				
						if(selection!=null){

							selection.deselectAll(frame, false, true);
							selection.addUndoRedoAction(frame, action);
							selection.handleNodeSelection(frame, rectangle);
							selection.addUndoRedoAction(frame, new SVGUndoRedoAction((String)labels.get("undoredocreate")){}); //$NON-NLS-1$
							selection.refreshSelection(frame);

						}else{
						    
							SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList((String)labels.get("undoredocreate")); //$NON-NLS-1$
							actionlist.add(action);
							editor.getUndoRedo().addActionList(frame, actionlist);
						}
					}
				}
			}
		}
	}
	
	/**
	 * draws what the rectangle will be if the user releases the mouse button
	 * @param frame the current SVGFrame
	 * @param g a graphics element
	 * @param bounds the bounds of the element
	 */
	protected void drawGhost(SVGFrame frame, Graphics2D g, Rectangle2D.Double bounds){
		
		if(g!=null && frame!=null && bounds!=null){

		    g=(Graphics2D)g.create();
		    
			//draws the new awt rectangle to be displayed
			g.setColor(GHOST_COLOR);
			g.setXORMode(Color.white);
			g.drawRect((int)bounds.x, (int)bounds.y, (int)bounds.width, (int)bounds.height);

			Rectangle2D.Double scaleBounds=frame.getScaledRectangle(bounds, true);
			frame.getStateBar().setSVGW(SVGEditor.getDisplayFormat().format(scaleBounds.width));
			frame.getStateBar().setSVGH(SVGEditor.getDisplayFormat().format(scaleBounds.height));
			g.dispose();
		}		
	}
	
	/**
	 * used to remove the listener added to draw a rectangle when the user clicks on the menu item
	 */
	public void cancelActions(){
	    
		if(rectAction!=null){
		    
			toolItem.removeActionListener(rectAction);
			toolItem.setSelected(false);
			toolItem.addActionListener(rectAction);
			
		    rectAction.cancelActions();
		}
	}
	
	/**
	 * @author Jordi SUC
	 * the class allowing to get the position and size of the future drawn rectangle 
	 */
	protected class RectActionListener implements ActionListener{

		/**
		 * the hashtable associating a frame to its mouse adapter
		 */
		private final Hashtable mouseAdapterFrames=new Hashtable();
		
		/**
		 * the cursor used when creating a rectangle
		 */
		private Cursor createCursor;

		private boolean isActive=false;
		
		/**
		 * the constructor of the class
		 */
		protected RectActionListener(){
			
			createCursor=getSVGEditor().getCursors().getCursor("rectangle"); //$NON-NLS-1$
		}
		
		/**
		 * resets the listener
		 */
		protected void reset(){
			
			if(isActive){
			    
				Collection frames=getSVGEditor().getFrameManager().getFrames();
				Iterator it;
				SVGFrame frm=null;
				Object mouseListener=null;
				
				//removes all the motion adapters from the frames
				for(it=new LinkedList(mouseAdapterFrames.keySet()).iterator(); it.hasNext();){
				    
					try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
			
					if(frm!=null && ! frames.contains(frm)){
					    
						try{
							mouseListener=mouseAdapterFrames.get(frm);
							frm.getScrollPane().getSVGCanvas().removeMouseListener((MouseAdapter)mouseListener);
							frm.getScrollPane().getSVGCanvas().removeMouseMotionListener((MouseMotionListener)mouseListener);
						}catch (Exception ex){}
						
						mouseAdapterFrames.remove(frm);
					}
				}

				RectMouseListener rml=null;
				
				//adds the new motion adapters
				for(it=frames.iterator(); it.hasNext();){
				    
					try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
			
					if(frm!=null && ! mouseAdapterFrames.containsKey(frm)){

						rml=new RectMouseListener(frm);

						try{
							frm.getScrollPane().getSVGCanvas().addMouseListener(rml);
							frm.getScrollPane().getSVGCanvas().addMouseMotionListener(rml);
							frm.getScrollPane().getSVGCanvas().setSVGCursor(createCursor);
						}catch (Exception ex){}
						
						mouseAdapterFrames.put(frm, rml);
					}
				}				
			}
		}
		
		/**
		 * used to remove the listener added to draw a rectangle when the user clicks on the menu item
		 */	
		protected void cancelActions(){
		    
			if(isActive){
				
				//removes the listeners
				Iterator it;
				SVGFrame frm=null;
				Object mouseListener=null;
				
				//removes all the motion adapters from the frames
				for(it=new LinkedList(mouseAdapterFrames.keySet()).iterator(); it.hasNext();){
				    
					try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
					
					if(frm!=null){
					    
						//resets the information displayed
						frm.getStateBar().setSVGW(""); //$NON-NLS-1$
						frm.getStateBar().setSVGH(""); //$NON-NLS-1$
						frm.getScrollPane().getSVGCanvas().setSVGCursor(frm.getSVGEditor().getCursors().getCursor("default")); //$NON-NLS-1$
						
						try{
							mouseListener=mouseAdapterFrames.get(frm);
							frm.getScrollPane().getSVGCanvas().removeMouseListener((MouseAdapter)mouseListener);
							frm.getScrollPane().getSVGCanvas().removeMouseMotionListener((MouseMotionListener)mouseListener);
							
							if(mouseListener!=null && ((RectMouseListener)mouseListener).paintListener!=null){
							    
								//removes the paint listener
								frm.getScrollPane().getSVGCanvas().removePaintListener(((RectMouseListener)mouseListener).paintListener, true);
							}
						}catch (Exception ex){}
						
						mouseAdapterFrames.remove(frm);
					}
				}
				
				isActive=false;
			}
		}
		
		/**
		 * the method called when an event occurs
		 * @param evt the event
		 */
		public void actionPerformed(ActionEvent evt){
			
			if((evt.getSource() instanceof JMenuItem && ! toolItem.isSelected()) || (evt.getSource() instanceof JToggleButton)){

				getSVGEditor().cancelActions(false);
			
				if(getSVGEditor().getFrameManager().getCurrentFrame()!=null){
			
					toolItem.removeActionListener(rectAction);
					toolItem.setSelected(true);
					toolItem.addActionListener(rectAction);
					
					//the listener is active
					isActive=true;

					Collection frames=getSVGEditor().getFrameManager().getFrames();
					Iterator it;
					SVGFrame frm=null;
					
					//adds the new mouse adapters
					for(it=frames.iterator(); it.hasNext();){
					    
						try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
				
						if(frm!=null){

							RectMouseListener rml=new RectMouseListener(frm);

							try{
								frm.getScrollPane().getSVGCanvas().addMouseMotionListener(rml);
								frm.getScrollPane().getSVGCanvas().addMouseListener(rml);
								frm.getScrollPane().getSVGCanvas().setSVGCursor(createCursor);
							}catch (Exception ex){}
							
							mouseAdapterFrames.put(frm, rml);
						}
					}
				}
			}
		}
			
		protected class RectMouseListener extends MouseAdapter implements MouseMotionListener{
		
			/**
			 * the points of the area corresponding to the future rectangle
			 */		
			private Point2D.Double point1=null, point2=null;
			
			private SVGFrame frame;
			
			/**
			 * the paint listener
			 */
			private CanvasPaintListener paintListener=null;
						
			/**
			 * the constructor of the class
			 * @param frame a frame
			 */
			public RectMouseListener(SVGFrame frame){
			    
				this.frame=frame;
				final SVGFrame fframe=frame;
				
				//adds a paint listener
				paintListener=new CanvasPaintListener(){

					public void paintToBeDone(Graphics g) {
						
						if(point1!=null && point2!=null){
				        
							Rectangle2D.Double rect=getSVGEditor().getSVGToolkit().getComputedRectangle(point1, point2);
							Rectangle2D.Double computedBounds=fframe.getScaledRectangle(rect, false);
							
							//draws the shape of the element that will be created if the user released the mouse button
							svgRect.drawGhost(fframe, (Graphics2D)g, computedBounds);
						}
					}
				};
				
				frame.getScrollPane().getSVGCanvas().addLayerPaintListener(SVGCanvas.DRAW_LAYER, paintListener, false);
			}
		
			/**
			 * the method called when an event occurs
			 * @param evt the event
			 */
			public void mouseDragged(MouseEvent evt) {
				
				//sets the second point of the element
				point2=frame.getAlignedWithRulersPoint(evt.getPoint());
				
				//asks the canvas to be repainted to draw the shape of the future element
				frame.getScrollPane().getSVGCanvas().delayedRepaint();
			}
				
			/**
			 * the method called when an event occurs
			 * @param evt the event
			 */
			public void mouseMoved(MouseEvent evt) {
			}
				
			/**
			 * the method called when an event occurs
			 * @param evt the event
			 */
			public void mousePressed(MouseEvent evt){
			    
				//sets the first point of the area corresponding to the future element
				point1=frame.getAlignedWithRulersPoint(evt.getPoint());
			}
				
			/**
			 * the method called when an event occurs
			 * @param evt the event
			 */
			public void mouseReleased(MouseEvent evt){
				
				Point2D.Double point=frame.getAlignedWithRulersPoint(evt.getPoint());
				
				//creates the element in the SVG document
				if(point1!=null && point1.x>=0 && point1.y>=0 && point!=null){
					
					final Rectangle2D.Double rect=getSVGEditor().getSVGToolkit().getComputedRectangle(point1, point);
					
					Runnable runnable=new Runnable(){
						
						public void run() {

							svgRect.drawRectangle(frame, rect);
						}
					};
					
					frame.enqueue(runnable);
				}

				getSVGEditor().cancelActions(true);
				point1=null;
				point2=null;
			}
		}
	}
}
