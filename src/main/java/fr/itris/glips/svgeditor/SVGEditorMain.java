package fr.itris.glips.svgeditor;

import java.awt.event.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.resources.*;

/**
 * the enter point of the application 
 * @author Jordi SUC
 */
public class SVGEditorMain {

	/**
	 * the constructor of the class
	 *@param fileName the name of a svg file
	 */
	public SVGEditorMain(String fileName){

		//creating the editor object
		final SVGEditor editor=new SVGEditor();
		
		//creating the parent frame of the editor
		JFrame mainFrame=new JFrame();
		mainFrame.setTitle("GLIPS Graffiti Editor"); //$NON-NLS-1$
		
		//handling the close case
		mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter(){
			
			@Override
			public void windowClosing(WindowEvent evt) {

				editor.exit();
			}
		});
		
		//setting the icon
		ImageIcon icon2=SVGResource.getIcon("Editor", false); //$NON-NLS-1$
		
		if(icon2!=null && icon2.getImage()!=null){
			
			mainFrame.setIconImage(icon2.getImage());
		}

		//intializing the editor
		editor.init(mainFrame, fileName, true, true, false, true, null);
	}
	
	/**
	 * the main method
	 * @param args the array of arguments
	 */
	public static void main(String[] args) {
		
		String fileName=""; //$NON-NLS-1$
		
		if(args!=null && args.length>0){
			
			fileName=args[0];
		}
		
		new SVGEditorMain(fileName);
	}
}
