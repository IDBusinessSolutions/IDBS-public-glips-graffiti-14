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
import java.awt.dnd.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.batik.bridge.*;
import org.apache.batik.dom.svg.*;
import org.apache.batik.gvt.*;
import org.apache.batik.gvt.event.*;
import org.apache.batik.swing.*;
import org.w3c.dom.*;
import org.w3c.dom.svg.*;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import fr.itris.glips.svgeditor.*;

/**
 * @author Jordi SUC
 * the class of the canvas of a SVG file
 */
public class SVGCanvas extends JLayeredPane {
	
	/**
	 * the constant for the grid layer
	 */
	public static final int GRID_LAYER=0;
	
	/**
	 * the constant for the grid layer
	 */
	public static final int BOTTOM_LAYER=1;
	
	/**
	 * the constant for the grid layer
	 */
	public static final int SELECTION_LAYER=2;
	
	/**
	 * the constant for the grid layer
	 */
	public static final int DRAW_LAYER=3;
	
	/**
	 * the constant for the grid layer
	 */
	public static final int TOP_LAYER=4;
	
	/**
	 * the labels
	 */
	private static String 	documentCreatingLabel, documentLoadingLabel="",  //$NON-NLS-1$
										buildStartedLabel="", renderingStartedLabel=""; //$NON-NLS-1$ //$NON-NLS-2$
	
