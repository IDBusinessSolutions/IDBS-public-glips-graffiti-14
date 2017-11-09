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

/**
 * @author Jordi SUC
 * the class of the undo/redo actions
 */
public abstract class SVGUndoRedoAction {

	/**
	 * the action's name
	 */
	private String name=""; //$NON-NLS-1$
	
	/**
	 * the construcor of the class
	 * @param name a string
	 */
	public SVGUndoRedoAction(String name){
		this.name=name;	
	}
	
	/**
	 * @return the action's name
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * @param name the action's name
	 *
	 */
	public void setName(String name){
		this.name=name;
	}
	
	/**
	 * used to call all the actions that have to be done to undo an action
	 *
	 */
	public void undo(){}
	
	/**
	 * used to call all the actions that have to be done to redo an action
	 *
	 */
	public void redo(){}
	
	@Override
    public String toString() {
       
        return getName();
    }

}
