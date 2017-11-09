/*
 * Created on 23 juin 2004
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
package fr.itris.glips.svgeditor.domactions;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.resources.*;
import fr.itris.glips.svgeditor.undoredo.*;

/**
 * @author Jordi SUC
 *
 * The class allowing to change the size of a canvas
 */
public class SVGCanvasModifier extends SVGModuleAdapter
{	
	/**
	 * the ids of the module
	 */
	final private String idcanvasmodifier="CanvasModifier", idsizemodifier="CanvasSizeModifier"; //$NON-NLS-1$ //$NON-NLS-2$
	
	/**
	 * the labels
	 */
	private String 		labelcanvasmodifier="", labelcanvassizemodifier="",  //$NON-NLS-1$ //$NON-NLS-2$
	canvasmodifierlabelok="", canvasmodifierlabelcancel="", canvasmodifierlabelwidth="", canvasmodifierlabelheight="", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	canvasmodifierwarningmessage="", canvasmodifierwarningtitle=""; //$NON-NLS-1$ //$NON-NLS-2$
	
	/**
	 * the undo/redo labels
	 */
	private String undoredocanvassizemodifier=""; //$NON-NLS-1$
	
	/**
	 * the menu items that will be contained in the menu
	 */
	private JMenuItem canvasSizeModifier;
	
	/**
	 * the menu in which the menu items will be inserted
	 */
	private JMenu canvasModifier;
	
	/**
	 * the editor
	 */
	private SVGEditor editor;
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGCanvasModifier(SVGEditor editor){
		
		this.editor=editor;
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
			
			try{
				labelcanvasmodifier=bundle.getString("labelcanvasmodifier"); //$NON-NLS-1$
				labelcanvassizemodifier=bundle.getString("labelcanvassizemodifier"); //$NON-NLS-1$
				undoredocanvassizemodifier=bundle.getString("undoredocanvassizemodifier"); //$NON-NLS-1$
				canvasmodifierlabelok=bundle.getString("labelok"); //$NON-NLS-1$
				canvasmodifierlabelcancel=bundle.getString("labelcancel"); //$NON-NLS-1$
				canvasmodifierlabelwidth=bundle.getString("labelwidth"); //$NON-NLS-1$
				canvasmodifierlabelheight=bundle.getString("labelheight"); //$NON-NLS-1$
				canvasmodifierwarningmessage=bundle.getString("canvasmodifierwarningmessage"); //$NON-NLS-1$
				canvasmodifierwarningtitle=bundle.getString("canvasmodifierwarningtitle"); //$NON-NLS-1$
			}catch (Exception ex){}
		}
		
		//getting the icons
		ImageIcon canvasSizeModifierIcon=SVGResource.getIcon("CanvasSizeModifier", false), //$NON-NLS-1$
		dcanvasSizeModifierIcon=SVGResource.getIcon("CanvasSizeModifier", true); //$NON-NLS-1$
		
		//creates the menu items
		canvasSizeModifier=new JMenuItem(labelcanvassizemodifier, canvasSizeModifierIcon);
		canvasSizeModifier.setDisabledIcon(dcanvasSizeModifierIcon);
		canvasSizeModifier.setEnabled(false);
		
		//a listener that listens to the changes of the SVGFrames
		final ActionListener svgframeListener=new ActionListener(){
			
			public void actionPerformed(ActionEvent e) {
				
				SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
				
				//enables or disables the menu items
				if(frame!=null){
					
					canvasSizeModifier.setEnabled(true);
					
				}else {
					
					canvasSizeModifier.setEnabled(false);
				}
			}
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);
		
		//adds the listeners
		canvasSizeModifier.addActionListener(new CanvasSizeModifierActionListener());
		
