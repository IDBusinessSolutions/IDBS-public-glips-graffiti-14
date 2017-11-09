/*
 * Created on 25 mars 2004
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
package fr.itris.glips.svgeditor.canvas;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.beans.*;
import java.io.File;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.resources.SVGResource;

/**
 * @author Jordi SUC
 * the class linked with every SVG file that handles the SVGScrollPane, the menu items linked 
 * with each SVG file, the state bar, etc
 */
public class SVGFrame {
	
	/**
	 * the editor
	 */
	private SVGEditor editor;
	
	/**
	 * the panel into which the widgets are inserted
	 */
	private JPanel framePanel=new JPanel();
	
	/**
	 * the name of the SVGFrame
	 */
	private String name;
	
	/**
	 * the scrollpane contained in this SVGFrame
	 */
	private SVGScrollPane scrollpane;
	
	/**
	 * the menuitem associated with the frame in the menu bar
	 */
	private JRadioButtonMenuItem menuitem=new JRadioButtonMenuItem();
	
	/**
	 * the state bar
	 */
	private SVGStateBar statebar;
	
	/**
	 * the interval frame into which the SVGFrame is inserted if the multi-windowed option is set to true
	 */
	private JInternalFrame internalFrame=null;
	
	/**
	 * the list of the runnables enabling to dispose the frame
	 */
	private LinkedList<Runnable> disposeRunnables=new LinkedList<Runnable>();
	
	/**
	 * the boolean set to true if the SVG picture has been modified 
	 */
	private boolean modified=false;

	/**
	 * the font
	 */
	public final Font theFont=new Font("theFont", Font.ROMAN_BASELINE, 10); //$NON-NLS-1$
	
	/**
	 * the map associating a node to the set of the listeners to this node
	 */
	private Map<Node, HashSet<SVGDOMListener>> domListeners=Collections.synchronizedMap(new HashMap<Node, HashSet<SVGDOMListener>>());
	
	/**
	 * the map associating the id of a resource to the list of the nodes using this resource
	 */
	private Hashtable<String, LinkedList<Element>> usedResources=new Hashtable<String, LinkedList<Element>>();
	
	/**
	 * the constuctor of the class
	 * @param editor the editor's object
	 * @param name the name that will be linked with the SVG picture
	 */
	public SVGFrame(SVGEditor editor, String name){
		
		super(); 
		this.editor=editor;
		
		//filling the frame panel
		framePanel.setLayout(new BorderLayout());
		
		//the state bar
		statebar=new SVGStateBar();
		framePanel.add(statebar, BorderLayout.SOUTH);
		
		//the scrollpane
		scrollpane=new SVGScrollPane(editor, this);
		framePanel.add(scrollpane, BorderLayout.CENTER);
		
		if(editor.isMultiWindow()){
			
			//creating the internal frame
			internalFrame=new JInternalFrame("", true, true, true, true); //$NON-NLS-1$
			
			//sets the icon
			final ImageIcon editorIcon=SVGResource.getIcon("EditorInner", false); //$NON-NLS-1$
			
			internalFrame.setFrameIcon(editorIcon);
			
			framePanel.setBorder(new EmptyBorder(3, 3, 3, 1));
			
			//setting the location of the frame
			Rectangle bounds=getSVGEditor().getPreferredWidgetBounds("frame"); //$NON-NLS-1$
			int nb=editor.getFrameManager().getFrameNumber(), offset=30, beginX=75, beginY=0;
			
			if(bounds!=null){
				
				beginX=bounds.x;
				beginY=bounds.y;
			}
			
			internalFrame.setLocation(beginX+nb*offset,beginY+nb*offset);
			
			internalFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			
			//adds an internal frame listener
			final InternalFrameListener internalFrameListener=new InternalFrameAdapter(){
				
				@Override
				public void internalFrameClosing(InternalFrameEvent e) {
					
					close();
				}
			};
			
			internalFrame.addInternalFrameListener(internalFrameListener);
			
			//the listener to the focus changes
			final VetoableChangeListener vetoableChangeListener=new VetoableChangeListener(){
				
				public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
					
					if(	evt.getPropertyName()!=null && evt.getPropertyName().equals("selected") &&  //$NON-NLS-1$
							getSVGEditor().getFrameManager().getCurrentFrame()!=SVGFrame.this && ((Boolean)evt.getNewValue()).booleanValue()){
						
						getSVGEditor().getFrameManager().setCurrentFrame(SVGFrame.this.getName());
					}
				}
			};
			
			internalFrame.addVetoableChangeListener(vetoableChangeListener);
			
			//adds a dispose runnable
			disposeRunnables.add(new Runnable(){
				
				public void run() {
					
					usedResources.clear();
					internalFrame.removeInternalFrameListener(internalFrameListener);
					internalFrame.removeVetoableChangeListener(vetoableChangeListener);
				}
			});
			
		}else{
			
			//adds the SVGFrame to the desktop panel contained in the main frame
			editor.getDesktop().add(framePanel);
		}
		
