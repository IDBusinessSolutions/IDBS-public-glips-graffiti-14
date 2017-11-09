/*
 * Created on 11 mai 2004
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
package fr.itris.glips.svgeditor.shape;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.*;
import java.util.*;

import javax.swing.*;

import org.apache.batik.ext.awt.geom.*;
import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.menutool.*;
import fr.itris.glips.svgeditor.resources.*;
import fr.itris.glips.svgeditor.selection.*;
import fr.itris.glips.svgeditor.undoredo.*;

/**
 * @author Jordi SUC
 * the class that allows to add, select,modify the properties, delete, transform a path on the canvas
 */
public class SVGPath extends SVGShape{

	/**
	 * the reference of an object of this class
	 */
	private final SVGPath svgPath=this;
	
	/**
	 * the action listener used to draw the path
	 */
	private PathActionListener pathActionQuadratic=null, pathActionCubic=null;
	
	/**
	 * the menu items
	 */
	private JMenuItem quadratic, cubic, union, subtraction, intersection, convert;
	
	/**
	 * the listeners to the menu items
	 */
	private ActionListener quadraticListener, cubicListener, unionListener, subtractionListener, intersectionListener, convertListener;
	
	/**
	 * the tool items
	 */
	private JToggleButton quadraticTool, cubicTool;
	
	/**
	 * the icons
	 */
	private ImageIcon	quadraticIcon, quadraticDisabledIcon, cubicIcon, cubicDisabledIcon, convertIcon, convertDisabledIcon, unionIcon, unionDisabledIcon, 
								subtractionIcon, subtractionDisabledIcon, intersectionIcon, intersectionDisabledIcon;
	
	/**
	 * the nodes that are currently selected
	 */
	private LinkedList selectedNodes=new LinkedList();
	
