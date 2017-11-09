/*
 * Created on 23 mars 2004
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
package fr.itris.glips.svgeditor.canvas;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.*;

/**
 * @author Jordi SUC
 * the class of the scroll pane that will include the SVG canvas
 */
public class SVGScrollPane extends JPanel{

	/**
	 * the editor
	 */
	private SVGEditor editor;
	
	/**
	 * the SVGFrame associated with this SVGScrollPane
	 */
	private SVGFrame frame;
	
	/**
	 * the canvas
	 */
	private SVGCanvas canvas;
	
	/**
	 * the inner scroll pane
	 */
	private JScrollPane innerScrollPane;
	
	/**
	 * the panel into which the svg image will be painted
	 */
	protected JPanel contentPanel;
	
	/**
	 * the panels containing the rulers
	 */
	protected JPanel northPanel, westPanel;
	
	/**
	 * the rulers
	 */
	protected Ruler horizontalRuler, verticalRuler;
	
	/**
	 * the grid
	 */
	protected Grid grid=null;
	
	/**
	 * the zoom manager
	 */
	protected Zoom zoom=null;
	
	/**
	 * the canvas tool bar
	 */
	protected CanvasToolBar canvasToolBar=null;

	/**
	 * the corners
	 */
	protected Component northWestCornerBox, northEastCornerBox, southWestCornerBox;
	
	/**
	 * whether the mouse corrdinates should be converted so that they are aligned with the rulers
	 */
	protected boolean alignWithRulers=false;
	
	/**
	 * whether the rulers are visible
	 */
	protected boolean rulersVisible=true;

	/**
	 * the constructor of the class
	 * @param editor the editor
	 * @param frame the frame it is linked with
	 */
	public SVGScrollPane(SVGEditor editor, SVGFrame frame) {
		
		this.editor=editor;
		this.frame=frame;
		this.canvas=new SVGCanvas(editor, this);
		setAutoscrolls(true);
		
		initializeScrollpane();
		innerScrollPane.setDoubleBuffered(true);
	}
	
