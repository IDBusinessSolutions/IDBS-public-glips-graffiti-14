package fr.itris.glips.svgeditor;

import java.util.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.menutool.*;

/**
 * the interface that each module must implement
 * @author jordi
 *
 */
public interface SVGModule {

	/**
	 * @return a map associating a menu item id to its menu item object
	 */
	public Hashtable<String, JMenuItem> getMenuItems();
	
	/**
	 * Returns the list of the popup items
	 * @return the list of the popup items
	 */
	public Collection<SVGPopupItem> getPopupItems();
	
	/**
	 * @return a map associating a tool item id to its tool item object
	 */
	public Hashtable<String, JToggleButton> getToolItems();
	
	/**
	 * @return a map associating a tool item id to its tool item object
	 */
	public SVGToolFrame getToolFrame();
	
	/**
	 * cancels all the actions that could be running
	 */
	public void cancelActions();
	
	/**
	 * initializes the module
	 */
	public void initialize();
}
