/*
 * Created on 10 mai 2004
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
 * @author suc
 *
 * @author Jordi SUC
 * the class that allows to add, select,modify the properties, delete, transform a ellipse on the canvas
 */
public class SVGEllipse extends SVGShape{

	/**
	 * the reference of an object of this class
	 */
	private final SVGEllipse svgEllipse=this;
	
	/**
	 * the action listener used to draw the ellipse
	 */
	private EllipseActionListener ellipseAction=null;
	
	/**
	 * used to convert numbers into a string
	 */
	private DecimalFormat format;
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGEllipse(SVGEditor editor) {
		super(editor);
		
		ids.put("id","ellipse"); //$NON-NLS-1$ //$NON-NLS-2$
		ids.put("idmenuitem","Ellipse"); //$NON-NLS-1$ //$NON-NLS-2$
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labels.put("label", bundle.getString("shapeellipselabel")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredocreate", bundle.getString("shapeellipseundoredocreate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredotranslate", bundle.getString("shapeundoredotranslate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredoresize", bundle.getString("shapeellipseundoredoresize")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredorotate", bundle.getString("shapeellipseundoredorotate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("helpcreate", bundle.getString("shapeellipsehelpcreate")); //$NON-NLS-1$ //$NON-NLS-2$
			}catch (Exception ex){}
		}
		
		DecimalFormatSymbols symbols=new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		format=new DecimalFormat("######.#",symbols); //$NON-NLS-1$
		
		//the icons
		icon=SVGResource.getIcon((String)ids.get("idmenuitem"), false); //$NON-NLS-1$
		disabledIcon=SVGResource.getIcon((String)ids.get("idmenuitem"), true); //$NON-NLS-1$
		
		//the menu item
		menuitem=new JMenuItem((String)labels.get("label"), icon); //$NON-NLS-1$
		menuitem.setDisabledIcon(disabledIcon);
		menuitem.setEnabled(false);
		ellipseAction=new EllipseActionListener();
		
		//the toggle button
		toolItem=new JToggleButton(disabledIcon);
		toolItem.setEnabled(false);
		toolItem.setToolTipText((String)labels.get("label")); //$NON-NLS-1$
		
		//adds a listener to the menu item and the toggle button
		menuitem.addActionListener(ellipseAction);
		toolItem.addActionListener(ellipseAction);

		//a listener that listens to the changes of the SVGFrames
		final ActionListener svgframeListener=new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				if(getSVGEditor().getFrameManager().getFrameNumber()>0){
				    
					menuitem.setEnabled(true);
					toolItem.setEnabled(true);
					toolItem.setIcon(icon);
					
				}else{
				    
					menuitem.setEnabled(false);
					toolItem.setEnabled(false);
					toolItem.setIcon(disabledIcon);
				}
				
				ellipseAction.reset();
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
		menuItems.put((String)ids.get("idmenuitem"), menuitem); //$NON-NLS-1$
		
		return menuItems;
	}
	
	/**
	 * @return a map associating a tool item id to its tool item object
	 */
	public Hashtable getToolItems(){
		
		Hashtable toolItems=new Hashtable();
		toolItems.put((String)ids.get("idmenuitem"), toolItem); //$NON-NLS-1$
		
		return toolItems;
	}

