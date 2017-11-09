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
import java.util.*;

import javax.swing.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.selection.*;
import fr.itris.glips.svgeditor.undoredo.*;

/**
 * @author Jordi SUC
 *
 * the class defining all the actions that can be done on a node 
 */
public class SVGShape extends SVGModuleAdapter{
	
	/**
	 * the variable describing the regular selection
	 */
	public static final int REGULAR_SELECTION=0;
	
	/**
	 * the variable describing the rotate selection
	 */
	public static final int ROTATE_SELECTION=1;
	
	/**
	 * the variable describing the do action
	 */
	public static final int DO_ACTION=0;

	/**
	 * the variable describing the validate action
	 */
	public static final int VALIDATE_ACTION=1;

	/**
	 * the variable describing the do translate action
	 */
	public static final int DO_TRANSLATE_ACTION=2;
	
	/**
	 * the variable describing the validate translate action
	 */
	public static final int VALIDATE_TRANSLATE_ACTION=3;
	
	/**
	 * the editor
	 */
	protected SVGEditor editor;
	
	/**
	 * the menu item that will be inserted into the menubar
	 */
	protected JMenuItem menuitem=null;
	
	/**
	 * the toggle button that will be displayed in the tool bar
	 */
	protected JToggleButton toolItem=null;
	
	/**
	 * the icons that will be displayed in the menu item and the tool item
	 */
	protected ImageIcon icon=null, disabledIcon=null;

	/**
	 * the map associating a frame to a paint listener
	 */
	protected final Map<SVGFrame,CanvasPaintListener> paintListenerTable=Collections.synchronizedMap(new Hashtable<SVGFrame,CanvasPaintListener>());
	
	/**
	 * the map associating a frame to hashtable associating a node to a list of selection squares
	 */
	protected Map<SVGFrame, Hashtable<Node, java.util.List<SVGSelectionSquare>>> selectionSquaresTable=
																Collections.synchronizedMap(new Hashtable<SVGFrame, Hashtable<Node, java.util.List<SVGSelectionSquare>>>());
	
	/**
	 * the map associating a frame to a paint listener painting the outline of the translated frame
	 */
	protected Map<SVGFrame,CanvasPaintListener> translateFrameTable=Collections.synchronizedMap(new Hashtable<SVGFrame,CanvasPaintListener>());
	
	/**
	 * the map associating a frame to a a paintListener
	 */
	protected Map<SVGFrame,CanvasPaintListener> resizeFrameTable=Collections.synchronizedMap(new Hashtable<SVGFrame,CanvasPaintListener>());
	
	/**
	 * the map associating a frame to a paint listener
	 */
	protected Map<SVGFrame,CanvasPaintListener> rotateFrameTable=Collections.synchronizedMap(new Hashtable<SVGFrame,CanvasPaintListener>());
	
	/**
	 * the map associating a frame to a paint listener
	 */
	protected Map<SVGFrame,CanvasPaintListener> modifyPointFrameTable=Collections.synchronizedMap(new Hashtable<SVGFrame,CanvasPaintListener>());
	
	/**
	 * the boolean telling if the selection can be repainted or not
	 */
	protected boolean canRepaintSelection=true;
	
	/**
	 * the hashtable associating a key to its label
	 */
	protected Hashtable<String, String> labels=new Hashtable<String, String>();
	
	/**
	 * the hashtable associating a key to its id
	 */
	protected Hashtable<String, String> ids=new Hashtable<String, String>();
	
	/**
	 * the array describing the accurate order for the selection types
	 */
	protected String[] selectionsOrder=new String[4];
	
	/**
	 * the used colors
	 */
	protected static final Color 	SQUARE_SELECTION_COLOR1=new Color(141, 168, 255),
											SQUARE_SELECTION_COLOR2=new Color(38, 76, 135),
											LINE_SELECTION_COLOR=new Color(75, 100, 200), 
											LOCK_COLOR=new Color(100,100,100), 
											GHOST_COLOR=new Color(0,0,0),
											GHOST_COLOR_HIGHLIGHT=new Color(255,255,255),
											OUTLINE_COLOR=new Color(75, 75, 255),
											OUTLINE_FILL_COLOR=new Color(75, 75, 255, 100);
	
	/**
	 * the stroke for the ghost outlines
	 */
	protected static final BasicStroke ghostStroke=new BasicStroke(	1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 
	        																							0, new float[]{8, 8}, 0);

	/**
	 * the constructor of the class
	 * @param editor the editor of the class
	 */
	public SVGShape(SVGEditor editor){

		this.editor=editor;
		selectionsOrder[0]="level1"; //$NON-NLS-1$
		selectionsOrder[1]="level2"; //$NON-NLS-1$
		selectionsOrder[2]="level3"; //$NON-NLS-1$
		selectionsOrder[3]="lock"; //$NON-NLS-1$
		
		//a listener that listens to the changes of the SVGFrames
		final ActionListener svgframeListener=new ActionListener(){

			public void actionPerformed(ActionEvent e) {

                SVGFrame frame=null;
                Collection currentFrames=getSVGEditor().getFrameManager().getFrames();
                
                //creating the list of all the frames stored in one of the maps
                HashSet<SVGFrame> frames=new HashSet<SVGFrame>();
                
                frames.addAll(paintListenerTable.keySet());
                frames.addAll(selectionSquaresTable.keySet());
                frames.addAll(translateFrameTable.keySet());
                frames.addAll(resizeFrameTable.keySet());
                frames.addAll(rotateFrameTable.keySet());
                frames.addAll(modifyPointFrameTable.keySet());
                
                //adds the new mouse motion and key listeners
                for(Iterator it=frames.iterator(); it.hasNext();){

                    try{frame=(SVGFrame)it.next();}catch (Exception ex){frame=null;}

                    if(frame!=null && ! currentFrames.contains(frame)){
                        
                        paintListenerTable.remove(frame);
        				selectionSquaresTable.remove(frame);
        				translateFrameTable.remove(frame);
        				resizeFrameTable.remove(frame);
        				rotateFrameTable.remove(frame);
        				modifyPointFrameTable.remove(frame);
                    }
                }
  
				frame=getSVGEditor().getFrameManager().getCurrentFrame();

				if(frame==null){
					
					getSVGEditor().cancelActions(true);
				}
			}
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);
	}
	
	/**
	 * @return the editor
	 */
	public SVGEditor getSVGEditor(){
		return editor;
	}
	
	/**
	 * selects the nodes and renders them graphically
	 * @param frame the current SVGFrame
	 * @param table the nodes to be selected
	 */
	public void select(SVGFrame frame, Hashtable table){
		
		//gets the hashtables associated with the frame
		Hashtable<Node, java.util.List<SVGSelectionSquare>> selectionSquares=selectionSquaresTable.get(frame);

		if(selectionSquares==null){
		    
			selectionSquares=new Hashtable<Node, java.util.List<SVGSelectionSquare>>();
			selectionSquaresTable.put(frame, selectionSquares);
		}

		LinkedList currentList=null;
		
		for(Iterator it=selectionSquares.keySet().iterator(); it.hasNext();){
		    
			currentList=(LinkedList)selectionSquares.get(it.next());
			
			if(currentList!=null){
			    
			    currentList.clear();
			}
		}
		
		//removes the paint listener
		CanvasPaintListener paintListener=paintListenerTable.get(frame);
		
		if(paintListener!=null){
		    
			frame.getScrollPane().getSVGCanvas().removePaintListener(paintListener, false);
			paintListenerTable.remove(frame);
		}
		
		if(table!=null && table.size()>0){
		    
			handleSelection(frame, table, frame.getScrollPane().getSVGCanvas().getGraphics());	
			
			final SVGFrame fframe=frame;
			final Hashtable ftable=new Hashtable(table);
			
			paintListener=new CanvasPaintListener(){
			    
				public void paintToBeDone(Graphics g) {
					
					if(canRepaintSelection()){
					    
						handleSelection(fframe, ftable, g);
						
						if(getSVGEditor().getSVGSelection()!=null){
						    
							getSVGEditor().getSVGSelection().addSelectionSquares(fframe, getSelectionSquares(fframe));
						}
					}
				}
			};
			
			frame.getScrollPane().getSVGCanvas().addLayerPaintListener(SVGCanvas.SELECTION_LAYER, paintListener, false);
			paintListenerTable.put(frame, paintListener);
		}
	}
	
