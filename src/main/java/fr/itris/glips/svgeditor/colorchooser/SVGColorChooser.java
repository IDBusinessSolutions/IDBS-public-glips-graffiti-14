/*
 * Created on 22 févr. 2005
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
package fr.itris.glips.svgeditor.colorchooser;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;

/**
 * the class of the color chooser for this editor
 * 
 * @author Jordi SUC
 */
public class SVGColorChooser extends JColorChooser{
    
    /**
     * the editor
     */
    protected SVGEditor editor=null;
    
    /**
     * the flavor of a color
     */
    private DataFlavor colorFlavor=null;
    
    /**
     * the flavor of a svg w3c color
     */
    private DataFlavor w3cSVGColorFlavor=null;
    
    /**
     * the constructor of the class
     * @param editor the editor
     */
    public SVGColorChooser(SVGEditor editor){
        
        this.editor=editor;
        
        //adds the w3c standard colors chooser panel
        SVGW3CColorChooserPanel w3cColorChooserPanel=new SVGW3CColorChooserPanel(editor);
        addChooserPanel(w3cColorChooserPanel);
        
        //creating the color flavors
        try{
            colorFlavor=new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=java.awt.Color"); //$NON-NLS-1$
            w3cSVGColorFlavor=new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=fr.itris.glips.svgeditor.colorchooser.SVGW3CColor"); //$NON-NLS-1$
        }catch (Exception ex){colorFlavor=DataFlavor.stringFlavor; w3cSVGColorFlavor=DataFlavor.stringFlavor;}
    }
    
    /**
     * shows a color chooser dialog
     * @param initialColor the initial Color set when the color-chooser is shown
     * @return the selected color or <code>null</code> if the user opted out
     */
    public Color showColorChooserDialog(Color initialColor) {

    	setColor(initialColor);
        SVGColorTracker ok = new SVGColorTracker(this);
        JDialog dialog=createDialog(editor.getParent(), "", true, this, ok, null); //$NON-NLS-1$
        dialog.setVisible(true);

        return ok.getColor();
    }
    
    /**
     * Returns the color corresponding to the given string
     * @param frame a svg frame
     * @param colorString a string representing a color
     * @return the color corresponding to the given string
     */
    public Color getColor(SVGFrame frame, String colorString){
        
        Color color=null;
        
        if(colorString==null){
            
            colorString=""; //$NON-NLS-1$
            
        }else if(editor.getResource()!=null){
            
            //checking if the given string represents a w3c color
            color=frame.getSVGEditor().getResource().getW3CColorsMap().get(colorString);
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
     * Returns the string representation of the given color
     * @param color a color
     * @return the string representation of the given color
     */
    public String getColorString(Color color){
        
        if(color==null){
            
            color=Color.black;
        }
        
        if(color instanceof SVGW3CColor){
            
            return ((SVGW3CColor)color).getId();
        }
        
        String	sr=Integer.toHexString(color.getRed()),
        sg=Integer.toHexString(color.getGreen()),
        sb=Integer.toHexString(color.getBlue());
        
        if(sr.length()==1){
            
            sr="0".concat(sr); //$NON-NLS-1$
        }
        
        if(sg.length()==1){
            
            sg="0".concat(sg); //$NON-NLS-1$
        }
        
        if(sb.length()==1){
            
            sb="0".concat(sb); //$NON-NLS-1$
        }
        
        return (("#".concat(sr)).concat(sg)).concat(sb); //$NON-NLS-1$
    }
    
    /**
     * checks each color value in each attribute and in the style property of the given element
     * @param frame a frame
     * @param element an element
     */
    @SuppressWarnings(value="all")
    public void checkColorString(SVGFrame frame, Element element){
        
    }
    
    /**
     * returns the data flavor of the given color
     * @param color a color
     * @return the data flavor of the given color
     */
    public DataFlavor getColorFlavor(Color color){
        
        DataFlavor flavor=null;
        
        if(color!=null){
            
            if(color instanceof SVGW3CColor){
                
                flavor=w3cSVGColorFlavor;
                
            }else{
            	
                flavor=colorFlavor;
            }
        }
        
        return flavor;
    }
    
    /**
     * returns whether the given flavor is a color flavor
     * @param flavor a flavor
     * @return whether the given flavor is a color flavor
     */
    public boolean isColorDataFlavor(DataFlavor flavor){
        
        boolean isColorDataFlavor=false;
        
        if(flavor!=null){
            
            isColorDataFlavor=(flavor.isMimeTypeEqual(colorFlavor) || flavor.isMimeTypeEqual(w3cSVGColorFlavor));
        }
        
        return isColorDataFlavor;
    }
    
    /**
     * @return the list of the data flavors
     */
    public Collection<DataFlavor> getColorDataFlavors(){
        
        LinkedList<DataFlavor> dataFlavors=new LinkedList<DataFlavor>();
        
        dataFlavors.add(colorFlavor);
        dataFlavors.add(w3cSVGColorFlavor);
        
        return dataFlavors;
    }
    
    /**
     * disposes the colors and blinking of the canvas linked with the given project file, if no
     * other canvas is linked to this project
     * @param projectFile a project file
     */
    @SuppressWarnings(value="all")
    public void disposeColorsAndBlinkings(File projectFile) { }
    
    /**
     * the project file corresponding to the given uri
     * @param uri a uri
     * @return the project file corresponding to the given uri
     */
    @SuppressWarnings(value="all")
    public File getProjectFile(String uri) {
    	
    	return null;
    }
    
    /**
     * @return Returns the editor.
     */
    public SVGEditor getSVGEditor() {
        return editor;
    }
    
    /**
     * the class of the svg color tracker
     * @author Jordi SUC
     */
    protected class SVGColorTracker implements ActionListener {
    	
    	/**
    	 * the color chooser
    	 */
       private JColorChooser chooser;
        
       /**
        * the color
        */
       private Color color;

       /**
        * the constructor of the class
        * @param c the color chooser
        */
        public SVGColorTracker(JColorChooser c) {
        	
            chooser=c;
        }

        public void actionPerformed(ActionEvent e) {
            color=chooser.getColor();
        }

        /**
         * @return the color
         */
        public Color getColor() {
            return color;
        }
    }
}
