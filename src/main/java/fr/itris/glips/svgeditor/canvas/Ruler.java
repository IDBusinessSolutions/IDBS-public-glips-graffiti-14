package fr.itris.glips.svgeditor.canvas;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.*;

/**
 * the class of the rulers for a canvas
 * @author Jordi SUC
 */
public class Ruler extends JPanel{

	/**
	 * the font
	 */
	private final static Font font=new Font("theFont", Font.ROMAN_BASELINE, 9); //$NON-NLS-1$
	
	/**
	 * the scrollpane
	 */
	private SVGScrollPane scrollpane=null;
	
	/**
	 * the ruler refresh manager
	 */
	private RulerRefreshManager rulerRefreshManager;
	
	/**
	 * whether the ruler is horizontal or vertical
	 */
	private boolean isHorizontal=true;
	
	/**
	 * the range object
	 */
	private Range range=null;
	
	/**
	 * the current position of the cursor
	 */
	private double currentPosition=0;
	
	/**
	 * the shape of the cursor 
	 */
	private GeneralPath cursorShape=new GeneralPath();
	
	/**
	 * the colors for drawing the cursors
	 */
	private static final Color fillColorCursor=new Color(255, 0, 0);
	
	/**
	 * the constructor of the class
	 * @param scrollpane a scroll pane
	 * @param isHorizontal whether the ruler is horizontal or vertical
	 */
	public Ruler(SVGScrollPane scrollpane, boolean isHorizontal){
		
		this.scrollpane=scrollpane;
		this.isHorizontal=isHorizontal;
		setDoubleBuffered(false);
		setOpaque(false);
		rulerRefreshManager=new RulerRefreshManager();
		rulerRefreshManager.start();
		
		//creating the shape of the cursor
		if(isHorizontal){
			
			cursorShape.moveTo(0, 0);
			cursorShape.lineTo(6, 0);
			cursorShape.lineTo(0, 6);
			cursorShape.lineTo(-6, 0);
			cursorShape.closePath();
			
		}else{
			
			cursorShape.moveTo(0, 0);
			cursorShape.lineTo(0, -6);
			cursorShape.lineTo(6, 0);
			cursorShape.lineTo(0, 6);
			cursorShape.closePath();
		}
	}
	
	@Override
	public void paint(Graphics g) {

		drawRuler((Graphics2D)g);
		drawCursor(g);
	}
	
	/**
	 * @return the number for aligning
	 */
	public double getRangeForAlignment(){
		
		double number=5;
		
		if(range!=null){
			
			try{
				number=range.getRanges().getLast();
			}catch (Exception ex){}
		}
		
		return number;
	}
	
	/**
	 * refreshes the range of this ruler
	 */
	public void refreshRange(){
		
		//reinitializing the refresh manager
		rulerRefreshManager.reinitialize();
		
		double viewPortLength=0;
		double canvasLength=0;
		double scale=scrollpane.getSVGCanvas().getScale();

		if(isHorizontal){
			
			viewPortLength=scrollpane.getViewPort().width;
			canvasLength=scrollpane.getSVGCanvas().getScaledCanvasSize().width;
			
		}else{
			
			viewPortLength=scrollpane.getViewPort().height;
			canvasLength=scrollpane.getSVGCanvas().getScaledCanvasSize().height;
		}
		
		if(canvasLength>viewPortLength){
			
			range=new Range(viewPortLength, scale);
			
		}else{
			
			range=new Range(canvasLength, scale);
		}
	}
	
