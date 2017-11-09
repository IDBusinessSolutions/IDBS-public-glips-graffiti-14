/*
 * Created on 1 juin 2004
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
package fr.itris.glips.svgeditor.resources;

import java.awt.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.*;

/**
 * @author Jordi SUC
 * the class of the cursors of the application
 */
public class SVGCursors{

	/**
	 * the hashtable of the cursors
	 */
	private Hashtable contents=new Hashtable();
	
	/**
	 * the hashtable of the already loaded images
	 */
	private Hashtable images=new Hashtable();
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGCursors(SVGEditor editor){
	    
		super();
		
		//gets the name of the cursors from the resources
		ResourceBundle bundle=null;
		
		try{
			bundle=ResourceBundle.getBundle("fr.itris.glips.svgeditor.resources.properties.SVGEditorCursors"); //$NON-NLS-1$
		}catch (Exception ex){bundle=null;}
		
		//the keys enumeration of the resources
		Enumeration keys=null;
		
		if(bundle!=null){
		    
			keys=bundle.getKeys();
		}
		
		String key="", value="", shortKey="", path=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		Cursor c=null;
		ImageIcon imgicn=null;
		
		//tells which size is better for the cursor images or if the cutom cursors option can't be used
		Dimension bestSize=Toolkit.getDefaultToolkit().getBestCursorSize(24,24);
		
		if(keys!=null){
		    
			Image img=null;
		    
			//for each key string
			while(keys.hasMoreElements()){
			    
				key=(String)keys.nextElement();
				
				if(key!=null){
				    
					//the key value without the prefix that tells whether the cursor is a default or a custom cursor
					shortKey=key.substring(key.indexOf('_')+1,key.length());
					
					if(bestSize.width!=0 && key.startsWith("custom_")){ //$NON-NLS-1$
					    
						value=(String)bundle.getObject(key);
						
						if(value!=null){

							if(images.containsKey(value)){
							    
								//the images have already been loaded, it is taken from the hashtable
								img=(Image)images.get(value);
								
							}else{
							    
								try{
									//load the images 
									path=SVGResource.getPath(value);
									
									if(path!=null && ! path.equals("")){ //$NON-NLS-1$
									    
										imgicn=new ImageIcon(new URL(path));
										img=imgicn.getImage();
									}
								}catch (Exception ex){img=null;}
							}
							
							if(img!=null){
							    
								//creates the custom cursor
								c=Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(5,5), shortKey);
								
								if(c!=null){
								    
									contents.put(shortKey, c);
								}
							}
						}
						
					}else if( ! contents.containsKey(shortKey) && key.startsWith("default_")){ //$NON-NLS-1$
					    
						value=(String)bundle.getObject(key);
						
						if(value!=null && ! value.equals("") ){ //$NON-NLS-1$
						    
							//gets the cursor that is defined by default in the Java implementation
							c=getDefaultCursor(value);
							
							if(c!=null){
							    
								contents.put(shortKey, c);
							}
						}
					}
				}	
			}
		}
	}

	/**
	 * @param name the name of the cursor
	 * @return the accurate cursor, the returned value is never null
	 */
	public Cursor getCursor(String name){
	    
		Cursor c=null;
		
		if(name!=null && ! name.equals("") && contents.containsKey(name)){ //$NON-NLS-1$
		    
			try{c=(Cursor)contents.get(name);}catch (Exception ex){c=null;}
		}
		
		//if the cursor found is valid, the cursor is returned
		if(c!=null){
		    
		    return c;
		    
		}else{
		    //otherwise the default cursor is returned
		    return new Cursor(Cursor.DEFAULT_CURSOR);
		}
	}
	
	/**
	 * gets the accurate cursor given a name
	 * @param name the name of a cursor
	 * @return the cursor
	 */
	protected Cursor getDefaultCursor(String name){
		
		if(name!=null && ! name.equals("")){ //$NON-NLS-1$
		    
			if(name.equals("CROSSHAIR_CURSOR")){ //$NON-NLS-1$
			    
				return new Cursor(Cursor.CROSSHAIR_CURSOR);
				
			}else if(name.equals("DEFAULT_CURSOR")){ //$NON-NLS-1$
			    
				return new Cursor(Cursor.DEFAULT_CURSOR);
				
			}else if(name.equals("E_RESIZE_CURSOR")){ //$NON-NLS-1$
			    
				return new Cursor(Cursor.E_RESIZE_CURSOR);
				
			}else if(name.equals("HAND_CURSOR")){ //$NON-NLS-1$
			    
				return new Cursor(Cursor.HAND_CURSOR);
				
			}else if(name.equals("MOVE_CURSOR")){ //$NON-NLS-1$
			    
				return new Cursor(Cursor.MOVE_CURSOR);
				
			}else if(name.equals("N_RESIZE_CURSOR")){ //$NON-NLS-1$
			    
				return new Cursor(Cursor.N_RESIZE_CURSOR);
				
			}else if(name.equals("NE_RESIZE_CURSOR")){ //$NON-NLS-1$
			    
				return new Cursor(Cursor.NE_RESIZE_CURSOR);
				
			}else if(name.equals("NW_RESIZE_CURSOR")){ //$NON-NLS-1$
			    
				return new Cursor(Cursor.NW_RESIZE_CURSOR);
				
			}else if(name.equals("S_RESIZE_CURSOR")){ //$NON-NLS-1$
			    
				return new Cursor(Cursor.S_RESIZE_CURSOR);
				
			}else if(name.equals("SE_RESIZE_CURSOR")){ //$NON-NLS-1$
			    
				return new Cursor(Cursor.SE_RESIZE_CURSOR);
				
			}else if(name.equals("SW_RESIZE_CURSOR")){ //$NON-NLS-1$
			    
				return new Cursor(Cursor.SW_RESIZE_CURSOR);
				
			}else if(name.equals("TEXT_CURSOR")){ //$NON-NLS-1$
			    
				return new Cursor(Cursor.TEXT_CURSOR);
				
			}else if(name.equals("W_RESIZE_CURSOR")){	 //$NON-NLS-1$
			    
				return new Cursor(Cursor.W_RESIZE_CURSOR);
				
			}else if(name.equals("WAIT_CURSOR")){ //$NON-NLS-1$
			    
				return new Cursor(Cursor.WAIT_CURSOR);
			}
		}
		
		return null;
	}

}
