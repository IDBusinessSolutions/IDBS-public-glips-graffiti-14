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

import java.util.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.undoredo.*;

/**
 * @author Jordi SUC
 *
 * The class allowing to know the name and value of a property
 */
public class SVGPropertyItem{
		
	/**
	 * the list of the selected nodes
	 */
	private LinkedList nodeList;
		
	/**
	 * the type, the name and the value of the property, the constraint linked with this property the generalPopertyValue is the value that will be displayed for the whole nodes of the list
	 */
	private String propertyType, propertyName, propertyValueType, defaultPropertyValue="", propertyConstraint="", generalPropertyValue=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
	/**
	 * the labels associated with the property
	 */
	private String propertyLabel;
	
	/**
	 * the map associating a node to the value of the property
	 */
	private LinkedHashMap propertyValues=new LinkedHashMap();
		
	/**
	 * the map associating one value name of the property to its value
	 */
	private LinkedHashMap valuesMap;
	
	/**
	 * the map associating one value name of the property to its label
	 */
	private LinkedHashMap valuesLabelMap;
	
	/**
	 * the undo/redo labels
	 */
	private String undoredoproperties=""; //$NON-NLS-1$
	
	/**
	 * the bundle used to get labels
	 */
	private ResourceBundle bundle=null;
	
	/**
	 * the properties object
	 */
	private SVGProperties properties=null;
		
	/**
	 *  the constructor of the class
	 * @param properties the properties object
	 * @param nodeList the list of the nodes
	 * @param propertyType the type of the property
	 * @param propName the name of the property
	 * @param propertyValueType the name of the widget that has to be used to modify the property
	 * @param defaultPropertyValue the default value of the property
	 * @param propertyConstraint whether the property is required or not
	 * @param valuesMap the map associated the name of a value to its value 
	 */
	public SVGPropertyItem(SVGProperties properties, LinkedList nodeList, String propertyType, String propName, String propertyValueType, String defaultPropertyValue, String propertyConstraint, LinkedHashMap valuesMap){
		
		this.properties=properties;
		this.nodeList=nodeList;
		this.propertyType=propertyType;
		
		try{
			this.propertyName=propName.substring(propName.indexOf("_")+1, propName.length()); //$NON-NLS-1$
		}catch (Exception ex){this.propertyName=propName;}
		
		this.propertyValueType=propertyValueType;
		this.defaultPropertyValue=defaultPropertyValue;
		this.propertyConstraint=propertyConstraint;
		this.valuesMap=valuesMap;
		
		this.bundle=SVGEditor.getBundle();
		
		//gets the property label from the resources
		if(bundle!=null){
		    
			try{
				propertyLabel=bundle.getString(propName);
				undoredoproperties=bundle.getString("undoredoproperties"); //$NON-NLS-1$
			}catch (Exception ex){}
			
			if(propertyLabel==null || (propertyLabel!=null && propertyLabel.equals(""))){ //$NON-NLS-1$
			    
			    propertyLabel=this.propertyName;
			}
		}
		
		//fils the valuesLabelMap with the labels associated with each value name of a property if it exits
		if(valuesMap!=null && valuesMap.size()>0 && bundle!=null){
		    
			valuesLabelMap=new LinkedHashMap();
			String name="", label=""; //$NON-NLS-1$ //$NON-NLS-2$
			
			for(Iterator it=valuesMap.keySet().iterator(); it.hasNext();){
			    
				try{
					name=(String)it.next();
				}catch (Exception ex){name=null;}
				
				if(name!=null && ! name.equals("")){ //$NON-NLS-1$
				    
					try{
						label=bundle.getString(name);
					}catch (Exception ex){label="";} //$NON-NLS-1$
					
					//if no label has been found, the label is set to the name
					if(label==null || (label!=null && label.equals(""))){ //$NON-NLS-1$
					    
					    label=name;
					}
					
					valuesLabelMap.put(name, label);
				}
			}
		}

		//sets the value of the property taking it from the node attributes
		if(propertyType!=null && this.propertyName!=null){
		    
			if(propertyType.equals("style")){ //$NON-NLS-1$
			    
				generalPropertyValue=getStylePropertyValue(this.propertyName);
				
			}else if (propertyType.equals("attribute")){ //$NON-NLS-1$
			    
				generalPropertyValue=getAttributeValue(this.propertyName);
				
			}else if (propertyType.equals("child")){ //$NON-NLS-1$
			    
				generalPropertyValue=getChildValue(this.propertyName);
			}
            else if (propertyType.equals("text-child")){ //$NON-NLS-1$
                
                generalPropertyValue=getTextChildValue(this.propertyName);
            }
		}	
	}

