/*
 * Created on 2 juin 2004
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
package fr.itris.glips.svgeditor.resources;

import java.awt.Color;
import java.awt.Image;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.helpers.XMLReaderFactory;

import fr.itris.glips.svgeditor.NodeIterator;
import fr.itris.glips.svgeditor.SVGEditor;
import fr.itris.glips.svgeditor.colorchooser.SVGW3CColor;

/**
 * @author Jordi SUC
 *
 * The class managing the resources
 */
public class SVGResource {
    
    /**
     * the current directory for the system
     */
    private File currentDirectory=null;
    
    /**
     * the hashtable associating a an icon's name to an icon
     */
    private static final Hashtable<String, ImageIcon> icons=new Hashtable<String, ImageIcon>();
    
    /**
     * the hashtable associating a an icon's name to a gray icon
     */
    private static final Hashtable<String, ImageIcon> grayIcons=new Hashtable<String, ImageIcon>();
    
    /**
     * the list of the recent files
     */
    private final LinkedList<String> recentFiles=new LinkedList<String>();
    
    /**
     * the map associating the name of a xml document to this document
     */
    private final static Hashtable<String, Document> cachedXMLDocuments=new Hashtable<String, Document>();
    
    /**
     * a user preference node
     */
    private Preferences preferences=null;
    
    /**
     * the document of the xml properties
     */
    private Document xmlProperties=null;
    
    /**
     * the document of the resource store
     */
    private Document resourceStore=null;
    
    /**
     * the set of the style element names
     */
    private final HashSet<String> styleProperties=new HashSet<String>();
    
    /**
     * the list of the runnables that will be run when the editor is exited
     */
    private final LinkedList<Runnable> exitRunnables=new LinkedList<Runnable>();
    
    /**
     * the map associating the name of a w3c color to a svg w3c color object
     */
    private LinkedHashMap<String, SVGW3CColor> w3cColorsMap=new LinkedHashMap<String, SVGW3CColor>();
    
    /**
     * the editor
     */
    private SVGEditor editor=null;
    
    /**
     * the constructor of the class
     * @param editor the editor
     */
    public SVGResource(SVGEditor editor){
        
        this.editor=editor;
        
        //creates the list of the recently opened files//
        
        //getting the preference node
        preferences=Preferences.userNodeForPackage(editor.getClass());
        
        if(preferences!=null){
            
            String[] keys=null;
            
            try{
                keys=preferences.keys();
            }catch (Exception ex){}
            
            if(keys!=null){
                
                //filling the list of the recent files
                String val=""; //$NON-NLS-1$
                
                for(int i=0; i<keys.length; i++){
                    
                    val=preferences.get(keys[i], null);
                    
                    if(val!=null && ! val.equals("")){ //$NON-NLS-1$
                        
                        recentFiles.add(val);
                    }
                }
            }
        }
        
        //gets the W3C colors
        retrieveW3CColors(getXMLDocument("svgColors.xml")); //$NON-NLS-1$
    }
    
    /**
     * @return the editor
     */
    public SVGEditor getSVGEditor(){
        return editor;
    }
    
    /**
     * retrieves all the colors in the given svg document and fills the list and the map of the colors
     * @param svgColorsDocument the document containing the name of each and its associated rgb value
     */
    protected void retrieveW3CColors(Document svgColorsDocument){
        
        if(svgColorsDocument!=null && svgColorsDocument.getDocumentElement()!=null){
            
            String id="", value=""; //$NON-NLS-1$ //$NON-NLS-2$
            Color color=null;
            SVGW3CColor svgColor=null;
            LinkedList<SVGW3CColor> colorsList=new LinkedList<SVGW3CColor>();
            
            //for each svg colors, gets the name and the value and adds them to the list
            for(Node cur=svgColorsDocument.getDocumentElement().getFirstChild(); cur!=null; cur=cur.getNextSibling()){
                
                if(cur instanceof Element && cur.getNodeName().equals("svgColor")){ //$NON-NLS-1$
                    
                    id=((Element)cur).getAttribute("id"); //$NON-NLS-1$
                    value=((Element)cur).getAttribute("value"); //$NON-NLS-1$
                    
                    if(id!=null && ! id.equals("") && value!=null && ! value.equals("")){ //$NON-NLS-1$ //$NON-NLS-2$
                        
                        color=getColor(value);
                        
                        if(color!=null){
                            
                            svgColor=new SVGW3CColor(id, color);
                            colorsList.add(svgColor);
                        }
                    }
                }
            }
            
            //puts the colors and the ids to the map of the colors
            for(SVGW3CColor svgColor2 : colorsList){

                if(svgColor2!=null){
                    
                    w3cColorsMap.put(svgColor2.getId(), svgColor2);
                }
            }
        }
    }
    
