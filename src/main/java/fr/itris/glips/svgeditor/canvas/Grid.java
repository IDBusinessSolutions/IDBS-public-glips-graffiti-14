package fr.itris.glips.svgeditor.canvas;

import java.awt.*;
import java.awt.geom.*;

/**
 * the class used to display a grid on a canvas
 * @author Jordi SUC
 */
public class Grid {

	/**
	 * the scrollpane
	 */
	private SVGScrollPane scrollpane=null;
	
	/**
	 * the distances for the grid
	 */
	private double horizontalDistance=50, verticalDistance=50;
	
	/**
	 * the color of the grid
	 */
	private Color gridColor=new Color(200, 200, 200);
	
	/**
	 * the stroke
	 */
	protected static final BasicStroke gridStroke=new BasicStroke(	1, BasicStroke.CAP_BUTT, 
	        																										BasicStroke.JOIN_BEVEL, 
	        																										0, new float[]{1, 2}, 0);
	
	/**
	 * whether the grid is enabled or not
	 */
	private boolean enableGrid=true;
	
	/**
	 * the constructor of the class
	 * @param scrollpane the scrollpane
	 */
	public Grid(SVGScrollPane scrollpane){
		
		this.scrollpane=scrollpane;
		
		initializeGrid();
	}
	
	/**
	 * initializes the grid
	 */
	protected void initializeGrid(){
		
		//creating the paint listener
		CanvasPaintListener paintListener=new CanvasPaintListener(){
			
			public void paintToBeDone(Graphics g) {
				
				if(enableGrid){
					
					//getting the canvas bounds and the viewport
					Rectangle canvasBounds=scrollpane.getCanvasBounds();
					Rectangle viewportRectangle=scrollpane.getViewPort();

					Rectangle2D.Double scaledCanvasBounds=scrollpane.getSVGFrame().getScaledRectangle(
							new Rectangle2D.Double(0, 0, canvasBounds.width, canvasBounds.height), true);
					Rectangle2D.Double innerRectangle=scrollpane.getSVGFrame().getScaledRectangle(
							new Rectangle2D.Double(-canvasBounds.x, -canvasBounds.y, viewportRectangle.width, viewportRectangle.height), true);
					
					Point2D.Double point=null;
					Rectangle2D.Double resultRect=new Rectangle2D.Double();

					if(canvasBounds.x>=0){
						
						resultRect.x=scaledCanvasBounds.x;
						resultRect.width=scaledCanvasBounds.width;
						
					}else{
						
						resultRect.x=innerRectangle.x;
						resultRect.width=innerRectangle.width;
					}
					
					if(canvasBounds.y>=0){
						
						
						resultRect.y=scaledCanvasBounds.y;
						resultRect.height=scaledCanvasBounds.height;
						
					}else{
						
						resultRect.y=innerRectangle.y;
						resultRect.height=innerRectangle.height;
					}
					
					Graphics2D g2=(Graphics2D)g.create();
					g2.setColor(gridColor);
					g2.setXORMode(Color.white);
					g2.setStroke(gridStroke);
					
					double startx=(Math.floor(resultRect.x/horizontalDistance)+1)*horizontalDistance;
					
					for(double i=startx; i<resultRect.x+resultRect.width; i+=horizontalDistance){
						
						point=scrollpane.getSVGFrame().getScaledPoint(new Point2D.Double(i, 0), false);
						g2.drawLine((int)point.x, 0,(int) point.x, canvasBounds.height);
					}
					
					double starty=(Math.floor(resultRect.y/verticalDistance)+1)*verticalDistance;
					
					for(double i=starty; i<resultRect.y+resultRect.height; i+=verticalDistance){
						
						point=scrollpane.getSVGFrame().getScaledPoint(new Point2D.Double(0, i), false);
						g2.drawLine(0, (int)point.y, canvasBounds.width, (int)point.y);
					}
					
					g2.dispose();
				}
			}
		};
		
		scrollpane.getSVGCanvas().addLayerPaintListener(SVGCanvas.GRID_LAYER, paintListener, false);
	}

	/**
	 * @return Returns the enableGrid.
	 */
	public boolean isEnableGrid() {
		return enableGrid;
	}

	/**
	 * @param enableGrid The enableGrid to set.
	 */
	public void setEnableGrid(boolean enableGrid) {
	
		this.enableGrid = enableGrid;
		scrollpane.getSVGCanvas().delayedRepaint();
	}

	/**
	 * @return Returns the gridColor.
	 */
	public Color getGridColor() {
		return gridColor;
	}

	/**
	 * @param gridColor The gridColor to set.
	 */
	public void setGridColor(Color gridColor) {
		this.gridColor = gridColor;
		scrollpane.getSVGCanvas().delayedRepaint();
	}

	/**
	 * @return Returns the horizontalDistance.
	 */
	public double getHorizontalDistance() {
		return horizontalDistance;
	}

	/**
	 * @param horizontalDistance The horizontalDistance to set.
	 */
	public void setHorizontalDistance(double horizontalDistance) {
		this.horizontalDistance = horizontalDistance;
		scrollpane.getSVGCanvas().delayedRepaint();
	}

	/**
	 * @return Returns the verticalDistance.
	 */
	public double getVerticalDistance() {
		return verticalDistance;
	}

	/**
	 * @param verticalDistance The verticalDistance to set.
	 */
	public void setVerticalDistance(double verticalDistance) {
		this.verticalDistance = verticalDistance;
		scrollpane.getSVGCanvas().delayedRepaint();
	}
}
