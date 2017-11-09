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
package fr.itris.glips.svgeditor.shape;

import java.util.*;

import fr.itris.glips.svgeditor.*;

/**
 * @author Jordi SUC
 * the class that allows to select any element on the canvas
 */
public class SVGAny extends SVGShape{

	/**
	 * the reference of an object of this class
	 */
	private final SVGAny any=this;
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGAny(SVGEditor editor) {
		super(editor);
		
		ids.put("id","any"); //$NON-NLS-1$ //$NON-NLS-2$
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labels.put("undoredotranslate", bundle.getString("shapeundoredotranslate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredoresize", bundle.getString("shapeanyundoredoresize")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredorotate", bundle.getString("shapeanyundoredorotate")); //$NON-NLS-1$ //$NON-NLS-2$
			}catch (Exception ex){}
		}
	}
	
}