	/**
	 * draws the cursor
	 * @param g a graphics object
	 */
	protected void drawCursor(Graphics g){
		
		//computing the position of the cursor
		Point mousePoint=scrollpane.getSVGCanvas().getMousePosition();
		
		if(mousePoint!=null){
			
			Rectangle canvasBounds=Ruler.this.scrollpane.getCanvasBounds();
			
			if(Ruler.this.isHorizontal){

				if(canvasBounds.x>0){
					
					currentPosition=mousePoint.x+canvasBounds.x;
					
					if(currentPosition<canvasBounds.x){
						
						currentPosition=canvasBounds.x;
						
					}else if(currentPosition>canvasBounds.x+canvasBounds.width){
						
						currentPosition=canvasBounds.x+canvasBounds.width;
					}
					
				}else{
					
					currentPosition=mousePoint.x+canvasBounds.x;
				}
				
			}else{
				
				if(canvasBounds.y>0){
					
					currentPosition=mousePoint.y+canvasBounds.y;
					
					if(currentPosition<canvasBounds.y){
						
						currentPosition=canvasBounds.y;
						
					}else if(currentPosition>canvasBounds.y+canvasBounds.height){
						
						currentPosition=canvasBounds.y+canvasBounds.height;
					}
					
				}else{
					
					currentPosition=mousePoint.y+canvasBounds.y;
				}
			}
			
			if(currentPosition<0){
				
				currentPosition=0;
			}
		}

		//drawing the cursor
		Shape cursor=null;

		if(isHorizontal){
			
			cursor=cursorShape.createTransformedShape(AffineTransform.getTranslateInstance(currentPosition, getHeight()-cursorShape.getBounds().height));
			
		}else{
			
			cursor=cursorShape.createTransformedShape(AffineTransform.getTranslateInstance(getWidth()-cursorShape.getBounds().width, currentPosition));
		}
		
		//paint the cursor
		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		
		g2.setColor(fillColorCursor);
		g2.fill(cursor);
		
		g.dispose();
	}
	
	/**
	 * draws a ruler
	 * @param g a graphics object
	 */
	protected void drawRuler(Graphics2D g) {
		
		if(range==null){
			
			refreshRange();
		}

		g.setColor(Color.black);

        double smallestRange = 5.0;
        try {
            smallestRange = range.getRanges().getLast();
        } catch (NoSuchElementException ex) {
            refreshRange();
            //smallestRange = range.getRanges().getLast();
        }

        double scale=scrollpane.getSVGCanvas().getScale();
		double viewPortOrigin=0, viewPortLength=0, canvasLength=0;
		Rectangle canvasBounds=scrollpane.getCanvasBounds();

		//getting values used to draw the ruler
		double startPoint=0, startRangePoint=0, endPoint=0;
		
		if(canvasLength>viewPortLength){
			
			if(isHorizontal){
				
				viewPortOrigin=-canvasBounds.x;
				viewPortLength=scrollpane.getViewPort().width;
				canvasLength=canvasBounds.width;
				
			}else{
				
				viewPortOrigin=-canvasBounds.y;
				viewPortLength=scrollpane.getViewPort().height;
				canvasLength=canvasBounds.height;
			}

			//the scrolling is enabled
			startPoint=viewPortOrigin/scale;
			endPoint=(viewPortOrigin+viewPortLength)/scale;
			startRangePoint=Math.floor(startPoint/smallestRange)*smallestRange;

			double cur=0;
			
			while(cur<=endPoint){
				
				paintItem(g, cur, (startRangePoint+cur)*scale-viewPortOrigin);
				
				cur+=smallestRange;
			}

		}else{
			
			if(isHorizontal){
				
				viewPortOrigin=canvasBounds.x;
				viewPortLength=canvasBounds.width;
				canvasLength=canvasBounds.width;
				
			}else{
				
				viewPortOrigin=canvasBounds.y;
				viewPortLength=canvasBounds.height;
				canvasLength=canvasBounds.height;
			}
			
			//the scrolling is disabled
			startPoint=0;
			endPoint=canvasLength/scale;
			startRangePoint=0;
			
			double cur=0;
			
			while(cur<=endPoint){
				
				paintItem(g, cur, (startRangePoint+cur)*scale+viewPortOrigin);
				
				cur+=smallestRange;
			}
		}
	}
	
