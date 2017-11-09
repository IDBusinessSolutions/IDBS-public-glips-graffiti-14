package fr.itris.glips.svgeditor.svgfile.export;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import fr.itris.glips.svgeditor.*;

/**
 * the class of the dialog used to choose the parameters of the bmp export action
 * 
 * @author Jordi SUC
 */
public class BMPExportDialog extends ExportDialog{
    
    /**
     * whether the bmp file should use indexed colors
     */
    private boolean usePalette=false;
    
    /**
     * whether the bmp file should be compressed or not
     */
    private boolean compress=false;
    
    /**
     * the constructor of the class
     * @param editor the editor
     * @param parentContainer the parent container
     */
    public BMPExportDialog(SVGEditor editor, JFrame parentContainer) {

        super(editor, parentContainer);
        
        String imageSettingsLabel="", usePaletteLabel="";//, compressLabel=""; //$NON-NLS-1$ //$NON-NLS-2$
        
        if(bundle!=null){
            
            try{
                exportDialogTitle=bundle.getString("labelbmpexport"); //$NON-NLS-1$
                imageSettingsLabel=bundle.getString("labelimagesettings"); //$NON-NLS-1$
                usePaletteLabel=bundle.getString("labelexportpalette"); //$NON-NLS-1$
                //compressLabel=bundle.getString("labelexportbmpcompression");
            }catch (Exception ex){}
        }
        
        //creating the size chooser panel
        JPanel sizechooser=getSizeChooserPanel();
        
        //setting the title of the dialog
        setTitle(exportDialogTitle);
        
        //creating the check boxes to select the compression settings
        final JCheckBox usePaletteChk=new JCheckBox(usePaletteLabel);
        //final JCheckBox compressChk=new JCheckBox(compressLabel);
        usePaletteChk.setHorizontalAlignment(SwingConstants.LEFT);
        //compressChk.setHorizontalAlignment(SwingConstants.LEFT);
        usePaletteChk.setSelected(false);
        //compressChk.setSelected(false);
        
        //adding the listener to the check boxes
        usePaletteChk.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent evt) {

                usePalette=usePaletteChk.isSelected();
            }
        });
        
        /*compressChk.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent evt) {

                compress=compressChk.isSelected();
            }
        });*/

        //the panel that will contain the check boxes to select the compression settings
        JPanel compressionSettingsPanel=new JPanel();
        compressionSettingsPanel.setLayout(new BoxLayout(compressionSettingsPanel, BoxLayout.Y_AXIS));
        
        //creating the layout and filling the panel
        GridBagLayout gridBag=new GridBagLayout();
        compressionSettingsPanel.setLayout(gridBag);
        GridBagConstraints c=new GridBagConstraints();
        
        c.anchor=GridBagConstraints.WEST;
        c.fill=GridBagConstraints.HORIZONTAL;
        c.gridwidth=GridBagConstraints.REMAINDER;
        
        gridBag.setConstraints(usePaletteChk, c);
        compressionSettingsPanel.add(usePaletteChk);
        /*gridBag.setConstraints(compressChk, c);
        compressionSettingsPanel.add(compressChk);*/
        
        compressionSettingsPanel.setBorder(new TitledBorder(imageSettingsLabel));
        
        //handling the parameters panel
        parametersPanel.setLayout(new BoxLayout(parametersPanel, BoxLayout.Y_AXIS));
        parametersPanel.add(sizechooser);
        parametersPanel.add(compressionSettingsPanel);
    }

    /**
     * @return Returns the compress.
     */
    public boolean isCompressed() {
        return compress;
    }

    /**
     * @return Returns the usePalette.
     */
    public boolean isUsePalette() {
        return usePalette;
    }

}
