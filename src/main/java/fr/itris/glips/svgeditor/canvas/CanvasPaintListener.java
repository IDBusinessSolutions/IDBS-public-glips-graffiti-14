package fr.itris.glips.svgeditor.canvas;

import java.awt.*;

/**
 * 
 * @author Jordi SUC
 *
 * The interface of the paint listener
 */
public interface CanvasPaintListener {

	/**
	 * the action that has to be done
	 * @param g the graphics object
	 */
	public void paintToBeDone(Graphics g);
}