	/**
	 * draws a ruler item
	 * @param g the graphics
	 * @param canvasPos the position of the item in the unscaled canvas coordinates
	 * @param scrollpanePos the position of the item in the scrollpane coordinates
	 */
	protected void paintItem(Graphics g, double canvasPos, double scrollpanePos){
		
		//determining which range should be used for this item and then paints the item
		if(range.getRanges().size()==3){

			if(canvasPos%range.getRanges().getFirst()==0){
				
				paintRawItem(g, (int)canvasPos, (int)Math.round(scrollpanePos), 0, range.getRanges().getFirst()*scrollpane.getSVGCanvas().getScale());
				
			}else if(canvasPos%range.getRanges().get(1)==0){
				
				paintRawItem(g, (int)canvasPos, (int)Math.round(scrollpanePos), 1, range.getRanges().get(1)*scrollpane.getSVGCanvas().getScale());
				
			}else{
				
				paintRawItem(g, (int)canvasPos, (int)Math.round(scrollpanePos), 2, range.getRanges().getLast()*scrollpane.getSVGCanvas().getScale());
			}

		}else if(range.getRanges().size()==2){
			
			if(canvasPos%range.getRanges().getFirst()==0){
				
				paintRawItem(g, (int)canvasPos, (int)Math.round(scrollpanePos), 0, range.getRanges().getFirst()*scrollpane.getSVGCanvas().getScale());
				
			}else{
				
				paintRawItem(g, (int)canvasPos, (int)Math.round(scrollpanePos), 1, range.getRanges().get(1)*scrollpane.getSVGCanvas().getScale());
			}

		}else if(range.getRanges().size()==1){
			
			paintRawItem(g, (int)canvasPos, (int)Math.round(scrollpanePos), 0, range.getRanges().getFirst()*scrollpane.getSVGCanvas().getScale());
		}
	}
	
	/**
	 * paints an item
	 * @param g the graphics
	 * @param canvasPos the position of the item in the canvas
	 * @param pos the position of the item
	 * @param index the kind of representation for this item
	 * @param availableSize the available size until the next item of the same index
	 */
	protected void paintRawItem(Graphics g, int canvasPos, int pos, int index, double availableSize){
		
		switch (index){
		
			case 0 :
				
				if(isHorizontal){

					//drawing the string
					String str=canvasPos+""; //$NON-NLS-1$
					Rectangle2D rect=font.getStringBounds(str, new FontRenderContext(new AffineTransform(), true, false));
					
					if(rect.getWidth()<availableSize){
						
						Graphics2D g2=(Graphics2D)g.create();
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
						g2.setFont(font);
						g2.drawString(str, pos+2, font.getSize()-2);
						g2.dispose();
					}
					
					g.drawLine(pos, getHeight()-8, pos, getHeight());
					
				}else{
					
					Graphics2D g2=(Graphics2D)g.create();
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
					g2.setFont(font);

					//drawing the strings
					char[] tab=new String(canvasPos+"").toCharArray(); //$NON-NLS-1$
					
					if(tab.length*font.getSize()<=availableSize){
						
						int vpos=font.getSize()-1;
						
						for(char c : tab){
							
							g2.drawString(c+"", 0, pos+vpos); //$NON-NLS-1$
							vpos+=font.getSize()-1;
						}

						g2.dispose();
					}
					
					g.drawLine(getWidth()-8, pos, getWidth(), pos);
				}
				
				break;
				
			case 1 :
				
				if(isHorizontal){
					
					g.drawLine(pos, getHeight()-6, pos, getHeight());
					
				}else{
					
					g.drawLine(getWidth()-6, pos, getWidth(), pos);
				}
				
				break;
				
			case 2 :
				
				if(isHorizontal){
					
					g.drawLine(pos, getHeight()-4, pos, getHeight());
					
				}else{
					
					g.drawLine(getWidth()-4, pos, getWidth(), pos);
				}
				
				break;
		}
	}
	
	/**
	 * disposes this ruler
	 */
	public void dispose() {
		
		rulerRefreshManager.dispose();
		rulerRefreshManager=null;
	}
	
	/**
	 * the class of the range of the ruler
	 * @author Jordi SUC
	 */
	protected class Range{
		
		/**
		 * the constant for large 
		 */
		public static final int LARGE=0;
		
		/**
		 * the constant for medium 
		 */
		public static final int MEDIUM=1;
		
		/**
		 * the constant for small 
		 */
		public static final int SMALL=2;
		
		/**
		 * the list of the computed ranges
		 */
		private LinkedList<Double> ranges=new LinkedList<Double>();
		
		/**
		 * the constructor of the class
		 * @param distance the available scaled distance
		 * @param scale the scale 
		 */
		protected Range(double distance, double scale){
			
			//the geometry distance
			double geometryDistance=distance/scale;
			
			//the maximal range
			double maxRange=Math.floor(Math.log10(geometryDistance));
			
			//computing the list of all the ranges 
			double crange=maxRange, availableSpace=0, scaledSpace=0;

			while(true){
				
				//checking if the available space can be a part of the ranges
				availableSpace=Math.pow(10, crange);
				scaledSpace=availableSpace*scale;
				
				if(scaledSpace<=3){

					break;
				}
				
				ranges.add(availableSpace);
				
				//checking if the middle of the available space can be a part of the ranges
				availableSpace=availableSpace/2;
				scaledSpace=availableSpace*scale;
				
				if(scaledSpace<=3){
					
					break;
				}
				
				ranges.add(availableSpace);
				
				crange--;
			}

			//handling the list of the ranges so that only the last three ranges remain in it
			if(ranges.size()>3){
				
				ranges=new LinkedList<Double>(ranges.subList(ranges.size()-3, ranges.size()));
			}
		}
		
