/*
 * Created on 24 mars 2004
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

import java.io.*;
import java.util.*;

import javax.swing.filechooser.FileFilter;

import fr.itris.glips.svgeditor.*;

/**
 * @author Jordi SUC
 * the class for filtering files in a JFileChooser
 */
public class SVGFileFilter extends FileFilter {

	/**
	 * the constructor of the class
	 */
	public SVGFileFilter() {
	    
		super();
	}

	@Override
	public boolean accept(File f) {
	    
	    String name=f.getName();
	    name=name.toLowerCase();
	    
		if(		f.isDirectory() || name.endsWith(SVGToolkit.SVG_FILE_EXTENSION) ||
				name.endsWith(SVGToolkit.SVGZ_FILE_EXTENSION)){
		    
		    return true;
		    
		}else{
		    
		    return false;
		}
	}

	@Override
	public String getDescription() {
	    
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		String messagefilter=""; //$NON-NLS-1$
		
		if(bundle!=null){
		    
			try{
				messagefilter=bundle.getString("messagefilter"); //$NON-NLS-1$
			}catch (Exception ex){}
		}
		
		return messagefilter;
	}
	
	/**
	 * Whether the given file is accepted by this filter
	 * @param f a file
	 * @return true to accept the file
	 */
	public boolean acceptFile(File f) {
	    
	    String name=f.getName().toLowerCase();
	    
		if(name.endsWith(SVGToolkit.SVG_FILE_EXTENSION) || name.endsWith(SVGToolkit.SVGZ_FILE_EXTENSION)){
		    
		    return true;
		}
		
		return false;
	}

}
