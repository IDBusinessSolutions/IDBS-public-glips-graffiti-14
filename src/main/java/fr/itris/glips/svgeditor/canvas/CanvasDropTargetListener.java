package fr.itris.glips.svgeditor.canvas;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.geom.*;
import java.util.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.resources.*;
import fr.itris.glips.svgeditor.selection.*;
import fr.itris.glips.svgeditor.undoredo.*;

/**
 * the class of the listener to a drop
 * @author Jordi SUC
 */
public class CanvasDropTargetListener extends DropTargetAdapter{
	
	/**
	 * the canvas
	 */
	private SVGCanvas canvas=null;
	
	/**
	 * the document
	 */
	private Document doc=null;
	
	/**
	 * the bundle used to get labels
	 */
	private ResourceBundle bundle=SVGEditor.getBundle();
	
	/**
	 * the map associating the node name of shape to the name of the style property 
	 * that will be modified by the drag action
	 */
	private final Hashtable<String, String> dndPaintMap=new Hashtable<String, String>();
	
	/**
	 * the labels
	 */
	private String undoRedoPropertyChanges=""; //$NON-NLS-1$
	
	/**
	 * the constructor of the class
	 * @param canvas the canvas the listener is linked to
	 * @param doc the document
	 */
	public CanvasDropTargetListener(SVGCanvas canvas, Document doc){
		
		this.canvas=canvas;
		this.doc=doc;
		
		if(bundle!=null){
			
			try{
				undoRedoPropertyChanges=bundle.getString("undoredoproperties"); //$NON-NLS-1$
			}catch (Exception ex){}
		}

		//filling the drag and drop map
		Document dndPaint=SVGResource.getXMLDocument("dndPaint.xml"); //$NON-NLS-1$
		
		if(dndPaint!=null){
			
			Node node=null;
			String name="", styleProperty=""; //$NON-NLS-1$ //$NON-NLS-2$
			
			for(NodeIterator it=new NodeIterator(dndPaint); it.hasNext();){
				
				try{node=it.next();}catch(Exception ex){node=null;}
				
				if(node!=null && node instanceof Element){
					
					name=((Element)node).getAttribute("name"); //$NON-NLS-1$
					styleProperty=((Element)node).getAttribute("styleProperty"); //$NON-NLS-1$
					
					if(name!=null && styleProperty!=null && ! name.equals("") && ! styleProperty.equals("")){ //$NON-NLS-1$ //$NON-NLS-2$
						
						dndPaintMap.put(name, styleProperty);
					}
				}
			}
		}
	}
	
	/**
	 * disposes this listener
	 */
	public void dispose(){
		
		canvas=null;
		dndPaintMap.clear();
	}
	