	/**
	 * calls the selection methods for each node in the hashtable
	 * @param frame the current SVGFrame
	 * @param table the table containing the nodes
	 * @param g the graphics used to paint
	 */
	protected void handleSelection(SVGFrame frame, Hashtable table, Graphics g){
		
		//gets the hashtable associated with the frame
		Hashtable<Node, java.util.List<SVGSelectionSquare>> selectionSquares=null;
		
		try{
			selectionSquares=selectionSquaresTable.get(frame);
		}catch (Exception ex){selectionSquares=null;}
		
			
		if(selectionSquares!=null){
		    
		    selectionSquares.clear();
		    
		}else{
		    
			selectionSquares=new Hashtable<Node, java.util.List<SVGSelectionSquare>>();
			selectionSquaresTable.put(frame, selectionSquares);
		}
		
		if(table!=null && table.size()>0){

			Node node=null;
			LinkedList<SVGSelectionSquare> squares=null;
			String type=""; //$NON-NLS-1$
			
			//for each node in the table, the accurate "drawSelection" method is called given the type of the selection
			for(Iterator it=table.keySet().iterator(); it.hasNext();){
			    
				try{node=(Node)it.next();}catch (Exception e){node=null;}
				
				if(node!=null){
				    
					type=(String)table.get(node);
					
					if(type!=null && ! type.equals("")){ //$NON-NLS-1$
						
						if(type.equals(selectionsOrder[0])){
						    
							squares=drawSelection(frame, g, node);

						}else if(type.equals(selectionsOrder[1])){
						    
							squares=drawRotateSelection(frame, g, node);

						}else if(type.equals(selectionsOrder[2])){
						    
							squares=drawModifyPointsSelection(frame, g, node);	

						}else if(type.equals(selectionsOrder[3])){
						    
							squares=drawLockedSelection(frame, g, node);
						}
						
						if(squares!=null && squares.size()>0){
						    
						    selectionSquares.put(node, squares);
						}
					}
				}
			}
		}
	}
	
	/**
	 * @return the boolean telling if the selection can be repainted or not
	 */
	public boolean canRepaintSelection(){
		return canRepaintSelection;
	}
	
	/**
	 * sets the canRepaintSelection boolean
	 * @param canRepaintSelection true to enable the selections to be repainted
	 */
	public synchronized void setCanRepaintSelection(boolean canRepaintSelection){
		
		if(this.canRepaintSelection!=canRepaintSelection){
		    
			this.canRepaintSelection=canRepaintSelection;
			getSVGEditor().getFrameManager().getCurrentFrame().getScrollPane().getSVGCanvas().delayedRepaint();
		}
	}
	
	/**
	 * gets the nexts level after the given selection level
	 * @param type a selection level
	 * @return the next selection level
	 */
	public String getNextLevel(String type){
	    
		if(type!=null){
		    
			if(type.equals("level1")){ //$NON-NLS-1$
			    
			    return "level2"; //$NON-NLS-1$
			    
			}else if(type.equals("level2")){ //$NON-NLS-1$
			    
			    return "level1"; //$NON-NLS-1$
			    
			}else if(type.equals("default")){ //$NON-NLS-1$
			    
			    return "level1"; //$NON-NLS-1$
			}
		}
		return "level1"; //$NON-NLS-1$
	}
	
	/**
	 * clears the table of the selected nodes
	 * @param frame the current SVGFrame
	 * @param makeRepaint the Boolean telling if a repaint action should be done or not
	 */
	public void deselectAll(SVGFrame frame, boolean makeRepaint){

		//removes the paint listener
		CanvasPaintListener paintListener=paintListenerTable.get(frame);
		
		if(paintListener!=null){
		    
			frame.getScrollPane().getSVGCanvas().removePaintListener(paintListener, false);
			paintListenerTable.remove(paintListener);
		}
		
		//removing the paint listeners of the translate actions
		paintListener=translateFrameTable.get(frame);
		
		if(paintListener!=null){
		    
			frame.getScrollPane().getSVGCanvas().removePaintListener(paintListener, false);
			translateFrameTable.remove(paintListener);
		}
		
		//removing the paint listeners of the resize actions
		paintListener=resizeFrameTable.get(frame);
		
		if(paintListener!=null){
		    
			frame.getScrollPane().getSVGCanvas().removePaintListener(paintListener, false);
			resizeFrameTable.remove(paintListener);
		}
		
		//removing the paint listeners of the rotate actions
		paintListener=rotateFrameTable.get(frame);
		
		if(paintListener!=null){
		    
			frame.getScrollPane().getSVGCanvas().removePaintListener(paintListener, false);
			rotateFrameTable.remove(paintListener);
		}
		
		//removing the paint listeners of the modify point actions
		paintListener=modifyPointFrameTable.get(frame);
		
		if(paintListener!=null){
		    
			frame.getScrollPane().getSVGCanvas().removePaintListener(paintListener, false);
			modifyPointFrameTable.remove(paintListener);
		}
		
		if(makeRepaint){
		    
		    frame.getScrollPane().getSVGCanvas().delayedRepaint();
		}
	}
	
	/**
	 * does the action that are linked with a special selection
	 * @param frame the current SVGFrame
	 * @param type the type of the selection
	 * @param action the type of the action (do or validate)
	 * @param args the array of the arguments
	 */
	public void doActions(SVGFrame frame, String type, int action, Object[] args){
		
	    final SVGFrame fframe=frame;
	    final Object[] fargs=args;
	    
		frame.getScrollPane().getSVGCanvas().setEnableWaitCursor(true);
	    
		if(type!=null && ! type.equals("")){ //$NON-NLS-1$
			
			//if it is a translation action
			if(action==DO_TRANSLATE_ACTION || action==VALIDATE_TRANSLATE_ACTION){
			    
				if(action==DO_TRANSLATE_ACTION){
				    
					setCanRepaintSelection(false);
					
					try{
						translateNodes(frame, (LinkedList)args[0], (Point2D.Double)args[1]);
					}catch (Exception ex){return;}
					
				}else if(action==VALIDATE_TRANSLATE_ACTION){
				    
				    Runnable runnable=new Runnable(){

                        public void run() {

        					setCanRepaintSelection(false);
        					
        					try{
        						validateTranslateNodes(fframe, (LinkedList<Node>)fargs[0], (Point2D.Double)fargs[1]);
        						
        						//sets that the svg has been modified
        						fframe.setModified(true);
        					}catch (Exception ex){return;}
        					
        					setCanRepaintSelection(true);
                        }
				    };
				    
					frame.enqueue(runnable);
				}	
			
			}else{
			    
				//make a different action given the current type of selection
				if(type.equals("level1")){ //$NON-NLS-1$
				    
					if(action==DO_ACTION){
					    
						setCanRepaintSelection(false);
						
						try{
							resizeNode(frame, (SVGSelectionSquare)args[0],(Point2D.Double)args[1],(Point2D.Double)args[2]);
						}catch (Exception e){return;}
						
					}else if(action==VALIDATE_ACTION){
					    
					    Runnable runnable=new Runnable(){

                            public void run() {

        						try{
        							validateResizeNode(fframe, (SVGSelectionSquare)fargs[0],(Point2D.Double)fargs[1],(Point2D.Double)fargs[2]);
        							
        							//sets that the svg has been modified
        							fframe.setModified(true);
        							
        						}catch (Exception e){return;}
        						
        						setCanRepaintSelection(true);		
                            }
					    };
					    
						frame.enqueue(runnable);
					}		
		
				}else if(type.equals("level2")){ //$NON-NLS-1$
				    
					if(action==DO_ACTION){
					    
						setCanRepaintSelection(false);
						
						try{
							rotateSkewNode(frame, (SVGSelectionSquare)args[0],(Point2D.Double)args[3],(Point2D.Double)args[4]);
						}catch (Exception e){return;}
						
					}else if(action==VALIDATE_ACTION){
					    
					    Runnable runnable=new Runnable(){

                            public void run() {

        						try{
        							validateRotateSkewNode(fframe, (SVGSelectionSquare)fargs[0],(Point2D.Double)fargs[3],(Point2D.Double)fargs[4]);
        							
        							//sets that the svg has been modified
        							fframe.setModified(true);
        							
        						}catch (Exception e){return;}
        						
        						setCanRepaintSelection(true);		
                            }
					    };
					    
						frame.enqueue(runnable);
					}				
				
				}else if(type.equals("level3")){ //$NON-NLS-1$
				    
					if(action==DO_ACTION){
					    
						setCanRepaintSelection(false);
						
						try{
							modifyPoint(frame, (SVGSelectionSquare)args[0],(Point2D.Double)args[1],(Point2D.Double)args[2]);
						}catch (Exception e){return;}

					}else if(action==VALIDATE_ACTION){

					    Runnable runnable=new Runnable(){

                            public void run() {

        						try{
        							validateModifyPoint(fframe, (SVGSelectionSquare)fargs[0], (Point2D.Double)fargs[1], (Point2D.Double)fargs[2]);
        							
        							//sets that the svg document has been modified
        							fframe.setModified(true);
        						}catch (Exception e){return;}
        						
        						setCanRepaintSelection(true);
                            }
					    };
					    
						frame.enqueue(runnable);
					}
				
				}else if(type.equals("locked")){ //$NON-NLS-1$
				
				}		
			}
		}
	}
	
