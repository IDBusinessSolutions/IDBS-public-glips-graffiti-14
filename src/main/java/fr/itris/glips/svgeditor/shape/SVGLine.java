/*
 * Created on 6 mai 2004
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

import org.apache.batik.bridge.*;
import org.apache.batik.gvt.*;
import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.resources.*;
import fr.itris.glips.svgeditor.selection.*;
import fr.itris.glips.svgeditor.undoredo.*;

/**
 * @author Jordi SUC
 * the class that allows to add, select,modify the properties, delete, transform a line on the canvas
 */
public class SVGLine extends SVGShape{

	/**
	 * the reference of an object of this class
	 */
	private final SVGLine svgLine=this;
	
	/**
	 * the action listener used to draw the line
	 */
	protected LineActionListener lineAction=null;
	
	/**
	 * the format
	 */
	protected DecimalFormat format=null;

	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGLine(SVGEditor editor) {
	    
		super(editor);
		initialize(editor);
	}
	
	   /**
     * the constructor of the class
     * @param editor the editor
     */
    protected SVGLine(SVGEditor editor, boolean initialise) {
        
        super(editor);
        if (initialise)
        {
            initialize(editor);
        }
    }
    
	
	protected void initialize(SVGEditor editor)
	{
	       ids.put("id","line"); //$NON-NLS-1$ //$NON-NLS-2$
	        ids.put("idmenuitem","Line"); //$NON-NLS-1$ //$NON-NLS-2$
	        
	        //gets the labels from the resources
	        ResourceBundle bundle=SVGEditor.getBundle();
	        
	        if(bundle!=null){
	            
	            try{
	                labels.put("label", bundle.getString("shapelinelabel")); //$NON-NLS-1$ //$NON-NLS-2$
	                labels.put("toolitemlabel", bundle.getString("shapelinetoolitemlabel")); //$NON-NLS-1$ //$NON-NLS-2$
	                labels.put("undoredocreate", bundle.getString("shapelineundoredocreate")); //$NON-NLS-1$ //$NON-NLS-2$
	                labels.put("undoredotranslate", bundle.getString("shapeundoredotranslate")); //$NON-NLS-1$ //$NON-NLS-2$
	                labels.put("undoredoresize", bundle.getString("shapelineundoredoresize")); //$NON-NLS-1$ //$NON-NLS-2$
	                labels.put("undoredomodifypoint", bundle.getString("shapelineundoredomodifypoint")); //$NON-NLS-1$ //$NON-NLS-2$
	                labels.put("undoredorotate", bundle.getString("shapelineundoredorotate")); //$NON-NLS-1$ //$NON-NLS-2$
	                labels.put("helpcreate", bundle.getString("shapelinehelpcreate")); //$NON-NLS-1$ //$NON-NLS-2$
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
	        lineAction=new LineActionListener();
	        
	        //the toggle button
	        toolItem=new JToggleButton(disabledIcon);
	        toolItem.setEnabled(false);
	        toolItem.setToolTipText(labels.get("toolitemlabel")); //$NON-NLS-1$
	        
	        //adds a listener to the menu item and the toggle button
	        menuitem.addActionListener(lineAction);
	        toolItem.addActionListener(lineAction);
	        
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
	                
	                lineAction.reset();
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
	 * draws a line
	 * @param frame the current SVGFrame
	 * @param point1 the first point
	 * @param point2 the second point
	 */
	protected void drawLine(SVGFrame frame, Point2D.Double point1, Point2D.Double point2){
		
		if(frame!=null && point1!=null && point2!=null){
		    
			Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
			
			if(getSVGEditor().getSVGSelection()!=null && doc!=null){
			    
				final Element parent=getSVGEditor().getSVGSelection().getCurrentParentElement(frame);
				
				if(parent!=null){

					Element line = createLine(frame, point1, point2, doc);
			
			        //sets that the svg has been modified
			        frame.setModified(true);
			        
					//creates final variables
					final Node fline=line;

					//attaches the line to the svg root element
					parent.appendChild(fline);
			
					putUndoRedo(frame, parent, line, fline);
				}
			}
		}
	}

    /**
     * @param frame
     * @param parent
     * @param line
     * @param fline
     */
    protected void putUndoRedo(SVGFrame frame, final Element parent, Element line, final Node fline)
    {
        //create the undo/redo action and insert it into the undo/redo stack
        if(getSVGEditor().getUndoRedo()!=null){

        	SVGUndoRedoAction action=new SVGUndoRedoAction(labels.get("undoredocreate")){ //$NON-NLS-1$

        		public void undo(){
        		    
        			parent.removeChild(fline);
        		}

        		public void redo(){
        		    
        			// attaches the circle to the svg root element
        			parent.appendChild(fline);
        		}
        	};

        	SVGSelection selection=getSVGEditor().getSVGSelection();

        	if(selection!=null){
        	    
        		selection.deselectAll(frame, false, true);
        		selection.addUndoRedoAction(frame, action);
        		selection.handleNodeSelection(frame, line);
        		selection.addUndoRedoAction(frame, new SVGUndoRedoAction((String)labels.get("undoredocreate")){}); //$NON-NLS-1$
        		selection.refreshSelection(frame);
        
        	}else{
        	    
        		SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList((String)labels.get("undoredocreate")); //$NON-NLS-1$
        		actionlist.add(action);
        		getSVGEditor().getUndoRedo().addActionList(frame, actionlist);
        	}
        }
    }

    /**
     * @param frame
     * @param point1
     * @param point2
     * @param doc
     * @return
     */
    protected Element createLine(SVGFrame frame, Point2D.Double point1, Point2D.Double point2, Document doc)
    {
        //creates the line
        Element line = doc.createElementNS(doc.getDocumentElement().getNamespaceURI(),"line"); //$NON-NLS-1$
        line.setAttributeNS(null,"x1", format.format(point1.x)); //$NON-NLS-1$
        line.setAttributeNS(null,"y1", format.format(point1.y)); //$NON-NLS-1$
        line.setAttributeNS(null,"x2", format.format(point2.x)); //$NON-NLS-1$
        line.setAttributeNS(null,"y2", format.format(point2.y)); //$NON-NLS-1$
        String colorString=SVGEditor.getColorChooser().getColorString(SVGColorManager.getCurrentColor());
        line.setAttributeNS(null, "style", "stroke:".concat(colorString.concat(";"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return line;
    }
	
	/**
	 * draws what the line will be if the user releases the mouse button
	 * @param frame the current SVGFrame
	 * @param g the graphics element
	 * @param point1 the first point
	 * @param point2 the second point
	 */
	protected void drawGhost(SVGFrame frame, Graphics2D g, Point2D.Double point1, Point2D.Double point2){
		
		if(frame!=null && g!=null && point1!=null && point2!=null){
		    
		    g=(Graphics2D)g.create();
		    
			//draws the new awt line to be displayed
			g.setColor(GHOST_COLOR);
			g.setXORMode(Color.white);
			g.drawLine((int)point1.x, (int)point1.y, (int)point2.x, (int)point2.y);
			g.dispose();
		}		
	}
	
	/**
	 * gets the nexts level after the given selection level
	 * @param type a selection level
	 * @param clicknb the number of mouse clicks
	 * @return the next selection level
	 */
	public String getNextLevel(String type){
	    
		if(type!=null){
		    
			if(type.equals("level1")){ //$NON-NLS-1$
			    
			    return "level2"; //$NON-NLS-1$
			    
			}else if(type.equals("level2")){ //$NON-NLS-1$
			    
			    return "level3"; //$NON-NLS-1$
			    
			}else if(type.equals("level3")){ //$NON-NLS-1$
			    
			    return "level1"; //$NON-NLS-1$
			}
		}
		
		return "level1"; //$NON-NLS-1$
	}
	
	/**
	 * draws the selection around the line
	 * @param frame the current SVGFrame
	 * @param graphics the graphics 
	 * @param node the node to be selected
	 * @return the list of the selection squares
	 */
	protected LinkedList drawRotateSelection(SVGFrame frame, Graphics graphics, Node node){
	    
		LinkedList squarelist=new LinkedList();
		Graphics2D g=(Graphics2D)graphics;
		
		if(g!=null && node!=null){
		    
			int sqd=5;
			Rectangle2D rect=frame.getNodeGeometryBounds((Element)node);
			
			if(rect!=null){
			    
				//computes and draws the new awt line to be displayed
				Rectangle2D.Double rect2=new Rectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
				Rectangle2D.Double scRect=frame.getScaledRectangle(rect2, false);
			
				double sx1=scRect.getX(), sy1=scRect.getY();
				//the coordinates of the selection point
				int[] sqx=new int[1];
				int[] sqy=new int[1];

				sqx[0]=(int)(sx1+scRect.getWidth()/2)-sqd;
				sqy[0]=(int)(sy1+scRect.getHeight()/2)-sqd;
			
				//the id for the selection point
				String[] types=new String[1];
				types[0]="C"; //$NON-NLS-1$
			
				//the cursor associated with the selection point
				Cursor[] cursors=new Cursor[1];
				cursors[0]=new Cursor(Cursor.HAND_CURSOR);
			
				//draws the graphic elements
				Shape shape=null;
				GradientPaint gradient=null;
				
				for(int i=0;i<1;i++){
				    
					if(editor.getSVGSelection()!=null){
					    
						squarelist.add(	new SVGSelectionSquare(node,types[i],
													new Rectangle2D.Double(sqx[i],sqy[i],2*sqd,2*sqd),
													cursors[i]));
					}		
					
					shape=getArrow(new Point2D.Double(sqx[i]+sqd, sqy[i]+sqd), types[i], ROTATE_SELECTION);
						
					if(shape!=null){
					    
						gradient=new GradientPaint(sqx[i], sqy[i], SQUARE_SELECTION_COLOR1, sqx[i]+2*sqd, sqy[i]+2*sqd, SQUARE_SELECTION_COLOR2, true);
						g.setPaint(gradient);
						g.fill(shape);
						g.setColor(LINE_SELECTION_COLOR);
						g.draw(shape);
					}
				}
			}
		}
		
		return squarelist;
	}
	
	/**
	 * draws the selection for the line
	 * @param frame the current SVGFrame
	 * @param graphics the graphics 
	 * @param nde the node to be selected
	 * @return the list of the selection squares
	 */
	protected LinkedList drawModifyPointsSelection(SVGFrame frame, Graphics graphics, Node nde){
	    
		LinkedList squarelist=new LinkedList();
		Element node=(Element)nde;
		Graphics2D g=(Graphics2D)graphics;
		
		if(g!=null && node!=null){
		    
			Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
			
			Element root=null;
			
			if(doc!=null){
			    
			    root=doc.getDocumentElement();
			}
			
			if(root!=null){
			    
				int sqd=5;
				double x1=0, y1=0, x2=0, y2=0;

				try{
					x1=new Double(node.getAttributeNS(null,"x1")).doubleValue(); //$NON-NLS-1$
					y1=new Double(node.getAttributeNS(null,"y1")).doubleValue(); //$NON-NLS-1$
					x2=new Double(node.getAttributeNS(null,"x2")).doubleValue(); //$NON-NLS-1$
					y2=new Double(node.getAttributeNS(null,"y2")).doubleValue(); //$NON-NLS-1$
				}catch (Exception ex){}

				//computes the transformed points
				BridgeContext ctxt=frame.getScrollPane().getSVGCanvas().getBridgeContext();
				
				if(ctxt!=null){
				    
					Point2D pt1=new Point2D.Double(), pt2=new Point2D.Double();
					GraphicsNode gnode=ctxt.getGraphicsNode(node);
					
					if(gnode!=null){
					    
						AffineTransform af=new AffineTransform();
						try{af.preConcatenate(gnode.getTransform());}catch (Exception ex){}
						try{af.preConcatenate(frame.getScrollPane().getSVGCanvas().getViewingTransform());}catch (Exception ex){}
						try{af.preConcatenate(frame.getScrollPane().getSVGCanvas().getRenderingTransform());}catch (Exception ex){}
						
						if(af!=null){
						    
							try{
								pt2=af.transform(new Point2D.Double(x2,y2), null);
								pt1=af.transform(new Point2D.Double(x1,y1), null);
							}catch (Exception e){}
							
							if(pt1!=null && pt2!=null){
							    
								x1=pt1.getX();
								y1=pt1.getY();
								x2=pt2.getX();
								y2=pt2.getY();
							}
						}
					}
				}
				
				//computes the coordinates of the selection squares
				Point2D.Double 	point1=new Point2D.Double(x1,y1), 
										point2=new Point2D.Double(x2,y2);
			
				double sx1=point1.x, sy1=point1.y, sx2=point2.x, sy2=point2.y; 
			
				int[] sqx=new int[2];
				int[] sqy=new int[2];
			
				double	sin=(sy2-sy1)/(2*Math.sqrt(Math.pow((sx2-sx1)/2,2)+Math.pow((sy2-sy1)/2,2))),
								cos=(sx2-sx1)/(2*Math.sqrt(Math.pow((sx2-sx1)/2,2)+Math.pow((sy2-sy1)/2,2)));

				//computes the coordinates of the two selection points
				sqx[0]=(int)(sx1+(-sqd/2*cos-sqd));
				sqx[1]=(int)(sx2+(sqd/2*cos-sqd));

				sqy[0]=(int)(sy1+(-sqd/2*sin-sqd));
				sqy[1]=(int)(sy2+(sqd/2*sin-sqd));
			
				//the ids of the selection points
				String[] types=new String[2];
				types[0]="Begin"; //$NON-NLS-1$
				types[1]="End"; //$NON-NLS-1$
			
				//the cursors of the selection points
				Cursor[] cursors=new Cursor[2];
				cursors[0]=new Cursor(Cursor.HAND_CURSOR);
				cursors[1]=new Cursor(Cursor.HAND_CURSOR);
			
				//draws the graphic elements for the selection
				Shape shape=null;
				GradientPaint gradient=null;

				for(int i=0;i<2;i++){
					
					if(editor.getSVGSelection()!=null){
					    
						squarelist.add(new SVGSelectionSquare(node, types[i], 
													new Rectangle2D.Double(sqx[i],sqy[i],2*sqd,2*sqd), 
													cursors[i]));
					}
						
					shape=getArrow(new Point2D.Double(sqx[i]+sqd, sqy[i]+sqd), types[i], REGULAR_SELECTION);
						
					if(shape!=null){
					    
						gradient=new GradientPaint(sqx[i], sqy[i], SQUARE_SELECTION_COLOR1, sqx[i]+2*sqd, sqy[i]+2*sqd, SQUARE_SELECTION_COLOR2, true);
						g.setPaint(gradient);
						g.fill(shape);
						g.setColor(LINE_SELECTION_COLOR);
						g.draw(shape);
					}
				}				
			}
		}	
		
		return squarelist;		
	}
	
	/**
	 * fills the given shape
	 * @param g a graphics
	 * @param shape a shape
	 */
	protected void fillShape(Graphics2D g, Shape shape){
	    
	    if(g!=null && shape!=null){
	        
	        g.setColor(OUTLINE_COLOR);
	        g.draw(shape);
	    }
	}
	
	/**
	 * the method to modify a point of a node
	 * @param frame the current SVGFrame
	 * @param square the selection square
	 * @param point1 the first point clicked
	 * @param point2 the second point clicked
	 */
	public void modifyPoint(SVGFrame frame, SVGSelectionSquare square, Point2D.Double point1, Point2D.Double point2){
	    
		if(frame!=null && square!=null && square.getNode()!=null && square.getNode() instanceof Element && point2!=null){
			
			//gets the paintlistener associated with the frame
			CanvasPaintListener paintListener=null;
			try{
				paintListener=modifyPointFrameTable.get(frame);
			}catch (Exception ex){paintListener=null;}
	
			//getting the node and the two points
			Element elt=(Element)square.getNode();
			double x1=0, y1=0, x2=0, y2=0;
			
			try{
			    x1=Double.parseDouble(elt.getAttribute("x1")); //$NON-NLS-1$
			    y1=Double.parseDouble(elt.getAttribute("y1")); //$NON-NLS-1$
			    x2=Double.parseDouble(elt.getAttribute("x2")); //$NON-NLS-1$
			    y2=Double.parseDouble(elt.getAttribute("y2")); //$NON-NLS-1$
			}catch (Exception ex){}

			//the matrix transform
			SVGTransformMatrix matrix=getSVGEditor().getSVGToolkit().getTransformMatrix(elt);

			//the path
			GeneralPath path=new GeneralPath();
			path.moveTo((float)x1, (float)y1);
			path.lineTo((float)x2, (float)y2);
			
			//the transform
			AffineTransform af=null;
			
			//transforming the outline
			if(matrix!=null && matrix.getTransform()!=null){
			    
				af=matrix.getTransform();
				
				try{
				    path=(GeneralPath)af.createTransformedShape(path);
				}catch (Exception ex){}

				//modifying the moved point
				PathIterator it=path.getPathIterator(new AffineTransform());
				double[] seg=new double[6];
				
				if(square.getType().equals("Begin")){ //$NON-NLS-1$
				    
				    //the first point
				    x1=point2.x;
				    y1=point2.y;
				    
				    //getting the second point
				    it.next();
				    it.currentSegment(seg);
				    x2=seg[0];
				    y2=seg[1];
				    
				}else if(square.getType().equals("End")){ //$NON-NLS-1$
				    
				    //getting the first point
				    it.currentSegment(seg);
				    x1=seg[0];
				    y1=seg[1];
				    
				    //the second point
				    x2=point2.x;
				    y2=point2.y;
				}
			}
			
			//the outline
			GeneralPath endPath=new GeneralPath();
			endPath.moveTo((float)x1, (float)y1);
			endPath.lineTo((float)x2, (float)y2);

			if(endPath!=null){

				//concatenates the transforms to draw the outline
				af=new AffineTransform();
				
				try{af.preConcatenate(frame.getScrollPane().getSVGCanvas().getViewingTransform());}catch (Exception ex){}
				try{af.preConcatenate(frame.getScrollPane().getSVGCanvas().getRenderingTransform());}catch (Exception ex){}

				//computing the outline
				Shape outline=af.createTransformedShape(endPath);

				final Shape foutline=outline;
					
				//removes the paint listener
				if(paintListener!=null){
				    
					frame.getScrollPane().getSVGCanvas().removePaintListener(paintListener, false);
				}

				//creates and sets the paint listener
				paintListener=new CanvasPaintListener(){
				    
					public void paintToBeDone(Graphics g) {

						Graphics2D g2=(Graphics2D)g;

						fillShape(g2, foutline);
					}
				};

			   frame.getScrollPane().getSVGCanvas().addLayerPaintListener(SVGCanvas.DRAW_LAYER, paintListener, true);
			   modifyPointFrameTable.put(frame, paintListener);
			}
		}
	}
	
	/**
	 * validates the modifyPoint method
	 * @param frame the current SVGFrame
	 * @param square the selection square
	 * @param point1 the first point clicked
	 * @param point2 the second point clicked
	 */
	public void validateModifyPoint(SVGFrame frame, SVGSelectionSquare square, Point2D.Double point1, Point2D.Double point2){
	    
		if(frame!=null && square!=null && square.getNode()!=null && square.getNode() instanceof Element && point2!=null){
		    
			//gets the paintlistener associated with the frame
			CanvasPaintListener paintListener=null;
			SVGFrame f=null;
			
			for(Iterator it=new LinkedList(modifyPointFrameTable.keySet()).iterator(); it.hasNext();){
			    
				try{
				    f=(SVGFrame)it.next();
					paintListener=(CanvasPaintListener)modifyPointFrameTable.get(f);
				}catch (Exception ex){paintListener=null;}
				
				if(paintListener!=null){
				    
				    modifyPointFrameTable.remove(frame);
					frame.getScrollPane().getSVGCanvas().removePaintListener(paintListener, false);
				}
			}

			//getting the node and the two points
			final Element elt=(Element)square.getNode();
			double x1=0, y1=0, x2=0, y2=0;
			
			try{
			    x1=Double.parseDouble(elt.getAttribute("x1")); //$NON-NLS-1$
			    y1=Double.parseDouble(elt.getAttribute("y1")); //$NON-NLS-1$
			    x2=Double.parseDouble(elt.getAttribute("x2")); //$NON-NLS-1$
			    y2=Double.parseDouble(elt.getAttribute("y2")); //$NON-NLS-1$
			}catch (Exception ex){}
			
			final double initX1=x1, initY1=y1, initX2=x2, initY2=y2;
			    
			//the matrix transform
			final SVGTransformMatrix matrix=getSVGEditor().getSVGToolkit().getTransformMatrix(elt);

			//the path
			GeneralPath path=new GeneralPath();
			path.moveTo((float)x1, (float)y1);
			path.lineTo((float)x2, (float)y2);
			
			//the transform
			AffineTransform af=null;
			
			//transforming the outline
			if(matrix!=null && matrix.getTransform()!=null){
			    
				af=matrix.getTransform();
				
				try{
				    path=(GeneralPath)af.createTransformedShape(path);
				}catch (Exception ex){}

				//modifying the moved point
				PathIterator it=path.getPathIterator(new AffineTransform());
				double[] seg=new double[6];
				
				if(square.getType().equals("Begin")){ //$NON-NLS-1$
				    
				    //the first point
				    x1=point2.x;
				    y1=point2.y;
				    
				    //getting the second point
				    it.next();
				    it.currentSegment(seg);
				    x2=seg[0];
				    y2=seg[1];
				    
				}else if(square.getType().equals("End")){ //$NON-NLS-1$
				    
				    //getting the first point
				    it.currentSegment(seg);
				    x1=seg[0];
				    y1=seg[1];
				    
				    //the second point
				    x2=point2.x;
				    y2=point2.y;
				}
			}
			
			//storing the new coordinates values
			final double fx1=x1, fy1=y1, fx2=x2, fy2=y2;
			
			//applying the modifications
			elt.setAttributeNS(null,"x1", format.format(x1)); //$NON-NLS-1$
			elt.setAttributeNS(null,"y1", format.format(y1)); //$NON-NLS-1$
			elt.setAttributeNS(null,"x2", format.format(x2)); //$NON-NLS-1$
			elt.setAttributeNS(null,"y2", format.format(y2)); //$NON-NLS-1$
			getSVGEditor().getSVGToolkit().setTransformMatrix(elt, new SVGTransformMatrix(1, 0, 0, 1, 0, 0));

			final SVGFrame fframe=frame;
			
			//create the undo/redo action and insert it into the undo/redo stack
			if(getSVGEditor().getUndoRedo()!=null){

				SVGUndoRedoAction action=new SVGUndoRedoAction((String)labels.get("undoredomodifypoint")){ //$NON-NLS-1$

					public void undo(){
					    
						//sets the matrix of the node with the old matrix
						getSVGEditor().getSVGToolkit().setTransformMatrix(elt, matrix);
						
						elt.setAttributeNS(null,"x1", format.format(initX1)); //$NON-NLS-1$
						elt.setAttributeNS(null,"y1", format.format(initY1)); //$NON-NLS-1$
						elt.setAttributeNS(null,"x2", format.format(initX2)); //$NON-NLS-1$
						elt.setAttributeNS(null,"y2", format.format(initY2)); //$NON-NLS-1$
						
						//notifies that the selection has changed
						if(getSVGEditor().getSVGSelection()!=null){
							
							getSVGEditor().getSVGSelection().selectionChanged(true);
						}
					}

					public void redo(){
					    
						//sets the matrix of the node with the old matrix
						getSVGEditor().getSVGToolkit().setTransformMatrix(elt, new SVGTransformMatrix(1, 0, 0, 1, 0, 0));
					    
						elt.setAttributeNS(null,"x1", format.format(fx1)); //$NON-NLS-1$
						elt.setAttributeNS(null,"y1", format.format(fy1)); //$NON-NLS-1$
						elt.setAttributeNS(null,"x2", format.format(fx2)); //$NON-NLS-1$
						elt.setAttributeNS(null,"y2", format.format(fy2)); //$NON-NLS-1$
						
						//notifies that the selection has changed
						if(getSVGEditor().getSVGSelection()!=null){
							
							getSVGEditor().getSVGSelection().selectionChanged(true);
						}
					}

				};
				
				//gets or creates the undo/redo list and adds the action into it
				SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList((String)labels.get("undoredomodifypoint")); //$NON-NLS-1$
				actionlist.add(action);

				getSVGEditor().getUndoRedo().addActionList(frame, actionlist);
			}
		}
	    
	}		

	/**
	 * used to remove the listener added to draw a line when the user clicks on the menu item
	 */
	public void cancelActions(){
	    
		if(lineAction!=null){
		    
			toolItem.removeActionListener(lineAction);
			toolItem.setSelected(false);
			toolItem.addActionListener(lineAction);
			
		    lineAction.cancelActions();
		}
	}
	
	/**
	 * 
	 * @author Jordi SUC
	 * the class allowing to get the position and size of the future drawn line
	 */
	protected class LineActionListener implements ActionListener{
		
		/**
		 * the hashtable associating a frame to its mouse adapter
		 */
		private final Hashtable mouseAdapterFrames=new Hashtable();
		
		/**
		 * an instance of this class
		 */
		private final LineActionListener action=this;
		
		/**
		 * the cursor used when creating a rectangle
		 */
		private Cursor createCursor;
		
		private boolean isActive=false;
		
		/**
		 * the source component
		 */
		private Object source=null;
		
		/**
		 * the constructor of the class
		 */
		public LineActionListener(){
			
			createCursor=getSVGEditor().getCursors().getCursor("line"); //$NON-NLS-1$
		}
		
		/**
		 * resets the listener
		 */
		public void reset(){
			
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
		
				
				LineMouseListener iml=null;
				
				//adds the new motion adapters
				for(it=frames.iterator(); it.hasNext();){
				    
					try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
			
					if(frm!=null && ! mouseAdapterFrames.containsKey(frm)){
				
						iml=new LineMouseListener(frm);

						try{
							frm.getScrollPane().getSVGCanvas().addMouseListener(iml);
							frm.getScrollPane().getSVGCanvas().addMouseMotionListener(iml);
							frm.getScrollPane().getSVGCanvas().setSVGCursor(createCursor);
						}catch (Exception ex){}
						
						mouseAdapterFrames.put(frm, iml);
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
							
							if(mouseListener!=null && ((LineMouseListener)mouseListener).paintListener!=null){
								//removes the paint listener
								frm.getScrollPane().getSVGCanvas().removePaintListener(((LineMouseListener)mouseListener).paintListener, true);
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
					
					toolItem.removeActionListener(lineAction);
					toolItem.setSelected(true);
					toolItem.addActionListener(lineAction);
			
					//the listener is active
					isActive=true;
					source=evt.getSource();
	
					Collection frames=getSVGEditor().getFrameManager().getFrames();
					Iterator it;
					SVGFrame frm=null;
					LineMouseListener lml=null;
					    
					//adds the new motion adapters
					for(it=frames.iterator(); it.hasNext();){
					    
						try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
					
						if(frm!=null){
	
							lml=new LineMouseListener(frm);
	
							try{
								frm.getScrollPane().getSVGCanvas().addMouseListener(lml);
								frm.getScrollPane().getSVGCanvas().addMouseMotionListener(lml);
								frm.getScrollPane().getSVGCanvas().setSVGCursor(createCursor);
							}catch (Exception ex){}
							
							mouseAdapterFrames.put(frm, lml);
						}
					}
				}
			}
		}
			
		protected class LineMouseListener extends MouseAdapter implements MouseMotionListener{
		
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
			public LineMouseListener(SVGFrame frame){
			    
				this.frame=frame;
				final SVGFrame fframe=frame;
				
				//adds a paint listener
				paintListener=new CanvasPaintListener(){

					public void paintToBeDone(Graphics g) {

						if(point1!=null && point2!=null){
							
							Point2D.Double 	pt1=fframe.getScaledPoint(new Point2D.Double(point1.x, point1.y), false),
																pt2=fframe.getScaledPoint(new Point2D.Double(point2.x, point2.y), false);
							
							if(pt1!=null && pt2!=null){
							    
								//draws the shape of the element that will be created if the user released the mouse button
								svgLine.drawGhost(fframe, (Graphics2D)g, pt1, pt2);
							}
						}
					}
				};
				
				frame.getScrollPane().getSVGCanvas().addLayerPaintListener(SVGCanvas.DRAW_LAYER, paintListener, false);
			}
			
			public void mouseMoved(MouseEvent e) {
				
			}

			public void mouseDragged(MouseEvent evt) {
				
			    if(evt.isControlDown()){
			        
			        point2=SVGToolkit.computeLinePointWhenCtrlDown(point1, frame.getAlignedWithRulersPoint(evt.getPoint()));
			    
			    }else{
			        
					//sets the second point of the element
					point2=frame.getAlignedWithRulersPoint(evt.getPoint());
			    }

				//asks the canvas to be repainted to draw the shape of the future element
				frame.getScrollPane().getSVGCanvas().delayedRepaint();
			}
				
			@Override
			public void mousePressed(MouseEvent evt){
			    
				//sets the first point of the area corresponding to the future element
				point1=frame.getAlignedWithRulersPoint(evt.getPoint());
			}
				
			@Override
			public void mouseReleased(MouseEvent evt){
				
				Point2D.Double pt=null;
				
			    if(evt.isControlDown()){
			        
			        pt=SVGToolkit.computeLinePointWhenCtrlDown(point1, frame.getAlignedWithRulersPoint(evt.getPoint()));
			        
			    }else{
			        
					//sets the second point of the element
					pt=frame.getAlignedWithRulersPoint(evt.getPoint());
			    }
			    
			    final Point2D.Double point=pt, fpoint1=point1;
				
				//creates the element in the SVG document
				if(point1!=null && point!=null){

					Runnable runnable=new Runnable(){
						
						public void run() {

							svgLine.drawLine(frame, fpoint1, point);
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
