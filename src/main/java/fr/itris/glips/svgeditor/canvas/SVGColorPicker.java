/*
 * Created on 6 déc. 2004
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
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.resources.*;

/**
 * the tool to pick a color on the canvas
 * 
 * @author Jordi SUC
 */
public class SVGColorPicker extends SVGModuleAdapter{

	/**
	 * the id
	 */
	private String idcolorpicker="ColorPicker"; //$NON-NLS-1$
	
	/**
	 * the labels
	 */
	private String 	labelcolorpicker=""; //$NON-NLS-1$
    
	/**
	 * the menu item that will be inserted into the menubar
	 */
	protected JMenuItem menuitem=null;
	
	/**
	 * the action listener used to pick color
	 */
	private ColorPickerActionListener colorPickerAction;
	
	/**
	 * the toggle button that will be displayed in the tool bar
	 */
	protected JToggleButton toolItem=null;
	
	/**
	 * the icons that will be displayed in the menu item and the tool item
	 */
	protected ImageIcon icon=null, disabledIcon=null;
    
	/**
	 * the editor
	 */
	private SVGEditor editor;
    
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
    public SVGColorPicker(SVGEditor editor) {
	    
		this.editor=editor;
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
			    labelcolorpicker=bundle.getString("labelcolorpicker"); //$NON-NLS-1$
			}catch (Exception ex){}
		}
		
		//the icons
		icon=SVGResource.getIcon(idcolorpicker, false);
		disabledIcon=SVGResource.getIcon(idcolorpicker, true);
		
		colorPickerAction=new ColorPickerActionListener();
		
		//the toggle button
		toolItem=new JToggleButton(disabledIcon);
		toolItem.setEnabled(false);
		toolItem.setToolTipText(labelcolorpicker);
		
		//adds a listener to the toggle button
		toolItem.addActionListener(colorPickerAction);
		
		//a listener that listens to the changes of the SVGFrames
		final ActionListener svgframeListener=new ActionListener(){
			
			/**
			 * called when an event occurs
			 */
			public void actionPerformed(ActionEvent e) {
				
				if(getSVGEditor().getFrameManager().getFrameNumber()>0){

					toolItem.setEnabled(true);
					toolItem.setIcon(icon);
					
				}else{

					toolItem.setEnabled(false);
					toolItem.setIcon(disabledIcon);
				}
				
				colorPickerAction.reset();
			}
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);
	}
    
    /**
     * @return Returns the editor.
     */
    public SVGEditor getSVGEditor() {
        
        return editor;
    }

    @Override
    public Hashtable<String, JToggleButton> getToolItems() {

        Hashtable<String, JToggleButton> map=new Hashtable<String, JToggleButton>();
        map.put(idcolorpicker, toolItem);
        
        return map;
    }
    
    @Override
    public void cancelActions() {

		if(colorPickerAction!=null){
		    
		    colorPickerAction.cancelActions();
			toolItem.removeActionListener(colorPickerAction);
			toolItem.setEnabled(false);
			toolItem.setSelected(false);
			toolItem.setEnabled(true);
			toolItem.addActionListener(colorPickerAction);
		}
    }

	/**
	 * @author Jordi SUC
	 * the class allowing to get a color on the canvas
	 */
	protected class ColorPickerActionListener implements ActionListener{

		/**
		 * the hashtable associating a frame to its mouse adapter
		 */
		private final Hashtable<SVGFrame, ColorPickerListener> mouseAdapterFrames=
																						new Hashtable<SVGFrame, ColorPickerListener>();
		
		/**
		 * the cursor used when picking a color
		 */
		private Cursor pickCursor;

		/**
		 * whether this listener is active
		 */
		private boolean isActive=false;
		
		/**
		 * the constructor of the class
		 */
		protected ColorPickerActionListener(){
			
			pickCursor=getSVGEditor().getCursors().getCursor("colorpicker"); //$NON-NLS-1$
		}
		
		/**
		 * resets the listener
		 */
		protected void reset(){
			
			if(isActive){
			    
				Collection frames=getSVGEditor().getFrameManager().getFrames();
			
				Iterator it;
				SVGFrame frm=null;
				LinkedList<SVGFrame> toBeRemoved=new LinkedList<SVGFrame>();
				Object mouseListener=null;
				
				//removes all the motion adapters from the frames
				for(it=mouseAdapterFrames.keySet().iterator(); it.hasNext();){
				    
					try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
			
					if(frm!=null && ! frames.contains(frm)){
					    
						try{
							mouseListener=mouseAdapterFrames.get(frm);
							Toolkit.getDefaultToolkit().removeAWTEventListener((ColorPickerListener)mouseListener);
						}catch (Exception ex){}
						
						toBeRemoved.add(frm);
					}
				}
					
				//removes the frames that have been closed
				for(it=toBeRemoved.iterator(); it.hasNext();){
				    
					try{mouseAdapterFrames.remove(it.next());}catch (Exception ex){}
				}
				
				ColorPickerListener cml=null;

				//adds the new motion adapters
				for(it=frames.iterator(); it.hasNext();){
				    
					try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
			
					if(frm!=null && ! mouseAdapterFrames.containsKey(frm)){

						cml=new ColorPickerListener();

						try{
						    Toolkit.getDefaultToolkit().addAWTEventListener(cml, AWTEvent.MOUSE_EVENT_MASK);
							frm.getScrollPane().getSVGCanvas().setSVGCursor(pickCursor);
						}catch (Exception ex){}
						
						mouseAdapterFrames.put(frm, cml);
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
				LinkedList<SVGFrame> toBeRemoved=new LinkedList<SVGFrame>();
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
							Toolkit.getDefaultToolkit().removeAWTEventListener((ColorPickerListener)mouseListener);
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
		 * the method called when an event occurs
		 * @param evt the event
		 */
		public void actionPerformed(ActionEvent evt){

			getSVGEditor().cancelActions(false);
		
			if(getSVGEditor().getFrameManager().getCurrentFrame()!=null){
				
				toolItem.setSelected(true);
		
				//the listener is active
				isActive=true;
				Collection frames=getSVGEditor().getFrameManager().getFrames();

				Iterator it;
				SVGFrame frm=null;
				ColorPickerListener cml=null;
				
				//adds the new mouse adapters
				for(it=frames.iterator(); it.hasNext();){
				    
					try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
			
					if(frm!=null){

						cml=new ColorPickerListener();

						try{
						    Toolkit.getDefaultToolkit().addAWTEventListener(cml, AWTEvent.MOUSE_EVENT_MASK);
							frm.getScrollPane().getSVGCanvas().setSVGCursor(pickCursor);
						}catch (Exception ex){}
						
						mouseAdapterFrames.put(frm, cml);
					}
				}
			}
		}
			
		protected class ColorPickerListener implements AWTEventListener{
            
            /**
             * @see java.awt.event.AWTEventListener#eventDispatched(java.awt.AWTEvent)
             */
            public void eventDispatched(AWTEvent evt) {

                if(evt instanceof MouseEvent){

                    MouseEvent mevt=(MouseEvent)evt;
                    mevt.consume();
                    
                    Point point=mevt.getPoint();
                    
                    if(mevt.getID()==MouseEvent.MOUSE_PRESSED){
                        
                        //converting the point
                        SwingUtilities.convertPointToScreen(point, (Component)mevt.getSource());
                        
                        //getting the color at the clicked point
						Color color=getSVGEditor().getSVGToolkit().pickColor(point);
						
						if(color!=null){

							editor.getSVGColorManager().setCurrentColor(color);
						}
                        
                    }else if(mevt.getID()==MouseEvent.MOUSE_RELEASED){
                        
                        //remove this listener and set the default state of the editor
                        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                        getSVGEditor().cancelActions(true);
                    }
                }
            } 
        }
	}
}
