/*
 * Created on 22 déc. 2004
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
package fr.itris.glips.svgeditor.svgfile;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.print.*;
import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.resources.*;

/**
 * @author Jordi SUC
 * the class that creates the static menu item exit in the menu bar
 */
public class SVGPrint extends SVGModuleAdapter{
    
    /**
     * the ids of the module
     */
    final private String	idprint="Print"; //$NON-NLS-1$
    
    /**
     * the labels
     */
    private String labelprint="", messageprinterror="", labelerror=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
    /**
     * the editor
     */
    private SVGEditor editor;
    
    /**
     * the menu item that will be added to the menubar
     */
    private JMenuItem print;
    
    /**
     * the dialog box used to choose the parameters for the print
     */
    private PrintDialog printDialog=null;
    
    /**
     * the resource bundle
     */
    private ResourceBundle bundle=null;
    
    /**
     * the constructor of the class
     * @param editor the editor
     */
    public SVGPrint(SVGEditor editor){
        
        this.editor=editor;
        
        //gets the labels from the resources
        bundle=SVGEditor.getBundle();
        
        if(bundle!=null){
            
            try{
                labelprint=bundle.getString("labelprint"); //$NON-NLS-1$
                messageprinterror=bundle.getString("messageprinterror"); //$NON-NLS-1$
                labelerror=bundle.getString("labelerror"); //$NON-NLS-1$
            }catch (Exception ex){}
        }
        
        //a listener that listens to the changes of the SVGFrames
        final ActionListener svgframeListener=new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                
                final SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
                
                if(frame!=null){
                    
                    //enables the menuitem
                    print.setEnabled(true);
                    
                }else{
                    
                    //disables the menuitem
                    print.setEnabled(false);
                }
            }	
        };
        
        //adds the SVGFrame change listener
        editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);
        
		//getting the icons
		ImageIcon printIcon=SVGResource.getIcon("Print", false), //$NON-NLS-1$
						dprintIcon=SVGResource.getIcon("Print", true); //$NON-NLS-1$

        //handling the menu item
        print=new JMenuItem(labelprint, printIcon);
        print.setDisabledIcon(dprintIcon);
        print.setAccelerator(KeyStroke.getKeyStroke("ctrl P")); //$NON-NLS-1$
        print.setEnabled(false);
        
        //adds a listener to the menu item
        print.addActionListener(
                
            new ActionListener(){
                
                public void actionPerformed(ActionEvent e){
                    
                    SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
                    
                    if(frame!=null){
                        
                        print(frame);
                    }
                }
            }
        );
        
        //creating the print dialog box
        if(getSVGEditor().getParent() instanceof JFrame){
        	
            printDialog=new PrintDialog((JFrame)getSVGEditor().getParent());
        	
        }else{
        	
            printDialog=new PrintDialog(new JFrame("")); //$NON-NLS-1$
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
        menuItems.put(idprint, print);
        
        return menuItems;
    }
    
    /**
     * @return a map associating a tool item id to its tool item object
     */
    public Hashtable getToolItems(){
        
        return null;
    }
    
	/**
	 * Returns the collection of the popup items
	 * @return the collection of the popup items
	 */
	public Collection getPopupItems(){
		
		return null;
	}
    
	/**
	 * layout some elements in the module
	 */
	public void initialize(){
		

	}
    
    /**
     * @see fr.itris.glips.svgeditor.SVGModuleAdapter#cancelActions()
     */
    public void cancelActions() {
        
    }
    
    /**
     * prints the document corresponding to the frame to the given destination file
     * @param frame a frame
     */
    protected void print(SVGFrame frame){
        
        if(frame!=null){

            Document clonedDocument=(Document)frame.getScrollPane().getSVGCanvas().getDocument().cloneNode(true);
            
            //creating the transcoder input and output
            TranscoderInput input=new TranscoderInput(clonedDocument);
            TranscoderOutput output=new TranscoderOutput();
            
            //the transcoder
            PrintTranscoder transcoder=new PrintTranscoder();
            
            transcoder.addTranscodingHint(PrintTranscoder.KEY_SHOW_PRINTER_DIALOG, new Boolean(true));
            //transcoder.addTranscodingHint(PrintTranscoder.KEY_SHOW_PAGE_DIALOG, new Boolean(true));
            
            try{
                transcoder.transcode(input, output);
                transcoder.print();
            }catch (Exception ex){
            
                JOptionPane.showMessageDialog(getSVGEditor().getParent(), messageprinterror, labelerror, JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    /**
     * the class of the dialogs used to choose the parameters of the print action for each kind of print format
     * 
     * @author Jordi SUC
     */
    protected class PrintDialog extends JDialog{
        
        /**
         * the constant describing a ok action
         */
        public static final int OK_ACTION=0;
        
        /**
         * the constant describing a cancel action
         */
        public static final int CANCEL_ACTION=1;
        
        /**
         * the labels for the prints
         */
        private String 	labelJpgPrint="", labelPngPrint="", labelTiffPrint="", okLabel="", cancelLabel="",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						        jpgQualityLabel="", pngBitDepthsLabel="", tiffForceTransparentWhite="", pngBestQualityLabel="", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						        printSizeLabel="", jpgPropertiesLabel="", pngPropertiesLabel="", tiffPropertiesLabel="", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						        heightLabel="", widthLabel=""; //$NON-NLS-1$ //$NON-NLS-2$
        
        /**
         * the panel containing the item to set the size of the printed image
         */
        private JPanel sizeChooserPanel=new JPanel();
        
        /**
         * the textfields used to specify the size of the printed image
         */
        private final JTextField txtw=new JTextField(), txth=new JTextField();
        
        /**
         * the jpeg quality
         */
        private float jpgQuality=1.0f;
        
        /**
         * the panel containing the widgets to specify the parameters for the jpg print
         */
        private JPanel jpgPanel=new JPanel();
        
        /**
         * the bit depths for a png image
         */
        private int pngBitDepths=9;
        
        /**
         * the dimension of the printed image
         */
        private double printWidth=0, printHeight=0;
        
        /**
         * the panel containing the widgets to specify the parameters for the png print
         */
        private JPanel pngPanel=new JPanel();
        
        /**
         * controls whether the encoder should force the image's fully transparent pixels to be fully transparent white instead of fully transparent black.
         */
        private boolean forceTransparentWhite=false;
        
        /**
         * the panel containing the widgets to specify the parameters for the tiff print
         */
        private JPanel tiffPanel=new JPanel();
        
        /**
         * the buttons panel
         */
        private JPanel buttonsPanel=new JPanel();
        
        /**
         * the current action
         */
        private int currentActionState=CANCEL_ACTION;
        
        /**
         * the constructor of the class
         */
        protected PrintDialog(JFrame parent){
            
            super(parent, labelprint, true);
            
            if(bundle!=null){
                
                try{
                    labelJpgPrint=bundle.getString("labeljpgprint"); //$NON-NLS-1$
                    labelPngPrint=bundle.getString("labelpngprint"); //$NON-NLS-1$
                    labelTiffPrint=bundle.getString("labeltiffprint"); //$NON-NLS-1$
                    okLabel=bundle.getString("labelok"); //$NON-NLS-1$
                    cancelLabel=bundle.getString("labelcancel"); //$NON-NLS-1$
                    jpgQualityLabel=bundle.getString("labeljpegquality"); //$NON-NLS-1$
                    pngBitDepthsLabel=bundle.getString("labelpngbitdepths"); //$NON-NLS-1$
                    tiffForceTransparentWhite=bundle.getString("labeltiffforcetransparentwhite"); //$NON-NLS-1$
                    pngBestQualityLabel=bundle.getString("labelpngbestquality"); //$NON-NLS-1$
                    printSizeLabel=bundle.getString("labelprintsize"); //$NON-NLS-1$
                    jpgPropertiesLabel=bundle.getString("labeljpgproperties"); //$NON-NLS-1$
                    pngPropertiesLabel=bundle.getString("labelpngproperties"); //$NON-NLS-1$
                    tiffPropertiesLabel=bundle.getString("labeltiffproperties"); //$NON-NLS-1$
                    widthLabel=bundle.getString("labelwidth"); //$NON-NLS-1$
                    heightLabel=bundle.getString("labelheight"); //$NON-NLS-1$
                }catch (Exception ex){}
            }
            
            /*******************creating the dimension chooser panel**************/
            
			//setting the layout
            GridBagLayout gridBag=new GridBagLayout();
            GridBagConstraints c=new GridBagConstraints();
            sizeChooserPanel.setLayout(gridBag);
            c.fill=GridBagConstraints.HORIZONTAL;
            c.insets=new Insets(3, 3, 3, 3);
			
			//setting the listeners to the textfields
			txtw.addCaretListener(new CaretListener(){

	            public void caretUpdate(CaretEvent evt) {
	                
	                try{
	                    printWidth=Double.parseDouble(txtw.getText());
	                }catch (Exception ex){}
	            }
			});
			
			txth.addCaretListener(new CaretListener(){

	            public void caretUpdate(CaretEvent evt) {
	                
	                try{
	                    printHeight=Double.parseDouble(txth.getText());
	                }catch (Exception ex){}
	            }
			});
			
			JPanel pw=new JPanel();
			pw.setLayout(new BoxLayout(pw,BoxLayout.X_AXIS));
			JLabel pxw=new JLabel("px"); //$NON-NLS-1$
			pxw.setBorder(new EmptyBorder(0, 4, 0, 0));
			pw.add(txtw);
			pw.add(pxw);
	
			JPanel ph=new JPanel();
			ph.setLayout(new BoxLayout(ph,BoxLayout.X_AXIS));
			JLabel pxh=new JLabel("px"); //$NON-NLS-1$
			pxh.setBorder(new EmptyBorder(0, 4, 0, 0));
			ph.add(txth);
			ph.add(pxh);
	
			JLabel lbw=new JLabel(widthLabel.concat(" :")); //$NON-NLS-1$
			JLabel lbh=new JLabel(heightLabel.concat(" :")); //$NON-NLS-1$
					
			//adding the widgets to the panel
			c.anchor=GridBagConstraints.EAST;
			c.gridwidth=1;
			c.weightx=0;
			gridBag.setConstraints(lbw, c);
			sizeChooserPanel.add(lbw);
			
			c.anchor=GridBagConstraints.WEST;
			c.gridwidth=GridBagConstraints.REMAINDER;
			c.weightx=50.0;
			gridBag.setConstraints(pw, c);
			sizeChooserPanel.add(pw);
			
			c.anchor=GridBagConstraints.EAST;
			c.gridwidth=1;
			c.weightx=0;
			gridBag.setConstraints(lbh, c);
			sizeChooserPanel.add(lbh);
			
			c.anchor=GridBagConstraints.WEST;
			c.gridwidth=GridBagConstraints.REMAINDER;
			c.weightx=50;
			gridBag.setConstraints(ph, c);
			sizeChooserPanel.add(ph);
			
			//setting the border
			TitledBorder border=new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), printSizeLabel);
			sizeChooserPanel.setBorder(border);
            
            /*******************the jpg case**********************/
            
            //the panel that will contain the slider and the label, the slider, and the initial value
            final JPanel jpgDisplayAndSlider=new JPanel();
            final JSlider jpgSlider=new JSlider(0, 100, 100);
            final JLabel jpgDisplayedValue=new JLabel(100+" %"); //$NON-NLS-1$
            
            jpgDisplayedValue.setPreferredSize(new Dimension(50, 20));	
            
            //the listener to the slider
            final MouseAdapter jpgSliderListener=new MouseAdapter(){
                
                public void mouseReleased(MouseEvent evt) {
                    
                    jpgQuality=(float)((float)(jpgSlider.getValue())/100);
                }
            };
            
            //adds a listener to the slider
            jpgSlider.addMouseListener(jpgSliderListener);
            
            ChangeListener jpgSliderChangeListener=new ChangeListener(){
                
                public void stateChanged(ChangeEvent arg0) {
                    
                    jpgDisplayedValue.setText(jpgSlider.getValue()+" %"); //$NON-NLS-1$
                }
            };
            
            //adds a listener to the slider
            jpgSlider.addChangeListener(jpgSliderChangeListener);
            
            jpgDisplayAndSlider.setLayout(new BorderLayout(3, 0));
            jpgDisplayAndSlider.add(jpgSlider, BorderLayout.CENTER);
            jpgDisplayAndSlider.add(jpgDisplayedValue, BorderLayout.EAST);
            
            //the label for the widgets
            JLabel jpgLabel=new JLabel(jpgQualityLabel.concat(" :")); //$NON-NLS-1$
            
            jpgPanel.setLayout(new BorderLayout(5, 0));
            jpgPanel.add(jpgLabel, BorderLayout.WEST);
            jpgPanel.add(jpgDisplayAndSlider, BorderLayout.CENTER);
            
            //setting the border for this panel
            border=new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), jpgPropertiesLabel);
            jpgPanel.setBorder(border);
            
            /*******************the png case**********************/
            
            //the panel that will contain the slider and the label, the slider, and the initial value
            final String fpngBestQualityLabel=pngBestQualityLabel;
            final JPanel pngDisplayAndSlider=new JPanel();
            final JSlider pngSlider=new JSlider(0, 100, 100);
            final JLabel pngDisplayedValue=new JLabel(fpngBestQualityLabel);
            
            pngDisplayedValue.setPreferredSize(new Dimension(75, 20));	
            
            ChangeListener pngSliderChangeListener=new ChangeListener(){
                
                public void stateChanged(ChangeEvent evt) {
                    
                    if(pngSlider.getValue()>=0 && pngSlider.getValue()<20){
                        
                        pngBitDepths=1;
                        pngSlider.setValue(0);
                        pngDisplayedValue.setText("1 bit"); //$NON-NLS-1$
                        
                    }else if(pngSlider.getValue()>=20 && pngSlider.getValue()<40){
                        
                        pngBitDepths=2;
                        pngDisplayedValue.setText("2 bit"); //$NON-NLS-1$
                        
                    }else if(pngSlider.getValue()>=40 && pngSlider.getValue()<60){
                        
                        pngBitDepths=4;
                        pngSlider.setValue(40);
                        pngDisplayedValue.setText("4 bit"); //$NON-NLS-1$
                        
                    }else if(pngSlider.getValue()>=60 && pngSlider.getValue()<80){
                        
                        pngBitDepths=8;
                        pngSlider.setValue(60);
                        pngDisplayedValue.setText("8 bit"); //$NON-NLS-1$
                        
                    }else if(pngSlider.getValue()>=80 && pngSlider.getValue()<=100){
                        
                        pngBitDepths=9;
                        pngSlider.setValue(100);
                        pngDisplayedValue.setText(fpngBestQualityLabel);
                    }
                }
            };
            
            //adds a listener to the slider
            pngSlider.addChangeListener(pngSliderChangeListener);
            
            //the label for the widgets
            JLabel pngLabel=new JLabel(pngBitDepthsLabel.concat(" :")); //$NON-NLS-1$
            
            pngDisplayAndSlider.setLayout(new BorderLayout(3, 0));
            pngDisplayAndSlider.add(pngSlider, BorderLayout.CENTER);
            pngDisplayAndSlider.add(pngDisplayedValue, BorderLayout.EAST);
            
            pngPanel.setLayout(new BorderLayout(5, 0));
            pngPanel.add(pngLabel, BorderLayout.WEST);
            pngPanel.add(pngDisplayAndSlider, BorderLayout.CENTER);
            
            //setting the border for this panel
            border=new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), pngPropertiesLabel);
            pngPanel.setBorder(border);
            
            /*******************the tiff case**********************/
            
            //the panel containg the check box and the label
            JPanel tiffCheckAndLabel=new JPanel();
            tiffCheckAndLabel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
            
            JCheckBox tiffCheckBox=new JCheckBox();
            tiffCheckBox.setSelected(false);
            
            //the label for the widget
            JLabel tiffLabel=new JLabel(tiffForceTransparentWhite);
            tiffCheckAndLabel.add(tiffCheckBox);
            tiffCheckAndLabel.add(tiffLabel);
            
            tiffPanel.setLayout(new BorderLayout(0, 0));
            tiffPanel.add(tiffCheckAndLabel, BorderLayout.CENTER);
            
            //setting the border for this panel
            border=new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), tiffPropertiesLabel);
            tiffPanel.setBorder(border);

            /******************the buttons***********************/
            
            //the buttons
            final JButton okButton=new JButton(okLabel), cancelButton=new JButton(cancelLabel);
            
            //the buttons panel
            buttonsPanel=new JPanel();
            buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            
            //the listener to the ok and cancel buttons
            final ActionListener buttonListener=new ActionListener(){
                
                public void actionPerformed(ActionEvent evt) {

                    if(evt.getSource().equals(okButton)){
                        
                        currentActionState=OK_ACTION;
                        
                    }else{
                        
                        currentActionState=CANCEL_ACTION;
                    }
                    
                    setVisible(false);
                } 
            };
            
            okButton.addActionListener(buttonListener);
            cancelButton.addActionListener(buttonListener);
            buttonsPanel.add(okButton);
            buttonsPanel.add(cancelButton);
            
            //setting the layout for the content pane of the dialog
            getContentPane().setLayout(new BorderLayout(0, 5));
        }
        
        /**
         * shows the dialog box to configure the print action
         * @param frame the frame whose document is to be printed
         * @return the state of the action : OK_ACTION or CANCEL_ACTION
         */
        protected int showPrintDialog(SVGFrame frame){
            
            currentActionState=CANCEL_ACTION;
            
            if(frame!=null && frame.getScrollPane().getSVGCanvas().getGeometryCanvasSize()!=null){
                
                //setting the default size for the textfields
                Point2D.Double imageSize=frame.getScrollPane().getSVGCanvas().getGeometryCanvasSize();
                txtw.setText(imageSize.getX()+""); //$NON-NLS-1$
                txth.setText(imageSize.getY()+""); //$NON-NLS-1$
                
                //the panel containing the size chooser and the panel linked with an print type
                JPanel contentPanel=new JPanel();
                contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                contentPanel.add(sizeChooserPanel);
                
                //setting an empty border for the content panel
                contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

                //fills the content panel
                /*if(printType==JPEG_EXPORT){
                    
                    contentPanel.add(jpgPanel);
                    setTitle(labelJpgPrint);
                }*/
                
                //filling the content pane
                getContentPane().removeAll();
                getContentPane().add(contentPanel, BorderLayout.CENTER);
                getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
                
                //packs the dialog box
                pack();
                
    			//sets the location of the dialog box
    			int 	x=(int)(getSVGEditor().getParent().getLocationOnScreen().getX()+getSVGEditor().getParent().getWidth()/2-getWidth()/2), 
    					y=(int)(getSVGEditor().getParent().getLocationOnScreen().getY()+getSVGEditor().getParent().getHeight()/2-getHeight()/2);
    			
    			setLocation(x,y);
                
    			//displays the dialog box
                setVisible(true);
                
                //waits until the parameters are chosen and one of the buttons is clicked
                while(isVisible()){
                    
                    try{
                        wait((long)100.0);
                    }catch (Exception ex){}
                }
            }

            return currentActionState;
        }
        
        /**
         * @return Returns the forceTransparentWhite boolean
         */
        protected boolean isForceTransparentWhite() {
            return forceTransparentWhite;
        }
        
        /**
         * @return Returns the jpgQuality.
         */
        protected float getJpgQuality() {
            return jpgQuality;
        }
        
        /**
         * @return Returns the pngBitDepths.
         */
        protected int getPngBitDepths() {
            return pngBitDepths;
        }
        
        /**
         * @return Returns the printHeight.
         */
        protected double getPrintHeight() {
            return printHeight;
        }
        
        /**
         * @return Returns the printWidth.
         */
        protected double getPrintWidth() {
            return printWidth;
        }
    }
    
}
