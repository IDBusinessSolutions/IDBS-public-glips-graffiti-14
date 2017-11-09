/*
 * Created on 23 mars 2004
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

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.resources.*;
import fr.itris.glips.svgeditor.undoredo.*;

/**
 * @author Jordi SUC
 * class allowing to zoom at different scales on the canvas of the current SVGFrame
 */
public class SVGZoom extends SVGModuleAdapter{
	
	/**
	 * the id of the module
	 */
	final private String idzoom="Zoom", idzoom11="Zoom 1:1", idzoomin="Zoom +", idzoomout="Zoom -"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	
	/**
	 * the labels
	 */
	private String labelzoom="", labelzoom11="", labelzoomin="", labelzoomout=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	
	/**
	 * an array of labels
	 */
	final private String[] labels=new String[13];
	
	/**
	 * defines the zoom factors
	 */
	private final double[] factors=new double[13];
	
	/**
	 * the menu items that will be contained in the zoom menu
	 */
	private JMenuItem[] zooms=new JMenuItem[13];
	
	/**
	 * the other menu items
	 */
	private JMenuItem zoom11, zoomIn, zoomOut;
	
	/**
	 * the action listeners
	 */
	private ActionListener zoom11Listener, zoomInListener, zoomOutListener;
	
	/**
	 * the editor
	 */
	private SVGEditor editor;
	
