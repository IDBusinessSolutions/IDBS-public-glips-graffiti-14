/*
 * Created on 4 juin 2005
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
import fr.itris.glips.svgeditor.visualresources.*;

/**
 * @author Jordi SUC
 */
public class SVGPropertiesPreserveAspectRatioChooser extends SVGPropertiesWidget{

	/**
	 * the runnable used for configuring the widgets
	 */
	protected Runnable configure=null;
	
	 /**
     * the constructor of the class
     * @param propertyItem a property item
     */
	public SVGPropertiesPreserveAspectRatioChooser(SVGPropertyItem propertyItem) {

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

		//getting the labels
		String meetOrSliceLabel="", alignLabel=""; //$NON-NLS-1$ //$NON-NLS-2$
		
		try{
			meetOrSliceLabel=bundle.getString("property_meetOrSlice"); //$NON-NLS-1$
			alignLabel=bundle.getString("property_preserveAspectRatioAlign"); //$NON-NLS-1$
		}catch (Exception ex){}
		
		//creating the label for the combo box
		final JLabel alignLbl=new JLabel(alignLabel+" : "); //$NON-NLS-1$

		//creating the array of the values of the align field
		final String[] alignValues=new String[10];
		
		alignValues[0]="none"; //$NON-NLS-1$
		alignValues[1]="xMinYMin"; //$NON-NLS-1$
		alignValues[2]="xMidYMin"; //$NON-NLS-1$
		alignValues[3]="xMaxYMin"; //$NON-NLS-1$
		alignValues[4]="xMinYMid"; //$NON-NLS-1$
		alignValues[5]="xMidYMid"; //$NON-NLS-1$
		alignValues[6]="xMaxYMid"; //$NON-NLS-1$
		alignValues[7]="xMinYMax"; //$NON-NLS-1$
		alignValues[8]="xMidYMax"; //$NON-NLS-1$
		alignValues[9]="xMaxYMax"; //$NON-NLS-1$

		//creating the combo items
		SVGComboItem[] items=new SVGComboItem[alignValues.length];
		String label=""; //$NON-NLS-1$
		int selectedIndex=-1;
		
		for(int i=0; i<alignValues.length; i++){
			
			//getting the label for this item and creating the item
			try{
				label=bundle.getString("item_"+alignValues[i]); //$NON-NLS-1$
			}catch (Exception ex){label=alignValues[i];}
			
			if(label!=null){
				
				items[i]=new SVGComboItem(alignValues[i], label);
			}
		}
		
		//creating the combo box that will be used to modify the align value
		final JComboBox combo=new JComboBox(items);
		combo.setFont(theFont);
		
		//creating the check box for the meet or slice
		final JCheckBox meetOrSliceCheckBox=new JCheckBox(meetOrSliceLabel);
		meetOrSliceCheckBox.setFont(theFont);

		//creating and adding the listener to the combo box
		final ActionListener comboAndMeetOrSliceListener=new ActionListener(){

			public void actionPerformed(ActionEvent evt) {

				if(combo.getSelectedItem()!=null){
					
					String meetOrSlice="meet"; //$NON-NLS-1$
					
					if(! meetOrSliceCheckBox.isSelected()){
						
						meetOrSlice="slice"; //$NON-NLS-1$
					}

					propertyItem.changePropertyValue(((SVGComboItem)combo.getSelectedItem()).getValue()+" "+meetOrSlice); //$NON-NLS-1$
				}
			}
		};
		
		combo.addActionListener(comboAndMeetOrSliceListener);
		meetOrSliceCheckBox.addActionListener(comboAndMeetOrSliceListener);
		
		//the runnable allowing to configure the widgets
		configure=new Runnable(){

			public void run() {

				String value=propertyItem.getGeneralPropertyValue();
        		combo.removeActionListener(comboAndMeetOrSliceListener);
        		meetOrSliceCheckBox.removeActionListener(comboAndMeetOrSliceListener);

				String align="", meetOrSlice=""; //$NON-NLS-1$ //$NON-NLS-2$

				if(value!=null){
					
					//getting the align value and the meet or slice value
					value=value.trim();
					value=value.replaceAll("\\s+", " "); //$NON-NLS-1$ //$NON-NLS-2$
					
					String[] splitValue=value.split(" "); //$NON-NLS-1$
					
					for(int i=0; i<splitValue.length; i++){
					
						if(splitValue[i].equals("meet") || splitValue[i].equals("slice")){ //$NON-NLS-1$ //$NON-NLS-2$
							
							meetOrSlice=splitValue[i];
							
						}else if(! splitValue[i].equals("defer")){ //$NON-NLS-1$
							
							align=splitValue[i];
						}
					}
				}
				
				//setting the default values
				if(align.equals("")){ //$NON-NLS-1$
					
					align="none"; //$NON-NLS-1$
				}
				
				if(meetOrSlice.equals("")){ //$NON-NLS-1$
					
					meetOrSlice="meet"; //$NON-NLS-1$
				}
				
				//enabling the widgets and setting their values
				combo.setEnabled(true);
				alignLbl.setEnabled(true);
				meetOrSliceCheckBox.setEnabled(true);
				
				//getting the selected index for the combo
				int selectedIndex=0;
				
				for(int i=0; i<alignValues.length; i++){
					
					if(align.equals(alignValues[i])){
						
						selectedIndex=i;
					}
				}
				
				//setting the selected index
				combo.setSelectedIndex(selectedIndex);
				
				//handles the meet or slice checkbox state
				meetOrSliceCheckBox.setSelected(meetOrSlice.equals("meet")); //$NON-NLS-1$

        		combo.addActionListener(comboAndMeetOrSliceListener);
        		meetOrSliceCheckBox.addActionListener(comboAndMeetOrSliceListener);
			}
		};

		//creating and filling the panel that will be returned
		JPanel panel=new JPanel();
		GridBagLayout gridBag=new GridBagLayout();
		panel.setLayout(gridBag);
		GridBagConstraints c=new GridBagConstraints();
		c.gridwidth=GridBagConstraints.REMAINDER;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.anchor=GridBagConstraints.WEST;

		c.gridwidth=1;
		alignLbl.setFont(theFont);
		gridBag.setConstraints(alignLbl, c);
		panel.add(alignLbl);

		c.gridwidth=GridBagConstraints.REMAINDER;
		gridBag.setConstraints(combo, c);
		panel.add(combo);

		gridBag.setConstraints(meetOrSliceCheckBox, c);
		panel.add(meetOrSliceCheckBox);
		
		//initializing the widgets
		configure.run();
		
		component=panel;

		//creates the disposer
		disposer=new Runnable(){

            public void run() {

        		combo.removeActionListener(comboAndMeetOrSliceListener);
        		meetOrSliceCheckBox.removeActionListener(comboAndMeetOrSliceListener);
            }
		};
	}
}