	/**
	 * the format
	 */
	private DecimalFormat format=null;
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGPath(SVGEditor editor) {
	    
		super(editor);
		
		ids.put("id","path"); //$NON-NLS-1$ //$NON-NLS-2$
		ids.put("idmenupathoperations","PathOperations"); //$NON-NLS-1$ //$NON-NLS-2$
		ids.put("idconvert","ConvertToPath"); //$NON-NLS-1$ //$NON-NLS-2$
		ids.put("idunion","PathUnion"); //$NON-NLS-1$ //$NON-NLS-2$
		ids.put("idsubtraction","PathSubtraction"); //$NON-NLS-1$ //$NON-NLS-2$
		ids.put("idintersection","PathIntersection"); //$NON-NLS-1$ //$NON-NLS-2$
		ids.put("idquadratic","QuadraticBezier"); //$NON-NLS-1$ //$NON-NLS-2$
		ids.put("idcubic","CubicBezier"); //$NON-NLS-1$ //$NON-NLS-2$
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labels.put("label", bundle.getString("shapepathlabel")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("labelpathoperations", bundle.getString("shapepathlabeloperations")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("labelquadratic", bundle.getString("shapepathlabelquadratic")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("labelcubic", bundle.getString("shapepathlabelcubic")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("labelunion", bundle.getString("shapepathlabelunion")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("labelsubtraction", bundle.getString("shapepathlabelsubtraction")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("labelintersection", bundle.getString("shapepathlabelintersection")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("labelconvert", bundle.getString("shapepathlabelconvert")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredocreatequadratic", bundle.getString("shapepathundoredocreatequadratic")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredocreatecubic", bundle.getString("shapepathundoredocreatecubic")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredotranslate", bundle.getString("shapeundoredotranslate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredoresize", bundle.getString("shapepathundoredoresize")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredomodifypoint", bundle.getString("shapepathundoredomodifypoint")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredorotate", bundle.getString("shapepathundoredorotate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredounion", bundle.getString("shapepathundoredounion")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredosubtraction", bundle.getString("shapepathundoredosubtraction")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredointersection", bundle.getString("shapepathundoredointersection")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredoconvert", bundle.getString("shapepathundoredoconvert")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("helpcreatequadratic", bundle.getString("shapepathhelpcreatequadratic")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("helpcreatecubic", bundle.getString("shapepathhelpcreatecubic")); //$NON-NLS-1$ //$NON-NLS-2$
			}catch (Exception ex){}		
		}
		
		//the object used to convert double values into strings
		DecimalFormatSymbols symbols=new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		format=new DecimalFormat("######.#",symbols); //$NON-NLS-1$
		
		//the icons
		quadraticIcon=SVGResource.getIcon(ids.get("idquadratic"), false); //$NON-NLS-1$
		quadraticDisabledIcon=SVGResource.getIcon(ids.get("idquadratic"), true); //$NON-NLS-1$
		cubicIcon=SVGResource.getIcon(ids.get("idcubic"), false); //$NON-NLS-1$
		cubicDisabledIcon=SVGResource.getIcon(ids.get("idcubic"), true); //$NON-NLS-1$
		convertIcon=SVGResource.getIcon(ids.get("idconvert"), false); //$NON-NLS-1$
		convertDisabledIcon=SVGResource.getIcon(ids.get("idconvert"), true); //$NON-NLS-1$
		unionIcon=SVGResource.getIcon(ids.get("idunion"), false); //$NON-NLS-1$
		unionDisabledIcon=SVGResource.getIcon(ids.get("idunion"), true); //$NON-NLS-1$
		subtractionIcon=SVGResource.getIcon(ids.get("idsubtraction"), false); //$NON-NLS-1$
		subtractionDisabledIcon=SVGResource.getIcon(ids.get("idsubtraction"), true); //$NON-NLS-1$
		intersectionIcon=SVGResource.getIcon(ids.get("idintersection"), false); //$NON-NLS-1$
		intersectionDisabledIcon=SVGResource.getIcon(ids.get("idintersection"), true); //$NON-NLS-1$
		
		//the menu items
		quadratic=new JMenuItem((String)labels.get("labelquadratic"), quadraticIcon); //$NON-NLS-1$
		quadratic.setDisabledIcon(quadraticDisabledIcon);
		quadratic.setEnabled(false);
		
		cubic=new JMenuItem((String)labels.get("labelcubic"), cubicIcon); //$NON-NLS-1$
		cubic.setDisabledIcon(cubicDisabledIcon);
		cubic.setEnabled(false);
		
		//the toggle buttons
		quadraticTool=new JToggleButton(quadraticDisabledIcon);
		quadraticTool.setEnabled(false);
		quadraticTool.setToolTipText((String)labels.get("labelquadratic")); //$NON-NLS-1$
		
		cubicTool=new JToggleButton(cubicDisabledIcon);
		cubicTool.setEnabled(false);
		cubicTool.setToolTipText((String)labels.get("labelcubic")); //$NON-NLS-1$
		
		//the listeners
		pathActionQuadratic=new PathActionListener(PathActionListener.QUADRATIC_BEZIER);
		pathActionCubic=new PathActionListener(PathActionListener.CUBIC_BEZIER);
		quadratic.addActionListener(pathActionQuadratic);
		cubic.addActionListener(pathActionCubic);
		quadraticTool.addActionListener(pathActionQuadratic);
		cubicTool.addActionListener(pathActionCubic);
		
		final SVGEditor feditor=editor;
		
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

				//clears the list of the selected items
				selectedNodes.clear();
				
				if(getSVGEditor().getFrameManager().getFrameNumber()>0){

				    convert.setEnabled(true);
				    
					quadratic.setEnabled(true);
					quadraticTool.setEnabled(true);
					quadraticTool.setIcon(quadraticIcon);
					
					cubic.setEnabled(true);
					cubicTool.setEnabled(true);
					cubicTool.setIcon(cubicIcon);
					
				}else{

				    convert.setEnabled(false);
				    
					quadratic.setEnabled(false);
					quadraticTool.setEnabled(false);
					quadraticTool.setIcon(quadraticDisabledIcon);
					
					cubic.setEnabled(false);
					cubicTool.setEnabled(false);
					cubicTool.setIcon(cubicDisabledIcon);
				}
				
				pathActionQuadratic.reset();
				pathActionCubic.reset();
				
				final SVGFrame frame=feditor.getFrameManager().getCurrentFrame();
				
				//if a selection listener is already registered on a selection module, it is removed	
				if(selection!=null && selectionListener!=null){
				    
					selection.removeSelectionListener(selectionListener);
				}

				//gets the current selection module	
				if(frame!=null){
				    
					selection=feditor.getSVGSelection();
				}
				
				if(frame!=null && selection!=null){
				    
					manageSelection();
					
					//the listener of the selection changes
					selectionListener=new ActionListener(){

						public void actionPerformed(ActionEvent e) {
						    
							manageSelection();
						}
					};
					
					//adds the selection listener
					if(selectionListener!=null){
					    
						selection.addSelectionListener(selectionListener);
					}
				}
			}
			
			/**
			 * updates the selected items and the state of the menu items
			 */
			protected void manageSelection(){
			    
				//disables the menuitems
			    convert.setEnabled(false);
				union.setEnabled(false);
				subtraction.setEnabled(false);
				intersection.setEnabled(false);

				LinkedList list=null;
				
				//gets the currently selected nodes list 
				if(selection!=null){
				    
					list=selection.getCurrentSelection(feditor.getFrameManager().getCurrentFrame());
				}
				
				selectedNodes.clear();
				
				//refresh the selected nodes list
				if(list!=null){
				    
				    selectedNodes.addAll(list);
				}
				
				if(selectedNodes.size()>0){
				    
				    convert.setEnabled(true);
				    
					if(selectedNodes.size()>1){
					    
						union.setEnabled(true);
						intersection.setEnabled(true);
						
						if(selectedNodes.size()==2){
						    
							subtraction.setEnabled(true);
						}
					}
				}
			}
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);
		
		//the menuitems
		convert=new JMenuItem((String)labels.get("labelconvert"), convertIcon); //$NON-NLS-1$
		convert.setDisabledIcon(convertDisabledIcon);
		convert.setEnabled(false);
		
		convertListener=new ActionListener(){

			public void actionPerformed(ActionEvent e){
			    
				getSVGEditor().cancelActions(true);
				
				if(selectedNodes.size()>0){
				    
					SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
					
					Runnable runnable=new Runnable(){
						
						public void run() {

							convertToPath(getSVGEditor().getFrameManager().getCurrentFrame(), selectedNodes);
						}
					};
					
					frame.enqueue(runnable);
				}
			}
		};
		
		convert.addActionListener(convertListener);
		
		union=new JMenuItem((String)labels.get("labelunion"), unionIcon); //$NON-NLS-1$
		union.setDisabledIcon(unionDisabledIcon);
		union.setEnabled(false);
		
		unionListener=new ActionListener(){

			public void actionPerformed(ActionEvent e){
			    
				getSVGEditor().cancelActions(true);
				
				if(selectedNodes.size()>1){
					
					SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
					
					Runnable runnable=new Runnable(){
						
						public void run() {

							union(getSVGEditor().getFrameManager().getCurrentFrame(), selectedNodes);
						}
					};
					
					frame.enqueue(runnable);
				}
			}
		};
		
		union.addActionListener(unionListener);
		
		subtraction=new JMenuItem((String)labels.get("labelsubtraction"), subtractionIcon); //$NON-NLS-1$
		subtraction.setDisabledIcon(subtractionDisabledIcon);
		subtraction.setEnabled(false);
		
		subtractionListener=new ActionListener(){

			public void actionPerformed(ActionEvent e){
			    
				getSVGEditor().cancelActions(true);
				
				if(selectedNodes.size()==2){
					
					SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
					
					Runnable runnable=new Runnable(){
						
						public void run() {

							subtraction(getSVGEditor().getFrameManager().getCurrentFrame(), selectedNodes);
						}
					};
					
					frame.enqueue(runnable);
				}
			}
		};
		
		subtraction.addActionListener(subtractionListener);
		
		intersection=new JMenuItem((String)labels.get("labelintersection"), intersectionIcon); //$NON-NLS-1$
		intersection.setDisabledIcon(intersectionDisabledIcon);
		intersection.setEnabled(false);
		
		intersectionListener=new ActionListener(){

			public void actionPerformed(ActionEvent e){
			    
				getSVGEditor().cancelActions(true);
				
				if(selectedNodes.size()>0){
					
					SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
					
					Runnable runnable=new Runnable(){
						
						public void run() {

							intersection(getSVGEditor().getFrameManager().getCurrentFrame(), selectedNodes);
						}
					};
					
					frame.enqueue(runnable);
				}
			}
		};
		
		intersection.addActionListener(intersectionListener);
		
		//the menu allowing to make unions or intersections of paths
		menuitem=new JMenu((String)labels.get("labelpathoperations")); //$NON-NLS-1$
		menuitem.add(convert);
		menuitem.add(union);
		menuitem.add(subtraction);
		menuitem.add(intersection);
	}
	
	/**
	 * @return a map associating a menu item id to its menu item object
	 */
	public Hashtable getMenuItems(){
		
		Hashtable menuItems=new Hashtable();
		menuItems.put((String)ids.get("idmenupathoperations"), menuitem); //$NON-NLS-1$
		menuItems.put((String)ids.get("idquadratic"), quadratic); //$NON-NLS-1$
		menuItems.put((String)ids.get("idcubic"), cubic); //$NON-NLS-1$
		
		return menuItems;
	}
	
	/**
	 * @return a map associating a tool item id to its tool item object
	 */
	public Hashtable getToolItems(){
		
		Hashtable toolItems=new Hashtable();
		toolItems.put((String)ids.get("idquadratic"), quadraticTool); //$NON-NLS-1$
		toolItems.put((String)ids.get("idcubic"), cubicTool); //$NON-NLS-1$
		
		return toolItems;
	}

	/**
	 * Returns the collection of the popup items
	 * @return the collection of the popup items
	 */
	public Collection getPopupItems(){
		
		LinkedList popupItems=new LinkedList();
		
		SVGPopupSubMenu subMenu=new SVGPopupSubMenu(getSVGEditor(), (String)ids.get("idmenupathoperations"), (String)labels.get("labelpathoperations"), ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		popupItems.add(subMenu);
		
		//creating the convert to path popup item
		SVGPopupItem convertItem=new SVGPopupItem(getSVGEditor(), (String)ids.get("idconvert"), (String)labels.get("labelconvert"), (String)ids.get("idconvert")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
			public JMenuItem getPopupItem(LinkedList nodes) {
				
				if(nodes!=null && nodes.size()>0){
					
					menuItem.setEnabled(true);
					
					//adds the action listener
					menuItem.addActionListener(convertListener);
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the path union item
		SVGPopupItem unionItem=new SVGPopupItem(getSVGEditor(), (String)ids.get("idunion"), (String)labels.get("labelunion"), (String)ids.get("idunion")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
			public JMenuItem getPopupItem(LinkedList nodes){
				
				if(nodes!=null && nodes.size()>=2){
					
					menuItem.setEnabled(true);
					
					//adds the action listener
					menuItem.addActionListener(unionListener);
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the path subtraction item
		SVGPopupItem subtractionItem=new SVGPopupItem(getSVGEditor(), (String)ids.get("idsubtraction"), (String)labels.get("labelsubtraction"), (String)ids.get("idsubtraction")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
			public JMenuItem getPopupItem(LinkedList nodes){
				
				if(nodes!=null && nodes.size()==2){
					
					menuItem.setEnabled(true);
					
					//adds the action listener
					menuItem.addActionListener(subtractionListener);
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the path intersection item
		SVGPopupItem intersectionItem=new SVGPopupItem(getSVGEditor(), (String)ids.get("idintersection"), (String)labels.get("labelintersection"), (String)ids.get("idintersection")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
			public JMenuItem getPopupItem(LinkedList nodes){
				
				if(nodes!=null && nodes.size()>=2){
					
					menuItem.setEnabled(true);
					
					//adds the action listener
					menuItem.addActionListener(intersectionListener);
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//adding the popup items to the sub menu
		subMenu.addPopupItem(convertItem);
		subMenu.addPopupItem(unionItem);
		subMenu.addPopupItem(subtractionItem);
		subMenu.addPopupItem(intersectionItem);
		
		return popupItems;
	}
	
	/**
	 * draws a quadratic Bezier curve
	 * @param frame the current SVGFrame
	 * @param points the array of points
	 */
	protected void drawQuadraticBezier(SVGFrame frame, Point2D.Double[] points){
		
		if(frame!=null && points!=null && points.length>1){
			
			Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
			
			if(getSVGEditor().getSVGSelection()!=null && doc!=null){
			    
				final Element parent=getSVGEditor().getSVGSelection().getCurrentParentElement(frame);
				
				if(parent!=null && points.length>1){
			
					//creates the string of the values for the attribute
					int i;
					String value="M "+format.format(points[0].x)+" "+format.format(points[0].y)+" Q ";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			
					for(i=1;i<points.length;i++){
					    
						value=value.concat(format.format(points[i-1].x+(points[i].x-points[i-1].x)/2)+" "+format.format(points[i-1].y+(points[i].y-points[i-1].y)/2)+" "); //$NON-NLS-1$ //$NON-NLS-2$
						value=value.concat(format.format(points[i].x)+" "+format.format(points[i].y)+" "); //$NON-NLS-1$ //$NON-NLS-2$
					}

					//creates the path
					Element path = doc.createElementNS(doc.getDocumentElement().getNamespaceURI(),"path"); //$NON-NLS-1$
			
					path.setAttributeNS(null, "d", value); //$NON-NLS-1$
					String colorString=getSVGEditor().getColorChooser().getColorString(getSVGEditor().getSVGColorManager().getCurrentColor());
					path.setAttributeNS(null, "style", "stroke:".concat(colorString.concat("; fill:none"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
					//sets that the svg has been modified
					frame.setModified(true);
			
					//creates final variables
					final Document fdoc=doc;
					final Node fpath=path;
			
					// attaches the element to the svg root element
					parent.appendChild(fpath);

					//create the undo/redo action and insert it into the undo/redo stack
					if(getSVGEditor().getUndoRedo()!=null){

						SVGUndoRedoAction action=new SVGUndoRedoAction((String)labels.get("undoredocreatequadratic")){ //$NON-NLS-1$

							public void undo(){
							    
							    parent.removeChild(fpath);
							}

							public void redo(){
							    
							    parent.appendChild(fpath);
							}
						};
				
						//adds the undo/redo actions into the stack
						SVGSelection selection=getSVGEditor().getSVGSelection();
				
						if(selection!=null){
						    
							selection.deselectAll(frame, false, true);
							selection.addUndoRedoAction(frame, action);
							selection.handleNodeSelection(frame, path);
							selection.addUndoRedoAction(frame, new SVGUndoRedoAction((String)labels.get("undoredocreatequadratic")){}); //$NON-NLS-1$
							selection.refreshSelection(frame);
					
						}else{
						    
							SVGUndoRedoActionList actionList=new SVGUndoRedoActionList((String)labels.get("undoredocreatequadratic")); //$NON-NLS-1$
							actionList.add(action);
							getSVGEditor().getUndoRedo().addActionList(frame, actionList);
						}
					}
				}
			}
		}

	}
	
	/**
	 * draws a cubic Bezier curve
	 * @param frame the current SVGFrame
	 * @param points the array of points
	 */
	protected void drawCubicBezier(SVGFrame frame, Point2D.Double[] points){
		
		if(frame!=null && points!=null && points.length>1){
			
			Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
			
			if(getSVGEditor().getSVGSelection()!=null && doc!=null){
			    
				final Element parent=getSVGEditor().getSVGSelection().getCurrentParentElement(frame);
				
				if(parent!=null && points.length>1){

					//creates the string of the values for the attribute
					int i;
					String value="M "+format.format(points[0].getX())+" "+format.format(points[0].getY())+" C "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
					for(i=1;i<points.length;i++){
					    
						value=value.concat(format.format(points[i-1].x+(points[i].x-points[i-1].x)/4)+" "+format.format(points[i-1].y+(points[i].y-points[i-1].y)/4)+" "); //$NON-NLS-1$ //$NON-NLS-2$
						value=value.concat(format.format(points[i].x-(points[i].x-points[i-1].x)/4)+" "+format.format(points[i].y-(points[i].y-points[i-1].y)/4)+" "); //$NON-NLS-1$ //$NON-NLS-2$
						value=value.concat(format.format(points[i].x)+" "+format.format(points[i].y)+" "); //$NON-NLS-1$ //$NON-NLS-2$
					}

					//creates the path
					Element path=doc.createElementNS(doc.getDocumentElement().getNamespaceURI(),"path"); //$NON-NLS-1$
			
					path.setAttributeNS(null,"d",value); //$NON-NLS-1$
					String colorString=getSVGEditor().getColorChooser().getColorString(getSVGEditor().getSVGColorManager().getCurrentColor());
					path.setAttributeNS(null, "style", "stroke:".concat(colorString.concat("; fill:none"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
					//sets that the svg has been modified
					frame.setModified(true);
			
					//creates final variables
					final Node fpath=path;
						
					// attaches the element to the svg root element
					parent.appendChild(fpath);
			
					//create the undo/redo action and insert it into the undo/redo stack
					if(getSVGEditor().getUndoRedo()!=null){

						SVGUndoRedoAction action=new SVGUndoRedoAction((String)labels.get("undoredocreatecubic")){ //$NON-NLS-1$

							public void undo(){
							    
							    parent.removeChild(fpath);
							}

							public void redo(){
							    
							    parent.appendChild(fpath);
							}
						};
				
						//adds the undo/redo actions into the stack
						SVGSelection selection=getSVGEditor().getSVGSelection();
				
						if(selection!=null){
						    
							selection.deselectAll(frame, false, true);
							selection.addUndoRedoAction(frame, action);
							selection.handleNodeSelection(frame, path);
							selection.addUndoRedoAction(frame, new SVGUndoRedoAction((String)labels.get("undoredocreatecubic")){}); //$NON-NLS-1$
							selection.refreshSelection(frame);
					
						}else{
						    
							SVGUndoRedoActionList actionList=new SVGUndoRedoActionList((String)labels.get("undoredocreatecubic")); //$NON-NLS-1$
							actionList.add(action);
							getSVGEditor().getUndoRedo().addActionList(frame, actionList);
						}
					}
				}
			}
		}
	}
		
	/**
	 * draws what the path will be if the user double clicks
	 * @param frame the current SVGFrame
	 * @param graphics the graphics
	 * @param points an array of points
	 */
	protected void drawGhost(SVGFrame frame, Graphics graphics, Point2D.Double[] points){
		
		Graphics2D g=(Graphics2D)graphics;
		
		if(frame!=null && points!=null && points.length>1 && g!=null){
		    
		    g=(Graphics2D)g.create();
			
			g.setColor(GHOST_COLOR);
			g.setXORMode(Color.white);

			for(int i=1;i<points.length;i++){
			    
				g.drawLine((int)points[i-1].x, (int)points[i-1].y, (int)points[i].x, (int)points[i].y);
			}
			
			g.dispose();			
		}		
	}
	
	/**
	 * converts the nodes of the given list into paths
	 * @param frame the current SVGFrame
	 * @param nodes the list of the nodes to be converted
	 */
	protected void convertToPath(SVGFrame frame, LinkedList nodes){
		
		Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
		
		LinkedList snodes=new LinkedList(nodes);
		
	    //getting the parent element
	    Element p=null;
	    
	    try{
	        p=(Element)((Element)snodes.getFirst()).getParentNode();
	    }catch(Exception ex){p=null;}
	    
	    final Element parent=p;
						
		if(doc!=null && snodes!=null && snodes.size()>0 && parent!=null){

			final LinkedList oldchildren=new LinkedList();
			NodeList ch=parent.getChildNodes();
			Node node=null;
			int i;
			
			//the list of the children of the parent element
			for(i=0;i<ch.getLength();i++){

				oldchildren.add(ch.item(i));
			}

			//the list of the path that will be created
			LinkedList paths=new LinkedList();
			Element cur=null;
			Shape outline=null;
			GeneralPath gpath=null;
			LinkedHashMap map=null;
			LinkedList nlist=null;
			PathIterator pit=null;
			char cmd=' ';
			int rg=0;
			double[] values=null, vals=new double[7];
			int type=-1;
			Node cnode=null;
			
			for(Iterator it=snodes.iterator(); it.hasNext();){
			    
				try{cur=(Element)it.next();}catch (Exception ex){cur=null;}
				
				if(cur!=null){

				    outline=frame.getOutline(cur);
					gpath=new GeneralPath(outline);
				    
				    if(gpath!=null){
				        
						gpath.closePath();
						map=new LinkedHashMap();
						rg=0;
						
						//for each command in the path, the command and its values are added to the string value
						for(pit=gpath.getPathIterator(new AffineTransform()); ! pit.isDone(); pit.next()){
						    
							type=pit.currentSegment(vals);
							
							if(type==PathIterator.SEG_CLOSE){
							    
								cmd='Z';
								
							}else if(type==PathIterator.SEG_CUBICTO){
							    
								values=new double[6];
								pit.currentSegment(values);
								cmd='C';
								
							}else if(type==PathIterator.SEG_LINETO){
							    
								values=new double[2];
								pit.currentSegment(values);
								cmd='L';
								
							}else if(type==PathIterator.SEG_MOVETO){
							    
								values=new double[2];
								pit.currentSegment(values);
								cmd='M';
								
							}else if(type==PathIterator.SEG_QUADTO){
							    
								values=new double[4];
								pit.currentSegment(values);
								cmd='Q';
								
							}else{
							    
							    cmd=' ';
							    values=null;
							}

							if(values!=null){
							    
								nlist=new LinkedList();
								
								for(i=0;i<values.length;i++){
								    
									nlist.add(new Double(values[i]));
								}
								
								map.put(cmd+new Integer(rg++).toString(), nlist);
							}
						}
						
						//creates the path element
						Element path=doc.createElementNS(doc.getDocumentElement().getNamespaceURI(),"path"); //$NON-NLS-1$
						
						//converts the map to pass to accurate argument for setting the d attribute
						LinkedHashMap map2=getSVGEditor().getSVGToolkit().convertPathValues(map);
						getSVGEditor().getSVGToolkit().setPathSeg(path, map2);
						String colorString=getSVGEditor().getColorChooser().getColorString(getSVGEditor().getSVGColorManager().getCurrentColor());
						
						path.setAttributeNS(null, "style", "fill:".concat(colorString.concat(";"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

						//appends the children nodes of the current node to the path node
						if(cur instanceof Element && ! cur.getNodeName().equals("text")){ //$NON-NLS-1$

						    for(node=cur.getFirstChild(); node!=null; node=node.getNextSibling()){
						        
						        cnode=node.cloneNode(true);
						        
						        if(cnode!=null){
						            
						            path.appendChild(cnode);
						        }
						    }
						}
						
						parent.appendChild(path);
						paths.add(path);
						parent.removeChild(cur);
						frame.unregisterAllUsedResource(node);
				    }
				}
			}

			//creates final variables
			final LinkedList newchildren=new LinkedList();
			final SVGFrame fframe=frame;

			ch=parent.getChildNodes();
			
			//the new list of the children of the parent element
			for(i=0;i<ch.getLength();i++){
			    
			    newchildren.add(ch.item(i));
			}
			
			//sets that the svg has been modified
			frame.setModified(true);
		
			//create the undo/redo action and insert it into the undo/redo stack
			if(getSVGEditor().getUndoRedo()!=null){

				SVGUndoRedoAction action=new SVGUndoRedoAction((String)labels.get("undoredoconvert")){ //$NON-NLS-1$

					public void undo(){
					    
						Node node=null;
					    
						//removes all the nodes from the parent element
						while(parent.hasChildNodes()){
						    
						    node=parent.getFirstChild();
						    
						    parent.removeChild(node);
						    fframe.unregisterAllUsedResource(node);
						}

						//appends all the old children	
						for(int i=0;i<oldchildren.size();i++){
						    
							try{
							    node=(Node)oldchildren.get(i);
							}catch(Exception ex){node=null;}
							
							if(node!=null){
							    
								parent.appendChild(node);
								fframe.registerUsedResource(node);
							}
						}
					}
					
					/**
					 * used to call all the actions that have to be done to redo an action
					 */
					public void redo(){
					    
						Node node=null;
						
						//removes all the old children	
						for(int i=0;i<oldchildren.size();i++){
						    
							try{
							    node=(Node)oldchildren.get(i);
							}catch(Exception ex){node=null;}
							
							if(node!=null){
							    
								parent.removeChild(node);
								fframe.unregisterAllUsedResource(node);
							}
						}

						//appends all the new children	
						for(int i=0;i<newchildren.size();i++){
						    
							try{
							    node=(Node)newchildren.get(i);
							}catch(Exception ex){}
							
							if(node!=null){
							    
								parent.appendChild(node);
								fframe.registerUsedResource(node);
							}
						}
					}
				};
			
				//adds the undo/redo actions into the stack
				SVGSelection selection=getSVGEditor().getSVGSelection();
			
				if(selection!=null){
				    
					selection.deselectAll(frame, false, false);
					selection.addUndoRedoAction(frame, action);
					
					//selects all the path nodes
					for(Iterator it=paths.iterator(); it.hasNext();){
					    
					    try{
					        node=(Node)it.next();
					    }catch(Exception ex){node=null;}
					    
					    if(node!=null && node instanceof Element){
					        
							selection.handleNodeSelection(frame, (Element)node);
					    }
					}

					selection.addUndoRedoAction(frame, new SVGUndoRedoAction((String)labels.get("undoredoconvert")){}); //$NON-NLS-1$
					selection.refreshSelection(frame);
				
				}else{
				    
					SVGUndoRedoActionList actionList=new SVGUndoRedoActionList((String)labels.get("undoredoconvert")); //$NON-NLS-1$
					actionList.add(action);
					getSVGEditor().getUndoRedo().addActionList(frame, actionList);
				}
			}
		}
	}
	
	/**
	 * converts the nodes of the given list into a path node by making a union
	 * @param frame the current SVGFrame
	 * @param nodes the list of the nodes to be converted
	 */
	protected void union(SVGFrame frame, LinkedList nodes){
		
		Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
		LinkedList snodes=new LinkedList(nodes);
		
	    //getting the parent element
	    Element p=null;
	    
	    try{
	        p=(Element)((Element)snodes.getFirst()).getParentNode();
	    }catch(Exception ex){p=null;}
	    
	    final Element parent=p;
						
		if(doc!=null && parent!=null && nodes!=null && nodes.size()>0){
			
			final LinkedList oldchildren=new LinkedList();
			NodeList ch=parent.getChildNodes();
			
			//the list of the children of the root element
			for(int i=0;i<ch.getLength();i++){

				oldchildren.add(ch.item(i));
			}

			//creating the union of the shapes of the elements
			Area area=new Area();
			Element cur=null;
			Shape outline=null;
			
			for(Iterator it=nodes.iterator(); it.hasNext();){
			    
				try{
					cur=(Element)it.next();
				}catch (Exception ex){cur=null;}
				
				if(cur!=null){

					 outline=frame.getOutline(cur);
					area.add(new Area(outline));

				    //removes this node from the children nodes
				    parent.removeChild(cur);
				    frame.unregisterAllUsedResource(cur);
				}
			}
			
			//creating the path
			ExtendedGeneralPath exPath=new ExtendedGeneralPath(area);

			if(exPath!=null){
			    
				LinkedHashMap map=new LinkedHashMap();
				LinkedList nlist=null;
				char cmd=' ';
				double[] values=new double[7], vals=new double[7];
				int type=-1, rg=0;
				
				//for each command in the path, the command and its values are added to the map
				for(ExtendedPathIterator pit=exPath.getExtendedPathIterator(); ! pit.isDone(); pit.next()){
				    
					type=pit.currentSegment(vals);
					
					if(type==ExtendedPathIterator.SEG_CLOSE){
					    
						values=null;
						cmd='Z';
						
					}else if(type==ExtendedPathIterator.SEG_CUBICTO){
					    
						values=new double[6];
						pit.currentSegment(values);
						cmd='C';
						
					}else if(type==ExtendedPathIterator.SEG_LINETO){
					    
						values=new double[2];
						pit.currentSegment(values);
						cmd='L';
						
					}else if(type==ExtendedPathIterator.SEG_MOVETO){
					    
						values=new double[2];
						pit.currentSegment(values);
						cmd='M';
						
					}else if(type==ExtendedPathIterator.SEG_QUADTO){
					    
						values=new double[4];
						pit.currentSegment(values);
						cmd='Q';
						
					}else if(type==ExtendedPathIterator.SEG_ARCTO){
					    
						values=new double[7];
						pit.currentSegment(values);
						cmd='A';
						
					}else{
					    
					    cmd=' ';
					    values=null;
					}
					
					//adding the current values to the map
					if(values!=null){
					    
						nlist=new LinkedList();
						
						for(int i=0;i<values.length;i++){
						    
							nlist.add(new Double(values[i]));
						}
						
						map.put(cmd+new Integer(rg++).toString(), nlist);
					}
				}
				
				//creates the path element
				Element path=doc.createElementNS(doc.getDocumentElement().getNamespaceURI(),"path"); //$NON-NLS-1$
				
				//converting the map of double values into point values
				LinkedHashMap map2=getSVGEditor().getSVGToolkit().convertPathValues(map);
				
				getSVGEditor().getSVGToolkit().setPathSeg(path, map2);
				String colorString=getSVGEditor().getColorChooser().getColorString(getSVGEditor().getSVGColorManager().getCurrentColor());
				path.setAttributeNS(null, "style", "fill:".concat(colorString.concat(";"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
				//sets that the svg has been modified
				frame.setModified(true);
			
				//creates final variables
				final Node fpath=path;
				final LinkedList newchildren=new LinkedList();
				final SVGFrame fframe=frame;

				//appends the path to the root node
				parent.appendChild(fpath);
				
				Node node=null;
				ch=parent.getChildNodes();
				
				//the newlist of the children of the root element
				for(int i=0;i<ch.getLength();i++){

					newchildren.add(ch.item(i));
				}
			
				//create the undo/redo action and insert it into the undo/redo stack
				if(getSVGEditor().getUndoRedo()!=null){

					SVGUndoRedoAction action=new SVGUndoRedoAction((String)labels.get("undoredounion")){ //$NON-NLS-1$

						public void undo(){
						    
						    Node node=null;
						    
							//removes all the nodes from the root element
							while(parent.hasChildNodes()){
							    
							    node=parent.getFirstChild();
							    
							    parent.removeChild(node);
							    fframe.unregisterAllUsedResource(node);
							}
						
							//appends all the old children	
							for(int i=0;i<oldchildren.size();i++){
							    
								try{
								    node=(Node)oldchildren.get(i);
								}catch(Exception ex){node=null;}
								
								if(node!=null){
								    
									parent.appendChild(node);
									fframe.registerUsedResource(node);
								}
							}
						}

						public void redo(){
						    
						    Node node=null;
						    
							//removes all the nodes from the root element
							while(parent.hasChildNodes()){
							    
							    node=parent.getFirstChild();
							    
							    parent.removeChild(node);
							    fframe.unregisterAllUsedResource(node);
							}
						
							//appends all the new children	
							for(int i=0;i<newchildren.size();i++){
							    
								try{
								    node=(Node)newchildren.get(i);
								}catch(Exception ex){node=null;}
								
								if(node!=null){
								    
									parent.appendChild(node);
									fframe.registerUsedResource(node);
								}
							}
						}
					};
				
					//adds the undo/redo actions into the stack
					SVGSelection selection=getSVGEditor().getSVGSelection();
				
					if(selection!=null){
					    
						selection.deselectAll(frame, false, false);
						selection.addUndoRedoAction(frame, action);
						selection.handleNodeSelection(frame, path);
						selection.addUndoRedoAction(frame, new SVGUndoRedoAction((String)labels.get("undoredounion")){}); //$NON-NLS-1$
						selection.refreshSelection(frame);
					
					}else{
					    
						SVGUndoRedoActionList actionList=new SVGUndoRedoActionList((String)labels.get("undoredounion")); //$NON-NLS-1$
						actionList.add(action);
						getSVGEditor().getUndoRedo().addActionList(frame, actionList);
					}
				}
			}
		}
	}
	
	/**
	 * converts the nodes of the given list into a path node by making a subtraction
	 * @param frame the current SVGFrame
	 * @param nodes the list of the nodes to be converted
	 */
	protected void subtraction(SVGFrame frame, LinkedList nodes){
		
		Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
		LinkedList snodes=new LinkedList(nodes);
		
	    //getting the parent element
	    Element p=null;
	    
	    try{
	        p=(Element)((Element)snodes.getFirst()).getParentNode();
	    }catch(Exception ex){p=null;}
	    
	    final Element parent=p;
						
		if(doc!=null && parent!=null && nodes!=null && nodes.size()==2){
			
			final LinkedList oldchildren=new LinkedList();
			NodeList ch=parent.getChildNodes();
			
			//the list of the children of the parent element
			for(int i=0;i<ch.getLength();i++){

				oldchildren.add(ch.item(i));
			}

			Element cur=null;
			Area area=null;
			Shape shape=null;
			
			for(Iterator it=nodes.iterator(); it.hasNext();){
			    
				try{cur=(Element)it.next();}catch (Exception ex){cur=null;}
				
				if(cur!=null){

					shape=frame.getOutline(cur);

					try{
						//creates the area (shape) that will be used to make the union
						if(area==null){
						    
						    area=new Area(shape);
						    
						}else{
						    
							//adds the current shape to the area
							area.subtract(new Area(shape));
						}
					}catch (Exception ex){}
					final Element fcur=cur;
					
					parent.removeChild(fcur);
					frame.unregisterAllUsedResource(fcur);
				}
			}
			
			if(area!=null){
			    
				//creates a path that allows to creates the string value of the path element
				ExtendedGeneralPath gpath=new ExtendedGeneralPath(area);
				LinkedHashMap map=new LinkedHashMap();
				LinkedList nlist=null;
				
				char cmd=' ';
				double[] values=new double[7], vals=new double[7];
				int type=-1, rg=0, i;
				
				//for each command in the path, the command and its values are added to the map
				for(ExtendedPathIterator pit=gpath.getExtendedPathIterator(); ! pit.isDone(); pit.next()){
				    
					type=pit.currentSegment(vals);
					
					if(type==ExtendedPathIterator.SEG_CLOSE){
					    
						values=null;
						cmd='Z';
						
					}else if(type==ExtendedPathIterator.SEG_CUBICTO){
					    
						values=new double[6];
						pit.currentSegment(values);
						cmd='C';
						
					}else if(type==ExtendedPathIterator.SEG_LINETO){
					    
						values=new double[2];
						pit.currentSegment(values);
						cmd='L';
						
					}else if(type==ExtendedPathIterator.SEG_MOVETO){
					    
						values=new double[2];
						pit.currentSegment(values);
						cmd='M';
						
					}else if(type==ExtendedPathIterator.SEG_QUADTO){
					    
						values=new double[4];
						pit.currentSegment(values);
						cmd='Q';
						
					}else if(type==ExtendedPathIterator.SEG_ARCTO){
					    
						values=new double[7];
						pit.currentSegment(values);
						cmd='A';
						
					}else{
					    
					    cmd=' ';
					    values=null;
					}
					
					//adding the current values to the map
					if(values!=null){
					    
						nlist=new LinkedList();
						
						for(i=0;i<values.length;i++){
						    
							nlist.add(new Double(values[i]));
						}
						
						map.put(cmd+new Integer(rg++).toString(), nlist);
					}
				}
				
				//creates the path element
				final Element path=doc.createElementNS(doc.getDocumentElement().getNamespaceURI(),"path"); //$NON-NLS-1$
				
				//converts the map to pass to accurate argument for setting the d attribute
				LinkedHashMap map2=getSVGEditor().getSVGToolkit().convertPathValues(map);
				getSVGEditor().getSVGToolkit().setPathSeg(path, map2);
				String colorString=getSVGEditor().getColorChooser().getColorString(getSVGEditor().getSVGColorManager().getCurrentColor());
				path.setAttributeNS(null, "style", "fill:".concat(colorString.concat(";"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
				//attaches the path to the svg parent element
				parent.appendChild(path);

				//sets that the svg has been modified
				frame.setModified(true);
			
				//creates final variables
				final Node fpath=path;
				final LinkedList newchildren=new LinkedList();
				final SVGFrame fframe=frame;
			
				Node node=null;
				ch=parent.getChildNodes();
				
				//the newlist of the children of the parent element
				for(i=0;i<ch.getLength();i++){
				    
				    newchildren.add(ch.item(i));
				}
			
				//create the undo/redo action and insert it into the undo/redo stack
				if(getSVGEditor().getUndoRedo()!=null){
					
					SVGUndoRedoAction action=new SVGUndoRedoAction((String)labels.get("undoredosubtraction")){ //$NON-NLS-1$

						public void undo(){
							
						    Node node=null;
						    
							//removes all the nodes from the parent element
							while(parent.hasChildNodes()){
							    
							    node=parent.getFirstChild();
							    
							    parent.removeChild(node);
							    fframe.unregisterAllUsedResource(node);
							}
						
							//appends all the old children	
							for(int i=0;i<oldchildren.size();i++){
							    
								try{
								    node=(Node)oldchildren.get(i);
								}catch(Exception ex){node=null;}
								
								if(node!=null){
								    
									parent.appendChild(node);
									fframe.registerUsedResource(node);
								}
							}
						}
						
						/**
						 * used to call all the actions that have to be done to redo an action
						 */
						public void redo(){
						    
						    Node node=null;
						    
							//removes all the nodes from the parent element
							while(parent.hasChildNodes()){
							    
							    node=parent.getFirstChild();
							    
							    parent.removeChild(node);
							    fframe.unregisterAllUsedResource(node);
							}
						
							//appends all the new children	
							for(int i=0;i<newchildren.size();i++){
							    
								try{
								    node=(Node)newchildren.get(i);
								}catch(Exception ex){node=null;}
								
								if(node!=null){
								    
								    parent.appendChild(node);
								    fframe.registerUsedResource(node);
								}
							}
						}
					};
				
					//adds the undo/redo actions into the stack
					SVGSelection selection=getSVGEditor().getSVGSelection();
				
					if(selection!=null){
					    
						selection.deselectAll(frame, false, false);
						selection.addUndoRedoAction(frame, action);
						selection.handleNodeSelection(frame, path);
						selection.addUndoRedoAction(frame, new SVGUndoRedoAction((String)labels.get("undoredosubtraction")){}); //$NON-NLS-1$
						selection.refreshSelection(frame);
					
					}else{
					    
						SVGUndoRedoActionList actionList=new SVGUndoRedoActionList((String)labels.get("undoredosubtraction")); //$NON-NLS-1$
						actionList.add(action);
						getSVGEditor().getUndoRedo().addActionList(frame, actionList);
					}
				}
			}
		}
	}
	
	/**
	 * converts the nodes of the given list into a path node by making an intersection
	 * @param frame the current SVGFrame
	 * @param nodes the list of the nodes to be converted
	 */
	protected void intersection(SVGFrame frame, LinkedList nodes){
		
		Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
		LinkedList snodes=new LinkedList(nodes);
		
	    //getting the parent element
	    Element p=null;
	    
	    try{
	        p=(Element)((Element)snodes.getFirst()).getParentNode();
	    }catch(Exception ex){p=null;}
	    
	    final Element parent=p;
						
		if(doc!=null && parent!=null && nodes!=null && nodes.size()>0){
			
			final LinkedList oldchildren=new LinkedList();
			NodeList ch=parent.getChildNodes();

			//the list of the children of the parent element
			for(int i=0;i<ch.getLength();i++){

				oldchildren.add(ch.item(i));
			}
			
			Shape shape=null;
			Element cur=null;
			Area area=null;
			
			for(Iterator it=nodes.iterator(); it.hasNext();){
			    
				try{cur=(Element)it.next();}catch (Exception ex){cur=null;}
				
				if(cur!=null){

				    shape=frame.getOutline(cur);

					try{
						//creates the area (shape) that will be used to make the union
						if(area==null){
						    
						    area=new Area(shape);
						    
						}else{
						    
							//adds the current shape to the area
							area.intersect(new Area(shape));
						}
					}catch (Exception ex){}
					
					parent.removeChild(cur);
					frame.unregisterAllUsedResource(cur);
				}
			}
			
			if(area!=null){
			    
				//creates a path that allows to creates the string value of the path element
				ExtendedGeneralPath gpath=new ExtendedGeneralPath(area);
				LinkedHashMap map=new LinkedHashMap();
				LinkedList nlist=null;
				char cmd=' ';
				double[] values=new double[7], vals=new double[7];
				int type=-1, rg=0, i;
				
				//for each command in the path, the command and its values are added to the map
				for(ExtendedPathIterator pit=gpath.getExtendedPathIterator(); ! pit.isDone(); pit.next()){
				    
					type=pit.currentSegment(vals);
					
					if(type==ExtendedPathIterator.SEG_CLOSE){
					    
						values=null;
						cmd='Z';
						
					}else if(type==ExtendedPathIterator.SEG_CUBICTO){
					    
						values=new double[6];
						pit.currentSegment(values);
						cmd='C';
						
					}else if(type==ExtendedPathIterator.SEG_LINETO){
					    
						values=new double[2];
						pit.currentSegment(values);
						cmd='L';
						
					}else if(type==ExtendedPathIterator.SEG_MOVETO){
					    
						values=new double[2];
						pit.currentSegment(values);
						cmd='M';
						
					}else if(type==ExtendedPathIterator.SEG_QUADTO){
					    
						values=new double[4];
						pit.currentSegment(values);
						cmd='Q';
						
					}else if(type==ExtendedPathIterator.SEG_ARCTO){
					    
						values=new double[7];
						pit.currentSegment(values);
						cmd='A';
						
					}else{
					    
					    cmd=' ';
					    values=null;
					}
					
					//adding the current values to the map
					if(values!=null){
					    
						nlist=new LinkedList();
						
						for(i=0;i<values.length;i++){
						    
							nlist.add(new Double(values[i]));
						}
						
						map.put(cmd+new Integer(rg++).toString(), nlist);
					}
				}
				
				//creates the path element
				final Element path=doc.createElementNS(doc.getDocumentElement().getNamespaceURI(),"path"); //$NON-NLS-1$
				
				//converts the map to pass to accurate argument for setting the d attribute
				LinkedHashMap map2=getSVGEditor().getSVGToolkit().convertPathValues(map);
				getSVGEditor().getSVGToolkit().setPathSeg(path, map2);
				String colorString=getSVGEditor().getColorChooser().getColorString(getSVGEditor().getSVGColorManager().getCurrentColor());
				path.setAttributeNS(null, "style", "fill:".concat(colorString.concat(";"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
				//attaches the path to the svg root element	
				parent.appendChild(path);
				
				//sets that the svg has been modified
				frame.setModified(true);
			
				//creates final variables
				final Node fpath=path;
				final LinkedList newchildren=new LinkedList();
				final SVGFrame fframe=frame;
			
				Node node=null;
				ch=parent.getChildNodes();
				
				//the newlist of the children of the root element
				for(i=0;i<ch.getLength();i++){
				    
					newchildren.add(ch.item(i));
				}

				//create the undo/redo action and insert it into the undo/redo stack
				if(getSVGEditor().getUndoRedo()!=null){
					
					SVGUndoRedoAction action=new SVGUndoRedoAction((String)labels.get("undoredointersection")){ //$NON-NLS-1$

						public void undo(){
						    
						    Node node=null;
						    
							//removes all the nodes from the root element
							while(parent.hasChildNodes()){
							    
							    node=parent.getFirstChild();
							    
							    parent.removeChild(node);
							    fframe.unregisterAllUsedResource(node);
							}
						
							//appends all the old children	
							for(int i=0;i<oldchildren.size();i++){
							    
								try{
								    node=(Node)oldchildren.get(i);
								}catch(Exception ex){node=null;}
								
								if(node!=null){
								    
								    parent.appendChild(node);
								    fframe.registerUsedResource(node);
								}
							}
						}

						public void redo(){
						    
						    Node node=null;
						    
							//removes all the nodes from the root element
							while(parent.hasChildNodes()){
							    
							    node=parent.getFirstChild();
							    
							    parent.removeChild(node);
							    fframe.unregisterAllUsedResource(node);
							}
						
							//appends all the new children	
							for(int i=0;i<newchildren.size();i++){
							    
								try{
								    node=(Node)newchildren.get(i);
								}catch(Exception ex){node=null;}
								
								if(node!=null){
								    
								    parent.appendChild(node);
								    fframe.registerUsedResource(node);
								}
							}
						}
					};
				
					//adds the undo/redo actions into the stack
					SVGSelection selection=getSVGEditor().getSVGSelection();
				
					if(selection!=null){
					    
						selection.deselectAll(frame, false, false);
						selection.addUndoRedoAction(frame, action);
						selection.handleNodeSelection(frame, path);
						selection.addUndoRedoAction(frame, new SVGUndoRedoAction((String)labels.get("undoredointersection")){}); //$NON-NLS-1$
						selection.refreshSelection(frame);
					
					}else{
					    
						SVGUndoRedoActionList actionList=new SVGUndoRedoActionList((String)labels.get("undoredointersection")); //$NON-NLS-1$
						actionList.add(action);
						getSVGEditor().getUndoRedo().addActionList(frame, actionList);
					}
				}
			}
		}
	}

	/**
	 * gets the nexts level after the given selection level
	 * @param type a selection level
	 * @return the next selection level
	 */
	public String getNextLevel(String type){
	    
		if(type!=null){
		    
			if(type.equals("level1")){ //$NON-NLS-1$
			    
			    return "level2"; //$NON-NLS-1$
			    
			}else if(type.equals("level2")){ //$NON-NLS-1$
			    
			    return "level3"; //$NON-NLS-1$
			    
			}else if(type.equals("level3")){ //$NON-NLS-1$
			    
			    return "level1"; //$NON-NLS-1$
			}
		}
		
		return "level1"; //$NON-NLS-1$
	}

	/**
	 * draws the selection for the path
	 * @param frame the current SVGFrame
	 * @param graphics the graphics 
	 * @param node the node to be selected
	 * @return the list of the selection squares
	 */
	protected LinkedList drawModifyPointsSelection(SVGFrame frame, Graphics graphics, Node node){

		LinkedList squarelist=new LinkedList();
		Graphics2D g=(Graphics2D)graphics;
		
		if(frame!=null && g!=null && node!=null){
			
			int sqd=5;
			final Element elt=(Element)node;
					    
		    //getting the path that will be analysed
		    ExtendedGeneralPath path=getSVGEditor().getSVGToolkit().getGeneralPath(elt);

			//the cursor associated with the selection points
			Cursor cursor=new Cursor(Cursor.HAND_CURSOR);
			Shape shape=null;
			int type=-1;
			double[] values=new double[7];
			int index=0, sqx=0, sqy=0;
			Point2D.Double scPoint=null, scCtrlPoint=null, scCtrlPoint2=null, lastScPoint=new Point2D.Double(0, 0);
			GradientPaint gradient=null;

			//draws the selection
			for(ExtendedPathIterator pit=path.getExtendedPathIterator(); ! pit.isDone(); pit.next()){

				type=pit.currentSegment(values);
				
				if(type==ExtendedPathIterator.SEG_CLOSE){

					
				}else if(type==ExtendedPathIterator.SEG_CUBICTO){
				    
					scCtrlPoint=frame.getScaledPoint(new Point2D.Double(values[0], values[1]), false);
					scCtrlPoint2=frame.getScaledPoint(new Point2D.Double(values[2], values[3]), false);
					scPoint=frame.getScaledPoint(new Point2D.Double(values[4], values[5]), false);

					g.setColor(LINE_SELECTION_COLOR);
					g.drawLine((int)lastScPoint.x, (int)lastScPoint.y, (int)scCtrlPoint.x, (int)scCtrlPoint.y);
				
					sqx=(int)(scCtrlPoint.x-sqd);
					sqy=(int)(scCtrlPoint.y-sqd);
				
					if(getSVGEditor().getSVGSelection()!=null){
					    
					    squarelist.add(new SVGSelectionSquare(node,"Cctrl"+new Integer(index++).toString(),new Rectangle2D.Double(sqx, sqy, 2*sqd, 2*sqd),cursor)); //$NON-NLS-1$
					}
						
					shape=getArrow(new Point2D.Double(sqx+sqd, sqy+sqd), "Ctrl", REGULAR_SELECTION); //$NON-NLS-1$

					if(shape!=null){
					    
						gradient=new GradientPaint(sqx, sqy, SQUARE_SELECTION_COLOR1, sqx+2*sqd, sqy+2*sqd, SQUARE_SELECTION_COLOR2, true);
						g.setPaint(gradient);
						g.fill(shape);
						g.setColor(LINE_SELECTION_COLOR);
						g.draw(shape);
					}
					
					sqx=(int)(scCtrlPoint2.x-sqd);
					sqy=(int)(scCtrlPoint2.y-sqd);
					
					if(getSVGEditor().getSVGSelection()!=null){
					    
					    squarelist.add(new SVGSelectionSquare(node,"Cctrl"+new Integer(index++).toString(),new Rectangle2D.Double(sqx, sqy, 2*sqd, 2*sqd),cursor)); //$NON-NLS-1$
					}

					shape=getArrow(new Point2D.Double(sqx+sqd, sqy+sqd), "Ctrl", REGULAR_SELECTION); //$NON-NLS-1$

					if(shape!=null){
					    
						gradient=new GradientPaint(sqx, sqy, SQUARE_SELECTION_COLOR1, sqx+2*sqd, sqy+2*sqd, SQUARE_SELECTION_COLOR2, true);
						g.setPaint(gradient);
						g.fill(shape);
						g.setColor(LINE_SELECTION_COLOR);
						g.draw(shape);
					}
					
					g.setColor(LINE_SELECTION_COLOR);
					g.drawLine((int)scCtrlPoint2.x, (int)scCtrlPoint2.y, (int)scPoint.x, (int)scPoint.y);
					
					sqx=(int)(scPoint.x-sqd);
					sqy=(int)(scPoint.y-sqd);

					if(getSVGEditor().getSVGSelection()!=null){
					    
					    squarelist.add(new SVGSelectionSquare(node,"C"+new Integer(index++).toString(),new Rectangle2D.Double(sqx, sqy, 2*sqd, 2*sqd),cursor)); //$NON-NLS-1$
					}

					shape=getArrow(new Point2D.Double(sqx+sqd, sqy+sqd), "P", REGULAR_SELECTION); //$NON-NLS-1$

					if(shape!=null){
					    
						gradient=new GradientPaint(sqx, sqy, SQUARE_SELECTION_COLOR1, sqx+2*sqd, sqy+2*sqd, SQUARE_SELECTION_COLOR2, true);
						g.setPaint(gradient);
						g.fill(shape);
						g.setColor(LINE_SELECTION_COLOR);
						g.draw(shape);
					}
					
					lastScPoint=scPoint;

				}else if(type==ExtendedPathIterator.SEG_LINETO){
					
					scPoint=frame.getScaledPoint(new Point2D.Double(values[0], values[1]), false);
					lastScPoint=scPoint;
					
					sqx=(int)(scPoint.x-sqd);
					sqy=(int)(scPoint.y-sqd);

					if(getSVGEditor().getSVGSelection()!=null){
					    
					    squarelist.add(new SVGSelectionSquare(node, "L"+new Integer(index++).toString(),  //$NON-NLS-1$
					            				new Rectangle2D.Double(sqx, sqy, 2*sqd, 2*sqd), cursor));
					}

					shape=getArrow(new Point2D.Double(sqx+sqd, sqy+sqd), "P", REGULAR_SELECTION); //$NON-NLS-1$

					if(shape!=null){
					    
						gradient=new GradientPaint(sqx, sqy, SQUARE_SELECTION_COLOR1, sqx+2*sqd, sqy+2*sqd, SQUARE_SELECTION_COLOR2, true);
						g.setPaint(gradient);
						g.fill(shape);
						g.setColor(LINE_SELECTION_COLOR);
						g.draw(shape);
					}
					
				}else if(type==ExtendedPathIterator.SEG_ARCTO){
					
					scPoint=frame.getScaledPoint(new Point2D.Double(values[5], values[6]), false);
					lastScPoint=scPoint;
					
					sqx=(int)(scPoint.x-sqd);
					sqy=(int)(scPoint.y-sqd);

					if(getSVGEditor().getSVGSelection()!=null){
					    
					    squarelist.add(new SVGSelectionSquare(node, "A"+new Integer(index++).toString(),  //$NON-NLS-1$
					            				new Rectangle2D.Double(sqx, sqy, 2*sqd, 2*sqd), cursor));
					}

					shape=getArrow(new Point2D.Double(sqx+sqd, sqy+sqd), "P", REGULAR_SELECTION); //$NON-NLS-1$

					if(shape!=null){
					    
						gradient=new GradientPaint(sqx, sqy, SQUARE_SELECTION_COLOR1, sqx+2*sqd, sqy+2*sqd, SQUARE_SELECTION_COLOR2, true);
						g.setPaint(gradient);
						g.fill(shape);
						g.setColor(LINE_SELECTION_COLOR);
						g.draw(shape);
					}
					
				}else if(type==ExtendedPathIterator.SEG_MOVETO){

					scPoint=frame.getScaledPoint(new Point2D.Double(values[0], values[1]), false);
					lastScPoint=scPoint;
					
					sqx=(int)(scPoint.x-sqd);
					sqy=(int)(scPoint.y-sqd);

					if(getSVGEditor().getSVGSelection()!=null){
					    
					    squarelist.add(new SVGSelectionSquare(node, "M"+new Integer(index++).toString(),  //$NON-NLS-1$
					            				new Rectangle2D.Double(sqx, sqy, 2*sqd, 2*sqd), cursor));
					}

					shape=getArrow(new Point2D.Double(sqx+sqd, sqy+sqd), "P", REGULAR_SELECTION); //$NON-NLS-1$

					if(shape!=null){
					    
						gradient=new GradientPaint(sqx, sqy, SQUARE_SELECTION_COLOR1, sqx+2*sqd, sqy+2*sqd, SQUARE_SELECTION_COLOR2, true);
						g.setPaint(gradient);
						g.fill(shape);
						g.setColor(LINE_SELECTION_COLOR);
						g.draw(shape);
					}

				}else if(type==ExtendedPathIterator.SEG_QUADTO){

					scCtrlPoint=frame.getScaledPoint(new Point2D.Double(values[0], values[1]), false);
					scPoint=frame.getScaledPoint(new Point2D.Double(values[2], values[3]), false);

					g.setColor(LINE_SELECTION_COLOR);
					g.drawLine((int)lastScPoint.x, (int)lastScPoint.y, (int)scCtrlPoint.x, (int)scCtrlPoint.y);
					
					sqx=(int)(scCtrlPoint.x-sqd);
					sqy=(int)(scCtrlPoint.y-sqd);
					
					if(getSVGEditor().getSVGSelection()!=null){
					    
					    squarelist.add(new SVGSelectionSquare(node,"Qctrl"+new Integer(index++).toString(),new Rectangle2D.Double(sqx, sqy, 2*sqd, 2*sqd),cursor)); //$NON-NLS-1$
					}

					shape=getArrow(new Point2D.Double(sqx+sqd, sqy+sqd), "Ctrl", REGULAR_SELECTION); //$NON-NLS-1$

					if(shape!=null){
					    
						gradient=new GradientPaint(sqx, sqy, SQUARE_SELECTION_COLOR1, sqx+2*sqd, sqy+2*sqd, SQUARE_SELECTION_COLOR2, true);
						g.setPaint(gradient);
						g.fill(shape);
						g.setColor(LINE_SELECTION_COLOR);
						g.draw(shape);
					}
					
					sqx=(int)(scPoint.x-sqd);
					sqy=(int)(scPoint.y-sqd);

					if(getSVGEditor().getSVGSelection()!=null){
					    
					    squarelist.add(new SVGSelectionSquare(node,"Q"+new Integer(index++).toString(),new Rectangle2D.Double(sqx, sqy, 2*sqd, 2*sqd),cursor)); //$NON-NLS-1$
                    }

					shape=getArrow(new Point2D.Double(sqx+sqd, sqy+sqd), "P", REGULAR_SELECTION); //$NON-NLS-1$

					if(shape!=null){
					    
						gradient=new GradientPaint(sqx, sqy, SQUARE_SELECTION_COLOR1, sqx+2*sqd, sqy+2*sqd, SQUARE_SELECTION_COLOR2, true);
						g.setPaint(gradient);
						g.fill(shape);
						g.setColor(LINE_SELECTION_COLOR);
						g.draw(shape);
					}
					
					lastScPoint=scPoint;
				}
			}
		}	

		return squarelist;
	}
	
	/**
	 * the method to modify a point of a node
	 * @param frame the current SVGFrame
	 * @param square the selection square
	 * @param point1 the first point clicked
	 * @param point2 the second point clicked
	 */
	public void modifyPoint(SVGFrame frame, SVGSelectionSquare square, Point2D.Double point1, Point2D.Double point2){
		
		if(frame!=null && square!=null && square.getNode()!=null && point2!=null){

			//gets the paintlistener associated with the frame
			CanvasPaintListener paintListener=null;
			
			try{
				paintListener=(CanvasPaintListener)modifyPointFrameTable.get(frame);
			}catch (Exception ex){paintListener=null;}

			final Element elt=(Element)square.getNode();
			    
		    //the new path
		    ExtendedGeneralPath newPath=new ExtendedGeneralPath(), tmpPath=null;
					    
		    //getting the path that will be analysed
		    tmpPath=getSVGEditor().getSVGToolkit().getGeneralPath(elt);

			int type=-1;
			double[] values=new double[7];
			
			int i=0, index=-1;
			
			//gets the index of the point that has to be modified 
			try{
				//if the point is a control point
				if(square.getType().indexOf("ctrl")==-1){ //$NON-NLS-1$
				    
					index=new Integer(square.getType().substring(1,square.getType().length())).intValue();
					
				}else{
				    
					index=new Integer(square.getType().substring(square.getType().indexOf("ctrl")+4, square.getType().length())).intValue(); //$NON-NLS-1$
				}
			}catch(Exception ex){}

			//for each command in the path, the command and its values are added to the string value
			for(ExtendedPathIterator pit=tmpPath.getExtendedPathIterator(); ! pit.isDone(); pit.next()){

				type=pit.currentSegment(values);
				
				if(type==ExtendedPathIterator.SEG_CLOSE){
				    
				    newPath.closePath();
					
				}else if(type==ExtendedPathIterator.SEG_CUBICTO){

					if(index==i){
					    
					    newPath.curveTo((float)point2.x, (float)point2.y, (float)values[2], (float)values[3], (float)values[4], (float)values[5]);
					    
					}else if(index==i+1){
					    
					    newPath.curveTo((float)values[0], (float)values[1], (float)point2.x, (float)point2.y, (float)values[4], (float)values[5]);
					    
					}else if(index==i+2){
					    
					    newPath.curveTo((float)values[0], (float)values[1], (float)values[2], (float)values[3], (float)point2.x, (float)point2.y);
					
					}else{
					    
					    newPath.curveTo((float)values[0], (float)values[1], (float)values[2], (float)values[3], (float)values[4], (float)values[5]);
					}
					
					i+=3;

				}else if(type==ExtendedPathIterator.SEG_LINETO){

					if(index==i){
					    
					    newPath.lineTo((float)point2.x, (float)point2.y);
					    
					}else{
					    
					    newPath.lineTo((float)values[0], (float)values[1]);
					}
					
					i++;
					
				}else if(type==ExtendedPathIterator.SEG_ARCTO){

					if(index==i){
					    
					    newPath.arcTo((float)values[0], (float)values[1], (float)values[2], values[3]==0?false:true, values[4]==0?false:true, (float)point2.x, (float)point2.y);
					    
					}else{
					    
					    newPath.arcTo((float)values[0], (float)values[1], (float)values[2], values[3]==0?false:true, values[4]==0?false:true, (float)values[5], (float)values[6]);
					}
					
					i++;
					
				}else if(type==ExtendedPathIterator.SEG_MOVETO){

					if(index==i){
					    
					    newPath.moveTo((float)point2.x, (float)point2.y);
					    
					}else{
					    
					    newPath.moveTo((float)values[0], (float)values[1]);
					}
					
					i++;
					
				}else if(type==ExtendedPathIterator.SEG_QUADTO){

					if(index==i){
					    
					    newPath.quadTo((float)point2.x, (float)point2.y, (float)values[2], (float)values[3]);
					    
					}else if(index==i+1){
					    
					    newPath.quadTo((float)values[0], (float)values[1], (float)point2.x, (float)point2.y);

					}else{
					    
					    newPath.quadTo((float)values[0], (float)values[1], (float)values[2], (float)values[3]);
					}
					
					i+=2;
				}
			}
			
			//computes the scale and translate values
			if(newPath!=null){
			    
				//concatenates the transforms to draw the outline
				//concatenates the transforms to draw the outline
				AffineTransform af=new AffineTransform();
				
				try{af.preConcatenate(frame.getScrollPane().getSVGCanvas().getViewingTransform());}catch (Exception ex){}
				try{af.preConcatenate(frame.getScrollPane().getSVGCanvas().getRenderingTransform());}catch (Exception ex){}

				//computing the outline
				Shape outline=af.createTransformedShape(newPath);

				final Shape foutline=outline;
				
				//removes the paint listener
				if(paintListener!=null){
				    
					frame.getScrollPane().getSVGCanvas().removePaintListener(paintListener, false);
				}
			
				//creates and sets the paint listener
				paintListener=new CanvasPaintListener(){
				    
					public void paintToBeDone(Graphics g) {

						Graphics2D g2=(Graphics2D)g;
						
						fillShape(g2, foutline);
					}
				};
				
			   frame.getScrollPane().getSVGCanvas().addLayerPaintListener(SVGCanvas.DRAW_LAYER, paintListener, true);
			   modifyPointFrameTable.put(frame, paintListener);
			}
		}
	}
	
	/**
	 * validates the modifyPoint method
	 * @param frame the current SVGFrame
	 * @param square the selection square
	 * @param point1 the first point clicked
	 * @param point2 the second point clicked
	 */
	public void validateModifyPoint(SVGFrame frame, SVGSelectionSquare square, Point2D.Double point1, Point2D.Double point2){
		
		if(frame!=null && square!=null && square.getNode()!=null && point2!=null){

			//gets the paintlistener associated with the frame
			CanvasPaintListener paintListener=null;
			SVGFrame f=null;
			
			for(Iterator it=new LinkedList(modifyPointFrameTable.keySet()).iterator(); it.hasNext();){
			    
				try{
				    f=(SVGFrame)it.next();
					paintListener=(CanvasPaintListener)modifyPointFrameTable.get(f);
				}catch (Exception ex){paintListener=null;}
				
				if(paintListener!=null){
				    
				    modifyPointFrameTable.remove(frame);
					frame.getScrollPane().getSVGCanvas().removePaintListener(paintListener, false);
				}  
			}
			
			final Element elt=(Element)square.getNode();
			    
		    //the new path
		    ExtendedGeneralPath newPath=new ExtendedGeneralPath(), tmpPath=null;
					    
		    //getting the path that will be analysed
		    tmpPath=getSVGEditor().getSVGToolkit().getGeneralPath(elt);

			int type=-1, i=0, index=-1;
			double[] values=new double[7];

			//gets the index of the point that has to be modified 
			try{
				//if the point is a control point
				if(square.getType().indexOf("ctrl")==-1){ //$NON-NLS-1$
				    
					index=new Integer(square.getType().substring(1,square.getType().length())).intValue();
					
				}else{
				    
					index=new Integer(square.getType().substring(square.getType().indexOf("ctrl")+4, square.getType().length())).intValue(); //$NON-NLS-1$
				}
			}catch(Exception ex){}

			//for each command in the path, the command and its values are added to the string value
			for(ExtendedPathIterator pit=tmpPath.getExtendedPathIterator(); ! pit.isDone(); pit.next()){

				type=pit.currentSegment(values);
				
				if(type==ExtendedPathIterator.SEG_CLOSE){
				    
				    newPath.closePath();
					
				}else if(type==ExtendedPathIterator.SEG_CUBICTO){

					if(index==i){
					    
					    newPath.curveTo((float)point2.x, (float)point2.y, (float)values[2], (float)values[3], (float)values[4], (float)values[5]);
					    
					}else if(index==i+1){
					    
					    newPath.curveTo((float)values[0], (float)values[1], (float)point2.x, (float)point2.y, (float)values[4], (float)values[5]);
					    
					}else if(index==i+2){
					    
					    newPath.curveTo((float)values[0], (float)values[1], (float)values[2], (float)values[3], (float)point2.x, (float)point2.y);
					
					}else{
					    
					    newPath.curveTo((float)values[0], (float)values[1], (float)values[2], (float)values[3], (float)values[4], (float)values[5]);
					}
					
					i+=3;

				}else if(type==ExtendedPathIterator.SEG_LINETO){

					if(index==i){
					    
					    newPath.lineTo((float)point2.x, (float)point2.y);
					    
					}else{
					    
					    newPath.lineTo((float)values[0], (float)values[1]);
					}
					
					i++;
					
				}else if(type==ExtendedPathIterator.SEG_ARCTO){

					if(index==i){
					    
					    newPath.arcTo((float)values[0], (float)values[1], (float)values[2], values[3]==0?false:true, values[4]==0?false:true, (float)point2.x, (float)point2.y);
					    
					}else{
					    
					    newPath.arcTo((float)values[0], (float)values[1], (float)values[2], values[3]==0?false:true, values[4]==0?false:true, (float)values[5], (float)values[6]);
					}
					
					i++;
					
				}else if(type==ExtendedPathIterator.SEG_MOVETO){

					if(index==i){
					    
					    newPath.moveTo((float)point2.x, (float)point2.y);
					    
					}else{
					    
					    newPath.moveTo((float)values[0], (float)values[1]);
					}
					
					i++;
					
				}else if(type==ExtendedPathIterator.SEG_QUADTO){

					if(index==i){
					    
					    newPath.quadTo((float)point2.x, (float)point2.y, (float)values[2], (float)values[3]);
					    
					}else if(index==i+1){
					    
					    newPath.quadTo((float)values[0], (float)values[1], (float)point2.x, (float)point2.y);

					}else{
					    
					    newPath.quadTo((float)values[0], (float)values[1], (float)values[2], (float)values[3]);
					}
					
					i+=2;
				}
			}
			
			if(newPath!=null){

				values=new double[7];
				String d=""; //$NON-NLS-1$
				
				//creating the new string value for the "d" attribute of the element
				for(ExtendedPathIterator pit=newPath.getExtendedPathIterator(); ! pit.isDone(); pit.next()){
				    
					type=pit.currentSegment(values);
					
					if(type==ExtendedPathIterator.SEG_CLOSE){
					    
						d=d.concat("Z "); //$NON-NLS-1$
						
					}else if(type==ExtendedPathIterator.SEG_CUBICTO){
					    
						pit.currentSegment(values);
						d=d.concat("C "+format.format(values[0])+" "+format.format(values[1])+" "+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						        			format.format(values[2])+" "+format.format(values[3])+" "+ //$NON-NLS-1$ //$NON-NLS-2$
						        			format.format(values[4])+" "+format.format(values[5])+" "); //$NON-NLS-1$ //$NON-NLS-2$

					}else if(type==ExtendedPathIterator.SEG_LINETO){

						pit.currentSegment(values);
						d=d.concat("L "+format.format(values[0])+" "+format.format(values[1])+" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						
					}else if(type==ExtendedPathIterator.SEG_ARCTO){

						pit.currentSegment(values);
						d=d.concat("A "+format.format(values[0])+" "+format.format(values[1])+" "+format.format(values[2])+" "+  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
						        			(values[3]==0?"0":"1")+" "+(values[4]==0?"0":"1")+" "+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
						        			format.format(values[5])+" "+format.format(values[6])); //$NON-NLS-1$
						
					}else if(type==ExtendedPathIterator.SEG_MOVETO){

						pit.currentSegment(values);
						d=d.concat("M "+format.format(values[0])+" "+format.format(values[1])+" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						
					}else if(type==ExtendedPathIterator.SEG_QUADTO){

						pit.currentSegment(values);
						d=d.concat("Q "+format.format(values[0])+" "+format.format(values[1])+" "+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			        						format.format(values[2])+" "+format.format(values[3])+" "); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				
				//getting the current matrix transform
				final SVGTransformMatrix matrix=getSVGEditor().getSVGToolkit().getTransformMatrix(elt);
				
				//getting the current node value
				final String oldD=elt.getAttributeNS(null, "d"); //$NON-NLS-1$
				
				//the new value for the node
				final String newD=d;
				
				//setting the new values for the node
				elt.setAttributeNS(null, "d", d); //$NON-NLS-1$
				getSVGEditor().getSVGToolkit().setTransformMatrix(elt, new SVGTransformMatrix(1, 0, 0, 1, 0, 0));

				//create the undo/redo action and insert it into the undo/redo stack
				if(getSVGEditor().getUndoRedo()!=null){
				    
					SVGUndoRedoAction action=new SVGUndoRedoAction((String)labels.get("undoredomodifypoint")){ //$NON-NLS-1$

						public void undo(){
						    
							if(matrix!=null && oldD!=null){
							    
							    elt.setAttributeNS(null, "d", oldD); //$NON-NLS-1$
							    getSVGEditor().getSVGToolkit().setTransformMatrix(elt, matrix);
							    
								//notifies that the selection has changed
								if(getSVGEditor().getSVGSelection()!=null){
									
									getSVGEditor().getSVGSelection().selectionChanged(true);
								}
							}
						}

						public void redo(){

							if(newD!=null){
							    
							    elt.setAttributeNS(null, "d", newD); //$NON-NLS-1$
							    getSVGEditor().getSVGToolkit().setTransformMatrix(elt, new SVGTransformMatrix(1, 0, 0, 1, 0, 0));
							    
								//notifies that the selection has changed
								if(getSVGEditor().getSVGSelection()!=null){
									
									getSVGEditor().getSVGSelection().selectionChanged(true);
								}
							}
						}
					};
					
					//gets or creates the undo/redo list and adds the action into it
					SVGUndoRedoActionList actionList=new SVGUndoRedoActionList((String)labels.get("undoredomodifypoint")); //$NON-NLS-1$
					actionList.add(action);
					
					getSVGEditor().getUndoRedo().addActionList(frame, actionList);
				}
			}
		}
	}
	
	/**
	 * used to remove the listener added to draw a path when the user clicks on the menu item
	 */
	public void cancelActions(){
	    
		if(pathActionQuadratic!=null){
		    
			quadraticTool.removeActionListener(pathActionQuadratic);
			quadraticTool.setSelected(false);
			quadraticTool.addActionListener(pathActionQuadratic);
		    pathActionQuadratic.cancelActions();
		}
		
		if(pathActionCubic!=null){
		    
			cubicTool.removeActionListener(pathActionCubic);
			cubicTool.setSelected(false);
			cubicTool.addActionListener(pathActionCubic);
		    pathActionCubic.cancelActions();
		}
	}
	
	/**
	 * the class allowing to get the position and size of the future drawn path 
	 * @author Jordi SUC
	 */
	protected class PathActionListener implements ActionListener{

		protected static final int QUADRATIC_BEZIER=0;
		
		protected static final int CUBIC_BEZIER=1;

		/**
		 * the hashtable associating a frame to its mouse adapter
		 */
		private final Hashtable mouseAdapterFrames=new Hashtable();
		
		/**
		 * an instance of this class
		 */
		private final PathActionListener action=this;
		
		/**
		 * the cursor used when creating a rectangle
		 */
		private Cursor createCursor;
		
		/**
		 * the type of the curve to be created
		 */
		private int type;
		
		private JToggleButton toggleButton;
		
		/**
		 * tells whether the menu item has been clicked or not
		 */
		private boolean isActive=false;
		
		/**
		 * the source component
		 */
		private Object source=null;
		
		/**
		 * the constructor of the class
		 * @param type the type of the listener (quadratic or cubic)
		 */
		protected PathActionListener(int type){
		    
			this.type=type;
			createCursor=getSVGEditor().getCursors().getCursor("path"); //$NON-NLS-1$
			
			if(type==QUADRATIC_BEZIER){
			    
			    toggleButton=quadraticTool;
			    
			}else if(type==CUBIC_BEZIER){
			    
			    toggleButton=cubicTool;
			}
		}
		
		/**
		 * resets the listener
		 */
		protected void reset(){
			
			if(isActive){
			    
				Collection frames=getSVGEditor().getFrameManager().getFrames();
			
				Iterator it;
				SVGFrame frm=null;
				LinkedList toBeRemoved=new LinkedList();
				Object mouseListener=null;
				
				//removes all the motion adapters from the frames
				for(it=mouseAdapterFrames.keySet().iterator(); it.hasNext();){
				    
					try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
			
					if(frm!=null && ! frames.contains(frm)){
					    
						try{
							mouseListener=mouseAdapterFrames.get(frm);
							frm.getScrollPane().getSVGCanvas().removeMouseListener((MouseAdapter)mouseListener);
							frm.getScrollPane().getSVGCanvas().removeMouseMotionListener((MouseMotionListener)mouseListener);

						}catch (Exception ex){}
						
						toBeRemoved.add(frm);
					}
				}
					
				//removes the frames that have been closed
				for(it=toBeRemoved.iterator(); it.hasNext();){
				    
					try{mouseAdapterFrames.remove(it.next());}catch (Exception ex){}
				}

				PathMouseListener pml=null;
				
				//adds the new motion adapters
				for(it=frames.iterator(); it.hasNext();){
				    
					try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
			
					if(frm!=null && ! mouseAdapterFrames.containsKey(frm)){
				
						pml=new PathMouseListener(frm);

						try{
							frm.getScrollPane().getSVGCanvas().addMouseListener(pml);
							frm.getScrollPane().getSVGCanvas().addMouseMotionListener(pml);
							frm.getScrollPane().getSVGCanvas().setSVGCursor(createCursor);
						}catch (Exception ex){}
						
						mouseAdapterFrames.put(frm, pml);
					}
				}				
			}
		}
		
		/**
		 * used to remove the listener added to draw a rectangle when the user clicks on the menu item
		 */	
		protected void cancelActions(){
			
			if(isActive){
				//removes the listeners
			
				Iterator it;
				SVGFrame frm=null;
				LinkedList toBeRemoved=new LinkedList();
				Object mouseListener=null;
				
				//removes all the motion adapters from the frames
				for(it=mouseAdapterFrames.keySet().iterator(); it.hasNext();){
				    
					try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
					
					if(frm!=null){
					    
						//resets the information displayed
						frm.getStateBar().setSVGW(""); //$NON-NLS-1$
						frm.getStateBar().setSVGH(""); //$NON-NLS-1$
						frm.getScrollPane().getSVGCanvas().setSVGCursor(frm.getSVGEditor().getCursors().getCursor("default")); //$NON-NLS-1$
						
						try{
							mouseListener=mouseAdapterFrames.get(frm);
							frm.getScrollPane().getSVGCanvas().removeMouseListener((MouseAdapter)mouseListener);
							frm.getScrollPane().getSVGCanvas().removeMouseMotionListener((MouseMotionListener)mouseListener);
							
							if(mouseListener!=null && ((PathMouseListener)mouseListener).paintListener!=null){
								
								//removes the paint listener
								frm.getScrollPane().getSVGCanvas().removePaintListener(((PathMouseListener)mouseListener).paintListener, true);
							}
						}catch (Exception ex){}
						
						toBeRemoved.add(frm);
					}
				}
			
				//removes the frames that have been closed
				for(it=toBeRemoved.iterator(); it.hasNext();){
				    
					try{mouseAdapterFrames.remove(it.next());}catch (Exception ex){}
				}
				
				isActive=false;	
			}
		}
		
		/**
		 * the action to be done
		 * @param e the event
		 */
		public void actionPerformed(ActionEvent e){

			if((e.getSource() instanceof JMenuItem && ! toggleButton.isSelected()) || (e.getSource() instanceof JToggleButton)){

				getSVGEditor().cancelActions(false);
			
				if(getSVGEditor().getFrameManager().getCurrentFrame()!=null){
					
					toggleButton.removeActionListener(this);
					toggleButton.setSelected(true);
					toggleButton.addActionListener(this);

					//the listener is active
					isActive=true;
					source=e.getSource();

					Collection frames=getSVGEditor().getFrameManager().getFrames();
					Iterator it;
					SVGFrame frm=null;
					PathMouseListener rml=null;
					    
					//adds the new motion adapters
					for(it=frames.iterator(); it.hasNext();){
					    
						try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
					
						if(frm!=null){
	
							rml=new PathMouseListener(frm);
	
							try{
								frm.getScrollPane().getSVGCanvas().addMouseListener(rml);
								frm.getScrollPane().getSVGCanvas().addMouseMotionListener(rml);
								frm.getScrollPane().getSVGCanvas().setSVGCursor(createCursor);
							}catch (Exception ex){}
							
							mouseAdapterFrames.put(frm, rml);
						}
					}
				}
			}
		}
			
		protected class PathMouseListener extends MouseAdapter implements MouseMotionListener{
		
			/**
			 * the list containing the points of the polyline
			 */
			private LinkedList<Point2D.Double> points=new LinkedList<Point2D.Double>();
			
			/**
			 * the frame
			 */
			private SVGFrame frame;
			
			/**
			 * the paint listener
			 */
			private CanvasPaintListener paintListener=null;
			
			/**
			 * the last clicked point
			 */
			private Point2D.Double lastPoint=null;
		
			/**
			 * the constructor of the class
			 * @param frame the frame
			 */
			public PathMouseListener(SVGFrame frame){
			    
				this.frame=frame;
				final SVGFrame fframe=frame;
				
				//adds a paint listener
				paintListener=new CanvasPaintListener(){

					public void paintToBeDone(Graphics g) {
						
						if(points.size()>1){
							
							//draws the shape of the path that will be created if the user released the mouse button
							Point2D.Double[] scPts=new Point2D.Double[points.size()];
							Point2D.Double pt=null;
							
							for(int i=0;i<scPts.length;i++){
							    
								pt=(Point2D.Double)points.get(i);
								scPts[i]=fframe.getScaledPoint(new Point2D.Double(pt.x, pt.y), false);
							}
							
							if(g!=null && scPts!=null){
							    
							    svgPath.drawGhost(fframe, (Graphics2D)g, scPts);
							}
						}
					}
				};
				
				frame.getScrollPane().getSVGCanvas().addLayerPaintListener(SVGCanvas.DRAW_LAYER, paintListener, false);
			}
			
			/**
			 * @param e the event
			 */
			public void mouseDragged(MouseEvent e) {
			}
				
			/**
			 * the method called when an action occurs
			 * @param evt the event
			 */
			public void mouseMoved(MouseEvent evt) {
				
				if(points.size()>0){
					
					Point2D.Double point=frame.getAlignedWithRulersPoint(evt.getPoint());

				    if(evt.isControlDown()){

				    	point=SVGToolkit.computeLinePointWhenCtrlDown(points.get(points.size()-2), point);
				    }

				    points.set(points.size()-1, point);
				}
				
				//asks the canvas to be repainted to draw the shape of the future path
				frame.getScrollPane().getSVGCanvas().delayedRepaint();
			}
			
			/**
			 * the method called when an action occurs
			 * @param evt the event
			 */
			public void mouseClicked(MouseEvent evt) {
				
				Point2D.Double point=frame.getAlignedWithRulersPoint(evt.getPoint());
				
			    if(evt.isControlDown() && points.size()>1){

			    	point=SVGToolkit.computeLinePointWhenCtrlDown(points.get(points.size()-2), point);
			    }
				
				if(point!=null){
				    
					boolean isDoubleClick=false;
					
					if(lastPoint!=null && Math.abs(lastPoint.getX()-evt.getPoint().getX())<2 && Math.abs(lastPoint.getY()-evt.getPoint().getY())<2){
						
						isDoubleClick=true;
					}
					
					lastPoint=new Point2D.Double(evt.getPoint().x, evt.getPoint().y);
				    
					if(isDoubleClick){
					
						if(points.size()>1){

							Point2D.Double[] pts=new Point2D.Double[points.size()-1];

							for(int i=0;i<pts.length;i++){
							    
								pts[i]=(Point2D.Double)points.get(i);
							}

							if(type==QUADRATIC_BEZIER){
							
								final Point2D.Double[] fpts=pts;
								
								Runnable runnable=new Runnable(){
									
									public void run() {

										svgPath.drawQuadraticBezier(frame, fpts);										
									}
								};
								
								frame.enqueue(runnable);
							
							}else if(type==CUBIC_BEZIER){

								final Point2D.Double[] fpts=pts;
								
								Runnable runnable=new Runnable(){
									
									public void run() {

										svgPath.drawCubicBezier(frame, fpts);								
									}
								};
								
								frame.enqueue(runnable);
							}

							getSVGEditor().cancelActions(true);
						
							//clears the array list
							points.clear();
							lastPoint=null;
						}
										
					}else{
					    
						if(points.size()==0){
						    
							points.add(point);
						}
						
						points.set(points.size()-1, point);
						points.add(point);
					}
				}
			}
		}
	}
}
