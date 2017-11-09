/*
 * Created on 1 juil. 2004
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
package fr.itris.glips.svgeditor.menutool;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.resources.*;

/**
 * @author Jordi SUC
 * the class creating and managing the svg toolbar
 */
public class SVGToolBar extends JToolBar{
	
	/**
	 * the editor
	 */
	private SVGEditor editor;

	/**
	 * the list of the ids of the tool items that have to be added to the tool bar
	 */
	private final LinkedList<String> toolNames=new LinkedList<String>();
	
	/**
	 * the map associating an id to a tool item
	 */
	private final Hashtable toolItems=new Hashtable();

	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGToolBar(SVGEditor editor){
	    
		super(SwingConstants.HORIZONTAL);
		this.editor=editor;

		//creates the tool bar and the frame that will contain it//
		setRollover(true);
		setFloatable(true);
		setBorderPainted(false);
		setMargin(new Insets(0, 0, 0, 0));
	}
	
	/**
	 * @return editor the editor
	 */
	public SVGEditor getSVGEditor(){
		return editor;
	}
	
	/**
	 * layout elements in the tool bar
	 */
	public void layoutElements(){
		
		parseXMLTools();
		findModuleToolItems();
		build();
	}
	
	/**
	 * gets all the tool items from the modules
	 */
	protected void findModuleToolItems(){
		
		Collection modules=getSVGEditor().getSVGModuleLoader().getModules();
		SVGModule module=null;
		Map tItems=null;
		
		for(Iterator it=modules.iterator(); it.hasNext();){

			module=(SVGModule)it.next();
			
			if(module!=null){
			    
			    tItems=module.getToolItems();
			    
			    if(tItems!=null){
			      
				    toolItems.putAll(tItems);
			    }
			}
		}
	}

	/**
	 * builds the toolbar
	 */
	protected void build(){

		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		//removes all the elements from the group
		String id=""; //$NON-NLS-1$
		JToggleButton button=null;
		//Insets buttonInsets=new Insets(0, 0, 0, 2);
		
		for(Iterator it=toolNames.iterator(); it.hasNext();){

			try{
				id=(String)it.next();
			}catch(Exception ex){id=null;}

			if(id!=null && ! id.equals("")){ //$NON-NLS-1$

				try{
					button=(JToggleButton)toolItems.get(id);
				}catch (Exception ex){button=null;}

				if(button!=null){
				    
				    //button.setMargin(buttonInsets);
					add(button);
				}
			}
		}
		
		addSeparator();
	}
	
	/**
	 *parses the document to get the items order specified in the "tools.xml" file
	 */
	protected void parseXMLTools(){

		Document doc=SVGResource.getXMLDocument("tool.xml"); //$NON-NLS-1$
		
		if(doc!=null){
		    
		    Element root=doc.getDocumentElement();
		    
		    if(root!=null){
		        
				String nameAttr=""; //$NON-NLS-1$
				
				//getting the element of the svg tools
				NodeList nodes=root.getElementsByTagName("svgTools"); //$NON-NLS-1$
				
				if(nodes!=null && nodes.getLength()>0){
					
					Element svgTools=(Element)nodes.item(0);
					
					for(Node current=svgTools.getFirstChild(); current!=null; current=current.getNextSibling()){
						
						if(current instanceof Element && current.getNodeName().equals("button")){ //$NON-NLS-1$

							nameAttr=((Element)current).getAttribute("name"); //$NON-NLS-1$
								
							if(nameAttr!=null && ! nameAttr.equals("")){ //$NON-NLS-1$
									
								toolNames.add(nameAttr);
							}
						}
					}
				}
		    }
		}
	}
	
	/**
	 * cancels all the actions that could be running
	 */
	public void cancelActions(){
	}
}
