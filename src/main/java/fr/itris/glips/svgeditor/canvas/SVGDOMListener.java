/*
 * Created on 7 juin 2005
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

import java.lang.ref.*;

import org.w3c.dom.*;

/**
 * the listener to the dom elements
 * 
 * @author Jordi SUC
 */
public abstract class SVGDOMListener {
    
    /**
     * the reference of the node that is listened to
     */
    protected WeakReference<Node> nodeRef=null;
    
    /**
     * the reference of the frame the listener is linked to
     */
    protected WeakReference<SVGFrame> frameRef=null;
    
    /**
     * the constructor of the class
     * @param node the node that is listened
     */
    public SVGDOMListener(Node node) {
        
        nodeRef=new WeakReference<Node>(node);
    }
    
    /**
	 * @param frame the frame to set
	 */
	public void setFrame(SVGFrame frame) {
		frameRef=new WeakReference<SVGFrame>(frame);
	}
	
	/**
	 * @return the frame
	 */
	public SVGFrame getFrame() {
		
		if(frameRef!=null) {
			
			return frameRef.get();
		}
		
		return null;
	}
	
	/**
	 * removes this dom listener
	 */
	public void removeListener() {
		
		if(frameRef!=null) {
			
			frameRef.get().removeDOMListener(this);
			frameRef=null;
			nodeRef=null;
		}
	}

	/**
     * @return the node the listener is listening to
     */
    public Node getNode() {
    	
    	if(nodeRef!=null) {
    		
    		return nodeRef.get();
    	}
    	
        return null;
    }

    /**
	 * notifies that the given node has been changed
	 */
	public abstract void nodeChanged();
	
	/**
	 * notifies that the given node has been inserted under the node of the listener
	 * @param insertedNode a node
	 */
	public abstract void nodeInserted(Node insertedNode);
	
	/**
	 * notifies that the given node has been removed from the node of the listener
	 * @param removedNode a node
	 */
	public abstract void nodeRemoved(Node removedNode);
	
	/**
	 * notifies that the sub tree from which the root can been found has been modified
     * @param lastModifiedNode the last node that has been modified
	 */
	public abstract void structureChanged(Node lastModifiedNode);
}