	/**
	 * @return Returns the properties.
	 */
	public SVGProperties getProperties() {
		return properties;
	}
	
	/**
	 * @return the list of the nodes
	 */
	public Collection getNodeList(){
		return nodeList;
	}
		
	/**
	 * @return the type of the property
	 */
	public String getPropertyType(){
		return propertyType;
	}
		
	/**
	 * @return the name of the property
	 */
	public String getPropertyName(){
		return propertyName;
	}
	
	/**
	 * @return the valueType of the property
	 */
	public String getPropertyValueType(){
		return propertyValueType;
	}
	
	/**
	 * @return the default value of the property
	 */
	public String getDefaultPropertyValue(){
		return defaultPropertyValue;
	}
	
	/**
	 * @return the property constraint (normal or required)
	 */
	public String getPropertyConstraint(){
		return propertyConstraint;
	}
		
	/**
	 * @return the value of the property
	 */
	public String getGeneralPropertyValue(){
		return generalPropertyValue;
	}
	
	/**
	 * returns the value of the property for a given node
	 * @param node a node
	 * @return the value of the property corresponding to the node
	 */
	public String getPropertyValue(Node node){
	    
	    String val=""; //$NON-NLS-1$
	    
	    if(node!=null){
	        
	        try{
	            
	            val=(String)propertyValues.get(node);
	        }catch (Exception ex){}
	    }
	    
	    return val;
	}
		
	/**
	 * @return the label of the property
	 */
	public String getPropertyLabel(){
		return propertyLabel;
	}
		
	/**
	 * @return the map associating a value name of the property to its value
	 */
	public LinkedHashMap getPropertyValuesMap(){
		return valuesMap;
	}
		
	/**
	 * @return the map associating a value name of the property to its label
	 */
	public LinkedHashMap getPropertyValuesLabelMap(){
		return valuesLabelMap;
	}