	/**
	 * draws an ellipse
	 * @param frame the current SVGFrame
	 * @param bounds the bounds of the element
	 */
	protected void drawEllipse(SVGFrame frame, Rectangle2D.Double bounds){
		
		if(frame!=null && bounds!=null){
			
			Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
			
			if(getSVGEditor().getSVGSelection()!=null && doc!=null){
			    
				final Element parent=getSVGEditor().getSVGSelection().getCurrentParentElement(frame);
				
				if(parent!=null){
			
					//creates the ellipse
					Element ellipse = doc.createElementNS(doc.getDocumentElement().getNamespaceURI(),"ellipse"); //$NON-NLS-1$
					ellipse.setAttributeNS(null,"cx", format.format(bounds.x+bounds.width/2)); //$NON-NLS-1$
					ellipse.setAttributeNS(null,"cy", format.format(bounds.y+bounds.height/2)); //$NON-NLS-1$
					ellipse.setAttributeNS(null,"rx", format.format(bounds.width/2)); //$NON-NLS-1$
					ellipse.setAttributeNS(null,"ry", format.format(bounds.height/2)); //$NON-NLS-1$
					String colorString=SVGEditor.getColorChooser().getColorString(SVGColorManager.getCurrentColor());
					ellipse.setAttributeNS(null, "style", "fill:".concat(colorString.concat(";"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
					//sets that the svg has been modified
					frame.setModified(true);
			
					//creates final variables
					final Node fellipse=ellipse;

					//attaches the ellipse to the svg root element
					parent.appendChild(fellipse);
			
					//create the undo/redo action and insert it into the undo/redo stack
					if(getSVGEditor().getUndoRedo()!=null){

						SVGUndoRedoAction action=new SVGUndoRedoAction((String)labels.get("undoredocreate")){ //$NON-NLS-1$

							public void undo(){
							    
							    parent.removeChild(fellipse);
							}

							public void redo(){
							    
							    parent.appendChild(fellipse);
							}
						};
				
						SVGSelection selection=getSVGEditor().getSVGSelection();
				
						if(selection!=null){
						    
							selection.deselectAll(frame, false, true);
							selection.addUndoRedoAction(frame, action);
							selection.handleNodeSelection(frame, ellipse);
							selection.addUndoRedoAction(frame, new SVGUndoRedoAction((String)labels.get("undoredocreate")){}); //$NON-NLS-1$
							selection.refreshSelection(frame);
					
						}else{
						    
							SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList((String)labels.get("undoredocreate")); //$NON-NLS-1$
							actionlist.add(action);
							getSVGEditor().getUndoRedo().addActionList(frame,actionlist);
						}
					}
				}
			}
		}
	}
	
	/**
	 * draws what the ellipse will be if the user releases the mouse button
	 * @param frame the current SVGFrame
	 * @param g a graphics element
	 * @param bounds the bounds of the element
	 */
	protected void drawGhost(SVGFrame frame, Graphics2D g, Rectangle2D.Double bounds){
		
		if(frame!=null && g!=null && bounds!=null){
			
		    g=(Graphics2D)g.create();
		    
			//draws the new awt ellipse to be displayed
			g.setColor(GHOST_COLOR);
			g.setXORMode(Color.white);
			g.drawOval((int)bounds.x, (int)bounds.y, (int)bounds.width, (int)bounds.height);

			Rectangle2D.Double scaleBounds=frame.getScaledRectangle(bounds, true);
			frame.getStateBar().setSVGW(SVGEditor.getDisplayFormat().format(scaleBounds.width));
			frame.getStateBar().setSVGH(SVGEditor.getDisplayFormat().format(scaleBounds.height));
			g.dispose();
		}		
	}
	
	/**
	 * used to remove the listener added to draw an ellipse when the user clicks on the menu item
	 */
	public void cancelActions(){
	    
		if(ellipseAction!=null){
		    
			toolItem.removeActionListener(ellipseAction);
			toolItem.setSelected(false);
			toolItem.addActionListener(ellipseAction);
			
		    ellipseAction.cancelActions();
		}
	}
	
	/**
	 * 
	 * @author Jordi SUC
	 * the class allowing to get the position and size of the future drawn ellipse 
	 */
	protected class EllipseActionListener implements ActionListener{

		/**
		 * the hashtable associating a frame to its mouse adapter
		 */
		private final Hashtable mouseAdapterFrames=new Hashtable();
		
		/**
		 * an instance of this class
		 */
		private final EllipseActionListener action=this;
		
		/**
		 * the cursor used when creating a rectangle
		 */
		private Cursor createCursor;
		
		/**
		 * the source component
		 */
		private Object source=null;
		
		private boolean isActive=false;
		
		/**
		 * the constructor of the class
		 */
		protected EllipseActionListener(){
			
			createCursor=getSVGEditor().getCursors().getCursor("ellipse"); //$NON-NLS-1$
		}
		
		/**
		 * resets the listener
		 */
		protected void reset(){
			
			if(isActive){
			    
				Collection frames=getSVGEditor().getFrameManager().getFrames();
			
				Iterator it;
				SVGFrame frm=null;
				LinkedList toBeRemoved=new LinkedList();
				Object mouseListener=null;
				
				//removes all the motion adapters from the frames
				for(it=mouseAdapterFrames.keySet().iterator(); it.hasNext();){
				    
					try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
			
					if(frm!=null && ! frames.contains(frm)){
					    
						try{
							mouseListener=mouseAdapterFrames.get(frm);
							frm.getScrollPane().getSVGCanvas().removeMouseListener((MouseAdapter)mouseListener);
							frm.getScrollPane().getSVGCanvas().removeMouseMotionListener((MouseMotionListener)mouseListener);
						}catch (Exception ex){}
						
						toBeRemoved.add(frm);
					}
				}
					
				//removes the frames that have been closed
				for(it=toBeRemoved.iterator(); it.hasNext();){
				    
					try{mouseAdapterFrames.remove(it.next());}catch (Exception ex){}
				}

				EllipseMouseListener eml=null;
				
				//adds the new motion adapters
				for(it=frames.iterator(); it.hasNext();){
				    
					try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
			
					if(frm!=null && ! mouseAdapterFrames.containsKey(frm)){
				
						eml=new EllipseMouseListener(frm);

						try{
							frm.getScrollPane().getSVGCanvas().addMouseListener(eml);
							frm.getScrollPane().getSVGCanvas().addMouseMotionListener(eml);
							frm.getScrollPane().getSVGCanvas().setSVGCursor(createCursor);
						}catch (Exception ex){}
						
						mouseAdapterFrames.put(frm, eml);
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
				LinkedList toBeRemoved=new LinkedList();
				Object mouseListener=null;
			
				//removes all the motion adapters from the frames
				for(it=mouseAdapterFrames.keySet().iterator(); it.hasNext();){
				    
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
							
							if(mouseListener!=null && ((EllipseMouseListener)mouseListener).paintListener!=null){
							    
								//removes the paint listener
								frm.getScrollPane().getSVGCanvas().removePaintListener(((EllipseMouseListener)mouseListener).paintListener, true);
							}
						}catch (Exception ex){}
						
						toBeRemoved.add(frm);
					}
				}
			
				//removes the frames that have been closed
				for(it=toBeRemoved.iterator(); it.hasNext();){
				    
					try{mouseAdapterFrames.remove(it.next());}catch (Exception ex){}
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
					
					toolItem.removeActionListener(ellipseAction);
					toolItem.setSelected(true);
					toolItem.addActionListener(ellipseAction);
			
					//the listener is active
					isActive=true;
					
					source=evt.getSource();
					Collection frames=getSVGEditor().getFrameManager().getFrames();
					Iterator it;
					SVGFrame frm=null;
					EllipseMouseListener eml=null;
					
					//adds the new motion adapters
					for(it=frames.iterator(); it.hasNext();){
					    
						try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
					
						if(frm!=null){
	
							eml=new EllipseMouseListener(frm);
	
							try{
								frm.getScrollPane().getSVGCanvas().addMouseListener(eml);
								frm.getScrollPane().getSVGCanvas().addMouseMotionListener(eml);
								frm.getScrollPane().getSVGCanvas().setSVGCursor(createCursor);
	
							}catch (Exception ex){}
							
							mouseAdapterFrames.put(frm, eml);
						}
					}
				}
			}
		}
			
		protected class EllipseMouseListener extends MouseAdapter implements MouseMotionListener{
		
			/**
			 * the points of the area corresponding to the future rectangle
			 */		
			private Point2D.Double point1=null, point2=null;
			
			/**
			 * the paint listener
			 */
			private CanvasPaintListener paintListener=null;
			
			private SVGFrame frame;
		
			/**
			 * the constructor of the class
			 * @param frame a frame
			 */
			public EllipseMouseListener(SVGFrame frame){
			    
				this.frame=frame;
				
				final SVGFrame fframe=frame;
				
				//adds a paint listener
				paintListener=new CanvasPaintListener(){

					public void paintToBeDone(Graphics g) {
					    
						if(point1!=null && point2!=null){
					        
							Rectangle2D.Double rect=getSVGEditor().getSVGToolkit().getComputedRectangle(point1, point2);
							Rectangle2D.Double rect2=new Rectangle2D.Double(rect.x, rect.y, rect.width, rect.height);
							Rectangle2D.Double computedBounds=fframe.getScaledRectangle(rect2, false);
							
							//draws the shape of the element that will be created if the user released the mouse button
							svgEllipse.drawGhost(fframe, (Graphics2D)g,computedBounds);
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

							svgEllipse.drawEllipse(frame, rect);							
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

