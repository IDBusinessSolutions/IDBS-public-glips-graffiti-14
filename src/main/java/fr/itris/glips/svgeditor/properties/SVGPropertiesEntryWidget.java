/*
 * Created on 19 janv. 2005
 * 
 =============================================
                   GNU LESSER GENERAL PUBLIC LICENSE Version 2.1
 =============================================
GLIPS Graffiti Editor, a SVG Editor
Copyright (C) 2004 Jordi SUC, Philippe Gil, SARL ITRIS

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
package fr.itris.glips.svgeditor.properties;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;

/**
 * @author Jordi SUC
 */
public class SVGPropertiesEntryWidget extends SVGPropertiesWidget{

    /**
     * the constructor of the class
     * @param propertyItem a property item
     */
	public SVGPropertiesEntryWidget(SVGPropertyItem propertyItem) {

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
		final JTextField textField=new JTextField(propertyItem.getGeneralPropertyValue());
		
		textField.setFont(theFont);
		textField.moveCaretPosition(0);
			
		final CaretListener listener=new CaretListener(){
		    
			public void caretUpdate(CaretEvent e) {
			    
				//modifies the widgetValue of the property item
				propertyItem.changePropertyValue(textField.getText());
			}
		};
			
		//adds a listener to the text field
		textField.addCaretListener(listener);
		
		JPanel panel=new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(textField);
		
		component=panel;

		//creates the disposer
		disposer=new Runnable(){

            public void run() {

				textField.removeCaretListener(listener);
            }
		};
	}
}

