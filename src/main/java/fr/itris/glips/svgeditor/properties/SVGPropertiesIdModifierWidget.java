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

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;

/**
 * @author Jordi SUC
 */
public class SVGPropertiesIdModifierWidget extends SVGPropertiesWidget{

    /**
     * the constructor of the class
     * @param propertyItem a property item
     */
	public SVGPropertiesIdModifierWidget(SVGPropertyItem propertyItem) {

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
		final JTextField textField=new JTextField(propertyItem.getGeneralPropertyValue(), 10);
		textField.setFont(theFont);
		textField.moveCaretPosition(0);
		
		final JButton okButton=new JButton();
		okButton.setFont(smallFont);
	    Insets buttonInsets=new Insets(1, 1, 1, 1);
	    okButton.setMargin(buttonInsets);
		
		String errorTitle="", errorIdMessage=""; //$NON-NLS-1$ //$NON-NLS-2$
		
		if(bundle!=null){
		    
			try{
				okButton.setText(bundle.getString("labelok")); //$NON-NLS-1$
				errorTitle=bundle.getString("property_errortitle"); //$NON-NLS-1$
				errorIdMessage=bundle.getString("property_erroridmessage"); //$NON-NLS-1$
			}catch(Exception ex){}
		}
		
		final String ferrorTitle=errorTitle;
		final String ferrorIdMessage=errorIdMessage;
		
		final ActionListener listener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent arg0) {
				
				String value=textField.getText();
				
				if(frame.checkId(value)){
					
				    //modifies the widgetValue of the property item
					propertyItem.changePropertyValue(value);
					
				}else if(	propertyItem.getGeneralPropertyValue()!=null && ! propertyItem.getGeneralPropertyValue().equals(value)){
				    
					JOptionPane.showMessageDialog(editor.getParent(), ferrorIdMessage, ferrorTitle, JOptionPane.WARNING_MESSAGE);
				}
			}
		};
		
		//adds a listener to the button
		okButton.addActionListener(listener);
	
		//creates the component that will be returned
		JPanel validatedPanel=new JPanel();
		validatedPanel.setLayout(new BorderLayout());
		validatedPanel.add(textField, BorderLayout.CENTER);
		validatedPanel.add(okButton, BorderLayout.EAST);
		
		component=validatedPanel;
		
		//creates the disposer
		disposer=new Runnable(){

            public void run() {

				okButton.removeActionListener(listener);
            }
		};
	}
}