	static {
		
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
			
			try{
				documentCreatingLabel=bundle.getString("canvasDocumentCreatingLabel"); //$NON-NLS-1$
				documentLoadingLabel=bundle.getString("canvasDocumentLoadingLabel"); //$NON-NLS-1$
				buildStartedLabel=bundle.getString("canvasBuildStartedLabel"); //$NON-NLS-1$
				renderingStartedLabel=bundle.getString("canvasRenderingStartedLabel"); //$NON-NLS-1$
			}catch (Exception ex){ex.printStackTrace();}
		}
	}
	
	/**
	 * whether to use the progress bar
	 */
	private boolean useMonitor=false;

	/**
	 * the canvas
	 */
	private JPanel canvas;
	
	/**
	 * the painters panel
	 */
	private JPanel paintersPanel;
	
	/**
	 * the canvas uri
	 */
	private String uri=""; //$NON-NLS-1$
	
	/**
	 * the canvas document
	 */
	private Document document;
	
	/**
	 * the user agent
	 */
	private UserAgentAdapter userAgent;
	
	/**
	 * the builder
	 */
    private GVTBuilder builder;
    
    /**
     * the bridge context
     */
    private BridgeContext ctx;
    
    /**
     * the update manager
     */
    private UpdateManager manager;
    
    /**
     * the update tracker
     */
    private CanvasGraphicsNodeChangeListener graphicsNodeChangeAdapter;
    
    /**
     * the root graphics node
     */
    private RootGraphicsNode gvtRoot;
    
    /**
     * the dirty areas to be updated
     */
    private java.util.List<Area> dirtyAreas=new LinkedList<Area>();
	
	/**
	 * the offscreen image of the canvas
	 */
	private BufferedImage canvasOffscreenImage=null;
	
	/**
	 * the rendered rectangle
	 */
	private Rectangle renderedRectangle=new Rectangle(0, 0, 1, 1), tmpRectangle=new Rectangle(0, 0, 0, 0);
	
	/**
	 * the scrollpane that contains the canvas
	 */
	private SVGScrollPane scrollpane;
	
	/**
	 * the file of the project this canvas is associated with
	 */
	private File projectFile=null;
	
	/**
	 * the map associating an id integer to the list of the paint listeners for a layer
	 */
	private final Map<Integer, Set<CanvasPaintListener>> paintListeners=Collections.synchronizedMap(new HashMap<Integer, Set<CanvasPaintListener>>());
	
	/**
	 * the boolean used by the repaint manager
	 */
	private boolean shouldRepaint=false;
	
	/**
	 * whether the svg content should be repainted or not
	 */
	private boolean shouldRepaintSVGContent=false;
	
	/**
	 * whether a part of the svg content should be updated or not
	 */
	private boolean shouldUpdateSVGContent=false;
	
	/**
	 * the boolean enabling or disabling the refresh action when a paint listener is added or removed
	 */
	private boolean repaintEnabled=true;
	
	/**
	 * the cursor that was set before the last change of the cursor
	 */	
	private Cursor lastCursor=null;
	
	/**
	 * the cursor used to show that the computer is busy and the default cursor
	 */
	private Cursor waitCursor, defaultCursor;
	
	/**
	 * the boolean enabling or disabling the wait cursor
	 */
	private boolean enableWaitCursor=true;
	
	/**
	 * whether the canvas is being disposed or not
	 */
	private boolean isDisposing=false;
	
	/**
	 * the paint manager
	 */
	private Thread paintManager=new CanvasRepaintManager();

	/**
	 * the canvas' current scale and last scale
	 */
	private double scale=1.0;
	
	/**
	 * the editor
	 */
	private SVGEditor editor=null;
	
	/**
	 * the svg frame
	 */
	private SVGFrame frame=null;
	
	/**
	 * the progress bar dialog
	 */
	private SVGProgressBarDialog progressBar=null;
	
	/**
	 * the drop target listener
	 */
	private CanvasDropTargetListener dropTargetListener=null;
	
	/**
	 * the drop target
	 */
	private DropTarget dropTarget=null;
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 * @param scrollpane the scrollpane into which the canvas will be inserted
	 */
	public SVGCanvas(SVGEditor editor, SVGScrollPane scrollpane) {

		this.editor=editor;
		this.scrollpane=scrollpane;
		this.frame=scrollpane.getSVGFrame();
		setDoubleBuffered(true);
		
		//creating the paint listeners map structure
		paintListeners.put(GRID_LAYER, Collections.synchronizedSet(new HashSet<CanvasPaintListener>()));
		paintListeners.put(BOTTOM_LAYER, Collections.synchronizedSet(new HashSet<CanvasPaintListener>()));
		paintListeners.put(SELECTION_LAYER, Collections.synchronizedSet(new HashSet<CanvasPaintListener>()));
		paintListeners.put(DRAW_LAYER, Collections.synchronizedSet(new HashSet<CanvasPaintListener>()));
		paintListeners.put(TOP_LAYER, Collections.synchronizedSet(new HashSet<CanvasPaintListener>()));
		
		//handling the cursors
		waitCursor=editor.getCursors().getCursor("wait"); //$NON-NLS-1$
		defaultCursor=editor.getCursors().getCursor("default"); //$NON-NLS-1$
	}
	
	/**
	 * initializes the canvas
	 * @param doc the document of the canvas
	 */
	protected void initializeCanvas(final Document doc){

		this.document=doc;
		
		//creating the canvas
		canvas=new JPanel(){
			
			@Override
			protected void paintComponent(Graphics g) {
				
				super.paintComponent(g);
				paintCanvas(g);
				
				//hiding the progress bar if it is not visible
				handleProgressBar(0, "", true, false); //$NON-NLS-1$
			}
		};
		
		canvas.setBackground(Color.white);
		
		//creating the painters panel
		paintersPanel=new JPanel(){
			
			@Override
			public void paintComponent(Graphics g) {
				
				super.paintComponent(g);
				drawPainters((Graphics2D)g);
			}
		};
		
		paintersPanel.setDoubleBuffered(true);
		paintersPanel.setOpaque(false);
		
		//filling the layered pane
		add(canvas);
		setLayer(canvas, JLayeredPane.DEFAULT_LAYER, -1);
		add(paintersPanel);
		setLayer(paintersPanel, JLayeredPane.DEFAULT_LAYER, 0);
		
		if(useMonitor) {
			
			//the thread for creating the graphics node
			Thread thread=new Thread() {
				
				@Override
				public void run() {
				
					 initializeMain();
				}
			};
			
			thread.start();
			
		}else {
			
			 initializeMain();
		}
	}
	
	/**
	 * initializes the main elements
	 */
	protected void initializeMain() {
		
		//creating the graphics node
        try {
    		userAgent=new UserAgentAdapter();
    		ctx=new BridgeContext(userAgent);
            builder=new GVTBuilder();
            ctx.setDynamicState(BridgeContext.DYNAMIC);
            
        	handleProgressBar(50, buildStartedLabel, false, useMonitor);
            GraphicsNode gvt=builder.build(ctx, document);
            
            if(gvt!=null) {
            	
            	gvtRoot=gvt.getRoot();
            }
            
            if(useMonitor) {
            	
              	initializeRemaining();
                
            }else {
            	
                SwingUtilities.invokeLater(new Runnable() {
                	
                	public void run() {

                		initializeRemaining();
                	}
                });
            }
        }catch (Exception ex) {ex.printStackTrace();}
	}
	
	/**
	 * initializes the remaining elements
	 */
	protected void initializeRemaining() {

        //creating the update manager and tracker
        manager=new UpdateManager(ctx, gvtRoot, document);
        graphicsNodeChangeAdapter=new CanvasGraphicsNodeChangeListener();
        
        if(gvtRoot!=null) {
        	
        	gvtRoot.getRoot().addTreeGraphicsNodeChangeListener(graphicsNodeChangeAdapter);
        }
 
        //setting the size of the canvas
        Dimension scaledCanvasSize=getScaledCanvasSize();
        setCanvasPreferredSize(scaledCanvasSize);
		frame.displayFrame(scaledCanvasSize);
		
		//notifies that the frames contained in the frame manager has changed
		frame.getSVGEditor().getFrameManager().frameChanged();
		
		//creating the drop target listener
		dropTargetListener=new CanvasDropTargetListener(this, document);
		dropTarget=new DropTarget(this, dropTargetListener);
		dropTarget.setActive(true);
        
		frame.getStateBar().setSVGInfos(""); //$NON-NLS-1$
		
		//adds a dispose runnable
		getScrollPane().getSVGFrame().addDisposeRunnable(new Runnable(){
			
			public void run() {
				
				synchronized(SVGCanvas.this){isDisposing=true;}
				removeAll();
				
				if(projectFile!=null) {
					
					SVGEditor.getColorChooser().disposeColorsAndBlinkings(projectFile);
				}
				
		        if(gvtRoot!=null) {
		        	
		        	gvtRoot.getRoot().removeTreeGraphicsNodeChangeListener(graphicsNodeChangeAdapter);
		        }
				
				if(dropTarget!=null && dropTargetListener!=null){
					
					dropTarget.removeDropTargetListener(dropTargetListener);
					dropTarget.setActive(false);
					dropTarget.setComponent(null);
					dropTargetListener.dispose();
				}

				removeAllPaintListeners();
				paintListeners.clear();
				document=null;
				builder=null;
				
				if(ctx!=null) {
					
					ctx.dispose();
					ctx=null;
				}
				userAgent=null;
				
				if(manager!=null) {
					
					manager.interrupt();
					manager=null;
				}

				gvtRoot=null;
				canvasOffscreenImage=null;
				projectFile=null;
				
				if(progressBar!=null) {
					
					progressBar.disposeDialog();
					progressBar.dispose();
				}
			}
		});
		
        handleProgressBar(75, renderingStartedLabel, false, false);
        
		//starting the paint manager
		paintManager.start();
	}
	
	/**
	 * initialize the progress bar
	 * @param title the title
	 */
	protected void initializeProgressBar(String title) {
		
		if(useMonitor) {
			
			//creating the progress bar
	        if(editor.getParent() instanceof JFrame){
	        	
	        	progressBar=new SVGProgressBarDialog((JFrame)editor.getParent(), ""); //$NON-NLS-1$
	        	
	        }else{
	        	
	        	progressBar=new SVGProgressBarDialog(new JFrame(""), ""); //$NON-NLS-1$ //$NON-NLS-2$
	        }
	        
	        //setting the title and the cancel runnable
			progressBar.setTitle(scrollpane.getSVGFrame().getName());
			progressBar.setCancelRunnable(new Runnable(){
				
				public void run() {

					scrollpane.getSVGFrame().dispose();
				}
			});
			
			//showing the progress bar
			progressBar.setVisible(true);
		}
	}
	
	/**
	 * handles the progress bar
	 * @param value the current value
	 * @param label the label
	 * @param dispose whether the progress bar should be disposed
	 * @param useAWTThread whether to execute the action in the AWT thread explicitly
	 */
	protected void handleProgressBar(	final int value, final String label, 
																final boolean dispose, boolean useAWTThread) {
		
		if(useMonitor) {

			Runnable runnable=new Runnable() {
				
				public void run() {
					
					if(dispose) {
						
						 progressBar.setVisible(false);
						
					}else {
						
						progressBar.setProgressBarValueThreadSafe(value, 0, 100, label);
					}
				}
			};
			
			if(useAWTThread) {
				
				SwingUtilities.invokeLater(runnable);
				
			}else {
				
				runnable.run();
			}
		}
	}
	
	/**
	 * creates a new svg document
	 * @param width the width of the new document
	 * @param height the height of the new document
	 */
	public void newDocument(final String width, final String height){

		useMonitor=false;
		
		//initializing the progress bar
		initializeProgressBar(scrollpane.getSVGFrame().getShortName());
		handleProgressBar(0, documentCreatingLabel, false, false);
		
		SwingUtilities.invokeLater(new Runnable(){
			
			public void run() {

				DOMImplementation impl=SVGDOMImplementation.getDOMImplementation();
				String svgNS=SVGDOMImplementation.SVG_NAMESPACE_URI;
				SVGDocument doc=(SVGDocument)impl.createDocument(svgNS, "svg", null); //$NON-NLS-1$
				
				//gets the root element (the svg element)
				Element svgRoot=doc.getDocumentElement();
				
				//set the width and height attribute on the root svg element
				svgRoot.setAttributeNS(null, "width", width); //$NON-NLS-1$
				svgRoot.setAttributeNS(null, "height", height); //$NON-NLS-1$
				svgRoot.setAttribute("viewBox","0 0 "+SVGToolkit.getPixelledNumber(width)+" "+SVGToolkit.getPixelledNumber(height)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
                //initializing the canvas
				SVGToolkit.checkRtdaXmlns(doc);
				initializeCanvas(doc);
				frame.setModified(true);
            }
		});
	}
	
	/**
	 * sets the uri for the canvas
	 * @param uri a uri
	 */
	public void setURI(final String uri){
		
		useMonitor=true;
		
		if(uri!=null && ! uri.equals("")){ //$NON-NLS-1$
			
			this.uri=uri;

			//initializing the progress bar
			initializeProgressBar(scrollpane.getSVGFrame().getShortName());
			handleProgressBar(0, documentLoadingLabel, false, false);
			
			Thread thread=new Thread(){
				
				@Override
				public void run() {

					if(SVGEditor.isRtdaAnimationsVersion) {
						
						projectFile=SVGEditor.getColorChooser().getProjectFile(uri);
					}
					
					try{
						//creating the svg document corresponding to this uri
                        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(XMLReaderFactory.createXMLReader()
                                .getClass().getName());
						SVGDocument doc=factory.createSVGDocument(uri);

						if(doc!=null){
							
							Dimension scaledCanvasSize=getScaledCanvasSize(doc.getDocumentElement());
							frame.handleInitialDOMOperations(doc, scaledCanvasSize);
							SVGToolkit.checkRtdaXmlns(doc);
							initializeCanvas(doc);
						}
					}catch(Exception ex){ex.printStackTrace();handleProgressBar(0, "", true, false);} //$NON-NLS-1$
				}
			};
			
			thread.start();
		}
	}
	
	/**
	 * @return the uri of the canvas
	 */
	public String getURI(){
		
		return uri;
	}
	
	/**
	 * @return the viewing transform
	 */
	public AffineTransform getViewingTransform(){
		
		CanvasGraphicsNode canvasGraphicsNode=getCanvasGraphicsNode();
		
		if(canvasGraphicsNode!=null) {
			
			return canvasGraphicsNode.getViewingTransform();
		}
		
		return null;
	}

	/**
	 * @return the rendering transform
	 */
	public AffineTransform getRenderingTransform(){
		
		return AffineTransform.getScaleInstance(scale, scale);
	}
	
	/**
	 * @return the offscreen image
	 */
	public BufferedImage getOffscreen(){
		
		return canvasOffscreenImage;
	}
	
	/**
	 * @return the document of the canvas
	 */
	public Document getDocument(){
		
		return document;
	}
	
	/**
	 * setting the preferred size for this canvas
	 * @param size the preferred size
	 */
	public void setCanvasPreferredSize(Dimension size){

		setPreferredSize(size);
		canvas.setSize(size);
		paintersPanel.setSize(size);
	}

	/**
	 * @return the bridge context
	 */
	public BridgeContext getBridgeContext(){
		
		return ctx;
	}
	
	/**
	 * @return the update manager
	 */
	public UpdateManager getUpdateManager() {
		return manager;
	}
	
	/**
	 * paints the canvas
	 * @param gr a graphics object
	 */
	protected void paintCanvas(Graphics gr){
		
		Graphics2D g=(Graphics2D)gr.create(); 
		
		//drawing the offscreen image and painting it
		if(shouldRepaintSVGContent){
			
			//getting the gvt root
			GraphicsNode root=gvtRoot;

			if(root!=null){
				
				Rectangle usedRectangle=null;
				boolean isScrollAction=false;
				int scrollX=0, scrollY=0;
				
				if(tmpRectangle!=null){
					
					usedRectangle=new Rectangle(tmpRectangle);
					scrollX=usedRectangle.x-renderedRectangle.x;
					scrollY=usedRectangle.y-renderedRectangle.y;
				}
				
				//checking if the rendered rectangle will be changed owing to a scroll action		
				if(	usedRectangle!=null && usedRectangle.width==renderedRectangle.width && usedRectangle.height==renderedRectangle.height && 
					Math.abs(scrollX)<usedRectangle.width && Math.abs(scrollY)<usedRectangle.height){
					
					renderedRectangle.x=usedRectangle.x;
					renderedRectangle.y=usedRectangle.y;
					
					BufferedImage image=new BufferedImage(renderedRectangle.width, renderedRectangle.height, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2=image.createGraphics();
					BufferedImage tmpImage=null;
					
					if(scrollY>0){
						
						tmpImage=canvasOffscreenImage.getSubimage(0, scrollY, renderedRectangle.width, renderedRectangle.height-scrollY);
						g2.drawImage(tmpImage, 0, 0, tmpImage.getWidth(), tmpImage.getHeight(), null);
						
					}else if(scrollY<0){
						
						tmpImage=canvasOffscreenImage.getSubimage(0, 0, renderedRectangle.width, renderedRectangle.height+scrollY);
						g2.drawImage(tmpImage, 0, -scrollY, tmpImage.getWidth(), tmpImage.getHeight(), null);
					}
					
					if(scrollX>0){
						
						tmpImage=canvasOffscreenImage.getSubimage(scrollX, 0, renderedRectangle.width-scrollX, renderedRectangle.height);
						g2.drawImage(tmpImage, 0, 0, tmpImage.getWidth(), tmpImage.getHeight(), null);
						
					}else if(scrollX<0){
						
						tmpImage=canvasOffscreenImage.getSubimage(0, 0, renderedRectangle.width+scrollX, renderedRectangle.height);
						g2.drawImage(tmpImage, -scrollX, 0, tmpImage.getWidth(), tmpImage.getHeight(), null);
					}
					
					g2.dispose();
					canvasOffscreenImage=image;
					isScrollAction=true;
					
				}else if( ! shouldUpdateSVGContent){
					
					if(usedRectangle!=null){
						
						renderedRectangle.x=usedRectangle.x;
						renderedRectangle.y=usedRectangle.y;
						renderedRectangle.width=usedRectangle.width;
						renderedRectangle.height=usedRectangle.height;
					}
		
					//creating the new offscreen image
					canvasOffscreenImage=new BufferedImage(renderedRectangle.width, renderedRectangle.height, BufferedImage.TYPE_INT_ARGB);
				}

				if(isScrollAction){
					
					//computing the transform
					AffineTransform af=AffineTransform.getScaleInstance(scale, scale);
					af.preConcatenate(AffineTransform.getTranslateInstance(-renderedRectangle.x, -renderedRectangle.y));
					
					//computing the rendered rectangle in the base coordinates
					Rectangle2D.Double baseRectangle=
								getSVGFrame().getScaledRectangle(new Rectangle2D.Double(renderedRectangle.x, renderedRectangle.y, 
																					renderedRectangle.width, renderedRectangle.height), true);

					//computing the image dimensions in the base coordinates
					Rectangle2D.Double rect=getSVGFrame().getScaledRectangle(
																				new Rectangle2D.Double(0, 0, canvasOffscreenImage.getWidth(), 
																													canvasOffscreenImage.getHeight()), true);
					Point2D.Double basedImageSize=new Point2D.Double(rect.getWidth(), rect.getHeight());
					
					//computing the scrolling values in the base coordinates
					Point2D.Double baseScrollPoint=getSVGFrame().getScaledPoint(new Point2D.Double(scrollX, scrollY), true);
					double baseScrollX=baseScrollPoint.getX(), baseScrollY=baseScrollPoint.getY();
					
					Graphics2D g2=canvasOffscreenImage.createGraphics();
					g2.setTransform(af);
					
					//clearing the image
					g2.setColor(getBackground());
					
					Rectangle2D.Double svgRectangle=null;
					
					if(baseScrollY>0){
						
						svgRectangle=new Rectangle2D.Double(baseRectangle.x, baseRectangle.y+basedImageSize.y-baseScrollY, basedImageSize.x, baseScrollY);
						
					}else if(baseScrollY<0){
				
						svgRectangle=new Rectangle2D.Double(baseRectangle.x, baseRectangle.y, basedImageSize.x, -baseScrollY);
					}
					
					if(baseScrollX>0){
					
						svgRectangle=new Rectangle2D.Double(baseRectangle.x+basedImageSize.x-baseScrollX, baseRectangle.y, baseScrollX, basedImageSize.y);
						
					}else if(baseScrollX<0){
						
						svgRectangle=new Rectangle2D.Double(baseRectangle.x, baseRectangle.y, -baseScrollX, basedImageSize.y);
					}
					
					if(svgRectangle!=null){
						
						g2.clip(svgRectangle);
						
						//setting the rendering hints
				        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
				        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_DEFAULT);
				        g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
				        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
				        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
				        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
				        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
				        
				        //painting the image
						root.paint(g2);
						g2.dispose();
					}
						
				}else{
					
					//computing the transform
					AffineTransform af=AffineTransform.getScaleInstance(scale, scale);
					af.preConcatenate(AffineTransform.getTranslateInstance(-renderedRectangle.x, -renderedRectangle.y));
					
					if (canvasOffscreenImage != null) // unpredictably null on start up - just don't paint if it is.
					{
					//root.setTransform(af);
					Graphics2D g2=canvasOffscreenImage.createGraphics();
					g2.setTransform(af);

					//setting the rendering hints
			        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
			        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_DEFAULT);
			        g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
			        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
			        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
			        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
			        
			        if(shouldUpdateSVGContent) {
			        	
			        	//getting the list of the dirty areas
			        	java.util.List<Area> areas=null;

			        	synchronized(this) {
			        		
			        		areas=dirtyAreas;
			        		dirtyAreas=new LinkedList<Area>();
			        	}

			        	if(areas!=null) {
			        		
			        		//computing the clip rectangle
			        		Area clip=null;
			        		
			        		for(Area area : areas) {
			        			
			        			if(area!=null) {
			        				
			        				if(clip==null) {
			        					
			        					clip=area;
			        					
			        				}else {
			        					
			        					clip.add(area);
			        				}
			        			}
			        		}
			        		
			        		if(clip!=null) {
			        			
			        			g2.setClip(clip);
			        			
						        //painting the background
						        g2.setColor(Color.white);
						        g2.fill(clip);
			        		}
			        	}
			        }

			        //painting the image
					root.paint(g2);
					g2.dispose();
					}
				}
			}

			synchronized(this) {
				
				tmpRectangle=null;
				shouldRepaintSVGContent=false;
				shouldUpdateSVGContent=false;
			}
		}

		g.drawRenderedImage(canvasOffscreenImage, AffineTransform.getTranslateInstance(renderedRectangle.x, renderedRectangle.y));
		g.dispose();
	}
	
	/**
	 * setting the zoom factor
	 * @param scale the zoom factor
	 */
	public void setZoomFactor(double scale){
		
		this.scale=scale;
		setCanvasPreferredSize(getScaledCanvasSize());
	}
	
	/**
	 * setting the new canvas size
	 * @param newSize the new size
	 */
	public void setCanvasSize(Point2D.Double newSize){

		CanvasGraphicsNode canvasGraphicsNode=getCanvasGraphicsNode();
		
		if(canvasGraphicsNode.getPositionTransform()==null){
			
			canvasGraphicsNode.setPositionTransform(new AffineTransform());
		}
		
		Element root=document.getDocumentElement();
		String width=SVGEditor.getFormat().format(newSize.x), height=SVGEditor.getFormat().format(newSize.y);
		root.setAttribute("width", width); //$NON-NLS-1$
		root.setAttribute("height", height); //$NON-NLS-1$
		root.setAttribute("viewBox", "0 0 "+width+" "+height); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		setCanvasPreferredSize(getScaledCanvasSize());
		requestRepaintContent();
	}
	
	/** 
	 * @return SVGScrollPane the scrollpane that contains the canvas
	 */
	public SVGScrollPane getScrollPane(){
		return scrollpane;
	}

	/**
	 * @return Returns the frame.
	 */
	public SVGFrame getSVGFrame() {
		return frame;
	}

	/**
	 * @return the editor
	 */
	public SVGEditor getSVGEditor(){
		
		return editor;
	}
	
	/**
	 * @return the rendering rectangle
	 */
	public Rectangle getRenderedRectangle() {
		return renderedRectangle;
	}
	
	/**
	 * sets the new rendered rectangle
	 * @param rect a rendered rectangle
	 * @param reinitialize whether the stored information about the rendered picture should be erased or not
	 * @param forceReinitialize whether the stored information about the rendered picture should be erased or not, forcing it if necessary
	 */
	public void setRenderedRectangle(Rectangle rect, boolean reinitialize, boolean forceReinitialize){

		if(forceReinitialize){
		
			renderedRectangle=new Rectangle(0, 0, 1, 1);
			tmpRectangle=rect;
			shouldRepaintSVGContent=true;
			
		}else if(reinitialize) {
			
			if(rect!=null && ! renderedRectangle.equals(rect) && reinitialize){
				
				renderedRectangle=new Rectangle(0, 0, 1, 1);
				tmpRectangle=rect;
				shouldRepaintSVGContent=true;
			}
			
		}else if(rect!=null && ! renderedRectangle.equals(rect) && renderedRectangle.width>0 && renderedRectangle.height>0){
			
			tmpRectangle=rect;
			shouldRepaintSVGContent=true;
		}
	}
	
	/**
	 * requests that the svg content should be repainted
	 */
	public void requestRepaintContent(){
		
		synchronized(this){
			
			shouldRepaintSVGContent=true;
		}
	}
	
	/**
	 * requests that the svg content should be updates
	 */
	@SuppressWarnings(value="all")
	public void requestUpdateContent(){

		if(graphicsNodeChangeAdapter!=null) {
			
			synchronized(this){
				
				Area dArea=graphicsNodeChangeAdapter.getDirtyArea();
				
				if(dArea!=null) {
					
					dirtyAreas.add(dArea);
				}
				
				shouldUpdateSVGContent=true;
				shouldRepaintSVGContent=true;
			}
		}
	}

	/**
	 * @return Returns the projectFile.
	 */
	public File getProjectFile() {
		return projectFile;
	}
	
	/**
	 * @param projectFile The projectFile to set.
	 */
	public void setProjectFile(File projectFile) {
		this.projectFile = projectFile;
	}
	
	/**
	 * sets the current cursor
	 * @param cursor the current cursor
	 */
	public void setSVGCursor(Cursor cursor){
		
		if(cursor!=null && ! cursor.equals(getCursor()) && ! waitCursor.equals(getCursor())){
			
			lastCursor=getCursor();
			setCursor(cursor);
		}
	}
	
	/**
	 * returns to the last cursor
	 */
	public void returnToLastCursor(){
		
		if(enableWaitCursor){
			
			if(lastCursor==null || (lastCursor!=null && lastCursor.equals(waitCursor))){
				
				setCursor(defaultCursor);
				
			}else if(lastCursor!=null){
				
				setCursor(lastCursor);
			}
		}
	}
	
	/**
	 * displays the wait cursor
	 */
	public void displayWaitCursor(){
		
		if(enableWaitCursor && waitCursor!=null && ! waitCursor.equals(getCursor())){
			
			lastCursor=getCursor();
			setCursor(waitCursor);
		}
	}
	
	/**
	 * enables or disables the display of the wait cursor
	 * @param enable true to enable the display of the wait cursor
	 */
	public void setEnableWaitCursor(boolean enable){
		
		enableWaitCursor=enable;
	}
	
	/**
	 * @return the canvas' size
	 */
	public Point2D.Double getGeometryCanvasSize(){
		
		//gets the root element
		if(document!=null){
			
			Element root=document.getDocumentElement();
			
			if(root!=null){
				
				double w=SVGToolkit.getPixelledNumber(root.getAttributeNS(null, "width")); //$NON-NLS-1$
				double h=SVGToolkit.getPixelledNumber(root.getAttributeNS(null, "height")); //$NON-NLS-1$
				
				return new Point2D.Double(w, h);
			}
		}
		
		return new Point2D.Double(0,0);
	}
	
	/**
	 * @param root the root element
	 * @return the scaled canvas' size
	 */
	public Dimension getScaledCanvasSize(Element root){
		
		Dimension scaledSize=new Dimension(0, 0);
		
		if(root!=null){
			
			double w=0, h=0;
			
			try{
				w=SVGToolkit.getPixelledNumber(root.getAttributeNS(null, "width"))*scale; //$NON-NLS-1$
				h=SVGToolkit.getPixelledNumber(root.getAttributeNS(null, "height"))*scale; //$NON-NLS-1$
				scaledSize.width=(int)w;
				scaledSize.height=(int)h;
			}catch (Exception ex){}
		}
		
		return scaledSize;
	}
	
	/**
	 * @return the scaled canvas' size
	 */
	public Dimension getScaledCanvasSize(){
		
		Dimension scaledSize=new Dimension(0, 0);
		
		//gets the root element
		if(document!=null){
			
			Element root=document.getDocumentElement();
			
			if(root!=null){
				
				double w=0, h=0;
				
				try{
					w=SVGToolkit.getPixelledNumber(root.getAttributeNS(null, "width"))*scale; //$NON-NLS-1$
					h=SVGToolkit.getPixelledNumber(root.getAttributeNS(null, "height"))*scale; //$NON-NLS-1$
					scaledSize.width=(int)w;
					scaledSize.height=(int)h;
				}catch (Exception ex){}
			}
		}
		
		return scaledSize;
	}
	
	/**
	 * @return Returns the repaintEnabled.
	 */
	public boolean isRepaintEnabled() {
		
		return repaintEnabled;
	}
	
	/**
	 * @param repaintEnabled The repaintEnabled to set.
	 */
	public synchronized void setRepaintEnabled(boolean repaintEnabled){
		
		this.repaintEnabled=repaintEnabled;
		
		if(repaintEnabled){
			
			shouldRepaint=true;
		}
	}
	
	/**
	 * adds a grid paint listener
	 * @param type the integer representing the layer at which the painting should be done
	 * @param l the grid paint listener to be added
	 * @param makeRepaint the boolean telling to make a repaint after the paint listener was added or not
	 */
	public void addLayerPaintListener(int type, CanvasPaintListener l, boolean makeRepaint){
		
		if(l!=null){
			
			Set<CanvasPaintListener> set=paintListeners.get(type);
			
			if(set!=null){
				
				set.add(l);
			}

			if(isRepaintEnabled() && makeRepaint){
				
				synchronized (this) {

					shouldRepaint=true;
				}
			}
		}
	}

	/**
	 * removes a paint listener
	 * @param l the paint listener to be removed
	 * @param makeRepaint the boolean telling to make a repaint after the paint listener was removed or not
	 */
	public void removePaintListener(CanvasPaintListener l, boolean makeRepaint){
		
		paintListeners.get(GRID_LAYER).remove(l);
		paintListeners.get(BOTTOM_LAYER).remove(l);
		paintListeners.get(SELECTION_LAYER).remove(l);
		paintListeners.get(DRAW_LAYER).remove(l);
		paintListeners.get(TOP_LAYER).remove(l);
		
		if(isRepaintEnabled() && makeRepaint){
			
			synchronized (this) {

				shouldRepaint=true;
			}
		}
	}
	
	/**
	 * notifies the paint listeners when a paint action is done
	 *@param g2 the graphics
	 */
	protected void drawPainters(Graphics2D g2){
		
		if(isRepaintEnabled()){
			
			//setting the rendering hints
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
	        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
	        g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
	        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
	        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
	        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
			
			for(CanvasPaintListener listener : new HashSet<CanvasPaintListener>(paintListeners.get(GRID_LAYER))){
				
				listener.paintToBeDone(g2);
			}
			
			for(CanvasPaintListener listener : new HashSet<CanvasPaintListener>(paintListeners.get(BOTTOM_LAYER))){
				
				listener.paintToBeDone(g2);
			}
			
			for(CanvasPaintListener listener : new HashSet<CanvasPaintListener>(paintListeners.get(SELECTION_LAYER))){
				
				listener.paintToBeDone(g2);
			}
			
			for(CanvasPaintListener listener : new HashSet<CanvasPaintListener>(paintListeners.get(DRAW_LAYER))){
				
				listener.paintToBeDone(g2);
			}
			
			for(CanvasPaintListener listener : new HashSet<CanvasPaintListener>(paintListeners.get(TOP_LAYER))){
				
				listener.paintToBeDone(g2);
			}
		}
	}
	
	/**
	 * removes all paint listeners
	 */
	public void removeAllPaintListeners(){
		
		paintListeners.get(GRID_LAYER).clear();
		paintListeners.get(BOTTOM_LAYER).clear();
		paintListeners.get(SELECTION_LAYER).clear();
		paintListeners.get(DRAW_LAYER).clear();
		paintListeners.get(TOP_LAYER).clear();
	}
	
	/**
	 * asks the repaint manager to repaint the canvas 
	 */
	public synchronized void delayedRepaint(){
		
		if(isRepaintEnabled()){
			
			shouldRepaint=true;
		}
	}
	
	/**
	 * Builds the InputMap of this canvas with a set of predefined
	 * Actions.
	 */
	protected void installKeyboardActions() {
		
	}
	
	/**
	 * gets the canvas' current scale
	 * @return the canvas' current scale
	 */
	public double getScale(){
		return scale;
	}
	
	/**
	 * @return the gvt root
	 */
	public GraphicsNode getGraphicsNode(){
		return getCanvasGraphicsNode();
	}
	
	
	/**
	 * @return the canvas graphics node for this canvas
	 */
	private CanvasGraphicsNode getCanvasGraphicsNode() {
		
        java.util.List children=gvtRoot.getChildren();
        
        if(children.size()==0) {
        	
            return null;
        }
        
        GraphicsNode gn=(GraphicsNode)children.get(0);
        
        if(! (gn instanceof CanvasGraphicsNode)) {
        	
        	 return null;
        }
           
        return (CanvasGraphicsNode)gn;
    }

	/**
	 * the class of the thread handling the painting of the svg canvas
	 * @author Jordi SUC
	 */
	protected class CanvasRepaintManager extends Thread{
		
		/**
		 * the current paint runnable
		 */
		protected PaintRunnable paintRunnable=null;
		
		@Override
		public void run() {
			
			while(! isDisposing){
				
				try{
					sleep(25);
				}catch (Exception ex){}
				
				if(		(shouldRepaint || shouldRepaintSVGContent) && 
						(paintRunnable==null || (paintRunnable!=null && paintRunnable.isExecuted()))){
					
					final boolean fshouldRepaint=shouldRepaint, fshouldRepaintSVGContent=shouldRepaintSVGContent;

					paintRunnable=new PaintRunnable(){
						
						@Override
						public void run() {
							
							if(fshouldRepaintSVGContent){
								
								canvas.repaint();
								
							}else if(fshouldRepaint){
								
								paintersPanel.repaint();
							}
							
							super.run();
						}  
					};
					
					SwingUtilities.invokeLater(paintRunnable);
					
					synchronized(SVGCanvas.this){
						
						shouldRepaint=false;
					}
				}
			}
		}
		
		/**
		 * the class of the runnables used to paint
		 * @author Jordi SUC
		 */
		protected abstract class PaintRunnable implements Runnable{
			
			/**
			 * whether this boolean has been executed
			 */
			protected boolean executed=false;
			
			/**
			 * the run method
			 */
			public void run(){
				
				executed=true;
			}

			/**
			 * @return Returns the executed.
			 */
			public boolean isExecuted() {
				return executed;
			}
		}
	}
	
	/**
	 * the extended class of a JSVGCanvas that will be used to provide useful information
	 * @author Jordi SUC
	 */
	protected class ExtendedJSVGCanvas extends JSVGCanvas{
		
		/**
		 * @return the bridge context
		 */
		public BridgeContext getBridgeContext(){
			
			return bridgeContext;
		}
	}
	
	/**
	 * the class of the listener to the changes on the graphic nodes
	 * @author Jordi SUC
	 */
	protected class CanvasGraphicsNodeChangeListener implements GraphicsNodeChangeListener{
		
		/**
		 * the map of the initial areas
		 */
		private Map<GraphicsNode, Area> initialAreas=new HashMap<GraphicsNode, Area>();
		
		/**
		 * the map associating a graphics node to a rectangle
		 */
		private Map<GraphicsNode, Area> changedAreas=new HashMap<GraphicsNode, Area>();
		
		/**
		 * @see org.apache.batik.gvt.event.GraphicsNodeChangeListener#changeStarted(org.apache.batik.gvt.event.GraphicsNodeChangeEvent)
		 */
		public void changeStarted(GraphicsNodeChangeEvent gnce) {
			
			gnce.consume();
			GraphicsNode gnode=gnce.getChangeSrc();
			
			if(gnode==null) {
				
				gnode=gnce.getGraphicsNode();
			}

			if(! (gnode instanceof CanvasGraphicsNode) && ! initialAreas.containsKey(gnode)) {

				Rectangle2D rect=gnode.getBounds();
				
				if(rect==null) {
					
					rect=gnode.getGeometryBounds();
				}
				
				if(gnode.getTransform()!=null) {
					
					rect=gnode.getTransform().createTransformedShape(rect).getBounds2D();
				}

				if(rect!=null) {

					rect.setRect(rect.getX()-2, rect.getY()-2, rect.getWidth()+4, rect.getHeight()+4);
					initialAreas.put(gnode, new Area(rect));
				}
			}
		}
		
		/**
		 * @see org.apache.batik.gvt.event.GraphicsNodeChangeListener#changeCompleted(org.apache.batik.gvt.event.GraphicsNodeChangeEvent)
		 */
		public void changeCompleted(GraphicsNodeChangeEvent gnce) {
		
			gnce.consume();
			GraphicsNode gnode=gnce.getChangeSrc();

			if(gnode==null) {
				
				gnode=gnce.getGraphicsNode();
			}

			if(	! (gnode instanceof CanvasGraphicsNode) && 
				! changedAreas.containsKey(gnode) && initialAreas.containsKey(gnode)) {
				
				//getting the old rectangle
				Area oldArea=initialAreas.get(gnode);
				Rectangle2D rect=gnode.getBounds();
				
				if(rect==null) {
					
					rect=gnode.getGeometryBounds();
				}
				
				if(gnode.getTransform()!=null) {
					
					rect=gnode.getTransform().createTransformedShape(rect).getBounds2D();
				}
				// as gnode.getBounds returns the old bounds before the change - we just set the whole screen as 
				// needing to be repainted : qc 5076
				rect.setRect(0,0,getScaledCanvasSize().getWidth(), getScaledCanvasSize().getHeight());
				//rect.setRect(rect.getX()-2, rect.getY()-2, rect.getWidth()+4, rect.getHeight()+4);
				
				if(oldArea!=null) {

					oldArea.add(new Area(rect));
					
				}else{
					
					oldArea=new Area(rect);
				}
				
				if(oldArea!=null) {
					
					changedAreas.put(gnode, oldArea);
				}
			}
		}
		
		/**
		 * @return the dirty area
		 */
		public Area getDirtyArea() {
			
			Area dirtyArea=null;
			Area initialArea=null, changedArea=null, currentArea=null;
			
			//computing the dirty area
			for(GraphicsNode gnode : initialAreas.keySet()) {
				
				initialArea=initialAreas.get(gnode);
				changedArea=changedAreas.get(gnode);
				
				currentArea=(changedArea==null)?initialArea:changedArea;
				
				if(dirtyArea==null) {
					
					dirtyArea=currentArea;
					
				}else {
					
					dirtyArea.add(currentArea);
				}
			}
			
			//clearing the maps
			initialAreas.clear();
			changedAreas.clear();

			return dirtyArea;
		}
	}
    }
