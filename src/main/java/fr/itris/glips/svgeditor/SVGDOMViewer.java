/*
 * Created on 10 déc. 2004
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

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.apache.batik.apps.svgbrowser.DOMViewer;
import org.apache.batik.apps.svgbrowser.DOMViewerController;
/**
 * @author Jordi SUC
 *
 * the module displaying a dom viewer
 */
public class SVGDOMViewer extends SVGModuleAdapter{
    
	/**
	 * the id
	 */
	private String idDOMViewer="DOMViewer"; //$NON-NLS-1$

	/**
	 * the labels
	 */
	private String labelDOMViewer=""; //$NON-NLS-1$
	
	/**
	 * the menu item
	 */
	private final JMenuItem domViewerMenuitem=new JMenuItem();
	
	/**
	 * the resource bundle
	 */
	private ResourceBundle bundle=null;
	
	/**
	 * the dom viewer
	 */
	private DOMViewer domViewerDialog=new DOMViewer((DOMViewerController) this);	
	/**
	 * the editor
	 */
	private SVGEditor editor;
    
    /**
     * the constructor of the class
	 * @param editor the editor
     */
    public SVGDOMViewer(SVGEditor theEditor) 
    {
		this.editor = theEditor;

		//the resource bundle
        bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
		    try{
		    	labelDOMViewer=bundle.getString("labeldomviewer"); //$NON-NLS-1$
		    }catch (Exception ex){}
		}
		
		//a listener that listens to the changes of the SVGFrames
		final ActionListener svgframeListener=new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				if(domViewerDialog.isVisible()){
					
					if(getSVGEditor().getFrameManager().getCurrentFrame()!=null){
						
						//sets the document for the dom viewer
						domViewerDialog.setDocument(
								getSVGEditor().getFrameManager().getCurrentFrame().
								getScrollPane().getSVGCanvas().getDocument());
						
					}else{
						
						domViewerDialog.setVisible(false);
					}
				}
			}
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);
		
		//the menuitem
		domViewerMenuitem.setText(labelDOMViewer);
		
		domViewerMenuitem.addActionListener(
				
			new ActionListener(){
				
				public void actionPerformed(ActionEvent arg0) {
					
					if(! domViewerDialog.isVisible()){
						
						if(getSVGEditor().getFrameManager().getCurrentFrame()!=null){
							
							//sets the document for the dom viewer
							domViewerDialog.setDocument(getSVGEditor().getFrameManager().
									getCurrentFrame().getScrollPane().getSVGCanvas().getDocument());
						}
							
						//sets the location of the dialog box
						int 	x=(int)(editor.getParent().getLocationOnScreen().getX()+editor.getParent().getWidth()/2-domViewerDialog.getSize().getWidth()/2), 
								y=(int)(editor.getParent().getLocationOnScreen().getY()+editor.getParent().getHeight()/2-domViewerDialog.getSize().getHeight()/2);

						domViewerDialog.setLocation(x,y);
						domViewerDialog.setVisible(true);
					}
				}
			}
		);
    }
    
	/**
	 * @return the editor
	 */
	public SVGEditor getSVGEditor(){
		return editor;
	}
    
	@Override
	public Hashtable<String, JMenuItem> getMenuItems(){
		
		Hashtable<String, JMenuItem> menuItems=new Hashtable<String, JMenuItem>();
		menuItems.put(idDOMViewer, domViewerMenuitem);
		
		return menuItems;
	}
}

