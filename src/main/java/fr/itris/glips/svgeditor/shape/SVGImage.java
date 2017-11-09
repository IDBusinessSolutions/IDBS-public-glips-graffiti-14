/*
 * Created on 26 mai 2004
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
import java.io.*;
import java.net.*;
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
 * the class that allows to add, select,modify the properties, delete, transform an image on the canvas
 */
public class SVGImage extends SVGShape{
	
	/**
	 * the reference of an object of this class
	 */
	private final SVGImage svgImage=this;
	
	/**
	 * the action listener used to draw the image
	 */
	private ImageActionListener imageAction=null;
	
	/**
	 * used to convert numbers into a string
	 */
	private DecimalFormat format;

	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGImage(SVGEditor editor) {
	    
		super(editor);
		
		ids.put("id","image"); //$NON-NLS-1$ //$NON-NLS-2$
		ids.put("idmenuitem","Image"); //$NON-NLS-1$ //$NON-NLS-2$
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labels.put("label", bundle.getString("shapeimagelabel")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredocreate", bundle.getString("shapeimageundoredocreate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredotranslate", bundle.getString("shapeundoredotranslate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredoresize", bundle.getString("shapeimageundoredoresize")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredorotate", bundle.getString("shapeimageundoredorotate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("helpcreate", bundle.getString("shapeimagehelpcreate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("filefilter", bundle.getString("shapeimagefilefilter")); //$NON-NLS-1$ //$NON-NLS-2$
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
		imageAction=new ImageActionListener();
		
		//the toggle button
		toolItem=new JToggleButton(disabledIcon);
		toolItem.setEnabled(false);
		toolItem.setToolTipText((String)labels.get("label")); //$NON-NLS-1$
		
		//adds a listener to the menu item and the toggle button
		menuitem.addActionListener(imageAction);
		toolItem.addActionListener(imageAction);
		
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
				
				imageAction.reset();
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
	 * draws an image
	 * @param frame the current SVGFrame
	 * @param bounds the bounds of the element
	 * @param imagePath the path of the image
	 */
	public void drawImage(SVGFrame frame, Rectangle2D.Double bounds, String imagePath){
		
		if(frame!=null && bounds!=null && imagePath!=null && ! imagePath.equals("")){ //$NON-NLS-1$
		    
			Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
			
			if(getSVGEditor().getSVGSelection()!=null && doc!=null){
			    
				final Element parent=getSVGEditor().getSVGSelection().getCurrentParentElement(frame);
				
				if(parent!=null){

					// creates the image element
					Element image = doc.createElementNS(doc.getDocumentElement().getNamespaceURI(), "image"); //$NON-NLS-1$
			
					image.setAttributeNS(SVGToolkit.xmlnsXLinkNS,"xlink:href", imagePath); //$NON-NLS-1$
					image.setAttributeNS(null,"x", format.format(bounds.x)); //$NON-NLS-1$
					image.setAttributeNS(null,"y", format.format(bounds.y)); //$NON-NLS-1$
					image.setAttributeNS(null,"width", format.format(bounds.width==0?1:bounds.width)); //$NON-NLS-1$
					image.setAttributeNS(null,"height", format.format(bounds.height==0?1:bounds.height)); //$NON-NLS-1$
					image.setAttributeNS(null,"preserveAspectRatio", "none meet"); //$NON-NLS-1$ //$NON-NLS-2$
					
					//sets that the svg has been modified
					frame.setModified(true);
		
					//creates final variables
					final Document fdoc=doc;
					final Node fimage=image;
					final SVGFrame fframe=frame;

					//attaches the image to the svg root element
					parent.appendChild(fimage);
		
					//create the undo/redo action and insert it into the undo/redo stack
					if(getSVGEditor().getUndoRedo()!=null){
				
						SVGUndoRedoAction action=new SVGUndoRedoAction((String)labels.get("undoredocreate")){ //$NON-NLS-1$

							public void undo(){
							    
							    parent.removeChild(fimage);
							}

							public void redo(){
							    
							    parent.appendChild(fimage);
							}
						};
			
						SVGSelection selection=getSVGEditor().getSVGSelection();
			
						if(selection!=null){
						    
							selection.deselectAll(frame, false, true);
							selection.addUndoRedoAction(frame, action);
							selection.handleNodeSelection(frame, image);
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
	 * draws what the image will be if the user releases the mouse button
	 * @param frame the current SVGFrame
	 * @param g the graphics
	 * @param bounds the bounds of the element
	 */
	protected void drawGhost(SVGFrame frame, Graphics2D g, Rectangle2D.Double bounds){
		
		if(frame!=null && g!=null && bounds!=null){
		    
		    g=(Graphics2D)g.create();
		    
			//draws the new awt rectangle outline to be displayed
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
	    
		if(imageAction!=null){
		    
			toolItem.removeActionListener(imageAction);
			toolItem.setSelected(false);
			toolItem.addActionListener(imageAction);
			
		    imageAction.cancelActions();
		}
	}

	/**
	 * 
	 * @author Jordi SUC
	 * the class allowing to get the position and size of the future drawn image 
	 */
	protected class ImageActionListener implements ActionListener{

		/**
		 * the hashtable associating a frame to its mouse adapter
		 */
		private final Hashtable mouseAdapterFrames=new Hashtable();
		
		/**
		 * an instance of this class
		 */
		private final ImageActionListener action=this;
		
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
		protected ImageActionListener(){
			
			createCursor=getSVGEditor().getCursors().getCursor("image"); //$NON-NLS-1$
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
				
				ImageMouseListener iml=null;

				//adds the new motion adapters
				for(it=frames.iterator(); it.hasNext();){
				    
					try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
			
					if(frm!=null && ! mouseAdapterFrames.containsKey(frm)){
				
						iml=new ImageMouseListener(frm);

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
							
							if(mouseListener!=null && ((ImageMouseListener)mouseListener).paintListener!=null){
								//removes the paint listener
								frm.getScrollPane().getSVGCanvas().removePaintListener(((ImageMouseListener)mouseListener).paintListener, true);
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
		 * the action to be done
		 * @param e the event
		 */
		public void actionPerformed(ActionEvent e){

			if((e.getSource() instanceof JMenuItem && ! toolItem.isSelected()) || (e.getSource() instanceof JToggleButton)){

				getSVGEditor().cancelActions(false);
				
				if(getSVGEditor().getFrameManager().getCurrentFrame()!=null){
					
					toolItem.removeActionListener(imageAction);
					toolItem.setSelected(true);
					toolItem.addActionListener(imageAction);
			
					//the listener is active
					isActive=true;
					source=e.getSource();
					
					Collection frames=getSVGEditor().getFrameManager().getFrames();
					Iterator it;
					SVGFrame frm=null;
					ImageMouseListener iml=null;
					    
					//adds the new motion adapters
					for(it=frames.iterator(); it.hasNext();){
					    
						try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
					
						if(frm!=null){
	
							iml=new ImageMouseListener(frm);
	
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
		}
			
		protected class ImageMouseListener extends MouseAdapter implements MouseMotionListener{
		
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
			 * @param frame the current frame
			 */
			public ImageMouseListener(SVGFrame frame){
			    
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
							svgImage.drawGhost(fframe, (Graphics2D)g, computedBounds);
						}
					}
				};
				
				frame.getScrollPane().getSVGCanvas().addLayerPaintListener(SVGCanvas.DRAW_LAYER, paintListener, false);
			}
		
			/**
			 * @param evt the event
			 */
			public void mouseDragged(MouseEvent evt) {
				
				//sets the second point of the element
				point2=frame.getAlignedWithRulersPoint(evt.getPoint());
				
				//asks the canvas to be repainted to draw the shape of the future element
				frame.getScrollPane().getSVGCanvas().delayedRepaint();
				
			}
			
			/**
			 * @param evt the event
			 */
			public void mouseMoved(MouseEvent evt) {
			}
				
			/**
			 * @param evt the event
			 */
			public void mousePressed(MouseEvent evt){
			    
				//sets the first point of the area corresponding to the future element
				point1=frame.getAlignedWithRulersPoint(evt.getPoint());
			}
			
			/**
			 * @param evt the event
			 */
			public void mouseReleased(MouseEvent evt){
				
				//the dialog box to specify the image file
				String uri;
				JFileChooser fileChooser=new JFileChooser();
				
				if(getSVGEditor().getResource().getCurrentDirectory()!=null){
				    
					fileChooser.setCurrentDirectory(getSVGEditor().getResource().getCurrentDirectory());
				}
				
				String canvasPath=""; //$NON-NLS-1$
				
				try{
					canvasPath=new URI(frame.getName()).toString();
				}catch (Exception ex){canvasPath="";} //$NON-NLS-1$

				final String fcanvasPath=canvasPath;
				
				//the file filter
				fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter(){

					public boolean accept(File f) {
					    
					    String name=f.getName();
					    name=name.toLowerCase();

						if(       f.isDirectory() || (name.endsWith(SVGToolkit.SVG_FILE_EXTENSION) &&
								! fcanvasPath.equals(f.toURI().toString())) || 
                                name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".gif")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						    
						    return true;
						}
						
						else return false;
					}

					public String getDescription() {
					    
						return (String)labels.get("filefilter"); //$NON-NLS-1$
					}
				});
				
				fileChooser.setMultiSelectionEnabled(false); 
					
				int returnVal=fileChooser.showOpenDialog(frame.getSVGEditor().getParent());
				
				if(returnVal==JFileChooser.APPROVE_OPTION) {
				    
					getSVGEditor().getResource().setCurrentDirectory(fileChooser.getCurrentDirectory());
					
					URI docUri=null;
					
					try{
					    docUri=new URI(fileChooser.getSelectedFile().getCanonicalPath());
					}catch (Exception ex){docUri=null;}

					if(docUri!=null){
					    if (docUri.isAbsolute()) {
						//relativizes the uri
						URI rUri=null;
						URI u=fileChooser.getSelectedFile().toURI();
						
						try{
							File docFile=new File(docUri);
							docUri=docFile.getParentFile().toURI();
							rUri=docUri.relativize(u);
						}catch (Exception ex){}
						
						uri=rUri.toString();
                        } else {
                            uri = docUri.toString();
                        }

                    }else{
					    
						try{
							
							uri=fileChooser.getSelectedFile().toURI().toString();
						}catch (Exception ex) {uri=null;}
					}

					Point2D.Double point=frame.getAlignedWithRulersPoint(evt.getPoint());

					//creates the image in the SVG document
					if(uri!=null && ! uri.equals("") && point1!=null && point1.x>=0 && point1.y>=0 && point!=null){ //$NON-NLS-1$
					
						final Rectangle2D.Double rect=getSVGEditor().getSVGToolkit().getComputedRectangle(point1, point);
						final String furi=new String(uri);

						Runnable runnable=new Runnable(){
							
							public void run() {

								svgImage.drawImage(frame, rect, furi);								
							}
						};
						
						frame.enqueue(runnable);
					}
				}
				
				getSVGEditor().cancelActions(true);
				point1=null;
				point2=null;
			}
		}
	}
}
