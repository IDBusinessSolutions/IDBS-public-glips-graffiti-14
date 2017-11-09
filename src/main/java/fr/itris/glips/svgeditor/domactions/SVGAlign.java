/*
 * Created on 27 avr. 2004
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
package fr.itris.glips.svgeditor.domactions;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.menutool.*;
import fr.itris.glips.svgeditor.resources.*;
import fr.itris.glips.svgeditor.selection.*;
import fr.itris.glips.svgeditor.undoredo.*;

/**
 * @author Jordi SUC
 *
 * the class used to align items to the left, right or to the upper or the lower item 
 */
public class SVGAlign extends SVGModuleAdapter{
	
	/**
	 * the ids of the module
	 */
	final private String idalign="Align", idleft="Left", idright="Right", idtop="Top", idbottom="Bottom", idcenter="Center", idhorizontalcenter="HorizontalCenter", idverticalcenter="VerticalCenter"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
	
	/**
	 * the labels
	 */
	private String labelalign="", labelalignleft="", labelalignright="", labelaligntop="", labelalignbottom="", labelaligncenter="", labelalignhorizontalcenter="", labelalignverticalcenter=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
	
	/**
	 * the undo/redo labels
	 */
	private String undoredoalignleft="", undoredoalignright="", undoredoaligntop="", undoredoalignbottom="", undoredoaligncenter="", undoredoalignhorizontalcenter="", undoredoalignverticalcenter=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	
	/**
	 * the constant defining the left alignment
	 */
	public final int ALIGN_LEFT=0;
	
	/**
	 * the constant defining the right alignment
	 */
	public final int ALIGN_RIGHT=1;
	
	/**
	 * the constant defining the top alignment
	 */
	public final int ALIGN_TOP=2;
	
	/**
	 * the constant defining the bottom alignment
	 */
	public final int ALIGN_BOTTOM=3;
	
	/**
	 * the constant defining the center alignment
	 */
	public final int ALIGN_CENTER=4;
	
	/**
	 * the constant defining the horizontal center alignment
	 */
	public final int ALIGN_HORIZONTAL_CENTER=5;
	
	/**
	 * the constant defining the vertical center alignment
	 */
	public final int ALIGN_VERTICAL_CENTER=6;
	
	/**
	 * the menu items that will be contained in the menu
	 */
	private JMenuItem alignLeft, alignRight, alignTop, alignBottom, alignCenter, alignHorizontalCenter, alignVerticalCenter;
	
	/**
	 * the menu items listeners
	 */
	private ActionListener alignLeftListener, alignRightListener, alignTopListener, alignBottomListener, alignCenterListener, 
										alignHorizontalCenterListener, alignVerticalCenterListener;
	
	/**
	 * the menu in which the menu items will be inserted
	 */
	private JMenu align;

	/**
	 * the editor
	 */
	private SVGEditor editor=null;
	
	/**
	 * the nodes that are currently selected
	 */
	private LinkedList selectednodes=new LinkedList();
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGAlign(SVGEditor editor) {
		
		this.editor=editor;
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
			try{
				labelalign=bundle.getString("labelalign"); //$NON-NLS-1$
				labelalignleft=bundle.getString("labelalignleft"); //$NON-NLS-1$
				labelalignright=bundle.getString("labelalignright"); //$NON-NLS-1$
				labelaligntop=bundle.getString("labelaligntop"); //$NON-NLS-1$
				labelalignbottom=bundle.getString("labelalignbottom"); //$NON-NLS-1$
				labelaligncenter=bundle.getString("labelaligncenter"); //$NON-NLS-1$
				labelalignhorizontalcenter=bundle.getString("labelalignhorizontalcenter"); //$NON-NLS-1$
				labelalignverticalcenter=bundle.getString("labelalignverticalcenter"); //$NON-NLS-1$
				undoredoalignleft=bundle.getString("undoredoalignleft"); //$NON-NLS-1$
				undoredoalignright=bundle.getString("undoredoalignright"); //$NON-NLS-1$
				undoredoaligntop=bundle.getString("undoredoaligntop"); //$NON-NLS-1$
				undoredoalignbottom=bundle.getString("undoredoalignbottom"); //$NON-NLS-1$
				undoredoaligncenter=bundle.getString("undoredoaligncenter"); //$NON-NLS-1$
				undoredoaligncenter=bundle.getString("undoredoalignhorizontalcenter"); //$NON-NLS-1$
				undoredoaligncenter=bundle.getString("undoredoalignverticalcenter"); //$NON-NLS-1$
			}catch (Exception ex){}
		}
		