    /**
     *@return the w3c color objects list
     */
    public LinkedList<SVGW3CColor> getW3CColors(){
        
        return new LinkedList<SVGW3CColor>(w3cColorsMap.values());
    }
    
    /**
     *@return the w3c colors map
     */
    public Map<String, SVGW3CColor> getW3CColorsMap(){
        
        return new HashMap<String, SVGW3CColor>(w3cColorsMap);
    }
    
    /**
     * Returns the color corresponding to the given string
     * @param colorString a string representing a color
     * @return the color corresponding to the given string
     */
    protected Color getColor(String colorString){
        
        Color color=null;
        
        if(colorString==null){
            
            colorString=""; //$NON-NLS-1$
        }
        
        if(color==null){
            
            try{color=Color.getColor(colorString);}catch (Exception ex){color=null;}
            
            if(color==null && colorString.length()==7){
                
                int r=0, g=0, b=0;
                
                try{
                    r=Integer.decode("#"+colorString.substring(1,3)).intValue(); //$NON-NLS-1$
                    g=Integer.decode("#"+colorString.substring(3,5)).intValue(); //$NON-NLS-1$
                    b=Integer.decode("#"+colorString.substring(5,7)).intValue(); //$NON-NLS-1$
                    
                    color=new Color(r,g,b);
                }catch (Exception ex){color=null;}
                
            }else if(color==null && colorString.indexOf("rgb(")!=-1){ //$NON-NLS-1$
                
                String tmp=colorString.replaceAll("\\s*[rgb(]\\s*", ""); //$NON-NLS-1$ //$NON-NLS-2$
                
                tmp=tmp.replaceAll("\\s*[)]\\s*", ""); //$NON-NLS-1$ //$NON-NLS-2$
                tmp=tmp.replaceAll("\\s+", ","); //$NON-NLS-1$ //$NON-NLS-2$
                tmp=tmp.replaceAll("[,]+", ","); //$NON-NLS-1$ //$NON-NLS-2$
                
                int r=0, g=0, b=0;
                
                try{
                    r=new Integer(tmp.substring(0, tmp.indexOf(","))).intValue(); //$NON-NLS-1$
                    tmp=tmp.substring(tmp.indexOf(",")+1, tmp.length()); //$NON-NLS-1$
                    
                    g=new Integer(tmp.substring(0, tmp.indexOf(","))).intValue(); //$NON-NLS-1$
                    tmp=tmp.substring(tmp.indexOf(",")+1, tmp.length()); //$NON-NLS-1$
                    
                    b=new Integer(tmp).intValue();
                    
                    color=new Color(r,g,b);
                }catch (Exception ex){color=null;}
            }
        }

        return color;
    }
    
    /**
     * computes the path of a resource given its name
     * @param resource the name of a resource 
     * @return the full path of a resource
     */
    public static String getPath(String resource){
        
        String path=""; //$NON-NLS-1$
        
        try{
            path=SVGResource.class.getResource(resource).toExternalForm();
        }catch (Exception ex){path="";} //$NON-NLS-1$
        
        return path;
    }
    
