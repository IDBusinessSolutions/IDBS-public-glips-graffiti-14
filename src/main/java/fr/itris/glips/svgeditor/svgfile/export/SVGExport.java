/*
 * Created on 8 déc. 2004
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

package fr.itris.glips.svgeditor.svgfile.export;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.imageio.*;
import javax.imageio.event.*;
import javax.imageio.plugins.bmp.*;
import javax.imageio.plugins.jpeg.*;
import javax.imageio.stream.*;
import javax.swing.*;

import org.apache.batik.gvt.*;
import org.apache.batik.swing.*;
import org.apache.batik.swing.gvt.*;
import org.apache.batik.swing.svg.*;
import org.w3c.dom.svg.*;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.sun.imageio.plugins.bmp.*;
import com.sun.imageio.plugins.jpeg.*;
import com.sun.imageio.plugins.png.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.resources.*;

/**
 * the class that creates the static menu item export in the menu bar
 * 
 * @author Jordi SUC
 */
public class SVGExport extends SVGModuleAdapter{
    
    /**
     * the ids of the module
     */
    final private String	idexport="Export"; //$NON-NLS-1$
    
    /**
     * the constant for the JPG format
     */
    public static final int JPG_FORMAT=0;
    
    /**
     * the constant for the PNG format
     */
    public static final int PNG_FORMAT=1;
    
    /**
     * the constant for the BMP format
     */
    public static final int BMP_FORMAT=2;
    
    /**
     * the constant for the GIF format
     */
    public static final int GIF_FORMAT=3;
    
    /**
     * the constant for the PDF format
     */
    public static final int PDF_FORMAT=4;
    
    /**
     * the labels
     */
    private String labelexport="", erasefilemessage="", erasefiletitle="",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    messageexporterror="", labelerror="", messageformaterrorexportjpg="",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    messageformaterrorexportpng="", messageformaterrorexportbmp="",   //$NON-NLS-1$ //$NON-NLS-2$
    messageformaterrorexportpdf="", labeljpg="", labelpng="", labelbmp="", labelpdf=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    
    /**
     * the editor
     */
    private SVGEditor editor;
    
    /**
     * the menu item that will be added to the menubar
     */
    private JMenu export;
    
    /**
     * the menu items used to choose the export file type
     */
    private JMenuItem jpgMenuItem, pngMenuItem, bmpMenuItem, pdfMenuItem;
    
    /**
     * the dialog box used to choose the parameters for the jpg export
     */
    private JPGExportDialog jpgExportDialog=null;
    
    /**
     * the dialog box used to choose the parameters for the jpg export
     */
    private PNGExportDialog pngExportDialog=null;
    
    /**
     * the dialog box used to choose the parameters for the bmp export
     */
    private BMPExportDialog bmpExportDialog=null;
    
    /**
     * the dialog box used to choose the parameters for the pdf export
     */
    private PDFExportDialog pdfExportDialog=null;
    
    /**
     * the resource bundle
     */
    private ResourceBundle bundle=null;
    
    /**
     * the constructor of the class
     * @param editor the editor
     */
    public SVGExport(SVGEditor theEditor)
    {        
        this.editor = theEditor;
        
        //gets the labels from the resources
        bundle=SVGEditor.getBundle();
        
        if(bundle!=null){
            
            try{
                labelexport=bundle.getString("labelexport"); //$NON-NLS-1$
                labeljpg=bundle.getString("labeljpgexportmi"); //$NON-NLS-1$
                labelpng=bundle.getString("labelpngexportmi"); //$NON-NLS-1$
                labelbmp=bundle.getString("labelbmpexportmi"); //$NON-NLS-1$
                labelpdf=bundle.getString("labelpdfexportmi"); //$NON-NLS-1$
                erasefilemessage=bundle.getString("erasefilemessage"); //$NON-NLS-1$
                erasefiletitle=bundle.getString("erasefiletitle"); //$NON-NLS-1$
                messageexporterror=bundle.getString("messageexporterror"); //$NON-NLS-1$
                labelerror=bundle.getString("labelerror"); //$NON-NLS-1$
                messageformaterrorexportjpg=bundle.getString("messageformaterrorexportjpg"); //$NON-NLS-1$
                messageformaterrorexportpng=bundle.getString("messageformaterrorexportpng"); //$NON-NLS-1$
                messageformaterrorexportbmp=bundle.getString("messageformaterrorexportbmp"); //$NON-NLS-1$
                messageformaterrorexportpdf=bundle.getString("messageformaterrorexportpdf"); //$NON-NLS-1$
            }catch (Exception ex){}
        }
        
        //a listener that listens to the changes of the SVGFrames
        final ActionListener svgframeListener=new ActionListener(){
            
            public void actionPerformed(ActionEvent e) {
                
                final SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
                
                if(frame!=null){
                    
                    //enables the menuitem
                    export.setEnabled(true);
                    
                }else{
                    
                    //disables the menuitem
                    export.setEnabled(false);
                }
            }	
        };
        
        //adds the SVGFrame change listener
        editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);
        
