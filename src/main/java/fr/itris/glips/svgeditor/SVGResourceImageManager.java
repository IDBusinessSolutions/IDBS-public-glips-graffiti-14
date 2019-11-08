/*
 * Created on 10 déc. 2004
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

package fr.itris.glips.svgeditor;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.plaf.metal.*;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.dom.svg.*;
import org.apache.batik.swing.*;
import org.apache.batik.swing.gvt.*;
import org.apache.batik.swing.svg.*;
import org.w3c.dom.*;
import org.w3c.dom.svg.*;

import fr.itris.glips.svgeditor.canvas.*;

/**
 * the class used to create an outline of a resource
 * 
 * @author Jordi SUC
 */
public class SVGResourceImageManager{
    
    /**
     * used to convert numbers into a string
     */
    private DecimalFormat format;
    
    /**
     * the map associating a frame to a map associating the id of a resource to the imageRepresentation object representing this resource
     */
    private final Map<SVGFrame, Map<String, ImageRepresentation>> frameToIdToImages=
    											Collections.synchronizedMap(new HashMap<SVGFrame, Map<String, ImageRepresentation>>());

    /**
     * the size of each image, and the size of the small images
     */
    private final Dimension imageSize=new Dimension(20, 20), smallImageSize=new Dimension(16, 16);
    
    /**
     * the list containing runnables to execute, create, or update 
     */
    private java.util.List<Runnable> queue=Collections.synchronizedList(new LinkedList<Runnable>());
    
    /**
     * the thread handling the queue
     */
    private Thread queueManager=null;
    
    /**
     * the list of the resource representations that have been added
     */
    private java.util.List<ResourceRepresentation> resourceRepresentationList=
    																							Collections.synchronizedList(new LinkedList<ResourceRepresentation>());
    
    /**
     * the editor
     */
    private SVGEditor editor=null;
    
    /**
     * the constructor of the class
     * @param editor the editor
     */
    public SVGResourceImageManager(SVGEditor editor){
        
        this.editor=editor;

        //the listener to the frame changes
        editor.getFrameManager().addSVGFrameChangedListener(new ActionListener(){

	        public void actionPerformed(ActionEvent evt) {

	            Collection frames=getSVGEditor().getFrameManager().getFrames();
	            
	            //removes the frames that have been closed from the map
	            for(SVGFrame frame : new LinkedList<SVGFrame>(frameToIdToImages.keySet())){

	                if(frame!=null && ! frames.contains(frame)){

	                    frameToIdToImages.remove(frame);
	                }
	            }
	            
	            //removes the image representations that belongs to disposed frames
	            for(ResourceRepresentation rep : new HashSet<ResourceRepresentation>(resourceRepresentationList)) {
	            	
	            	if(rep!=null && ! frames.contains(rep.getFrame())) {
	            		
	            		rep.dispose();
	            		resourceRepresentationList.remove(rep);
	            	}
	            }
	        }
        });

        //sets the format used to convert numbers into a string
        DecimalFormatSymbols symbols=new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        format=new DecimalFormat("######.#",symbols); //$NON-NLS-1$
        
        //the queue manager
        queueManager=new Thread(){

        	@Override
            public void run() {

                Runnable runnable=null;
                
                while(true){
                    
                	while(queue.size()>0){

                        //getting the runnable
                        runnable=queue.get(0);
                        
                        //removing it from the queue
                        queue.remove(runnable);
                        
                        //running the runnable
                        runnable.run();
                	}

					try{
						sleep(100);
					}catch (Exception ex){}
                }
            }
        };
        
        queueManager.start();
    }
    
