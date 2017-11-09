package fr.itris.glips.svgeditor.svgfile.export;

//import java.awt.*;
//import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.*;

/**
 * the class of the dialog used to choose the parameters of the png export action
 * 
 * @author Jordi SUC
 */
public class PNGExportDialog extends ExportDialog{

    /**
     * the labels 
     */
    /*private String pngBitDepthsLabel="", imageSettingsLabel="", imageTypeLabel="", rgbLabel="", 
                            paletteLabel="", grayLabel="", labelBit="", labelBits="";*/
    
    /**
     * the type of the image
     */
    private int imageType=BufferedImage.TYPE_INT_RGB;
    
    /**
     * the bit depths for a png image
     */
    private int pngBitDepths=16;
    
    /**
     * the constructor of the class
     * @param editor the editor
     * @param parentContainer the parent container
     */
    public PNGExportDialog(SVGEditor editor, JFrame parentContainer) {

        super(editor, parentContainer);
        
        if(bundle!=null){
            
            try{
                exportDialogTitle=bundle.getString("labelpngexport"); //$NON-NLS-1$
                /*imageSettingsLabel=bundle.getString("labelimagesettings");
                pngBitDepthsLabel=bundle.getString("labelexportbitdepths");
                imageTypeLabel=bundle.getString("labelexportpngimagetype");
                rgbLabel=bundle.getString("labelexportpngtruecolors");
                paletteLabel=bundle.getString("labelexportpngindexed");
                grayLabel=bundle.getString("labelexportpnggray");
                labelBit=bundle.getString("labelexportbit");
                labelBits=bundle.getString("labelexportbits");*/
            }catch (Exception ex){}
        }
        
        //creating the size chooser panel
        JPanel sizechooser=getSizeChooserPanel();
        
        //setting the title of the dialog
        setTitle(exportDialogTitle);
        
/*        //the panel that will containing the radio buttons
        JPanel compressionPanel=new JPanel();
        
        JLabel radiobuttonsPanelLabel=new JLabel(imageTypeLabel+" : ");
        
        final JRadioButton    rgbRadioButton=new JRadioButton(rgbLabel),
                                         indexedRadioButton=new JRadioButton(paletteLabel);,
                                         grayRadioButton=new JRadioButton(grayLabel);
        
        ButtonGroup group=new ButtonGroup();
        group.add(rgbRadioButton);
        group.add(indexedRadioButton);
        //group.add(grayRadioButton);
        
        rgbRadioButton.setSelected(true);

        //the panel that will contain the slider and the label
        final JPanel pngDisplayAndSlider=new JPanel();
        final JSlider pngSlider=new JSlider(0, 100, 100);
        pngSlider.setPaintTicks(true);
        pngSlider.setSnapToTicks(true);
        pngSlider.setMinorTickSpacing(100);
        
        pngSlider.setPreferredSize(new Dimension(100, 28));
        final JLabel pngDisplayedValue=new JLabel("16 "+labelBits);
        pngDisplayedValue.setHorizontalAlignment(SwingConstants.CENTER);
        pngDisplayedValue.setPreferredSize(new Dimension(75, 20));
        
        final Runnable sliderRunnable=new Runnable() {
            
           public void run() {

               if(rgbRadioButton.isSelected()) {
                   
                   if(pngSlider.getValue()==0){
                       
                       pngBitDepths=8;
                       pngDisplayedValue.setText("8 "+labelBits);
                       
                   }else if(pngSlider.getValue()==100){
                       
                       pngBitDepths=16;
                       pngDisplayedValue.setText("16 "+labelBits);
                   }
                   
               }else if(indexedRadioButton.isSelected()) {
                   
                   if(pngSlider.getValue()>=0 && pngSlider.getValue()<33){
                       
                       pngBitDepths=1;
                       pngDisplayedValue.setText("1 "+labelBit);
                       
                   }else if(pngSlider.getValue()>=33 && pngSlider.getValue()<66){
                       
                       pngBitDepths=2;
                       pngDisplayedValue.setText("2 "+labelBits);
                       
                   }else if(pngSlider.getValue()>=66 && pngSlider.getValue()<99){
                       
                       pngBitDepths=4;
                       pngDisplayedValue.setText("4 "+labelBits);
                       
                   }else if(pngSlider.getValue()>=99){
                       
                       pngBitDepths=8;
                       pngDisplayedValue.setText("8 "+labelBits);
                   }
                   
               }/*else if(grayRadioButton.isSelected()) {
                   
                   if(pngSlider.getValue()>=0 && pngSlider.getValue()<25){
                       
                       pngBitDepths=1;
                       pngDisplayedValue.setText("1 "+labelBit);
                       
                   }else if(pngSlider.getValue()>=25 && pngSlider.getValue()<50){
                       
                       pngBitDepths=2;
                       pngDisplayedValue.setText("2 "+labelBits);
                       
                   }else if(pngSlider.getValue()>=50 && pngSlider.getValue()<75){
                       
                       pngBitDepths=4;
                       pngDisplayedValue.setText("4 "+labelBits);
                       
                   }else if(pngSlider.getValue()>=75 && pngSlider.getValue()<100){
                       
                       pngBitDepths=8;
                       pngDisplayedValue.setText("8 "+labelBits);
                       
                   }else if(pngSlider.getValue()==100){
                       
                       pngBitDepths=16;
                       pngDisplayedValue.setText("16 "+labelBits);
                   }
               }     
            } 
        };
        
        //the listener to the slider
        ChangeListener pngSliderChangeListener=new ChangeListener(){
            
            public void stateChanged(ChangeEvent evt) {
 
                sliderRunnable.run();
            }
        };
        
        //adds a listener to the slider
        pngSlider.addChangeListener(pngSliderChangeListener);
        
        //adding the listener to the radio buttons
        ActionListener radioButtonActionListener=new ActionListener() {
            
            public void actionPerformed(ActionEvent evt) {

                if(evt.getSource().equals(rgbRadioButton)) {
                    
                    imageType=BufferedImage.TYPE_INT_RGB;
                    pngSlider.setMinorTickSpacing(100);
                    
                }else if(evt.getSource().equals(indexedRadioButton)) {
                    
                    imageType=BufferedImage.TYPE_BYTE_INDEXED;
                    pngSlider.setMinorTickSpacing(100/3);
                    
                }else if(evt.getSource().equals(grayRadioButton)) {
                    
                    imageType=BufferedImage.TYPE_BYTE_GRAY;
                    pngSlider.setMinorTickSpacing(25);
                }
                
                pngSlider.setValue(100);
                sliderRunnable.run();
            }
        };
        
        rgbRadioButton.addActionListener(radioButtonActionListener);
        indexedRadioButton.addActionListener(radioButtonActionListener);
        //grayRadioButton.addActionListener(radioButtonActionListener);

        //the label for the widgets
        JLabel pngLabel=new JLabel(pngBitDepthsLabel.concat(" :"));
        
        pngDisplayAndSlider.setLayout(new BorderLayout(3, 0));
        pngDisplayAndSlider.add(pngSlider, BorderLayout.CENTER);
        pngDisplayAndSlider.add(pngDisplayedValue, BorderLayout.NORTH);

        //creating the layout and filling the compression panel
        GridBagLayout gridBag=new GridBagLayout();
        compressionPanel.setLayout(gridBag);
        GridBagConstraints c=new GridBagConstraints();
        
        c.anchor=GridBagConstraints.WEST;
        c.fill=GridBagConstraints.HORIZONTAL;
        c.gridwidth=GridBagConstraints.REMAINDER;
        
        c.insets=new Insets(0, 0, 5, 0);
        gridBag.setConstraints(radiobuttonsPanelLabel, c);
        compressionPanel.add(radiobuttonsPanelLabel);
        
        c.insets=new Insets(0, 25, 0, 0);
        gridBag.setConstraints(rgbRadioButton, c);
        compressionPanel.add(rgbRadioButton);
        gridBag.setConstraints(indexedRadioButton, c);
        compressionPanel.add(indexedRadioButton);
        //gridBag.setConstraints(grayRadioButton, c);
        //compressionPanel.add(grayRadioButton);
        
        c.insets=new Insets(0, 0, 0, 5);
        c.gridwidth=1;
        gridBag.setConstraints(pngLabel, c);
        compressionPanel.add(pngLabel);
        
        c.gridwidth=GridBagConstraints.REMAINDER;
        c.insets=new Insets(0, 0, 0, 0);
        gridBag.setConstraints(pngDisplayAndSlider, c);
        compressionPanel.add(pngDisplayAndSlider);
        
        compressionPanel.setBorder(new CompoundBorder(
                                                        new TitledBorder(imageSettingsLabel), new EmptyBorder(4, 4, 4, 4)));
        */
        //handling the parameters panel
        parametersPanel.setLayout(new BoxLayout(parametersPanel, BoxLayout.Y_AXIS));
        parametersPanel.add(sizechooser);
        //parametersPanel.add(compressionPanel);
    }

    /**
     * @return Returns the imageType.
     */
    public int getImageType() {
        return imageType;
    }

    /**
     * @return Returns the pngBitDepths.
     */
    public int getPngBitDepths() {
        return pngBitDepths;
    }
}
