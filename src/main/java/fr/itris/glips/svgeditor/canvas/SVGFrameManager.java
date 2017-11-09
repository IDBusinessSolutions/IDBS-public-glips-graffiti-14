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

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.menutool.*;
import fr.itris.glips.svgeditor.resources.*;

/**
 * @author Jordi SUC
 * the class that manages the different panels (SVGFrame) containing the SVG pictures 
 */
public class SVGFrameManager {
	
	/**
	 * the ids
	 */
	final private String idmenuframe="menuframe"; //$NON-NLS-1$
	
	/**
	 * the list of the SVGFrames that can be displayed
	 */
	private List<SVGFrame> frames=Collections.synchronizedList(new LinkedList<SVGFrame>());
	
	/**
	 * the currently displayed SVGFrame
	 */
	private SVGFrame currentFrame=null;
	
	/**
	 * the editor
	 */
	private SVGEditor editor;
	
	/**
	 * the list of the listeners on the changes of the current SVGFrame
	 */
	private List<ActionListener> svgFrameChangedListeners=Collections.synchronizedList(new LinkedList<ActionListener>());
	
	/**
	 * the group that manages the frame menu items
	 */
	private ButtonGroup group=new ButtonGroup();

	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGFrameManager(SVGEditor editor) {
	    
		this.editor=editor;
	}
	
	/**
	 *layouts some elements
	 */
	public void layoutElements(){
		
		//getting the icons
		ImageIcon     icon=SVGResource.getIcon("Window", false), //$NON-NLS-1$
		                     disabledIcon=SVGResource.getIcon("Window", true); //$NON-NLS-1$
		
		//getting the menu
		JMenu menu=editor.getMenuBar().getMenu(idmenuframe);
		
		if(menu!=null){

			menu.setIcon(icon);
			menu.setDisabledIcon(disabledIcon);
			editor.getMenuBar().build();
		}
	}
	
	/**
	 * the figure associated with a frame in the menu
	 */
	private int frameFigure=0;
	
	/**
	 * adds a listener called when the current SVGFrame changes
	 * @param listener the listener to be added
	 */
	public synchronized void addSVGFrameChangedListener(ActionListener listener){
	    
		svgFrameChangedListeners.add(listener);
	}
	
	/**
	 * removes a listener called when the current SVGFrame changes
	 * @param listener the listener to be removed
	 */
	public synchronized void removeSVGFrameChangedListener(ActionListener listener){
	    
		svgFrameChangedListeners.remove(listener);
	}
	
	/**
	 * notifies the listeners when the SVGFrame is changed
	 */
	public void frameChanged(){

        SwingUtilities.invokeLater(new Runnable(){
            
            public void run() {
                
                for(ActionListener listener : svgFrameChangedListeners){
                    
                    if(listener!=null){
                        
                        listener.actionPerformed(new ActionEvent(editor.getDesktop(),ActionEvent.ACTION_PERFORMED,"frameChanged")); //$NON-NLS-1$
                    }
                }
                
                if(currentFrame!=null){
                    
                    currentFrame.getFrameMenuItem().setSelected(true);
                }
            }
        });
	}
	
	/**
	 * @param name the name of a SVGFrame
	 * @return a SVGFrame given its name
	 */
	public SVGFrame getFrame(String name){

		for(SVGFrame current : frames){

			if(current.getName()!=null && current.getName().equals(name)){
			    
			    return current;
			}
		}
		
		return null;
	}
	
	/**
	 * @return the number of the available SVGFrames
	 */
	public int getFrameNumber(){
		return frames.size();
	}
	
	/**
	 * @return a collection of all the available SVGFrames
	 */
	public Collection<SVGFrame> getFrames(){
		return frames;
	}
	
	/**
	 * adds the SVGFrame'z menu item into the menu bar
	 * @param frame the SVGFrame whose menuitem will be inserted into the menu bar
	 */
	protected synchronized void addFrameInMenu(SVGFrame frame){
		
		final SVGFrame f=frame;
		final SVGFrameManager fmg=this;
		SVGMenuBar menubar=editor.getMenuBar();
		
		//gets or create the menu that will contain the menu item
		//adds the action to the menu item
		frame.getFrameMenuItem().addActionListener(new ActionListener(){		
			
			public void actionPerformed(ActionEvent e){
			    
				fmg.setCurrentFrame(f.getName());
			}	
		});
		
		if(menubar!=null){
			
			if(frameFigure<10){
			    
				frame.getFrameMenuItem().setAccelerator(KeyStroke.getKeyStroke("ctrl NUMPAD"+frameFigure)); //$NON-NLS-1$
				frameFigure++;
			}
			
			menubar.addUnknownMenuItem(idmenuframe, frame.getFrameMenuItem());
			group.add(frame.getFrameMenuItem());
			frame.getFrameMenuItem().setSelected(true);
			
			menubar.build();
		}
	}
	
