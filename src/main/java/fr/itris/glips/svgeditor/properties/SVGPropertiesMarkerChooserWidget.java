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
import java.util.List;

import javax.swing.*;

import org.w3c.dom.*;

import com.idbs.svg.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.SVGFrame;

/**
 * @author Jordi SUC
 * @author Liam Lynch (IDBS tweaks)
 */
public class SVGPropertiesMarkerChooserWidget extends SVGPropertiesWidget{

    private JComboBox markerStyleCombo;
    private JComboBox sizeCombo;
    private SVGComboResourceItem selectedItem;
    private final SVGComboResourceItem emptyItem;
    private MarkerSize selectedMarkerSize = null; 
    
    private final SVGEditor editor;
    private final SVGFrame frame;
    private final Map<MarkerSize, List<SVGComboResourceItem>> sizeToMarkerItems = new EnumMap<MarkerSize, List<SVGComboResourceItem>>(MarkerSize.class);
    
    /**
     * the constructor of the class
     * @param propertyItem a property item
     */
	public SVGPropertiesMarkerChooserWidget(SVGPropertyItem propertyItem) {

		super(propertyItem);
		editor = propertyItem.getProperties().getSVGEditor();
		frame = editor.getFrameManager().getCurrentFrame();	
		emptyItem =  new SVGComboResourceItem(frame,  "","", null); //$NON-NLS-1$ //$NON-NLS-2$
		buildComponent();
	}
	
	/**
	 * builds the component that will be displayed
	 */
	protected void buildComponent(){
	    
        String propertyName = propertyItem.getPropertyName();
        String propertyValue = propertyItem.getGeneralPropertyValue();
	    if(propertyValue != null && ! propertyValue.isEmpty() && ! propertyValue.equals("none")){ //$NON-NLS-1$
	        
	        propertyValue=SVGToolkit.toUnURLValue(propertyValue);
	    }
	    
	    populateSizeToMarkerMap(propertyName, propertyValue);			
		
		List<SVGComboResourceItem> curItemList = sizeToMarkerItems.get(selectedMarkerSize);		
		Object[] itemsDisplayed = curItemList.toArray();
		
		//the marker style combo box		
		markerStyleCombo=new JComboBox(itemsDisplayed);
		markerStyleCombo.setFont(theFont);
		markerStyleCombo.setRenderer(new SVGComboResourceCellRenderer());
		markerStyleCombo.setPreferredSize(new Dimension(markerStyleCombo.getWidth(), 24));		
		if(selectedItem!=null)markerStyleCombo.setSelectedItem(selectedItem);						
		markerStyleCombo.addActionListener(styleComboListener);
			
		MarkerSize[] mSizes= MarkerSize.values();
		String [] sizesDisplayed = new String[mSizes.length];
		for (int i = 0; i < sizesDisplayed.length; ++i)
		{
		    sizesDisplayed[i] = mSizes[i].getDisplayName();
		}
		sizeCombo = new JComboBox(sizesDisplayed);
		sizeCombo.addActionListener(sizeComboListener);
		sizeCombo.setSelectedItem(selectedMarkerSize.getDisplayName());
		sizeCombo.setFont(theFont);		
		
		
		//the panel that will be contained in the widget object		
		JPanel panel=new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		String sizeString =  "Size"; //$NON-NLS-1$
		try { editor.getBundle().getString("property_marker_size"); } //$NON-NLS-1$
		catch (MissingResourceException e) { System.out.println("no property: property_marker_size");} //$NON-NLS-1$
		
		String styleString =  "Style"; //$NON-NLS-1$
		try { editor.getBundle().getString("property_marker_style"); } //$NON-NLS-1$
		catch (MissingResourceException e) { System.out.println("no property: property_marker_style");}		 //$NON-NLS-1$
		
		JLabel styleLabel = new JLabel(styleString);
		styleLabel.setFont(theFont);
		styleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(styleLabel);
		
		markerStyleCombo.setAlignmentX(Component.LEFT_ALIGNMENT);		
		panel.add(markerStyleCombo);
		panel.add(Box.createRigidArea(new Dimension(0,8)));
		JLabel sizeLabel = new JLabel(sizeString); 
		sizeLabel.setFont(theFont);
		sizeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(sizeLabel);
		
		sizeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(sizeCombo);				
		
		component=panel;
		
		//creates the disposer
		disposer=new Runnable(){

            public void run() {

				markerStyleCombo.removeActionListener(styleComboListener);
            }
		};
	}