	/**
	 * the method used to initialize the scrollpane
	 */
	public void initializeScrollpane(){
		
		/*//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labelok=bundle.getString("labelok");
				labelcancel=bundle.getString("labelcancel");
				scrollpanegridrangexaxis=bundle.getString("scrollpanegridrangexaxis");
				scrollpanegridrangeyaxis=bundle.getString("scrollpanegridrangeyaxis");
				scrollpanegriddialogtitle=bundle.getString("scrollpanegriddialogtitle");
				scrollpanegriddialogwarningtitle=bundle.getString("scrollpanegriddialogwarningtitle");
				scrollpanegriddialogwarningmessage=bundle.getString("scrollpanegriddialogwarningmessage");
				scrollpanegridscale=bundle.getString("scrollpanegridscale");
			}catch (Exception ex){}
		}*/

		grid=new Grid(this);
		zoom=new Zoom(this);
		canvasToolBar=new CanvasToolBar(this);
		
		//creates the content panel and setting the properties
		contentPanel=new JPanel();
		
		GridBagLayout gridBag=new GridBagLayout();
		contentPanel.setLayout(gridBag);
		GridBagConstraints c=new GridBagConstraints();
		c.fill=GridBagConstraints.NONE;
		c.anchor=GridBagConstraints.CENTER;
		c.gridwidth=GridBagConstraints.REMAINDER;

		gridBag.setConstraints(canvas, c);
		contentPanel.add(canvas);

		//creates the scrollpane
		innerScrollPane=new JScrollPane(contentPanel);
		innerScrollPane.getHorizontalScrollBar().setUnitIncrement(innerScrollPane.getHorizontalScrollBar().getBlockIncrement());
		innerScrollPane.getVerticalScrollBar().setUnitIncrement(innerScrollPane.getVerticalScrollBar().getBlockIncrement());
		innerScrollPane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
		
		//creating the rulers
		horizontalRuler=new Ruler(this, true);
		verticalRuler=new Ruler(this, false);

		//creates the spacers and distributes the panels
		Dimension cornerSize=new Dimension(innerScrollPane.getVerticalScrollBar().getPreferredSize().width, innerScrollPane.getVerticalScrollBar().getPreferredSize().width);
		
		northPanel=new JPanel(new BorderLayout(0, 0));
		northWestCornerBox=Box.createRigidArea(cornerSize);
		northPanel.add(northWestCornerBox, BorderLayout.LINE_START);
		northPanel.add(horizontalRuler, BorderLayout.CENTER);
		horizontalRuler.setPreferredSize(new Dimension(innerScrollPane.getWidth(), cornerSize.height));

		westPanel=new JPanel(new BorderLayout(0, 0));
		southWestCornerBox=Box.createRigidArea(cornerSize);
		westPanel.add(verticalRuler, BorderLayout.CENTER);
		verticalRuler.setPreferredSize(new Dimension(cornerSize.width, innerScrollPane.getHeight()));
		
		//building the svg scrollpane
		setLayout(new BorderLayout(0, 0));
		add(BorderLayout.NORTH, northPanel);
		add(BorderLayout.WEST, westPanel);
		add(BorderLayout.CENTER, innerScrollPane);
		innerScrollPane.setWheelScrollingEnabled(false);
		
		final ComponentListener componentListener=new ComponentAdapter(){
			
			@Override
			public void componentResized(ComponentEvent evt) {

				canvas.setRenderedRectangle(new Rectangle(innerScrollPane.getViewport().getViewRect()), true, false);
			}
		};
		
		addComponentListener(componentListener);
		
		//adding the listener to the scrollbar changes
		final AdjustmentListener adjustListener=new AdjustmentListener(){
			
			public void adjustmentValueChanged(AdjustmentEvent evt) {

				if(! evt.getValueIsAdjusting()){

					canvas.setRenderedRectangle(new Rectangle(innerScrollPane.getViewport().getViewRect()), false, false);
				}
				
				refreshRulers();
			}
		};
		
		innerScrollPane.getHorizontalScrollBar().addAdjustmentListener(adjustListener);
		innerScrollPane.getVerticalScrollBar().addAdjustmentListener(adjustListener);
		
		final MouseWheelListener mouseWheelListener=new MouseWheelListener(){
			
			public void mouseWheelMoved(MouseWheelEvent evt) {
	
				if(evt.getScrollType()==MouseWheelEvent.WHEEL_UNIT_SCROLL){
	
					JScrollBar scrollBar=innerScrollPane.getVerticalScrollBar();
					
					scrollBar.setValue(scrollBar.getValue()+2*scrollBar.getBlockIncrement()*evt.getWheelRotation());
					canvas.setRenderedRectangle(new Rectangle(innerScrollPane.getViewport().getViewRect()), false, false);
					refreshRulers();
				}
			}
		};
		
		innerScrollPane.addMouseWheelListener(mouseWheelListener);

		//adds a dispose runnable
		getSVGFrame().addDisposeRunnable(new Runnable(){

            public void run() {

            	horizontalRuler.dispose();
            	verticalRuler.dispose();
            	canvasToolBar.dispose();
        		innerScrollPane.getHorizontalScrollBar().removeAdjustmentListener(adjustListener);
        		innerScrollPane.getVerticalScrollBar().removeAdjustmentListener(adjustListener);
        		innerScrollPane.removeMouseWheelListener(mouseWheelListener);
        		removeComponentListener(componentListener);
            }
		});
	}

	/**
	 * @return Returns the editor.
	 */
	public SVGEditor getSVGEditor() {
		return editor;
	}

	/**
	 * @return Returns the canvas.
	 */
	public SVGCanvas getSVGCanvas() {
		return canvas;
	}

	/**
	 * @return Returns the frame.
	 */
	public SVGFrame getSVGFrame() {
		return frame;
	}

	/**
	 * @return Returns the grid.
	 */
	public Grid getGrid() {
		return grid;
	}

	/**
	 * @return Returns the horizontalRuler.
	 */
	public Ruler getHorizontalRuler() {
		return horizontalRuler;
	}

	/**
	 * @return Returns the canvasToolBar.
	 */
	public CanvasToolBar getCanvasToolBar() {
		return canvasToolBar;
	}

	/**
	 * @return Returns the verticalRuler.
	 */
	public Ruler getVerticalRuler() {
		return verticalRuler;
	}

	/**
	 * @return Returns the alignWithRulers.
	 */
	public boolean isAlignWithRulers() {
		return alignWithRulers;
	}