	/**
	 * removes the SVGFrame's menu item from the menu bar
	 * @param frame the SVGFrame whose menuitem will be removed from the menu bar
	 */
	protected synchronized void removeFrameInMenu(SVGFrame frame){
		
		SVGMenuBar menubar=editor.getMenuBar();
		
		if(frame!=null){
			
			menubar.removeUnknownMenuItem(idmenuframe, frame.getFrameMenuItem());
			group.remove(frame.getFrameMenuItem());	
			
			menubar.build();
		}
	}
	
	/**
	 * creates a new SVGFrame
	 * @param name the name of the new SVGFrame
	 * @return the new SVGFrame
	 */
	public synchronized SVGFrame createFrame(String name){
		
		if(name!=null && ! name.equals("")){ //$NON-NLS-1$
		    
			//checks if the name is correct
			//if another frame has the same name
			//a number is concatenated at the end of the name
			int count=countName(name), nb=0;
			
			while(count>0){
			    
				count=countName(name+" ("+new Integer(nb+1)+")"); //$NON-NLS-1$ //$NON-NLS-2$
				nb++;
			}
			
			if(nb>0){
			    
			    name=name+" ("+new Integer(nb)+")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			SVGFrame frame=new SVGFrame(editor, name);

			//adds the SVGFrame in the list
			frames.add(frames.size(), frame);
			
			if(currentFrame!=null){
			    
				//hides the current SVGFrame
				currentFrame.moveToBack();
			}
			
			currentFrame=frame;
			addFrameInMenu(frame);

			frame.moveToFront();

			return frame;
		}
        
        return null;
	}
	
	/**
	 * modifies the name of the frame
	 * @param oldName the current name of the frame
	 * @param newName the new name
	 */
	public synchronized void changeName(String oldName, String newName){
		
		SVGFrame frame=getFrame(oldName);
		
		if(newName!=null && ! newName.equals("")){ //$NON-NLS-1$
		    
			//checks if the name is correct
			//if another frame has the same name,
			//a number is concatenated at the end of the name
			int count=countName(newName), nb=0;
			
			while(count>0){
			    
				count=countName(newName+" ("+new Integer(nb+1)+")"); //$NON-NLS-1$ //$NON-NLS-2$
				nb++;
			}
			
			if(nb>0){
			    
			    newName=newName+" ("+new Integer(nb)+")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			frame.setName(newName);
		}	
	}
	
	/**
	 * sets the SVGFrame as the current SVGFrame, i.d. : set this SVGFrame visible and hide the current one
	 * @param name the name of the SVGFrame
	 */
	public synchronized void setCurrentFrame(String name){
		
		SVGFrame frame=getFrame(name);
		
		if(frame!=null){
			
			if(currentFrame!=null){
			    
				currentFrame.moveToBack();
			}
			
			currentFrame=frame;
			frames.remove(frame);
			frames.add(frames.size(), frame);
			frame.moveToFront();
			
			//notifies that the current frame has changed
			frameChanged();
		}
	}
	
	/**
	 * @return the visible SVGFrame
	 */
	public SVGFrame getCurrentFrame(){
		return currentFrame;
	}
	
	/**
	 * removes the frame from the list : the frame cannot be seen and is deleted
	 * @param name the name of the SVGFrame
	 */
	public void removeFrame(String name){

		SVGFrame frame=getFrame(name);

		if(frame!=null){
		    
		    removeFrameInMenu(frame);
			frame.moveToBack();
			frame.removeFromDesktop();
			frames.remove(frame);
			
			//if it is not the last SVGFrame in the list, another SVGFrame is displayed
			if(frames.size()>0){
			    
				setCurrentFrame(frames.get(frames.size()-1).getName());
				
			}else{
				
				currentFrame=null;
                
                //notifies that the current frame has changed       
                frameChanged();
			}

			frame.dispose();
		}
	}
	
	/**
	 * @param name the name of the SVGFrame
	 * @return the number of SVGFrames having the same name as "name"
	 */
	protected int countName(String name){

		SVGFrame current=null;
		int nb=0;
		
		for(Iterator it=frames.iterator(); it.hasNext();){
		    
			current=((SVGFrame)it.next());
			
			if(current.getName()!=null && current.getName().equals(name)){
			    
			    nb++;
			}
		}
		
		return nb;
	}
}
