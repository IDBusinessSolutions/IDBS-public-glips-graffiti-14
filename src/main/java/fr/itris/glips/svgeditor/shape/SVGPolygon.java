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
 * the class that allows to add, select,modify the properties, delete, transform a polygon on the canvas
 */
public class SVGPolygon extends SVGShape{

	/**
	 * the reference of an object of this class
	 */
	private final SVGPolygon svgPolygon=this;
	
	/**
	 * the format
	 */
	private DecimalFormat format=null;
	
	/**
	 * the action listener used to draw the polygon
	 */
	private PolygonActionListener polygonAction=null;
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGPolygon(SVGEditor editor) {
	    
		super(editor);
		
		ids.put("id","polygon"); //$NON-NLS-1$ //$NON-NLS-2$
		ids.put("idmenuitem","Polygon"); //$NON-NLS-1$ //$NON-NLS-2$
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labels.put("label", bundle.getString("shapepolygonlabel")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredocreate", bundle.getString("shapepolygonundoredocreate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredotranslate", bundle.getString("shapeundoredotranslate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredoresize", bundle.getString("shapepolygonundoredoresize")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredomodifypoint", bundle.getString("shapepolygonundoredomodifypoint")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredorotate", bundle.getString("shapepolygonundoredorotate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("helpcreate", bundle.getString("shapepolygonhelpcreate")); //$NON-NLS-1$ //$NON-NLS-2$
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
		polygonAction=new PolygonActionListener();
		
		//the toggle button
		toolItem=new JToggleButton(disabledIcon);
		toolItem.setEnabled(false);
		toolItem.setToolTipText((String)labels.get("label")); //$NON-NLS-1$
		
		//adds a listener to the menu item and the toggle button
		menuitem.addActionListener(polygonAction);
		toolItem.addActionListener(polygonAction);
		
		//a listener that listens to the changes of the SVGFrames
		final ActionListener svgframeListener=new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				if(getSVGEditor().getFrameManager().getFrameNumber()>0){
				    
					menuitem.setEnabled(true);
					//menuitem.setIcon(icon);
					toolItem.setEnabled(true);
					toolItem.setIcon(icon);

				}else{
				    
					menuitem.setEnabled(false);
					//menuitem.setIcon(disabledIcon);
					toolItem.setEnabled(false);
					toolItem.setIcon(disabledIcon);
				}
				
				polygonAction.reset();
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
	 * draws a polygon
	 * @param frame the current SVGFrame
	 * @param points the array of points
	 */
	protected void drawPolygon(SVGFrame frame, Point2D.Double[] points){
		
		if(frame!=null && points!=null && points.length>1){
			
			Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
			
			if(getSVGEditor().getSVGSelection()!=null && doc!=null){
			    
				final Element parent=getSVGEditor().getSVGSelection().getCurrentParentElement(frame);
				
				if(parent!=null){

					String value=""; //$NON-NLS-1$

					for(int i=0;i<points.length;i++){
					    
						value=value.concat(format.format(points[i].getX())+","+format.format(points[i].getY())+" "); //$NON-NLS-1$ //$NON-NLS-2$
					}
			
					//creates the polygon
					final Element polygon=doc.createElementNS(doc.getDocumentElement().getNamespaceURI(),"polygon"); //$NON-NLS-1$

					polygon.setAttributeNS(null,"points",value); //$NON-NLS-1$
					String colorString=getSVGEditor().getColorChooser().getColorString(getSVGEditor().getSVGColorManager().getCurrentColor());
					polygon.setAttributeNS(null, "style", "fill:".concat(colorString.concat(";stroke:none;"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
					//sets that the svg has been modified
					frame.setModified(true);
			
					//creates final variables
					final SVGFrame fframe=frame;

					// attaches the element to the svg parent element	
					parent.appendChild(polygon);
			
					//create the undo/redo action and insert it into the undo/redo stack
					if(getSVGEditor().getUndoRedo()!=null){

						SVGUndoRedoAction action=new SVGUndoRedoAction((String)labels.get("undoredocreate")){ //$NON-NLS-1$

							public void undo(){
							    
							    parent.removeChild(polygon);
							}
							
							public void redo(){
							    
							    parent.appendChild(polygon);
							}
						};
				
						SVGSelection selection=getSVGEditor().getSVGSelection();
				
						if(selection!=null){
						    
							selection.deselectAll(frame, false, true);
							selection.addUndoRedoAction(frame, action);
							selection.handleNodeSelection(frame, polygon);
							selection.addUndoRedoAction(frame, new SVGUndoRedoAction((String)labels.get("undoredocreate")){}); //$NON-NLS-1$
							selection.refreshSelection(frame);
					
						}else{
						    
							SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList((String)labels.get("undoredocreate")); //$NON-NLS-1$
							actionlist.add(action);
							getSVGEditor().getUndoRedo().addActionList(frame, actionlist);
						}
					}
				}
			}
		}
	}
	
	/**
	 * draws what the polygon will be if the user double clicks
	 * @param frame the current SVGFrame
	 * @param g the graphics element
	 * @param points an array of points
	 */
	protected void drawGhost(SVGFrame frame, Graphics2D g, Point2D.Double[] points){
		
		if(frame!=null && points!=null && points.length>1 && g!=null){

		    g=(Graphics2D)g.create();
		    
			//draws the new awt polygon to be displayed
			g.setColor(GHOST_COLOR);
			g.setXORMode(Color.white);

			for(int i=1;i<points.length;i++){
			    
				g.drawLine((int)points[i-1].x, (int)points[i-1].y, (int)points[i].x, (int)points[i].y);
			}
			
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
	 * draws the selection for the polygon
	 * @param frame the current SVGFrame
	 * @param graphics the graphics 
	 * @param nde the node to be selected
	 * @return the list of the selection squares
	 */
	protected LinkedList<SVGSelectionSquare> drawModifyPointsSelection(SVGFrame frame, Graphics graphics, Node nde){

		LinkedList squarelist=new LinkedList();
		Element node=(Element)nde;
		Graphics2D g=(Graphics2D)graphics;
		
		if(frame!=null && g!=null && node!=null){
			
			int sqd=5;
			String pts=node.getAttributeNS(null,"points"); //$NON-NLS-1$
			
			if(pts!=null && ! pts.equals("")){ //$NON-NLS-1$
			    
				double px=0, py=0;
				//gets the coordinates of the points
				Point2D.Double[] points=null;
				ArrayList list=new ArrayList();
				int i=0;
				
				pts=pts.replaceAll("[,]"," "); //$NON-NLS-1$ //$NON-NLS-2$
				
				//removes the first space characters from the string
				if(!pts.equals("") && pts.charAt(0)==' '){ //$NON-NLS-1$
				    
				    pts=pts.replaceFirst("[\\s]+",""); //$NON-NLS-1$ //$NON-NLS-2$
				}
				 
				while(! pts.equals("")){ //$NON-NLS-1$
				    
					try{
						px=new Double(pts.substring(0,pts.indexOf(' '))).doubleValue();
						pts=pts.substring(pts.indexOf(' ')+1,pts.length());
						
						if(!pts.equals("") && pts.charAt(0)==' '){ //$NON-NLS-1$
						    
						    pts=pts.replaceFirst("[\\s]+",""); //$NON-NLS-1$ //$NON-NLS-2$
						}
						
						py=new Double(pts.substring(0,pts.indexOf(' '))).doubleValue();
						pts=pts.substring(pts.indexOf(' ')+1,pts.length());
						
						if(!pts.equals("") && pts.charAt(0)==' '){ //$NON-NLS-1$
						    
						    pts=pts.replaceFirst("[\\s]+",""); //$NON-NLS-1$ //$NON-NLS-2$
						}
						
						list.add(new Point2D.Double(px, py));
					}catch (Exception ex){break;}
				}
				
				//the values of the coordinates of the points
				points=new Point2D.Double[list.size()];
				
				for(i=0;i<list.size();i++){
				    
				    points[i]=(Point2D.Double)list.get(i);
				}
				
				if(points!=null && points.length>0){
				    
					//computes the transformed points
					BridgeContext ctxt=frame.getScrollPane().getSVGCanvas().getBridgeContext();
					
					if(ctxt!=null){
					    
						GraphicsNode gnode=ctxt.getGraphicsNode(node);
						
						if(gnode!=null){
						    
							AffineTransform af=new AffineTransform();
							try{af.preConcatenate(gnode.getTransform());}catch (Exception ex){}
							try{af.transform(points,0,points,0,points.length);}catch (Exception e){}
						}
					}
					
					//computes the coordinates of the selection squares
					Point2D.Double[] scpoints=new Point2D.Double[points.length];
					
					for(i=0;i<points.length;i++){
					    
						scpoints[i]=frame.getScaledPoint(points[i], false);
					}

					//the cursor associated with the selection points
					Cursor cursor=new Cursor(Cursor.HAND_CURSOR);
		
					//draws the selection
					Shape shape=null;
					GradientPaint gradient=null;
					int sqx=0,sqy=0;
					
					for(i=0;i<scpoints.length;i++){
			
						sqx=(int)(scpoints[i].getX()-sqd);
						sqy=(int)(scpoints[i].getY()-sqd);
			
						if(getSVGEditor().getSVGSelection()!=null){
						    
							squarelist.add(new SVGSelectionSquare(node, new Integer(i).toString(),
														new Rectangle2D.Double(sqx, sqy, 2*sqd, 2*sqd),
														cursor));
						}
						
						shape=getArrow(new Point2D.Double(sqx+sqd, sqy+sqd), "P", REGULAR_SELECTION); //$NON-NLS-1$
					
						if(shape!=null){
						    
							gradient=new GradientPaint(sqx, sqy, SQUARE_SELECTION_COLOR1, sqx+2*sqd, sqy+2*sqd, SQUARE_SELECTION_COLOR2, true);
							g.setPaint(gradient);
							g.fill(shape);
							g.setColor(LINE_SELECTION_COLOR);
							g.draw(shape);
						}
					}	
				}
			}
		}
		
		return squarelist;		
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
			try{paintListener=modifyPointFrameTable.get(frame);}catch (Exception ex){paintListener=null;}
			
			Element elt=(Element)square.getNode();
			String pts=elt.getAttributeNS(null,"points"); //$NON-NLS-1$
			
			if(pts!=null && ! pts.equals("")){ //$NON-NLS-1$
			
				double px=0, py=0;
				
				//gets the coordinates of the points
				ArrayList listpt=new ArrayList();
				int i=0;
				
				pts=pts.replaceAll("[,]"," "); //$NON-NLS-1$ //$NON-NLS-2$
				
				//removes the first space characters from the string
				if(!pts.equals("") && pts.charAt(0)==' '){ //$NON-NLS-1$
				    
				    pts=pts.replaceFirst("[\\s]+",""); //$NON-NLS-1$ //$NON-NLS-2$
				}
				 
				while(! pts.equals("")){ //$NON-NLS-1$
				    
					try{
						px=new Double(pts.substring(0,pts.indexOf(' '))).doubleValue();
						pts=pts.substring(pts.indexOf(' ')+1,pts.length());
						
						if(!pts.equals("") && pts.charAt(0)==' '){ //$NON-NLS-1$
						    
						    pts=pts.replaceFirst("[\\s]+",""); //$NON-NLS-1$ //$NON-NLS-2$
						}
						
						py=new Double(pts.substring(0,pts.indexOf(' '))).doubleValue();
						pts=pts.substring(pts.indexOf(' ')+1,pts.length());
						
						if(!pts.equals("") && pts.charAt(0)==' '){ //$NON-NLS-1$
						    
						    pts=pts.replaceFirst("[\\s]+",""); //$NON-NLS-1$ //$NON-NLS-2$
						}
						
						listpt.add(new Point2D.Double(px, py));
					}catch (Exception ex){break;}
				}
				
				//the transform matrix
				SVGTransformMatrix matrix=getSVGEditor().getSVGToolkit().getTransformMatrix(elt);
				
				//the affine transform
				AffineTransform af=matrix.getTransform();
				
				//the path will be displayed
				GeneralPath path=new GeneralPath();
				Point2D.Double pt0=(Point2D.Double)listpt.get(0), pt=null;
				
				//the initial point
				path.moveTo((float)pt0.x, (float)pt0.y);
				
				//building the path
				for(i=1;i<listpt.size();i++){
				    
				    pt=(Point2D.Double)listpt.get(i);
				    path.lineTo((float)pt.x, (float)pt.y);
				}
				
				//transforming the path//
				try{
				    path=(GeneralPath)af.createTransformedShape(path);
				}catch (Exception ex){}
				
				//the index of the moved point
				int index=-1;
				
				try{
				    index=Integer.parseInt(square.getType());
				}catch(Exception ex){}
				
				//creating the new path
				GeneralPath endPath=new GeneralPath();
				float[] seg=new float[6];
				i=0;

				for(PathIterator it=path.getPathIterator(new AffineTransform()); ! it.isDone(); i++){
				    
				    //getting the current segment
				    it.currentSegment(seg);
				    it.next();
				    
				    try{
					    if(i==0 && i!=index){
					        
					        endPath.moveTo(seg[0], seg[1]);
					        
					    }else if(i>0 && i!=index){
					        
					        endPath.lineTo(seg[0], seg[1]);
					        
					        //modifying the moved point
					    }else if(index>=0 && index==i){
					        
					        if(i==0){
					            
						        endPath.moveTo((float)point2.x, (float)point2.y);
						        
					        }else if(i>0){
					            
						        endPath.lineTo((float)point2.x, (float)point2.y);
					        }
					    }
				    }catch (Exception ex){}
				}

				//computes the scale and translate values
				if(endPath!=null){
					
				    //closing the path
				    endPath.closePath();
				   
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
			
			final Element elt=(Element)square.getNode();
			final String initPts=elt.getAttributeNS(null,"points"); //$NON-NLS-1$
			String pts=new String(initPts);
			
			if(pts!=null && ! pts.equals("")){ //$NON-NLS-1$
			
				double px=0, py=0;
				
				//gets the coordinates of the points
				ArrayList listpt=new ArrayList();
				int i=0;
				
				pts=pts.replaceAll("[,]"," "); //$NON-NLS-1$ //$NON-NLS-2$
				
				//removes the first space characters from the string
				if(!pts.equals("") && pts.charAt(0)==' '){ //$NON-NLS-1$
				    
				    pts=pts.replaceFirst("[\\s]+",""); //$NON-NLS-1$ //$NON-NLS-2$
				}
				 
				while(!pts.equals("")){ //$NON-NLS-1$
				    
					try{
						px=new Double(pts.substring(0,pts.indexOf(' '))).doubleValue();
						pts=pts.substring(pts.indexOf(' ')+1,pts.length());
						
						if(!pts.equals("") && pts.charAt(0)==' '){ //$NON-NLS-1$
						    
						    pts=pts.replaceFirst("[\\s]+",""); //$NON-NLS-1$ //$NON-NLS-2$
						}
						
						py=new Double(pts.substring(0,pts.indexOf(' '))).doubleValue();
						pts=pts.substring(pts.indexOf(' ')+1,pts.length());
						
						if(!pts.equals("") && pts.charAt(0)==' '){ //$NON-NLS-1$
						    
						    pts=pts.replaceFirst("[\\s]+",""); //$NON-NLS-1$ //$NON-NLS-2$
						}
						
						listpt.add(new Point2D.Double(px, py));
					}catch (Exception ex){break;}
				}
				
				//the transform matrix
				final SVGTransformMatrix matrix=getSVGEditor().getSVGToolkit().getTransformMatrix(elt);
				
				//the affine transform
				AffineTransform af=matrix.getTransform();
				
				//the path will be displayed
				GeneralPath path=new GeneralPath();
				Point2D.Double pt0=(Point2D.Double)listpt.get(0), pt=null;
				
				//the initial point
				path.moveTo((float)pt0.x, (float)pt0.y);
				
				//building the path
				for(i=1;i<listpt.size();i++){
				    
				    pt=(Point2D.Double)listpt.get(i);
				    path.lineTo((float)pt.x, (float)pt.y);
				}
				
				//transforming the path//
				try{
				    path=(GeneralPath)af.createTransformedShape(path);
				}catch (Exception ex){}
				
				//the index of the moved point
				int index=-1;
				
				try{
				    index=Integer.parseInt(square.getType());
				}catch(Exception ex){}
				
				//creating the new string path
				String points=""; //$NON-NLS-1$
				float[] seg=new float[6];
				i=0;

				for(PathIterator it=path.getPathIterator(new AffineTransform()); ! it.isDone();){
				    
				    //getting the current segment
				    it.currentSegment(seg);
				    it.next();
				        
			        if(index!=i){
			            
			            points=points.concat(format.format(seg[0]).concat(" , ").concat(format.format(seg[1]).concat(" "))); //$NON-NLS-1$ //$NON-NLS-2$
			            
			        }else{
			            
			            points=points.concat(format.format(point2.x).concat(" , ").concat(format.format(point2.y).concat(" "))); //$NON-NLS-1$ //$NON-NLS-2$
			        }
				    
				    i++;
				}

				//computes the scale and translate values
				if(points!=null && ! points.equals("")){ //$NON-NLS-1$
				    
				    //applying the modifications
					getSVGEditor().getSVGToolkit().setTransformMatrix(elt, new SVGTransformMatrix(1, 0, 0, 1, 0, 0));
					elt.setAttributeNS(null, "points", points); //$NON-NLS-1$
					
					//create the undo/redo action and insert it into the undo/redo stack
					if(getSVGEditor().getUndoRedo()!=null){

					    final String fpoints=points;

						SVGUndoRedoAction action=new SVGUndoRedoAction((String)labels.get("undoredomodifypoint")){ //$NON-NLS-1$

							public void undo(){
							    
								getSVGEditor().getSVGToolkit().setTransformMatrix(elt, matrix);
								elt.setAttributeNS(null, "points", initPts); //$NON-NLS-1$
								
								//notifies that the selection has changed
								if(getSVGEditor().getSVGSelection()!=null){
									
									getSVGEditor().getSVGSelection().selectionChanged(true);
								}
							}

							public void redo(){
							    
								getSVGEditor().getSVGToolkit().setTransformMatrix(elt, new SVGTransformMatrix(1, 0, 0, 1, 0, 0));
								elt.setAttributeNS(null, "points", fpoints); //$NON-NLS-1$
								
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
						actionlist=null;
					}
				}
			}
		}
	}
	
	/**
	 * used to remove the listener added to draw a polygon when the user clicks on the menu item
	 */
	public void cancelActions(){
	    
		if(polygonAction!=null) {
			
			toolItem.removeActionListener(polygonAction);
			toolItem.setSelected(false);
			toolItem.addActionListener(polygonAction);
			
			polygonAction.cancelActions();
		}
	}
	
	/**
	 * 
	 * @author Jordi SUC
	 * the class allowing to get the position and size of the future drawn polygon 
	 */
	protected class PolygonActionListener implements ActionListener{

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
		protected PolygonActionListener(){
			
			createCursor=getSVGEditor().getCursors().getCursor("polygon"); //$NON-NLS-1$
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

				PolygonMouseListener pml=null;
				
				//adds the new motion adapters
				for(it=frames.iterator(); it.hasNext();){
				    
					try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
			
					if(frm!=null && ! mouseAdapterFrames.containsKey(frm)){
				
						pml=new PolygonMouseListener(frm);

						try{
							frm.getScrollPane().getSVGCanvas().addMouseListener(pml);
							frm.getScrollPane().getSVGCanvas().addMouseMotionListener(pml);
							frm.getScrollPane().getSVGCanvas().setSVGCursor(createCursor);
						}catch (Exception ex){}
						
						mouseAdapterFrames.put(frm, pml);
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
							
							if(mouseListener!=null && ((PolygonMouseListener)mouseListener).paintListener!=null){
							    
								//removes the paint listener
								frm.getScrollPane().getSVGCanvas().removePaintListener(((PolygonMouseListener)mouseListener).paintListener, true);
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
					
					toolItem.removeActionListener(polygonAction);
					toolItem.setSelected(true);
					toolItem.addActionListener(polygonAction);
			
					//the listener is active
					isActive=true;
	
					Collection frames=getSVGEditor().getFrameManager().getFrames();
					Iterator it;
					SVGFrame frm=null;
					PolygonMouseListener pml=null;
					
					//adds the new motion adapters
					for(it=frames.iterator(); it.hasNext();){
					    
						try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
					
						if(frm!=null){
	
							pml=new PolygonMouseListener(frm);
	
							try{
								frm.getScrollPane().getSVGCanvas().addMouseListener(pml);
								frm.getScrollPane().getSVGCanvas().addMouseMotionListener(pml);
								frm.getScrollPane().getSVGCanvas().setSVGCursor(createCursor);
							}catch (Exception ex){}
							
							mouseAdapterFrames.put(frm, pml);
						}
					}
				}
			}
		}
			
		protected class PolygonMouseListener extends MouseAdapter implements MouseMotionListener{
		
			/**
			 * the list containing the points of the polygon
			 */
			private LinkedList<Point2D.Double> points=new LinkedList<Point2D.Double>();
			
			/**
			 * the frame
			 */
			private SVGFrame frame;
			
			/**
			 * the paint listener
			 */
			private CanvasPaintListener paintListener=null;
			
			/**
			 * the last clicked point
			 */
			private Point2D.Double lastPoint=null;
		
			/**
			 * the constructor of the class
			 * @param frame a frame
			 */
			public PolygonMouseListener(SVGFrame frame){
				
				this.frame=frame;
				final SVGFrame fframe=frame;
				
				//adds a paint listener
				paintListener=new CanvasPaintListener(){

					public void paintToBeDone(Graphics g) {
				
						if(points.size()>1){

							//draws the shape of the path that will be created if the user released the mouse button
							Point2D.Double[] scPts=new Point2D.Double[points.size()];
							Point2D.Double pt=null;
							
							for(int i=0;i<scPts.length;i++){
							    
								pt=points.get(i);
								scPts[i]=fframe.getScaledPoint(new Point2D.Double(pt.x, pt.y), false);
							}
							
							if(g!=null && scPts!=null){
							    
							    svgPolygon.drawGhost(fframe, (Graphics2D)g, scPts);
							}
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
			}
			
			/**
			 * the method called when an event occurs
			 * @param evt the event
			 */
			public void mouseMoved(MouseEvent evt) {
				
				if(points.size()>0){
				
					Point2D.Double point=frame.getAlignedWithRulersPoint(evt.getPoint());

				    if(evt.isControlDown()){

				    	point=SVGToolkit.computeLinePointWhenCtrlDown(points.get(points.size()-2), point);
				    }

				    points.set(points.size()-1, point);
				}
				
				//asks the canvas to be repainted to draw the shape of the future polygon
				frame.getScrollPane().getSVGCanvas().delayedRepaint();
			}
			
			/**
			 * the method called when an event occurs
			 * @param evt the event
			 */
			public void mouseClicked(MouseEvent evt) {
				
				Point2D.Double point=frame.getAlignedWithRulersPoint(evt.getPoint());

			    if(evt.isControlDown() && points.size()>1){

			    	point=SVGToolkit.computeLinePointWhenCtrlDown(points.get(points.size()-2), point);
			    }
				
				if(point!=null){
					
					//checking if the click is a double click
					boolean isDoubleClick=false;
					
					if(lastPoint!=null && Math.abs(lastPoint.getX()-evt.getPoint().getX())<2 && Math.abs(lastPoint.getY()-evt.getPoint().getY())<2){
						
						isDoubleClick=true;
					}
					
					lastPoint=new Point2D.Double(evt.getPoint().x, evt.getPoint().y);
				    
					if(isDoubleClick){
					
						if(points.size()>1){
							
							points.removeLast();
							final Point2D.Double[] pointsTab=points.toArray(new Point2D.Double[0]);
						
							Runnable runnable=new Runnable(){
							    
								public void run(){
								    
									svgPolygon.drawPolygon(frame, pointsTab);
								}
							};
							
							frame.enqueue(runnable);

							getSVGEditor().cancelActions(true);
							
							//clears the array list
							points.clear();
							lastPoint=null;
						}
										
					}else{
					
						if(points.size()==0){
						    
							points.add(point);					
						}
						
						points.set(points.size()-1, point);
						points.add(point);
					}
				}
			}	
		}
	}
}

