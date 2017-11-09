/*
 * Created on 1 déc. 2004
 
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
import java.awt.event.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.resources.*;

/**
 * @author Jordi SUC
 *
 * the class allowing to display a frame containing the components 
 * used for modifying the svg elements
 */
public class SVGToolFrame{

    /**
     * the editor
     */
    private SVGEditor editor;
    
    /**

     * the font
	 */
	public final Font theFont=new Font("theFont", Font.ROMAN_BASELINE, 10); //$NON-NLS-1$
    
    /**
     * the panel containing the widgets that will be displayed
     */
    protected JPanel toolPanel=new JPanel();
    
    /**
     * the toggle button used to show or hide the frame, and that will be found in the tool bar
     */
    private JToggleButton frameButton=new JToggleButton();
    
    /**
     * the menu item used to handle the visibility of the frame
     */
    private JMenuItem frameMenuItem=new JMenuItem();
    
    /**
     * whether the frame has already been shown
     */
    private boolean hasBeenShown=false;
    
    /**
     * the runnable that should be executed when the visibility of the frame changes
     */
    private Runnable visibilityChangedRunnable=null;
    
	/**
	 * the id
	 */
	private String id=""; //$NON-NLS-1$
    
    /**
     * the labels
     */
    private String showFrameLabel="", hideFrameLabel=""; //$NON-NLS-1$ //$NON-NLS-2$
    
    /**
     * the constructor of the class
     *@param editor the editor
     *@param id the id of a module
     *@param label the label that will be displayed in the title
     *@param toolPanel the panel that will be displayed
     */
    public SVGToolFrame(final SVGEditor editor, String id, String label, final JPanel toolPanel){
        
        this.editor=editor;
        this.toolPanel=toolPanel;
        this.id=id;
    	
        //building the dialog//

        //setting the properties for the dialog
    	toolPanel.setVisible(true);
		
		//adds a listener to the resizement of the desktop
		/*editor.getDesktop().addComponentListener(new ComponentAdapter(){

			@Override
            public void componentResized(ComponentEvent evt) {

                Rectangle dialogBounds=toolPanel.getBounds();
                Dimension parentSize=SVGToolFrame.this.editor.getDesktop().getSize();
                
                if(dialogBounds.x+dialogBounds.width>parentSize.width){
                    
                    dialogBounds.x=parentSize.width-dialogBounds.width;
                }
                
                if(dialogBounds.y+dialogBounds.height>parentSize.height){
                    
                    dialogBounds.y=parentSize.height-dialogBounds.height;
                }
                
                if(dialogBounds.x<0){
                	
                	dialogBounds.x=0;
                }
                
                if(dialogBounds.y<0){
                	
                	dialogBounds.y=0;
                }
                
                toolPanel.setBounds(dialogBounds);
            }
		});*/
		
		//adds the dialog to the desktop
		editor.getDesktop().add(toolPanel);
		
        //getting the labels
        if(SVGEditor.getBundle()!=null){
        	
        	try{
        		showFrameLabel=SVGEditor.getBundle().getString("label_hidden_"+id.toLowerCase()); //$NON-NLS-1$
        		hideFrameLabel=SVGEditor.getBundle().getString("label_shown_"+id.toLowerCase()); //$NON-NLS-1$
        	}catch (Exception ex){ex.printStackTrace();}
        }
        
        //getting the icon for this tool frame
        ImageIcon toolFrameIcon=SVGResource.getIcon(id+"Window", false); //$NON-NLS-1$
        
        //handling the menuitem and the toggle button used to show or hide this frame
        frameButton.setIcon(toolFrameIcon);        
        frameButton.setSelected(true);
        updateAttributePanelVisibility();
        
        //the runnable used to place the frame
        final Runnable runnable=new Runnable()
        {        	
        	public void run() 
        	{
    			//getting the location of the toggle button
    			Point location=SwingUtilities.convertPoint(frameButton.getParent(), frameButton.getLocation(), 
    																			editor.getDesktop());    			
    			toolPanel.setLocation(location.x, 0);
    			hasBeenShown=true;
        	}
        };
        
        //adding the listener to the frame button
        frameButton.addActionListener(new ActionListener(){
        	
        	public void actionPerformed(ActionEvent evt) 
        	{
        	    updateAttributePanelVisibility();
        	    
        	    toolPanel.setVisible(frameButton.isSelected());

                if(visibilityChangedRunnable!=null)
                {
                    visibilityChangedRunnable.run();
                }

                //toolPanel.pack();

                if(! hasBeenShown)
                {
                    runnable.run();
                }
        	}
        });
        
        //the listener to the menu item
        frameMenuItem.addActionListener(new ActionListener()
        {        	
        	public void actionPerformed(ActionEvent evt) 
        	{
        		frameButton.doClick();
        	}
        });        
    }
    
    private void updateAttributePanelVisibility()
    {
        if(frameButton.isSelected())
        {
            frameMenuItem.setText(hideFrameLabel);
            frameButton.setToolTipText(hideFrameLabel);
        }
        else
        {
            frameMenuItem.setText(showFrameLabel);
            frameButton.setToolTipText(showFrameLabel);
        }        
    }


    /**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
     * @return whether this tool frame is visible
     */
    public boolean isVisible(){
    	
    	return toolPanel.isVisible();
    }
    
    /**
     * sets whether the frame should be visible or not
     * @param visible
     */
    public void setVisible(boolean visible){
    	
    	toolPanel.setVisible(visible);
    }

	/**
	 * @return the tool bar button
	 */
	public JToggleButton getToolBarButton() {
		return frameButton;
	}

	/**
	 * @return the menu item t
	 */
	public JMenuItem getMenuItem() {
		return frameMenuItem;
	}

	/**
	 * @return Returns the editor.
	 */
	public SVGEditor getSVGEditor() {
		return editor;
	}
	
	/**
	 * revalidates the tool frame
	 */
	public void revalidate(){
		
		toolPanel.revalidate();
	}

	/**
	 * @param visibilityChangedRunnable The visibilityChangedRunnable to set.
	 */
	public void setVisibilityChangedRunnable(Runnable visibilityChangedRunnable) {
		
		this.visibilityChangedRunnable=visibilityChangedRunnable;
	}

}
