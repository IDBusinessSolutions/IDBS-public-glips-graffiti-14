/*
 * Created on 26 août 2004
 * 
 =============================================
 GNU LESSER GENERAL PUBLIC LICENSE Version 2.1
 =============================================
 GLIPS Graffiti Editor, a SVG Editor
 Copyright (C) 2003 Jordi SUC, Philippe Gil, SARL ITRIS
 
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
package fr.itris.glips.svgeditor.visualresources;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 * the class of the widgets that will be displayed in the properties dialog, and enabling to modify the properties (with a combo)
 * of an attribute (or a group of attributes) of a resource node.
 * 
 * @author Jordi SUC
 */
public class SVGVisualResourceComboAttributeWidget extends SVGVisualResourceAttributeWidget{

	/**
	 * the constructor of the class
	 * @param resourceObjectAttribute the attribute object that will be modified
	 */
    public SVGVisualResourceComboAttributeWidget(SVGVisualResourceObjectAttribute resourceObjectAttribute) {

        super(resourceObjectAttribute);
        
        buildComponent();
    }
    
    /**
     * builds the component to be displayed
     */
    protected void buildComponent(){
        
        if(resourceObjectAttribute!=null){

            //the list of the items
            final LinkedList itemList=new LinkedList();
            
            //the map of the possible values
            LinkedHashMap possibleValues=resourceObjectAttribute.getModel().getPossibleValues();

            SVGComboItem item=null, selectedItem=null;
            String nm="", val="", lbl=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            
            //an item with an empty string
            item=new SVGComboItem("",""); //$NON-NLS-1$ //$NON-NLS-2$
            itemList.add(item);
            
            //builds the array of items for the combo
            for(Iterator it=possibleValues.keySet().iterator(); it.hasNext();){

                try{
                    nm=(String)it.next();
                    val=(String)possibleValues.get(nm);
                    
                    //gets the label
                    lbl=nm;
                    
                    if(bundle!=null){
                        
                        lbl=bundle.getString(nm);
                    }
                    
                    item=new SVGComboItem(val, lbl);
                    
                    //checks if the current possible value is equals to the current value of the property
                    if(val!=null && val.equals(resourceObjectAttribute.getValue())){
                        
                        selectedItem=item;
                    }
                    
                }catch (Exception ex){item=null; nm="";} //$NON-NLS-1$
                
                //adds the item to the list
                if(item!=null){
                    
                    itemList.add(item);
                }
            }
            
            Object[] items=itemList.toArray();
            
            //the combo box
            final JComboBox combo=new JComboBox(items);
            combo.setFont(theFont);
            
            //sets the selected item
            if(selectedItem!=null)combo.setSelectedItem(selectedItem);
            
            //the listener to the combo
            final ActionListener comboListener=new ActionListener(){
                
                public void actionPerformed(ActionEvent evt) {
                    
                    String val=""; //$NON-NLS-1$
                    
                    if(combo.getSelectedItem()!=null){
                        
                        val=((SVGComboItem)combo.getSelectedItem()).getValue();
                    }
                    
                    //modifies the widgetValue of the property item
                    if(val!=null && ! val.equals("")){ //$NON-NLS-1$
                        
                        resourceObjectAttribute.setValue(val);
                    }
                }
            };
            
            //adds a listener to the combo box
            combo.addActionListener(comboListener);
            
            //the panel containing the combo
            JPanel comboPanel=new JPanel();
            comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.X_AXIS));
            JPanel cPanel=new JPanel();
            cPanel.setLayout(new BoxLayout(cPanel, BoxLayout.X_AXIS));
            cPanel.setBorder(new EmptyBorder(0, 20, 0, 0));
            cPanel.add(combo);
            comboPanel.add(cPanel);
            
            component=comboPanel;
            
            disposer=new Runnable(){

                public void run() {

                    //removes the combo listener
                    combo.removeActionListener(comboListener);
                    itemList.clear();
                }
            };
        }
    }
}