        //getting the icons
        ImageIcon exportIcon=SVGResource.getIcon("Export", false), //$NON-NLS-1$
        dexportIcon=SVGResource.getIcon("Export", true); //$NON-NLS-1$
        
        //creating the menu items
        jpgMenuItem=new JMenuItem(labeljpg);
        pngMenuItem=new JMenuItem(labelpng);
        bmpMenuItem=new JMenuItem(labelbmp);
        pdfMenuItem=new JMenuItem(labelpdf);
        
        //handling the menu
        export=new JMenu(labelexport);
        
        export.add(jpgMenuItem);
        export.add(pngMenuItem);
        export.add(bmpMenuItem);
        export.add(pdfMenuItem);
        
        export.setIcon(exportIcon);
        export.setDisabledIcon(dexportIcon);
        
        //disabled by default since no SVGFrame has been loaded
        export.setEnabled(false);
        
        //creating and adding a  listener to the menu items
        ActionListener listener=new ActionListener() {
            
            public void actionPerformed(ActionEvent evt) {
                
                SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
                
                if(frame!=null){
                    
                    if(evt.getSource().equals(jpgMenuItem)) {
                        
                        exportAction(frame, JPG_FORMAT);
                        
                    }else if(evt.getSource().equals(pngMenuItem)) {
                        
                        exportAction(frame, PNG_FORMAT);
                        
                    }else if(evt.getSource().equals(bmpMenuItem)) {
                        
                        exportAction(frame, BMP_FORMAT);
                        
                    }else if(evt.getSource().equals(pdfMenuItem)) {
                        
                        exportAction(frame, PDF_FORMAT);
                    }
                }
            } 
        };
        
        jpgMenuItem.addActionListener(listener);
        pngMenuItem.addActionListener(listener);
        bmpMenuItem.addActionListener(listener);
        pdfMenuItem.addActionListener(listener);
        
