package fr.itris.glips.svgeditor.canvas;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import org.w3c.dom.*;

/**
 * the class of the zoom
 * @author Jordi SUC
 */
public class Zoom {

	/**
	 * the scrollpane
	 */
	private SVGScrollPane scrollpane=null;
	
	/**
	 * the constructor of the class
	 * @param scrollpane the scrollpane
	 */
	public Zoom(SVGScrollPane scrollpane){
		
		this.scrollpane=scrollpane;
	}

	/**
	 * sets the new s factor
	 * @param scale a positive number
	 */
	public void scaleTo(double scale){
		
		scrollpane.renderZoom(scale);
	}
	
	/**
	 * setting the scale
	 * @param selection the rectangle defining a selected area on the canvas
	 */
	public void scaleTo(Rectangle2D selection){
		
		if(selection!=null){
			
			Rectangle viewportBounds=scrollpane.getViewPort();
			//Rectangle canvasBounds=scrollpane.getCanvasBounds();
			
			//computing the new scale and the scroll bar values for the scrollpane
			double newScale=1.0, 
						vScale=viewportBounds.width/selection.getWidth(), 
						hScale=viewportBounds.height/selection.getHeight();
			int vScrollValue=0, hScrollValue=0;
			
			if(vScale>=hScale){
				
				newScale=hScale;
				
			}else{
				
				newScale=vScale;
			}
			
			vScrollValue=(int)(selection.getX()*newScale);
			hScrollValue=(int)(selection.getY()*newScale);
			
			scrollpane.renderZoom(newScale);
			scrollpane.setScrollValues(new Dimension(hScrollValue, vScrollValue));
			
			scrollpane.getCanvasToolBar().update();
		}
	}
	
	/**
	 * scales the canvas so that the entire page is visible and spreads onto
	 * the most available space
	 */
	public void fitPageToScreen(){
		
		scaleTo(new Rectangle2D.Double(0, 0, scrollpane.getSVGCanvas().getWidth(), scrollpane.getSVGCanvas().getHeight()));
	}
	
	/**
	 * scales the canvas so that the entire page is visible and spreads onto
	 * the most available space
	 */
	public void fitWidthPageToScreen(){
		
		scaleTo(new Rectangle2D.Double(0, 0, scrollpane.getSVGCanvas().getWidth(), 0));
	}
	
	/**
	 * scales the canvas so that the selected nodes take the whole viewport
	 */
	public void fitSelectedNodesToScreen(){
		
		Collection<Element> selectedNodes=scrollpane.getSVGEditor().getSVGSelection().getCurrentSelection(scrollpane.getSVGFrame());
		
		Area area=null;
		Rectangle2D bounds=null;
		
		if(selectedNodes!=null){
			
			for(Element element : selectedNodes){
				
				if(element!=null){
					
					bounds=scrollpane.getSVGFrame().getNodeBounds(element);
					
					if(area==null){
						
						area=new Area(bounds);
					}else{
						
						area.add(new Area(bounds));
					}
				}
			}
			
			if(area!=null){
				
				scaleTo(area.getBounds2D());
			}
		}
	}
	
	/**
	 * scales the canvas so that the selected nodes take the whole viewport
	 */
	public void fitDrawingToScreen(){
		
		Rectangle2D drawingBounds=scrollpane.getSVGCanvas().getGraphicsNode().getBounds();
		
		if(drawingBounds!=null){
			
			scaleTo(drawingBounds);
		}
	}

}
