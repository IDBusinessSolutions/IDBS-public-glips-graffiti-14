/*
 * Created on 2 juin 2004
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
import javax.swing.border.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.menutool.*;
import fr.itris.glips.svgeditor.resources.*;
import fr.itris.glips.svgeditor.selection.*;

/**
 * @author Jordi SUC
 *
 * the class allowing to change the properties of a node by modifying its attributes and style elements
 */
public class SVGProperties extends SVGModuleAdapter{
	
	/**
	 * the ids of the module
	 */
	final private String idproperties="Properties"; //$NON-NLS-1$
	
	/**
	 * the labels
	 */
	private String labelproperties=""; //$NON-NLS-1$

	/**
	 * the editor
	 */
	private SVGEditor editor=null;
	
	/**
	 * the document of the properties
	 */
	private Document docProperties=null;

	/**
	 * the font
	 */
	public final Font theFont=new Font("theFont", Font.ROMAN_BASELINE, 10); //$NON-NLS-1$
	
	/**
	 * the nodes that are currently selected
	 */
	private final LinkedList selectedNodes=new LinkedList();
	
	/**
	 * the bundle used to get labels
	 */
	private ResourceBundle bundle=null;
	
	/**
	 * the panel in which the widgets panel will be inserted
	 */
	private JPanel propertiesPanel=new JPanel();
	
	/**
	 * the panel displaying the properties
	 */
	private SVGPropertiesWidgetsPanel widgetsPanel=null;
	
	/**
	 * the frame into which the properties panel will be inserted
	 */
	private SVGToolFrame propertiesFrame;
	
	/**
	 * the name of the last selected tab
	 */
	private String tabId=""; //$NON-NLS-1$
	