    /**
     * @param propertyName
     * @return
     */
    private void populateSizeToMarkerMap(String propertyName, String propertyValue)
    {
	    selectedMarkerSize = IdbsSvgConstants.defaultMarkerSize(); //changed below if another size was selected before
	    selectedItem = emptyItem;  //changed below if another item was selected before
	    
	    for(MarkerSize markerSize : MarkerSize.values())
	    {
	        extractItemListForMarkerSize(propertyName, propertyValue, markerSize);
	    }		
    }

    /**
     * @param propertyName
     * @param propertyValue
     * @param valuesMap
     * @param labelsMap
     * @param markerSize
     */
    private void extractItemListForMarkerSize(String propertyName, String propertyValue, MarkerSize markerSize)
    {        
        //copies of properties and values maps for UI use
        Map valuesMap=new LinkedHashMap(propertyItem.getPropertyValuesMap());
        Map labelsMap=new LinkedHashMap(propertyItem.getPropertyValuesLabelMap());
        //list of combo items for marker size
	    LinkedList<SVGComboResourceItem> itemList=new LinkedList<SVGComboResourceItem>();
	    //the map associating the id of a resource contained in the "defs" element 
	    //of the svg document of the frame to this resource 
	    Map idsToMarkerElements = null;	    
	    if(frame!=null)
	    {	       
	        //the list of types of resources that should appear in the combo
	        LinkedList resourceTagNames=new LinkedList();
	        resourceTagNames.add("marker");	        	        	         //$NON-NLS-1$
	        Document svgFileDoc = frame.getScrollPane().getSVGCanvas().getDocument();	        
	        
	        //get defs in the file not within group nodes - may be needed to replace an arrow head the way it was
	        idsToMarkerElements = frame.getResourcesFromDefs(svgFileDoc, resourceTagNames);	        	        	        
	        
	        //add std markers that are in the group nodes in the file	        
	        //Examples
	        //<g id="idbs_defs_v1_marker-start_small">
	        //<g id="idbs_defs_v1_marker-end_small">
	        //<g id="idbs_defs_v1_marker-any_medium">	        	        

	        //add those of any marker point, specific to size
	        Element eitherEndLibElement = svgFileDoc.getElementById(IdbsSvgConstants.getRootDefsGroupId() + "_marker-any_" + markerSize.name().toLowerCase());   //$NON-NLS-1$
	        frame.collectResourcesFromElement(eitherEndLibElement , 
	            idsToMarkerElements, 
	            resourceTagNames, 
	            false); //dont need to recurse
	        
	        //specific to given marker position and size
	        Element endSpecificLibElement = svgFileDoc.getElementById(IdbsSvgConstants.getRootDefsGroupId() + "_" + propertyName + "_" + markerSize.name().toLowerCase());	         //$NON-NLS-1$ //$NON-NLS-2$
	        frame.collectResourcesFromElement(endSpecificLibElement, 
	            idsToMarkerElements, 
	            resourceTagNames, 
	            false); //dont need to recurse
	        
	    }
		
		if(idsToMarkerElements != null && ! idsToMarkerElements.isEmpty())
		{
			//fills the values map with the values of the resource ids
			for(Iterator it=idsToMarkerElements.keySet().iterator(); it.hasNext();  )
			{			    
			    String curId=null;
			    try{
			        curId=(String)it.next();
			    }catch (Exception ex){curId=null;}
			    
			    if(curId!=null)
			    {
			        valuesMap.put(curId, curId);
			        //For labels slice off idbs metadata and remove underscores
			        // e.g.
			        //  ":idbs_defs_v1_start_small:Triangle_empty" 
			        // becomes "Triangle empty"			        
			        String label = curId.replaceAll("^idbs.+\\:", ""); //$NON-NLS-1$ //$NON-NLS-2$
			        if ( ! label.equals(curId)) //only remove "_" for IDBS stuff
			        {
			            label = label.replaceAll("\\_", " "); //$NON-NLS-1$ //$NON-NLS-2$
			        }
			        

			        labelsMap.put(curId, label);
			    }
			}
		}

		//an item with an empty string
//		itemList.add(emptyItem);
			
		//builds the list of items (later used for the combo)
		for(Iterator it=valuesMap.keySet().iterator(); it.hasNext();  )
		{		    
		    SVGComboResourceItem item = null;			
			try{
			    String curKey = (String)it.next();
				String value = (String)valuesMap.get(curKey);
								
				Element resource = (Element)idsToMarkerElements.get(curKey);	
				item = new SVGComboResourceItem(frame, value, (String)labelsMap.get(curKey), resource);
				
                if(value!=null && propertyValue!=null && ! "none".equals(value) && value.equals(propertyValue)) //$NON-NLS-1$
                {
                    selectedItem=item;
                    selectedMarkerSize = markerSize;
                }				
				
			}catch (Exception ex){item=null;}
				
			if(item!=null)
			{			   
			    itemList.add(item);
			}
		}
		//add to overall size to markers map
		sizeToMarkerItems.put(markerSize, itemList);
    }
	
    
	