		setName(name);
	}
	
	/**
	 * displays this frame
	 * @param canvasSize the canvas size
	 */
	public void displayFrame(Dimension canvasSize){
		
		if(internalFrame!=null && canvasSize!=null){
			
			//computing the size of the internal frame
			int border=100;
			Dimension frameSize=new Dimension(canvasSize.width+border, canvasSize.height+border);
			Dimension availableSpace=new Dimension(		editor.getDesktop().getWidth()-internalFrame.getX(), 
					editor.getDesktop().getHeight()-internalFrame.getY());					
			
			if(frameSize.width>availableSpace.width){
				
				frameSize.width=availableSpace.width;
			}
			
			if(frameSize.height>availableSpace.height){
				
				frameSize.height=availableSpace.height;
			}

			internalFrame.setSize(frameSize);
			internalFrame.setPreferredSize(frameSize);
			internalFrame.getContentPane().add(framePanel);
			
			//adds the internalFrame to the desktop panel contained in the main frame
			editor.getDesktop().add(internalFrame);
			
			internalFrame.setVisible(true);
		}
	}
	
	/**
	 * closes the frame
	 */
	public void close() {
		
		Object obj=null;
		
		if(getSVGEditor().getSVGModuleLoader()!=null){
			
			obj=getSVGEditor().getSVGModuleLoader().getModule("SaveClose"); //$NON-NLS-1$
		}
		
		if(obj!=null){
			
			Class[] cargs={SVGFrame.class};
			Object[] args={this};
			
			try{
				obj.getClass().getMethod("closeAction", cargs).invoke(obj,args); //$NON-NLS-1$
			}catch (Exception ex){}  
		}
	}
	
	/**
	 * @return whether this frame is selected or not
	 */
	public boolean isSelected(){
		
		SVGFrame currentFrame=getSVGEditor().getFrameManager().getCurrentFrame();
		
		if(currentFrame!=null && currentFrame.equals(this)){
			
			if(internalFrame!=null){
				
				return internalFrame.isSelected();
			}
			
			return true;
		}
		
		return false;
	}
	
	private void ensureIdbsDefs(Document doc)
	{
	    final String currentIdbsDefsId = "idbs_defs_v1"; //$NON-NLS-1$
	    Element idbsDefsInFile = doc.getElementById(currentIdbsDefsId);
	    	    
	    if (idbsDefsInFile == null)
	    {
	        Document resourceStore = editor.getResource().getResourceStore();
	        Element idbsDefsGroupFromStore = resourceStore.getElementById(currentIdbsDefsId);
	        
	        final Element defsNode = getDefsElement(doc); //this creates the node if absent
	        //add this to our document
	        final Node importedIdbsDefsGroup = doc.importNode(idbsDefsGroupFromStore, true);
	        
            Runnable runnable=new Runnable(){                
                public void run() {                    
                    defsNode.appendChild(importedIdbsDefsGroup);
                }
            };
	        
            enqueue(runnable);
	    }        
	}
	
	/**
	 * handles the initial operations on the svg document
	 * @param doc the svg document
	 * @param scaledSize the scaled size of the canvas
	 */
	public void handleInitialDOMOperations(Document doc, Dimension scaledSize){
		
		if(doc!=null){
			
			Element root=doc.getDocumentElement();
			
			if(root!=null && root.getAttribute("viewBox").equals("")){ //$NON-NLS-1$ //$NON-NLS-2$
				
				root.setAttributeNS(null, "viewBox", "0 0 "+(int)scaledSize.getWidth()+" "+(int)scaledSize.getHeight()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			
			if(root!=null){
				
				//getting the map associating the id of a resource to the resource node
				LinkedList<String> resourceNames=new LinkedList<String>();
				resourceNames.add("linearGradient"); //$NON-NLS-1$
				resourceNames.add("radialGradient"); //$NON-NLS-1$
				resourceNames.add("pattern"); //$NON-NLS-1$
				resourceNames.add("marker"); //$NON-NLS-1$
				
				ensureIdbsDefs(doc);
				
				final String styleAttributeName="style", gNodeName="g", regex1="\\n+", regex2="\\r+", regex3="\\t+"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				Hashtable<String, Element> resources=getResourcesFromDefs(doc, resourceNames);
				
				//applying modifications on the dom
				Node node=null;
				Element el=null;
				String nsp=root.getNamespaceURI();
				LinkedList<Element> nodesList=null;
				String style="", nodeValue=""; //$NON-NLS-1$ //$NON-NLS-2$
				//the set of the nodes to remove
				HashSet<Node> nodesToRemove=new HashSet<Node>();
				
				//the list of the group nodes
				LinkedList<Element> groupNodes=new LinkedList<Element>();
				
				for(NodeIterator nit=new NodeIterator(root); nit.hasNext();){
					
					node=nit.next();
					
					if(node!=null && nsp.equals(node.getNamespaceURI()) && node instanceof Element){
						
						el=(Element)node;
						
						//normalizing the node
						editor.getSVGToolkit().normalizeNode(el);
						
						//checking the color values consistency
						SVGEditor.getColorChooser().checkColorString(SVGFrame.this, el);
						
						//getting the style attribute
						style=el.getAttribute(styleAttributeName);

						if(style!=null && ! style.equals("")){ //$NON-NLS-1$
							
							//for each resource id, checks if it is contained in the style attribute
							for(String id : resources.keySet()){
								
								nodesList=usedResources.get(id);
								
								//adds the node in the used resource map
								if(id!=null && ! id.equals("") && style.indexOf("#".concat(id))!=-1){ //$NON-NLS-1$ //$NON-NLS-2$
									
									if(nodesList==null){
										
										nodesList=new LinkedList<Element>();
										usedResources.put(id, nodesList);
									}
									
									nodesList.add(el);
								}
							}
						}
						
						if(node.getNodeName().equals(gNodeName)){
							
							groupNodes.add(el);
						}
						
					}else if(node!=null && node instanceof Text){
						
						nodeValue=node.getNodeValue();
						
						if(nodeValue!=null && ! nodeValue.equals("")){ //$NON-NLS-1$
							
							nodeValue=nodeValue.replaceAll(regex1, ""); //$NON-NLS-1$
							nodeValue=nodeValue.replaceAll(regex2, ""); //$NON-NLS-1$
							nodeValue=nodeValue.replaceAll(regex3, ""); //$NON-NLS-1$
						}

						if(nodeValue==null || nodeValue.equals("")){ //$NON-NLS-1$
							
							nodesToRemove.add(node);
							
						}else{
							
							node.setNodeValue(nodeValue);
						}
					}
				}
				
				//removing the unused text nodes
				for(Node textNode : nodesToRemove){
					
					textNode.getParentNode().removeChild(textNode);
				}
				
				//normalizes the group nodes
				for(Element groupElement : groupNodes){

					if(groupElement!=null){
						
						//normalizing the node
						editor.getSVGToolkit().normalizeGroupNode(groupElement);
					}
				}				
				
			}
		}
	}
	
	/**
	 * adds the given id of a resource and its associated node to the map of the nodes using a resource
	 * @param resourceId the id of a resource
	 * @param node the node using the given resource
	 */
	public synchronized void addNodeUsingResource(String resourceId, Node node){
		
		if(resourceId!=null && ! resourceId.equals("") && node!=null){ //$NON-NLS-1$
			
			LinkedList<Element> nodesList=null;
			
			//checking if the id of the resource is contained in the map
			if(usedResources.containsKey(resourceId)){
				
				try{
					//getting the associated list of nodes
					nodesList=usedResources.get(resourceId); 
				}catch (Exception ex){}
			}
			
			if(nodesList==null){
				
				//if the id was not contained in the map, creates a new list of nodes
				nodesList=new LinkedList<Element>();
				usedResources.put(resourceId, nodesList);
			}
			
			if(nodesList!=null){
				
				//adding the node to the list
				nodesList.add((Element)node);
			}
		}
	}
	
	/**
	 * adds the given id of a resource and its associated node to the map of the nodes using a resource
	 * @param resourceId the id of a resource
	 * @param list the nodes using the given resource
	 */
	public synchronized void addNodesUsingResource(String resourceId, LinkedList<Element> list){
		
		if(resourceId!=null && ! resourceId.equals("") && list!=null){ //$NON-NLS-1$
			
			LinkedList<Element> nodesList=null;

			//checking if the id of the resource is contained in the map
			if(usedResources.containsKey(resourceId)){
				
				try{
					//getting the associated list of nodes
					nodesList=usedResources.get(resourceId); 
				}catch (Exception ex){}
			}
			
			if(nodesList==null){
				
				//if the id was not contained in the map, creates a new list of nodes
				nodesList=new LinkedList<Element>();
				usedResources.put(resourceId, nodesList);
			}

			for(Element cur : new HashSet<Element>(list)){
				
				if(cur!=null){
					
					//adding the node to the list
					nodesList.add(cur);
				}
			}
		}
	}
	
	/**
	 * removes the given id of a resource and its associated node to the map of the nodes using a resource
	 * @param resourceId the id of a resource
	 * @param node the node using the given resource
	 */
	public synchronized void removeNodeUsingResource(String resourceId, Node node){
		
		if(resourceId!=null && ! resourceId.equals("") && node!=null){ //$NON-NLS-1$
			
			LinkedList<Element> nodesList=null;
			
			//checking if the id of the resource is contained in the map
			if(usedResources.containsKey(resourceId)){
				
				try{
					//getting the associated list of nodes
					nodesList=usedResources.get(resourceId); 
				}catch (Exception ex){}
			}
			
			if(nodesList!=null && nodesList.contains(node)){
				
				//removing the node from the list
				nodesList.remove(node);
			}
		}
	}
	
	/**
	 * adds the given id of a resource and its associated node to the map of the nodes using a resource
	 * @param resourceId the id of a resource
	 * @param list the nodes using the given resource
	 */
	public synchronized void removeNodesUsingResource(String resourceId, LinkedList<Element> list){
		
		if(resourceId!=null && ! resourceId.equals("") && list!=null){ //$NON-NLS-1$
			
			LinkedList<Element> nodesList=null;
			
			//checking if the id of the resource is contained in the map
			if(usedResources.containsKey(resourceId)){
				
				try{
					//getting the associated list of nodes
					nodesList=usedResources.get(resourceId); 
				}catch (Exception ex){}
			}
			
			if(nodesList!=null){
				
				for(Element cur : list){
					
					if(cur!=null){
						
						//removing the node from the list
						nodesList.remove(cur);
					}
				}
			}
		}
	}
	
	/**
	 * redraws the node that use the resource of the given id
	 * @param id the id of a resource
	 */
	public void refreshNodesUsingResource(String id){
		
		if(id!=null && ! id.equals("")){ //$NON-NLS-1$
			
			LinkedList<Element> list=null;
			
			try{
				list=usedResources.get(id);
			}catch (Exception ex){}

			if(list!=null){

				for(Element cur : list){

					if(cur!=null){
						
						cur.setAttribute("display", "none"); //$NON-NLS-1$ //$NON-NLS-2$
						cur.removeAttribute("display"); //$NON-NLS-1$
					}
				}
			}
		}
	}
	
	/**
	 * modifies the id of a resource in the nodes that are using this resource
	 * @param newId the new id for the resource
	 * @param lastId the previous id for the resource
	 */
	public void modifyResourceId(String newId, String lastId){
		
		if(		newId!=null && ! newId.equals("") && lastId!=null && ! lastId.equals("") &&  //$NON-NLS-1$ //$NON-NLS-2$
				! lastId.equals(newId) && usedResources.containsKey(lastId)){
			
			//getting the list of the nodes using the resource
			LinkedList<Element> nodesList=null;
			
			try{
				nodesList=usedResources.get(lastId);
			}catch (Exception ex){}
			
			if(nodesList!=null){
				
				String style=""; //$NON-NLS-1$
				
				//for each node, replaces the last id of the resource with the new id
				for(Element cur : nodesList){
					
					if(cur!=null){
						
						style=cur.getAttribute("style"); //$NON-NLS-1$
						
						if(style!=null && ! style.equals("")){ //$NON-NLS-1$
							
							style=style.replaceAll("#"+lastId+"[)]", "#"+newId+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							cur.setAttribute("style", style); //$NON-NLS-1$
						}
					}
				}
				
				//modifies the map of the used resources
				usedResources.remove(lastId);
				usedResources.put(newId, nodesList);
			}
		}
	}
	
	/**
	 * checks if the resource denoted by the given id is used or not
	 * @param resourceId the id of a resource
	 * @return true if the resource is used
	 */
	public boolean isResourceUsed(String resourceId){
		
		boolean isUsed=false;
		
		if(resourceId!=null && ! resourceId.equals("")){ //$NON-NLS-1$
			
			LinkedList<Element> list=null;
			
			try{
				list=usedResources.get(resourceId);
			}catch (Exception ex){}
			
			if(list!=null && list.size()>0){
				
				isUsed=true;
			}
		}
		
		return isUsed;
	}
	
	/**
	 * @return Returns the usedResources.
	 */
	public Hashtable<String, LinkedList<Element>> getUsedResources() {
		
		return new Hashtable<String, LinkedList<Element>>(usedResources);
	}
	
	/**
	 *disposes this frame
	 */
	public void dispose(){
		
		if(internalFrame!=null){
			
			internalFrame.setVisible(false);
			getSVGEditor().getDesktop().remove(internalFrame);
		}
		
		//removing the frame from the frame manager
		getSVGEditor().getFrameManager().removeFrame(getName());
		
		//removing each dom listener
		Set<SVGDOMListener> domListenersSet=null;
		
		for(Node node : new HashSet<Node>(domListeners.keySet())) {
			
			if(node!=null) {
				
				domListenersSet=new HashSet<SVGDOMListener>(domListeners.get(node));
				
				if(domListenersSet!=null) {
					
					for(SVGDOMListener listener : domListenersSet) {
						
						listener.removeListener();
					}
				}
			}
		}
		
		domListeners.clear();
		
		//run the dispose runnables
		Runnable runnable=null;
		
		for(Iterator it=disposeRunnables.iterator(); it.hasNext();){
			
			try{
				runnable=(Runnable)it.next();
			}catch (Exception ex){runnable=null;}
			
			if(runnable!=null){
				
				runnable.run();
			}
		}
		
		disposeRunnables.clear();
		
		framePanel.removeAll();
		statebar.removeAll();
		scrollpane.removeAll();
	}
	
	/**
	 * registers a listener 
	 * @param runnable a runnable
	 */
	public void addDisposeRunnable(Runnable runnable){
		
		if(runnable!=null){
			
			disposeRunnables.add(runnable);
		}
	}
	
	/**
	 * enqueues the given runnable into the batik runnable queue
	 * @param runnable a runnable
	 */
	public void enqueue(final Runnable runnable){

		if(runnable!=null){

			SwingUtilities.invokeLater(new Runnable() {
				
				public void run() {
				
					runnable.run();
					scrollpane.getSVGCanvas().requestUpdateContent();
				}
			});
		}
	}
	
	/**
	 * adding a listener to the svg dom of this frame
	 * @param listener a listener
	 */
	public synchronized void addDOMListener(SVGDOMListener listener){
		
		if(listener!=null){
			
			HashSet<SVGDOMListener> set=null;
			
			if(domListeners.containsKey(listener.getNode())) {
				
				set=domListeners.get(listener.getNode());
				
			}else {
				
				//creating and putting the new set into the map
				set=new HashSet<SVGDOMListener>();
				domListeners.put(listener.getNode(), set);
			}
			
			set.add(listener);
			listener.setFrame(this);
		}
	}
	
	/**
	 * removing a listener from the svg dom of this frame
	 * @param listener a listener
	 */
	public synchronized void removeDOMListener(SVGDOMListener listener){

		if(listener!=null && domListeners.containsKey(listener.getNode())){
			
			HashSet<SVGDOMListener> set=domListeners.get(listener.getNode());
			
			if(set!=null) {
				
				set.remove(listener);
		
				return;
			}
		}
	}
	
	/**
	 * fires that the given node has been modified ( one of its attributes has been modified)
	 * @param node a node
	 */
	public synchronized void fireNodeChanged(Node node){
		
		if(node!=null) {
			
			//getting the set associated with the given node
			Set<SVGDOMListener> set=domListeners.get(node);
			
			if(set!=null) {
				
				for(SVGDOMListener listener : new HashSet<SVGDOMListener>(set)){
					
					if(listener!=null){
						
						//firing that the node has changed
						listener.nodeChanged();
					}
				}
			}
		}
	}
	
	/**
	 * fires that the given node has been removed 
	 * @param node a node
	 * @param removedNode the node that has been removed
	 */
	public synchronized void fireNodeRemoved(Node node, Node removedNode){
		
		if(node!=null && removedNode!=null) {
			
			//getting the set associated with the given node
			Set<SVGDOMListener> set=domListeners.get(node);
			
			if(set!=null) {
				
				for(SVGDOMListener listener : new HashSet<SVGDOMListener>(set)){
					
					if(listener!=null){
						
						//firing that the node has changed
						listener.nodeRemoved(removedNode);
					}
				}
			}
		}
	}
	
	/**
	 * fires that the given node has been inserted
	 * @param node a node
	 * @param nodeInserted the node that has been inserted into the node children
	 */
	public synchronized void fireNodeInserted(Node node, Node nodeInserted){
		
		if(node!=null) {
			
			//getting the set associated with the given node
			Set<SVGDOMListener> set=domListeners.get(node);
			
			if(set!=null) {
				
				for(SVGDOMListener listener : new HashSet<SVGDOMListener>(set)){
					
					if(listener!=null){
						
						//firing that the node has changed
						listener.nodeInserted(nodeInserted);
					}
				}
			}
		}
	}
	
	/**
	 * fires that the given sub tree has been modified
	 * @param rootNode a node
	 * @param lastModifiedNode the last node that has been modified
	 */
	public synchronized void fireStructureChanged(Node rootNode, Node lastModifiedNode){
		
		if(rootNode!=null) {
			
			//getting the set associated with the given node
			Set<SVGDOMListener> set=domListeners.get(rootNode);
			
			if(set!=null) {
				
				for(SVGDOMListener listener : new HashSet<SVGDOMListener>(set)){
					
					if(listener!=null){
						
						//firing that the node has changed
						listener.structureChanged(lastModifiedNode);
					}
				}
			}
		}
	}
	
	/**
	 * @return true if the SVG picture has been modified
	 */
	public boolean isModified(){
		
		return modified;
	}
	
	/**
	 * sets that the svg document has been modified
	 * @param modified must be true if the svg picture has been modified
	 */
	public void setModified(boolean modified){
		this.modified=modified;
		setSVGFrameLabel(name);
	}
	
	/**
	 * 
	 * @return the state bar
	 */
	public SVGStateBar getStateBar(){
		return statebar;
	}
	
	/**
	 * 
	 * @return the editor
	 */
	public SVGEditor getSVGEditor(){
		return editor;
	}
	
	/**
	 * 
	 * @return the SVGScrollpane contained in this frame
	 */
	public SVGScrollPane getScrollPane(){
		return scrollpane;
	}
	
	/**
	 * @return the name linked with the SVG picture
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * sets the new name of the SVGFrame
	 * @param name the new name
	 */
	public void setName(String name){
		this.name=name;
		setSVGFrameLabel(name);
	}
	
	/**
	 * sets the label of the svg frame
	 * @param name the string associated with the SVGFrame
	 */
	protected void setSVGFrameLabel(String name){
		
		String label=""; //$NON-NLS-1$
		
		try{
			File file=new File(name);
			label=file.getName();
		}catch (Exception ex){label=null;}
		
		if((label==null || (label!=null && label.equals(""))) && name!=null && ! name.equals("")){ //$NON-NLS-1$ //$NON-NLS-2$
			
			label=name;	
		}
		
		if(modified){
			
			label=label.concat("*"); //$NON-NLS-1$
		}
		
		menuitem.setText(label);
		getStateBar().setSVGName(label);
		
		if(internalFrame!=null){
			
			internalFrame.setTitle(label);
		}
	}
	
	/**
	 * @return the short name of this frame
	 */
	public String getShortName(){
		
		String shortName=""; //$NON-NLS-1$
		
		try{
			File file=new File(name);
			shortName=file.getName();
		}catch (Exception ex){shortName=null;}
		
		return shortName;
	}
	
	/**
	 * @return the menu item that will be displayed in the menu bar to switch from one SVG picture to another 
	 */
	public JMenuItem getFrameMenuItem(){
		return menuitem;
	}
	
	/**
	 * hides this frame in the main frame
	 */
	public void moveToBack(){
		
		if(editor.isMultiWindow()){
			
			try{internalFrame.setSelected(false);}catch(Exception ex){}
			
		}else{
			
			framePanel.setVisible(false);
			scrollpane.setVisible(false);
			statebar.setVisible(false);
		}
	}
	
	/**
	 * shows this frame in the main frame
	 */
	public void moveToFront(){
		
		if( (editor.isMultiWindow())){
			
			try{internalFrame.setSelected(true);}catch(Exception ex){}
			
		}else{
			
			framePanel.setVisible(true);
			scrollpane.setVisible(true);
			statebar.setVisible(true);
		}
	}
	
	/**
	 * removes this SVGFrame from the desktop
	 */
	public void removeFromDesktop(){
		
		if( (editor.isMultiWindow())){
			
			editor.getDesktop().remove(internalFrame);
			
		}else{
			
			editor.getDesktop().remove(framePanel);
		}
		
		editor.getDesktop().repaint();
	}
	
	/**
	 * @return the internal frame if the multi windowed mode is activated
	 */
	public JInternalFrame getInternalFrame(){
		return internalFrame;
	}
	
	/**
	 * the method used to get the point correponding to the given point when aligned with the rulers
	 * @param point the point
	 * @return the point correponding to the given point when aligned with the rulers
	 */
	public Point2D.Double getAlignedWithRulersPoint(Point point){
		
		if(point!=null){
			
			return getScrollPane().getAlignedWithRulersPoint(new Point2D.Double(point.x, point.y));
		}
		
		return null;
	}
	
	/**
	 * the method used to get the point correponding to the given point with integer coordinates when aligned with the rulers
	 * @param point the point
	 * @return the point correponding to the given point when aligned with the rulers
	 */
	public Point getAlignedWithRulersIntegerPoint(Point point){
		
		if(point!=null){
			
			Point2D.Double point2D=getAlignedWithRulersPoint(point);
			
			if(point2D!=null){
				
				return new Point((int)point2D.x, (int)point2D.y);
			}
		}
		
		return null;
	}
	
	/**
	 * computes the position and the size of a node on the canvas 
	 * @param shape the node whose position and size is to be computed
	 * @return a rectangle representing the position and size of the given node
	 */
	public Rectangle2D getNodeBounds(Element shape){
		
		Rectangle2D bounds=new Rectangle();
		
		if(shape!=null){
			
			//gets the bridge context 
			BridgeContext ctxt=getScrollPane().getSVGCanvas().getBridgeContext();
			
			if(ctxt!=null){
				
				//gets the graphics node corresponding to the given node
				GraphicsNode gnode=null;
				
				try{gnode=ctxt.getGraphicsNode(shape);}catch (Exception e){gnode=null;}
				
				if(gnode!=null){
					
					bounds=gnode.getGeometryBounds();

					AffineTransform affine=new AffineTransform();

					if(gnode.getTransform()!=null){
						
						affine.preConcatenate(gnode.getTransform());
					}

					if(scrollpane.getSVGCanvas().getViewingTransform()!=null){

						affine.preConcatenate(scrollpane.getSVGCanvas().getViewingTransform());
					}
					
					if(scrollpane.getSVGCanvas().getRenderingTransform()!=null){
						
						affine.preConcatenate(scrollpane.getSVGCanvas().getRenderingTransform());
					}

					try{
						bounds=affine.createTransformedShape(bounds).getBounds2D();
					}catch (Exception ex) {}
				}
			}
		}
		
		return bounds;
	}
	
	/**
	 * computes the position and the size of a node on the canvas 
	 * @param shape the node whose position and size is to be computed
	 * @return a rectangle representing the position and size of the given node
	 */
	public Rectangle2D.Double getNodeGeometryBounds(Element shape){
		
		Rectangle2D.Double bounds=new Rectangle2D.Double();
		
		if(shape!=null){
			
			//gets the bridge context 
			BridgeContext ctxt=getScrollPane().getSVGCanvas().getBridgeContext();
			
			if(ctxt!=null){
				
				//gets the graphics node corresponding to the given node
				GraphicsNode gnode=null;
				
				try{gnode=ctxt.getGraphicsNode(shape);}catch (Exception e){gnode=null;}
				
				if(gnode!=null){

					Rectangle2D bounds2D=gnode.getTransformedBounds(new AffineTransform());
					
					if(bounds2D!=null) {
						
						bounds=new Rectangle2D.Double(	bounds2D.getX(), bounds2D.getY(), 
																				bounds2D.getWidth(), bounds2D.getHeight());
					}
				}
			}
		}
		
		return bounds;
	}
	
	/**
	 * computes the outline of a node on the canvas that has the node's transform applied
	 * @param shapeNode a shape node
	 * @param af an affine transform
	 * @return the outline of the given node
	 */
	public Shape getTransformedOutline(Element shapeNode, AffineTransform af){

		Shape shape=new Rectangle();
		
		if(shapeNode!=null){

			if(af==null){
				
				af=new AffineTransform();
			}
			
			//gets the bridge context 
			BridgeContext ctxt=getScrollPane().getSVGCanvas().getBridgeContext();
			
			if(ctxt!=null){

				//gets the graphics node corresponding to the given node
				GraphicsNode gnode=null;
				
				try{gnode=ctxt.getGraphicsNode(shapeNode);}catch (Exception e){gnode=null;}
				
				if(gnode!=null){

					shape=gnode.getOutline();
					
					//transforming the shape
					if(shape!=null){

						AffineTransform affine=new AffineTransform();

						if(gnode.getTransform()!=null){
							
							affine.preConcatenate(gnode.getTransform());
						}
						
						affine.preConcatenate(af);

						if(scrollpane.getSVGCanvas().getViewingTransform()!=null){

							affine.preConcatenate(scrollpane.getSVGCanvas().getViewingTransform());
						}
						
						if(scrollpane.getSVGCanvas().getRenderingTransform()!=null){
							
							affine.preConcatenate(scrollpane.getSVGCanvas().getRenderingTransform());
						}

						shape=affine.createTransformedShape(shape);
					}
				}
			}
		}
		
		return shape;
	}
	
	/**
	 * computes the transform of a node on the canvas that has the node's transform applied
	 * @param shapeNode a shape node
	 * @return the transform of the given node
	 */
	public AffineTransform getTransform(Node shapeNode){

		AffineTransform af=null;
		
		if(shapeNode!=null && shapeNode instanceof Element){

			//gets the bridge context 
			BridgeContext ctxt=getScrollPane().getSVGCanvas().getBridgeContext();
			
			if(ctxt!=null){

				//gets the graphics node corresponding to the given node
				GraphicsNode gnode=null;
				
				try{gnode=ctxt.getGraphicsNode((Element)shapeNode);}catch (Exception e){gnode=null;}
				
				if(gnode!=null){

					af=gnode.getTransform();
				}
			}
		}
		
		return af;
	}

	/**
	 * computes the outline of a node on the canvas
	 * @param node the node
	 * @return the outline of the given node
	 */
	public Shape getOutline(Node node){
		
		Shape shape=new Rectangle();
		
		if(node!=null && node instanceof Element){
			
			//gets the bridge context 
			BridgeContext ctxt=getScrollPane().getSVGCanvas().getBridgeContext();
			
			if(ctxt!=null){
				
				//gets the graphics node corresponding to the given node
				GraphicsNode gnode=null;
				
				try{gnode=ctxt.getGraphicsNode((Element)node);}catch (Exception e){gnode=null;}
				
				if(gnode!=null){
					
					AffineTransform affine=new AffineTransform();

					if(gnode.getTransform()!=null){
						
						affine.preConcatenate(gnode.getTransform());
					}
					
					try{
						shape=affine.createTransformedShape(getGeometryShape(gnode));
					}catch (Exception ex){}
				}
			}
		}
		
		return shape;
	}
	
	/**
	 * returns the shape of the given graphics node
	 * @param graphicsNode a graphics node
	 * @return the shape of the given graphics node
	 */
	protected Shape getGeometryShape(GraphicsNode graphicsNode){
		
		Shape shape=new Rectangle();
		
		if(graphicsNode!=null){
			
			shape=graphicsNode.getOutline();
		}
		
		return shape;
	}
	
	/**
	 * returns the nodes at the given point
	 * @param parent the parent node
	 * @param point the point on which a mouse event has been done
	 * @return the node on which a mouse event has been done
	 */
	public Node getNodeAt(Node parent, Point2D.Double point){
		
		Node pointNode=null;
		
		if(parent!=null && point!=null){
			
			int diff=6;
			GraphicsNode gpointNode=null;
			
			//getting the graphics node of the parent node
			BridgeContext ctxt=getScrollPane().getSVGCanvas().getBridgeContext();
			
			if(ctxt!=null){
				
				GraphicsNode gparentNode=null;
				
				try{gparentNode=ctxt.getGraphicsNode((Element)parent);}catch (Exception e){gparentNode=null;}
				
				if(gparentNode!=null){
					
					//setting the selection mode for the parent and its children
					gparentNode.setPointerEventType(GraphicsNode.VISIBLE_PAINTED);
					
					//retrieving the node corresponding to the given point and its associated area
					gpointNode=gparentNode.nodeHitAt(point);
					
					if(gpointNode==null){
						
						for(int i=-diff/2; i<=diff/2; i++){
							
							gpointNode=gparentNode.nodeHitAt(new Point2D.Double(point.x-diff/2, point.y+i));
							
							if(gpointNode!=null){
								
								break;
							}
							
							gpointNode=gparentNode.nodeHitAt(new Point2D.Double(point.x+diff/2, point.y+i));
							
							if(gpointNode!=null){
								
								break;
							}
							
							gpointNode=gparentNode.nodeHitAt(new Point2D.Double(point.x+i, point.y-diff/2));
							
							if(gpointNode!=null){
								
								break;
							}
							
							gpointNode=gparentNode.nodeHitAt(new Point2D.Double(point.x+i, point.y+diff/2));
							
							if(gpointNode!=null){
								
								break;
							}
						}
					}
					
					if(gpointNode!=null){
						
						//getting the graphics node corresponding to the found graphics node and that is
						//a parent of the parent graphics node
						while(gpointNode!=null && ! gpointNode.getParent().equals(gparentNode)){
							
							gpointNode=gpointNode.getParent();
						}
						
						if(gpointNode!=null){
							
							//retrieving the element corresponding to this node
							pointNode=ctxt.getElement(gpointNode);
							
							//checks the type of the nodes
							if(		pointNode!=null && (pointNode.getNodeName().equals("defs") ||  //$NON-NLS-1$
									pointNode.getNodeName().equals("svg"))){ //$NON-NLS-1$
								
								pointNode=null;
							}
						}
					}
				}
			}
		}
		
		return pointNode;
	}
	
	/**
	 * find the accurate id for a node
	 * @param baseString the base of the id
	 * @return an id
	 */
	public String getId(String baseString){
		
		Document doc=getScrollPane().getSVGCanvas().getDocument();
		
		if(doc!=null){
			
			LinkedList<String> ids=new LinkedList<String>();
			Node current=null;
			String attId=""; //$NON-NLS-1$
			
			//adds to the list all the ids found among the children of the root element
			for(NodeIterator it=new NodeIterator(doc.getDocumentElement()); it.hasNext();){
				
				current=it.next();
				
				if(current!=null && current instanceof Element){
					
					attId=((Element)current).getAttribute("id"); //$NON-NLS-1$
					
					if(attId!=null && ! attId.equals("")){ //$NON-NLS-1$
						
						ids.add(attId);
					}
				}
			}
			
			int i=0;
			
			//tests for each integer string if it is already an id
			while(ids.contains(baseString.concat(i+""))){ //$NON-NLS-1$
				
				i++;
			}
			
			return new String(baseString.concat(i+"")); //$NON-NLS-1$
		}
		
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * checks whether the given id already exists or not among the children of the given root node
	 * @param id an id to be checked
	 * @return true if the id does not already exists
	 */
	public boolean checkId(String id){
		
		Document doc=getScrollPane().getSVGCanvas().getDocument();
		
		if(doc!=null){
			
			LinkedList<String> ids=new LinkedList<String>();
			Node current=null;
			String attId=""; //$NON-NLS-1$
			
			//adds to the list all the ids found among the children of the root element
			for(NodeIterator it=new NodeIterator(doc.getDocumentElement()); it.hasNext();){
				
				current=it.next();
				
				if(current!=null && current instanceof Element){
					
					attId=((Element)current).getAttribute("id"); //$NON-NLS-1$
					
					if(attId!=null && ! attId.equals("")){ //$NON-NLS-1$
						
						ids.add(attId);
					}
				}
			}
			
			//tests for each integer string if it is already an id
			if(ids.contains(id)){
				
				return false;
				
			}
			
			return true; 
		}
		
		return false;
	}
	
	
	
	public Element getDefsElement()
	{
	    Document doc=getScrollPane().getSVGCanvas().getDocument();
	    return getDefsElement(doc);
	}
	
	/**
	 * @return the "defs" node in a svg document
	 */
	public Element getDefsElement(Document doc){
		
		Element defs=null;
		
		
		if(doc!=null){
			
			Element root=doc.getDocumentElement();
			
			if(root!=null){
				
				final NodeList defsNodes=doc.getElementsByTagName("defs"); //$NON-NLS-1$
				
				if(defsNodes!=null && defsNodes.getLength()>0){
					
					defs=(Element)defsNodes.item(0);
					
					//getting the list of the additionnal defs
					if(defsNodes.getLength()>1) {
						
						ArrayList<Element> additionalDefs=new ArrayList<Element>();
						Element defs2=null;
						
						//inserting each resource that could be found in other "defs" nodesto the first "defs" node
						for(int i=1; i<defsNodes.getLength(); i++) {
							
							defs2=(Element)defsNodes.item(i);
							additionalDefs.add(defs2);
							
							while (defs2.hasChildNodes()){
								
								defs.appendChild(defs2.removeChild(defs2.getFirstChild()));
							}
						}
						
						//for each extra "defs" found nodes
						for(Element defs3 : additionalDefs) {
							
							defs3.getParentNode().removeChild(defs3);
						}
					}
				}
				else { //no def nodes
					
					//adds a "defs" node to the svg document
					defs=doc.createElementNS(null, "defs"); //$NON-NLS-1$
                       
					final Node froot=root;
					final Node fdefs=defs;
					
					//the runnable that contains the code that will be executed to append the "defs" node to the root node
					Runnable runnable=new Runnable(){
						
						public void run() {
							
							if(froot.getFirstChild()!=null){
								
								froot.insertBefore(fdefs, froot.getFirstChild());
								
							}else{
								
								froot.appendChild(fdefs);
							}
						}
					};
					
					enqueue(runnable);
				}
			}
		}
		
		return defs;
	}
	
	/**
	 * Returns the map associating an id to a resource node.
	 * Does not recurse into groups within the defs node
	 * @param doc the document
	 * @param resourceTagNames the list of the tag names of the resources that should appear in the returned list
	 * @return the map associating an id to a resource node
	 */
	public Hashtable<String, Element> getResourcesFromDefs(Document doc, LinkedList<String> resourceTagNames){
		
		Hashtable<String, Element> idResources=new Hashtable<String, Element>();

		//getting the list of the resources that can be found in the defs elements
		if(doc!=null && resourceTagNames!=null && resourceTagNames.size()>0){

			//the list of the available defs nodes that can be found in the document
			final NodeList defsNodes=doc.getElementsByTagName("defs"); //$NON-NLS-1$

			if(defsNodes!=null && defsNodes.getLength()>0){
				
				Element defs=null, el=null;
				Node cur=null;
				String id=""; //$NON-NLS-1$
				
				for(int i=0; i<defsNodes.getLength(); i++) {
					
					defs=(Element)defsNodes.item(i);
					
					collectResourcesFromElement(defs, idResources, resourceTagNames);
				}
			}
		}
		
		return idResources;
	}
	
   public void collectResourcesFromElement(Element element, 
           Map<String, Element> resourceCollector,
           LinkedList<String> resourceTagNames)
   {
       collectResourcesFromElement(element, resourceCollector, resourceTagNames, false);
   }
	
	
	public void collectResourcesFromElement(Element element, 
	                        Map<String, Element> resourceCollector,
	                        LinkedList<String> resourceTagNames,
	                        boolean recurse)
    {
        if (element == null) return;
        // for each child of the main element, add an id->element if a
        // relevant resource. Recurse into groups.
        for (Node curNode = element.getFirstChild(); curNode != null; curNode = curNode.getNextSibling())
        {
            if ( !(curNode instanceof Element)) continue;
            
            if (resourceTagNames.contains(curNode.getNodeName()))
            {
                Element resElement = (Element)curNode;
                String id = resElement.getAttribute("id"); //$NON-NLS-1$
                if (id != null && !id.isEmpty())
                {
                    resourceCollector.put(id, resElement);
                }
            }
            else if (recurse && "g".equals(curNode.getNodeName())) //recurse down into groups //$NON-NLS-1$
            {
                collectResourcesFromElement((Element)curNode, resourceCollector, resourceTagNames, true);
            }
        }
    }
	
	
	/**
     * @return the list of the ids of the shape nodes that are contained in the
     *         given svg document
     */
	public LinkedList<String> getShapeNodesIds(){
		
		LinkedList<String> idNodes=new LinkedList<String>();
		Document doc=getScrollPane().getSVGCanvas().getDocument();
		
		if(doc!=null && doc.getDocumentElement()!=null){
			
			Node cur=null;
			String id=""; //$NON-NLS-1$
			Element el=null;
			
			//for each children of the root element (but the "defs" element), adds its id to the map
			for(NodeIterator it=new NodeIterator(doc.getDocumentElement()); it.hasNext();){
				
				cur=it.next();
				
				if(cur instanceof Element) {
					
					el=(Element)cur;
					
					if(SVGToolkit.isElementAShape(el)) {
						
						id=el.getAttribute("id"); //$NON-NLS-1$
						
						if(id!=null && ! id.equals("")){ //$NON-NLS-1$
							
							idNodes.add(id);
						}
					}
				}
			}
		}
		
		return idNodes;
	}
	
	/**
	 * checks if the given node is using a resource and registers it on the canvas if this is such a case
	 * @param node a node
	 */
	public void registerUsedResource(Node node){
		
		if(node!=null && node instanceof Element){
			
			//the map of the used resources
			Map usedResourcesMap=getUsedResources();
			
			//getting the style attribute
			String style=((Element)node).getAttribute("style"), id=""; //$NON-NLS-1$ //$NON-NLS-2$
			
			if(style!=null && ! style.equals("")){ //$NON-NLS-1$
				
				LinkedList nodesList=null;
				
				//for each resource id, checks if it is contained in the style attribute
				for(Iterator it=usedResourcesMap.keySet().iterator(); it.hasNext();){
					
					try{
						id=(String)it.next();
						nodesList=(LinkedList)usedResourcesMap.get(id);
					}catch (Exception ex){id=null; nodesList=null;}
					
					//adds the node in the used resource map
					if(id!=null && ! id.equals("") && style.indexOf("#".concat(id))!=-1 && nodesList!=null && ! nodesList.contains(node)){ //$NON-NLS-1$ //$NON-NLS-2$
						
						addNodeUsingResource(id, node);
					}
				}
			}
			
			//registers all the used resources that could be found in the children nodes of this node
			for(Node cur=node.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
				
				if(cur instanceof Element){
					
					registerUsedResource(cur);
				}
			}
		}
	}
	
	/**
	 * checks if the given node is using a resource and unregisters it in the canvas if this is such a case
	 * @param node a node
	 */
	public void unregisterAllUsedResource(Node node){
		
		if(node!=null && node instanceof Element){
			
			//the map of the used resources
			Map usedResourcesMap=getUsedResources();
			
			//getting the style attribute
			String style=((Element)node).getAttribute("style"), id=""; //$NON-NLS-1$ //$NON-NLS-2$
			
			if(style!=null && ! style.equals("")){ //$NON-NLS-1$
				
				LinkedList nodesList=null;
				
				//for each resource id, checks if it is contained in the style attribute
				for(Iterator it=usedResourcesMap.keySet().iterator(); it.hasNext();){
					
					try{
						id=(String)it.next();
						nodesList=(LinkedList)usedResourcesMap.get(id);
					}catch (Exception ex){id=null; nodesList=null;}
					
					//removes the node from the used resource map
					if(id!=null && ! id.equals("") && style.indexOf("#".concat(id))!=-1 && nodesList!=null && nodesList.contains(node)){ //$NON-NLS-1$ //$NON-NLS-2$
						
						removeNodeUsingResource(id, node);
					}
				}
			}
			
			//unregisters all the used resources that could be found in the children nodes of this node
			for(Node cur=node.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
				
				if(cur instanceof Element){
					
					unregisterAllUsedResource(cur);
				}
			}
		}
	}
	
	/**
	 * scales the given rectangle 
	 * @param rectangle the rectangle to scale
	 * @param toBaseScale 	true to scale it to 100%
	 * 										false to scale it at the current canvas scale
	 * @return the scaled rectangle
	 */
	public Rectangle2D.Double getScaledRectangle(Rectangle2D.Double rectangle, boolean toBaseScale){
		
		Rectangle2D.Double rect=new Rectangle2D.Double(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
		
		if(toBaseScale){
			
			//applying the inverse of the transforms
			AffineTransform af=new AffineTransform();
			
			try{
				af.preConcatenate(scrollpane.getSVGCanvas().getRenderingTransform().createInverse());
			}catch (Exception ex){}
			
			try{
				af.preConcatenate(scrollpane.getSVGCanvas().getViewingTransform().createInverse());
			}catch (Exception ex){}

			if(af!=null){
				
				Rectangle2D rect2=af.createTransformedShape(rect).getBounds2D();
				rect=new Rectangle2D.Double(rect2.getX(), rect2.getY(), rect2.getWidth(), rect2.getHeight());
			}
			
		}else{
			
			//applying the transforms
			AffineTransform af=new AffineTransform();
			
			try{
				af.preConcatenate(scrollpane.getSVGCanvas().getViewingTransform());
			}catch (Exception ex){}
			
			try{
				af.preConcatenate(scrollpane.getSVGCanvas().getRenderingTransform());
			}catch (Exception ex){}

			if(af!=null){
				
				Rectangle2D rect2=af.createTransformedShape(rect).getBounds2D();
				rect=new Rectangle2D.Double(rect2.getX(), rect2.getY(), rect2.getWidth(), rect2.getHeight());
			}
		}
		
		return rect;
	}
	
	/**
	 * scales the given point 
	 * @param point the point to scale
	 * @param toBaseScale 	true to scale it to 100%
	 * 									false to scale it at the current canvas scale
	 * @return the scaled point
	 */
	public Point2D.Double getScaledPoint(Point2D.Double point, boolean toBaseScale){

		Point2D.Double point2D=new Point2D.Double(point.getX(), point.getY());
		
		if(point!=null){
			
			if(toBaseScale){
				
				//applying the inverse of the transforms
				AffineTransform af=new AffineTransform();
				
				try{
					af.preConcatenate(scrollpane.getSVGCanvas().getRenderingTransform().createInverse());
				}catch (Exception ex){}
				
				try{
					af.preConcatenate(scrollpane.getSVGCanvas().getViewingTransform().createInverse());
				}catch (Exception ex){}
				
				if(af!=null){

					Point2D pt=af.transform(point, null);
					point2D=new Point2D.Double(pt.getX(), pt.getY());
				}

			}else{

				//applying the inverse of the transforms
				AffineTransform af=new AffineTransform();
				
				try{
					af.preConcatenate(scrollpane.getSVGCanvas().getViewingTransform());
				}catch (Exception ex){}
				
				try{
					af.preConcatenate(scrollpane.getSVGCanvas().getRenderingTransform());
				}catch (Exception ex){}
				
				if(af!=null){

					Point2D pt=af.transform(point, null);
					point2D=new Point2D.Double(pt.getX(), pt.getY());
				}
			}	
		}
		
		return point2D;
	}
	
	/**
	 * picks the color at the given point on a canvas
	 * @param point a point
	 * @return the color corresponding to the given point
	 */
	public Color pickColor(Point point){
		
		Color color=new Color(255, 255, 255);
		
		if(point!=null){
			
			//getting the offscreen image of the canvas
			BufferedImage image=getScrollPane().getSVGCanvas().getOffscreen();
			
			int pos=image.getRGB(point.x, point.y);
			ColorModel model=image.getColorModel();
			
			if(pos!=0){
				
				color=new Color(model.getRed(pos), model.getGreen(pos), model.getBlue(pos));
			}
		}
		
		return color;
	}
	
	/**
	 *the class of the state bar
	 *
	 * @author Jordi SUC
	 *
	 */
	public class SVGStateBar extends JPanel{
		
		/**
		 * the labels for displayed information
		 */
		private JLabel fileName=new JLabel(), zoom=new JLabel() , lx=new JLabel(), 
		ly=new JLabel(), lw=new JLabel(), lh=new JLabel(), infos=new JLabel();
		
		/**
		 * the default font
		 */
		private Font myFont=new Font("myFont", Font.ROMAN_BASELINE,10); //$NON-NLS-1$
		
		private String statebarx="", statebary="", statebarw="", statebarh=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
		/**
		 * the constructor of the class
		 */
		public SVGStateBar(){
			
			setPreferredSize(new Dimension(framePanel.getWidth(), 20));
			
			//gets the labels from the resources
			ResourceBundle bundle=SVGEditor.getBundle();
			
			if(bundle!=null){
				
				statebarx=bundle.getString("labelx"); //$NON-NLS-1$
				statebary=bundle.getString("labely"); //$NON-NLS-1$
				statebarw=bundle.getString("labelw"); //$NON-NLS-1$
				statebarh=bundle.getString("labelh"); //$NON-NLS-1$
			}
			
			//Border borderr=new SoftBevelBorder(BevelBorder.RAISED);
			setBorder(new EmptyBorder(2, 2, 1, 1));
			Border border=BorderFactory.createEtchedBorder();
			fileName.setBorder(border);
			zoom.setBorder(border);
			infos.setBorder(border);
			
			JPanel pxy=new JPanel();
			pxy.setLayout(new GridLayout(1,2,5,0));
			pxy.setBorder(border);
			pxy.add(lx);
			pxy.add(ly);
			
			JPanel pwh=new JPanel();
			pwh.setLayout(new GridLayout(1,2,5,0));
			pwh.setBorder(border);
			pwh.add(lw);
			pwh.add(lh);
			
			fileName.setHorizontalAlignment(SwingConstants.CENTER);
			zoom.setHorizontalAlignment(SwingConstants.CENTER);
			lx.setHorizontalAlignment(SwingConstants.RIGHT);
			ly.setHorizontalAlignment(SwingConstants.LEFT);
			lw.setHorizontalAlignment(SwingConstants.RIGHT);
			lh.setHorizontalAlignment(SwingConstants.LEFT);
			infos.setHorizontalAlignment(SwingConstants.CENTER);
			
			setLayout(new GridLayout(1,5,2,2));
			
			if(editor.isMultiWindow()){
				
				fileName.setFont(myFont);
				zoom.setFont(myFont);
				lx.setFont(myFont);
				ly.setFont(myFont);
				lw.setFont(myFont);
				lh.setFont(myFont);
				infos.setFont(myFont);
			}
			
			add(fileName);
			add(zoom);
			add(pxy);
			add(pwh);
			add(infos);
			
			setSVGZoom("100 %"); //$NON-NLS-1$
		}
		
		/**
		 * sets the name of the SVG picture
		 * @param nm the name of the SVG picture
		 */
		public void setSVGName(String nm){
			fileName.setText(nm);
		}
		
		/**
		 * sets a string representation of the zoom scale of the SVG picture
		 * @param zm a string representation of the zoom scale of the SVG picture
		 */		
		public void setSVGZoom(String zm){
			zoom.setText(zm);
		}	
		
		/**
		 * sets a string representation of the x position of the mouse on the canvas
		 * @param sx a string representation of the x position of the mouse on the canvas
		 */				
		public void setSVGX(String sx){
			
			if(sx!=null && !sx.equals("")){ //$NON-NLS-1$
				
				lx.setText(statebarx+sx);
				
			}else{
				
				lx.setText(""); //$NON-NLS-1$
			}
		}
		
		/**
		 * sets a string representation of the y position of the mouse on the canvas
		 * @param sy a string representation of the y position of the mouse on the canvas
		 */				
		public void setSVGY(String sy){
			
			if(sy!=null && !sy.equals("")){ //$NON-NLS-1$
				
				ly.setText(statebary+sy);
				
			}else{
				
				ly.setText(""); //$NON-NLS-1$
			}
		}
		
		/**
		 * sets a string representation of the width of a shape
		 * @param sw a string representation of the width of a shape
		 */				
		public void setSVGW(String sw){
			
			if(sw!=null && !sw.equals("")){ //$NON-NLS-1$
				
				lw.setText(statebarw+sw);
				
			}else{
				
				lw.setText(""); //$NON-NLS-1$
			}
		}
		
		/**
		 * sets a string representation of the height of a shape
		 * @param sh a string representation of the height of a shape
		 */				
		public void setSVGH(String sh){
			
			if(sh!=null && !sh.equals("")){ //$NON-NLS-1$
				
				lh.setText(statebarh+sh);
				
			}else{
				
				lh.setText(""); //$NON-NLS-1$
			}
		}
		
		/**
		 * sets the string representing information to be displayed
		 * @param in a string representing information to be displayed
		 */
		public void setSVGInfos(String in){
			infos.setText(in);
		}		
	}
}