		//getting the icons
		ImageIcon alignLeftIcon=SVGResource.getIcon("AlignLeft", false), //$NON-NLS-1$
						dalignLeftIcon=SVGResource.getIcon("AlignLeft", true), //$NON-NLS-1$
						alignRightIcon=SVGResource.getIcon("AlignRight", false), //$NON-NLS-1$
						dalignRightIcon=SVGResource.getIcon("AlignRight", true), //$NON-NLS-1$
						alignTopIcon=SVGResource.getIcon("AlignTop", false), //$NON-NLS-1$
						dalignTopIcon=SVGResource.getIcon("AlignTop", true), //$NON-NLS-1$
						alignBottomIcon=SVGResource.getIcon("AlignBottom", false), //$NON-NLS-1$
						dalignBottomIcon=SVGResource.getIcon("AlignBottom", true), //$NON-NLS-1$
						alignCenterIcon=SVGResource.getIcon("AlignCenter", false), //$NON-NLS-1$
						dalignCenterIcon=SVGResource.getIcon("AlignCenter", true), //$NON-NLS-1$
						alignHorizontalCenterIcon=SVGResource.getIcon("AlignHorizontalCenter", false), //$NON-NLS-1$
						dalignHorizontalCenterIcon=SVGResource.getIcon("AlignHorizontalCenter", true), //$NON-NLS-1$
						alignVerticalCenterIcon=SVGResource.getIcon("AlignVerticalCenter", false), //$NON-NLS-1$
						dalignVerticalCenterIcon=SVGResource.getIcon("AlignVerticalCenter", true); //$NON-NLS-1$
		
		//creates the menu items, sets the keyboard shortcuts
		alignLeft=new JMenuItem(labelalignleft, alignLeftIcon);
		alignLeft.setDisabledIcon(dalignLeftIcon);
		alignLeft.setAccelerator(KeyStroke.getKeyStroke("shift LEFT")); //$NON-NLS-1$
		alignLeft.setEnabled(false);
		
		alignRight=new JMenuItem(labelalignright, alignRightIcon);
		alignRight.setDisabledIcon(dalignRightIcon);
		alignRight.setAccelerator(KeyStroke.getKeyStroke("shift RIGHT")); //$NON-NLS-1$
		alignRight.setEnabled(false);
		
		alignTop=new JMenuItem(labelaligntop, alignTopIcon);
		alignTop.setDisabledIcon(dalignTopIcon);
		alignTop.setAccelerator(KeyStroke.getKeyStroke("shift UP")); //$NON-NLS-1$
		alignTop.setEnabled(false);
		
		alignBottom=new JMenuItem(labelalignbottom, alignBottomIcon);
		alignBottom.setDisabledIcon(dalignBottomIcon);
		alignBottom.setAccelerator(KeyStroke.getKeyStroke("shift DOWN")); //$NON-NLS-1$
		alignBottom.setEnabled(false);
		
		alignCenter=new JMenuItem(labelaligncenter, alignCenterIcon);
		alignCenter.setDisabledIcon(dalignCenterIcon);
		alignCenter.setAccelerator(KeyStroke.getKeyStroke("shift C")); //$NON-NLS-1$
		alignCenter.setEnabled(false);
		
		alignHorizontalCenter=new JMenuItem(labelalignhorizontalcenter, alignHorizontalCenterIcon);
		alignHorizontalCenter.setDisabledIcon(dalignHorizontalCenterIcon);
		alignHorizontalCenter.setEnabled(false);
		
		alignVerticalCenter=new JMenuItem(labelalignverticalcenter, alignVerticalCenterIcon);
		alignVerticalCenter.setDisabledIcon(dalignVerticalCenterIcon);
		alignVerticalCenter.setEnabled(false);
		
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
				selectednodes.clear();
				
				//disables the menuitems
				alignLeft.setEnabled(false);
				alignRight.setEnabled(false);
				alignTop.setEnabled(false);
				alignBottom.setEnabled(false);
				alignCenter.setEnabled(false);
				alignHorizontalCenter.setEnabled(false);
				alignVerticalCenter.setEnabled(false);
				
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
				alignLeft.setEnabled(false);
				alignRight.setEnabled(false);
				alignTop.setEnabled(false);
				alignBottom.setEnabled(false);
				alignCenter.setEnabled(false);
				alignHorizontalCenter.setEnabled(false);
				alignVerticalCenter.setEnabled(false);
				
