/*
 * Created on 7 avr. 2004
 *
 =============================================
                   GNU LESSER GENERAL PUBLIC LICENSE Version 2.1
 =============================================
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
package fr.itris.glips.svgeditor.selection;

import java.awt.*;
import java.awt.geom.*;

import org.w3c.dom.*;

/**
 * @author Jordi SUC
 *
 * the class representing one of the squares displayed when a node is selected
 */
public class SVGSelectionSquare{

    private Node node;
    private String type;
    private Rectangle2D.Double rect;
    private Cursor cursor;

    /**
     * the constructor of the class
     * @param node the selected node
     * @param type the type of the selection
     * @param rect the position and size of the square
     * @param cursor the cursor associated with this square
     */
    public SVGSelectionSquare(Node node, String type, Rectangle2D.Double rect, Cursor cursor){
        
        this.node=node;
        this.type=type;
        this.rect=rect;
        this.cursor=cursor;
    }

    /**
     * @return the selected node
     */
    public Node getNode(){
        return node;
    }

    /**
     * @return the type of the selection
     */
    public String getType(){
        return type;
    }

    /**
     * @return the position and size of the square
     */
    public Rectangle2D.Double getRectangle(){
        return rect;
    }

    /**
     * @return the cursor associated with the square
     */
    public Cursor getCursor(){
        return cursor;
    }
}
