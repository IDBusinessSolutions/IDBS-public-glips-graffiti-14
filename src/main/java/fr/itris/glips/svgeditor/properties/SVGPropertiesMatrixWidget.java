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

/**
 * @author Jordi SUC
 */
public class SVGPropertiesMatrixWidget extends SVGPropertiesWidget{

    /**
     * the constructor of the class
     * @param propertyItem a property item
     */
	public SVGPropertiesMatrixWidget(SVGPropertyItem propertyItem) {

		super(propertyItem);
		
		buildComponent();
	}
	
	/**
	 * builds the component that will be displayed
	 */
	protected void buildComponent(){
		
		final SVGEditor editor=propertyItem.getProperties().getSVGEditor();
		final ResourceBundle bundle=SVGEditor.getBundle();

		//the panel containing the entry widgets that will be used to modify the values of the matrix
		final JPanel matrix=new JPanel();
		matrix.setLayout(new GridLayout(3,3));
			
		final JTextField[][] entryMatrix=new JTextField[2][3];
		final String[][] initValues=new String[2][3];
		String propertyValue=propertyItem.getGeneralPropertyValue();
		int i, j;
		
		if(propertyValue==null || (propertyValue!=null && propertyValue.indexOf("matrix")==-1)){ //$NON-NLS-1$
		    
			//sets the initial values to the empty string
			initValues[0][0]="1"; //$NON-NLS-1$
			initValues[0][1]="0"; //$NON-NLS-1$
			initValues[0][2]="0"; //$NON-NLS-1$
			initValues[1][0]="0"; //$NON-NLS-1$
			initValues[1][1]="1"; //$NON-NLS-1$
			initValues[1][2]="0"; //$NON-NLS-1$
			
		}else{
		    
			String val=new String(propertyValue);
			val=editor.getSVGToolkit().cleanTransformString(val);
			val=val.substring(7,val.length()-1);
			val=val.trim();
			if(! val.endsWith(",")){ //$NON-NLS-1$
				
				val=val.concat(","); //$NON-NLS-1$
			}

			i=0; j=0;
			int k=0;
			
			while(! val.equals("")){ //$NON-NLS-1$

				k=val.indexOf(',');
				
				if(k==-1){
				    
				    k=0;
				}
				
				initValues[i][j]=val.substring(0,k);
				val=val.substring(val.length()>0?k+1:k,val.length());
				val=val.trim();
				
				if(i==1){
				    
				    j++;
				}
				
				i=(i+1)%2;
			}
		}

		//creates and adds the text fields to the panels
		for(i=0;i<2;i++){
		    
			for(j=0;j<3;j++){
			    
				entryMatrix[i][j]=new JTextField(initValues[i][j], 1);
				entryMatrix[i][j].setFont(theFont);
				entryMatrix[i][j].moveCaretPosition(0);
				matrix.add(entryMatrix[i][j]);
			}
		}
		
		double a=Double.parseDouble(initValues[0][0]), b=Double.parseDouble(initValues[1][0]), c=Double.parseDouble(initValues[0][1]), e0=-100, 
					d=Double.parseDouble(initValues[1][1]), e=Double.parseDouble(initValues[0][2]), f=Double.parseDouble(initValues[1][2]), f0=0;
		
		//double e2=(e0*d-c*f0)/(c*b-a*d);
		//double f2=-(f0+b*e0)/d;
			
		final JButton okButton=new JButton();
		okButton.setFont(smallFont);
		
		if(bundle!=null){
		    
			try{
				okButton.setText(bundle.getString("labelok")); //$NON-NLS-1$
			}catch(Exception ex){}
		}
			
		final ActionListener actionListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent arg0) {
			    
				String value="matrix("+ //$NON-NLS-1$
											(entryMatrix[0][0].getText().equals("")?"1":entryMatrix[0][0].getText())+" , "+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											(entryMatrix[1][0].getText().equals("")?"0":entryMatrix[1][0].getText())+" , "+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											(entryMatrix[0][1].getText().equals("")?"0":entryMatrix[0][1].getText())+" , "+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											(entryMatrix[1][1].getText().equals("")?"1":entryMatrix[1][1].getText())+" , "+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											(entryMatrix[0][2].getText().equals("")?"0":entryMatrix[0][2].getText())+" , "+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											(entryMatrix[1][2].getText().equals("")?"0":entryMatrix[1][2].getText())+" )"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
				
														
				//modifies the widgetValue of the property item
				propertyItem.changePropertyValue(value);
			}
		};
			
		//adds a listener to the button
		okButton.addActionListener(actionListener);
			
		//adds the button to the panel
		matrix.add(new JPanel());
		matrix.add(new JPanel());
		matrix.add(okButton);
		
		component=matrix;
		
		//creates the disposer
		disposer=new Runnable(){

            public void run() {

				okButton.removeActionListener(actionListener);
            }
		};
	}
}

