package fr.itris.glips.svgeditor.menutool;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.resources.*;

/**
 * @author Jordi SUC
 *
 * the class creating and managing the frames toolbar
 */
public class FramesToolBar extends JToolBar{
	
	/**
	 * the editor
	 */
	private SVGEditor editor;
	
	/**
	 * the set of the tool frames
	 */
	private HashSet<SVGToolFrame> toolFrames=new HashSet<SVGToolFrame>();
	
	/**
	 * the list of the ids of the tool items that have to be added to the tool bar
	 */
	private final LinkedList<String> toolNames=new LinkedList<String>();
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public FramesToolBar(SVGEditor editor){
		
		super(SwingConstants.HORIZONTAL);
		
		this.editor=editor;
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		//setting the properties of the tool bar
		setRollover(true);
		setFloatable(true);
		setBorderPainted(false);
		setMargin(new Insets(0, 0, 0, 0));
		
		findModuleToolFrames();
		parseXMLTools();
		build();
	}
	
	/**
	 * @return editor the editor
	 */
	public SVGEditor getSVGEditor(){
		return editor;
	}
	
	/**
	 * gets all the tool frames from the modules
	 */
	protected void findModuleToolFrames(){
		
		Collection modules=getSVGEditor().getSVGModuleLoader().getModules();
		SVGModule module=null;
		SVGToolFrame toolFrame=null;
		
		for(Iterator it=modules.iterator(); it.hasNext();){

			module=(SVGModule)it.next();
			
			if(module!=null){
			    
				toolFrame=module.getToolFrame();
			    
			    if(toolFrame!=null){
			      
			    	toolFrames.add(toolFrame);
			    }
			}
		}
	}
	
	/**
	 * builds this toolbar
	 */
	protected void build(){

		for(String toolName : toolNames){
			
			for(SVGToolFrame toolFrame : toolFrames){
				
				if(toolFrame.getId().equals(toolName)){
					
					add(toolFrame.getToolBarButton());
				}
			}
		}
		
		 addSeparator();
	}
	
	/**
	 *parses the document to get the items order specified in the "tools.xml" file
	 */
	protected void parseXMLTools(){

		Document doc=SVGResource.getXMLDocument("tool.xml"); //$NON-NLS-1$
		
		if(doc!=null){
		    
		    Element root=doc.getDocumentElement();
		    
		    if(root!=null){
		        
				String nameAttr=""; //$NON-NLS-1$
				
				//getting the element of the svg tools
				NodeList nodes=root.getElementsByTagName("toolFrames"); //$NON-NLS-1$
				
				if(nodes!=null && nodes.getLength()>0){
					
					Element svgTools=(Element)nodes.item(0);
					
					for(Node current=svgTools.getFirstChild(); current!=null; current=current.getNextSibling()){
						
						if(current instanceof Element && current.getNodeName().equals("button")){ //$NON-NLS-1$

							nameAttr=((Element)current).getAttribute("name"); //$NON-NLS-1$
								
							if(nameAttr!=null && ! nameAttr.equals("")){ //$NON-NLS-1$
									
								toolNames.add(nameAttr);
							}
						}
					}
				}
		    }
		}
	}
}