    /**
     * gives an ImageIcon object given the name of it as it is witten in the SVGEditorIcons.properties file
     * @param name the name of an icon
     * @param isGrayIcon true if the icon should be used for a disabled widget
     * @return an image icon 
     */
    public static ImageIcon getIcon(String name, boolean isGrayIcon){
        
        ImageIcon icon=null;
        
        if(name!=null && ! name.equals("")){ //$NON-NLS-1$
            
            if(icons.containsKey(name)){
                
                if(isGrayIcon){
                    
                    icon=grayIcons.get(name);
                    
                }else{
                    
                    icon=icons.get(name);
                }
                
            }else{
                
                //gets the name of the icons from the resources
                ResourceBundle bundle=null;
                
                try{
                    bundle=ResourceBundle.getBundle("fr.itris.glips.svgeditor.resources.properties.SVGEditorIcons"); //$NON-NLS-1$
                }catch (Exception ex){bundle=null;}
                
                String path=""; //$NON-NLS-1$
                
                if(bundle!=null){
                    
                    try{path=bundle.getString(name);}catch (Exception ex){path="";} //$NON-NLS-1$
                    
                    if(path!=null && ! path.equals("")){ //$NON-NLS-1$
                        
                        try{
                            icon=new ImageIcon(new URL(getPath("icons/"+path))); //$NON-NLS-1$
                        }catch (Exception ex){icon=null;}

                        if(icon!=null){
                            
                            icons.put(name, icon);
                            Image image=icon.getImage();
                            
                            ImageIcon grayIcon=new ImageIcon(GrayFilter.createDisabledImage(image));
                            grayIcons.put(name, grayIcon);
                            
                            if(isGrayIcon){
                                
                                icon=grayIcon;
                            }
                        }
                    }
                }
            }
        }
        
        return icon;
    }
    
    /**
     * @return the current directory
     */
    public File getCurrentDirectory(){
        return currentDirectory;
    }
    
    /**
     * sets he current directory
     * @param directory a file representing a directory
     */
    public void setCurrentDirectory(File directory){
        currentDirectory=directory;
    }
    
    /**
     * create a document from tthe given file in the resource files
     * @param name the name of the xml file
     * @return the document
     */
    public static Document getXMLDocument(String name){
        
        Document doc=null;
        
        if(name!=null && ! name.equals("")){ //$NON-NLS-1$
            
            if(cachedXMLDocuments.containsKey(name)){
                
                doc=cachedXMLDocuments.get(name);
                
            }else{
                
                DocumentBuilderFactory docBuildFactory=DocumentBuilderFactory.newInstance();
                
                String path=""; //$NON-NLS-1$
                
                try{
                    //parses the XML file
                    DocumentBuilder docBuild=docBuildFactory.newDocumentBuilder();
                    path=getPath("xml/".concat(name)); //$NON-NLS-1$
                    doc=docBuild.parse(path);
                }catch (Exception ex){doc=null;}
                
                if(doc!=null){
                    
                    cachedXMLDocuments.put(name, doc);
                }
            }
        }
        
        return doc;
    }
    
    /**
     * @return the resource store document
     */
    public Document getResourceStore()
    {
    	if (resourceStore == null)
    	{
            try
            {
                final SAXSVGDocumentFactory factory =
                	new SAXSVGDocumentFactory(XMLReaderFactory.createXMLReader().getClass().getName());
                resourceStore=factory.createDocument(getPath("xml/visualResourceStore.xml")); //$NON-NLS-1$
            }
            catch (Exception ex)
            {
                resourceStore = null;
            }
    	}
    	
        return resourceStore;
    }
    
    /**
     * writes a documents to a file
     * @param path the path of the file in which the document will be written
     * @param doc the document to be written
     */
    public void writeXMLDocument(String path, Document doc){
        
        if(doc!=null && path!=null && ! path.equals("")){ //$NON-NLS-1$
            
            Element root=doc.getDocumentElement();
            
            if(root!=null){
                
                String res="<?xml version=\"1.0\" ?>"; //$NON-NLS-1$
                
                res=printChild(root, res);
                
                byte[] result=new byte[0];
                
                try{result=res.getBytes("UTF-8");}catch (Exception e) {} //$NON-NLS-1$
                
                //writes the string
                OutputStream writer=null;
                
                try{
                    writer=new BufferedOutputStream(new FileOutputStream(new URI(getPath("xml/".concat(path))).getPath())); //$NON-NLS-1$
                }catch (Exception ex){writer=null;}
                
                if(writer!=null){
                    
                    try{
                        writer.write(result, 0, result.length);
                        writer.flush();
                        writer.close();
                    }catch (Exception ex){}
                }
            }
        }
    }
    