	/**
	 * @param alignWithRulers The alignWithRulers to set.
	 */
	public void setAlignWithRulers(boolean alignWithRulers) {
		this.alignWithRulers=alignWithRulers;
	}
	
	/**
	 * sets the visibility state of the rulers
	 * @param visible whether the rulers are visible
	 */
	public void setRulersVisible(boolean visible){
		
		if(visible!=rulersVisible){
			
			this.rulersVisible=visible;
			
			if(visible){
				
				add(BorderLayout.NORTH, northPanel);
				add(BorderLayout.WEST, westPanel);
				
			}else{
				
				remove(northPanel);
				remove(westPanel);
			}
			
			revalidate();
		}
	}

	/**
	 * @return Returns the rulersVisible.
	 */
	public boolean isRulersVisible() {
		return rulersVisible;
	}

	/**
	 * @return Returns the zoom.
	 */
	public Zoom getZoom() {
		return zoom;
	}

	/**
	 * refreshes the rulers
	 */
	protected void refreshRulers(){
		
		horizontalRuler.refreshRange();
		horizontalRuler.repaint();
		verticalRuler.refreshRange();
		verticalRuler.repaint();
	}

	/**
	 * the method used to get the point corresponding to the given point when aligned with the rulers
	 * @param point the point
	 * @return the point correponding to the given point when aligned with the rulers
	 */
	public Point2D.Double getAlignedWithRulersPoint(Point2D.Double point){
		
		if(point!=null){
			
			Point2D.Double rulersRanges=new Point2D.Double(horizontalRuler.getRangeForAlignment(), verticalRuler.getRangeForAlignment());
			point=frame.getScaledPoint(point, true);
			
			if(alignWithRulers && rulersRanges!=null && point!=null){
				
				point.x=Math.round(point.x/rulersRanges.x)*rulersRanges.x;
				point.y=Math.round(point.y/rulersRanges.y)*rulersRanges.y;
			}
		}
		
		return point;
	}

	/**
	 * @return the horizontal ruler range
	 */
	public double getHorizontalRulerRange() {
		return horizontalRuler.getRangeForAlignment();
	}
	
	/**
	 * @return the vertical ruler range
	 */
	public double getVerticalRulerRange() {
		return verticalRuler.getRangeForAlignment();
	}

	/**
	 * renders a zoom action
	 * @param scale the new scale factor
	 */
	public void renderZoom(double scale){
		 
		//sets the new scale for the canvas
		canvas.setZoomFactor(scale);
		canvas.revalidate();
		revalidate();
		canvas.setRenderedRectangle(new Rectangle(innerScrollPane.getViewport().getViewRect()), true, true);
		refreshRulers();
	}
	
	/**
	 * changes the canvas size
	 * @param newSize the new canvas size
	 */
	public void changeSize(Point2D.Double newSize){
		 
		//sets the new scale for the canvas
		canvas.setCanvasSize(newSize);
		
		canvas.revalidate();
		revalidate();
		refreshRulers();
	}
	
	/**
	 * setting the scroll values
	 * @param scrollValues the scroll values
	 */
	public void setScrollValues(Dimension scrollValues){
		
		if(scrollValues!=null){
			
			innerScrollPane.getHorizontalScrollBar().setValue(scrollValues.width);
			innerScrollPane.getVerticalScrollBar().setValue(scrollValues.width);
		}
	}
	
	/**
	 * @return the scroll bar values
	 */
	public Dimension getScrollValues(){
		
		return new Dimension(innerScrollPane.getHorizontalScrollBar().getValue(), innerScrollPane.getVerticalScrollBar().getValue());
	}

	/**
	 * @return the viewport
	 */
	public Rectangle getViewPort(){
		
		return innerScrollPane.getViewportBorderBounds();
	}
	
	/**
	 * @return the canvas bounds
	 */
	public Rectangle getCanvasBounds(){
		
		Rectangle rect=new Rectangle();
		
		if(canvas.getBounds().width>=getViewPort().width){
			
			rect.x=contentPanel.getX();
			
		}else{
			
			rect.x=canvas.getX();
		}
		
		if(canvas.getBounds().height>=getViewPort().height){
			
			rect.y=contentPanel.getY();
			
		}else{
			
			rect.y=canvas.getY();
		}
		
		rect.width=canvas.getWidth();
		rect.height=canvas.getHeight();
		
		return rect;
	}

}
