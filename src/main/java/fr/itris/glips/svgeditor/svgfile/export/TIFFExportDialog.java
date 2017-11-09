package fr.itris.glips.svgeditor.svgfile.export;

import javax.swing.*;

import fr.itris.glips.svgeditor.*;

/**
 * the class of the dialog used to choose the parameters of the tiff export action
 * 
 * @author Jordi SUC
 */
public class TIFFExportDialog extends ExportDialog{
    
    /**
     * the constructor of the class
     * @param editor the editor
     * @param parentContainer the parent container
     */
    public TIFFExportDialog(SVGEditor editor, JFrame parentContainer) {

        super(editor, parentContainer);
        
       if(bundle!=null){
            
            try{
                exportDialogTitle=bundle.getString("labeltiffexport"); //$NON-NLS-1$
            }catch (Exception ex){}
        }
       
       //setting the title of the dialog
       setTitle(exportDialogTitle);
       
       //creating the size chooser panel
       JPanel sizechooser=getSizeChooserPanel();
       //handling the parameters panel
       parametersPanel.setLayout(new BoxLayout(parametersPanel, BoxLayout.Y_AXIS));
       parametersPanel.add(sizechooser);
    }
}