    private ActionListener sizeComboListener=new ActionListener()
    {        
        public void actionPerformed(ActionEvent evt) 
        {
            
            String sizeDisplayName = (String) sizeCombo.getSelectedItem();            
            MarkerSize newSize = MarkerSize.forDisplayName(sizeDisplayName);
            
            if (newSize == selectedMarkerSize) return;
            
            //size must have changed now
            selectedMarkerSize = newSize;
            
            //change list of items
            List<SVGComboResourceItem> newItemList = sizeToMarkerItems.get(selectedMarkerSize);
            DefaultComboBoxModel newModel = new DefaultComboBoxModel(newItemList.toArray());            
            markerStyleCombo.setModel(newModel);
            
            //try to reselect an item with the same display name
            // this may or may not be the very same item (could be the same name, different size)
            String labelPreviouslySelected = selectedItem.toString();                           
            SVGComboResourceItem newlySelectedItem = null;
            
            for(SVGComboResourceItem item : newItemList)
            {
                if(labelPreviouslySelected.equals(item.toString()))
                {
                    newlySelectedItem = item;
                }                
            }
                
            if (newlySelectedItem == null)
            {
                newlySelectedItem = emptyItem;
            }
            
            //has the selection changed?
            if (newlySelectedItem.equals(selectedItem))
            {
                return; //if not nothing more to do                
            }
            
            //Otherwise we must reset things, including the underlying data
            markerStyleCombo.setSelectedItem(newlySelectedItem);                            
            selectedItem = newlySelectedItem;
            
            updatePropertyItem(selectedItem.getValue());
        }
    };
    
	private ActionListener styleComboListener=new ActionListener(){
	    
	    public void actionPerformed(ActionEvent evt) {
	        
            //gets the new value and adds it, and registers the node to the resource manager
            String value="";             //$NON-NLS-1$
            if(markerStyleCombo.getSelectedItem()!=null){
                
                selectedItem = (SVGComboResourceItem)markerStyleCombo.getSelectedItem();
                value = selectedItem.getValue();
            }
            updatePropertyItem(value);
	    }
	};
	
	private void updatePropertyItem(String value)
	{
        //unregisters the nodes with the last value that each of them had
        Node cur=null;
        String val=null;
        
        for(Iterator it=nodesList.iterator(); it.hasNext();){
            
            try{
                cur=(Node)it.next();
            }catch (Exception ex){cur=null;}
            
            if(cur!=null){
                
                val=propertyItem.getPropertyValue(cur);
                val=editor.getSVGToolkit().toUnURLValue(val);
                
                if(val!=null && ! val.equals("") && ! val.equals("none")){ //$NON-NLS-1$ //$NON-NLS-2$
                    
                    frame.removeNodeUsingResource(val, cur);
                }
            }
        }
        
        //modifies the widgetValue of the property item
        if(value!=null && ! value.equals("")){ //$NON-NLS-1$
            
            if(! value.equals("none")){ //$NON-NLS-1$
                
                //registers the id for the list of nodes
                frame.addNodesUsingResource(value, nodesList);                  
                value=editor.getSVGToolkit().toURLValue(value);
            }
            
            propertyItem.changePropertyValue(value);
        }	    
	}
}