	/**
	 * sets the value of the property, the value is taken from the value given by the widget
	 * @param value the new value for the property
	 */
	public void changePropertyValue(String value){
		
		if((value==null || (value!=null && value.equals("")) && (propertyConstraint!=null && propertyConstraint.equals("required")))){ //$NON-NLS-1$ //$NON-NLS-2$
				
			value=defaultPropertyValue;
		}
		
		//the current SVGFrame
		final SVGFrame frame=properties.getSVGEditor().getFrameManager().getCurrentFrame();
		
		if(frame!=null && propertyType!=null){

			final SVGCanvas canvas=frame.getScrollPane().getSVGCanvas();
			
			final LinkedHashMap oldPropertyValues=new LinkedHashMap(propertyValues);
			
			//creates a new values map associating a node to the current widgetPropertyValue
			LinkedHashMap values=new LinkedHashMap();
			Node node=null;
			
			for(Iterator it=propertyValues.keySet().iterator(); it.hasNext();){
			    
				try{node=(Node)it.next();}catch(Exception ex){node=null;}
				
				if(node!=null){
				    
					values.put(node, value);
				}
			}
			
			//the maps that will be used for the undo/redo action
			final LinkedHashMap oldValues=new LinkedHashMap(propertyValues);
			final LinkedHashMap newValues=new LinkedHashMap(values);
			final LinkedHashMap fvalues=values;
			
			Runnable runnable=new Runnable(){
			    
			    public void run(){
			    
					//sets the new value taken from the widget and gets the new value once set from the node
					if(propertyType.equals("style")){ //$NON-NLS-1$
					    
						setStylePropertyValue(propertyName, fvalues);
						
					}else if(propertyType.equals("attribute")){ //$NON-NLS-1$
					    
						setAttributeValue(propertyName, fvalues);
						
					}else if(propertyType.equals("child")){ //$NON-NLS-1$
					    
						setChildValue(propertyName, fvalues);
					}else if(propertyType.equals("text-child")){ //$NON-NLS-1$
                    
					    setTextChildValue(propertyName, fvalues);
					}
					
					frame.getScrollPane().getSVGCanvas().delayedRepaint();	
				}
			};
				
			frame.enqueue(runnable);
			frame.setModified(true);

			//the undo/redo action that will be used to undo or redo the changes of the value for this property
			SVGUndoRedoAction action=new SVGUndoRedoAction(""){ //$NON-NLS-1$

				public void undo(){
				    
					//sets the old value
					if(propertyType.equals("style")){ //$NON-NLS-1$
					    
						setStylePropertyValue(propertyName, oldValues);
						
					}else if(propertyType.equals("attribute")){ //$NON-NLS-1$
					    
						setAttributeValue(propertyName, oldValues);
						
					}else if(propertyType.equals("child")){ //$NON-NLS-1$
					    
						setChildValue(propertyName, oldValues);
						
                    }else if(propertyType.equals("text-child")){ //$NON-NLS-1$
                        
                        setTextChildValue(propertyName, oldValues);
                    }					
					
					//redraws the window
					properties.handleProperties(properties.getSelectedNodes());
				}

				public void redo(){
				    
					//sets the new value
					if(propertyType.equals("style")){ //$NON-NLS-1$
					    
						setStylePropertyValue(propertyName, newValues);
						
					}else if(propertyType.equals("attribute")){ //$NON-NLS-1$
					    
						setAttributeValue(propertyName, newValues);
						
					}else if(propertyType.equals("child")){ //$NON-NLS-1$
					    
						setChildValue(propertyName, newValues);
						
                    }else if(propertyType.equals("text-child")){ //$NON-NLS-1$
                        
                        setTextChildValue(propertyName, newValues);
                    }
					//redraws the window
					properties.handleProperties(properties.getSelectedNodes());
				}
			};
			
			//creates the undo/redo list so that actions can be added to it
			SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList(undoredoproperties);
			actionlist.add(action);
			
			if(properties.getSVGEditor().getUndoRedo()!=null){
			    
				properties.getSVGEditor().getUndoRedo().addActionList(frame, actionlist); 
			}
		}
	}
	
	/**
	 * @param name the name of the property in the style attribute
	 * @return the value of he property
	 */
	public String getStylePropertyValue(String name){
		
		//clears the map associating a node to its value
		propertyValues.clear();

		if(nodeList!=null){
		    
			String value=""; //$NON-NLS-1$
			Element element=null;
			
			//for each node in the list
			for(Iterator it=nodeList.iterator(); it.hasNext();){
			    
				try{element=(Element)it.next();}catch (Exception ex){element=null;}
			
				value=properties.getSVGEditor().getSVGToolkit().getStyleProperty(element, name);
				
				if(value==null || (value!=null && value.equals(""))){ //$NON-NLS-1$
				    
				    value=defaultPropertyValue;
				}
				
				if(element!=null){
				    
					propertyValues.put(element, value);
				}
			}
		}
		
		//the value that will be returned
		String returnedValue=""; //$NON-NLS-1$
		
		//if the list contains a single element, its value will be returned, otherwise the empty string is returned
		if(nodeList.size()==1){
		    
			try{
				returnedValue=(String)propertyValues.get(nodeList.getFirst());
			}catch (Exception ex){returnedValue="";} //$NON-NLS-1$
		}

		return returnedValue;
	}
		