				LinkedList list=null;
				
				//gets the currently selected nodes list 
				if(selection!=null){
				    
					list=selection.getCurrentSelection(getSVGEditor().getFrameManager().getCurrentFrame());
				}
				
				selectednodes.clear();
				
				//refresh the selected nodes list
				if(list!=null){
				    
				    selectednodes.addAll(list);
				}
				
				if(selectednodes.size()>1){
				    
					alignLeft.setEnabled(true);
					alignRight.setEnabled(true);
					alignTop.setEnabled(true);
					alignBottom.setEnabled(true);
					alignCenter.setEnabled(true);
					alignHorizontalCenter.setEnabled(true);
					alignVerticalCenter.setEnabled(true);
				}								
			}
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);	
		
		//adds the listeners
		alignLeftListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>0){
						
						align(selectednodes,ALIGN_LEFT);
						
						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
			    }
			}
		};

		alignLeft.addActionListener(alignLeftListener);
		
		alignRightListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>0){
						
						align(selectednodes,ALIGN_RIGHT);
						
						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
			    }
			}
		};
		
		alignRight.addActionListener(alignRightListener);
		
		alignTopListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>0){

						align(selectednodes,ALIGN_TOP);

						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
			    }
			}
		};
		
		alignTop.addActionListener(alignTopListener);
		
		alignBottomListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>0){

						align(selectednodes,ALIGN_BOTTOM);

						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
			    }
			}
		};
		
		alignBottom.addActionListener(alignBottomListener);
		
		alignCenterListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>0){

						align(selectednodes,ALIGN_CENTER);

						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
			    }
			}
		};
		
		alignCenter.addActionListener(alignCenterListener);
		
		alignHorizontalCenterListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>0){

						align(selectednodes, ALIGN_HORIZONTAL_CENTER);

						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
			    }
			}
		};
		
		alignHorizontalCenter.addActionListener(alignHorizontalCenterListener);
		
		alignVerticalCenterListener=new ActionListener(){
		    
			public void actionPerformed(ActionEvent e) {
			    
			    if(getSVGEditor().getSVGSelection()!=null && ! getSVGEditor().getSVGSelection().isActing()){
			        
					getSVGEditor().cancelActions(true);
					
					if(selectednodes.size()>0){

						align(selectednodes, ALIGN_VERTICAL_CENTER);

						//sets that the svg has been modified
						getSVGEditor().getFrameManager().getCurrentFrame().setModified(true);
					}
			    }
			}
		};
		
		alignVerticalCenter.addActionListener(alignVerticalCenterListener);
		
		//adds the menu items to the menu
		align=new JMenu(labelalign);
		align.add(alignLeft);
		align.add(alignRight);
		align.add(alignTop);
		align.add(alignBottom);
		align.add(alignCenter);
		align.add(alignHorizontalCenter);
		align.add(alignVerticalCenter);
	}
	
	/**
	 * @return the editor
	 */
	public SVGEditor getSVGEditor(){
		return editor;
	}
	
	/**
	 * @return a map associating a menu item id to its menu item object
	 */
	public Hashtable getMenuItems(){
		
		Hashtable menuItems=new Hashtable();
		menuItems.put(idalign,align);
		
		return menuItems;
	}
	
	/**
	 * @return a map associating a tool item id to its tool item object
	 */
	public Hashtable getToolItems(){
		
		return null;
	}
	
	/**
	 * Returns the collection of the popup items
	 * @return the collection of the popup items
	 */
	public Collection getPopupItems(){
		
		LinkedList popupItems=new LinkedList();
		
		SVGPopupSubMenu subMenu=new SVGPopupSubMenu(getSVGEditor(), idalign, labelalign, ""); //$NON-NLS-1$
		
		popupItems.add(subMenu);
		
		//creating the align left popup item
		SVGPopupItem alignLeftItem=new SVGPopupItem(getSVGEditor(), idleft, labelalignleft, "AlignLeft"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>1){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(alignLeftListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the align bottom popup item
		SVGPopupItem alignRightItem=new SVGPopupItem(getSVGEditor(), idright, labelalignright, "AlignRight"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>1){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(alignRightListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the align top popup item
		SVGPopupItem alignTopItem=new SVGPopupItem(getSVGEditor(), idtop, labelaligntop, "AlignTop"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>1){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(alignTopListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};

		//creating the align bottom popup item
		SVGPopupItem alignBottomItem=new SVGPopupItem(getSVGEditor(), idbottom, labelalignbottom, "AlignBottom"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>1){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(alignBottomListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the align center popup item
		SVGPopupItem alignCenterItem=new SVGPopupItem(getSVGEditor(), idcenter, labelaligncenter, "AlignCenter"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>1){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(alignCenterListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the horizontal align center popup item
		SVGPopupItem horizontalAlignCenterItem=new SVGPopupItem(getSVGEditor(), idhorizontalcenter, labelalignhorizontalcenter, "AlignHorizontalCenter"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>1){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(alignHorizontalCenterListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//creating the vertical align center popup item
		SVGPopupItem verticalAlignCenterItem=new SVGPopupItem(getSVGEditor(), idverticalcenter, labelalignverticalcenter, "AlignVerticalCenter"){ //$NON-NLS-1$
		
			public JMenuItem getPopupItem(LinkedList nodes) {

				if(nodes!=null && nodes.size()>1){
					
					menuItem.setEnabled(true);
					
					//adds the action listeners
					if(menuItem.isEnabled()){
						
						menuItem.addActionListener(alignVerticalCenterListener);
					}
					
				}else{
					
					menuItem.setEnabled(false);
				}
				
				return super.getPopupItem(nodes);
			}
		};
		
		//adding the popup items to the sub menu
		subMenu.addPopupItem(alignLeftItem);
		subMenu.addPopupItem(alignRightItem);
		subMenu.addPopupItem(alignTopItem);
		subMenu.addPopupItem(alignBottomItem);
		subMenu.addPopupItem(alignCenterItem);
		subMenu.addPopupItem(horizontalAlignCenterItem);
		subMenu.addPopupItem(verticalAlignCenterItem);
		
		return popupItems;
	}
	
	/**
	 * aligns the nodes contained in the selection list horizontally with the node that is placed at the very left of the selected nodes
	 * @param list a list of nodes
	 * @param type the type of the alignment
	 */
	protected void align(LinkedList list, int type){
		
		final SVGFrame frame=editor.getFrameManager().getCurrentFrame();
		
		if(list!=null && list.size()>1 && frame!=null){

			final LinkedList snodes=new LinkedList(list);
			final int ftype=type;

			//the map associating a node to its transform for the alignment
			final Hashtable transformMap=new Hashtable();
			
			//computing the union of all the outlines of the selected nodes
			Node current=null;
			final Area area=new Area();
			Shape outline=null;

			for(Iterator it=snodes.iterator(); it.hasNext();){
			    
				try{current=(Node)it.next();}catch (Exception e){current=null;}
				
				if(current!=null){	
				    
					outline=frame.getNodeGeometryBounds((Element)current);
					
					if(outline!=null){
					    
						area.add(new Area(outline));
					}
				}
			}
			
			//computes the values of the translation depending on the type of the alignment
			Runnable runnable=new Runnable(){
			    
				public void run(){

					AffineTransform af=null;
					SVGTransformMatrix matrix=null;
					Rectangle2D rect, rect0=area.getBounds();
					Node current=null;
					double e=0, f=0;
					
					for(Iterator it=snodes.iterator(); it.hasNext();){
					    
						try{current=(Node)it.next();}catch (Exception ex){current=null;}
						
						if(current!=null){

							rect=frame.getNodeGeometryBounds((Element)current);
						
							if(ftype==ALIGN_LEFT){
							    
								if(rect0!=null && rect!=null){
								    
									e=rect0.getX()-rect.getX();
								}
	
							}else if(ftype==ALIGN_RIGHT){
							    
								if(rect0!=null && rect!=null){
								    
									e=(rect0.getX()+rect0.getWidth())-(rect.getX()+rect.getWidth());
								}
							
							}else if(ftype==ALIGN_TOP){
							    
								if(rect0!=null && rect!=null){
								    
									f=rect0.getY()-rect.getY();
								}
							
							}else if(ftype==ALIGN_BOTTOM){
							    
								if(rect0!=null && rect!=null){
								    
									f=(rect0.getY()+rect0.getHeight())-(rect.getY()+rect.getHeight());
								}
								
							}else if(ftype==ALIGN_CENTER){
							    
								if(rect0!=null && rect!=null){
								    
								    e=(rect0.getX()+(rect0.getWidth()-rect.getWidth())/2)-rect.getX();
								    f=rect0.getY()+(rect0.getHeight()-rect.getHeight())/2-rect.getY();
								}
								
							}else if(ftype==ALIGN_HORIZONTAL_CENTER){
							    
								if(rect0!=null && rect!=null){
								    
								    e=(rect0.getX()+(rect0.getWidth()-rect.getWidth())/2)-rect.getX();
								}
								
							}else if(ftype==ALIGN_VERTICAL_CENTER){
							    
								if(rect0!=null && rect!=null){
								    
								    f=rect0.getY()+(rect0.getHeight()-rect.getHeight())/2-rect.getY();
								}

							}else return;	
						
							af=AffineTransform.getTranslateInstance(e, f);
							transformMap.put(current, af);

							if(! af.isIdentity()){
							    
							    //gets, modifies and sets the transform matrix
							    matrix=editor.getSVGToolkit().getTransformMatrix(current);
							    matrix.concatenateTransform(af);
							    editor.getSVGToolkit().setTransformMatrix(current, matrix);
							}
						}
					}
					
				    
				    frame.getScrollPane().getSVGCanvas().delayedRepaint();
				}
			};
			
			frame.enqueue(runnable);

			//sets the name of the undo/redo action
			String actionName=""; //$NON-NLS-1$
			
			if(type==ALIGN_LEFT){
			    
				actionName=undoredoalignleft;
				
			}else if(type==ALIGN_RIGHT){
			    
				actionName=undoredoalignright;
				
			}else if(type==ALIGN_TOP){
			    
				actionName=undoredoaligntop;
				
			}else if(type==ALIGN_BOTTOM){
			    
				actionName=undoredoalignbottom;
				
			}else if(type==ALIGN_CENTER){
			    
				actionName=undoredoaligncenter;
				
			}else if(type==ALIGN_HORIZONTAL_CENTER){
			    
				actionName=undoredoalignhorizontalcenter;
				
			}else if(type==ALIGN_VERTICAL_CENTER){
			    
				actionName=undoredoalignverticalcenter;
			}
			
			//creates the undo/redo action and insert it into the undo/redo stack
			if(editor.getUndoRedo()!=null){
				
				SVGUndoRedoAction action=new SVGUndoRedoAction(actionName){

					public void undo(){
					    
						//sets the nodes transformation matrix 
						if(transformMap.size()>0){

							Node current=null;
							SVGTransformMatrix matrix=null;
							AffineTransform af=null;
							
							for(Iterator it=transformMap.keySet().iterator(); it.hasNext();){
							    
								try{current=(Node)it.next();}catch (Exception ex){current=null;}
								
								if(current!=null){
								    
									try{
									    af=(AffineTransform)transformMap.get(current);
									}catch (Exception ex){af=null;}
							
									if(af!=null && ! af.isIdentity()){
									    
									    matrix=editor.getSVGToolkit().getTransformMatrix(current);
									    try{matrix.concatenateTransform(af.createInverse());}catch(Exception ex){}
									    editor.getSVGToolkit().setTransformMatrix(current, matrix);
									}
								}
							}

						    frame.getScrollPane().getSVGCanvas().delayedRepaint();
						}
					}

					public void redo(){
					    
						//sets the nodes transformation matrix 				
						if(transformMap.size()>0){

							Node current=null;
							SVGTransformMatrix matrix=null;
							AffineTransform af=null;
							
							for(Iterator it=transformMap.keySet().iterator(); it.hasNext();){
							    
								try{current=(Node)it.next();}catch (Exception ex){current=null;}
								
								if(current!=null){
								    
									try{
										af=(AffineTransform)transformMap.get(current);
									}catch (Exception ex){af=null;}
							
									if(af!=null && ! af.isIdentity()){
									    
									    matrix=editor.getSVGToolkit().getTransformMatrix(current);
									    matrix.concatenateTransform(af);
									    editor.getSVGToolkit().setTransformMatrix(current, matrix);
									}
								}
							}

						    frame.getScrollPane().getSVGCanvas().delayedRepaint();
						}							
					}
				};
				
				//gets or creates the undo/redo list and adds the action into it
				SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList(actionName);
				actionlist.add(action);
				editor.getUndoRedo().addActionList(frame, actionlist);
			}	
		}
	}
	
	/**
	 * gets the name of the module
	 * @return the name of the module
	 */
	public String getName(){
		return idalign;
	}
	
	/**
	 * cancels all the actions that could be running
	 */
	public void cancelActions(){
	}

	/**
	 * layout some elements in the module
	 */
	public void initialize(){
		

	}
}
