/*
 * Created on 2 avr. 2004
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
package fr.itris.glips.svgeditor.undoredo;

import java.util.*;

/**
 * @author Jordi SUC
 * the class of the list of SVGUndoRedoActions
 */
public class SVGUndoRedoActionList extends LinkedList<SVGUndoRedoAction> {

	/**
	 * the name of the action list
	 */
	private String name=""; //$NON-NLS-1$
	
	/**
	 * the constructor of the class
	 * @param name  the name of the action list
	 */
	public SVGUndoRedoActionList(String name) {
		super();
		this.name=name;
	}
	
	/**
	 * @return the name of the action list
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * sets the name of the action list
	 * @param name the name of the action list
	 */
	public void setName(String name){
		this.name=name;
	}

	@Override
    public synchronized String toString() {
        
        String toString=getName().concat("=["); //$NON-NLS-1$
        
        for(Iterator it=iterator(); it.hasNext();){
            
            toString=toString.concat(it.next().toString().concat(", ")); //$NON-NLS-1$
        }
        
        toString=toString.concat("]"); //$NON-NLS-1$
        
        return toString;
    }
	
}
