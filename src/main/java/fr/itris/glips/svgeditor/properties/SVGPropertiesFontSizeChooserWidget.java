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

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;

/**
 * @author Jordi SUC
 */
public class SVGPropertiesFontSizeChooserWidget extends SVGPropertiesWidget{

    /**
     * the constructor of the class
     * @param propertyItem a property item
     */
	public SVGPropertiesFontSizeChooserWidget(SVGPropertyItem propertyItem) {

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

		
		//the value of the property
		String propertyValue=propertyItem.getGeneralPropertyValue();
		propertyValue=propertyValue.replaceAll("[pt]",""); //$NON-NLS-1$ //$NON-NLS-2$
		propertyValue=propertyValue.replaceAll("\\s",""); //$NON-NLS-1$ //$NON-NLS-2$
			
		//the list of the items that will be displayed in the combo box
		LinkedList itemList=new LinkedList();
		String[] items={"6","7","8","9","10","11","12","13","14","15","16","18","20","22","24","26","28","32","36","40","44","48","54","60","66","72","80","88","96"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$ //$NON-NLS-17$ //$NON-NLS-18$ //$NON-NLS-19$ //$NON-NLS-20$ //$NON-NLS-21$ //$NON-NLS-22$ //$NON-NLS-23$ //$NON-NLS-24$ //$NON-NLS-25$ //$NON-NLS-26$ //$NON-NLS-27$ //$NON-NLS-28$ //$NON-NLS-29$

		for(int i=0;i<items.length;i++){
		    
		    itemList.add(items[i]);
		}
		
		if(! itemList.contains(propertyValue)){
		    
		    itemList.add(propertyValue);
		}
		
		Collections.sort(itemList);

		//the combo box
		final JComboBox combo=new JComboBox(items);
		combo.setFont(theFont);
		combo.setEditable(true);
		
		//sets the selected item
		combo.setSelectedItem(propertyValue);
		
		final ActionListener listener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent evt) {
			    
				String value=""; //$NON-NLS-1$
				
				if(combo.getSelectedItem()!=null){
				    
					try{
						value=(String)combo.getSelectedItem();
					}catch (Exception ex){value="";} //$NON-NLS-1$
				}
					
				//modifies the widgetValue of the property item
				if(value!=null && !value.equals("")){ //$NON-NLS-1$
				    
					propertyItem.changePropertyValue(value.concat("pt")); //$NON-NLS-1$
				}
			}		
		};
		
		//adds a listener to the combo box
		combo.addActionListener(listener);

		//the panel that will be contained in the widget object
		JPanel panel=new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(combo);
		
		component=panel;

		//creates the disposer
		disposer=new Runnable(){

            public void run() {

				combo.removeActionListener(listener);
            }
		};
	}
}

