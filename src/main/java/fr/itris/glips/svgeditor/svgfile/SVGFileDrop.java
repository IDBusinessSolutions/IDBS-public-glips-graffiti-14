/*
 * Created on 26 mars 2005
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
package fr.itris.glips.svgeditor.svgfile;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.*;

/**
 * the module for opening files by a drop action on the main frame
 * 
 * @author Felipe Rech Meneguzzi, Jordi SUC
 */
public class SVGFileDrop extends SVGModuleAdapter{

	/**
	 * the id
	 */
	private String idFileDrop="FileDrop"; //$NON-NLS-1$
	
	/**
	 * the editor
	 */
	private SVGEditor editor;
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGFileDrop(SVGEditor editor) {

		this.editor=editor;
		
		//creating the listener to the drop actions on the main frame of the editor
		DropTargetAdapter dropTargetAdapter=new DropTargetAdapter(){
			
		    public void drop(DropTargetDropEvent dtde) {
		    	
		        try {
		            Transferable tr=dtde.getTransferable();
		            String filename=null;
		            File svgFile=null;

		            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

		            if(dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
		            	
		            	//if the transferable contains a string//
		            	
		            	//getting the file name contained in the transferable
		                filename=(String) tr.getTransferData(DataFlavor.stringFlavor);
		                svgFile=new File(filename);
		                
		            }else if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
		            	
		            	//if the transferable contains a file list//
		                java.util.List files=(java.util.List) tr.getTransferData(DataFlavor.javaFileListFlavor);
		                
		                //getting the module enabling to open a file
		                SVGNewOpen svgNewOpen=(SVGNewOpen) getSVGEditor().getModule("NewOpen"); //$NON-NLS-1$
		                
		                if(svgNewOpen!=null){
		                	
			                //for each file in the list, the file is opened in the editor
			                for(Iterator it=files.iterator(); it.hasNext(); ) {
			                	
			                    svgFile=(File)it.next();
			                    
			                    if(svgFile!=null && svgFile.exists()) {

			                        svgNewOpen.open(svgFile);
			                        dtde.dropComplete(true);
			                        
			                    }else{
			                    	
			                    	dtde.rejectDrop();
			                    } 
			                }
		                }

		            }else {

		                dtde.rejectDrop();
		                return;
		            }
		            
		        } catch (Exception ex) {}
		    }
		};
		
		Container target=editor.getParent();
		
		if(editor.getParent() instanceof JFrame){
			
			target=((JFrame)editor.getParent()).getContentPane();
			
		}else if(editor.getParent() instanceof JApplet){
			
			target=((JApplet)editor.getParent()).getContentPane();
		}
		
		//adding the drag and drop support to the main frame of the editor
		DropTarget dropTarget=new DropTarget(target, DnDConstants.ACTION_COPY_OR_MOVE, dropTargetAdapter);
		editor.getParent().setDropTarget(dropTarget);
	}
	
	/**
	 * @return a map associating a menu item id to its menu item object
	 */
	public Hashtable getMenuItems(){
		
		return null;
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
		
		return null;
	}
	
	/**
	 * @return the editor
	 */
	public SVGEditor getSVGEditor(){
		return editor;
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
	
	/**
	 * gets the name of the module
	 * @return the name of the module
	 */
	public String getName(){
	    
		return idFileDrop;
	}

}
