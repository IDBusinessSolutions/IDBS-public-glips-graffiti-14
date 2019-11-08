/*
 * Created on 21 janv. 2005
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
package fr.itris.glips.svgeditor.visualresources;

import java.awt.geom.*;
import java.util.*;

import org.apache.batik.dom.svg.*;
import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;

/**
 * the class providing methods to handle the resources
 * @author Jordi SUC
 */
public class SVGVisualResourceToolkit {

    /**
     * the visual resources module
     */
    private SVGVisualResources visualResources=null;
    
    /**
     * the constructor of the class
     * @param visualResources the visual resource module
     */
    public SVGVisualResourceToolkit(SVGVisualResources visualResources){
        
        this.visualResources=visualResources;
    }

    /**
     * @return Returns the visualResources.
     */
    protected SVGVisualResources getVisualResources() {
        return visualResources;
    }
    
    /**
     * creates a resource node given its name
     * @param frame the current frame
     * @param parentNode the parentNode
     * @param nodeName the name of the node of the accurate resource
     * @param idShapeToBeAppended the id of the shape to be appended as a child of the resource
     * @return the element corresponding to the given name of the resource
     */
    protected Element createVisualResource(SVGFrame frame, Element parentNode, String nodeName, String idShapeToBeAppended){
        
        Element resource=null;
        ResourceBundle bundle=SVGEditor.getBundle();

        if(frame!=null && parentNode!=null && nodeName!=null && ! nodeName.equals("")){ //$NON-NLS-1$

            Element childElement=null;
            
            if(idShapeToBeAppended!=null && ! idShapeToBeAppended.equals("")){ //$NON-NLS-1$
                
                Element elt=null;
                Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
                
                if(doc!=null){
                    
                    elt=doc.getElementById(idShapeToBeAppended);
                    
                    if(elt!=null){
                        
                        childElement=(Element)doc.importNode(elt, true);
                        childElement.setAttribute("id", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        
                        //setting a transform value to put the upper left corner of the document
                        SVGTransformMatrix matrix=getVisualResources().getSVGEditor().getSVGToolkit().getTransformMatrix(childElement);
                        
                        //computing the translation values and adding them
                        if(matrix!=null){
                            
                            double e=0, f=0;
                            Rectangle2D bounds=frame.getNodeBounds(elt);
                            
                            if(bounds!=null){
                                
                                e=-bounds.getX();
                                f=-bounds.getY();
                            }
                            
                            matrix.concatenateTranslate(e, f);
                            getVisualResources().getSVGEditor().getSVGToolkit().setTransformMatrix(childElement, matrix);
                        }
                    }
                }
            }
            
            //creates the resource node
    		String svgNS=parentNode.getOwnerDocument().getDocumentElement().getNamespaceURI();
            resource=parentNode.getOwnerDocument().createElementNS(svgNS, nodeName);
            
            if(nodeName.equals("linearGradient")){ //$NON-NLS-1$
                
                resource.setAttributeNS(null, "x1", "0%"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "y1", "0%"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "x2", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "y2", "0%"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "spreadMethod", "pad"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "gradientTransform", ""); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "gradientUnits", "objectBoundingBox"); //$NON-NLS-1$ //$NON-NLS-2$
                
                Element child=parentNode.getOwnerDocument().createElementNS(svgNS, "stop"); //$NON-NLS-1$
                child.setAttributeNS(null, "offset", "0%"); //$NON-NLS-1$ //$NON-NLS-2$
                child.setAttributeNS(null, "style", "stop-color:#000000;stop-opacity:1.0;"); //$NON-NLS-1$ //$NON-NLS-2$
                
                resource.appendChild(child);
                
            }else if(nodeName.equals("radialGradient")){ //$NON-NLS-1$
                
                resource.setAttributeNS(null, "cx", "50%"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "cy", "50%"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "r", "50%"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "fx", "50%"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "fy", "50%"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "spreadMethod", "pad"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "gradientTransform", ""); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "gradientUnits", "objectBoundingBox"); //$NON-NLS-1$ //$NON-NLS-2$
                
                Element child=parentNode.getOwnerDocument().createElementNS(svgNS, "stop"); //$NON-NLS-1$
                child.setAttributeNS(null, "offset", "0%"); //$NON-NLS-1$ //$NON-NLS-2$
                child.setAttributeNS(null, "style", "stop-color:#000000;stop-opacity:1.0;"); //$NON-NLS-1$ //$NON-NLS-2$
                
                resource.appendChild(child);
                
            }else if(nodeName.equals("pattern")){ //$NON-NLS-1$
                
                resource.setAttributeNS(null, "x", "0%"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "y", "0%"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "height", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "patternUnits", "objectBoundingBox"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "patternContentUnits", "userSpaceOnUse"); //$NON-NLS-1$ //$NON-NLS-2$
                
            }else if(nodeName.equals("marker")){ //$NON-NLS-1$
                
                resource.setAttributeNS(null, "refX", "0"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "refY", "0"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "markerWidth", "0"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "markerHeight", "0"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "markerUnits", "strokeWidth"); //$NON-NLS-1$ //$NON-NLS-2$
                resource.setAttributeNS(null, "orient", "auto"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            //appends the node to the parent node//

            //getting the base string for the id
            String baseId=resource.getNodeName();

            if(bundle!=null){
                
                try{
                    baseId="vresourcename_".concat(baseId); //$NON-NLS-1$
                    baseId=bundle.getString(baseId);
                    baseId=baseId.replaceAll("\\s+", ""); //$NON-NLS-1$ //$NON-NLS-2$
                }catch (Exception ex){baseId=resource.getNodeName();}
            }
            
            String id=frame.getId(baseId);
            resource.setAttributeNS(null, "id", id); //$NON-NLS-1$
            
            if(childElement!=null){
                
                //sets the child node, if it exists
                resource.appendChild(childElement);
            }
        }
        
        return resource;
    }
    
    /**
     * returns the duplicate resource of the given resource
     * @param frame a frame
     * @param resourceNode a resource node
     * @return the duplicated node
     */
    protected Element duplicateVisualResource(SVGFrame frame, Element resourceNode){
        
        Element resource=null;
        ResourceBundle bundle=getVisualResources().getBundle();
        
        if(resourceNode!=null){
            
            Element parentNode=(Element)resourceNode.getParentNode();
            
            if(parentNode!=null){
                
                //cloning the node
                resource=(Element)resourceNode.cloneNode(true);

                
                //getting the id of the resource node and creating the id for the cloned node
                String suffix=""; //$NON-NLS-1$
                
                if(bundle!=null){
                    
                    try{
                        suffix=bundle.getString("vresource_duplicatednodesuffix"); //$NON-NLS-1$
                    }catch (Exception ex){}
                }
                
                //creating the new id
                String id=resource.getAttribute("id"); //$NON-NLS-1$
                id=frame.getId(id+suffix);
                
                //setting the id
                resource.setAttribute("id", id); //$NON-NLS-1$
            }
        }
        
        return resource;
    }
    
    /**
     * removes the given resource node
     * @param frame the current frame
     * @param resourceNode the resource node
     */
    protected void removeVisualResource(SVGFrame frame, Element resourceNode){
    
        if(frame!=null && resourceNode!=null){

            Element parentNode=(Element)resourceNode.getParentNode();
            
            if(parentNode!=null){
            	
                //removing the node from its parent
                parentNode.removeChild(resourceNode);
            }
        }
    }
    
    /**
     * appends the givn resource node to the given parent node
     * @param frame a frame 
     * @param parentNode a parent node
     * @param resourceNode a resource node
     */
    protected void appendVisualResource(SVGFrame frame, Element parentNode, Element resourceNode){
        
        if(frame!=null && parentNode!=null && resourceNode!=null){

            String id=resourceNode.getAttribute("id"); //$NON-NLS-1$
            
            if(! frame.checkId(id)){
                
                id=frame.getId(id);
            }

            //removing the node from its parent
            parentNode.appendChild(resourceNode);
        }
    }


    
    /**
     * whether the given node can be removed or not
     * @param frame the current frame
     * @param resourceNode the resource node
     * @return whether the given node can be removed or not
     */
    protected boolean canRemoveVisualResource(SVGFrame frame, Element resourceNode){
        
        if(frame!=null && resourceNode!=null){

            String id=resourceNode.getAttribute("id"); //$NON-NLS-1$
            
            if(! frame.isResourceUsed(id)){
                
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * imports a resource node from the resource store
     * @param frame the current frame
     * @param parentNode the parentNode
     * @param nodeToBeImported the node that should be imported
     * @return the imported node
     */
    protected Element importVisualResource(SVGFrame frame, Element parentNode, Element nodeToBeImported){
        
        Element resourceNode=null;
        Document visualResourceStore=getVisualResources().getVisualResourceStore();
        ResourceBundle bundle=getVisualResources().getBundle();
        
        if(frame!=null && parentNode!=null && visualResourceStore!=null && nodeToBeImported!=null){
  
            final Element defs=getVisualResources().getDefs(frame);

            if(defs!=null){
                
                Document doc=defs.getOwnerDocument();
                resourceNode=(Element)doc.importNode(nodeToBeImported, true);
                
                //getting the base string for the id
                String baseId=resourceNode.getNodeName(), importedLabel=""; //$NON-NLS-1$

                if(bundle!=null){
                    
                    try{
                        importedLabel=bundle.getString("labelimported"); //$NON-NLS-1$
                        baseId="vresourcename_".concat(baseId); //$NON-NLS-1$
                        baseId=bundle.getString(baseId);
                        baseId=importedLabel.concat(baseId);
                        baseId=baseId.replaceAll("\\s+", ""); //$NON-NLS-1$ //$NON-NLS-2$
                    }catch (Exception ex){baseId=resourceNode.getNodeName();}
                }
                
                String id=frame.getId(baseId);
                resourceNode.setAttribute("id", id); //$NON-NLS-1$
            }
        }
        
        return resourceNode;
    }
    
    /**
     * gets the attribute node of the parent node given its name or creates it if it does not exist
     * @param frame the current frame
     * @param parentNode the parent node
     * @param name the name of the attribute
     * @param value the value of the attribute if it is created
     * @return the attribute node
     */
    public Node getVisualResourceAttributeNode(SVGFrame frame, Element parentNode, String name, String value){
        
        Node attNode=null;
        
        if(frame!=null && parentNode!=null && name!=null && ! name.equals("")){ //$NON-NLS-1$

            //gets the attribute node if it exists
            attNode=parentNode.getAttributeNode(name);

            if(attNode==null){
                
                //if the attribute node does not exist, it is created
                if(value==null){
                    
                    value=""; //$NON-NLS-1$
                }
                
                attNode=parentNode.getOwnerDocument().createAttribute(name);
                attNode.setNodeValue(value);

                //appends the element to the resource node
                parentNode.setAttributeNode((Attr)attNode);
            }
        }

        return attNode;
    }
    
    /**
     * creates a child of a resource node given its parent
     * @param frame the current frame
     * @param parentNode the parentNode to which the child will be appended
     * @return the element corresponding to a child of a resource whose name is nodeName
     */
    protected Element createVisualResourceChildStructure(SVGFrame frame, Node parentNode){
        
        Element resourceChild=null;
        
        if(frame!=null && parentNode!=null){
            
            //creates the resource child
            if(parentNode.getNodeName().equals("linearGradient") || parentNode.getNodeName().equals("radialGradient")){ //$NON-NLS-1$ //$NON-NLS-2$

        		String svgNS=SVGDOMImplementation.SVG_NAMESPACE_URI;
                resourceChild=parentNode.getOwnerDocument().createElementNS(svgNS, "stop"); //$NON-NLS-1$
                resourceChild.setAttributeNS(null, "offset", "0%"); //$NON-NLS-1$ //$NON-NLS-2$
                resourceChild.setAttributeNS(null, "style", "stop-color:#000000;stop-opacity:1.0;"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        
        return resourceChild;
    }
    
    /**
     * appends a child of a resource node to its parent
     * @param frame the current frame
     * @param parentNode the parentNode to which the child will be appended
     * @param childNode the child of a resource node
     */
    protected void appendVisualResourceChild(SVGFrame frame, Node parentNode, Node childNode){

        if(frame!=null && parentNode!=null && childNode!=null){
            
            //creates the resource child
            if(parentNode.getNodeName().equals("linearGradient") || parentNode.getNodeName().equals("radialGradient")){ //$NON-NLS-1$ //$NON-NLS-2$

                //appends the element to the resource node
                parentNode.appendChild(childNode);
            }
        }
    }
    
    /**
     * removes the child node from the dom
     * @param frame a frame
     * @param childNode the node to be removed
     */
    protected void removeVisualResourceChild(SVGFrame frame, Node childNode){
        
        if(frame!=null && childNode!=null){
            
            final Node parentNode=childNode.getParentNode();
            final Node fchildNode=childNode;
            
            if(parentNode!=null){
                
                parentNode.removeChild(fchildNode);
            }
        }
    }
}
