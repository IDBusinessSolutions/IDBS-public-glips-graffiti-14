/*
 * Created on 21 juin 2004
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
package fr.itris.glips.svgeditor;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.plaf.metal.*;

import org.apache.batik.ext.awt.geom.*;
import org.w3c.dom.*;
import org.w3c.dom.svg.*;

import fr.itris.glips.svgeditor.canvas.*;

/**
 * a class providing utility methods 
 * @author Jordi SUC, Maciej Wojtkiewicz
 */
public class SVGToolkit {
    
    /**
     * the xml  file extension
     */
    public static final String XML_FILE_EXTENSION=".xml"; //$NON-NLS-1$
    
    /**
     * the svg  file extension
     */
    public static final String SVG_FILE_EXTENSION=".svg"; //$NON-NLS-1$
    
    /**
     * the svgz  file extension
     */
    public static final String SVGZ_FILE_EXTENSION=".svgz"; //$NON-NLS-1$
    
    /**
     * the editor
     */
    private SVGEditor editor;
    
    /**
     * the name space for declaring namespaces
     */
    public static final String xmlnsNS="http://www.w3.org/2000/xmlns/"; //$NON-NLS-1$
    
    /**
     * the xlink attribute namespace name
     */
    public static final String xmlnsXLinkAttributeName="xmlns:xlink"; //$NON-NLS-1$
    
    /**
     * the xlink prefix
     */
    public static final String xLinkprefix="xlink:"; //$NON-NLS-1$
    
    /**
     * the xlink namespace
     */
    public static final String xmlnsXLinkNS="http://www.w3.org/1999/xlink"; //$NON-NLS-1$
    
    /**
     * the namespace of the rtda animations
     */
    public static final String rtdaNameSpace="http://www.itris.fr/2003/animation"; //$NON-NLS-1$
    
    /**
     * the prefix for the rtda animations
     */
    public static final String rtdaPrefix="rtda:"; //$NON-NLS-1$
    
    /**
     * the jwidget tag name
     */
    public static final String jwidgetTagName="rtda:jwidget"; //$NON-NLS-1$
    
    /**
     * the decimal formatter
     */
    public static DecimalFormat format=null;
    
    /**
     * the map associating the name of a svg element shape to its label
     */
    protected static HashMap<String, String> svgElementLabels=new HashMap<String, String>();
    
    /**
     * the label for an unknow shape
     */
    public static String unknownShapeLabel=""; //$NON-NLS-1$
    
    static {
        
        //the object used to convert double values into strings
        DecimalFormatSymbols symbols=new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        format=new DecimalFormat("######.#", symbols); //$NON-NLS-1$
        
		svgElementLabels.put("g", SVGEditor.getBundle().getString("svgElementName_g")); //$NON-NLS-1$ //$NON-NLS-2$
		svgElementLabels.put("circle", SVGEditor.getBundle().getString("svgElementName_circle")); //$NON-NLS-1$ //$NON-NLS-2$
		svgElementLabels.put("ellipse", SVGEditor.getBundle().getString("svgElementName_ellipse")); //$NON-NLS-1$ //$NON-NLS-2$
		svgElementLabels.put("image", SVGEditor.getBundle().getString("svgElementName_image")); //$NON-NLS-1$ //$NON-NLS-2$
		svgElementLabels.put("line", SVGEditor.getBundle().getString("svgElementName_line")); //$NON-NLS-1$ //$NON-NLS-2$
		svgElementLabels.put("path", SVGEditor.getBundle().getString("svgElementName_path")); //$NON-NLS-1$ //$NON-NLS-2$
		svgElementLabels.put("polygon", SVGEditor.getBundle().getString("svgElementName_polygon")); //$NON-NLS-1$ //$NON-NLS-2$
		svgElementLabels.put("polyline", SVGEditor.getBundle().getString("svgElementName_polyline")); //$NON-NLS-1$ //$NON-NLS-2$
		svgElementLabels.put("rect", SVGEditor.getBundle().getString("svgElementName_rect")); //$NON-NLS-1$ //$NON-NLS-2$
		svgElementLabels.put("text", SVGEditor.getBundle().getString("svgElementName_text")); //$NON-NLS-1$ //$NON-NLS-2$
		
		unknownShapeLabel=SVGEditor.getBundle().getString("svgElementName_unknown"); //$NON-NLS-1$
    }
    
    /**
     * the constructror of the class
     * @param editor the editor
     */
    public SVGToolkit(SVGEditor editor){
        
        this.editor=editor;
    }

    /**
     * checks if the xlink namespace is defined in the given document
     * @param doc a document
     */
    public static void checkXLinkNameSpace(Document doc){
        
        if(doc!=null && ! doc.getDocumentElement().hasAttributeNS(xmlnsNS, xmlnsXLinkAttributeName)){

            doc.getDocumentElement().setAttributeNS(xmlnsNS, xmlnsXLinkAttributeName, xmlnsXLinkNS);
        }
    }
    
