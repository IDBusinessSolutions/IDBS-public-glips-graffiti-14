/*
 * Created on 18 juil. 2004
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

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import fr.itris.glips.svgeditor.menutool.*;
import fr.itris.glips.svgeditor.resources.*;

/**
 * the class used to display the "about" dialog
 * @author Jordi SUC
 *
 */
public class SVGAbout extends SVGModuleAdapter{
	
	/**
	 * the id
	 */
	private String idAbout="About"; //$NON-NLS-1$

	/**
	 * the labels
	 */
	private String labelAbout=""; //$NON-NLS-1$
	
	/**
	 * the menu item
	 */
	private final JMenuItem about=new JMenuItem();
	
	/**
	 * the resource bundle
	 */
	private ResourceBundle bundle=null;
	
	/**
	 * the editor
	 */
	private SVGEditor editor;
	
	/**
	 * the listener to the about menu items
	 */
	private ActionListener aboutListener=null;

	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGAbout(SVGEditor theEditor)
    {
		this.editor = theEditor;
		
		//the resource bundle
        bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
		    try{
		        labelAbout=bundle.getString("labelabout"); //$NON-NLS-1$
		    }catch (Exception ex){}
		}
		
		//getting the icons
		ImageIcon aboutIcon=SVGResource.getIcon("About", false), //$NON-NLS-1$
						daboutIcon=SVGResource.getIcon("About", true); //$NON-NLS-1$
		
		//the menuitem
		about.setText(labelAbout);
		about.setIcon(aboutIcon);
		about.setDisabledIcon(daboutIcon);
		
		//the dialog
		final AboutDialog aboutDialog=new AboutDialog();
		
		//creating the listener to the menu item
		aboutListener=new ActionListener()
        {		   
			public void actionPerformed(ActionEvent evt) 
            {			    
				//sets the location of the dialog box
				int 	x=(int)(editor.getParent().getLocationOnScreen().getX()+editor.getParent().getWidth()/2-aboutDialog.getSize().getWidth()/2), 
						y=(int)(editor.getParent().getLocationOnScreen().getY()+editor.getParent().getHeight()/2-aboutDialog.getSize().getHeight()/2);
				
				aboutDialog.setLocation(x,y);
			    aboutDialog.setVisible(true);
			}
		};
		
		about.addActionListener(aboutListener);
	}

	@Override
	public Hashtable<String, JMenuItem> getMenuItems(){
		
		Hashtable<String, JMenuItem> menuItems=new Hashtable<String, JMenuItem>();
		menuItems.put(idAbout, about);
		
		return menuItems;
	}
	
	@Override
	public Collection<SVGPopupItem> getPopupItems(){
		
		LinkedList<SVGPopupItem> popupItems=new LinkedList<SVGPopupItem>();
		
		//creating the about popup item
		SVGPopupItem item=new SVGPopupItem(getSVGEditor(), idAbout, labelAbout, "About"){ //$NON-NLS-1$
		
			@Override
			public JMenuItem getPopupItem(LinkedList nodes) {
				
				//adds the action listeners
				menuItem.addActionListener(aboutListener);
				
				return super.getPopupItem(nodes);
			}
		};
		
		popupItems.add(item);

		return popupItems;
	}
	
	/**
	 * @return the editor
	 */
	public SVGEditor getSVGEditor(){
		return editor;
	}
	
	/**
	 * the dialog that will be shown
	 * @author Jordi SUC
	 */
	protected class AboutDialog extends JDialog{
	    
		/**
		 * the dialog frame
		 */
		private final JDialog dialog=this;
	    
		/**
		 * the constructor of the class
		 */
	    protected AboutDialog(){
	        
	        setTitle(labelAbout);
	        
	        JPanel panel=new JPanel();
	        panel.setLayout(new BorderLayout());
	        
	        final ImageIcon image=SVGResource.getIcon("Splash", false); //$NON-NLS-1$

	        if(image!=null){

	            //creating the panel that will contain the image
	            JPanel imagePanel=new JPanel(){

	            	@Override
                    protected void paintComponent(Graphics evt) {

                        super.paintComponent(evt);
                        image.paintIcon(this, evt, 0, 0);
                    }
	            };
	            
	            imagePanel.setLayout(null);
	            imagePanel.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
	            
	            imagePanel.setPreferredSize(new Dimension(image.getIconWidth(), image.getIconHeight()));
	            panel.add(imagePanel, BorderLayout.CENTER);
	        }
	        
	        //creating the panel that will contain the button
	        JPanel buttonPanel=new JPanel();
	        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
	        panel.add(buttonPanel, BorderLayout.SOUTH);
	        
	        //creating the ok button
	        String okLabel=""; //$NON-NLS-1$
	        
			//gets the labels from the resources
			
			if(bundle!=null){
			    
		        try{
		            okLabel=bundle.getString("labelok"); //$NON-NLS-1$
		        }catch (Exception ex){}
			}
	        
	        final JButton okButton=new JButton(okLabel);
	        buttonPanel.add(okButton);
	        
	        //adding the listeners
	        ActionListener actionListener=new ActionListener(){
	            
	            public void actionPerformed(ActionEvent arg0) {
	                
	                dialog.setVisible(false);
	            } 
	        };
	        
	        okButton.addActionListener(actionListener);
	        
	        final AWTEventListener keyListener=new AWTEventListener(){
	            
	            public void eventDispatched(AWTEvent e) {
	                
	                if(e instanceof KeyEvent && dialog.isVisible()){
	                    
	                    KeyEvent kev=(KeyEvent)e;

		                if(kev.getID()==KeyEvent.KEY_PRESSED && kev.getKeyCode()==KeyEvent.VK_ENTER){
		                    
		                    okButton.doClick();
		                }
	                }
	            }
	        };
	        
	        Toolkit.getDefaultToolkit().addAWTEventListener(keyListener, AWTEvent.KEY_EVENT_MASK);
	        
	        getContentPane().add(panel);
	        pack();
	    }
	}
}
