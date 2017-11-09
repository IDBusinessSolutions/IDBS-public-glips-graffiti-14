package fr.itris.glips.svgeditor.menutool;

import java.awt.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.*;

/**
 * the class of the tool bars of the editor
 * @author Jordi SUC
 */
public class SVGEditorToolbars extends JMenuBar{

	/**
	 * the color tool bar
	 */
	private ColorToolBar colorToolBar=null;
	
	/**
	 * the svg tool bar
	 */
	private SVGToolBar svgToolBar=null;
	
	/**
	 * the frames tool bar
	 */
	private FramesToolBar framesToolBar=null;

	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGEditorToolbars(SVGEditor editor) {

		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		//creating the tool bars
		colorToolBar=new ColorToolBar(editor);
		colorToolBar.setOpaque(false);
		add(colorToolBar);
		svgToolBar=new SVGToolBar(editor);
		svgToolBar.setOpaque(false);
		add(svgToolBar);
		framesToolBar=new FramesToolBar(editor);
		framesToolBar.setOpaque(false);
		add(framesToolBar);
		//TODO setPreferredSize(new Dimension(100, 77));
	}
	
	/**
	 * lays out the elements
	 */
	public void layoutElements(){
		
		colorToolBar.layoutElements();
		svgToolBar.layoutElements();

	}
	
	/**
	 * adds a new tool bar
	 * @param toolbar a tool bar
	 */
	public void addToolBar(JToolBar toolbar) {
		
		add(toolbar);
		toolbar.setOpaque(false);
		revalidate();
	}
	
	/**
	 * cancels the actions
	 */
	public void cancelActions(){
		
		svgToolBar.cancelActions();
	}
	
}