    /**
     * returns a representation of the resource given by its id
     * @param frame a frame
     * @param resourceId the id of a resource node
     * @param useSmallImage whether the returned representation should be small or large
     * @return a resource representation
     */
    public ResourceRepresentation getResourceRepresentation(SVGFrame frame, String resourceId, boolean useSmallImage){

        final SVGFrame fframe=frame;
        final String fresourceId=resourceId;
        
        if(frame!=null && resourceId!=null){
            
            //creating the runnable
            Runnable runnable=new Runnable(){

	            public void run() {

	                ImageRepresentation imageRepresentation=getResourceImage(fframe, fresourceId);

	                if(imageRepresentation==null){
	                    
	                    createNewImage(fframe, fresourceId);
	                }
	            } 
            };
            
            //enqueueing the runnable
            invokeLater(runnable);
        }
        
        return getRepresentation(frame, resourceId, useSmallImage);
    }
    
    /**
     * enqueues the given runnable
     * @param runnable a runnable
     */
    protected void invokeLater(Runnable runnable){

    	if(runnable!=null){
    		
    	    queue.add(runnable);
    	}
    }
    
    /**
     * @return Returns the editor.
     */
    protected SVGEditor getSVGEditor() {
        return editor;
    }
    
    /**
     * invalidates the representation of a resource
     * @param frame a frame
     * @param resourceId the id of a resource node
     */
    public void invalidateResourceRepresentation(SVGFrame frame, String resourceId){
    	
    	if(resourceId!=null && frame!=null){

            Map<String, ImageRepresentation> idToImageMap=null;
            
            if(frameToIdToImages.containsKey(frame)){
                
                idToImageMap=frameToIdToImages.get(frame);
                
                if(idToImageMap!=null){
                	
                	//removes the id in the map
                	synchronized(this){idToImageMap.remove(resourceId);}
                }
            }
    	}
    }
    
    /**
     * checks the consistency of the stored images
     * 
     * WHEN? LL
     * 
     * @param frame
     */
    public void checkConsistency(SVGFrame frame){
        
        if(frame!=null){
            
            final SVGFrame fframe=frame;
            
            Runnable runnable=new Runnable(){

	            public void run() {

	                checkResourceRepresentationsConsistency(fframe);
	            } 
            };
            
            //enqueueing the runnable
            invokeLater(runnable);
        }
    }
    
    /**
     * checks if the map associating an id to an image is consistent
     * @param frame
     */
    protected void checkResourceRepresentationsConsistency(SVGFrame frame){
    	
    	if(frame!=null){

            Map<String, ImageRepresentation> idToImageMap=null;
            
            if(frameToIdToImages.containsKey(frame)){
                
                idToImageMap=frameToIdToImages.get(frame);
                
                if(idToImageMap!=null){
                	
				    //getting the map associating the id of a resource to the resource node
				    LinkedList<String> resourceNames=new LinkedList<String>();
			        resourceNames.add("linearGradient"); //$NON-NLS-1$
			        resourceNames.add("radialGradient"); //$NON-NLS-1$
			        resourceNames.add("pattern"); //$NON-NLS-1$
			        resourceNames.add("marker"); //$NON-NLS-1$

			        
					Map resources=frame.getResourcesFromDefs(	frame.getScrollPane().getSVGCanvas().getDocument(), 
																								resourceNames);

					for(String id : new LinkedList<String>(idToImageMap.keySet())){
						
						if(id!=null && ! id.equals("") && ! resources.containsKey(id)){ //$NON-NLS-1$
							
							idToImageMap.remove(id);
						}
					}
                }
            }
    	}
    }
    