	/**
	 * @param name the name of the attribute 
	 * @return the value of the attribute
	 */
	public String getAttributeValue(String name){
		
		//clears the map associating a node to its value
		propertyValues.clear();
		
		String value=""; //$NON-NLS-1$
		Element element=null;
			
		//for each node in the list
		for(Iterator it=nodeList.iterator(); it.hasNext();){
		    
			try{
				element=(Element)it.next();
			}catch (Exception ex){element=null;}
			
			if(element!=null && name!=null && ! name.equals("")){ //$NON-NLS-1$
			    
				value=element.getAttribute(name);
			}
				
			if(value==null || (value!=null && value.equals(""))){ //$NON-NLS-1$
			    
			    value=defaultPropertyValue;
			}
				
			if(element!=null){
			    
				propertyValues.put(element, value);
			}
		}
		//the value that will be returned
		String returnedValue=""; //$NON-NLS-1$
			
		//if the list contains a single element, its value will be returned, otherwise the empty string is returned
		if(nodeList.size()==1){
		    
			try{
				returnedValue=(String)propertyValues.get(nodeList.getFirst());
			}catch (Exception ex){returnedValue="";} //$NON-NLS-1$
		}

		return returnedValue;
	}
	
	/**
	 * @param name the name of the child node
	 * @return the value of the child node
	 */
	public String getChildValue(String name){
		
		//clears the map associating a node to its value
		propertyValues.clear();
		
		String value=""; //$NON-NLS-1$
		Node node=null, cur=null;
			
		//for each node in the list
		for(Iterator it=nodeList.iterator(); it.hasNext();){
		    
			try{node=(Node)it.next();}catch (Exception ex){node=null;}
			
			if(node!=null && name!=null && ! name.equals("")){ //$NON-NLS-1$
				
				//for each child of the given element, tests if the name of these children is equals to the parameter string
				for(cur=node.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
				    
					if(cur.getNodeName().equals(name)){
					    
						value=cur.getNodeValue();
						break;
					}
				}
			}
				
			if(value==null || (value!=null && value.equals(""))){ //$NON-NLS-1$
			    
			    value=defaultPropertyValue;
			}

			if(node!=null){
			    
				//value=getSVGEditor().getSVGToolkit().normalizeTextNodeValue(value);
				propertyValues.put(node, value);
			}
		}
		
		//the value that will be returned
		String returnedValue=""; //$NON-NLS-1$
			
		//if the list contains a single element, its value will be returned, otherwise the empty string is returned
		if(nodeList.size()==1){
		    
			try{returnedValue=(String)propertyValues.get(nodeList.getFirst());}catch (Exception ex){returnedValue="";} //$NON-NLS-1$
		}

		return returnedValue;
	}
		
	/**
	 * Extracts multiline text from a structure of tspans or text nodes (or both) 
	 * underneath the named given node.
	 * <p>
	 * Note that tspans which do not seem to be being used as separate lines, as there is no y-shift given by a dy attribute, 
	 * are not treated as a new line. 
	 * 
     * @param name the name of the child node
     * @return the value of the child node
     */
    public String getTextChildValue(String name){
        
        //clears the map associating a node to its value
        propertyValues.clear();        
        //for each node in the list
        for(Iterator it=nodeList.iterator(); it.hasNext();){
            Node node = null;                        
            String value="";             //$NON-NLS-1$
            try{node=(Node)it.next();}catch (Exception ex){node=null;}
            
            if(node!=null && name!=null && ! name.equals("")){             //$NON-NLS-1$
                StringBuilder sb = new StringBuilder();                  
                value = TextConversionUtil.extractMultilineTextUnderElement(node);
            }
                
            if(value==null || (value!=null && value.equals(""))){ //$NON-NLS-1$
                
                value=defaultPropertyValue;
            }

            if(node!=null){
                
                //value=getSVGEditor().getSVGToolkit().normalizeTextNodeValue(value);
                propertyValues.put(node, value);
            }
        }
        
        //the value that will be returned
        String returnedValue=""; //$NON-NLS-1$
            
        //if the list contains a single element, its value will be returned, otherwise the empty string is returned
        if(nodeList.size()==1){
            
            try{returnedValue=(String)propertyValues.get(nodeList.getFirst());}catch (Exception ex){returnedValue="";} //$NON-NLS-1$
        }

        return returnedValue;
    }
        
	
	
	
	/**
	 * sets the value of a property in the style attribute
	 * @param name the name of the property
	 * @param values the map associating a node to its value of the property
	 */
	public void setStylePropertyValue(String name, LinkedHashMap values){
		
		if(nodeList!=null){

			Element element=null;
			String value="", oldValue=""; //$NON-NLS-1$ //$NON-NLS-2$
		
			//for each node in the list
			for(Iterator it=nodeList.iterator(); it.hasNext();){
			    
				try{
					element=(Element)it.next();
					value=(String)values.get(element);
					oldValue=(String)propertyValues.get(element);
				}catch (Exception ex){element=null;value=null;oldValue="";} //$NON-NLS-1$
			
				if(element!=null && name!=null && ! name.equals("") && value!=null && ! value.equals(oldValue)){ //$NON-NLS-1$
						
					properties.getSVGEditor().getSVGToolkit().setStyleProperty(element, name, value);
				}				
			}
		}
		
		generalPropertyValue=getStylePropertyValue(name);
	}
		
