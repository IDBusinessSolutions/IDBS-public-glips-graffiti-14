package fr.itris.glips.svgeditor.properties;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;

/**
 * Based on {@link SVGPropertiesEntryWidget} - to allow editing of multiline text.
 *  
 * @author Liam Lynch
 * @author Jordi SUC
 */
public class SVGPropertiesMultiLineEntryWidget extends SVGPropertiesWidget{

    /**
     * the constructor of the class
     * @param propertyItem a property item
     */
	public SVGPropertiesMultiLineEntryWidget(SVGPropertyItem propertyItem) {

		super(propertyItem);
		
		buildComponent();
	}
	
	/**
	 * builds the component that will be displayed
	 */
	protected void buildComponent(){
		
		final SVGEditor editor=propertyItem.getProperties().getSVGEditor();
		final ResourceBundle bundle=SVGEditor.getBundle();
		final SVGFrame frame=editor.getFrameManager().getCurrentFrame();
				
		//the text field in which the value will be entered
		final JTextArea textArea=new JTextArea(propertyItem.getGeneralPropertyValue(), 4, 40);		
		final JScrollPane scroller = new JScrollPane(textArea);		
		
		textArea.setFont(theFont);
		textArea.moveCaretPosition(0);
			
		final CaretListener listener=new CaretListener(){
		    
			public void caretUpdate(CaretEvent e) {
			    
				//modifies the widgetValue of the property item
				propertyItem.changePropertyValue(textArea.getText());
			}
		};
			
		//adds a listener to the text field
		textArea.addCaretListener(listener);
		
		JPanel panel=new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(scroller);
		
		component=panel;

		//creates the disposer
		disposer=new Runnable(){

            public void run() {

				textArea.removeCaretListener(listener);
            }
		};
	}
}