		//adds the menu items to the menu
		canvasModifier=new JMenu(labelcanvasmodifier);
		canvasModifier.add(canvasSizeModifier);
	}
	
	/**
	 * @return the editor
	 */
	public SVGEditor getSVGEditor(){
		return editor;
	}
	
	/**
	 * @return a map associating a menu item id to its menu item object
	 */
	public Hashtable getMenuItems(){
		
		Hashtable menuItems=new Hashtable();
		menuItems.put(idcanvasmodifier, canvasModifier);
		
		return menuItems;
	}
	
	/**
	 * @return a map associating a tool item id to its tool item object
	 */
	public Hashtable getToolItems(){
		
		return null;
	}
	
	/**
	 * Returns the collection of the popup items
	 * @return the collection of the popup items
	 */
	public Collection getPopupItems(){
		
		return null;
	}
	
	/**
	 * gets the name of the module
	 * @return the name of the module
	 */
	public String getName(){
		return idcanvasmodifier;
	}
	
	/**
	 * cancels all the actions that could be running
	 */
	public void cancelActions(){
	}
	
	/**
	 * layout some elements in the module
	 */
	public void initialize(){
		
	}
	
	/**
	 * The class of the action that will be executed when clicking on resize menu item
	 */
	protected class CanvasSizeModifierActionListener implements ActionListener{
		
		/**
		 * the dialog box displayed when clicking on the resize menu item
		 */
		private CanvasSizeModifierDialog dialog;
		
		/**
		 * the constructor of the class
		 */
		protected CanvasSizeModifierActionListener(){
			
			//creates the dialog
			if(editor.getParent() instanceof JFrame){
				
				dialog=new CanvasSizeModifierDialog((JFrame)editor.getParent());
				
			}else{
				
				dialog=new CanvasSizeModifierDialog(new JFrame("")); //$NON-NLS-1$
			}

			//gets the buttons from the dialog box
			final JButton ok=dialog.getOKButton(), cancel=dialog.getCancelButton();
			
			//adds the listeners
			ok.addActionListener(new ActionListener(){
				
				public void actionPerformed(ActionEvent e){
					
					//tests if the size values are right
					if(dialog.isVisible() && dialog.getEnteredSize()!=null && dialog.getEnteredSize().getWidth()>0 && dialog.getEnteredSize().getHeight()>0){
						
						dialog.setVisible(false);
						
						//gets the current frame
						final SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
						final Point2D.Double newSize=new Point2D.Double(dialog.getEnteredSize().getWidth(), dialog.getEnteredSize().getHeight()),
														oldSize=frame.getScrollPane().getSVGCanvas().getGeometryCanvasSize();
						
						if(frame!=null){
							
							Runnable runnable=new Runnable(){
								
								public void run(){
									
									frame.getScrollPane().changeSize(newSize);
									
									//create the undo/redo action and insert it into the undo/redo stack
									if(editor.getUndoRedo()!=null){
										
										SVGUndoRedoAction action=new SVGUndoRedoAction("undoredocanvassizemodifier"){ //$NON-NLS-1$
											
											public void undo(){
												
												frame.getScrollPane().changeSize(oldSize);
											}
											
											public void redo(){
												
												frame.getScrollPane().changeSize(newSize);
											}
										};
										
										SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList(undoredocanvassizemodifier);
										actionlist.add(action);
										editor.getUndoRedo().addActionList(frame, actionlist);
									}
								}
							};
							
							frame.enqueue(runnable);
							frame.setModified(true);
						}
						
					}else if(dialog.isVisible()){
						
						//shows an error message
						JOptionPane.showMessageDialog(editor.getParent(), canvasmodifierwarningmessage, canvasmodifierwarningtitle, JOptionPane.WARNING_MESSAGE);
						dialog.toFront();
					}		
				}
			});
			
			cancel.addActionListener(new ActionListener(){
				
				public void actionPerformed(ActionEvent e){
					
					dialog.setVisible(false);
				}
			});
			
			final AWTEventListener keyListener=new AWTEventListener(){
				
				public void eventDispatched(AWTEvent e) {
					
					if(e instanceof KeyEvent && dialog.isVisible()){
						
						KeyEvent kev=(KeyEvent)e;
						
						if(kev.getKeyCode()==KeyEvent.VK_ENTER){
							
							ok.doClick();
							
						}else if(kev.getKeyCode()==KeyEvent.VK_CANCEL){
							
							cancel.doClick();
						}
					}
				}
			};
			
			Toolkit.getDefaultToolkit().addAWTEventListener(keyListener, AWTEvent.KEY_EVENT_MASK);
		}
		
		/**
		 * the method called when receiving an event
		 * @param e the event
		 */
		public void actionPerformed(ActionEvent e){
			
			getSVGEditor().cancelActions(true);
			
			//sets the location of the dialog box
			int 	x=(int)(editor.getParent().getLocationOnScreen().getX()+editor.getParent().getWidth()/2-dialog.getSize().getWidth()/2), 
			y=(int)(editor.getParent().getLocationOnScreen().getY()+editor.getParent().getHeight()/2-dialog.getSize().getHeight()/2);
			
			dialog.setLocation(x,y);
			
			//sets the initial values of the dialog
			//gets the current frame
			SVGFrame frame=getSVGEditor().getFrameManager().getCurrentFrame();
			
			if(frame!=null){
				
				//gets the related canvas
				SVGCanvas canvas=frame.getScrollPane().getSVGCanvas();
				
				if(canvas!=null){
					
					Point2D.Double oldSize=canvas.getGeometryCanvasSize();
					dialog.setDisplayedValues(oldSize);
				}
			}
			
			dialog.setVisible(true);
		}
		
		/**
		 * the class of the dialog that will be displayed to enter the value of the resizement
		 */
		protected class CanvasSizeModifierDialog extends JDialog{
			
			/**
			 * the textfields that will be used to enter the size values
			 */		
			private JTextField txtw=new JTextField(4), txth=new JTextField(4);
			
			/**
			 * the buttons used to cancel or validate the values entered
			 */
			private JButton ok=new JButton(canvasmodifierlabelok), cancel=new JButton(canvasmodifierlabelcancel);
			
			/**
			 * the constructor of the class
			 * @param parent the parent frame
			 */
			protected CanvasSizeModifierDialog(JFrame parent){
				
				super(parent);
				
				//creates the form
				JPanel panel=new JPanel();
				panel.setLayout(new GridLayout(2,2,5,5));
				
				JPanel pw=new JPanel();
				pw.setLayout(new BoxLayout(pw,BoxLayout.X_AXIS));
				
				JLabel pxw=new JLabel("px"); //$NON-NLS-1$
				pxw.setHorizontalAlignment(SwingConstants.LEFT);
				pw.add(txtw);
				pw.add(pxw);
				
				JPanel ph=new JPanel();
				ph.setLayout(new BoxLayout(ph,BoxLayout.X_AXIS));
				
				JLabel pxh=new JLabel("px"); //$NON-NLS-1$
				pxh.setHorizontalAlignment(SwingConstants.LEFT);
				ph.add(txth);
				ph.add(pxh);
				
				JLabel lbw=new JLabel(canvasmodifierlabelwidth+" :"); //$NON-NLS-1$
				lbw.setHorizontalAlignment(SwingConstants.RIGHT);	
				
				JLabel lbh=new JLabel(canvasmodifierlabelheight+" :"); //$NON-NLS-1$
				lbh.setHorizontalAlignment(SwingConstants.RIGHT);	
				
				panel.add(lbw);
				panel.add(pw);
				panel.add(lbh);
				panel.add(ph);
				
				JPanel buttons=new JPanel();
				buttons.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
				buttons.add(ok);
				buttons.add(cancel);
				
				JPanel pall=new JPanel();
				pall.setLayout(new BorderLayout());
				pall.add(panel, BorderLayout.CENTER);
				pall.add(buttons,BorderLayout.SOUTH);
				pall.setBorder(BorderFactory.createEmptyBorder(10,10,5,10));
				
				getContentPane().add(pall);
				
				setTitle(labelcanvassizemodifier);	
				pack();
			}
			
			/**
			 * gets the values entered
			 * @return the values entered
			 */
			protected Dimension getEnteredSize(){
				
				int x,y;
				
				try{
					x=Integer.parseInt(txtw.getText());
					y=Integer.parseInt(txth.getText());
				}catch (Exception e){return null;}
				
				return new Dimension(x,y);
			}
			
			/**
			 * sets the values displayed in the dialog
			 * @param size the values that will be displayed in the dialog
			 */
			protected void setDisplayedValues(Point2D.Double size){
				
				txtw.setText(""+(int)size.getX()); //$NON-NLS-1$
				txth.setText(""+(int)size.getY()); //$NON-NLS-1$
			}
			
			/**
			 * @return the ok button
			 */
			protected JButton getOKButton(){
				return ok;
			}
			
			/**
			 * @return the ok button
			 */
			public JButton getCancelButton(){
				return cancel;
			}
		}
	}
}
