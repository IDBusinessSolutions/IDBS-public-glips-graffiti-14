/*
 * Created on 7 juin 2004
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
package fr.itris.glips.svgeditor;

import java.util.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.menutool.*;
import fr.itris.glips.svgeditor.resources.*;
import fr.itris.glips.svgeditor.shape.*;

/**
 * @author Jordi SUC
 *
 * the class loading the modules 
 */
public class SVGModuleManager {
	
	/**
	 * the editor
	 */
	private SVGEditor editor;
	
	/**
	 * the object that manages the different SVGFrames in the desktop pane
	 */
	private SVGFrameManager frameManager;
	
	/**
	 * the menu bar
	 */
	private SVGMenuBar menubar;
	
	/**
	 * the popup manager
	 */
	private SVGPopup popupManager;
	
	/**
	 * the color manager
	 */
	private SVGColorManager colorManager=null;
	
	/**
	 * the toolBar
	 */
	private SVGEditorToolbars editorToolBars;
	
	/**
	 * the manager of the cursors
	 */
	private SVGCursors cursors;
	
	/**
	 * the list of the modules
	 */
	private LinkedList<SVGModule> modules=new LinkedList<SVGModule>();
	
	/**
	 * the list of the shape modules
	 */
	private LinkedList<SVGShape> shapeModules=new LinkedList<SVGShape>();
	
	/**
	 * the list of the classes of the modules
	 */
	private LinkedList<String> moduleClasses=new LinkedList<String>();
	
	/**
	 * the resource image manager
	 */
	private SVGResourceImageManager resourceImageManager=null;

	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGModuleManager(SVGEditor editor) {
	    
		this.editor=editor;
	}
	
	/**
	 * initializes the object
	 */
	public void init(){

		//the object that manages the different SVGFrames in the desktop pane
		frameManager=new SVGFrameManager(editor);
		
		//the menu bar
		menubar=new SVGMenuBar(editor);
		
		//the color manager
		colorManager=new SVGColorManager(editor);
		
		//the cursor manager
		cursors=new SVGCursors(editor);
		
		//the resource image manager 
		resourceImageManager=new SVGResourceImageManager(editor);
		
		//gets the module's classes
		parseXMLModules();
		
		//creates the static modules
		createModuleObjects();
		
		//the popup menu manager
		popupManager=new SVGPopup(editor);
		
		//the toolBar
		editorToolBars=new SVGEditorToolbars(editor);
		
		//initializes the menu bar
		menubar.init();
	}
	
	/**
	 * layouts some elements created by this class
	 */
	public void layoutElements(){

		Collection<SVGModule> mds=getModules();

		for(SVGModule module : mds){

			if(module!=null){
			    
				module.initialize();
			}
		}
		
		editorToolBars.layoutElements();
		frameManager.layoutElements();
	}

	/**
	 * parses the XML document to get the modules
	 */
	protected void parseXMLModules(){
		
		Document doc=null;
		doc=SVGResource.getXMLDocument("modules.xml");	 //$NON-NLS-1$
		if(doc==null)return;
		
		Element root=doc.getDocumentElement();
		Node current=null;
		NamedNodeMap attributes=null;
		String name=null, sclass=null;
		
		for(NodeIterator it=new NodeIterator(root); it.hasNext();){
		    
			current=it.next();
			
			if(current!=null){	
			    
				name=current.getNodeName();
				attributes=current.getAttributes();	
				
				if(name!=null && name.equals("module") && attributes!=null){ //$NON-NLS-1$
				    
					//adds the string representing a class in the list linked with static items
					sclass=attributes.getNamedItem("class").getNodeValue(); //$NON-NLS-1$
					
					if(sclass!=null && ! sclass.equals("")){ //$NON-NLS-1$
					    
					    moduleClasses.add(sclass);
					}
				}
			}
		}
	}
	
	/**
	 * creates the objects corresponding to the modules
	 */
	protected void createModuleObjects(){

		Object obj=null;
		
		for(String current : moduleClasses){

			if(current!=null && ! current.equals("")){ //$NON-NLS-1$
			    
				try{
					Class[] classargs={SVGEditor.class};
					Object[] args={editor};
					
					//creates instances of each static module
					obj=Class.forName(current).getConstructor(classargs).newInstance(args);
					
					//if it is a shape module, it is added to the list of the shape module
					if(obj instanceof SVGShape){
						
						shapeModules.add((SVGShape)obj);
					}
					
					modules.add((SVGModule)obj);
				}catch (Exception ex){ex.printStackTrace();}	
			}
		}		
	}
	
	/**
	 * gets the module given its name
	 * @param name the module's name
	 * @return a module
	 */
	public Object getModule(String name){

		Object current=null;
		String cname=null;
		
		for(Iterator it=modules.iterator(); it.hasNext();){
			
			current=it.next();
			
			try{
				cname=(String)current.getClass().getMethod("getName", (Class[])null).invoke(current, (Object[])null); //$NON-NLS-1$
			}catch (Exception e){cname=null;}
			
			if(cname!=null && cname.equals(name)){
			    
			    return current;
			}
		}
		return null;
	}
	
	/**
	 * gets a shape module given its name
	 * @param name the module's name
	 * @return a module
	 */
	public SVGShape getShapeModule(String name){

		SVGShape current=null;
		String cname=null;
		
		for(Iterator it=shapeModules.iterator(); it.hasNext();){

			try{
				current=(SVGShape)it.next();
				cname=current.getName();
			}catch (Exception e){cname=null;}
			
			if(cname!=null && cname.equals(name)){
			    
			    return current;
			}
		}
		
		return null;
	}
	
	/**
	 * @return the collection of the objects corresponding to the modules
	 */
	public Collection<SVGModule> getModules(){
		return modules;
	}
	
	/**
	 * @return the collection of the objects corresponding to the shape modules
	 */
	public Collection getShapeModules(){
		return shapeModules;
	}
	
	/**
	 * @return SVGFrameManager the class that manages the different frames in the desktop pane
	 */
	public SVGFrameManager getFrameManager(){
		return  frameManager;
	}

    /**
     * @return Returns the color manager.
     */
    public SVGColorManager getColorManager() {
        return colorManager;
    }
    
	/**
	 * @return the menubar
	 */
	public SVGMenuBar getMenuBar(){
		return menubar;
	}
	
	/**
	 * @return the tool bar
	 */
	public SVGEditorToolbars getEditorToolBars(){
		return editorToolBars;
	}
	
	/**
	 * @return the popup manager
	 */
	public SVGPopup getPopupManager() {
		return popupManager;
	}
	
	/**
	 * @return Returns the resourceImageManager.
	 */
	protected SVGResourceImageManager getResourceImageManager() {
		return resourceImageManager;
	}
    
	/**
	 * @return the manager of the cursors
	 */
	public SVGCursors getCursors(){
		return cursors;
	}

}