    /**
     * checks if the given document contains the rtda name space, if not, the namespace is added
     * @param doc a svg document
     */
    public static void checkRtdaXmlns(Document doc){
        
        if(SVGEditor.isRtdaAnimationsVersion && doc!=null){
            
            Element svgRoot=doc.getDocumentElement();
            
            if(! svgRoot.hasAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:rtda")){ //$NON-NLS-1$ //$NON-NLS-2$
                
                svgRoot.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:rtda", rtdaNameSpace); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }
    
    /**
     * checks if the given document contains the given name space, if not, the namespace is added
     * @param doc a svg document
     * @param prefix the name space prefix
     * @param nameSpace a name space
     */
    public static void checkXmlns(Document doc, String prefix, String nameSpace){
        
        if(doc!=null && prefix!=null && nameSpace!=null){
            
            Element svgRoot=doc.getDocumentElement();
            
            if(! svgRoot.hasAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:"+prefix)){ //$NON-NLS-1$ //$NON-NLS-2$
                
                svgRoot.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:"+prefix, nameSpace); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }
    
    /**
     * computes and returns a split path corresponding to the given path
     * @param path
     * @return a split path corresponding to the given path
     */
    public String[] getSplitPath(String path){
    	
    	String[] splitPath=null;
    	
    	if(path!=null && ! path.equals("")){ //$NON-NLS-1$
    		
    		String[] splitPath2=path.split("/"); //$NON-NLS-1$
    		LinkedList<String> list=new LinkedList<String>();
            int i;
    		
    		for(i=0; i<splitPath2.length; i++){
    			
    			if(splitPath2[i]!=null && ! splitPath.equals("")){ //$NON-NLS-1$
    				
    				list.add(splitPath2[i]);
    			}
    		}
    		
    		splitPath=new String[list.size()];
            i=0;
    		
    		for(String str : list){
    			
    			splitPath[i]=str;
                i++;
    		}
    	}
    	
    	return splitPath;
    }
    
    /**
     * @return creates and returns a combo box enabling to choose the units
     */
    public JComboBox getUnitsComboBoxChooser(){

    	//creating the items
    	String[] items=new String[]{"px", "pt", "pc", "mm", "cm", "in"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    	
    	JComboBox combo=new JComboBox(items);
    	combo.setSelectedIndex(0);
    	
    	return combo;
    }
    
    /**
     * computes the number corresponding to this string in pixel
     * @param str
     * @return the number corresponding to this string in pixel
     */
    public static double getPixelledNumber(String str){
        
        double i=0;
        
        if(str!=null && ! str.equals("")){ //$NON-NLS-1$
            
            str=str.trim();
            
            if(! Character.isDigit(str.charAt(str.length()-1))){
                
                String unit=str.substring(str.length()-2, str.length());
                String nb=str.substring(0, str.length()-2);
                
                try{
                    i=Double.parseDouble(nb);
                }catch (Exception ex){}
                
                if(unit.equals("pt")){ //$NON-NLS-1$
                    
                    i=i*1.25;
                    
                }else if(unit.equals("pc")){ //$NON-NLS-1$
                    
                    i=i*15;
                    
                }else if(unit.equals("mm")){ //$NON-NLS-1$
                    
                    i=i*3.543307;
                    
                }else if(unit.equals("cm")){ //$NON-NLS-1$
                    
                    i=i*35.43307;
                    
                }else if(unit.equals("in")){ //$NON-NLS-1$
                    
                    i=i*90;
                }
                
            }else{
                
                try{
                    i=Double.parseDouble(str);
                }catch (Exception ex){}
            }
        }
        
        return i;
    }
    
    /**
     * converts the given pixelled value into the given units value
     * @param value the pixelled value
     * @param unit the new unit
     * @return the value in the given units
     */
    public static double convertFromPixelToUnit(double value, String unit){
    	
       double i=value;
        
        if(unit!=null && ! unit.equals("")){ //$NON-NLS-1$
            
        	unit=unit.trim();
            
            if(unit.equals("pt")){ //$NON-NLS-1$
                
                i=value/1.25;
                
            }else if(unit.equals("pc")){ //$NON-NLS-1$
                
                i=value/15;
                
            }else if(unit.equals("mm")){ //$NON-NLS-1$
                
                i=value/3.543307;
                
            }else if(unit.equals("cm")){ //$NON-NLS-1$
                
                i=value/35.43307;
                
            }else if(unit.equals("in")){ //$NON-NLS-1$
                
                i=value/90;
            }
        }
        
        return i;
    }

    /**
     * normalizes the given node
     * @param node a node
     */
    public void normalizeNode(Node node){
        
        //removeTspans(node);
        transformToMatrix(node);
        translateFromAttributesToStyle(node);
    }
    
    /**
     * removes the attributes specified in the properties.xml and adds them to the style attribute for this node
     * @param node a node
     */
    protected void translateFromAttributesToStyle(Node node){
        
        if(node!=null && node instanceof Element && !(node instanceof SVGFontFaceElement)){
            
        	Element element=(Element)node;
            String style=element.getAttribute("style"); //$NON-NLS-1$
            
            if(style==null){
                
                style=""; //$NON-NLS-1$
            }
            
            //the list of the attributes to be removed and added to the style attribute
            HashSet<String> styleProperties=editor.getResource().getAttributesToTranslate();
            styleProperties.add("stop-opacity"); //$NON-NLS-1$
            styleProperties.add("stop-color"); //$NON-NLS-1$
            
            String name="", value=""; //$NON-NLS-1$ //$NON-NLS-2$
            
            NamedNodeMap attributes=node.getAttributes();
            LinkedList<String> attToBeRemoved=new LinkedList<String>();
            Node att=null;
            
            for(int i=0;i<attributes.getLength();i++){
                
                att=attributes.item(i);
                
                if(att.getNodeName()!=null && styleProperties.contains(att.getNodeName())){
                    
                    name=att.getNodeName();
                    value=att.getNodeValue();
                    
                    if(value!=null && ! value.equals("") && style.indexOf(name+":")==-1){ //$NON-NLS-1$ //$NON-NLS-2$
                        
                        //if the attribute is not in the style value, it is added to the style value
                        if(style.length()>0 && ! style.endsWith(";")){ //$NON-NLS-1$
                            
                            style=style.concat(";"); //$NON-NLS-1$
                        }
                        
                        style=style+name+":"+value+";";		 //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    
                    attToBeRemoved.add(new String(name));
                }
            }
            
            //removes the attributes that have been added to the style attribute
            String str=""; //$NON-NLS-1$
            
            for(Iterator it=attToBeRemoved.iterator(); it.hasNext();){
                
                try{str=(String)it.next();}catch (Exception ex){str=null;}
                
                if(str!=null && !str.equals("")){ //$NON-NLS-1$
                    
                	element.removeAttribute(str);
                }
            }
            
            if(style.equals("")){ //$NON-NLS-1$
                
                //removes the style attribute
            	element.removeAttribute("style"); //$NON-NLS-1$
                
            }else{
                
                //modifies the style attribute
            	element.setAttribute("style", style); //$NON-NLS-1$
            }
        }
    }
    
    /**
     * removes the tspans inside a text node
     * @param node the node on which the changes will be made
     */
    protected void removeTspans(Node node){
        
        if(node!=null && node.getNodeName().equals("text")){ //$NON-NLS-1$
            
            String value=getText(node);
            
            if(value==null){
                
                value=""; //$NON-NLS-1$
            }
            
            //removes all the text children from the node
            NodeList children=node.getChildNodes();
            
            for(int i=0; i<children.getLength(); i++){
                
                if(		children.item(i)!=null && 
                        (children.item(i) instanceof Text || children.item(i).getNodeName().equals("tspan"))){ //$NON-NLS-1$
                    
                    node.removeChild(children.item(i));
                }
            }
            
            children=node.getChildNodes();
            
            for(int i=0; i<children.getLength(); i++){
                
                if(		children.item(i)!=null && 
                        (children.item(i) instanceof Text || children.item(i).getNodeName().equals("tspan"))){ //$NON-NLS-1$
                    
                    node.removeChild(children.item(i));
                }
            }
            
            //adds a #text node
            Document doc=node.getOwnerDocument();
            
            if(doc!=null){
                
                Text txt=doc.createTextNode(value);
                node.appendChild(txt);
            }
        }
    }
    
    /**
     * normalizes a group element, setting the transform of this node to identity 
     * and modifying the matrix transform of its children
     * @param g a group element
     */
    public void normalizeGroupNode(Element g){
        
        if(g!=null && g.getNodeName().equals("g")){ //$NON-NLS-1$
            
        	boolean canNormalize=true;
        	
        	if(getElementChildCount(g)==1 && getElementFirstChild(g).getNodeName().equals("svg")){ //$NON-NLS-1$
        		
        		canNormalize=false;
        	}
        	
            SVGTransformMatrix gMatrix=getTransformMatrix(g), matrix=null;
            
            if(canNormalize && ! gMatrix.isIdentity()){
                
                for(Node cur=g.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
                    
                    if(cur instanceof Element){
                        
                        //getting, modifying and setting the matrix of this node
                        matrix=getTransformMatrix(cur);
                        matrix.concatenateMatrix(gMatrix);
                        
                        setTransformMatrix(cur, matrix);
                    }
                }
                
                //setting the matrix of the g element to identity
                g.removeAttribute("transform"); //$NON-NLS-1$
            }
        }
    }
    
    /**
     * gets the text written in a text node
     * @param node the text node
     * @return a string of the text value
     */
    protected String getText(Node node){
        
        String value=""; //$NON-NLS-1$
        
        if(node!=null && (node.getNodeName().equals("text") || node.getNodeName().equals("tspan"))){ //$NON-NLS-1$ //$NON-NLS-2$
            
            //for each child of the given node, computes the text it contains and concatenates it to the current value
            for(Node cur=node.getFirstChild();cur!=null;cur=cur.getNextSibling()){
                
                if(cur.getNodeName().equals("#text")){ //$NON-NLS-1$
                    
                    value=value+cur.getNodeValue();
                    
                }else if(cur.getNodeName().equals("tspan")){ //$NON-NLS-1$
                    
                    value=value+getText(cur);
                }
            }
        }
        
        value=normalizeTextNodeValue(value);
        
        return value;
    }
    
    
    /**
     * modifies the string to removes the extra whitespaces
     * @param value the string to be modified
     * @return the modified string
     */
    public String cleanTransformString(String value){
        
        String val=new String(value);
        
        val=val.replaceAll("0\\s","0,"); //$NON-NLS-1$ //$NON-NLS-2$
        val=val.replaceAll("1\\s","1,"); //$NON-NLS-1$ //$NON-NLS-2$
        val=val.replaceAll("2\\s","2,"); //$NON-NLS-1$ //$NON-NLS-2$
        val=val.replaceAll("3\\s","3,"); //$NON-NLS-1$ //$NON-NLS-2$
        val=val.replaceAll("4\\s","4,");  //$NON-NLS-1$//$NON-NLS-2$
        val=val.replaceAll("5\\s","5,"); //$NON-NLS-1$ //$NON-NLS-2$
        val=val.replaceAll("6\\s","6,"); //$NON-NLS-1$ //$NON-NLS-2$
        val=val.replaceAll("7\\s","7,"); //$NON-NLS-1$ //$NON-NLS-2$
        val=val.replaceAll("8\\s","8,"); //$NON-NLS-1$ //$NON-NLS-2$
        val=val.replaceAll("9\\s","9,"); //$NON-NLS-1$ //$NON-NLS-2$
        val=val.replaceAll("\\s*[,]\\s*[,]\\s*",","); //$NON-NLS-1$ //$NON-NLS-2$
        val=val.replaceAll("\\s+",""); //$NON-NLS-1$ //$NON-NLS-2$
        
        return val;
    }
    
    /**
     * normalizes the value of a text node
     * @param value
     * @return a normalized string
     */
    public String normalizeTextNodeValue(String value){
        
        String textValue=""; //$NON-NLS-1$
        
        if(value!=null && !value.equals("")){ //$NON-NLS-1$
            
            textValue=new String(value);
            textValue=textValue.replaceAll("\\t+", " "); //$NON-NLS-1$ //$NON-NLS-2$
            textValue=textValue.replaceAll("\\s+", " "); //$NON-NLS-1$ //$NON-NLS-2$
            textValue.trim();

            if(! textValue.equals("") && textValue.charAt(0)==' '){ //$NON-NLS-1$
                
                textValue=textValue.substring(1, textValue.length());
            }
            
            if(! textValue.equals("") && textValue.charAt(textValue.length()-1)==' '){ //$NON-NLS-1$
                
                textValue=textValue.substring(0, textValue.length()-1);
            }
        }
        
        return textValue;
    }
    
    /**
     * returns the previous element sibling of the given element
     * @param element an element
     * @return the previous element sibling of the given element
     */
    public Element getPreviousElementSibling(Element element){
    	
    	Element previousSibling=null;
    	
    	if(element!=null){
    		
        	Node cur=null;
        	
        	for(cur=element.getPreviousSibling(); cur!=null; cur=cur.getPreviousSibling()){
        		
        		if(cur instanceof Element){
        			
        			previousSibling=(Element)cur;
        			break;
        		}
        	}
    	}

    	return previousSibling;
    }
    
    /**
     * returns the next element sibling of the given element
     * @param element an element
     * @return the next element sibling of the given element
     */
    public Element getNextElementSibling(Element element){
    	
    	Element nextSibling=null;
    	
    	if(element!=null){
    		
        	Node cur=null;
        	
        	for(cur=element.getNextSibling(); cur!=null; cur=cur.getNextSibling()){
        		
        		if(cur instanceof Element){
        			
        			nextSibling=(Element)cur;
        			break;
        		}
        	}
    	}

    	return nextSibling;
    }
    
    /**
     * returns the number of elements that are children of the given node
     * @param element an element
     * @return the number of elements that are children of the given node
     */
    public int getElementChildCount(Element element){
    	
    	int count=0;
    	
    	if(element!=null){
    		
        	Node cur=null;

        	for(cur=element.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
        		
        		if(cur instanceof Element){
        			
        			count++;
        		}
        	}
    	}

    	return count;
    }
    
    /**
     * returns the first child element that is an element
     * @param element an element
     * @return the first child element that is an element
     */
    public Element getElementFirstChild(Element element){
    	
    	Element firstElement=null;
    	
    	if(element!=null){
    		
        	Node cur=null;

        	for(cur=element.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
        		
        		if(cur instanceof Element){
        			
        			firstElement=(Element)cur;
        		}
        	}
    	}

    	return firstElement;
    }
    
    /**
     * converts the given transform string into a transform matrix
     * @param value the input string
     * @return a transform matrix
     */
    public SVGTransformMatrix transformToMatrix(String value){
        
        //creates the matrix that will replace the transforms
        SVGTransformMatrix matrix=new SVGTransformMatrix(1,0,0,1,0,0);
        
        if(value!=null && ! value.equals("")){ //$NON-NLS-1$
            
            String transf="", tvalues=""; //$NON-NLS-1$ //$NON-NLS-2$
            
            //cleans the string
            value=cleanTransformString(value);
            
            AffineTransform af=new AffineTransform();
            
            //for each transform found in the value of the transform attribute
            while(value.length()!=0 && value.indexOf('(')!=-1){
                
                //the name of the transform
                transf=value.substring(0, value.indexOf('('));
                tvalues=value.substring(0, value.indexOf(')'));
                
                //removes the current transform from the value of the transform attribute
                value=value.substring(tvalues.length()+1, value.length());
                
                //the numeric value of the transform
                tvalues=tvalues.substring(tvalues.indexOf('(')+1, tvalues.length());
                
                //for each kind of transform, gets the numeric values and concatenates the transform to the matrix
                if(transf.equals("translate")){ //$NON-NLS-1$
                    
                    double e=0, f=0;
                    
                    try{
                        e=new Double(tvalues.substring(0, tvalues.indexOf(','))).doubleValue();
                        f=new Double(tvalues.substring(tvalues.indexOf(',')+1, tvalues.length())).doubleValue();
                    }catch(Exception ex){e=0; f=0;}
                    
                    af.concatenate(AffineTransform.getTranslateInstance(e, f));
                    
                }else if(transf.equals("scale")){ //$NON-NLS-1$
                    
                    double a=0, d=0;
                    
                    if(tvalues.indexOf(',')==-1){
                        
                        try{
                            a=new Double(tvalues).doubleValue();
                            d=a;
                        }catch(Exception ex){a=0; d=0;	}
                        
                    }else{
                        
                        try{
                            a=new Double(tvalues.substring(0,tvalues.indexOf(','))).doubleValue();
                            d=new Double(tvalues.substring(tvalues.indexOf(',')+1,tvalues.length())).doubleValue();
                        }catch(Exception ex){a=0; d=0;}
                    }
                    
                    af.concatenate(AffineTransform.getScaleInstance(a , d));
                    
                }else if(transf.equals("rotate")){ //$NON-NLS-1$
                    
                    double angle=0, e=0, f=0;
                    
                    if(tvalues.indexOf(',')==-1){
                        
                        try{
                            angle=new Double(tvalues).doubleValue();
                        }catch(Exception ex){angle=0;}
                        
                        af.concatenate(AffineTransform.getRotateInstance(Math.toRadians(angle)));
                        
                    }else{
                        
                        try{
                            angle=new Double(tvalues.substring(0,tvalues.indexOf(','))).doubleValue();
                            tvalues=tvalues.substring(tvalues.indexOf(',')+1,tvalues.length());
                            e=new Double(tvalues.substring(0,tvalues.indexOf(','))).doubleValue();
                            tvalues=tvalues.substring(tvalues.indexOf(',')+1,tvalues.length());
                            f=new Double(tvalues).doubleValue();
                        }catch(Exception ex){angle=0; e=0; f=0;}
                        
                        af.concatenate(AffineTransform.getTranslateInstance(e, f));
                        af.concatenate(AffineTransform.getRotateInstance(Math.toRadians(angle)));
                        af.concatenate(AffineTransform.getTranslateInstance(-e, -f));
                    }						
                    
                }else if(transf.equals("skewX")){ //$NON-NLS-1$
                    
                    double angle=0;
                    
                    try{
                        angle=new Double(tvalues).doubleValue();
                    }catch(Exception ex){angle=0;}
                    
                    af.concatenate(AffineTransform.getShearInstance(Math.tan(Math.toRadians(angle)), 0));
                    
                }else if(transf.equals("skewY")){ //$NON-NLS-1$
                    
                    double angle=0;
                    
                    try{
                        angle=new Double(tvalues).doubleValue();
                    }catch(Exception ex){angle=0;}
                    
                    af.concatenate(AffineTransform.getShearInstance(0, Math.tan(Math.toRadians(angle))));
                    
                }else if(transf.equals("matrix")){ //$NON-NLS-1$
                    
                    double[] m=new double[6];
                    int j=0, i=tvalues.indexOf(',');
                    tvalues=tvalues.concat(","); //$NON-NLS-1$
                    
                    while(i !=-1){
                        
                        try{
                            m[j]=new Double(tvalues.substring(0,i)).doubleValue();
                        }catch (Exception ex){}
                        
                        tvalues=tvalues.substring(tvalues.indexOf(',')+1, tvalues.length());
                        i=tvalues.indexOf(',');
                        
                        j++;
                    }
                    
                    af.concatenate(new AffineTransform(m[0], m[1], m[2], m[3], m[4], m[5]));
                    
                }else{
                    
                    break;
                }
            }
            
            matrix.concatenateTransform(af);
        }
        
        return matrix;
    }
    
    /**
     * replaces all the tranforms by their equivalent matrix transform
     * @param node the node on which the changes will be made
     */
    public void transformToMatrix(Node node){
        
        if(node!=null){
            
            NamedNodeMap attributes=node.getAttributes();
            
            if(attributes!=null){
                
                //if the node has the transform atrribute
                Node att=attributes.getNamedItem("transform"); //$NON-NLS-1$
                
                if(att!=null){
                    
                    //gets the value of the transform attribute
                    String value=new String(att.getNodeValue());
                    
                    if(value!=null && ! value.equals("")){ //$NON-NLS-1$
                        
                        //converts the transforms contained in the string to a single matrix transform
                        SVGTransformMatrix matrix=transformToMatrix(value);
                        
                        //if the matrix is not the identity matrix, it is set as the new transform matrix
                        if(matrix!=null && ! matrix.isIdentity()){
                            
                            //if the node has the transform attribute					
                            ((Element)node).setAttributeNS(null,"transform", matrix.getMatrixRepresentation()); //$NON-NLS-1$
                        }
                    }
                }
            }
        }
    }
    
    /**
     * gets a transformation matrix given a string containing a matrix transform
     * @param value the string containing a matrix transform
     * @return the corresponding transform matrix
     */
    public SVGTransformMatrix getTransformMatrix(String value){
        
        SVGTransformMatrix matrix=new SVGTransformMatrix(1,0,0,1,0,0);
        
        if(value!=null && ! value.equals("")){ //$NON-NLS-1$
            
            int rang=value.indexOf("matrix"); //$NON-NLS-1$
            
            //computes the double values of the matrix in the transform attribute
            if(rang>-1){
                
                String subValue=""; //$NON-NLS-1$
                subValue=value.substring(rang,value.length());
                subValue=subValue.substring(0,subValue.indexOf(")")+1); //$NON-NLS-1$
                value=value.replaceAll("["+subValue+"]",""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                subValue=subValue.substring(subValue.indexOf("("),subValue.length()); //$NON-NLS-1$
                
                //cleans the string
                value=cleanTransformString(value);
                subValue=subValue.replaceAll("[(]",""); //$NON-NLS-1$ //$NON-NLS-2$
                subValue=subValue.replaceAll("[)]",""); //$NON-NLS-1$ //$NON-NLS-2$
                subValue=subValue.concat(","); //$NON-NLS-1$
                
                int i=subValue.indexOf(','), j=0;
                double[] matrixDb=new double[6];
                
                while(i !=-1){
                    
                    try{
                        matrixDb[j]=new Double(subValue.substring(0,i)).doubleValue();
                    }catch (Exception ex){return new SVGTransformMatrix(1,0,0,1,0,0);}
                    
                    subValue=subValue.substring(subValue.indexOf(',')+1, subValue.length());
                    i=subValue.indexOf(',');
                    
                    j++;
                }
                
                matrix=new SVGTransformMatrix(matrixDb[0], matrixDb[1], matrixDb[2], matrixDb[3], matrixDb[4], matrixDb[5]);
            }
        }
        
        return matrix;
    }
    
    /**
     * gets a node's transformation matrix
     * @param node the node from which to get the transformation matrix
     * @return the transformation matrix
     */
    public SVGTransformMatrix getTransformMatrix(Node node){
        
        if(node!=null){
            
            NamedNodeMap attributes=node.getAttributes();
            
            if(attributes!=null){
                
                //if the node has the transform atrribute
                Node att=attributes.getNamedItem("transform"); //$NON-NLS-1$
                
                if(att!=null){
                    
                    //gets the value of the transform attribute
                    String value=att.getNodeValue();
                    
                    //creating the matrix transform
                    return getTransformMatrix(value);
                }
            }
        }
        
        //otherwise returns the identity matrix
        return new SVGTransformMatrix(1,0,0,1,0,0);
    }
    
    /**
     * sets the transform matrix of a node
     * @param node the given node
     * @param matrix the transformation matrix
     */
    public void setTransformMatrix(Node node, SVGTransformMatrix matrix){
        
        if(node!=null && node instanceof Element && matrix!=null){
        	
        	boolean isParticularGNode=false;
        	
        	if(		node.getNodeName().equals("g") &&  //$NON-NLS-1$
        			(getElementChildCount((Element)node)>1 || (getElementChildCount((Element)node)==1 && ! getElementFirstChild((Element)node).getNodeName().equals("svg")))){ //$NON-NLS-1$

        		isParticularGNode=true;
        	}
            
            if(isParticularGNode){
                
                SVGTransformMatrix cMatrix=null;
                
                //for each child of the g element, gets its matrix, modifies and sets it
                for(Node cur=node.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
                    
                    if(cur instanceof Element){
                        
                        cMatrix=getTransformMatrix(cur);
                        cMatrix.concatenateMatrix(matrix);
                        setTransformMatrix(cur, cMatrix);
                    }
                }
                
                ((Element)node).removeAttribute("transform"); //$NON-NLS-1$
                
            }else{
                
                //if the node has the transform attribute					
                ((Element)node).setAttributeNS(null,"transform", matrix.getMatrixRepresentation()); //$NON-NLS-1$
            }
        }
    }
    
    /**
     * returns the cloned node of the given node whose use nodes have been removed
     * @param node a node
     * @return the cloned node of the given node whose use nodes have been removed
     */
    public Node getClonedNodeWithoutUseNodes(Node node){
        
        Node clonedNode=null;
        
        if(node!=null){
            
            clonedNode=node.cloneNode(true);
            
            if(! clonedNode.getNodeName().equals("use")){ //$NON-NLS-1$
                
                //removes the use nodes from the subtree of the cloned node
                removeUseNodes(clonedNode);
                
            }else{
                
                clonedNode=null;
            }
        }
        
        return clonedNode;
    }
    
    /**
     * removes the use nodes in the given nodes
     * @param node a node
     */
    protected void removeUseNodes(Node node){
        
        if(node!=null && node.hasChildNodes()){
            
            NodeList children=node.getChildNodes();
            Node cur=null;
            
            for(int i=0; i<children.getLength(); i++){
                
                cur=children.item(i);
                
                if(cur!=null && cur.getNodeName().equals("use")){ //$NON-NLS-1$
                    
                    //if the node is a use node, it is removed
                    node.removeChild(cur);
                    
                }else if(cur!=null){
                    
                    //if the node is not a use node, its subtre is checked
                    removeUseNodes(cur);
                }
            }
        }
    }
    
    /**
     * computes a rectangle given 2 the coordinates of two points
     * @param point1 the first point
     * @param point2 the second point
     * @return the correct rectangle
     */
    public Rectangle2D.Double getComputedRectangle(Point2D.Double point1, Point2D.Double point2){
        
        if(point1!=null && point2!=null){
            
            double 	width=point2.x-point1.x, 
            height=point2.y-point1.y, 
            x=point1.x, 
            y=point1.y;
            
            if(point1.x>point2.x && point1.y>point2.y){
                
                x=point2.x;
                y=point2.y;
                width=point1.x-point2.x;
                height=point1.y-point2.y;
                
            }else if(point1.x>point2.x && point1.y<point2.y){
                
                width=point1.x-point2.x;
                height=point2.y-point1.y;
                x=point2.x;
                y=point1.y;
                
            }else if(point1.x<point2.x && point1.y>point2.y){
                
                width=point2.x-point1.x;
                height=point1.y-point2.y;
                x=point1.x;
                y=point2.y;
            }
            
            return new Rectangle2D.Double(x, y, width, height);	
        }
        
        return new Rectangle2D.Double(0, 0, 0, 0);
    }
    
    /**
     * computes a square given 2 the coordinates of two points
     * @param point1 the first point
     * @param point2 the second point
     * @return the correct square
     */
    public Rectangle2D.Double getComputedSquare(Point2D.Double point1, Point2D.Double point2){
        
        if(point1!=null && point2!=null){
            
            double	width=point2.x-point1.x,
            height=point2.y-point1.y,
            x=point1.x,
            y=point1.y;
            
            if(point1.x>point2.x && point1.y>point2.y){
                
                x=point2.x;
                y=point2.y;
                width=point1.x-point2.x;
                height=point1.y-point2.y;
                
                if(width<height){
                    
                    y=point2.y+(height-width);
                    height=width;
                    
                }else{
                    
                    x=point2.x+(width-height);
                    width=height;
                }
                
            }else if(point1.x>point2.x && point1.y<=point2.y){
                
                width=point1.x-point2.x;
                height=point2.y-point1.y;
                x=point2.x;
                y=point1.y;
                
                if(width<height){
                    
                    height=width;
                    
                }else{
                    
                    x=point2.x+(width-height);
                    width=height;
                }
                
            }else if(point1.x<=point2.x && point1.y>point2.y){
                
                width=point2.x-point1.x;
                height=point1.y-point2.y;
                x=point1.x;
                y=point2.y;
                
                if(width<height){
                    
                    y=point2.y+(height-width);
                    height=width;
                    
                }else{
                    
                    width=height;
                }
                
            }else if(point1.x<=point2.x && point1.y<=point2.y){
                
                if(width<height){
                    
                    height=width;
                    
                }else{
                    
                    width=height;
                }
            }
            
            return new Rectangle2D.Double(x, y, width, height);	
        }
        
        return new Rectangle2D.Double(0, 0, 0, 0);
    }

    /**
     * removes the "url()" prefix in the given string
     * @param value the string to be modified
     * @return the string without the "url()" prefix
     */
    public static String toUnURLValue(String value){
        
        if(value==null){
            
            value=""; //$NON-NLS-1$
        }
        
        String val=new String(value);
        int ind0=val.indexOf("url(#"), ind1=val.indexOf(")"); //$NON-NLS-1$ //$NON-NLS-2$
        
        if(ind0>=0 && ind1>=0){
            
            val=val.substring(ind0+5, ind1);
        }
        
        return val;
    }
    
    /**
     * adds the "url()" prefix in the given string
     * @param value the string to be modified
     * @return the string withthe "url()" prefix
     */
    public static String toURLValue(String value){
        
        if(value==null || (value!=null && value.equals(""))){ //$NON-NLS-1$
            
            value=""; //$NON-NLS-1$
            
        }else{
            
            value=new String("url(#"+value+")"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        return value;
    }

    /**
     * creates the list of the resources used by the given node and returns it
     * @param element an element
     * @param deep true if the children of the given node should be inspected
     * @return the list of the resources used by the given node
     */
    public LinkedList<Element> getResourcesUsedByNode(Element element, boolean deep){
        
        LinkedList<Element> resources=new LinkedList<Element>();
        
        if(element!=null){
            
            //getting the defs element
            Element root=element.getOwnerDocument().getDocumentElement();
            Node cur=null;
            Element defs=null;
            
            for(cur=root.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
                
                if(cur instanceof Element && cur.getNodeName().equals("defs")){ //$NON-NLS-1$
                    
                    defs=(Element)cur;
                }
            }
            
            //the string containing the ids of the resources needed
            String style=element.getAttribute("style"); //$NON-NLS-1$
            
            if(deep){
                
                for(NodeIterator it=new NodeIterator(element); it.hasNext();){
                    
                    cur=it.next();
                    
                    if(cur instanceof Element){
                        
                        style=style.concat(((Element)cur).getAttribute("style")); //$NON-NLS-1$
                    }
                }
            }
            
            if(defs!=null && style!=null && ! style.equals("")){ //$NON-NLS-1$
                
                String id=""; //$NON-NLS-1$
                Element el=null;
                
                //for each child of the "defs" element, adds it to the list if it is used by the given element
                for(cur=defs.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
                    
                    if(cur instanceof Element){
                        
                        el=(Element)cur;
                        id=el.getAttribute("id"); //$NON-NLS-1$
                        
                        //if the id of the resource is contained in the style attribute
                        if(id!=null && style.indexOf("#".concat(id))!=-1){ //$NON-NLS-1$
                            
                            resources.add(el);
                        }
                    }
                }
            }
        }
        
        return resources;
    }

    /**
     * returns whether the given element is a shape node or not
     * @param element
     * @return whether the given element is a shape node or not
     */
    public static boolean isElementAShape(Element element){
        
        if(element!=null){
            
            String name=element.getNodeName();
            
            return (name.equals("g") || name.equals("circle") || name.equals("ellipse") || name.equals("image") || name.equals("line") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                    || name.equals("path") || name.equals("polygon") || name.equals("polyline") || name.equals("rect") || name.equals("text")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }
        
        return false;
    }
    
    /**
     * returns the label corresponding to the given element
     * @param element an element
     * @return the label corresponding to the given element
     */
    public static String getElementLabel(Element element) {
    	
    	if(element!=null && svgElementLabels.containsKey(element.getTagName())) {
    		
    		return svgElementLabels.get(element.getTagName());
    	}
    	
    	return unknownShapeLabel;
    }
    
    /**
     * force a refresh of the current selection
     */
    public void forceReselection(){
        
        if(editor.getSVGSelection()!=null){
            
            //sets that the selection has changed
            editor.getSVGSelection().selectionChanged(true);
        }
    }
    
    /**
     * converts a string to a double that is a percentage
     * @param str a string
     * @param isPercentage the boolean telling if the string describes a percentage value
     * @return the corresponding value of the given string
     */
    public double getDoubleValue(String str, boolean isPercentage){
        
        if(str==null){
            
            str=""; //$NON-NLS-1$
        }
        
        str=str.replaceAll("\\s+", ""); //$NON-NLS-1$ //$NON-NLS-2$
        
        double val=0;
        boolean hasPercentSign=(str.indexOf("%")!=-1); //$NON-NLS-1$
        
        try{
            if(isPercentage){
                
                if(hasPercentSign){
                    
                    str=str.replaceAll("%", ""); //$NON-NLS-1$ //$NON-NLS-2$
                    val=Double.parseDouble(str);
                    
                }else{
                    
                    val=Double.parseDouble(str);
                    val=val*100;
                }
                
            }else{
                
                val=Double.parseDouble(str);
            }
        }catch (Exception ex){}
        
        return val;
    }
    
    /**
     * removes all the attributes and children in the duplicated node
     * and adds the attributes and children of the reference node to the duplicated node
     * @param duplicatedNode the duplicated node
     * @param referenceNode the reference node
     */
    public void duplicateNode(Node duplicatedNode, Node referenceNode){
        
        if(		duplicatedNode!=null && referenceNode!=null && 
                duplicatedNode instanceof Element && referenceNode instanceof Element){
            
            //removes all the attributes and children of the duplicated node
            //and replaces them by those of the reference node
            
            //removes all the children of the duplicated node
            while(duplicatedNode.hasChildNodes()){
                
                duplicatedNode.removeChild(duplicatedNode.getFirstChild());
            }
            
            //appends all the cloned children of the reference node
            Node cur=null, clonedNode=null;
            
            for(cur=referenceNode.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
                
                if(cur instanceof Element){
                    
                    clonedNode=cur.cloneNode(true);
                    duplicatedNode.appendChild(clonedNode);  
                }
            }
            
            //removes all the attributes of the duplicated node
            NamedNodeMap attr=duplicatedNode.getAttributes();
            Node at=null;
            int i;
            
            for(i=0; i<attr.getLength(); i++){
                
                at=attr.item(i);
                
                if(at!=null){
                    
                    ((Element)duplicatedNode).removeAttribute(at.getNodeName());
                }
            }
            
            //adds the attributes of the reference node
            attr=referenceNode.getAttributes();
            
            for(i=0; i<attr.getLength(); i++){
                
                at=attr.item(i);
                
                if(at!=null){
                    
                    ((Element)duplicatedNode).setAttribute(at.getNodeName(), at.getNodeValue());
                }
            }
        }
    }

    /**
     * creates a general path given the node
     * @param elt
     * @return a general path
     */
    public ExtendedGeneralPath getGeneralPath(Element elt){
        
        ExtendedGeneralPath path=null;
        
        if(elt!=null){
            
            Map segs=getPathSeg(elt);

            Point2D.Double point1=null, point2=null, point3=null;
            double d1=0, d2=0, d3=0;
            int i1=0, i2=0;
            String command="";  //$NON-NLS-1$
            char cmd=' ';
            java.util.List list=null;
            path=new ExtendedGeneralPath();
            
            if(segs==null){

                SVGTransformMatrix matrix=getTransformMatrix(elt);
                
                if(matrix!=null){

                    //transforms the path
                    AffineTransform af = matrix.getTransform();
                    path=new ExtendedGeneralPath(path.createTransformedShape(af));
                }
                
                return path;
            }
            
            for(Iterator it=segs.keySet().iterator(); it.hasNext();){
                
                try{
                    command=(String)it.next();
                    list=(java.util.List)segs.get(command);
                    cmd=command.charAt(0);
                }catch (Exception ex){cmd=' '; list=null;}
                
                try{
                    if(cmd!=' ' && list!=null){
                        
                        if(cmd=='Z'){
                            
                            path.closePath();
                            
                        }else if(cmd=='C'){
                            
                            point1=(Point2D.Double)list.get(0);
                            point2=(Point2D.Double)list.get(1);
                            point3=(Point2D.Double)list.get(2);
                            
                            path.curveTo((float)point1.x, (float)point1.y, (float)point2.x, (float)point2.y, (float)point3.x, (float)point3.y);
                            
                        }else if(cmd=='L'){
                            
                            point1=(Point2D.Double)list.get(0);
                            
                            path.lineTo((float)point1.x, (float)point1.y);
                            
                        }else if(cmd=='M'){
                            
                            point1=(Point2D.Double)list.get(0);
                            
                            path.moveTo((float)point1.x, (float)point1.y);
                            
                        }else if(cmd=='Q'){
                            
                            point1=(Point2D.Double)list.get(0);
                            point2=(Point2D.Double)list.get(1);
                            
                            path.quadTo((float)point1.x, (float)point1.y, (float)point2.x, (float)point2.y);
                            
                        }else if(cmd=='A'){
                            
                            d1=((Double)list.get(0)).doubleValue();
                            d2=((Double)list.get(1)).doubleValue();
                            d3=((Double)list.get(2)).doubleValue();
                            
                            i1=((Integer)list.get(3)).intValue();
                            i2=((Integer)list.get(4)).intValue();
                            
                            point1=(Point2D.Double)list.get(5);
                            path.arcTo((float)d1, (float)d2, (float)d3, i1==0?false:true, i2==0?false:true, (float)point1.x, (float)point1.y);
                        }
                    }
                }catch (Exception ex){}
            }
            
            SVGTransformMatrix matrix=getTransformMatrix(elt);
            
            if(matrix!=null){
                
                //transforms the path
                AffineTransform af=matrix.getTransform();
                path=new ExtendedGeneralPath(path.createTransformedShape(af));
            }
        }
        
        return path;
    }
    
    /**
     * gets the different segments of a path
     * @param node the node
     * @return the map containing the different segments of a path
     */
    public LinkedHashMap<String, LinkedList<Object>> getPathSeg(Element node){

        NamedNodeMap attributes=node.getAttributes();
        
        if(attributes!=null){
            
            //if the node has the "d" attribute
            Node att=attributes.getNamedItem("d"); //$NON-NLS-1$
            
            if(att!=null){
                
                //gets the value of the "d" attribute
                String value=att.getNodeValue();
                
                value.replaceAll("[,]",""); //$NON-NLS-1$ //$NON-NLS-2$
                //removes the first space characters from the string
                if(!value.equals("") && value.charAt(0)==' ')value=value.replaceFirst("[\\s]+",""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                
                //if the first character is not a 'M', the string value is not correct
                if(value.charAt(0)=='M' || value.charAt(0)=='m'){
                    
                    LinkedHashMap<String, LinkedList<Double>> map=new LinkedHashMap<String, LinkedList<Double>>();
                    String command=""; //$NON-NLS-1$
                    Double d=null;
                    LinkedList<Double> list=null;
                    int rg=0;
                    
                    //for each command in the value string
                    while(! value.equals("") && Character.isLetter(value.charAt(0))){ //$NON-NLS-1$
                        
                        //gets the command
                        command=value.substring(0,1);
                        value=value.substring(1,value.length());
                        
                        if(!value.equals("") && value.charAt(0)==' '){ //$NON-NLS-1$
                            
                            value=value.replaceFirst("[\\s]+",""); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        
                        //the list that will contains the values of the command (Double objects)
                        list=new LinkedList<Double>();
                        
                        while( ! value.equals("") && ! Character.isLetter(value.charAt(0))){ //$NON-NLS-1$
                            
                            //adds the Double values found in the value string in the list
                            try{
                                
                                if(value.indexOf(' ')!=-1){
                                    
                                    d=new Double(value.substring(0,value.indexOf(' ')));
                                    
                                }else{
                                    
                                    d=new Double(value);
                                    value="";	 //$NON-NLS-1$
                                }
                            }catch (Exception ex){return null;}
                            
                            value=value.substring(value.indexOf(' ')+1,value.length());
                            
                            if(!value.equals("") && value.charAt(0)==' '){ //$NON-NLS-1$
                                
                                value=value.replaceFirst("[\\s]+",""); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                            
                            list.add(d);
                        }
                        
                        //puts the command and its value to the map
                        map.put(command+new Integer(rg++).toString(), list);
                    }

                    //converts the map made of Double values into a map that contains points and other values
                    return convertPathValues(map);
                }
            }
        }
        
        return null;
    }
    
    /**
     * converts a map made of Double values into a map that contains points and other values
     * @param map the map containing only Double values
     * @return the map that contains points and other values
     */
    @SuppressWarnings("unused")
    public LinkedHashMap<String, LinkedList<Object>> convertPathValues(LinkedHashMap<String, LinkedList<Double>> map){
        
        if(map.size()>0){
            
            //the map that will be returned
            LinkedHashMap<String, LinkedList<Object>> map2=new LinkedHashMap<String, LinkedList<Object>>();
            Iterator it2=null;
            LinkedList<Double> list=null;
            LinkedList<Object> nlist=null;
            Point2D.Double lastPoint=null, point=null, beforeLastPoint=null;
            Double[] dtab=new Double[6];
            int rg=0, i, j;
            char cmd=' ', lastCmd=' ';
            
            //for each command in the map
            for(String command : map.keySet()){

                //gets the list of the Double values
                if(command!=null && ! command.equals("")){ //$NON-NLS-1$
                    
                    list=map.get(command);
                }

                if(list!=null){
                    
                    cmd=command.charAt(0);
                    
                    //according to the command
                    if(cmd=='M' || cmd=='m' || cmd=='L' || cmd=='l'){
                        
                        for(i=0; i<list.size()-1; i+=2){
                            
                            //the list of the points of the command
                            nlist=new LinkedList<Object>();
                            
                            //getting the values
                            dtab[0]=list.get(i);
                            dtab[1]=list.get(i+1);
                            
                            //adds the point created with the two double values to the list
                            if(Character.isLowerCase(cmd)){
                                
                                if(lastPoint==null) {
                                    
                                    lastPoint=new Point2D.Double(0, 0);
                                }
                                
                                point=new Point2D.Double(lastPoint.x+dtab[0].doubleValue(), lastPoint.y+dtab[1].doubleValue());
                                
                            }else{
                                
                                point=new Point2D.Double(dtab[0].doubleValue(),dtab[1].doubleValue());
                            }
                            
                            //sets the value of the lastPoint
                            beforeLastPoint=lastPoint;
                            lastPoint=point;
                            
                            nlist.add(point);

                            //adds the command to the list
                            map2.put(Character.toUpperCase(cmd)+new Integer(rg++).toString(), nlist);
                        }
                        
                    }else if(cmd=='T' || cmd=='t'){
                        
                        for(i=0; i<list.size()-1; i+=2){
                            
                            //the list of the points of the command
                            nlist=new LinkedList<Object>();
                            
                            //computing the control point 
                            if(lastPoint==null){
                                
                                lastPoint=new Point2D.Double(0, 0);
                            }
                            
                            //adding the first control point
                            if(lastCmd=='Q' && beforeLastPoint!=null){
                                
                                point=new Point2D.Double(2*lastPoint.x-beforeLastPoint.x, 2*lastPoint.y-beforeLastPoint.y);
                                nlist.add(point);
                                
                                beforeLastPoint=lastPoint;
                                lastPoint=point;
                                
                            }else{
                                
                                nlist.add(lastPoint);
                                beforeLastPoint=lastPoint;
                            }
                            
                            //computing the new current point
                            dtab[0]=list.get(i);
                            dtab[1]=list.get(i+1);
                            
                            //adds the point created with the two double values to the list
                            if(Character.isLowerCase(cmd)){
                                
                                if(lastPoint==null){
                                    
                                    lastPoint=new Point2D.Double(0, 0);
                                }
                                
                                point=new Point2D.Double(lastPoint.x+dtab[0].doubleValue(), lastPoint.y+dtab[1].doubleValue());
                                
                            }else{
                                
                                point=new Point2D.Double(dtab[0].doubleValue(),dtab[1].doubleValue());
                            }
                            
                            //sets the value of the lastPoint
                            beforeLastPoint=lastPoint;
                            lastPoint=point;
                            
                            nlist.add(point);
                            
                            //adds the command to the list
                            cmd='Q';
                            map2.put(cmd+new Integer(rg++).toString(),nlist);
                        }
                        
                    }else if(cmd=='Q' || cmd=='q'){
                        
                        for(i=0; i<list.size()-3; i+=4){
                            
                            //the list of the points of the command
                            nlist=new LinkedList<Object>();
                            
                            dtab[0]=list.get(i);
                            dtab[1]=list.get(i+1);
                            dtab[2]=list.get(i+2);
                            dtab[3]=list.get(i+3);
                            
                            for(j=0; j<4; j+=2){
                                
                                //adds the point created with the two double values to the list
                                if(Character.isLowerCase(cmd)){
                                    
                                    if(lastPoint==null){
                                        
                                        lastPoint=new Point2D.Double(0, 0);
                                    }
                                    
                                    point=new Point2D.Double(lastPoint.x+dtab[j].doubleValue(), lastPoint.y+dtab[j+1].doubleValue());
                                    
                                }else{
                                    
                                    point=new Point2D.Double(dtab[j].doubleValue(), dtab[j+1].doubleValue());
                                }
                                
                                //sets the value of the last point
                                beforeLastPoint=lastPoint;
                                lastPoint=point;
                                
                                nlist.add(point);
                            }
                            
                            //adds the command to the list
                            cmd=Character.toUpperCase(cmd);
                            map2.put(cmd+new Integer(rg++).toString(), nlist);
                        }
                        
                    }else if(cmd=='C' || cmd=='c'){
                        
                        for(i=0; i<list.size()-5; i+=6){
                            
                            //the list of the points of the command
                            nlist=new LinkedList<Object>();
                            
                            dtab[0]=list.get(i);
                            dtab[1]=list.get(i+1);
                            dtab[2]=list.get(i+2);
                            dtab[3]=list.get(i+3);
                            dtab[4]=list.get(i+4);
                            dtab[5]=list.get(i+5);
                            
                            for(j=0; j<6; j+=2){
                                
                                //adds the point created with the two double values to the list
                                if(Character.isLowerCase(cmd)){
                                    
                                    if(lastPoint==null){
                                        
                                        lastPoint=new Point2D.Double(0, 0);
                                    }
                                    
                                    point=new Point2D.Double(lastPoint.x+dtab[j].doubleValue(), lastPoint.y+dtab[j+1].doubleValue());
                                    
                                }else{
                                    
                                    point=new Point2D.Double(dtab[j].doubleValue(),dtab[j+1].doubleValue());
                                }
                                
                                //sets the value of the lastPoint
                                beforeLastPoint=lastPoint;
                                lastPoint=point;
                                
                                nlist.add(point);
                            }
                            
                            //adds the command to the list
                            cmd=Character.toUpperCase(cmd);
                            map2.put(cmd+new Integer(rg++).toString(), nlist);
                        }
                        
                    }else if(cmd=='S' || cmd=='s'){
                        
                        for(i=0; i<list.size()-3; i+=4){
                            
                            //the list of the points of the command
                            nlist=new LinkedList<Object>();
                            
                            //computing the control point 
                            if(lastPoint==null){
                                
                                lastPoint=new Point2D.Double(0, 0);
                            }
                            
                            //adding the first control point
                            if(lastCmd=='Q' && beforeLastPoint!=null){
                                
                                point=new Point2D.Double(2*lastPoint.x-beforeLastPoint.x, 2*lastPoint.y-beforeLastPoint.y);
                                nlist.add(point);
                                
                                beforeLastPoint=lastPoint;
                                lastPoint=point;
                                
                            }else{
                                
                                nlist.add(lastPoint);
                                beforeLastPoint=lastPoint;
                            }
                            
                            //computing the two next points
                            dtab[0]=list.get(i);
                            dtab[1]=list.get(i+1);
                            dtab[2]=list.get(i+2);
                            dtab[3]=list.get(i+3);
                            
                            for(j=0; j<4; j+=2){
                                
                                //adds the point created with the two double values to the list
                                if(Character.isLowerCase(cmd)){
                                    
                                    if(lastPoint==null){
                                        
                                        lastPoint=new Point2D.Double(0, 0);
                                    }
                                    
                                    point=new Point2D.Double(lastPoint.x+dtab[j].doubleValue(), lastPoint.y+dtab[j+1].doubleValue());
                                    
                                }else{
                                    
                                    point=new Point2D.Double(dtab[j].doubleValue(),dtab[j+1].doubleValue());
                                }
                                
                                //sets the value of the lastPoint
                                beforeLastPoint=lastPoint;
                                lastPoint=point;
                                
                                nlist.add(point);
                            }
                            
                            //adds the command to the list
                            cmd='C';
                            map2.put(cmd+new Integer(rg++).toString(), nlist);
                        }
                        
                    }else if(cmd=='H' || cmd=='h'){
                        
                        if(list!=null && list.size()>0){
                            
                            for(i=0; i<list.size(); i++){
                                
                                //the list of the values of the command
                                nlist=new LinkedList<Object>();
                                
                                dtab[0]=list.get(i);
                                
                                if(dtab[0]!=null){
                                    
                                    if(Character.isLowerCase(cmd)){
                                        
                                        if(lastPoint==null)lastPoint=new Point2D.Double(0, 0);
                                        
                                        point=new Point2D.Double(lastPoint.x+dtab[0].doubleValue(), lastPoint.y);
                                        
                                    }else{
                                        
                                        point=new Point2D.Double(dtab[0].doubleValue(), lastPoint.y);
                                    }
                                    
                                    beforeLastPoint=lastPoint;
                                    lastPoint=point;
                                    nlist.add(point);
                                }
                                
                                //adds the command to the list
                                cmd='L';
                                map2.put(cmd+new Integer(rg++).toString(),nlist);
                            }
                        }
                        
                    }else if(cmd=='V' || cmd=='v'){

                        if(list!=null && list.size()>0){
                            
                            for(i=0; i<list.size(); i++){
                                
                                //the list of the values of the command
                                nlist=new LinkedList<Object>();
                                
                                dtab[0]=list.get(i);
                                
                                if(dtab[0]!=null){
                                    
                                    if(Character.isLowerCase(cmd) && lastPoint!=null){
                                        
                                        if(lastPoint==null)lastPoint=new Point2D.Double(0, 0);
                                        
                                        point=new Point2D.Double(lastPoint.x, lastPoint.y+dtab[0].doubleValue());
                                        
                                    }else{
                                        
                                        point=new Point2D.Double(lastPoint.x, dtab[0].doubleValue());
                                    }
                                    
                                    beforeLastPoint=lastPoint;
                                    lastPoint=point;
                                    nlist.add(point);
                                }
                                
                                //adds the command to the list
                                cmd='L';
                                map2.put(cmd+new Integer(rg++).toString(), nlist);
                            }
                        }
                        
                    }else if(cmd=='A' || cmd=='a'){
                        
                        if(list.size()%7==0){
                            
                            it2=list.iterator();
                            
                            while(it2.hasNext()){
                                
                                //the list of the values of the command
                                nlist=new LinkedList<Object>();
                                
                                //adds the values to the list
                                nlist.add(it2.next());
                                if(it2.hasNext()){
                                    
                                    nlist.add(it2.next());
                                }
                                
                                if(it2.hasNext()){
                                    
                                    nlist.add(it2.next());
                                }
                                
                                if(it2.hasNext()){
                                    
                                    dtab[0]=(Double)it2.next();
                                    
                                    if(dtab[0]!=null){
                                        
                                        nlist.add(new Integer((int)dtab[0].doubleValue()));
                                    }
                                }
                                
                                if(it2.hasNext()){
                                    
                                    dtab[0]=(Double)it2.next();
                                    
                                    if(dtab[0]!=null){
                                        
                                        nlist.add(new Integer((int)dtab[0].doubleValue()));
                                    }
                                }
                                
                                //creates the point with the two last double values
                                if(it2.hasNext()){
                                    
                                    try{
                                        dtab[0]=(Double)it2.next();
                                        if(it2.hasNext())dtab[1]=(Double)it2.next();
                                    }catch (Exception ex){return null;}	
                                    
                                    if(dtab[0]!=null && dtab[1]!=null){
                                        
                                        if(Character.isLowerCase(cmd)){
                                            
                                            if(lastPoint==null)lastPoint=new Point2D.Double(0, 0);
                                            
                                            point=new Point2D.Double(dtab[0].doubleValue()+lastPoint.x, dtab[1].doubleValue()+lastPoint.y);
                                            
                                        }else{
                                            
                                            point=new Point2D.Double(dtab[0].doubleValue(), dtab[1].doubleValue());
                                        }
                                        
                                        beforeLastPoint=lastPoint;
                                        lastPoint=point;
                                        nlist.add(point);
                                    }
                                }
                                
                                //adds the command to the list
                                cmd='A';
                                map2.put(cmd+new Integer(rg++).toString(),nlist);			
                            }
                        }
                        
                    }else if(cmd=='Z' || cmd=='z'){
                        
                        nlist=new LinkedList<Object>();
                        nlist.add(new Point2D.Double());
                        
                        //adds the command to the list
                        map2.put(cmd+new Integer(rg++).toString(),nlist);
                        
                    }else{
                        
                        return null;
                    }
                    
                    lastCmd=cmd;
                }				
            }
            
            return map2;
        }
        
        return null;
    }
    
    /**
     * sets the value od the "d" attribute for this node
     * @param node
     * @param map
     */
    public void setPathSeg(Element node, LinkedHashMap<String, LinkedList<Object>> map){

        if(map!=null && map.size()>0){
            
            StringBuffer value=new StringBuffer(""); //$NON-NLS-1$
            Iterator it2=null;
            LinkedList<Object> list=null;
            Point2D.Double point=null, lastPoint=null;
            char cmd=' ', lastcmd=' ';
            boolean success=false;
            
            //for each command contained in the map : 
            for(String command : map.keySet()){
                
                list=map.get(command);
                
                if(command!=null && ! command.equals("") && list!=null && list.size()>0){ //$NON-NLS-1$
                    
                    cmd=command.charAt(0);
                    
                    if(cmd=='M' || cmd=='m' || cmd=='Q' || cmd=='q' || cmd=='T' || cmd=='t' || cmd=='C' || cmd=='c' || cmd=='S' || cmd=='s'){
                        
                        //concatenates the name of the command to the value string
                        if(cmd!=lastcmd && cmd!=' '){
                            
                            value.append(cmd+" "); //$NON-NLS-1$
                        }
                        
                        //gets the points contained in the list and concatenates the coordinates to the value string	
                        for(it2=list.iterator(); it2.hasNext();){
                            
                            try{point=(Point2D.Double)it2.next();}catch(Exception ex){point=null;}
                            
                            if(point!=null){
                                
                                lastPoint=point;
                                value.append(format.format(point.x)+" "+format.format(point.y)+" "); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                        
                    }else if(cmd=='L' || cmd=='l'){
                        
                        //gets the point contained in the list, checks if a simplification can be made and concatenates the coordinates to the value string 
                        it2=list.iterator();
                        
                        try{
                            if(it2.hasNext()){
                                
                                point=(Point2D.Double)it2.next();
                                
                            }else{
                                
                                point=null;
                            }
                        }catch(Exception ex){point=null;}
                        
                        if(point!=null){	
                            
                            success=false;
                            
                            //converts the 'L' command into h or v commands according to the value of the current point
                            if(lastPoint!=null){
                                
                                if(Character.isLowerCase(cmd)){
                                    
                                    if(point.x==0){
                                        
                                        cmd='v';
                                        
                                        //concatenates the name of the command to the value string
                                        if(cmd!=lastcmd && cmd!=' '){
                                            
                                            value.append(cmd+" "); //$NON-NLS-1$
                                        }
                                        
                                        value.append(format.format(point.y)+" "); //$NON-NLS-1$
                                        success=true;
                                        
                                    }else if(point.y==0){
                                        
                                        cmd='h';
                                        
                                        //concatenates the name of the command to the value string
                                        if(cmd!=lastcmd && cmd!=' '){
                                            
                                            value.append(cmd+" "); //$NON-NLS-1$
                                        }
                                        
                                        value.append(format.format(point.x)+" "); //$NON-NLS-1$
                                        success=true;
                                    }
                                    
                                }else{
                                    
                                    if(point.x==lastPoint.x){
                                        
                                        cmd='V';
                                        //concatenates the name of the command to the value string
                                        if(cmd!=lastcmd && cmd!=' '){
                                            
                                            value.append(cmd+" "); //$NON-NLS-1$
                                        }
                                        
                                        value.append(format.format(point.y)+" "); //$NON-NLS-1$
                                        success=true;
                                        
                                    }else if(point.y==lastPoint.y){
                                        
                                        cmd='H';
                                        
                                        //concatenates the name of the command to the value string
                                        if(cmd!=lastcmd && cmd!=' '){
                                            
                                            value.append(cmd+" "); //$NON-NLS-1$
                                        }
                                        
                                        value.append(format.format(point.x)+" "); //$NON-NLS-1$
                                        success=true;
                                        
                                    }
                                }
                            }
                            
                            if(! success){
                                
                                //concatenates the name of the command to the value string
                                if(cmd!=lastcmd && cmd!=' '){
                                    
                                    value.append(cmd+" "); //$NON-NLS-1$
                                }
                                
                                value.append(format.format(point.x)+" "+format.format(point.y)+" "); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                            
                            lastPoint=point;
                        }
                        
                    }else if(cmd=='A' || cmd=='a'){
                        
                        if(list.size()==6){
                            
                            //concatenates the name of the command to the value string
                            if(cmd!=lastcmd && cmd!=' '){
                                
                                value.append(cmd+" "); //$NON-NLS-1$
                                
                            }else{
                                
                                value.append(" "); //$NON-NLS-1$
                            }
                            
                            //gets the values contained in the list and concatenates them to the value string
                            try{
                                value.append(format.format(((Double)list.get(0)).doubleValue())+" "); //$NON-NLS-1$
                                value.append(format.format(((Double)list.get(1)).doubleValue())+" "); //$NON-NLS-1$
                                value.append(format.format(((Double)list.get(2)).doubleValue())+" "); //$NON-NLS-1$
                                value.append(format.format(((Integer)list.get(3)).intValue())+" "); //$NON-NLS-1$
                                value.append(format.format(((Integer)list.get(4)).intValue())+" "); //$NON-NLS-1$
                                point=(Point2D.Double)list.get(5);
                                lastPoint=point;
                                value.append(format.format(point.x)+" "+format.format(point.y)+" "); //$NON-NLS-1$ //$NON-NLS-2$
                            }catch (Exception e){}
                        }
                        
                    }else if(cmd=='Z'){
                        
                        //concatenates the name of the command to the value string
                        if(cmd!=lastcmd && cmd!=' '){
                            
                            value.append(cmd+" "); //$NON-NLS-1$
                            
                        }else{
                            
                            value.append(" "); //$NON-NLS-1$
                        }
                    }
                }
                
                lastcmd=cmd;
            }
            
            node.setAttributeNS(null,"d", value.toString()); //$NON-NLS-1$
        }
    }

    /**
     * picks the color at the given point on the screen
     * @param point a point in the screen coordinates
     * @return the color corresponding to the given point
     */
    public Color pickColor(Point point){
        
        Color color=new Color(255, 255, 255);
        
        if(point!=null){
            
            try{
                //getting the color at this point
                Robot robot=new Robot();
                color=robot.getPixelColor(point.x, point.y);
            }catch (Exception ex){}
        }
        
        return color;
    }
    
    /**
     * returns an icon displaying the given color
     * @param color a color 
     * @param size the size of the image to be returned
     * @return an icon displaying the given color
     */
    public Image getImageFromColor(Color color, Dimension size){
        
        if(color!=null && size!=null && size.width>0 && size.height>0){
            
            BufferedImage image=new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g=(Graphics2D)image.getGraphics();
            
            g.setColor(color);
            g.fillRect(0, 0, size.width, size.height);
            
            g.setColor(MetalLookAndFeel.getSeparatorForeground());
            g.drawRect(0, 0, size.width-1, size.height-1);
            
            return image;
        }
        
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    }
    
    /**
     * creates a cursor containing the given color and returns it
     * @param color a color
     * @return a cursor containing the given color
     */
    public Cursor createCursorImageFromColor(Color color){
        
        Cursor cursor=null;
        
        if(color!=null){
            
            //tells which size is better for the cursor images or if the cutom cursors option can't be used
            Dimension bestSize=Toolkit.getDefaultToolkit().getBestCursorSize(22,22);
            
            try{
                cursor=Toolkit.getDefaultToolkit().createCustomCursor(getImageFromColor(color, bestSize), new Point(0, 0), "color"); //$NON-NLS-1$
            }catch (Exception ex){}
        }
        
        return cursor;
    }
    
    /**
     * creates a cursor given an image
     * @param image an image
     * @return a cursor containing the image
     */
    public Cursor createCursorFromImage(Image image){
        
        Cursor cursor=null;
        
        if(image!=null){
            
            try{
                cursor=Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "resource"); //$NON-NLS-1$
            }catch (Exception ex){}
        }
        
        return cursor;
    }
    
    /**
     * returns the value of a style property
     * @param element an element
     * @param name the name of a style property
     * @return  the value of a style property
     */
    public String getStyleProperty(Element element, String name){
        
        String value=""; //$NON-NLS-1$
        
        if(element!=null && name!=null && ! name.equals("")){ //$NON-NLS-1$
            
            //gets the value of the style attribute
            String styleValue=element.getAttribute("style"); //$NON-NLS-1$
            styleValue=styleValue.replaceAll("\\s*[;]\\s*", ";"); //$NON-NLS-1$ //$NON-NLS-2$
            styleValue=styleValue.replaceAll("\\s*[:]\\s*", ":"); //$NON-NLS-1$ //$NON-NLS-2$
            
            int rg=styleValue.indexOf(";".concat(name.concat(":"))); //$NON-NLS-1$ //$NON-NLS-2$
            
            if(rg!=-1){
                
                rg++;
            }
            
            if(rg==-1){
                
                rg=styleValue.indexOf(name.concat(":")); //$NON-NLS-1$
                
                if(rg!=0){
                    
                    rg=-1;
                }
            }
            
            //if the value of the style attribute contains the property
            if(styleValue!=null && ! styleValue.equals("") && rg!=-1){ //$NON-NLS-1$
                
                //computes the value of the property
                value=styleValue.substring(rg+name.length()+1, styleValue.length());
                rg=value.indexOf(";"); //$NON-NLS-1$
                value=value.substring(0, rg==-1?value.length():rg);
            }
        }
        
        return value;
    }
    
	/**
	 * setting the value of the given style element for the given node
	 * @param element an element
	 * @param name the name of a style element
	 * @param value the value for this style element
	 */
	public static void setStyleProperty(Element element, String name, String value){
		
		if(element!=null && name!=null && ! name.equals("")){ //$NON-NLS-1$
			
			if(value==null){
				
				value=""; //$NON-NLS-1$
			}
			
			//the separators
			String valuesSep=";", nameToValueSep=":"; //$NON-NLS-1$ //$NON-NLS-2$
			
			//the map associating the name of a property to its value
			HashMap<String, String> values=new HashMap<String, String>();

			//getting the value of the style attribute
			String styleValue=element.getAttribute("style"); //$NON-NLS-1$
			styleValue=styleValue.replaceAll("\\s*[;]\\s*", ";"); //$NON-NLS-1$ //$NON-NLS-2$
			styleValue=styleValue.replaceAll("\\s*[:]\\s*", ":"); //$NON-NLS-1$ //$NON-NLS-2$
			
			//filling the map associating a property to its value
			String[] splitValues=styleValue.split(valuesSep);
			int pos=-1;
			String sname="", svalue=""; //$NON-NLS-1$ //$NON-NLS-2$
			
			for(int i=0; i<splitValues.length; i++){
				
				if(splitValues[i]!=null && ! splitValues[i].equals("")){ //$NON-NLS-1$
					
					pos=splitValues[i].indexOf(nameToValueSep);
					
					sname=splitValues[i].substring(0, pos);
					svalue=splitValues[i].substring(pos+nameToValueSep.length(), splitValues[i].length());
					
					if(! sname.equals("") && ! svalue.equals("")){ //$NON-NLS-1$ //$NON-NLS-2$
						
						values.put(sname, svalue);
					}
				}
			}
			
			//adding the new value
			if(value.equals("")){ //$NON-NLS-1$
				
				values.remove(name);
				
			}else{
				
				values.put(name, value);
			}
			
			//computing the new style value
			styleValue=""; //$NON-NLS-1$
			
			for(String newName : values.keySet()){
				
				styleValue+=newName+nameToValueSep+values.get(newName)+valuesSep;
			}
			
			//sets the value of the style attribute
			if(styleValue!=null && ! styleValue.equals("")){ //$NON-NLS-1$
				
				element.setAttribute("style", styleValue); //$NON-NLS-1$
				
			}else{
				
				element.removeAttribute("style"); //$NON-NLS-1$
			}
		}
	}
    
    /**
     * recursively removes all the attributes that are not necessary for the given node
     * @param element a node
     */
    public void removeUselessAttributes(Element element){
        
        if(element!=null){
            
            String nspXLink="http://www.w3.org/1999/xlink", nspNS="http://www.w3.org/2000/xmlns/"; //$NON-NLS-1$ //$NON-NLS-2$
            Node node=null;
            Element el=null;
            
            for(NodeIterator it=new NodeIterator(element); it.hasNext();){
                
                node=it.next();
                
                if(node!=null && node instanceof Element && ! node.getNodeName().equals("svg")){ //$NON-NLS-1$
                    
                	el=(Element)node;
                	
                	el.removeAttributeNS(nspXLink, "xlink:show"); //$NON-NLS-1$
                	el.removeAttributeNS(nspXLink, "xlink:type"); //$NON-NLS-1$
                	el.removeAttributeNS(nspXLink, "xlink:actuate"); //$NON-NLS-1$
                	el.removeAttributeNS(nspNS, "xlink"); //$NON-NLS-1$
                }
            }
        }
    }
    
	/**
	 * computes the last point of a line given the current point of the mouse when the control button is down
	 * @param startPoint the start point for the segment
	 * @param basePoint the current's mouse point
	 * @return the computed point
	 */
	public static Point2D.Double computeLinePointWhenCtrlDown(Point2D.Double startPoint, Point2D.Double basePoint){

	    Point2D.Double pt=new Point2D.Double();
	    
	    if(startPoint!=null && basePoint!=null){
	        
	        pt.x=basePoint.x;
	        pt.y=basePoint.y;
	        
	        Point2D.Double pt1=new Point2D.Double(startPoint.x, startPoint.y), pt2=new Point2D.Double(basePoint.x, basePoint.y);
	        
	        //the norme
	        double n=Math.sqrt(Math.pow((pt2.x-pt1.x), 2)+Math.pow((pt2.y-pt1.y), 2));
	        
	        //the x-distance and the y-distance
	        double xDistance=Math.abs(pt2.x-pt1.x), yDistance=Math.abs(pt2.y-pt1.y);

	        //the angle
	        double cosinus=(pt2.x-pt1.x)/n;

	        //computing the new point
	        if(pt1.x<=pt2.x && pt1.y>=pt2.y){
	            
	            if(cosinus<=1 && cosinus>Math.cos(Math.PI/8)){
	                
	               pt.x=(int)(pt1.x+xDistance);
	               pt.y=pt1.y;

	            }else if(cosinus<=Math.cos(Math.PI/8) && cosinus>Math.cos(3*Math.PI/8)){
	                
	               pt.x=(int)(pt1.x+xDistance);
	               pt.y=(int)(pt1.y-xDistance);
	                
	            }else if(cosinus<=Math.cos(3*Math.PI/8) && cosinus>0){
	                
	               pt.x=pt1.x;
	               pt.y=(int)(pt1.y-yDistance);
	            }

	        }else if(pt1.x>pt2.x && pt1.y>=pt2.y){
	            
	            if(cosinus<=0 && cosinus>Math.cos(5*Math.PI/8)){
	                
		               pt.x=pt1.x;
		               pt.y=(int)(pt1.y-yDistance);

	            }else if(cosinus<=Math.cos(5*Math.PI/8) && cosinus>=Math.cos(7*Math.PI/8)){
	                
		               pt.x=(int)(pt1.x-xDistance);
		               pt.y=(int)(pt1.y-xDistance);
	                
	            }else if(cosinus<=Math.cos(7*Math.PI/8) && cosinus>=-1){
		               
		               pt.x=(int)(pt1.x-xDistance);
		               pt.y=pt1.y;
	            }

	        }else if(pt1.x>=pt2.x && pt1.y<pt2.y){
	            
	            if(cosinus>=-1 && cosinus<Math.cos(7*Math.PI/8)){
	                
		               pt.x=(int)(pt1.x-xDistance);
		               pt.y=pt1.y;

	            }else if(cosinus>=Math.cos(7*Math.PI/8) && cosinus<Math.cos(5*Math.PI/8)){
	                
		               pt.x=(int)(pt1.x-xDistance);
		               pt.y=(int)(pt1.y+xDistance);
		               
	            }else if(cosinus>=Math.cos(5*Math.PI/8) && cosinus<0){
	                
		               pt.x=pt1.x;
		               pt.y=(int)(pt1.y+yDistance);
	            }

	        }else if(pt1.x<=pt2.x && pt1.y<=pt2.y){
	            
	            if(cosinus>=0 && cosinus<Math.cos(3*Math.PI/8)){
	                
		               pt.x=pt1.x;
		               pt.y=(int)(pt1.y+yDistance);

	            }else if(cosinus>=Math.cos(3*Math.PI/8) && cosinus<Math.cos(Math.PI/8)){
	                
		               pt.x=(int)(pt1.x+xDistance);
		               pt.y=(int)(pt1.y+xDistance);
	                
	            }else if(cosinus>=Math.cos(Math.PI/8) && cosinus<1){
	                
		               pt.x=(int)(pt1.x+xDistance);
		               pt.y=pt1.y;
	            }
	        }
	    }

	    return pt;
	}
}