    private final static int DEFAULT_PROPERTY_PANEL_HEIGHT = 452;
    private final static int DEFAULT_PROPERTY_PANEL_WIDTH = 240;
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGProperties(SVGEditor editor) {
	
		this.editor=editor;
		
		//gets the labels from the resources
        bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labelproperties=bundle.getString("label_properties"); //$NON-NLS-1$
			}catch (Exception ex){}
		}

		//a listener that listens to the changes of the SVGFrames
		final ActionListener svgframeListener=new ActionListener(){
			
			/**
			 * a listener on the selection changes
			 */
			private ActionListener selectionListener=null;
			
			/**
			 * the current selection module
			 */
			private SVGSelection selection=null;

			public void actionPerformed(ActionEvent e) {
			
				//removes the widgets panel
				if(widgetsPanel!=null){

				    propertiesPanel.removeAll();
				    widgetsPanel.dispose();
				    widgetsPanel=null;
				}
				
				//clears the list of the selected items
				selectedNodes.clear();
				
				final SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
				
				//if a selection listener is already registered on a selection module, it is removed	
				if(selection!=null && selectionListener!=null){
				    
					selection.removeSelectionListener(selectionListener);
				}

				//gets the current selection module
				if(frame!=null){
				    
					selection=getSVGEditor().getSVGSelection();
				}
				
				if(frame!=null && selection!=null){
				    
					manageSelection();
					
					//the listener of the selection changes
					selectionListener=new ActionListener(){

						public void actionPerformed(ActionEvent evt) {
						    
							manageSelection();
						}
					};
					
					//adds the selection listener
					if(selectionListener!=null){
					    
						selection.addSelectionListener(selectionListener);
					}
					
				}else if(propertiesPanel.isVisible()){
				    
					handleProperties(null);
				}
			}	
			
			/**
			 * updates the selected items and the state of the menu items
			 */
			protected void manageSelection(){
				
				LinkedList list=null;
				
				//gets the currently selected nodes list 
				if(selection!=null){
				    
					list=selection.getCurrentSelection(getSVGEditor().getFrameManager().getCurrentFrame());
				}

				selectedNodes.clear();
				
				//refresh the selected nodes list
				if(list!=null){
				    
				    selectedNodes.addAll(list);
				}
				
				if(selectedNodes.size()>=1){
				    
					if(propertiesPanel.isVisible()){
					    
						handleProperties(selectedNodes);
					}
					
				}else if(propertiesPanel.isVisible()){
				    
					handleProperties(null);		
				}
			}
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);
		
		//setting the layout for the properties panel
		propertiesPanel.setLayout(new BoxLayout(propertiesPanel, BoxLayout.X_AXIS));

		//creating the document
		docProperties=SVGResource.getXMLDocument("properties.xml"); //$NON-NLS-1$
		docProperties=normalizeXMLProperties(docProperties);

		//creating the internal frame containing the properties panel
		propertiesFrame=new SVGToolFrame(editor, idproperties, labelproperties, propertiesPanel);
		
		//setting the visibility changes handler
		Runnable visibilityRunnable=new Runnable(){
			
			public void run() {

    			if(selectedNodes.size()>=1){

    				handleProperties(selectedNodes);
    				
    			}else{
    			    
    				handleProperties(null);
    			}	
			}
		};
		
		this.propertiesFrame.setVisibilityChangedRunnable(visibilityRunnable);
	}
	
	/**
	 * @return the editor
	 */
	public SVGEditor getSVGEditor(){
		return editor;
	}
	
	@Override
	public Hashtable<String, JMenuItem> getMenuItems() {

        Hashtable<String, JMenuItem> menuItems=new Hashtable<String, JMenuItem>();
        menuItems.put("ToolFrame_"+this.idproperties, propertiesFrame.getMenuItem()); //$NON-NLS-1$
        return menuItems;
	}
	
	@Override
	public SVGToolFrame getToolFrame() {
		return propertiesFrame;
	}

	/**
	 * @return Returns the selectedNodes.
	 */
	protected LinkedList getSelectedNodes() {
		return selectedNodes;
	}

	/**
	 * @return Returns the tabId.
	 */
	protected String getTabId() {
		return tabId;
	}
	
	/**
	 * @param tabId The tabId to set.
	 */
	protected void setTabId(String tabId) {
		this.tabId = tabId;
	}
	
	/**
	 * the method that manages the changes of the properties for the given nodes
	 * @param list a list of nodes
	 */
	protected void handleProperties(LinkedList list){
        int height = propertiesPanel.getHeight();
        int width = propertiesPanel.getWidth();
		//removes the widgets panel
		if(widgetsPanel!=null){
		
		    propertiesPanel.removeAll();
		    widgetsPanel.dispose();
		    widgetsPanel=null;
		}
		
		final SVGFrame frame=editor.getFrameManager().getCurrentFrame();

		if(frame!=null && list!=null && list.size()>0){
		    
			LinkedList snodes=new LinkedList(list);

			//gets the accurate subtree given the list of nodes
			Node treeProperties=getXMLProperties(snodes);
			
			if(treeProperties!=null){
			    
				//the map associating a tab to its label and the map associating a tab to a list of property item objects
				LinkedHashMap tabMap=new LinkedHashMap(), propertyItemsMap=new LinkedHashMap();
		
				if(treeProperties!=null){

					String name="", label=""; //$NON-NLS-1$ //$NON-NLS-2$
					LinkedList propertyItemsList=null;
					
					//builds the list of the tabs 
					for(Node cur=treeProperties.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
					    
						if(cur.getNodeName().equals("tab")){ //$NON-NLS-1$
						    
							name=((Element)cur).getAttribute("name"); //$NON-NLS-1$
					
							if(name!=null && ! name.equals("")){ //$NON-NLS-1$
				
								//gets the label of the tab and puts it into the map
								if(bundle!=null){
								    
									try{label=bundle.getString(name);}catch (Exception ex){label=null;}
								}
								
								if(label==null || (label!=null && label.equals(""))){ //$NON-NLS-1$
								    
								    label=name;
								}
						
								tabMap.put(name, label);
				
								//gets the property items list and puts it into the map
								propertyItemsList=getPropertyList(snodes, cur);
								propertyItemsMap.put(name, propertyItemsList);
							}
						}		
					}
		
					//creates the panel
					if(tabMap!=null && propertyItemsMap!=null && tabMap.size()>0 && propertyItemsMap.size()>0){
					    
						if(widgetsPanel!=null){

						    widgetsPanel.dispose();
						}

						widgetsPanel=new SVGPropertiesWidgetsPanel(this, tabMap, propertyItemsMap);
					} 
					
					//adds the property panel into the container and displays it
					if(propertiesPanel!=null && widgetsPanel!=null){

					    propertiesPanel.removeAll();
					    propertiesPanel.add(widgetsPanel);
					    propertiesFrame.revalidate();
					    
						return;
					}
				}
			}
		}
		
		if(bundle!=null){

			//initializes the value of the last selected tab
			tabId=""; //$NON-NLS-1$
			String message=""; //$NON-NLS-1$
			
			try{
				if(selectedNodes.size()<1){
				    
				    message=bundle.getString("property_empty_dialog_none"); //$NON-NLS-1$
				    
				}else if(selectedNodes.size()==1){
				    
				    message=bundle.getString("property_empty_dialog_one"); //$NON-NLS-1$
				    
				}else if(selectedNodes.size()>1){
				    
				    message=bundle.getString("property_empty_dialog_many"); //$NON-NLS-1$
				}
			}catch (Exception ex){}
			JLabel label=new JLabel(message);
            label.setBorder(new EmptyBorder(5, 5, 5, 5));
			label.setFont(theFont);

			propertiesPanel.removeAll();
            if ((height==0) || (width==0) ) 
            {
                /** set a default height */
                height=DEFAULT_PROPERTY_PANEL_HEIGHT;
                width=DEFAULT_PROPERTY_PANEL_WIDTH;
            }
            propertiesPanel.setPreferredSize(new Dimension(width,height));
			propertiesPanel.add(label);
			propertiesFrame.revalidate();
		}
	}
	
	/**
	 * normalizes the xml document
	 * @param doc the raw xml document
	 * @return the normalized document
	 */
	public Document normalizeXMLProperties(Document doc){
		
		//modifies the document to resolve the links within the dom
		//each tab node is appended to the node which have declared having a child whose type is like one of the predefined tabs
		if(doc!=null){
		    
			Node root=doc.getDocumentElement();
		
			if(root!=null){
			    
				//the map associating the name attributes of a tab to its tab node
				Hashtable tabMap=new Hashtable();
			
				//the list of the "define" nodes
				LinkedList defineList=new LinkedList();
			
				//finds the node that contains the predefined node tabs, and adds all its children to the tab map
				Node cur=null, tab=null;
				String name=""; //$NON-NLS-1$

				for(cur=root.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
				
					if(cur.getNodeName().equals("define")){ //$NON-NLS-1$
					    
						//adds the "define" node to the list
						defineList.add(cur);
					
						//adds all the predefined "tab" nodes to the map
						for(tab=cur.getFirstChild(); tab!=null; tab=tab.getNextSibling()){
						    
							if(tab.getNodeName().equals("tab")){ //$NON-NLS-1$

								try{name=((Element)tab).getAttribute("name");}catch (Exception ex){name="";} //$NON-NLS-1$ //$NON-NLS-2$
							
								if(name!=null && ! name.equals("")){ //$NON-NLS-1$
								    
								    tabMap.put(name, tab.cloneNode(true));
								}
							}
						}
					}
				}
			
				//removes all the "define" nodes from the root node
				for(Iterator it=defineList.iterator(); it.hasNext();){
				    
					try{root.removeChild((Node)it.next());}catch (Exception ex){}
				}
				
				defineList.clear();

				//the list of the "tab" nodes to add to the "module" node
				LinkedList tabList=new LinkedList();
				Node use=null;
			
				//appends the tab nodes to the node that define a link to a predefined tab
				for(cur=root.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
				
					if(cur.getNodeName().equals("module")){ //$NON-NLS-1$
					
						//gets the "use" nodes defined in the "module" node and the "tab" nodes that will be used to replace them
						for(use=cur.getFirstChild(); use!=null; use=use.getNextSibling()){
						    
							if(use.getNodeName().equals("use")){ //$NON-NLS-1$
							    
								try{name=((Element)use).getAttribute("name");}catch(Exception ex){name="";} //$NON-NLS-1$ //$NON-NLS-2$
							
								//adds the tab node to the list
								if(name!=null && !name.equals("")){ //$NON-NLS-1$
								    
									tab=(Node)tabMap.get(name);
									
									if(tab!=null){
									    
									    tabList.add(tab.cloneNode(true));
									}
								}
								
							}else if(use.getNodeName().equals("tab")){ //$NON-NLS-1$
							    
								tabList.add(use.cloneNode(true));
							}
						}
					
						//removes all the child nodes from the "module" node
						for(tab=cur.getFirstChild(); tab!=null; tab=cur.getFirstChild()){
						    
							cur.removeChild(tab);
						}
						
						//appends all the "tab" nodes to the "module" node
						
						for(Iterator it=tabList.iterator(); it.hasNext();){
						    
							try{cur.appendChild((Node)it.next());}catch (Exception ex){}
						}
					}
					
					//initializes the list of "tab" nodes
					tabList.clear();
				}
			}
		}

		return doc;
	}
	
	/**
	 *  gets the subtree describing the properties that can be changed for the given list of nodes
	 * @param list a list of nodes
	 * @return the subtree
	 */
	protected Node getXMLProperties(LinkedList list){
		
		Node subTree=null;
		
		if(list !=null && list.size()>0){

			Node current=null;
			String name=""; //$NON-NLS-1$
			Iterator it=list.iterator();
			
			try{
				current=(Node)it.next();
				subTree=getXMLProperties(current);
				name=current.getNodeName();
			}catch (Exception ex){return null;}
			
			while(it.hasNext()){
			    
				try{
					current=(Node)it.next();
				}catch (Exception ex){current=null;}
				
				if(current!=null){
					
					if(name!=null && ! name.equals(current.getNodeName())){
					    
					    subTree=intersectXMLProperties(subTree, getXMLProperties(current));
					}
					
					name=current.getNodeName();
				}
			}
		}
		
		return subTree;
	}
	
	/**
	 * gets a subtree that contains the nodes that can be found in the node1 subtree AND in the node2 subtree
	 * @param node1 a subtree
	 * @param node2 a subtree
	 * @return a subtree that contains the nodes that can be found in the node1 subtree AND in the node2 subtree
	 */
	protected Node intersectXMLProperties(Node node1, Node node2){
	    
		Node node=null;
		
		if(node1!=null && node2!=null){
		    
			//clones the node
			node=node1.cloneNode(false);
			
			//removes all of its children
			while(node.hasChildNodes()){
			    
			    node.removeChild(node.getFirstChild());
			}
			
			Node 	tab=null, ctab=null, tab2=null, 
						property=null, cproperty=null, property2=null;
			String name=null;
			
			for(tab=node1.getFirstChild(); tab!=null; tab=tab.getNextSibling()){

				if(tab.getNodeName().equals("tab")){ //$NON-NLS-1$
				    
					//for each tab in node1, tests if it is in node2
					if((tab2=getTab(node2, tab))!=null){
					    
						//clones the node
						ctab=tab.cloneNode(false);
						
						//removes all of its children
						while(ctab.hasChildNodes()){
						    
						    ctab.removeChild(ctab.getFirstChild());
						}
						
						node.appendChild(ctab);
						
						//for each property in the current tab in node1, tests if it is in node2
						for(property=tab.getFirstChild(); property!=null;property=property.getNextSibling()){
							
							if((property2=getProperty(tab2, property))!=null){
							    
								//clones the node
								cproperty=property.cloneNode(true);
								//appends it to the tab
								ctab.appendChild(cproperty);	
							}
						}
					}
				}
			}
		}
		
		return node;
	}
	
	/**
	 * 
	 * @param node a subtree
	 * @param tab a node whose name is "tab"
	 * @return the tab node whose attribute name is equal to the attribute name of the given tab or null if the node does not contain the tab
	 */
	protected Node getTab(Node node, Node tab){
		
		if(node!=null && tab!=null && tab.getNodeName().equals("tab")){ //$NON-NLS-1$
		    
			Node tab2=null;
			String tabName=""; //$NON-NLS-1$
			
			//gets the value of the name attribute of the tab
			try{
				Element element=(Element)tab;
				tabName=element.getAttribute("name"); //$NON-NLS-1$
			}catch (Exception ex){tabName="";} //$NON-NLS-1$
			
			if(tabName!=null && ! tabName.equals("")){ //$NON-NLS-1$
				
				String name=""; //$NON-NLS-1$

				//for each tab in node2, test if it has the same name as the given tab
				for(tab2=node.getFirstChild(); tab2!=null; tab2=tab2.getNextSibling()){
					
					if(tab2.getNodeName().equals("tab")){ //$NON-NLS-1$
					    
						//gets the value of the name attribute of the tab
						try{
							Element element=(Element)tab2;
							name=element.getAttribute("name"); //$NON-NLS-1$
						}catch (Exception ex){name="";} //$NON-NLS-1$
						
						if(name!=null && name.equals(tabName)){
						    
						    return tab2;
						}
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * @param tab the tab in which the property is contained
	 * @param property the property node
	 * @return the property node whose attribute name is equal to the attribute name of the given property 
	 * or null if the node does not contain the property
	 */
	protected Node getProperty(Node tab, Node property){
		
		if(tab!=null && property!=null && property instanceof Element){
			
			Node prop=null;
			String propertyName="", propertyType="", propertyName2="", propertyType2=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			
			//gets the name and type of the given property
			try{
				Element element=(Element)property;
				propertyName=element.getAttribute("name"); //$NON-NLS-1$
				propertyType=element.getAttribute("type"); //$NON-NLS-1$
			}catch (Exception ex){return null;}
			
			if(propertyName!=null && ! propertyName.equals("") && propertyType!=null && ! propertyType.equals("")){ //$NON-NLS-1$ //$NON-NLS-2$

				for(prop=tab.getFirstChild(); prop!=null; prop=prop.getNextSibling()){
				    
					//if the node is a property node
					if(prop.getNodeName().equals("property")){ //$NON-NLS-1$
						//gets the name and type of the current node
						try{
							Element element=(Element)prop;
							propertyName2=element.getAttribute("name"); //$NON-NLS-1$
							propertyType2=element.getAttribute("type"); //$NON-NLS-1$
						}catch (Exception ex){propertyName2=null; propertyType2=null;}
						
						//tests if the current property name and type are equal to the gievn property name and type
						if(propertyName2!=null && propertyType2!=null 
							&& propertyName2.equals(propertyName) && propertyType2.equals(propertyType)){
						    
								return prop;
						}
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * gets the subtree describing the properties that can be changed for the type of the given node
	 * @param node a node
	 * @return the subtree
	 */
	protected Node getXMLProperties(Node node){
		
		if(docProperties!=null && node!=null){
		    
			String name=node.getNodeName();
			
			if(name!=null && ! name.equals("")){ //$NON-NLS-1$
			    
				Document docPrp=(Document)docProperties.cloneNode(true);
				Element root=docPrp.getDocumentElement();
				
				if(root!=null){
				    
				    Node current=null;
					String cname=""; //$NON-NLS-1$
					
					//for each root child, searches the one whom value of the "name" attribute is the type of the given node
					for(current=root.getFirstChild(); current!=null; current=current.getNextSibling()){
					    
						if(current.getNodeName().equals("module")){ //$NON-NLS-1$
						    
							cname=((Element)current).getAttribute("name"); //$NON-NLS-1$
							
							if(cname!=null && cname.equals(name)){
							    
							    break;
							}
						}
					}
					
					if(current!=null){
					    
						return current;
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * @param nodelist the list of the selected nodes
	 * @param subTree a node 
	 * @return the list of the property item objects
	 */
	protected LinkedList getPropertyList(LinkedList nodelist, Node subTree){
		
		LinkedList list=new LinkedList();
		
		if(subTree!=null){
		    
			Node cur=null;
			SVGPropertyItem item=null;

			//for each property node
			for(cur=subTree.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
			    
				//get the property item object
				if(cur.getNodeName().equals("property")){ //$NON-NLS-1$
				    
					//get the property item object
					item=getProperty(nodelist, cur);
					
					//adds it to the list
					if(item!=null)list.add(item);
				}
			}
		}
		
		if(list!=null && list.size()>0){
		    
		    return list;
		}
		
		return null;
	}
	
	/**
	 * creates the property item object given a property node
	 * @param nodelist the list of the selected nodes
	 * @param property a property node
	 * @return a SVGPropertyItem object
	 */
	protected SVGPropertyItem getProperty(LinkedList nodelist, Node property){
		
		SVGPropertyItem propertyItem=null;
		
		if(property!=null){
			
			String type="", name="", valueType="", defaultValue="", constraint=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			
			//the attributes of the property node
			type=((Element)property).getAttribute("type"); //$NON-NLS-1$
			name=((Element)property).getAttribute("name"); //$NON-NLS-1$
			valueType=((Element)property).getAttribute("valuetype"); //$NON-NLS-1$
			defaultValue=((Element)property).getAttribute("defaultvalue"); //$NON-NLS-1$
			constraint=((Element)property).getAttribute("constraint"); //$NON-NLS-1$
			
			LinkedHashMap values=null;
			
			if(property.hasChildNodes()){
			    
				//fills the map with the attributes of each possible value for the property item
				values=new LinkedHashMap();
				String itemName="", itemValue="";  //$NON-NLS-1$ //$NON-NLS-2$
				Node cur;
				
				for(cur=property.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
				    
					if(cur.getNodeName().equals("item")){ //$NON-NLS-1$
					    
						//the attributes of the item
						itemName=((Element)cur).getAttribute("name"); //$NON-NLS-1$
						itemValue=((Element)cur).getAttribute("value"); //$NON-NLS-1$
						
						if(itemName!=null && ! itemName.equals("") && itemValue!=null && !itemValue.equals("")){ //$NON-NLS-1$ //$NON-NLS-2$
						    
							values.put(itemName, itemValue);
						}
					}
				}
				
				//if the map is empty, it is set to null
				if(values.size()<=0){
				    
				    values=null;
				}
			}
			
			//creates the property item object
			if(type!=null && name!=null && valueType!=null && ! type.equals("") && ! name.equals("") && ! valueType.equals("")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
			    propertyItem=new SVGPropertyItem(this, nodelist, type, name, valueType, defaultValue, constraint, values);
			}
		}

		return propertyItem;
	}
	
	/**
	 * gets the name of the module
	 * @return the name of the module
	 */
	public String getName(){
	    
		return idproperties;
	}

	/**
	 * cancels all the actions that could be running
	 */
	public void cancelActions(){
	}

}