		/**
		 * @return the list of the ranges
		 */
		protected LinkedList<Double> getRanges(){
			
			return ranges;
		}
		
		/**
		 * computes and returns the closest number from the given number that belongs to a range 
		 * @param pos a number
		 * @return the closest number from the given number that belongs to a range 
		 */
		protected double getRound(double pos){
			
			double res=0;
			
			//getting the smallest integer range
			double smallestRange=ranges.getLast();
			double pos1=Math.floor(pos/smallestRange)*smallestRange;
			double pos2=(Math.floor(pos/smallestRange)+1)*smallestRange;

			if(Math.abs(pos1-pos)<Math.abs(pos2-pos)){
				
				res=pos1;
				
			}else{
				
				res=pos2;
			}
			
			return res;
		}
	}
	
	/**
	 * the class used to refresh 
	 * @author Jordi SUC
	 */
	protected class RulerRefreshManager extends Thread{
		
		/**
		 * the listener to the mouse motion actions on the canvas
		 */
		private MouseMotionListener canvasListener=null;
		
		/**
		 * whether this thread is disposed
		 */
		protected boolean isDisposed=false;
		
		/**
		 * whether the point has changed
		 */
		private boolean hasChanged=false;
		
		/**
		 * the position of the mouse
		 */
		private int pos=0;
		
		/**
		 * the base scaled position of the mouse
		 */
		private Point2D.Double displayedPoint=null;
		
		/**
		 * the constructor of the class
		 */
		protected RulerRefreshManager() {
			
			canvasListener=new MouseMotionListener(){

				public void mouseDragged(MouseEvent evt) {

					handleEvent(evt.getPoint());
				}
				
				public void mouseMoved(MouseEvent evt) {

					handleEvent(evt.getPoint());
				}
				
				/**
				 * handles the event
				 * @param point the mouse point for the event
				 */
				protected void handleEvent(Point point) {
					
					//computing the position of the mouse
					int tmpPos=pos=(isHorizontal?point.x:point.y);
					Point2D.Double dPoint=null;
					
					if(isHorizontal) {
						
						Point2D.Double point2D=new Point2D.Double(point.x, point.y);
						dPoint=scrollpane.getSVGFrame().getScaledPoint(point2D, true);
					}

					synchronized(this) {

						pos=tmpPos;
						
						if(isHorizontal) {
							
							displayedPoint=dPoint;
						}
						
						hasChanged=true;
					}
				}
			};
			
			//adding a listener to the mouse move actions on the canvas
			scrollpane.getSVGCanvas().addMouseMotionListener(canvasListener);
		}
		
		/**
		 * reinitializes this manager
		 */
		public void reinitialize() {
			
			synchronized(this) {

				hasChanged=false;
				pos=0;
				displayedPoint=null;
			}
		}
		
		@Override
		public void run() {
			
			while(! isDisposed) {
				
				if(hasChanged) {
					
					synchronized(this) {
						
						hasChanged=false;
					}

					SwingUtilities.invokeLater(new Runnable() {
						
						public void run() {
							
							//repainting all the rulers
							currentPosition=pos;
							repaint();
							
							if(isHorizontal && displayedPoint!=null) {
								
								scrollpane.getSVGFrame().getStateBar().setSVGX(
												SVGEditor.getDisplayFormat().format(displayedPoint.getX()));
								scrollpane.getSVGFrame().getStateBar().setSVGY(
												SVGEditor.getDisplayFormat().format(displayedPoint.getY()));
							}
						}
					});
				}

				try {sleep(100);}catch (Exception ex) {}
			}
		}
		
		/**
		 * disposes this manager
		 */
		public void dispose() {
			
			synchronized (this) {

				isDisposed=true;
			}
			
			scrollpane.getSVGCanvas().removeMouseMotionListener(canvasListener);
			canvasListener=null;
		}
	}

}