	/**
	 * sets the value of the given attribute
	 * @param name the name of the attribute
	 * @param values the map associating a node to its value of the property
	 */
	public void setAttributeValue(String name, LinkedHashMap values){
		
		if(nodeList!=null){
			
			Element element=null;
			String value="", oldValue=""; //$NON-NLS-1$ //$NON-NLS-2$
		
			//for each node in the list
			for(Iterator it=nodeList.iterator(); it.hasNext();){
			    
				try{
					element=(Element)it.next();
					value=(String)values.get(element);
					oldValue=(String)propertyValues.get(element);
				}catch (Exception ex){element=null;value=null;oldValue="";} //$NON-NLS-1$
			
				if(element!=null && name!=null && ! name.equals("") && value!=null && ! value.equals(oldValue)){ //$NON-NLS-1$
				    
					//sets the value of the attribute
					element.setAttribute(name, value);
				}
			}
		}
		
		generalPropertyValue=getAttributeValue(name);
	}
	
	/**
	 * sets the value of the child with the given name
	 * @param name the name of the child node
	 * @param values the map associating a node to its value of the property
	 */
	public void setChildValue(String name, LinkedHashMap values){
		
		if(nodeList!=null){

			Element element=null;
			String value="", oldValue=""; //$NON-NLS-1$ //$NON-NLS-2$
		
			//for each node in the list
			for(Iterator it=nodeList.iterator(); it.hasNext();){
			    
				try{
					element=(Element)it.next();
					value=(String)values.get(element);
					oldValue=(String)propertyValues.get(element);
				}catch (Exception ex){element=null;value=null;oldValue="";} //$NON-NLS-1$
			
				if(element!=null && name!=null && ! name.equals("") && value!=null && ! value.equals(oldValue)){ //$NON-NLS-1$
						
					//checks all the child nodes of the element to find the text node, if it is found, sets its value
					for(Node cur=element.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
					    
						if(cur.getNodeName().equals(name)){
						    
							//sets the value of the node
							cur.setNodeValue(value);
							break;
						}
					}
				}
			}
		}
		
		generalPropertyValue=getChildValue(name);
	}
	
	
	
	/**
     * Creates multiline text under the current element ("text" node) using tspans.
     * @param name the name of the child node
     * @param values the map associating a node to its value of the property
     */
    public void setTextChildValue(String name, LinkedHashMap values){
        
        if(nodeList!=null){

            Element element=null;
            String value="", oldValue=""; //$NON-NLS-1$ //$NON-NLS-2$
            SVGFrame frame=properties.getSVGEditor().getFrameManager().getCurrentFrame();
        
            //for each node in the list
            for(Iterator it=nodeList.iterator(); it.hasNext();){                
                try{
                    element=(Element)it.next();                    
                    value=(String)values.get(element);
                    oldValue=(String)propertyValues.get(element);
                }catch (Exception ex){element=null;value=null;oldValue="";} //$NON-NLS-1$
            
                if (element != null && name != null && !name.equals("") && value != null && !value.equals(oldValue)){ //$NON-NLS-1$
                    Document doc = frame.getScrollPane().getSVGCanvas().getDocument();
                    TextConversionUtil.putMultilineTextUnderElement(element, value,doc);                    
                    frame.setModified(true);
                }
            }
        }
        
        generalPropertyValue=getTextChildValue(name);
    }	
    

}
