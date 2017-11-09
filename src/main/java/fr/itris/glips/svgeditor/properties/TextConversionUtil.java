package fr.itris.glips.svgeditor.properties;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TextConversionUtil
{
    /**
     * Creates multiline text under the given element (should be a "text" node) from the given documtn,
     * using tspans.
     * So - replaces all the children of the element with a structure with one tspan for each line
     * of the string value (which comes from the propertyValues map).
     * @param element
     * @param multilineText
     * @param doc
     */
    public static void putMultilineTextUnderElement(Element element, String multilineText, Document doc)
    {        
        String[] lines = multilineText.split("(\r\n?|\n)"); //$NON-NLS-1$
        
        //Remove existing child nodes
        NodeList childNodeList = element.getChildNodes();
        while(childNodeList.getLength() > 0)
        {
            element.removeChild(childNodeList.item(childNodeList.getLength() - 1));                        
        }
                                
        String xString = "0"; //making sure it has a value //$NON-NLS-1$
        xString = element.getAttribute("x"); //$NON-NLS-1$
                                
        for (int i = 0; i < lines.length; ++i)
        {
            Element tspanElement = doc.createElementNS(doc.getDocumentElement().getNamespaceURI(),"tspan"); //$NON-NLS-1$
            //make space preserving, so the user has some control
            Attr spaceAttr = doc.createAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:space"); //$NON-NLS-1$ //$NON-NLS-2$
            spaceAttr.setNodeValue("preserve"); //$NON-NLS-1$
            tspanElement.setAttributeNodeNS(spaceAttr);
            
            //add the text                              
            String thisLine = lines[i];                        
            //if they have added a blank line, reflect this in xml (need a text node for tspan to work)
            if (thisLine.isEmpty())
            {
                thisLine = " "; //$NON-NLS-1$
            }
            tspanElement.appendChild(doc.createTextNode(thisLine));
            tspanElement.setAttribute("x", xString); //$NON-NLS-1$
            if (i >0)
            {
                tspanElement.setAttribute("dy", "1.1em"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            element.appendChild(tspanElement);
        }                
    }    
    
    /**
     * Extracts multiline text from a structure of tspans or text nodes (or both) 
     * underneath the named given node.
     * <p>
     * Note that tspans which do not seem to be being used as separate lines, as there is no y-shift given by a dy attribute, 
     * are not treated as a new line. 
     * 
     * @param node
     * @return extracted multiline text
     */
    public static String extractMultilineTextUnderElement(Node node)
    {        
        StringBuilder sb = new StringBuilder();

        boolean firstLine = true;
        for(Node cur=node.getFirstChild(); cur!=null; cur=cur.getNextSibling()){
            
            // If subsequent tspans (after the first) have dy settings we assume these are new lines 
            // Apart from this we just concatenate over the tspans
            if(cur.getNodeName().equals("tspan")) //$NON-NLS-1$
            {                        
                Element curEl = (Element)cur;
                
                //Find out of yShifted
                boolean yShifted = false;                        
                String dy = curEl.getAttribute("dy");                         //$NON-NLS-1$
                if ( ! (dy == null || dy.isEmpty() || "0".equals(dy) ) ) { //$NON-NLS-1$
                    yShifted = true;
                }
                
                //implies this is a newline of text
                if ( yShifted && ! firstLine){
                    sb.append("\n");                                 //$NON-NLS-1$
                }                        
                firstLine = false;
                
                //append the text from the textnode(s)
                for(Node childNode=curEl.getFirstChild(); childNode!=null; childNode=childNode.getNextSibling()){
                    if ( ! "#text".equals(childNode.getNodeName())) { continue; } //$NON-NLS-1$
                    //when within a tspan, use the text as is (as space is preserved)
                    String line = childNode.getNodeValue();                        
                    sb.append(line);                              
                }                        
            }
            else if (cur.getNodeName().equals("#text")){ //$NON-NLS-1$
                String text = cur.getNodeValue();
                //Outside a tspan, normalise space when reading, but do not trim aggressively, 
                // as there may be necessary spaces at the end of the text node, e.g.:
                //<text x="200" y="150" fill="blue" font-family="Verdana" font-size="45">
                //You are <tspan font-weight="bold" fill="red" >not</tspan> a banana.</text>
                if (text != null && ! text.trim().isEmpty()) {
                    String normedText = text.replaceAll("\\s+", " ");                         //$NON-NLS-1$ //$NON-NLS-2$
                    sb.append(normedText);  
                }
            }
        }
      
        return sb.toString();
    }
    
}