	public void drop(DropTargetDropEvent e) {
		
		final DropTargetDropEvent evt=e;
		SVGSelection selection=canvas.getSVGEditor().getSVGSelection();
		
		//computes whether the data flavor of the event is a color data flavor
		DataFlavor supportedColorDataFlavor=null;
		Collection dataFlavors=SVGEditor.getColorChooser().getColorDataFlavors();
		DataFlavor df=null;
		
		for(Iterator it=dataFlavors.iterator(); it.hasNext();){
			
			df=(DataFlavor)it.next();
			
			if(df!=null && evt.isDataFlavorSupported(df)){
				
				supportedColorDataFlavor=df;
				break;
			}
		}
		
		if(selection!=null && evt!=null && (supportedColorDataFlavor!=null || evt.isDataFlavorSupported(DataFlavor.stringFlavor))){
			
			Point2D.Double location=canvas.getSVGFrame().getAlignedWithRulersPoint(evt.getLocation());
			
			//getting the node that has to be modified
			final Node node=canvas.getSVGFrame().getNodeAt(selection.getCurrentParentElement(canvas.getSVGFrame()), location);
			
			if(node!=null && node instanceof Element && dndPaintMap.containsKey(node.getNodeName())){
				
				//getting te name of the style property that has to be modified
				final String styleProperty=dndPaintMap.get(node.getNodeName());
				final String lastStylePropertyValue=canvas.getSVGEditor().getSVGToolkit().getStyleProperty((Element)node, styleProperty);
				
				if(styleProperty!=null && ! styleProperty.equals("")){ //$NON-NLS-1$
					
					Transferable transferable=evt.getTransferable();
					Color c=null;
					String ri=null;
					
					//getting the value of the transfered object
					if(supportedColorDataFlavor!=null){
						
						//the transfered object is a color
						try{
							c=(Color)transferable.getTransferData(supportedColorDataFlavor);
						}catch (Exception ex){c=null;}
						
					}else{
						
						//the transfered object is a string representing a resource
						try{
							ri=(String)transferable.getTransferData(DataFlavor.stringFlavor);
						}catch (Exception ex){ri=null;}
					}
					
					final Color color=c;
					final String resId=ri;
					
					Runnable runnable=new Runnable(){
						
						public void run() {
							
							//notifies that the document has changed
							canvas.getScrollPane().getSVGFrame().setModified(true);
							
							//if the last value of the property is a resource, this resource is unregistered for the node that is to be modified
							if(lastStylePropertyValue.indexOf("url(")!=-1){ //$NON-NLS-1$
								
								canvas.getScrollPane().getSVGFrame().removeNodeUsingResource(SVGToolkit.toUnURLValue(lastStylePropertyValue), node);
							}
							
							if(color!=null){
								
								//setting the color for the paint attribute
								SVGToolkit.setStyleProperty((Element)node, styleProperty, SVGEditor.getColorChooser().getColorString(color));
								
								if(canvas.getSVGEditor().getUndoRedo()!=null){
									
									final Color fcolor=color;
									
									SVGUndoRedoAction action=new SVGUndoRedoAction(undoRedoPropertyChanges){
										
										@Override
										public void undo(){
											
											SVGToolkit.setStyleProperty((Element)node, styleProperty, lastStylePropertyValue);
											
											//if the last value of the property is a resource, this resource is registered for the node that is to be modified
											if(lastStylePropertyValue.indexOf("url(")!=-1){ //$NON-NLS-1$
												
												canvas.getScrollPane().getSVGFrame().
																		addNodeUsingResource(SVGToolkit.toUnURLValue(lastStylePropertyValue), node);
											}
											
											//refreshing
											canvas.getSVGEditor().getSVGToolkit().forceReselection();
										}
										
										@Override
										public void redo(){
											
											SVGToolkit.setStyleProperty((Element)node, styleProperty, SVGEditor.getColorChooser().getColorString(fcolor));
											
											//if the last value of the property is a resource, this resource is unregistered for the node that is to be modified
											if(lastStylePropertyValue.indexOf("url(")!=-1){ //$NON-NLS-1$
												
												canvas.getScrollPane().getSVGFrame().
															removeNodeUsingResource(SVGToolkit.toUnURLValue(lastStylePropertyValue), node);
											}
											
											//refreshing
											canvas.getSVGEditor().getSVGToolkit().forceReselection();
										}
									};
									
									SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList(undoRedoPropertyChanges);
									actionlist.add(action);
									canvas.getSVGEditor().getUndoRedo().addActionList(canvas.getScrollPane().getSVGFrame(), actionlist);
								}
								
								//refreshing
								canvas.getSVGEditor().getSVGToolkit().forceReselection();
								
							}else if(resId!=null && ! resId.equals("")){ //$NON-NLS-1$
								
								//sets the resource id for the value of the paint attribute
								
								LinkedList<String> resourceNames=new LinkedList<String>();
								resourceNames.add("linearGradient"); //$NON-NLS-1$
								resourceNames.add("radialGradient"); //$NON-NLS-1$
								resourceNames.add("pattern"); //$NON-NLS-1$
								
								Hashtable resources=canvas.getSVGFrame().getResourcesFromDefs(doc, resourceNames);
								
								if(resources.containsKey(resId)){
									
									SVGToolkit.setStyleProperty((Element)node, styleProperty, SVGToolkit.toURLValue(resId));
									canvas.getScrollPane().getSVGFrame().addNodeUsingResource(resId, node);
									
									if(canvas.getSVGEditor().getUndoRedo()!=null){
										
										final String fresId=resId;
										
										SVGUndoRedoAction action=new SVGUndoRedoAction(undoRedoPropertyChanges){
											
											@Override
											public void undo(){
												
												SVGToolkit.setStyleProperty((Element)node, styleProperty, lastStylePropertyValue);
												
												//if the last value of the property is a resource, this resource is registered for the node that is to be modified
												if(lastStylePropertyValue.indexOf("url(")!=-1){ //$NON-NLS-1$
													
													canvas.getScrollPane().getSVGFrame().addNodeUsingResource(SVGToolkit.toUnURLValue(lastStylePropertyValue), node);
												}
												
												//unregisters the resource, as it is no more used by the node
												canvas.getScrollPane().getSVGFrame().removeNodeUsingResource(fresId, node);
												
												//refreshing
												canvas.getSVGEditor().getSVGToolkit().forceReselection();
											}
											
											@Override
											public void redo(){
												
												SVGToolkit.setStyleProperty((Element)node, styleProperty, SVGToolkit.toURLValue(fresId));
												
												//registers the resource as it is used by the node
												canvas.getScrollPane().getSVGFrame().addNodeUsingResource(fresId, node);
												
												//if the last value of the property is a resource, this resource is unregistered for the node that is to be modified
												if(lastStylePropertyValue.indexOf("url(")!=-1){ //$NON-NLS-1$
													
													canvas.getScrollPane().getSVGFrame().removeNodeUsingResource(SVGToolkit.toUnURLValue(lastStylePropertyValue), node);
												}
												
												//refreshing
												canvas.getSVGEditor().getSVGToolkit().forceReselection();
											}
										};
										
										SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList(undoRedoPropertyChanges);
										actionlist.add(action);
										canvas.getSVGEditor().getUndoRedo().addActionList(canvas.getScrollPane().getSVGFrame(), actionlist);
									}
								}
								
								//refreshing
								canvas.getSVGEditor().getSVGToolkit().forceReselection();
							}
						}
					};
					
					canvas.getScrollPane().getSVGFrame().enqueue(runnable);
				}
			}
		}
	} 
}