	/**
	 * the menu
	 */
	private JMenu zoom;
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGZoom(SVGEditor editor){
		
		this.editor=editor;
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labelzoom=bundle.getString("labelzoom"); //$NON-NLS-1$
				labelzoom11=bundle.getString("labelzoom11"); //$NON-NLS-1$
				labelzoomin=bundle.getString("labelzoomin"); //$NON-NLS-1$
				labelzoomout=bundle.getString("labelzoomout"); //$NON-NLS-1$
			}catch (Exception ex){}
		}
		
		//getting the icons
		ImageIcon zoom11Icon=SVGResource.getIcon("Zoom11", false), //$NON-NLS-1$
						dzoom11Icon=SVGResource.getIcon("zoom11", true), //$NON-NLS-1$
						zoomInIcon=SVGResource.getIcon("Zoom+", false), //$NON-NLS-1$
						dzoomInIcon=SVGResource.getIcon("Zoom+", true), //$NON-NLS-1$
						zoomOutIcon=SVGResource.getIcon("Zoom-", false), //$NON-NLS-1$
						dzoomOutIcon=SVGResource.getIcon("Zoom-", true), //$NON-NLS-1$
						zoomIcon=SVGResource.getIcon("Zoom", false), //$NON-NLS-1$
						dzoomIcon=SVGResource.getIcon("Zoom", true); //$NON-NLS-1$
		
		//creates the menu in which the menu items will be inserted
		zoom=new JMenu(labelzoom);
		zoom.setIcon(zoomIcon);
		zoom.setDisabledIcon(dzoomIcon);
		
		//defines the labels for the menuitems contained in the zoom menu
		labels[0]="5 %"; //$NON-NLS-1$
		labels[1]="10 %"; //$NON-NLS-1$
		labels[2]="20 %"; //$NON-NLS-1$
		labels[3]="50 %"; //$NON-NLS-1$
		labels[4]="75 %"; //$NON-NLS-1$
		labels[5]="100 %"; //$NON-NLS-1$
		labels[6]="125 %"; //$NON-NLS-1$
		labels[7]="150 %"; //$NON-NLS-1$
		labels[8]="200 %"; //$NON-NLS-1$
		labels[9]="400 %"; //$NON-NLS-1$
		labels[10]="500 %"; //$NON-NLS-1$
		labels[11]="800 %"; //$NON-NLS-1$
		labels[12]="1000 %"; //$NON-NLS-1$
		
		//the zoom factors
		factors[0]=0.05;
		factors[1]=0.1;
		factors[2]=0.2;
		factors[3]=0.5;
		factors[4]=0.75;
		factors[5]=1.0;
		factors[6]=1.25;
		factors[7]=1.5;
		factors[8]=2.0;
		factors[9]=4.0;
		factors[10]=5.0;
		factors[11]=8.0;
		factors[12]=10.0;

		//creates the menu items
		int i;
		
		for(i=0;i<13;i++){
		    
		    zooms[i]=new JMenuItem(labels[i]);
		}

		//adds the listeners to the menu items
		for(i=0;i<13;i++){
		    
			final int ind=i;
			
			zooms[i].addActionListener(
			        
				new SVGZoomAction(){
				    
					@Override
					public void actionPerformed(ActionEvent evt) {
					    
						setScale(factors[ind]);
						super.actionPerformed(evt);
					}		
				}
			);
		}
		
		//adds the menu items to the menu
		for(i=0;i<13;i++){
		    
			zoom.add(zooms[i]);
			zooms[i].setEnabled(false);
		}
		
		//creates the other menu items
		zoom11=new JMenuItem(labelzoom11, zoom11Icon);
		zoom11.setDisabledIcon(dzoom11Icon);
		zoom11.setAccelerator(KeyStroke.getKeyStroke('1'));
		zoom11.setEnabled(false);
		
		//creates the listeners
		zoom11Listener=new SVGZoomAction(){
		    
			@Override
			public void actionPerformed(ActionEvent evt) {
			    
				setScale(factors[5]);
				super.actionPerformed(evt);
			}		
		};
		
		zoom11.addActionListener(zoom11Listener);
		
		zoomIn=new JMenuItem(labelzoomin, zoomInIcon);
		zoomIn.setDisabledIcon(dzoomInIcon);
		zoomIn.setAccelerator(KeyStroke.getKeyStroke('+'));
		zoomIn.setEnabled(false);
		
		zoomInListener=new SVGZoomAction(){
			
			@Override
			public void actionPerformed(ActionEvent evt) {
			    
				setScale(getFactor(true));
				super.actionPerformed(evt);
			}
		};
		
		zoomIn.addActionListener(zoomInListener);
		
		zoomOut=new JMenuItem(labelzoomout, zoomOutIcon);
		zoomOut.setDisabledIcon(dzoomOutIcon);
		zoomOut.setAccelerator(KeyStroke.getKeyStroke('-'));
		zoomOut.setEnabled(false);
		
		zoomOutListener=new SVGZoomAction(){
		    
			@Override
			public void actionPerformed(ActionEvent evt) {
			    
				setScale(getFactor(false));
				super.actionPerformed(evt);
			}		
		};
		
		zoomOut.addActionListener(zoomOutListener);
		
		//a listener that listens to the changes of the SVGFrames
		final ActionListener svgframeListener=new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				final SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
				
				if(frame!=null){
				    
					//enables the menuitems
					zoom11.setEnabled(true);
					zoomOut.setEnabled(true);
					zoomIn.setEnabled(true);

					for(int j=0;j<13;j++){

						zooms[j].setEnabled(true);
					}
					
				}else{
				    
					//disables the menuitems
					zoom11.setEnabled(false);
					zoomOut.setEnabled(false);
					zoomIn.setEnabled(false);

					for(int j=0;j<13;j++){

						zooms[j].setEnabled(false);
					}
				}
			}	
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);
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
		menuItems.put(idzoom11,zoom11);
		menuItems.put(idzoomin,zoomIn);
		menuItems.put(idzoomout,zoomOut);
		menuItems.put(idzoom,zoom);
		
		return menuItems;
	}
	
	/**
	 * gets the name of the module
	 * @return the name of the module
	 */
	public String getName(){
		return idzoom;
	}
	
	/**
	 * the method used by the actions to know the scale factor to apply
	 * @param isZoomIn true if the action is a zoom in action
	 * @return the computed scale
	 */
	protected double getFactor(boolean isZoomIn){
		
		//gets the index of the current scale
		SVGFrame currentFrame=getSVGEditor().getFrameManager().getCurrentFrame();
		double scale=currentFrame.getScrollPane().getSVGCanvas().getScale();
						
		int currentFactor=0;
		
		for(int i=0;i<13;i++){
		    
			if(factors[i]==scale){
			    
				currentFactor=i;
				break;
			}
		}
		
		if(isZoomIn){
		    
			return (factors[(currentFactor+1)<13?currentFactor+1:currentFactor]);
			
		}else{
		    
			return (factors[(currentFactor-1)>=0?currentFactor-1:currentFactor]);
		}
	}
	
	/**
	 * @author Jordi SUC
	 * the class that allows to perform a zoom at a single scale 
	 */
	public class SVGZoomAction implements ActionListener{
		
		private double scale=1.0;
		
		/**
		 * the undo/redo labels
		 */
		private String undoredozoom=""; //$NON-NLS-1$
		
		/**
		 * the zoom action
		 */
		public SVGZoomAction(){
			
			//gets the labels from the resources
			ResourceBundle bundle=null;
			try{
				bundle=ResourceBundle.getBundle("fr.itris.glips.svgeditor.resources.properties.SVGEditorStrings"); //$NON-NLS-1$
			}catch (Exception ex){bundle=null;}
		
			if(bundle!=null){
			    
				undoredozoom=bundle.getString("undoredozoom"); //$NON-NLS-1$
			}
		}
		
		/**
		 * sets the current scale
		 * @param scale the current scale
		 */
		public void setScale(double scale){
			this.scale=scale;
		}
		
		/**
		 * the method called when an action occurs
		 * @param evt the event
		 */
		public void actionPerformed(ActionEvent evt) {
			
			final SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
			
			if(frame!=null){
			
				getSVGEditor().cancelActions(true);

				//create the undo/redo action and insert it into the undo/redo stack
				if(getSVGEditor().getUndoRedo()!=null){
				    
					SVGUndoRedoAction action=new SVGUndoRedoAction(undoredozoom){

						/**
						 * the last scale
						 */
						private final double lastScale=frame.getScrollPane().getSVGCanvas().getScale();
					
						/**
						 * the new scale
						 */
						private final double currentScale=scale;

						@Override
						public void undo(){
						    
							setToScale(frame, lastScale);
						}

						@Override
						public void redo(){		
						    
							setToScale(frame, currentScale);
						}
					};
					
					SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList(undoredozoom);
					actionlist.add(action);
					getSVGEditor().getUndoRedo().addActionList(frame, actionlist);	
				}

				//scales the picture
				setToScale(frame, scale);
			}
		}
		
		/**
		 * sets the canvas to the given scale
		 * @param frame the current frame
		 * @param scl the scale
		 */
		protected void setToScale(SVGFrame frame, double scl){

			frame.getScrollPane().renderZoom(scl);
				
			//sets the piece of information in the state bar
			frame.getStateBar().setSVGZoom(new Integer((int)(100*scl)).toString()+" %");			 //$NON-NLS-1$
		}
	}
}
