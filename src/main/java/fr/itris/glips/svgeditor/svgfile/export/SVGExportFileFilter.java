package fr.itris.glips.svgeditor.svgfile.export;

import java.io.*;
import java.util.*;

/**
 * the class for filtering files in a JFileChooser
 * 
 * @author Jordi SUC
 */
public class SVGExportFileFilter extends javax.swing.filechooser.FileFilter {

    /**
     * the type of the filter
     */
    protected int type=-1;
    
    /**
     * the description of the filter
     */
    protected String messagefilter=""; //$NON-NLS-1$
    
    /**
     * the constructor of the class
     * @param a resource bundle
     * @param filterType the type of the filter
     */
    public SVGExportFileFilter(ResourceBundle bundle, int filterType) {

        this.type=filterType;
        
        if(bundle!=null){
            
            try{
                
                switch(type) {
                
                    case SVGExport.JPG_FORMAT :
                        
                        messagefilter=bundle.getString("messageexportfilterjpg"); //$NON-NLS-1$
                        break;
                        
                    case SVGExport.PNG_FORMAT :
                        
                        messagefilter=bundle.getString("messageexportfilterpng"); //$NON-NLS-1$
                        break;
                        
                    case SVGExport.BMP_FORMAT :
                        
                        messagefilter=bundle.getString("messageexportfilterbmp"); //$NON-NLS-1$
                        break;
                    
                    case SVGExport.PDF_FORMAT :
                        
                        messagefilter=bundle.getString("messageexportfilterpdf"); //$NON-NLS-1$
                        break;
                }

            }catch (Exception ex){}
        }
    }

    /**
     * Whether the given file is accepted by this filter
     * @param f a file
     * @return true to accept the file
     */
    public boolean accept(File f) {
        
        String name=f.getName().toLowerCase();
        
        boolean accept=false;
        
        if(f.isDirectory() || name.indexOf(".")==-1) { //$NON-NLS-1$
            
            accept=true;

        }else {
            
            switch(type) {
            
                case SVGExport.JPG_FORMAT :
                    
                    accept=name.endsWith(".jpg"); //$NON-NLS-1$
                    break;
                    
                case SVGExport.PNG_FORMAT :
                    
                    accept=name.endsWith(".png"); //$NON-NLS-1$
                    break;
                    
                case SVGExport.BMP_FORMAT :
                    
                    accept=name.endsWith(".bmp"); //$NON-NLS-1$
                    break;
                
                case SVGExport.PDF_FORMAT :
                    
                    accept=name.endsWith(".pdf"); //$NON-NLS-1$
                    break;
            }
        }
        
        return accept;
    }

    /**
     * The description of this filter
     * @return the description of the files that must be selected
     */
    public String getDescription() {

        return messagefilter;
    }
    
    /**
     * Whether the given file is accepted by this filter
     * @param f a file
     * @return true to accept the file
     */
    public boolean acceptFile(File f) {
        
        return accept(f);
    }
}