    /**
     * the recursive function allowing to concat the string representation of the given node
     * @param node a node
     * @param str a string
     * @return the string representation of the subtree of the given node
     */
    protected String printChild(Node node, String str){
        
        String res=new String(str);
        
        if(node instanceof Element){
            
            //writes the node name
            res=res.concat("<"+node.getNodeName()+" "); //$NON-NLS-1$ //$NON-NLS-2$
            
            //writes the attributes
            NamedNodeMap att=node.getAttributes();
            Node at=null;
            
            for(int i=0;i<att.getLength();i++){
                
                at=att.item(i);
                
                if(at!=null){
                    
                    res=res.concat(at.getNodeName()+"=\""+at.getNodeValue()+"\" "); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            
            //writes the children
            Node cur=null;
            
            if(node.hasChildNodes()){
                
                res=res.concat(">"); //$NON-NLS-1$
                cur=node.getFirstChild();
                
            }else{
                
                res=res.concat("/>"); //$NON-NLS-1$
            }
            
            while(cur!=null){
                
                res=printChild(cur, res);
                
                cur=cur.getNextSibling();
            }
            
            if(node.hasChildNodes()){
                
                res=res.concat("</"+node.getNodeName()+">"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        
        return res;
    }
    
    /**
     * @return the collection of the recently opened files
     */
    public Collection<String> getRecentFiles(){
        
        return new LinkedList<String>(recentFiles);
    }
    
    /**
     * adds a new file to the list of the recent files
     * @param fileName the name of the file
     */
    public void addRecentFile(String fileName){
        
        if(recentFiles!=null && fileName!=null && ! fileName.equals("")){ //$NON-NLS-1$
            
        	if(recentFiles.contains(fileName)){
        		
        		//if the file name is already contained in the list, it is removed
        		recentFiles.remove(fileName);
        		
        	}else if(recentFiles.size()>5){
                
                recentFiles.removeLast();
            }
            
            recentFiles.addFirst(fileName);
        }
        
        saveRecentFiles();
    }
    
    /**
     * removes a the file that has the given name from the list of the recent files
     * @param fileName the name of the file
     */
    public void removeRecentFile(String fileName){
        
        if(recentFiles!=null && fileName!=null && ! fileName.equals("") && recentFiles.contains(fileName)){ //$NON-NLS-1$

            recentFiles.remove(fileName);
        }
        
        saveRecentFiles();
    }
    
    /**
     * saves the list of the recent files into a file
     */
    protected void saveRecentFiles(){
        
        if(preferences!=null && recentFiles!=null){
            
            //removing all the child nodes from the preference node
            String[] keys=null;
            
            try{keys=preferences.keys();}catch (Exception ex){}
            
            if(keys!=null){
                
                //filling the list of the recent files
                
                for(int i=0; i<keys.length; i++){
                    
                    preferences.remove(keys[i]);
                }
            }
            
            if(recentFiles.size()>0){
                
                String rf=""; //$NON-NLS-1$
                int n=0;
                
                for(Iterator  it=recentFiles.iterator(); it.hasNext();){
                    
                    try{rf=(String)it.next();}catch (Exception ex){}
                    
                    if(rf!=null && ! rf.equals("")){ //$NON-NLS-1$
                        
                        preferences.put(n+"", rf); //$NON-NLS-1$
                        n++;
                    }
                }
                
                try{preferences.flush();}catch (Exception ex){}
            }
        }
    }
    
    /**
     * @return the list of the style properties found in the properties.xml file
     */
    public HashSet<String> getAttributesToTranslate(){
        
        if(styleProperties.size()<=0){
            
            Document doc=null;
            
            if(xmlProperties!=null){
                
                doc=xmlProperties;
                
            }else{
                
                doc=getXMLDocument("properties.xml"); //$NON-NLS-1$
            }
            
            if(doc!=null){
                
                Node root=doc.getDocumentElement();
                
                if(root!=null){
                    
                    //the node iterator
                    Node node=null;
                    String str=""; //$NON-NLS-1$
                    
                    //for each property npde
                    for(NodeIterator it=new NodeIterator(root); it.hasNext();){
                        
                        try{node=it.next();}catch (Exception ex){node=null;}
                        
                        if(node!=null && node instanceof Element && node.getNodeName().equals("property")){ //$NON-NLS-1$
                            
                            //tests if the node is a style property
                            if(((Element)node).getAttribute("type")!=null && ((Element)node).getAttribute("type").equals("style")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                
                                str=((Element)node).getAttribute("name"); //$NON-NLS-1$
                                //gets the name of the stye property
                                if(str!=null && !str.equals("")){ //$NON-NLS-1$
                                    
                                    str=str.substring(9, str.length());
                                    styleProperties.add(str);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return styleProperties;
    }
    
    /**
     * adds a runnable to the list of the runnables that will be run when the editor is exited
     * @param runnable a runnable
     */
    public void addExitRunnable(Runnable runnable){
        
        synchronized(this){exitRunnables.add(runnable);}
    }
    
    /**
     * saves the editor's current state before it is exited
     */
    public void saveEditorsCurrentState(){
        
        //saves the current files list
        saveRecentFiles();
        
        //runs the list of the runnables
        LinkedList<Runnable> exitRun=new LinkedList<Runnable>(exitRunnables);
        
        for(Runnable runnable : exitRun){

            if(runnable!=null){
                
                runnable.run();
            }
        }
    }
    
    /**
     * the comparator for the colors
     * 
     * @author Jordi SUC
     */
    protected class ColorComparator implements Comparator{
        
        private static final int GREY_TYPE=0;
        private static final int BLUE_TYPE=1;
        private static final int MAGENTA_TYPE=2;
        private static final int RED_TYPE=3;
        private static final int YELLOW_TYPE=4;
        private static final int GREEN_TYPE=5;
        private static final int CYAN_TYPE=6;
        
        /**
         * returns the type of the color
         * @param color a color 
         * @return the type of the color
         */
        protected int getType(Color color){
            
            int type=-2;
            
            if(color!=null){
                
                int r=color.getRed(), g=color.getGreen(), b=color.getBlue();
                
                if(r==g && g==b){
                    
                    type=GREY_TYPE;
                    
                }else if(b>g && b>r){
                    
                    type=BLUE_TYPE;
                    
                }else if(b==r && r>g){
                    
                    type=MAGENTA_TYPE;
                    
                }else if(r>g && r>g){
                    
                    type=RED_TYPE;
                    
                }else if(r==g && r>b){
                    
                    type=YELLOW_TYPE;
                    
                }else if(g>r && g>b){
                    
                    type=GREEN_TYPE;
                    
                }else if(g==b && g>r){
                    
                    type=CYAN_TYPE;
                }
            }
            
            return type;
        }
        
        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object arg0, Object arg1) {
            
            if(arg0!=null && arg1!=null && arg0 instanceof Color && arg1 instanceof Color){
                
                Color color0=(Color)arg0, color1=(Color)arg1;
                
                int 	r0=color0.getRed(), g0=color0.getGreen(), b0=color0.getRed(),
                		r1=color1.getRed(), g1=color1.getGreen(), b1=color1.getBlue();
                
                float[] hsbVal0=new float[3], hsbVal1=new float[3];
                hsbVal0=Color.RGBtoHSB(r0, g0, b0, hsbVal0);
                hsbVal1=Color.RGBtoHSB(r1, g1, b1, hsbVal1);
                
                float hue0=hsbVal0[0], hue1=hsbVal1[0];
                float sat0=hsbVal0[1], sat1=hsbVal1[1];
                float br0=hsbVal0[2], br1=hsbVal1[2];

                if(Math.abs(hue1-hue0)>=0.5){
                	
                	if(hue1>hue0){
                		
                		return -1;
                		
                	}else if(hue1<hue0){
                		
                		return 1;
                		
                	}else{
                		
                		return 0;
                	}
                }
                
            	if(Math.abs(sat1-sat0)>=0.5){
            		
            		if(sat1>sat0){
            			
            			return 1;
            		}
                    
                    if(sat1<sat0){
            			
            			return -1;
            		}
                    
                    return 0;
            	}
            		
        		if(br1>br0){
        			
        			return 1;
        			
        		}else if(br1<br0){
        			
        			return -1;
        			
        		}else{
        			
        			return 0;
        		}
                
                /*if(hue0<hue1){
                	
                	return 1;
                	
                }else if(hue0>hue1){

                	return -1;
                	
                }else

                if(hue0!=type1){
                    
                    if(type0<type1){
                        
                        return -1;
                        
                    }else if(type0>type1){
                        
                        return 1;
                        
                    }else{
                        
                        return 0;
                    }
                    
                }else{

                    if(lum0<lum1){
                        
                        return 1;
                        
                    }else if(lum0>lum1){
                        
                        return -1;
                        
                    }else{
                        
                        return 0;
                    }
                }*/
            }
            
            return 0;
        }
    }
}