    /**
     * creates a new image
     * @param frame a frame 
     * @param resourceId the id of the resource from which the image will be created
     */
    protected void createNewImage(SVGFrame frame, String resourceId){
        
        if(frame!=null && resourceId!=null && ! resourceId.equals("")){ //$NON-NLS-1$
            
            Element resourceElement=null;
            
            try{
                resourceElement=frame.getScrollPane().getSVGCanvas().getDocument().getElementById(resourceId);
            }
            catch (Exception ex){resourceElement = null;}
            
            //If not in the svg doc, have a look in the visual resources store
            if (resourceElement == null) {
                try {
                    resourceElement = editor.getResource().getResourceStore().getElementById(resourceId);
                }  
                catch (Exception ex){resourceElement = null;}
            }
            
            final String fresourceId=resourceId;
            
            if(resourceElement!=null){

                final SVGFrame fframe=frame;
                
                //creating the canvas and setting its properties
                final JSVGCanvas canvas=new JSVGCanvas(){

                	@Override
					public void dispose() {

						removeKeyListener(listener);
						removeMouseMotionListener(listener);
						removeMouseListener(listener);
						disableInteractions=true;
						selectableText=false;
						userAgent=null;
						
						bridgeContext.dispose();
						super.dispose();
					}
                };
                
                //the element to be added
                Element elementToAdd=null;
                
                canvas.setDocumentState(JSVGComponent.ALWAYS_STATIC);
                canvas.setDisableInteractions(true);

                //creating the new document
        		final String svgNS=SVGDOMImplementation.SVG_NAMESPACE_URI;
        		final SVGDocument doc=(SVGDocument)resourceElement.getOwnerDocument().cloneNode(false);
        		
        		//creating the root element
        		final Element root=(Element)doc.importNode(resourceElement.getOwnerDocument().getDocumentElement(), false);
        		doc.appendChild(root);

        		//removing all the attributes of the root element
        		NamedNodeMap attributes=doc.getDocumentElement().getAttributes();
        		
        		for(int i=0; i<attributes.getLength(); i++){
        		    
        		    if(attributes.item(i)!=null){
        		        
        		        doc.getDocumentElement().removeAttribute(attributes.item(i).getNodeName());
        		    }
        		}

        		//adding the new attributes for the root
        		root.setAttributeNS(null, "width", imageSize.width+""); //$NON-NLS-1$ //$NON-NLS-2$
                root.setAttributeNS(null, "height", imageSize.height+""); //$NON-NLS-1$ //$NON-NLS-2$
                root.setAttributeNS(null, "viewBox", "0 0 "+imageSize.width+" "+imageSize.height); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         
                //the defs element that will contain the cloned resource node
                final Element defs=(Element)doc.importNode(resourceElement.getParentNode(), true);
                root.appendChild(defs);

                if(	resourceElement.getNodeName().equals("linearGradient") ||  //$NON-NLS-1$
                        resourceElement.getNodeName().equals("radialGradient") ||  //$NON-NLS-1$
                        resourceElement.getNodeName().equals("pattern")){ //$NON-NLS-1$
                    
                    //the rectangle that will be drawn
                    final Element rect=doc.createElementNS(svgNS, "rect"); //$NON-NLS-1$
                    rect.setAttributeNS(null, "x", "0"); //$NON-NLS-1$ //$NON-NLS-2$
                    rect.setAttributeNS(null, "y", "0"); //$NON-NLS-1$ //$NON-NLS-2$
                    rect.setAttributeNS(null, "width", imageSize.width+""); //$NON-NLS-1$ //$NON-NLS-2$
                    rect.setAttributeNS(null, "height", imageSize.height+""); //$NON-NLS-1$ //$NON-NLS-2$

                    elementToAdd=rect;

                    //setting that the rectangle uses the resource
                    String id=resourceElement.getAttribute("id"); //$NON-NLS-1$
                    
                    if(id==null){
                        
                        id=""; //$NON-NLS-1$
                    }
                    
                    rect.setAttributeNS(null, "style", "fill:url(#"+id+");"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

                    //getting the cloned resource node
                    Node cur=null;
                    Element clonedResourceElement=null;
                    String id2=""; //$NON-NLS-1$
                    
                    for(cur=defs.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
                        
                        if(cur instanceof Element){

                            id2=((Element)cur).getAttribute("id"); //$NON-NLS-1$
                            
                            if(id2!=null && id.equals(id2)){
                                
                                clonedResourceElement=(Element)cur;
                            }
                        }
                    }
                    
                    if(clonedResourceElement!=null){
                        
                        //getting the root element of the initial resource element
                        Element initialRoot=resourceElement.getOwnerDocument().getDocumentElement();
                        
                        //getting the width and height of the initial root element
                        double initialWidth=0, initialHeight=0;
                        
                        try{
                            initialWidth=SVGToolkit.getPixelledNumber(initialRoot.getAttributeNS(null, "width")); //$NON-NLS-1$
                            initialHeight=SVGToolkit.getPixelledNumber(initialRoot.getAttributeNS(null, "height")); //$NON-NLS-1$
                        }catch (Exception ex){}

                        if(resourceElement.getNodeName().equals("linearGradient")){ //$NON-NLS-1$
                            
                            if(resourceElement.getAttributeNS(null, "gradientUnits").equals("userSpaceOnUse")){ //$NON-NLS-1$ //$NON-NLS-2$
                                
                                double x1=0, y1=0, x2=0, y2=0;
                                
                                //normalizing the values for the vector to fit the rectangle
                                try{
                                    x1=Double.parseDouble(resourceElement.getAttributeNS(null, "x1")); //$NON-NLS-1$
                                    y1=Double.parseDouble(resourceElement.getAttributeNS(null, "y1")); //$NON-NLS-1$
                                    x2=Double.parseDouble(resourceElement.getAttributeNS(null, "x2")); //$NON-NLS-1$
                                    y2=Double.parseDouble(resourceElement.getAttributeNS(null, "y2")); //$NON-NLS-1$
                                    
                                    x1=x1/initialWidth*imageSize.width;
                                    y1=y1/initialHeight*imageSize.height;
                                    x2=x2/initialWidth*imageSize.width;
                                    y2=y2/initialHeight*imageSize.height;                           
                                }catch (Exception ex){}

                                clonedResourceElement.setAttributeNS(null, "x1", format.format(x1)); //$NON-NLS-1$
                                clonedResourceElement.setAttributeNS(null, "y1", format.format(y1)); //$NON-NLS-1$
                                clonedResourceElement.setAttributeNS(null, "x2", format.format(x2)); //$NON-NLS-1$
                                clonedResourceElement.setAttributeNS(null, "y2", format.format(y2)); //$NON-NLS-1$
                            }
                            
                        }else if(resourceElement.getNodeName().equals("radialGradient")){ //$NON-NLS-1$
                            
                            if(resourceElement.getAttributeNS(null, "gradientUnits").equals("userSpaceOnUse")){ //$NON-NLS-1$ //$NON-NLS-2$
                                
                                double cx=0, cy=0, r=0, fx=0, fy=0;
                                
                                //normalizing the values for the circle to fit the rectangle
                                try{
                                    cx=Double.parseDouble(resourceElement.getAttributeNS(null, "cx")); //$NON-NLS-1$
                                    cy=Double.parseDouble(resourceElement.getAttributeNS(null, "cy")); //$NON-NLS-1$
                                    r=Double.parseDouble(resourceElement.getAttributeNS(null, "r")); //$NON-NLS-1$
                                    fx=Double.parseDouble(resourceElement.getAttributeNS(null, "fx")); //$NON-NLS-1$
                                    fy=Double.parseDouble(resourceElement.getAttributeNS(null, "fy")); //$NON-NLS-1$
                                    
                                    cx=cx/initialWidth*imageSize.width;
                                    cy=cy/initialHeight*imageSize.height;
                                    
                                    r=r/(Math.abs(Math.sqrt(Math.pow(initialWidth, 2)+Math.pow(initialHeight, 2))))*
                                    		Math.abs(Math.sqrt(Math.pow(imageSize.width, 2)+Math.pow(imageSize.width, 2)));
                                    
                                    fx=fx/initialWidth*imageSize.width;
                                    fy=fy/initialHeight*imageSize.height;                           
                                }catch (Exception ex){}

                                clonedResourceElement.setAttributeNS(null, "cx", format.format(cx)); //$NON-NLS-1$
                                clonedResourceElement.setAttributeNS(null, "cy", format.format(cy)); //$NON-NLS-1$
                                clonedResourceElement.setAttributeNS(null, "r", format.format(r)); //$NON-NLS-1$
                                clonedResourceElement.setAttributeNS(null, "fx", format.format(fx)); //$NON-NLS-1$
                                clonedResourceElement.setAttributeNS(null, "fy", format.format(fy)); //$NON-NLS-1$
                            }
                            
                        }else if(resourceElement.getNodeName().equals("pattern")){ //$NON-NLS-1$
                            
                            if(resourceElement.getAttributeNS(null, "patternUnits").equals("userSpaceOnUse")){ //$NON-NLS-1$ //$NON-NLS-2$
                            
                                double x=0, y=0, w=0, h=0;
                                
                                //normalizing the values for the vector to fit the rectangle
                                try{
                                    x=Double.parseDouble(resourceElement.getAttributeNS(null, "x")); //$NON-NLS-1$
                                    y=Double.parseDouble(resourceElement.getAttributeNS(null, "y")); //$NON-NLS-1$
                                    w=Double.parseDouble(resourceElement.getAttributeNS(null, "w")); //$NON-NLS-1$
                                    h=Double.parseDouble(resourceElement.getAttributeNS(null, "h")); //$NON-NLS-1$
                                    
                                    x=x/initialWidth*imageSize.width;
                                    y=y/initialHeight*imageSize.height;
                                    w=w/initialWidth*imageSize.width;
                                    h=h/initialHeight*imageSize.height;                           
                                }catch (Exception ex){}

                                clonedResourceElement.setAttributeNS(null, "x", format.format(x)); //$NON-NLS-1$
                                clonedResourceElement.setAttributeNS(null, "y", format.format(y)); //$NON-NLS-1$
                                clonedResourceElement.setAttributeNS(null, "width", format.format(w)); //$NON-NLS-1$
                                clonedResourceElement.setAttributeNS(null, "height", format.format(h)); //$NON-NLS-1$
                            }
                        }
                    }
                    
                }else if(resourceElement.getNodeName().equals("marker")){ //$NON-NLS-1$
                    
                    //the line that will be drawn
                    final Element line=doc.createElementNS(svgNS, "line"); //$NON-NLS-1$
                    line.setAttributeNS(null, "x1", "0.0"); //$NON-NLS-1$ //$NON-NLS-2$
                    line.setAttributeNS(null, "y1", (((double)imageSize.height)/2)+""); //$NON-NLS-1$ //$NON-NLS-2$
                    line.setAttributeNS(null, "x2", ((double)imageSize.width/2) +""); //$NON-NLS-1$ //$NON-NLS-2$
                    line.setAttributeNS(null, "y2", (((double)imageSize.height)/2)+""); //$NON-NLS-1$ //$NON-NLS-2$
                    
                    //We need to work out where the design of the marker comes relative to the 
                    // reference point* (when going along the line axis, in the direction created).
                    // The possibilities are:
                    // - largely after the ref point 
                    // - largely before it, 
                    // - around it, roughly equally (blobs and squares)
                    // These correspond to the marker-end, marker-start and any node cases, respectively
                    // *the reference point is basically the pin by which the donkey's tail is pinned on!
                    
                    //read width of view box, frame in which visible marker is rendered
                    double viewboxWidth = 100;
                    {
                        String viewBoxAtt = resourceElement.getAttribute("viewBox");                     //$NON-NLS-1$
                        String[] splitViewBox = viewBoxAtt.split("\\s");                     //$NON-NLS-1$
                        String widthString = splitViewBox[2].trim();
                        try{
                            viewboxWidth = Double.parseDouble(widthString);
                        } catch (NumberFormatException e) { System.out.println("Problem reading width - guess value used");}                         //$NON-NLS-1$
                    }
                                        
                    //get location of reference point along x-axis in coord system defined by viewbox attribute 
                    double refX = 0;
                    try{
                        refX = Double.parseDouble(resourceElement.getAttribute("refX")); //$NON-NLS-1$
                    } catch (NumberFormatException e) {  System.out.println("Problem reading ref X - guess value used");} //$NON-NLS-1$
                    
                    //work out how far along the ref point is
                    // a low value means the marker is rendered after the ref point (donkeys head), 
                    // a high value means before  (donkeys tail) (the line is the donkey in this metaphor...)
                    double slideFactor = refX/viewboxWidth;                    
                    boolean isStartMarker = slideFactor > 0.75; // <------
                    boolean isEndMarker = slideFactor < 0.25;   // ------>
                    
                    //if start marker shift the line over to the right, with room to put arrow on the back
                    // we are shoving the line slightly out of shot - only the back 1/2 will appear
                    // This is so we can get big arrows in! :-)
                    if (isStartMarker)                        
                    {
                        line.setAttributeNS(null, "transform", "translate(" +( (3.0/4)*(double)imageSize.width) + ")");                         //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                    else if (isEndMarker) //a shove to the left, see above
                    {
                        line.setAttributeNS(null, "transform", "translate(-" +( (double)imageSize.width)/4 + ")");                                                 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                    
                    elementToAdd=line;
                    
                    //setting that the line uses the resource
                    String id=resourceElement.getAttribute("id"); //$NON-NLS-1$
                    if(id==null)id=""; //$NON-NLS-1$
                    
                    if (isStartMarker)                      
                    {
                        line.setAttributeNS(null, "style", "stroke:#000000;fill:none;marker-start:url(#"+id+");");                         //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                    else //end marker or doesn't matter
                    {
                        line.setAttributeNS(null, "style", "stroke:#000000;fill:none;marker-end:url(#"+id+");"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                }
                
		        root.appendChild(elementToAdd);

                //adding a rendering listener to the canvas
                GVTTreeRendererAdapter gVTTreeRendererAdapter=new GVTTreeRendererAdapter(){

                	@Override
    				public void gvtRenderingCompleted(GVTTreeRendererEvent evt) {

    				    Image bufferedImage=canvas.getOffScreen();
    				    
    				    if(bufferedImage!=null){
    				        
    				        Graphics g=bufferedImage.getGraphics();
    			            Color borderColor=MetalLookAndFeel.getSeparatorForeground();
    			            
    			            g.setColor(borderColor);
    			            g.drawRect(0, 0, imageSize.width-1, imageSize.height-1);
    				    }
    				    
    				    setImage(fframe, fresourceId, bufferedImage);

    				    //refreshing the panels that have been created when no image was available for them
    				    Image image=null;
    				    
    				    for( ResourceRepresentation resourceRepresentation : new LinkedList<ResourceRepresentation>(resourceRepresentationList)){

    				        if(resourceRepresentation!=null){

    				            resourceRepresentation.refreshRepresentation();
    				            image=resourceRepresentation.getImage();
    				            
    				            if(image!=null){
    				                
    				                resourceRepresentationList.remove(resourceRepresentation);
    				            }
    				        }
    				    }
				        
    				    canvas.removeGVTTreeRendererListener(this);
    	        		canvas.stopProcessing();
    				    canvas.dispose();
    				}
                };
                
                canvas.addGVTTreeRendererListener(gVTTreeRendererAdapter);

                //setting the document for the canvas
                canvas.setSVGDocument(doc);
                
                canvas.setBackground(Color.white);
                canvas.setBounds(1, 1, imageSize.width, imageSize.height);
            }
        }
    }
    
    /**
     * Returns the representation of the resource whose id is given
     * @param frame a frame 
     * @param resourceId the id of the resource from which the image has been created
     * @param useSmallImage whether the representation should display a small or a large image
     * @return the representation of the resource whose id is given
     */
    protected ResourceRepresentation getRepresentation(SVGFrame frame, String resourceId, boolean useSmallImage){
    	
        ResourceRepresentation rep=null;
        
        if(frame!=null && resourceId!=null && ! resourceId.equals("")){ //$NON-NLS-1$
            
            rep=new ResourceRepresentation(frame, resourceId, useSmallImage);
        }
 
        return rep;
    }
    
    /**
     * creates a new image
     * @param frame a frame 
     * @param resourceId the id of the resource from which the image has been created
     * @param image the image representing the resource
     */
    protected void setImage(SVGFrame frame, String resourceId, Image image){
        
        if(frame!=null && resourceId!=null && image!=null){
            
        	Map<String, ImageRepresentation> idToImageMap=null;
            
            if(frameToIdToImages.containsKey(frame)){
                
                idToImageMap=frameToIdToImages.get(frame);
                
            }else{
                
                idToImageMap=new Hashtable<String, ImageRepresentation>();
                frameToIdToImages.put(frame, idToImageMap);
            }
            
            synchronized(this){idToImageMap.put(resourceId, new ImageRepresentation(image));}
        }
    }
    
    /**
     * getting the image representing a resource given the id of a resource
     * @param frame a frame 
     * @param resourceId the id of the resource from which the image has been created
     * @return the image representation object representing the resource
     */
    public ImageRepresentation getResourceImage(SVGFrame frame, String resourceId){
        
        ImageRepresentation imageRepresentation=null;
        
        if(frame!=null && resourceId!=null && ! resourceId.equals("")){ //$NON-NLS-1$
            
        	Map<String, ImageRepresentation> idToImageMap=null;
            
            if(frameToIdToImages.containsKey(frame)){
                
                idToImageMap=frameToIdToImages.get(frame);
                
                if(idToImageMap.containsKey(resourceId)){
                    
                	imageRepresentation=idToImageMap.get(resourceId);
                }
            }
        }
        
        return imageRepresentation;
    }
    
    /**
     * getting the image representing a resource given the id of a resource
     * @param frame a frame 
     * @param resourceId the id of the resource from which the image has been created
     * @param useSmallImage whether the returned image should be small or large
     * @return the image representing the resource
     */
    public Image getImage(SVGFrame frame, String resourceId, boolean useSmallImage){
        
        Image image=null;
        
        if(frame!=null && resourceId!=null && ! resourceId.equals("")){ //$NON-NLS-1$
            
        	Map<String, ImageRepresentation> idToImageMap=null;
            
            if(frameToIdToImages.containsKey(frame)){
                
                idToImageMap=frameToIdToImages.get(frame);
                
                if(idToImageMap.containsKey(resourceId)){
                    
                	ImageRepresentation imageRepresentation=idToImageMap.get(resourceId);
                	
                	if(imageRepresentation!=null){
                		
                		if(useSmallImage){
                			
                			image=imageRepresentation.getSmallImage();
                			
                		}else{
                			
                			image=imageRepresentation.getLargeImage();
                		}
                	}
                }
            }
        }
        
        return image;
    }
    
    /**
     * the class of the panel displaying a representation of a resource
     * 
     * @author Jordi SUC
     */
    public class ResourceRepresentation extends JPanel{
    	
    	/**
    	 * a frame
    	 */
    	private SVGFrame frame;
    	
    	/**
    	 * the id of a resource
    	 */
    	private String resourceId=""; //$NON-NLS-1$
    	
    	/**
    	 * the image of the representation of the resource
    	 */
    	public Image resourceImage=null;
    	
    	/**
    	 * whether this resource representation should display a large or a small image
    	 */
    	private boolean useSmallImage=false;
    	
    	/**
    	 * the constructor of the class
    	 * @param frame a frame
    	 * @param resourceId the id of a resource
    	 * @param useSmallImage whether this resource representation should display a large or a small image
    	 */
    	protected ResourceRepresentation(SVGFrame frame, String resourceId, boolean useSmallImage){
    		
    		this.frame=frame;
    		this.resourceId=resourceId;
    		this.useSmallImage=useSmallImage;

            setBackground(Color.white);
            setLayout(null);
            
            if(useSmallImage){
            	
                setPreferredSize(new Dimension(smallImageSize.width+2, smallImageSize.height+2));
            	
            }else{
            	
                setPreferredSize(new Dimension(imageSize.width+2, imageSize.height+2));
            }
            
            ImageRepresentation imageRepresentation=getResourceImage(frame, resourceId);
            Image image=null;
            
            if(imageRepresentation!=null){
            	
                if(useSmallImage){

                    image=new ImageIcon(imageRepresentation.getSmallImage()).getImage();
                	
                }else{

                    image=new ImageIcon(imageRepresentation.getLargeImage()).getImage();
                }
            }

            if(image==null){

                resourceRepresentationList.add(this);
                
            }else{
                
                synchronized(this){resourceImage=image;}
            }
    	}
    	
    	/**
    	 * refreshes the representation
    	 */
    	protected void refreshRepresentation(){

    		ImageRepresentation imageRepresentation=getResourceImage(frame, resourceId);
    		
    		if(imageRepresentation!=null){
    		    
        		if(useSmallImage && imageRepresentation.getSmallImage()!=null){
        			
        			synchronized(this){resourceImage=new ImageIcon(imageRepresentation.getSmallImage()).getImage();}
        			
        		}else if(imageRepresentation.getLargeImage()!=null){
        			
        			synchronized(this){resourceImage=new ImageIcon(imageRepresentation.getLargeImage()).getImage();}
        		}

        		if(resourceImage!=null){ 			

    				refreshParents();
        		}
    		}
    	}

        /**
         * @return Returns the resourceImage.
         */
        public Image getImage() {
            return resourceImage;
        }
        
        /**
         * disposes this image representation
         */
        public void dispose(){
        	
        	frame=null;
        	resourceImage=null;
        }
        
        /**
		 * @return the frame
		 */
		public SVGFrame getFrame() {
			return frame;
		}
        
    	/**
    	 * refreshes the parents
    	 */
    	protected void refreshParents(){
    		
		    if(getParent()!=null){
		        
		        if(getParent().getParent()!=null){
		            
		            if(getParent().getParent().getParent()!=null){
		                
			            if(getParent().getParent().getParent().getParent()!=null){
			                
					        getParent().getParent().getParent().repaint();
					        
			            }else{
			                
					        getParent().getParent().getParent().repaint();
			            }

		            }else{
		                
		                getParent().getParent().repaint();
		            }
		            
		        }else{
		          
		            getParent().repaint();
		        }
		    }
    	}
    	
    	@Override
		protected void paintComponent(Graphics g) {

			super.paintComponent(g);

			if(resourceImage!=null){
				
                g.drawImage(resourceImage, 1, 1, this);
			}
		}
    }
    
    /**
     * the class containing the images representing
     * 
     * @author Jordi SUC
     */
    protected class ImageRepresentation{
    	
    	/**
    	 * the large image
    	 */
    	private Image largeImage=null;
    	
    	/**
    	 * the small image
    	 */
    	private Image smallImage=null;
    	
    	/**
    	 * the constructor of the class
    	 * @param image an image
    	 */
    	protected ImageRepresentation(Image image){
    		
    		this.largeImage=image;
    		this.smallImage=image.getScaledInstance(smallImageSize.width, smallImageSize.height, Image.SCALE_SMOOTH);
    	}

		/**
		 * @return Returns the largeImage.
		 */
		protected Image getLargeImage() {
			return largeImage;
		}
		
		/**
		 * @return Returns the smallImage.
		 */
		protected Image getSmallImage() {
			return smallImage;
		}
    }
}