        //creating the export dialog boxes
        if(getSVGEditor().getParent() instanceof JFrame){
        	
            jpgExportDialog=new JPGExportDialog(getSVGEditor(), (JFrame)getSVGEditor().getParent());
            pngExportDialog=new PNGExportDialog(getSVGEditor(), (JFrame)getSVGEditor().getParent());
            bmpExportDialog=new BMPExportDialog(getSVGEditor(), (JFrame)getSVGEditor().getParent());
            pdfExportDialog=new PDFExportDialog(getSVGEditor(), (JFrame)getSVGEditor().getParent());
        	
        }else{
        	
            jpgExportDialog=new JPGExportDialog(getSVGEditor(), new JFrame("")); //$NON-NLS-1$
            pngExportDialog=new PNGExportDialog(getSVGEditor(), new JFrame("")); //$NON-NLS-1$
            bmpExportDialog=new BMPExportDialog(getSVGEditor(), new JFrame("")); //$NON-NLS-1$
            pdfExportDialog=new PDFExportDialog(getSVGEditor(), new JFrame("")); //$NON-NLS-1$
        }
    }
    
    /**
     * @return the editor
     */
    public SVGEditor getSVGEditor(){
        return editor;
    }
    
    /**
     * @return a map associating a menu item id to its menu item object
     */
    public Hashtable getMenuItems(){
        
        Hashtable menuItems=new Hashtable();
        menuItems.put(idexport, export);
        
        return menuItems;
    }
    
    /**
     * @return a map associating a tool item id to its tool item object
     */
    public Hashtable getToolItems(){return null;}
    
    /**
     * Returns the collection of the popup items
     * @return the collection of the popup items
     */
    public Collection getPopupItems(){return null;}
    
    /**
     * layout some elements in the module
     */
    public void initialize(){}
    
    /**
     * @see fr.itris.glips.svgeditor.SVGModuleAdapter#cancelActions()
     */
    public void cancelActions() {}
    
    /**
     * returns the message warning that there is an error in the format
     * @param exportType the type of the export
     * @return the message warning that there is an error in the format
     */
    protected String getMessageFormatExportError(int exportType) {
        
        String formatErrorMessage=""; //$NON-NLS-1$
        
        switch(exportType) {
        
        case JPG_FORMAT :
            
            formatErrorMessage=messageformaterrorexportjpg;
            break;
            
        case PNG_FORMAT :
            
            formatErrorMessage=messageformaterrorexportpng;
            break;
            
        case BMP_FORMAT :
            
            formatErrorMessage=messageformaterrorexportbmp;
            break;
        case PDF_FORMAT :
            
            formatErrorMessage=messageformaterrorexportpdf;
            break;
        }
        
        return formatErrorMessage;
    }
    
    /**
     * exports the svg file represented by the given frame
     * @param frame a frame
     * @param exportType the type of the export action
     */
    protected void exportAction(final SVGFrame frame, final int exportType){
        
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                
                if(frame!=null){
                    
                    //opens a file chooser
                    JFileChooser fileChooser=new JFileChooser();
                    
                    //getting the title of the file chooser
                    String fileChooserTitle=""; //$NON-NLS-1$
                    
                    //getting the extension
                    String extension=""; //$NON-NLS-1$
                    
                    switch(exportType) {
                    
                    case JPG_FORMAT :
                        
                        extension=".jpg"; //$NON-NLS-1$
                        fileChooserTitle=jpgExportDialog.getExportDialogTitle();
                        break;
                        
                    case PNG_FORMAT :
                        
                        extension=".png"; //$NON-NLS-1$
                        fileChooserTitle=pngExportDialog.getExportDialogTitle();
                        break;
                        
                    case BMP_FORMAT :
                        
                        extension=".bmp"; //$NON-NLS-1$
                        fileChooserTitle=bmpExportDialog.getExportDialogTitle();
                        break;
                        
                    case PDF_FORMAT :
                        
                        extension=".pdf"; //$NON-NLS-1$
                        fileChooserTitle=pdfExportDialog.getExportDialogTitle();
                        break;
                    }
                    
                    fileChooser.setDialogTitle(fileChooserTitle);
                    
                    if(getSVGEditor().getResource().getCurrentDirectory()!=null){
                        
                        fileChooser.setCurrentDirectory(getSVGEditor().getResource().getCurrentDirectory());
                    }
                    
                    SVGExportFileFilter fileFilter=new SVGExportFileFilter(bundle, exportType);
                    fileChooser.setFileFilter(fileFilter);
                    fileChooser.setMultiSelectionEnabled(false); 
                    
                    File selectedFile=null;
                    int returnVal=0, rVal=-1;
                    
                    do{
                        
                        returnVal=fileChooser.showSaveDialog(getSVGEditor().getParent());
                        
                        if(returnVal==JFileChooser.APPROVE_OPTION) {
                            
                            getSVGEditor().getResource().setCurrentDirectory(fileChooser.getCurrentDirectory());
                            selectedFile=fileChooser.getSelectedFile();
                            
                            if(selectedFile!=null && fileFilter.acceptFile(selectedFile)){
                                
                                //checking if the selected file already exists, if the entered file has not extension, it is added
                                try {
                                    String path=selectedFile.toURI().toString();
                                    
                                    if(! path.endsWith(extension)) {
                                        
                                        path+=extension;
                                    }
                                    
                                    selectedFile=new File(new URI(path));
                                    
                                }catch (Exception ex) {}
                                
                                if(selectedFile.exists()){
                                    
                                    //if the file exist prompts a dialog to confirm that the file will be erased
                                    rVal=JOptionPane.showConfirmDialog(
                                            getSVGEditor().getParent(), 
                                            erasefilemessage, 
                                            erasefiletitle, 
                                            JOptionPane.YES_NO_OPTION);
                                    
                                    //if the file should be erased
                                    if(rVal==JOptionPane.YES_OPTION){
                                        
                                        export(frame, selectedFile.toURI().toString(), true, exportType);
                                        break;
                                    }
                                    
                                }else{
                                    
                                    //if the file does not already exists
                                    export(frame, selectedFile.toURI().toString(), false, exportType);
                                    break;
                                }
                                
                            }else if(selectedFile!=null){
                                
                                JOptionPane.showMessageDialog(getSVGEditor().getParent(), getMessageFormatExportError(exportType), 
                                        labelerror,JOptionPane.ERROR_MESSAGE);
                                
                            }else{
                                
                                JOptionPane.showMessageDialog(getSVGEditor().getParent(), messageexporterror, labelerror, 
                                        JOptionPane.ERROR_MESSAGE);
                                break;
                            }
                            
                        }else{
                            
                            break;
                        }
                        
                    }while(selectedFile!=null);
                }
            }
        });
    }
    
    /**
     * exports the document corresponding to the frame to the given destination file
     * @param frame a frame
     * @param path the destination file path
     * @param alreadyExist whether the destination file already exists
     * @param exportType the type of the export action
     */
    protected void export(SVGFrame frame, String path, boolean alreadyExist, int exportType){
        
        //whether the export has been done
        boolean hasExported=false;
        
        if(frame!=null && path!=null && ! path.equals("")){ //$NON-NLS-1$

            //exporting into the specified format
            int res=-1;
            
            //getting the title of the progress bar
            String progressBarDialogTitle=""; //$NON-NLS-1$
            
            //the extension
            String extension=""; //$NON-NLS-1$
            
            switch(exportType) {
            
            case JPG_FORMAT :
                
                extension=".jpg"; //$NON-NLS-1$
                
                progressBarDialogTitle=jpgExportDialog.getExportDialogTitle();
                break;
                
            case PNG_FORMAT :
                
                extension=".png"; //$NON-NLS-1$
                
                progressBarDialogTitle=pngExportDialog.getExportDialogTitle();
                break;
                
            case BMP_FORMAT :
                
                extension=".bmp"; //$NON-NLS-1$
                
                progressBarDialogTitle=bmpExportDialog.getExportDialogTitle();
                break;
                
            case PDF_FORMAT :
                
                extension=".pdf"; //$NON-NLS-1$
                
                progressBarDialogTitle=pdfExportDialog.getExportDialogTitle();
                break;
            }
            
            //handling the path
            if(! path.endsWith(extension)){ 
                
                path+=extension;
            }
            
            final String fpath=path;
            
            //the progress bar
            SVGProgressBarDialog pb=null;
            
            if(getSVGEditor().getParent() instanceof JFrame){
            	
            	pb=new SVGProgressBarDialog((JFrame)getSVGEditor().getParent(), progressBarDialogTitle);
            	
            }else{
            	
            	pb=new SVGProgressBarDialog(new JFrame(""), progressBarDialogTitle); //$NON-NLS-1$
            }
            
            final SVGProgressBarDialog progressBar=pb;
            
            switch(exportType) {
            
            case JPG_FORMAT :
                
                res=jpgExportDialog.showExportDialog(frame);
                
                if(res==ExportDialog.OK_ACTION){
                    
                    //initializing the progress bar and showing it
                    progressBar.setIndeterminate(true, true);
                    progressBar.setVisible(true);
                    
                    final double width=jpgExportDialog.getExportSize().x, height=jpgExportDialog.getExportSize().y;
                    final float jpgQuality=jpgExportDialog.getJpgQuality();
                    final boolean  isOptimized=jpgExportDialog.isOptimized(), 
                    isProgressive=jpgExportDialog.isProgressive();
                    
                    //creating the canvas that will be used to generate the pdf
                    new ExportCanvas(frame.getScrollPane().getSVGCanvas().getDocument(), progressBar) {
                        
                        protected void doWhenRenderingDone() {
                            
                            try{
                                
                                //creating the image that will be written
                                BufferedImage wImage=new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_RGB);
                                
                                Graphics2D g2=(Graphics2D)wImage.getGraphics();
                                
                                //getting the graphics node that will be painted
                                GraphicsNode gn=getGraphicsNode();
                                
                                //computing the graphics transformation so that the image is scaled to the defined size
                                Dimension canvasSize=getCanvasSize();
                                gn.setTransform(AffineTransform.getScaleInstance(width/canvasSize.width, height/canvasSize.height));
                                
                                //painting the image
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                                g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                                g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                                g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
                                g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                                
                                g2.setColor(Color.white);
                                g2.fillRect(0, 0, (int)width, (int)height);
                                gn.paint(g2);
                                
                                //creating the IIOImage
                                IIOImage iioImage=new IIOImage(wImage, null, null);
                                
                                Iterator<ImageWriter> it=ImageIO.getImageWritersByMIMEType("image/jpeg"); //$NON-NLS-1$
                                
                                //getting the image writer
                                ImageWriter w=null;
                                
                                while(it.hasNext()) {
                                    
                                    w=it.next();
                                    
                                    if(w instanceof JPEGImageWriter) {
                                        
                                        break;
                                    }
                                }
                                
                                final ImageWriter writer=w;
                                
                                //setting the parameters for the jpeg transcoding
                                JPEGImageWriteParam params=new JPEGImageWriteParam(Locale.getDefault());
                                params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                                params.setCompressionQuality(jpgQuality);
                                params.setOptimizeHuffmanTables(isOptimized);
                                
                                if(isProgressive) {
                                    
                                    params.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
                                    
                                }else {
                                    
                                    params.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
                                }
                                
                                //writing the image
                                ImageOutputStream out=ImageIO.createImageOutputStream(new File(new URI(fpath)));
                                writer.setOutput(out);
                                
                                //adding the listener to the writer
                                writer.addIIOWriteProgressListener(new IIOWriteProgressListener() {
                                    
                                    public void imageComplete(ImageWriter wr) {
                                        
                                        writer.removeIIOWriteProgressListener(this);
                                        progressBar.setVisible(false);
                                    }
                                    
                                    public void writeAborted(ImageWriter wr) {
                                        
                                        writer.removeIIOWriteProgressListener(this);
                                        progressBar.setVisible(false);
                                    }
                                    
                                    public void imageProgress(ImageWriter wr, float value) {

                                        progressBar.setProgressBarValue(25+3*value/4, 0, 100);
                                        
                                        if(progressBar.cancelAction()) {
                                            
                                            writer.abort();
                                            
                                            JOptionPane.showMessageDialog(  getSVGEditor().getParent(), 
                                                    messageexporterror, labelerror, JOptionPane.WARNING_MESSAGE);    
                                        }
                                    }

                                    public void imageStarted(ImageWriter arg0, int arg1) {}

                                    public void thumbnailComplete(ImageWriter wr) {}
                                    
                                    public void thumbnailProgress(ImageWriter wr, float arg1) {}
                                    
                                    public void thumbnailStarted(ImageWriter wr, int arg1, int arg2) {}
                                });
                                
                                writer.write(null, iioImage, params);
                                
                                //cleaning up
                                out.flush();
                                writer.removeAllIIOWriteProgressListeners();
                                progressBar.setVisible(false);
                                writer.dispose();
                                out.close();
                                
                            }catch (Exception ex){
                                
                                progressBar.setVisible(false);
                                
                                SwingUtilities.invokeLater(new Runnable() {
                                    
                                    public void run() {
                                        
                                        JOptionPane.showMessageDialog(  getSVGEditor().getParent(), 
                                                messageexporterror, labelerror, JOptionPane.WARNING_MESSAGE);                                                
                                    }
                                });
                                
                                try{
                                    //removing the file that had to be written
                                    new File(new URI(fpath)).delete();
                                }catch (Exception ex2) {}
                            }
                        }
                    };
                }
                
                break;
                
            case PNG_FORMAT :
                
                res=pngExportDialog.showExportDialog(frame);
                
                if(res==ExportDialog.OK_ACTION){
                    
                    //initializing the progress bar and showing it
                    progressBar.setIndeterminate(true, true);
                    progressBar.setVisible(true);
                    
                    final double width=pngExportDialog.getExportSize().x, height=pngExportDialog.getExportSize().y;
                    /*final int imageType=pngExportDialog.getImageType();
                    final int pngQuality=pngExportDialog.getPngBitDepths();*/
                    
                    //creating the canvas that will be used to generate the pdf
                    new ExportCanvas(frame.getScrollPane().getSVGCanvas().getDocument(), progressBar) {
                        
                    	@Override
                        protected void doWhenRenderingDone() {
                            
                            try{
                                
                                //creating the image that will be written
                                BufferedImage wImage=new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_RGB);
                                
                                Graphics2D g2=(Graphics2D)wImage.getGraphics();
                                
                                //getting the graphics node that will be painted
                                GraphicsNode gn=getGraphicsNode();
                                
                                //computing the graphics transformation so that the image is scaled to the defined size
                                Dimension canvasSize=getCanvasSize();
                                gn.setTransform(AffineTransform.getScaleInstance(width/canvasSize.width, height/canvasSize.height));
                                
                                //painting the image
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                                g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                                g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                                g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
                                g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                                
                                g2.setColor(Color.white);
                                g2.fillRect(0, 0, (int)width, (int)height);
                                gn.paint(g2);
                                
                                //creating the IIOImage
                                IIOImage iioImage=new IIOImage(wImage, null, null);
                                
                                Iterator<ImageWriter> it=ImageIO.getImageWritersByMIMEType("image/png"); //$NON-NLS-1$
                                
                                ImageWriter w=null;
                                
                                while(it.hasNext()) {
                                    
                                    w=it.next();
                                    
                                    if(w instanceof PNGImageWriter) {
                                        
                                        break;
                                    }
                                }
                                
                                final ImageWriter writer=w;
                                
                                //writing the image
                                ImageOutputStream out=ImageIO.createImageOutputStream(new File(new URI(fpath)));
                                writer.setOutput(out);
                                
                                //adding the listener to the writer
                                writer.addIIOWriteProgressListener(new IIOWriteProgressListener() {
                                    
                                    public void imageComplete(ImageWriter wr) {
                                        
                                        writer.removeIIOWriteProgressListener(this);
                                        progressBar.setVisible(false);
                                    }
                                    
                                    public void writeAborted(ImageWriter wr) {
                                        
                                        writer.removeIIOWriteProgressListener(this);
                                        progressBar.setVisible(false);
                                    }
                                    
                                    public void imageProgress(ImageWriter wr, float value) {
                                        
                                        progressBar.setProgressBarValue(25+3*value/4, 0, 100);
                                        
                                        if(progressBar.cancelAction()) {
                                            
                                            writer.abort();
                                            
                                            JOptionPane.showMessageDialog(  getSVGEditor().getParent(), 
                                                    messageexporterror, labelerror, JOptionPane.WARNING_MESSAGE);    
                                        }
                                    }
                                    
                                    public void imageStarted(ImageWriter wr, int arg1) {}
                                    
                                    public void thumbnailComplete(ImageWriter wr) {}
                                    
                                    public void thumbnailProgress(ImageWriter wr, float arg1) {}
                                    
                                    public void thumbnailStarted(ImageWriter wr, int arg1, int arg2) {}
                                });
                                
                                writer.write(null, iioImage, writer.getDefaultWriteParam());
                                
                                //cleaning up
                                out.flush();
                                writer.removeAllIIOWriteProgressListeners();
                                progressBar.setVisible(false);
                                writer.dispose();
                                out.close();
                                
                            }catch (Exception ex){
                                ex.printStackTrace();
                                
                                progressBar.setVisible(false);
                                
                                SwingUtilities.invokeLater(new Runnable() {
                                    
                                    public void run() {
                                        
                                        JOptionPane.showMessageDialog(  getSVGEditor().getParent(), 
                                                messageexporterror, labelerror, JOptionPane.WARNING_MESSAGE);                                                
                                    }
                                });
                                
                                try{
                                    //removing the file that had to be written
                                    new File(new URI(fpath)).delete();
                                }catch (Exception ex2) {}
                            }
                        }
                    };
                    
                }
                
                break;
                
            case BMP_FORMAT :
                
                res=bmpExportDialog.showExportDialog(frame);
                
                if(res==ExportDialog.OK_ACTION){
                    
                    //initializing the progress bar and showing it
                    progressBar.setIndeterminate(true, true);
                    progressBar.setVisible(true);
                    
                    final double width=bmpExportDialog.getExportSize().x, height=bmpExportDialog.getExportSize().y;
                    final boolean usePalette=bmpExportDialog.isUsePalette();
                    
                    //creating the canvas that will be used to generate the pdf
                    new ExportCanvas(frame.getScrollPane().getSVGCanvas().getDocument(), progressBar) {
                        
                        protected void doWhenRenderingDone() {

                            try{
                                
                                int type=BufferedImage.TYPE_3BYTE_BGR;
                                
                                if(usePalette) {
                                    
                                    type=BufferedImage.TYPE_BYTE_INDEXED;
                                }
                                
                                //creating the image that will be written
                                BufferedImage wImage=new BufferedImage((int)width, (int)height, type);
                                
                                Graphics2D g2=(Graphics2D)wImage.getGraphics();
                                
                                //getting the graphics node that will be painted
                                GraphicsNode gn=getGraphicsNode();
                                
                                //computing the graphics transformation so that the image is scaled to the defined size
                                Dimension canvasSize=getCanvasSize();
                                gn.setTransform(AffineTransform.getScaleInstance(width/canvasSize.width, height/canvasSize.height));
                                
                                //painting the image
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                                g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                                g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                                g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
                                g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                                
                                g2.setColor(Color.white);
                                g2.fillRect(0, 0, (int)width, (int)height);
                                gn.paint(g2);
                                
                                //creating the IIOImage
                                IIOImage iioImage=new IIOImage(wImage, null, null);
                                
                                Iterator<ImageWriter> it=ImageIO.getImageWritersByMIMEType("image/bmp"); //$NON-NLS-1$
                                ImageWriter w=null;
                                
                                while(it.hasNext()) {
                                    
                                    w=it.next();
                                    
                                    if(w instanceof BMPImageWriter) {
                                        
                                        break;
                                    }
                                }
                                
                                //getting the image writer
                                final ImageWriter writer=w;

                                BMPImageWriteParam param=new BMPImageWriteParam();
                                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                                
                                if(usePalette) {
                                    
                                    param.setCompressionType("BI_RGB"); //$NON-NLS-1$
                                    
                                }else {
                                    
                                    param.setCompressionType("BI_BITFIELDS"); //$NON-NLS-1$
                                }
                                
                                ImageOutputStream out=ImageIO.createImageOutputStream(new RandomAccessFile(new File(new URI(fpath)), "rw")); //$NON-NLS-1$
                                
                                //writing the image
                                writer.setOutput(out);
                                
                                //adding the listener to the writer
                                writer.addIIOWriteProgressListener(new IIOWriteProgressListener() {
                                    
                                    public void imageComplete(ImageWriter wr) {
                                        
                                        writer.removeIIOWriteProgressListener(this);
                                        progressBar.setVisible(false);
                                    }
                                    
                                    public void writeAborted(ImageWriter wr) {
                                        
                                        writer.removeIIOWriteProgressListener(this);
                                        progressBar.setVisible(false);
                                    }
                                    
                                    public void imageProgress(ImageWriter wr, float value) {

                                        progressBar.setProgressBarValue(25+3*value/4, 0, 100);
                                        
                                        if(progressBar.cancelAction()) {
                                            
                                            writer.abort();
                                            
                                            JOptionPane.showMessageDialog(  getSVGEditor().getParent(), 
                                                    messageexporterror, labelerror, JOptionPane.WARNING_MESSAGE);    
                                        }
                                    }
                                    
                                    public void imageStarted(ImageWriter wr, int arg1) {}
                                    
                                    public void thumbnailComplete(ImageWriter wr) {}
                                    
                                    public void thumbnailProgress(ImageWriter wr, float arg1) {}
                                    
                                    public void thumbnailStarted(ImageWriter wr, int arg1, int arg2) {}
                                });
                                
                                writer.write(null, iioImage, param);
                                
                                //cleaning up
                                out.flush();
                                writer.removeAllIIOWriteProgressListeners();
                                progressBar.setVisible(false);
                                writer.dispose();
                                out.close();
                                
                            }catch (Exception ex){
                                ex.printStackTrace();
                                
                                progressBar.setVisible(false);
                                
                                SwingUtilities.invokeLater(new Runnable() {
                                    
                                    public void run() {
                                        
                                        JOptionPane.showMessageDialog(  getSVGEditor().getParent(), 
                                                messageexporterror, labelerror, JOptionPane.WARNING_MESSAGE);                                                
                                    }
                                });
                                
                                try{
                                    //removing the file that had to be written
                                    new File(new URI(fpath)).delete();
                                }catch (Exception ex2) {}
                            }
                        }
                    };
                    
                }
                
                break;

            case PDF_FORMAT :
                
                //getting the compression quality
                res=pdfExportDialog.showExportDialog(frame);
                
                if(res==ExportDialog.OK_ACTION) {
                    
                    //initializing the progress bar and showing it
                    progressBar.setIndeterminate(true, true);
                    progressBar.setVisible(true);
                    
                    //getting all information
                    final com.lowagie.text.Rectangle pageSize=pdfExportDialog.getPageSize();
                    final boolean isPortrait=pdfExportDialog.isPortrait();
                    final Insets margin=pdfExportDialog.getMargins();
                    final String title=pdfExportDialog.getPDFTitle(),
                    author=pdfExportDialog.getAuthor(),
                    subject=pdfExportDialog.getSubject(),
                    keywords=pdfExportDialog.getKeywords(),
                    creator=pdfExportDialog.getCreator();
                    
                    //creating the canvas that will be used to generate the pdf file
                    new ExportCanvas(frame.getScrollPane().getSVGCanvas().getDocument(), progressBar) {
                        
                        /**
                         * whether the pdf document is open or not
                         */
                        protected boolean pdfDocOpen=false;
                        
                        @Override
                        protected void doWhenRenderingDone() {
                            
                            //setting that the state of the progress bar is indeterminate
                            progressBar.setIndeterminate(true, false);
                            
                            try{
                                //the output stream used to write the pdf file
                                final BufferedOutputStream out=new BufferedOutputStream(
                                        new FileOutputStream(new File(new URI(fpath))));
                                
                                com.lowagie.text.Rectangle rect=pageSize;
                                
                                //setting if the orientation of the pdf
                                rect=isPortrait?rect:rect.rotate();
                                
                                //creating the pdf document and the writer
                                final com.lowagie.text.Document pdfDoc=new Document(  rect, margin.left, margin.right,
                                                                                                                                   margin.top, margin.bottom) {
                                    public void open() {
                                        
                                        super.open();
                                        pdfDocOpen=true;
                                    }
                                    
                                    public void close() {
                                        
                                        try{super.close();}catch (Exception ex) {}
                                        pdfDocOpen=false;
                                    }
                                };

                                //adding meta data
                                pdfDoc.addTitle(title);
                                pdfDoc.addAuthor(author);
                                pdfDoc.addSubject(subject);
                                pdfDoc.addKeywords(keywords);
                                pdfDoc.addCreator(creator);
                                
                                PdfWriter writer=PdfWriter.getInstance(pdfDoc, out);
  
                                pdfDoc.open();

                                //getting the pdf graphics
                                PdfContentByte cb=writer.getDirectContent();
                                final PdfTemplate tp=cb.createTemplate(rect.getWidth(), rect.getHeight());
                                tp.setWidth(rect.getWidth());
                                tp.setHeight(rect.getHeight());
                                
                                Graphics2D g2=tp.createGraphics(rect.getWidth(), rect.getHeight(), new DefaultFontMapper());
                                
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                                g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                                g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                                g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
                                g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                                //the thread checking the pdf document state
                                Thread thread=new Thread() {
                                    
                                       @Override
                                    public void run() {
    
                                       while(pdfDocOpen) {

                                           if(progressBar.cancelAction()) {

                                               pdfDocOpen=false;
                                               
                                               try {
                                                   tp.reset();
                                                   pdfDoc.close();
                                               }catch (Exception ex) {ex.printStackTrace();}
                                               
                                               try {
                                                   out.close();
                                                   new File(new URI(fpath)).delete();
                                               }catch (Exception ex) {ex.printStackTrace();}

                                               break;
                                           }
                                           
                                           try {sleep(500);}catch (Exception ex) {}
                                       }
                                       
                                       progressBar.setVisible(false);
                                    } 
                                };
                                
                                thread.start();

                                //painting
                                getGraphicsNode().paint(g2);
                                
                                g2.dispose();
                                cb.addTemplate(tp, 0, 0);
                                
                                //closing the document
                                pdfDoc.close();
                                
                                out.flush();
                                out.close();
                                
                                progressBar.setVisible(false);

                            }catch (Exception ex){
                                
                                ex.printStackTrace();
                                
                                progressBar.setVisible(false);
                                
                                SwingUtilities.invokeLater(new Runnable() {
                                    
                                    public void run() {
                                        
                                        JOptionPane.showMessageDialog(  getSVGEditor().getParent(), 
                                                messageexporterror, labelerror, JOptionPane.WARNING_MESSAGE);                                                
                                    }
                                });
                                
                                try{
                                    //removing the file that had to be written
                                    new File(new URI(fpath)).delete();
                                }catch (Exception ex2) {}
                            }
                        }
                    };
                }
                
                hasExported=true;
                break;
            }
        }
        
        //deletes the destination file if the export action has not been done
        if(! hasExported && ! alreadyExist){
            
            try {
                new File(new URI(path)).delete();
            }catch (Exception ex) {}
        }
    }
    
    /**
     * the class of the canvas to export
     * @author Jordi SUC
     */
    protected abstract class ExportCanvas extends JSVGCanvas{
        
        /**
         * the progress bar dialog
         */
        protected SVGProgressBarDialog progressBar=null;
        
        /**
         * the constructor of the class
         * @param doc the document of the canvas
         * @param progressBarDialog the progress bar
         */
        protected ExportCanvas(org.w3c.dom.Document doc, SVGProgressBarDialog progressBarDialog) {
            
            this.progressBar=progressBarDialog;
            
            //adding the listener to the building of the gvt tree
            addGVTTreeBuilderListener(new GVTTreeBuilderAdapter(){
                
            	@Override
                public void gvtBuildStarted(GVTTreeBuilderEvent evt) {
                    
                    progressBar.setIndeterminate(false, true);
                    progressBar.setProgressBarValue(0, 0, 100);
                    
                    if(progressBar.cancelAction()) {
                        
                        removeGVTTreeBuilderListener(this);
                        abort();
                    }
                    
                    //setting the size for the canvas
                    Dimension canvasSize=getCanvasSize();
                    setSize(canvasSize);
                }
                
                @Override
                public void gvtBuildCompleted(GVTTreeBuilderEvent evt) {
              
                    progressBar.setProgressBarValue(10, 0, 100);
                    
                    if(progressBar.cancelAction()) {
                        
                        removeGVTTreeBuilderListener(this);
                        abort();
                        
                    }else {
                        
                        removeGVTTreeBuilderListener(this);
                    }
                }
                
                @Override
                public void gvtBuildFailed(GVTTreeBuilderEvent arg0) {
                    
                    removeGVTTreeBuilderListener(this);
                }
                
                @Override
                public void gvtBuildCancelled(GVTTreeBuilderEvent arg0) {

                    removeGVTTreeBuilderListener(this);
                }
            });
            
            //adding the listener to the rendering of the canvas
            addGVTTreeRendererListener(new GVTTreeRendererAdapter(){
                
                @Override
                public void gvtRenderingStarted(GVTTreeRendererEvent evt) {

                    if(progressBar.cancelAction()) {
                        
                        abort();
                    }
                }
                
                @Override
                public void gvtRenderingCompleted(GVTTreeRendererEvent evt) {
                    
                    progressBar.setProgressBarValue(25, 0, 100);
                    
                    if(progressBar.cancelAction()) {
                        
                        removeGVTTreeRendererListener(this);
                        abort();
                        
                    }else {
                        
                        Thread thread=new Thread() {
                            
                            @Override
                            public void run() {

                                doWhenRenderingDone();
                            }
                        };
                        
                        thread.start();
                    }

                    removeGVTTreeRendererListener(this);
                }
                
                @Override
                public void gvtRenderingFailed(GVTTreeRendererEvent arg0) {
                    
                    removeGVTTreeRendererListener(this);
                }
                
                @Override
                public void gvtRenderingCancelled(GVTTreeRendererEvent arg0) {

                    removeGVTTreeRendererListener(this);
                }
            });

            setDocument(doc);
            setVisible(true);
        }
        
        /**
         * aborting each process running on the canvas
         */
        protected void abort() {
            
            stopProcessing();
            dispose();
            
            JOptionPane.showMessageDialog(  editor.getParent(), 
                    messageexporterror, labelerror, JOptionPane.WARNING_MESSAGE);    
        }
        
        /**
         * the method that is called when the rendering is done
         */
        protected abstract void doWhenRenderingDone();
        
        /**
         * @return the canvas' size
         */
        public Dimension getCanvasSize(){
            
            //gets the document and the root element
            SVGDocument doc=getSVGDocument();
            
            if(doc!=null){
                
                SVGSVGElement root=doc.getRootElement();
                
                if(root!=null){
                    
                    int w=(int)SVGToolkit.getPixelledNumber(root.getAttributeNS(null, "width")); //$NON-NLS-1$
                    int h=(int)SVGToolkit.getPixelledNumber(root.getAttributeNS(null, "height")); //$NON-NLS-1$
                    
                    return new Dimension(w, h);
                }
            }
            
            return new Dimension(0,0);
        }
    }
}