	/**
	 * @param opoint the origin point
	 * @param place the place of the selection square
	 * @param type the type of the selection
	 * @return the shape of the selection square
	 */
	protected Shape getArrow(Point2D.Double opoint, String place, int type){
		
		Shape shape=null;
		AffineTransform af=new AffineTransform();
		
		if(place!=null && place.equals("C")){ //$NON-NLS-1$
			
			GeneralPath path=new GeneralPath();
			path.moveTo(0, -4);
			path.lineTo(-3, -4);
			path.lineTo(-4, -3);
			path.lineTo(-4, 3);
			path.lineTo(-3, 4);
			path.lineTo(4, 4);
			path.lineTo(4, 0);
			path.lineTo(6, 0);
			path.lineTo(3, -2);
			path.lineTo(0, 0);
			path.lineTo(2, 0);
			path.lineTo(2, 2);
			path.lineTo(-1, 2);
			path.lineTo(-2, 1);
			path.lineTo(-2, -2);
			path.lineTo(0, -2);
			path.lineTo(0, -4);
			
			af.preConcatenate(AffineTransform.getTranslateInstance(opoint.x, opoint.y));
			shape=path.createTransformedShape(af);
			
		}else if(place!=null && (place.equals("P") || place.equals("Begin") || place.equals("End"))){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
			Rectangle2D.Double rect=new Rectangle2D.Double(-2.5, -2.5, 5, 5);
			
			af.preConcatenate(AffineTransform.getTranslateInstance(opoint.x, opoint.y));
			shape=af.createTransformedShape(rect);
			
		}else if(place!=null && place.equals("Ctrl")){ //$NON-NLS-1$
			
			GeneralPath path=new GeneralPath();
			
			path.moveTo(0, -4);
			path.lineTo(-3, 0);
			path.lineTo(0, 4);
			path.lineTo(3, 0);
			path.lineTo(0, -4);
			
			af.preConcatenate(AffineTransform.getTranslateInstance(opoint.x, opoint.y));
			shape=path.createTransformedShape(af);
			
		}else if(place!=null){
			
			GeneralPath path=new GeneralPath();
			path.moveTo(0, -5);
			path.lineTo(-3, -2);
			path.lineTo(-1, -2);
			path.lineTo(-1, 2);	
			path.lineTo(-3, 2);		
			path.lineTo(0, 5);
			path.lineTo(3, 2);
			path.lineTo(1, 2);
			path.lineTo(1, -2);
			path.lineTo(3, -2);
			path.lineTo(0, -5);
		
			double angle=0;
			
			if(place.equals("N") || place.equals("S")){ //$NON-NLS-1$ //$NON-NLS-2$
				
				if(type==REGULAR_SELECTION){
				    
					angle=0;
					
				}else if(type==ROTATE_SELECTION){
				    
					angle=Math.PI/2;
				}

			
			}else if(place.equals("E") || place.equals("W")){ //$NON-NLS-1$ //$NON-NLS-2$
				
				if(type==REGULAR_SELECTION){
				    
					angle=Math.PI/2;
					
				}else if(type==ROTATE_SELECTION){
				    
					angle=0;
				}
			
			}else if(place.equals("NW") || place.equals("SE")){ //$NON-NLS-1$ //$NON-NLS-2$
			    
				angle=-Math.PI/4;
			
			}else if(place.equals("NE") || place.equals("SW")){ //$NON-NLS-1$ //$NON-NLS-2$
			    
				angle=Math.PI/4;
			}

			af.preConcatenate(AffineTransform.getRotateInstance(angle));
			af.preConcatenate(AffineTransform.getTranslateInstance(opoint.x, opoint.y));
			shape=path.createTransformedShape(af);
		}
		
		return shape;
	}
	
	/**
	 * draws the selection around the node
	 * @param frame the current SVGFrame
	 * @param graphics the graphics 
	 * @param node the node to be selected
	 * @return the list of the selection squares
	 */
	protected LinkedList<SVGSelectionSquare> drawSelection(SVGFrame frame, Graphics graphics, Node node){
	    
		LinkedList<SVGSelectionSquare> squarelist=new LinkedList<SVGSelectionSquare>();
		
		Graphics2D g=(Graphics2D)graphics;
		
		if(frame!=null && g!=null && node!=null){
			
			int sqd=5;
			Rectangle2D rect=frame.getNodeBounds((Element)node);

			if(rect!=null){
			    
				//computes and draws the new awt rectangle to be displayed
				Rectangle2D.Double scRect=new Rectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
				
				if(scRect!=null){
				    
					double x=scRect.getX(), y=scRect.getY(), w=scRect.getWidth(), h=scRect.getHeight();
					
					int[] sqx=new int[8];
					int[] sqy=new int[8];
					
					//the coordinates of the selection points
					sqx[0]=(int)x-2*sqd;
					sqx[1]=(int)(x+w/2)-sqd;
					sqx[2]=(int)(x+w);
					sqx[3]=sqx[2];
					sqx[4]=sqx[2];
					sqx[5]=sqx[1];
					sqx[6]=sqx[0];
					sqx[7]=sqx[0];

					sqy[0]=(int)y-2*sqd;
					sqy[3]=(int)(y+h/2)-sqd;
					sqy[4]=(int)(y+h);
					sqy[1]=sqy[0];
					sqy[2]=sqy[0];
					sqy[5]=sqy[4];
					sqy[6]=sqy[4];
					sqy[7]=sqy[3];
			
					//the ids of the selection squares
					String[] types=new String[8];
					types[0]="NW"; //$NON-NLS-1$
					types[1]="N"; //$NON-NLS-1$
					types[2]="NE"; //$NON-NLS-1$
					types[3]="E"; //$NON-NLS-1$
					types[4]="SE"; //$NON-NLS-1$
					types[5]="S"; //$NON-NLS-1$
					types[6]="SW"; //$NON-NLS-1$
					types[7]="W"; //$NON-NLS-1$
			
					//the cursors associated with the selection square
					Cursor[] cursors=new Cursor[8];
					cursors[0]=new Cursor(Cursor.NW_RESIZE_CURSOR);
					cursors[1]=new Cursor(Cursor.N_RESIZE_CURSOR);
					cursors[2]=new Cursor(Cursor.NE_RESIZE_CURSOR);
					cursors[3]=new Cursor(Cursor.E_RESIZE_CURSOR);
					cursors[4]=new Cursor(Cursor.SE_RESIZE_CURSOR);
					cursors[5]=new Cursor(Cursor.S_RESIZE_CURSOR);
					cursors[6]=new Cursor(Cursor.SW_RESIZE_CURSOR);
					cursors[7]=new Cursor(Cursor.W_RESIZE_CURSOR);

					//an array of indices
					int i;
					int[] tin=null;

					if(w>2*sqd && h>2*sqd){
					    
						int[] tmp=new int[8];
						
						for(i=0;i<8;i++){
						    
						    tmp[i]=i;
						}
						
						tin=tmp;
				
					}else if((w<=2*sqd && h>2*sqd) || (w>2*sqd && h<=2*sqd) || (w<=2*sqd && h<=2*sqd)){
					    
						int[] tmp={1,3,5,7};
						tin=tmp;
					}
			
					//draws the graphic elements
					Shape shape=null;
					GradientPaint gradient=null;
					
					for(i=0;i<tin.length;i++){
						
						if(editor.getSVGSelection()!=null){
						    
							squarelist.add(new SVGSelectionSquare(node, types[tin[i]], 
														new Rectangle2D.Double(sqx[tin[i]],sqy[tin[i]],2*sqd,2*sqd), 
														cursors[tin[i]]));
						}
						
						shape=getArrow(new Point2D.Double(sqx[tin[i]]+sqd, sqy[tin[i]]+sqd), types[tin[i]], REGULAR_SELECTION);
						
						if(shape!=null){
						    
							gradient=new GradientPaint(sqx[tin[i]], sqy[tin[i]], SQUARE_SELECTION_COLOR1, sqx[tin[i]]+2*sqd, sqy[tin[i]]+2*sqd, SQUARE_SELECTION_COLOR2, true);
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
	 * draws the selection around the node
	 * @param frame the current SVGFrame
	 * @param graphics the graphics 
	 * @param node the node to be selected
	 * @return the list of the selection squares
	 */
	protected LinkedList<SVGSelectionSquare> drawRotateSelection(SVGFrame frame, Graphics graphics, Node node){
	    
		LinkedList<SVGSelectionSquare> squarelist=new LinkedList<SVGSelectionSquare>();
		
		Graphics2D g=(Graphics2D)graphics;

		if(frame!=null && g!=null && node!=null){
			
			int sqd=5;
			Rectangle2D rect=frame.getNodeBounds((Element)node);
			
			if(rect!=null){
			    
				//computes and draws the new awt rectangle to be displayed
				Rectangle2D.Double scRect=new Rectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
			
				double x=scRect.getX(), y=scRect.getY(), w=scRect.getWidth(), h=scRect.getHeight();
				int[] sqx=new int[5];
				int[] sqy=new int[5];
				
				//the coordinates of the selection points
				sqx[0]=(int)(x+w/2)-sqd;
				sqx[1]=(int)(x+w);
				sqx[2]=sqx[0];
				sqx[3]=(int)x-2*sqd;
				sqx[4]=(int)(x+w/2-sqd);

				sqy[0]=(int)y-2*sqd;
				sqy[1]=(int)(y+h/2)-sqd;
				sqy[2]=(int)(y+h);
				sqy[3]=sqy[1];
				sqy[4]=(int)(y+h/2-sqd);

				//the ids of the selection points			
				String[] types=new String[5];
				types[0]="N"; //$NON-NLS-1$
				types[1]="E"; //$NON-NLS-1$
				types[2]="S"; //$NON-NLS-1$
				types[3]="W"; //$NON-NLS-1$
				types[4]="C"; //$NON-NLS-1$
			
				Cursor[] cursors=new Cursor[5];
				cursors[0]=new Cursor(Cursor.HAND_CURSOR);
				cursors[1]=new Cursor(Cursor.HAND_CURSOR);
				cursors[2]=new Cursor(Cursor.HAND_CURSOR);
				cursors[3]=new Cursor(Cursor.HAND_CURSOR);
				cursors[4]=new Cursor(Cursor.HAND_CURSOR);

				//an array of indices
				int i;
				int[] tin=null;
				
				if(w>2*sqd && h>2*sqd){
				    
					int[] tmp=new int[5];
					
					for(i=0;i<5;i++){
					    
					    tmp[i]=i;
					}
					
					tin=tmp;
				
				}else{
				    
					int[] tmp={0,1,2,3};
					
					tin=tmp;
				}
			
				//draws the graphic elements
				Shape shape=null;
				GradientPaint gradient=null;
			
				for(i=0;i<tin.length;i++){
					
					if(editor.getSVGSelection()!=null){
					    
						squarelist.add(new SVGSelectionSquare(node,types[tin[i]],
													new Rectangle2D.Double(sqx[tin[i]],sqy[tin[i]],2*sqd,2*sqd),
													cursors[tin[i]]));
					}		
					
					shape=getArrow(new Point2D.Double(sqx[tin[i]]+sqd, sqy[tin[i]]+sqd), types[tin[i]], ROTATE_SELECTION);
						
					if(shape!=null){
					    
						gradient=new GradientPaint(sqx[tin[i]], sqy[tin[i]], SQUARE_SELECTION_COLOR1, sqx[tin[i]]+2*sqd, sqy[tin[i]]+2*sqd, SQUARE_SELECTION_COLOR2, true);
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
	 * draws the selection around the node
	 * @param frame the current SVGFrame
	 * @param graphics the graphics 
	 * @param node the node to be selected
	 * @return the list of the selection squares
	 */
	protected LinkedList<SVGSelectionSquare> drawModifyPointsSelection(SVGFrame frame, Graphics graphics, Node node){
		return null;
	}
	
	/**
	 * draws the selection around the node
	 * @param frame the current SVGFrame
	 * @param graphics the graphics 
	 * @param node the node to be selected
	 * @return the list of the selection squares
	 */
	protected LinkedList<SVGSelectionSquare> drawLockedSelection(SVGFrame frame, Graphics graphics, Node node){
		
		Graphics2D g=(Graphics2D)graphics;
		
		if(frame!=null && g!=null && node!=null){
			
			int sqd=5;
			
			Rectangle2D rect=frame.getNodeBounds((Element)node);
			
			if(rect!=null){
			    
				//computes and draws the new awt rectangle to be displayed
				Rectangle2D.Double scRect=new Rectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
			
				double x=scRect.getX(), y=scRect.getY(), w=scRect.getWidth(), h=scRect.getHeight();
				int[] sqx=new int[8];
				int[] sqy=new int[8];
				
				//the coordinates of the selection points
				sqx[0]=(int)x-2*sqd;
				sqx[1]=(int)(x+w/2)-sqd;
				sqx[2]=(int)(x+w);
				sqx[3]=sqx[2];
				sqx[4]=sqx[2];
				sqx[5]=sqx[1];
				sqx[6]=sqx[0];
				sqx[7]=sqx[0];

				sqy[0]=(int)y-2*sqd;
				sqy[3]=(int)(y+h/2)-sqd;
				sqy[4]=(int)(y+h);
				sqy[1]=sqy[0];
				sqy[2]=sqy[0];
				sqy[5]=sqy[4];
				sqy[6]=sqy[4];
				sqy[7]=sqy[3];
				
				//the ids of the selection squares
				String[] types=new String[8];
				types[0]="NW"; //$NON-NLS-1$
				types[1]="N"; //$NON-NLS-1$
				types[2]="NE"; //$NON-NLS-1$
				types[3]="E"; //$NON-NLS-1$
				types[4]="SE"; //$NON-NLS-1$
				types[5]="S"; //$NON-NLS-1$
				types[6]="SW"; //$NON-NLS-1$
				types[7]="W"; //$NON-NLS-1$

				//an array of indices
				int i;
				int[] tin=null;
				
				if(w>2*sqd && h>2*sqd){
				    
					int[] tmp=new int[8];
					
					for(i=0;i<8;i++){
					    
					    tmp[i]=i;
					}
					
					tin=tmp;
			
				}else if((w<=2*sqd && h>2*sqd) || (w>2*sqd && h<=2*sqd) || (w<=2*sqd && h<=2*sqd)){
				    
					int[] tmp={1,3,5,7};
					
					tin=tmp;
				}

				//draws the graphic elements
				Shape shape=null;
				GradientPaint gradient=null;
				
				for(i=0;i<tin.length;i++){
					
					shape=getArrow(new Point2D.Double(sqx[tin[i]]+sqd, sqy[tin[i]]+sqd), types[tin[i]], REGULAR_SELECTION);
						
					if(shape!=null){
					    
						gradient=new GradientPaint(sqx[tin[i]], sqy[tin[i]], LOCK_COLOR, sqx[tin[i]]+2*sqd, sqy[tin[i]]+2*sqd, Color.white, true);
						g.setPaint(gradient);
						g.fill(shape);
						g.setColor(LINE_SELECTION_COLOR);
						g.draw(shape);
					}
				}
			}
		}	
		
		return null;		
	}
	
	/**
	 * 
	 * @return the table of the selection squares
	 * @param frame the current SVGFrame
	 */
	public Hashtable<Node, java.util.List<SVGSelectionSquare>> getSelectionSquares(SVGFrame frame){

		return selectionSquaresTable.get(frame);
	}
	
	/**
	 * fills the given shape
	 * @param g a graphics
	 * @param shape a shape
	 */
	protected void fillShape(Graphics2D g, Shape shape){
	    
	    if(g!=null && shape!=null){

	        g.setColor(OUTLINE_FILL_COLOR);
	        g.fill(shape);
	    }
	}

	/**
	 * the method to translate a node
	 * @param frame the current SVGFrame
	 * @param list the list of the nodes to be translated
	 * @param translationValues the values of the translation
	 */
	/*public void translateNodes(SVGFrame frame, final LinkedList<Node> list, final Point2D.Double translationValues){

		if(frame!=null && list!=null && list.size()>0 && translationValues!=null){

			//gets the paintlistener associated with the frame
			CanvasPaintListener paintListener=null;
			
			for(SVGFrame f : new LinkedList<SVGFrame>(translateFrameTable.keySet())){
			    
				try{
					paintListener=translateFrameTable.get(f);
				}catch (Exception ex){paintListener=null;}

				//removes the paint listener
				if(paintListener!=null){
				    
					frame.getScrollPane().getSVGCanvas().removePaintListener(paintListener, true);
					translateFrameTable.remove(frame);
				}
			}
	
			//for each node, sets the new matrix transform
			frame.enqueue(new Runnable(){
				
				public void run() {

					//final Hashtable<Node, AffineTransform> transformMap=new Hashtable<Node, AffineTransform>();
					SVGTransformMatrix matrix;
					AffineTransform af=null;
					
					for(Node node : list){

						if(node!=null){
							
							af=AffineTransform.getTranslateInstance(translationValues.x, translationValues.y);
							//transformMap.put(node, af);

							if(! af.isIdentity()){
							    
								//gets, modifies and sets the matrix
								matrix=getSVGEditor().getSVGToolkit().getTransformMatrix(node);
								matrix.concatenateTransform(af);
								getSVGEditor().getSVGToolkit().setTransformMatrix(node, matrix);
							}
						}
					}
				}
			});
		}
	}*/
	
	/**
	 * the method to translate a node
	 * @param frame the current SVGFrame
	 * @param list the list of the nodes to be translated
	 * @param translationValues the values of the translation
	 */
	public void translateNodes(SVGFrame frame, LinkedList<Node> list, Point2D.Double translationValues){

		if(frame!=null && list!=null && list.size()>0 && translationValues!=null){

			//gets the paintlistener associated with the frame
			CanvasPaintListener paintListener=translateFrameTable.get(frame);

			if(translationValues!=null){

				Shape outline=null;
				Node node=null;
				final LinkedList<Shape> outlines=new LinkedList<Shape>();
				
				//for each node, creates the outline
				for(Iterator it=list.iterator(); it.hasNext();){
	
					try{node=(Node)it.next();}catch (Exception ex){node=null;}
					
					if(node!=null){

						outline=frame.getTransformedOutline((Element)node, AffineTransform.getTranslateInstance(translationValues.x, translationValues.y));
						outlines.add(outline);
					}
					
					//removes the paint listener
					if(paintListener!=null){
					    
					    translateFrameTable.remove(frame);
					    frame.getScrollPane().getSVGCanvas().removePaintListener(paintListener, false);
					}
				}

				//creates and sets the paint listener
				paintListener=new CanvasPaintListener(){
				    
					public void paintToBeDone(Graphics g) {

						Graphics2D g2=(Graphics2D)g;
						
						for(Shape outln : outlines){

							if(outln!=null){
							    
								fillShape(g2, outln);
							}
						}
					}
				};

				frame.getScrollPane().getSVGCanvas().addLayerPaintListener(SVGCanvas.DRAW_LAYER, paintListener, true);
				translateFrameTable.put(frame, paintListener);
			}
		}
	}
	
	/**
	 * validates the translateNode method
	 * @param frame the current SVGFrame
	 * @param list the list of the nodes to be translated
	 * @param translationValues the values of the translation
	 */
	public void validateTranslateNodes(SVGFrame frame, LinkedList<Node> list, Point2D.Double translationValues){

		if(frame!=null && list!=null && list.size()>0 && translationValues!=null){

			//gets the paintlistener associated with the frame
			CanvasPaintListener paintListener=null;
			
			for(SVGFrame f : new LinkedList<SVGFrame>(translateFrameTable.keySet())){
			    
				try{
					paintListener=translateFrameTable.get(f);
				}catch (Exception ex){paintListener=null;}

				//removes the paint listener
				if(paintListener!=null){
				    
					frame.getScrollPane().getSVGCanvas().removePaintListener(paintListener, true);
					translateFrameTable.remove(frame);
				}
			}
			
			final Hashtable<Node, AffineTransform> transformMap=new Hashtable<Node, AffineTransform>();
			SVGTransformMatrix matrix;
			AffineTransform af=null;
			
			//for each node, sets the new matrix transform
			for(Node node : list){

				if(node!=null){
					
					af=AffineTransform.getTranslateInstance(translationValues.x, translationValues.y);
					transformMap.put(node, af);

					if(! af.isIdentity()){
					    
						//gets, modifies and sets the matrix
						matrix=getSVGEditor().getSVGToolkit().getTransformMatrix(node);
						matrix.concatenateTransform(af);
						getSVGEditor().getSVGToolkit().setTransformMatrix(node, matrix);
					}
				}
			}

			//creates the undo/redo action and insert it into the undo/redo stack
			if(editor.getUndoRedo()!=null){
			    
				SVGUndoRedoAction action=new SVGUndoRedoAction(labels.get("undoredotranslate")){ //$NON-NLS-1$

					@Override
					public void undo(){
					    
						SVGTransformMatrix fmatrix=null;
						AffineTransform faf=null;
						Node node=null;
			
						for(Iterator it=transformMap.keySet().iterator(); it.hasNext();){
				
							try{node=(Node)it.next();}catch (Exception ex){node=null;}
				
							if(node!=null){
							    
								faf=transformMap.get(node);
								
								if(faf!=null && ! faf.isIdentity()){
								    
									//gets, modifies and sets the matrix
									fmatrix=getSVGEditor().getSVGToolkit().getTransformMatrix(node);
									try{fmatrix.concatenateTransform(faf.createInverse());}catch (Exception ex){}
									getSVGEditor().getSVGToolkit().setTransformMatrix(node, fmatrix);
								}
							}
						}
						
						//notifies that the selection has changed
						if(getSVGEditor().getSVGSelection()!=null){
							
							getSVGEditor().getSVGSelection().selectionChanged(true);
						}
					}

					@Override
					public void redo(){
					    
						SVGTransformMatrix fmatrix=null;
						AffineTransform faf=null;
						Node node=null;
			
						for(Iterator it=transformMap.keySet().iterator(); it.hasNext();){
				
							try{node=(Node)it.next();}catch (Exception ex){node=null;}
				
							if(node!=null){
							    
								faf=transformMap.get(node);
								
								if(faf!=null && ! faf.isIdentity()){
								    
									//gets, modifies and sets the matrix
									fmatrix=getSVGEditor().getSVGToolkit().getTransformMatrix(node);
									fmatrix.concatenateTransform(faf);
									getSVGEditor().getSVGToolkit().setTransformMatrix(node, fmatrix);
								}
							}
						}
						
						//notifies that the selection has changed
						if(getSVGEditor().getSVGSelection()!=null){
							
							getSVGEditor().getSVGSelection().selectionChanged(true);
						}
					}
				};

				//gets or creates the undo/redo list and adds the action into it
				SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList(labels.get("undoredotranslate")); //$NON-NLS-1$
				actionlist.add(action);
				
				editor.getUndoRedo().addActionList(frame, actionlist);	
				actionlist=null;
				
				/*if(getSVGEditor().getSVGSelection()!=null){
				    
				    getSVGEditor().getSVGSelection().addUndoRedoAction(frame, action);
				    
				}else{
				    

				}*/
			}
		}
	}
	
	/**
	 * resizes a node
	 * @param frame the current SVGFrame
	 * @param square the Selection square that is used by the user to resize the node
	 * @param point1 the first point clicked
	 * @param point2 the second point clicked
	 */
	public void resizeNode(SVGFrame frame, SVGSelectionSquare square, Point2D.Double point1, Point2D.Double point2){

		if(frame!=null && square!=null && square.getNode()!=null && point1!=null && point2!=null){
			
			//gets the paintlistener associated with the frame
			CanvasPaintListener paintListener=null;
			
			try{
				paintListener=resizeFrameTable.get(frame);
			}catch (Exception ex){paintListener=null;}
	
			Node node=square.getNode();

			//the outline and the bounds of the node
			Point2D.Double diff=new Point2D.Double(point2.x-point1.x, point2.y-point1.y);

			//computes the scale and translate values taking the type of the selection square into account
			Rectangle2D bounds=frame.getNodeGeometryBounds((Element)node);
			
			if(bounds!=null){
			    
				double sx=1.0, sy=1.0, tx=0, ty=0;
				
				if(square.getType().equals("NW")){ //$NON-NLS-1$
					
					sx=1-diff.x/bounds.getWidth();
					sy=1-diff.y/bounds.getHeight();
					
					tx=(bounds.getX()+bounds.getWidth())*(1-sx);
					ty=(bounds.getY()+bounds.getHeight())*(1-sy);
				
				}else if(square.getType().equals("N")){ //$NON-NLS-1$
				
					sy=1-diff.y/bounds.getHeight();
					
					ty=(bounds.getY()+bounds.getHeight())*(1-sy);
				
				}else if(square.getType().equals("NE")){ //$NON-NLS-1$
					
					sx=1+diff.x/bounds.getWidth();
					sy=1-diff.y/bounds.getHeight();

					tx=(bounds.getX())*(1-sx);
					ty=(bounds.getY()+bounds.getHeight())*(1-sy);		
					
				}else if(square.getType().equals("E")){			 //$NON-NLS-1$
											
					sx=1+diff.x/bounds.getWidth();
					
					tx=bounds.getX()*(1-sx);				
											
				}else if(square.getType().equals("SE")){ //$NON-NLS-1$
					
					sx=1+diff.x/bounds.getWidth();
					sy=1+diff.y/bounds.getHeight();
					
					tx=bounds.getX()*(1-sx);
					ty=bounds.getY()*(1-sy);
					
				}else if(square.getType().equals("S")){ //$NON-NLS-1$
					
					sy=1+diff.y/bounds.getHeight();
					
					ty=bounds.getY()*(1-sy);
					
				}else if(square.getType().equals("SW")){ //$NON-NLS-1$
					
					sx=1-diff.x/bounds.getWidth();
					sy=1+diff.y/bounds.getHeight();
					
					tx=(bounds.getX()+bounds.getWidth())*(1-sx);
					ty=(bounds.getY())*(1-sy);			
					
				}else if(square.getType().equals("W")){ //$NON-NLS-1$
					
					sx=1-diff.x/bounds.getWidth();
					
					tx=(bounds.getX()+bounds.getWidth())*(1-sx);
				}
				
				AffineTransform af=new AffineTransform();
				
				//concatenates the transforms to draw the outline
            	af.preConcatenate(AffineTransform.getScaleInstance(sx, sy));
				af.preConcatenate(AffineTransform.getTranslateInstance(tx, ty));

				final Shape outline=frame.getTransformedOutline((Element)node, af);
				
				//removes the paint listener
				if(paintListener!=null){
				    
					frame.getScrollPane().getSVGCanvas().removePaintListener(paintListener, false);
				}

				if(outline!=null){
					
					//creates and sets the paint listener
					paintListener=new CanvasPaintListener(){
					    
						public void paintToBeDone(Graphics g) {

							Graphics2D g2=(Graphics2D)g;
							
							fillShape(g2, outline);
						}
					};

				   frame.getScrollPane().getSVGCanvas().addLayerPaintListener(SVGCanvas.DRAW_LAYER, paintListener, true);
				   resizeFrameTable.put(frame, paintListener);
				}
			}
		}
	}
	
	/**
	 * validates the resize transform
	 * @param frame the current SVGFrame
	 * @param square the Selection square that is used by the user to resize the node
	 *@param point1 the first point clicked
	 * @param point2 the second point clicked
	 */
	public void validateResizeNode(SVGFrame frame, SVGSelectionSquare square, Point2D.Double point1, Point2D.Double point2){

		if(frame!=null && square!=null && square.getNode()!=null && point1!=null && point2!=null){
			
			//gets the paintlistener associated with the frame
			CanvasPaintListener paintListener=null;
			SVGFrame f=null;
			
			for(Iterator it=new LinkedList<SVGFrame>(resizeFrameTable.keySet()).iterator(); it.hasNext();){
			    
				try{
				    f=(SVGFrame)it.next();
					paintListener=resizeFrameTable.get(f);
				}catch (Exception ex){paintListener=null;}
				
				if(paintListener!=null){
				    
					frame.getScrollPane().getSVGCanvas().removePaintListener(paintListener, true);
					resizeFrameTable.remove(frame);
				}
			}
	
			final Node node=square.getNode();
			Point2D.Double diff=new Point2D.Double(point2.x-point1.x, point2.y-point1.y);

			//the bounds of the node
			Rectangle2D bounds=frame.getNodeGeometryBounds((Element)node);
			
			if(bounds!=null){
			    
			    //correcting the resize action values
			    if(diff.x==bounds.getWidth()){
			        
			        diff.x--;
			        
			    }else if(diff.x==-bounds.getWidth()){
			        
			        diff.x++;
			    }
			    
			    if(diff.y==bounds.getHeight()){
			        
			        diff.y--;
			        
			    }else if(diff.y==-bounds.getHeight()){
			        
			        diff.y++;
			    }
				
				double sx=1.0, sy=1.0, tx=0, ty=0;
				
				if(square.getType().equals("NW")){ //$NON-NLS-1$
					
					sx=1-diff.x/bounds.getWidth();
					sy=1-diff.y/bounds.getHeight();
					
					tx=(bounds.getX()+bounds.getWidth())*(1-sx);
					ty=(bounds.getY()+bounds.getHeight())*(1-sy);
				
				}else if(square.getType().equals("N")){ //$NON-NLS-1$
				
					sy=1-diff.y/bounds.getHeight();

					ty=(bounds.getY()+bounds.getHeight())*(1-sy);
				
				}else if(square.getType().equals("NE")){ //$NON-NLS-1$
					
					sx=1+diff.x/bounds.getWidth();
					sy=1-diff.y/bounds.getHeight();
					
					tx=(bounds.getX())*(1-sx);
					ty=(bounds.getY()+bounds.getHeight())*(1-sy);		
					
				}else if(square.getType().equals("E")){			 //$NON-NLS-1$
											
					sx=1+diff.x/bounds.getWidth();
						
					tx=bounds.getX()*(1-sx);				
											
				}else if(square.getType().equals("SE")){ //$NON-NLS-1$
					
					sx=1+diff.x/bounds.getWidth();
					sy=1+diff.y/bounds.getHeight();
					
					tx=bounds.getX()*(1-sx);
					ty=bounds.getY()*(1-sy);
					
				}else if(square.getType().equals("S")){ //$NON-NLS-1$
					
					sy=1+diff.y/bounds.getHeight();
					
					ty=bounds.getY()*(1-sy);
					
				}else if(square.getType().equals("SW")){ //$NON-NLS-1$
					
					sx=1-diff.x/bounds.getWidth();
					sy=1+diff.y/bounds.getHeight();
					
					tx=(bounds.getX()+bounds.getWidth())*(1-sx);
					ty=(bounds.getY())*(1-sy);			
					
				}else if(square.getType().equals("W")){ //$NON-NLS-1$
					
					sx=1-diff.x/bounds.getWidth();
					tx=(bounds.getX()+bounds.getWidth())*(1-sx);	
				}
				
				final AffineTransform af=AffineTransform.getScaleInstance(sx, sy);
				af.preConcatenate(AffineTransform.getTranslateInstance(tx, ty));
					
				if(! af.isIdentity()){
					
					//gets, modifies and sets the matrix
					SVGTransformMatrix matrix=getSVGEditor().getSVGToolkit().getTransformMatrix(node);
					matrix.concatenateTransform(af);
					
					if(matrix.isMatrixCorrect()){
					    
						getSVGEditor().getSVGToolkit().setTransformMatrix(node, matrix);
						
						//create the undo/redo action and insert it into the undo/redo stack
						if(editor.getUndoRedo()!=null){

							SVGUndoRedoAction action=new SVGUndoRedoAction(labels.get("undoredoresize")){ //$NON-NLS-1$

								@Override
								public void undo(){
								    
									//gets, modifies and sets the matrix
								    SVGTransformMatrix fmatrix=getSVGEditor().getSVGToolkit().getTransformMatrix(node);
									try{fmatrix.concatenateTransform(af.createInverse());}catch (Exception ex){}
									
									if(fmatrix.isMatrixCorrect()){
									    
										getSVGEditor().getSVGToolkit().setTransformMatrix(node, fmatrix);
									}
									
									//notifies that the selection has changed
									if(getSVGEditor().getSVGSelection()!=null){
										
										getSVGEditor().getSVGSelection().selectionChanged(true);
									}
								}

								@Override
								public void redo(){
								    
									//gets, modifies and sets the matrix
								    SVGTransformMatrix fmatrix=getSVGEditor().getSVGToolkit().getTransformMatrix(node);
									fmatrix.concatenateTransform(af);
									
									if(fmatrix.isMatrixCorrect()){
									    
										getSVGEditor().getSVGToolkit().setTransformMatrix(node, fmatrix);  
									}
									
									//notifies that the selection has changed
									if(getSVGEditor().getSVGSelection()!=null){
										
										getSVGEditor().getSVGSelection().selectionChanged(true);
									}
								}
							};
							
							//gets or creates the undo/redo list and adds the action into it
							SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList(labels.get("undoredoresize")); //$NON-NLS-1$
							actionlist.add(action);

							editor.getUndoRedo().addActionList(frame, actionlist);	
							actionlist=null;
						}
					}
				}		
			}
		}
	}
	
	/**
	 * the method to rotate a node
	 * @param frame the current SVGFrame
	 * @param square the current selection square
	 * @param point1 the first point clicked
	 * @param point2 the second point clicked
	 */
	public void rotateSkewNode(SVGFrame frame, SVGSelectionSquare square, Point2D.Double point1, Point2D.Double point2){
		
		if(frame!=null && square!=null && square.getNode()!=null && point1!=null && point2!=null){
			
			//gets the paintlistener associated with the frame
			CanvasPaintListener paintListener=null;
			
			try{
				paintListener=rotateFrameTable.get(frame);
			}catch (Exception ex){paintListener=null;}
	
			Node node=square.getNode();

			//the bounds of the node
			Rectangle2D bounds=frame.getNodeGeometryBounds((Element)node);
			
			//the transform of the action
			AffineTransform af=new AffineTransform();

			//computes the scale and translate values taking the type of the selection square into account
			if(bounds!=null && point1!=null && point2!=null){
				
				double angle=0, cx=0, cy=0;
				Point2D.Double centerpoint=null;
				
				if(square.getType().equals("C")){ //$NON-NLS-1$
					
					centerpoint=new Point2D.Double(bounds.getX()+bounds.getWidth()/2, bounds.getY()+bounds.getHeight()/2);
					double x2=0, y2=0, n2=0;
					
					n2=Math.sqrt(Math.pow(point2.x-centerpoint.x,2)+Math.pow(point2.y-centerpoint.y,2));
					
					x2=(point2.x-centerpoint.x)/n2;
					y2=(point2.y-centerpoint.y)/n2;
					
					if(y2>=0){

						angle=Math.acos(x2);
						
					}else{
					    
						angle=-Math.acos(x2);
					}

					cx=centerpoint.x; 
					cy=centerpoint.y;

					//sets the new rotation values
					af.preConcatenate(AffineTransform.getTranslateInstance(-cx, -cy));
					af.preConcatenate(AffineTransform.getRotateInstance(angle));
					af.preConcatenate(AffineTransform.getTranslateInstance(cx, cy));

				}else if(square.getType().equals("N")){ //$NON-NLS-1$
				    
					centerpoint=new Point2D.Double(bounds.getX()+bounds.getHeight()/2,bounds.getY());
					angle=Math.toRadians(point2.x-point1.x);
					cx=centerpoint.x; 
					cy=centerpoint.y;

					//sets the new skew values
					af.preConcatenate(AffineTransform.getTranslateInstance(-cx, -cy));
					af.preConcatenate(AffineTransform.getShearInstance(Math.tan(angle), 0));
					af.preConcatenate(AffineTransform.getTranslateInstance(cx, cy));			
					
				}else if(square.getType().equals("S")){ //$NON-NLS-1$
				    
					centerpoint=new Point2D.Double(bounds.getX()+bounds.getWidth()/2,bounds.getY()+bounds.getHeight());
					angle=Math.toRadians(point2.x-point1.x);
					cx=centerpoint.x; 
					cy=centerpoint.y;
					
					//sets the new skew values
					af.preConcatenate(AffineTransform.getTranslateInstance(-cx, -cy));
					af.preConcatenate(AffineTransform.getShearInstance(Math.tan(angle), 0));
					af.preConcatenate(AffineTransform.getTranslateInstance(cx, cy));		
					
				}else if(square.getType().equals("E")){ //$NON-NLS-1$
				    
					centerpoint=new Point2D.Double(bounds.getX()+bounds.getWidth(), bounds.getY()+bounds.getHeight()/2);
					angle=Math.toRadians(point2.y-point1.y);
					cx=centerpoint.x; 
					cy=centerpoint.y;	

					//sets the new skew values
					af.preConcatenate(AffineTransform.getTranslateInstance(-cx, -cy));
					af.preConcatenate(AffineTransform.getShearInstance(0, Math.tan(angle)));
					af.preConcatenate(AffineTransform.getTranslateInstance(cx, cy));		

				}else if(square.getType().equals("W")){ //$NON-NLS-1$
				    
					centerpoint=new Point2D.Double(bounds.getX() ,bounds.getY()+bounds.getHeight()/2);
					angle=Math.toRadians(point2.y-point1.y);
					cx=centerpoint.x; 
					cy=centerpoint.y;	

					//sets the new skew values
					af.preConcatenate(AffineTransform.getTranslateInstance(-cx, -cy));
					af.preConcatenate(AffineTransform.getShearInstance(0, Math.tan(angle)));
					af.preConcatenate(AffineTransform.getTranslateInstance(cx, cy));
				}

				final Shape foutline=frame.getTransformedOutline((Element)node, af);

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
			   rotateFrameTable.put(frame, paintListener);
			}
		}
	}
	
	/**
	 * validates the rotateNode method
	 * @param frame the current SVGFrame
	 * @param square the current selection square
	 * @param point1 the first point clicked
	 * @param point2 the second point clicked
	 */
	public void validateRotateSkewNode(SVGFrame frame, SVGSelectionSquare square, Point2D.Double point1, Point2D.Double point2){

		if(frame!=null && square!=null && square.getNode()!=null){
		
			//gets the paintlistener associated with the frame
			CanvasPaintListener paintListener=null;
			SVGFrame f=null;
			
			for(Iterator it=new LinkedList<SVGFrame>(rotateFrameTable.keySet()).iterator(); it.hasNext();){
			    
				try{
				    f=(SVGFrame)it.next();
					paintListener=rotateFrameTable.get(f);
				}catch (Exception ex){paintListener=null;}
				
				if(paintListener!=null){
				    
					rotateFrameTable.remove(frame);
					frame.getScrollPane().getSVGCanvas().removePaintListener(paintListener, false);
				}
			}

			final Node node=square.getNode();
			
			//the bounds of the node
			Rectangle2D bounds=frame.getNodeGeometryBounds((Element)node);
			final AffineTransform af=new AffineTransform();
			
			if(point1!=null && point2!=null && bounds!=null){
				
				//the values used for computing the rotate or skew values
				double angle=0, cx=0, cy=0;
				Point2D.Double centerpoint=null;
				
				if(square.getType().equals("C")){	 //$NON-NLS-1$
				    
					centerpoint=new Point2D.Double(bounds.getX()+bounds.getWidth()/2, bounds.getY()+bounds.getHeight()/2);
					double x2=0, y2=0, n2=0;
					
					n2=Math.sqrt(Math.pow(point2.x-centerpoint.x, 2)+Math.pow(point2.y-centerpoint.y, 2));
					
					x2=(point2.x-centerpoint.x)/n2;
					y2=(point2.y-centerpoint.y)/n2;
					
					if(y2>=0){
					    
						angle=Math.acos(x2);
						
					}else{
					    
						angle=-Math.acos(x2);
					}

					cx=centerpoint.x; 
					cy=centerpoint.y;			

					//sets the new rotation values
					af.preConcatenate(AffineTransform.getTranslateInstance(-cx, -cy));
					af.preConcatenate(AffineTransform.getRotateInstance(angle));
					af.preConcatenate(AffineTransform.getTranslateInstance(cx, cy));
					
				}else if(square.getType().equals("N")){ //$NON-NLS-1$
				    
					centerpoint=new Point2D.Double(bounds.getX()+bounds.getWidth()/2,bounds.getY());
					angle=Math.toRadians(point2.x-point1.x);
					cx=centerpoint.x; 
					cy=centerpoint.y;	

					//sets the new rotation values
					af.preConcatenate(AffineTransform.getTranslateInstance(-cx, -cy));
					af.preConcatenate(AffineTransform.getShearInstance(Math.tan(angle), 0));
					af.preConcatenate(AffineTransform.getTranslateInstance(cx, cy));
					
				}else if(square.getType().equals("S")){ //$NON-NLS-1$
				    
					centerpoint=new Point2D.Double(bounds.getX()+bounds.getWidth()/2,bounds.getY()+bounds.getHeight());
					angle=Math.toRadians(point2.x-point1.x);
					cx=centerpoint.x; 
					cy=centerpoint.y;	

					//sets the new rotation values
					af.preConcatenate(AffineTransform.getTranslateInstance(-cx, -cy));
					af.preConcatenate(AffineTransform.getShearInstance(Math.tan(angle), 0));
					af.preConcatenate(AffineTransform.getTranslateInstance(cx, cy));
										
				}else if(square.getType().equals("E")){ //$NON-NLS-1$
				    
					centerpoint=new Point2D.Double(bounds.getX()+bounds.getWidth(), bounds.getY()+bounds.getHeight()/2);
					angle=Math.toRadians(point2.y-point1.y);
					cx=centerpoint.x; 
					cy=centerpoint.y;	

					//sets the new rotation values
					af.preConcatenate(AffineTransform.getTranslateInstance(-cx, -cy));
					af.preConcatenate(AffineTransform.getShearInstance(0, Math.tan(angle)));
					af.preConcatenate(AffineTransform.getTranslateInstance(cx, cy));

				}else if(square.getType().equals("W")){ //$NON-NLS-1$
				    
					centerpoint=new Point2D.Double(bounds.getX(), bounds.getY()+bounds.getHeight()/2);
					angle=Math.toRadians(point2.y-point1.y);
					cx=centerpoint.x; 
					cy=centerpoint.y;	

					//sets the new rotation values
					af.preConcatenate(AffineTransform.getTranslateInstance(-cx, -cy));
					af.preConcatenate(AffineTransform.getShearInstance(0, Math.tan(angle)));
					af.preConcatenate(AffineTransform.getTranslateInstance(cx, cy));									
				}

				if(! af.isIdentity()){
				    
					//gets, modifies and sets the matrix
				    SVGTransformMatrix matrix=getSVGEditor().getSVGToolkit().getTransformMatrix(node);
					matrix.concatenateTransform(af);

					if(matrix.isMatrixCorrect()){
					    
						getSVGEditor().getSVGToolkit().setTransformMatrix(node, matrix);
						
						//creates the undo/redo action and insert it into the undo/redo stack
						if(editor.getUndoRedo()!=null){
						    
							SVGUndoRedoAction action=new SVGUndoRedoAction(labels.get("undoredorotate")){ //$NON-NLS-1$

								@Override
								public void undo(){
								    
									//gets, modifies and sets the matrix
								    SVGTransformMatrix fmatrix=getSVGEditor().getSVGToolkit().getTransformMatrix(node);
									try{fmatrix.concatenateTransform(af.createInverse());}catch (Exception ex){}
									
									if(fmatrix.isMatrixCorrect()){
									    
										getSVGEditor().getSVGToolkit().setTransformMatrix(node, fmatrix);
									}
									
									//notifies that the selection has changed
									if(getSVGEditor().getSVGSelection()!=null){
										
										getSVGEditor().getSVGSelection().selectionChanged(true);
									}
								}

								@Override
								public void redo(){
								    
									//gets, modifies and sets the matrix
								    SVGTransformMatrix fmatrix=getSVGEditor().getSVGToolkit().getTransformMatrix(node);
									fmatrix.concatenateTransform(af);
									
									if(fmatrix.isMatrixCorrect()){
									    
										getSVGEditor().getSVGToolkit().setTransformMatrix(node, fmatrix);
									}
									
									//notifies that the selection has changed
									if(getSVGEditor().getSVGSelection()!=null){
										
										getSVGEditor().getSVGSelection().selectionChanged(true);
									}
								}
							};
							
							//gets or creates the undo/redo list and adds the action into it
							SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList(labels.get("undoredorotate")); //$NON-NLS-1$
							actionlist.add(action);
			
							editor.getUndoRedo().addActionList(frame, actionlist);	
							actionlist=null;	
						}
					}
				}		
			}
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
		
	}
	
	/**
	 * validates the modifyPoint method
	 * @param frame the current SVGFrame
	 * @param square the selection square
	 * @param point1 the first point clicked
	 * @param point2 the second point clicked
	 */
	public void validateModifyPoint(SVGFrame frame, SVGSelectionSquare square, Point2D.Double point1, Point2D.Double point2){

	}
	
	/**
	 * gets the name associated with the module
	 * @return the module's name
	 */
	public String getName(){
		return ids.get("id"); //$NON-NLS-1$
	}

}
