/*
 * Created on 25 mai 2004
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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.properties.TextConversionUtil;
import fr.itris.glips.svgeditor.resources.*;
import fr.itris.glips.svgeditor.selection.*;
import fr.itris.glips.svgeditor.undoredo.*;

/**
 * @author Jordi SUC
 * the class that allows to add, select,modify the properties, delete, transform a text on the canvas
 */
public class SVGText extends SVGShape{
	
	/**
	 * the reference of an object of this class
	 */
	private final SVGText svgText=this;
	
	/**
	 * the action listener used to draw the text
	 */
	private TextActionListener textAction=null;

	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGText(SVGEditor editor) {
	    
		super(editor);
		
		ids.put("id","text"); //$NON-NLS-1$ //$NON-NLS-2$
		ids.put("idmenuitem","Text"); //$NON-NLS-1$ //$NON-NLS-2$
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labels.put("label", bundle.getString("shapetextlabel")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredocreate", bundle.getString("shapetextundoredocreate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredotranslate", bundle.getString("shapeundoredotranslate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredoresize", bundle.getString("shapetextundoredoresize")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("undoredorotate", bundle.getString("shapetextundoredorotate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("helpcreate", bundle.getString("shapetexthelpcreate")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("initdialogtitle", bundle.getString("shapetextinitdialogtitle")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("initdialoghelp", bundle.getString("shapetextinitdialoghelp")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("initok", bundle.getString("labelok")); //$NON-NLS-1$ //$NON-NLS-2$
				labels.put("initcancel", bundle.getString("labelcancel")); //$NON-NLS-1$ //$NON-NLS-2$
			}catch (Exception ex){}
		}
		
		//the icons
		icon=SVGResource.getIcon((String)ids.get("idmenuitem"), false); //$NON-NLS-1$
		disabledIcon=SVGResource.getIcon((String)ids.get("idmenuitem"), true); //$NON-NLS-1$
		
		//the menu item
		menuitem=new JMenuItem((String)labels.get("label"), icon); //$NON-NLS-1$
		menuitem.setDisabledIcon(disabledIcon);
		menuitem.setEnabled(false);
		textAction=new TextActionListener();
		
		//the toggle button
		toolItem=new JToggleButton(disabledIcon);
		toolItem.setEnabled(false);
		toolItem.setToolTipText((String)labels.get("label")); //$NON-NLS-1$
		
		//adds a listener to the menu item and the toggle button
		menuitem.addActionListener(textAction);
		toolItem.addActionListener(textAction);
		
		//a listener that listens to the changes of the SVGFrames
		final ActionListener svgframeListener=new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				if(getSVGEditor().getFrameManager().getFrameNumber()>0){
				    
					menuitem.setEnabled(true);
					//menuitem.setIcon(icon);
					toolItem.setEnabled(true);
					toolItem.setIcon(icon);

				}else{
				    
					menuitem.setEnabled(false);
					//menuitem.setIcon(disabledIcon);
					toolItem.setEnabled(false);
					toolItem.setIcon(disabledIcon);
				}
				
				textAction.reset();
			}
		};
		
		//adds the SVGFrame change listener
		editor.getFrameManager().addSVGFrameChangedListener(svgframeListener);
	}
	
	/**
	 * @return a map associating a menu item id to its menu item object
	 */
	public Hashtable getMenuItems(){
		
		Hashtable menuItems=new Hashtable();
		menuItems.put((String)ids.get("idmenuitem"), menuitem); //$NON-NLS-1$
		
		return menuItems;
	}
	
	/**
	 * @return a map associating a tool item id to its tool item object
	 */
	public Hashtable getToolItems(){
		
		Hashtable toolItems=new Hashtable();
		toolItems.put((String)ids.get("idmenuitem"), toolItem); //$NON-NLS-1$
		
		return toolItems;
	}
	
	/**
	 * draws a text
	 * @param frame the current SVGFrame
	 * @param point the clicked point
	 * @param value the value of the text to be drawn
	 */
	protected void drawText(SVGFrame frame, Point2D.Double point, String value){
		
		if(frame!=null && point!=null){
		    
			Document doc=frame.getScrollPane().getSVGCanvas().getDocument();
			
			if(getSVGEditor().getSVGSelection()!=null && doc!=null){
			    
				final Element parent=getSVGEditor().getSVGSelection().getCurrentParentElement(frame);
				
				if(parent!=null){
				    
					DecimalFormatSymbols symbols=new DecimalFormatSymbols();
					symbols.setDecimalSeparator('.');
					DecimalFormat format=new DecimalFormat("######.#",symbols); //$NON-NLS-1$
			
					if(value!=null && !value.equals("")){ //$NON-NLS-1$
						// creates the text element
						final Element text = doc.createElementNS(doc.getDocumentElement().getNamespaceURI(),"text");												 //$NON-NLS-1$
						text.setAttributeNS(null,"x", format.format(point.x)); //$NON-NLS-1$
						text.setAttributeNS(null,"y", format.format(point.y)); //$NON-NLS-1$
						String colorString=getSVGEditor().getColorChooser().getColorString(getSVGEditor().getSVGColorManager().getCurrentColor());
						text.setAttributeNS(null, "style", "font-size:12pt;fill:".concat(colorString.concat(";"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						//set multiline text - important to do this after attributes are set
						TextConversionUtil.putMultilineTextUnderElement(text, value, doc);			
				
						//attaches the element to the svg root element	
						parent.appendChild(text);
			
						//sets that the svg has been modified
						frame.setModified(true);
			
						//creates final variables
						final Document fdoc=doc;
						final Node ftext=text;
						final SVGFrame fframe=frame;

						//create the undo/redo action and insert it into the undo/redo stack
						if(getSVGEditor().getUndoRedo()!=null){

							SVGUndoRedoAction action=new SVGUndoRedoAction((String)labels.get("undoredocreate")){ //$NON-NLS-1$

								public void undo(){
								    
								    parent.removeChild(text);
								}

								public void redo(){
								    
								    parent.appendChild(text);
								}
							};
				
							SVGSelection selection=getSVGEditor().getSVGSelection();
				
							if(selection!=null){
							    
								selection.deselectAll(frame, false, true);
								selection.addUndoRedoAction(frame, action);
								selection.handleNodeSelection(frame, text);
								selection.addUndoRedoAction(frame, new SVGUndoRedoAction((String)labels.get("undoredocreate")){}); //$NON-NLS-1$
								selection.refreshSelection(frame);
					
							}else{
							    
								SVGUndoRedoActionList actionlist=new SVGUndoRedoActionList((String)labels.get("undoredocreate")); //$NON-NLS-1$
								actionlist.add(action);
								getSVGEditor().getUndoRedo().addActionList(frame, actionlist);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * @author Jordi SUC
	 *
	 * A class allowing to enter the initial value of  a text
	 */
	protected class TextDialog extends JDialog{
		
		/**
		 * the text field in which the text will be entered
		 */
		private JTextArea textArea  =new JTextArea(4, 20);
		
		/**
		 * the constructor of the class
		 */
		protected TextDialog(JFrame parent){
			
			super(parent, (String)labels.get("initdialogtitle"), true); //$NON-NLS-1$
			
			//the content pane  the dialog
			Container pane=getContentPane();
			pane.setLayout(new BorderLayout());
						
			this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			
			//creates the widgets used to enter the text value
			JPanel panel=new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			
			//No instruction label necessary - it's obvious you need to enter text
//			JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//			JLabel label=new JLabel((String)labels.get("initdialoghelp")+" : ");
//			labelPanel.add(label);			
//			panel.add(labelPanel);
			
			JScrollPane textScroll = new JScrollPane(textArea);
			panel.add(textScroll);
			panel.setBorder(new EmptyBorder(10,10,5,10));
			
			//creates the ok and cancel buttons
			JPanel buttonPanel = new JPanel();
			final JButton ok=new JButton((String)labels.get("initok")), cancel=new JButton((String)labels.get("initcancel")); //$NON-NLS-1$ //$NON-NLS-2$
			
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
			buttonPanel.add(ok);
			buttonPanel.add(cancel);
			buttonPanel.setBorder(new EmptyBorder(0,10,5,10));

			pane.add(panel, BorderLayout.CENTER);
			pane.add(buttonPanel, BorderLayout.SOUTH);

			//creating the listeners
			final ActionListener buttonsListener=new ActionListener(){
			    
				public void actionPerformed(ActionEvent evt) {

				    if( ! evt.getSource().equals(ok)){
				        
						textArea.setText(""); //$NON-NLS-1$
				    }
				    
					TextDialog.this.dispose();    
				    ok.removeActionListener(this);
				    cancel.removeActionListener(this);
				}
			};
			
			//adds the listeners
			ok.addActionListener(buttonsListener);
			cancel.addActionListener(buttonsListener);
			
			//deal with the dialog close button
			addWindowListener(new WindowAdapter(){
			    
			    @Override
			    public void windowClosing(WindowEvent evt)
			    {
			        if( ! evt.getSource().equals(ok)){
			            
			            textArea.setText(""); //$NON-NLS-1$
			        }
			        
			        TextDialog.this.dispose();		        
			    }
			});
			
	        final AWTEventListener keyListener=new AWTEventListener(){
	            
	            public void eventDispatched(AWTEvent e) {
	                
	                if(e instanceof KeyEvent && TextDialog.this.isVisible()){
	                    
	                    KeyEvent kev=(KeyEvent)e;
	                    
	                    if(kev.getID()==KeyEvent.KEY_PRESSED){
			                    if(kev.getKeyCode()==KeyEvent.VK_CANCEL){

			                    cancel.doClick();
			                }
	                    }
	                }
	            }
	        };
	        
	        Toolkit.getDefaultToolkit().addAWTEventListener(keyListener, AWTEvent.KEY_EVENT_MASK);

			//set size and location of the dialog box
			final int desiredWidth = 250;
			final int desiredHeight = 250;
			int x =(int)(getSVGEditor().getParent().getLocationOnScreen().getX()+getSVGEditor().getParent().getWidth()/2- desiredWidth/2);
			int	y =(int)(getSVGEditor().getParent().getLocationOnScreen().getY()+getSVGEditor().getParent().getHeight()/2- desiredHeight/2);			
			setBounds(x, y, desiredWidth, desiredHeight);			
		}
		
		/**
		 * @return the value of the text entered
		 */
		protected String getText(){
			return textArea.getText();
		}
	}
	
	/**
	 * used to remove the listener added to draw a rectangle when the user clicks on the menu item
	 */
	public void cancelActions(){
	    
		if(textAction!=null){
		    
			toolItem.removeActionListener(textAction);
			toolItem.setSelected(false);
			toolItem.addActionListener(textAction);
			
		    textAction.cancelActions();
		}
	}
	
	/**
	 * 
	 * @author Jordi SUC
	 * the class allowing to know the position of the future text
	 */
	protected class TextActionListener implements ActionListener{

		/**
		 * the hashtable associating a frame to its mouse adapter
		 */
		private final Hashtable mouseAdapterFrames=new Hashtable();
		
		/**
		 * an instance of this class
		 */
		private final TextActionListener action=this;
		
		/**
		 * the cursor used when creating a rectangle
		 */
		private Cursor createCursor;
		
		private boolean isActive=false;
		
		/**
		 * the source component
		 */
		private Object source=null;
		
		/**
		 * the constructor of the class
		 */
		protected TextActionListener(){
			
			createCursor=getSVGEditor().getCursors().getCursor("text"); //$NON-NLS-1$
		}
		
		/**
		 * resets the listener
		 */
		protected void reset(){
			
			if(isActive){
			    
				Collection frames=getSVGEditor().getFrameManager().getFrames();
			
				Iterator it;
				SVGFrame frm=null;
				LinkedList toBeRemoved=new LinkedList();
				Object mouseListener=null;
				
				//removes all the motion adapters from the frames
				for(it=mouseAdapterFrames.keySet().iterator(); it.hasNext();){
				    
					try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
			
					if(frm!=null && ! frames.contains(frm)){
					    
						try{
							mouseListener=mouseAdapterFrames.get(frm);
							frm.getScrollPane().getSVGCanvas().removeMouseListener((MouseAdapter)mouseListener);
							frm.getScrollPane().getSVGCanvas().removeMouseMotionListener((MouseMotionListener)mouseListener);
						}catch (Exception ex){}
						
						toBeRemoved.add(frm);
					}
				}
					
				//removes the frames that have been closed
				
				for(it=toBeRemoved.iterator(); it.hasNext();){
				    
					try{mouseAdapterFrames.remove(it.next());}catch (Exception ex){}
				}

				TextMouseListener tml=null;
				
				//adds the new motion adapters
				for(it=frames.iterator(); it.hasNext();){
				    
					try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
			
					if(frm!=null && ! mouseAdapterFrames.containsKey(frm)){
				
						tml=new TextMouseListener(frm);

						try{
							frm.getScrollPane().getSVGCanvas().addMouseListener(tml);
							frm.getScrollPane().getSVGCanvas().addMouseMotionListener(tml);
							frm.getScrollPane().getSVGCanvas().setSVGCursor(createCursor);
						}catch (Exception ex){}
						
						mouseAdapterFrames.put(frm, tml);
					}
				}				
			}
		}
		
		/**
		 * used to remove the listener added to draw a rectangle when the user clicks on the menu item
		 */	
		protected void cancelActions(){
			
			if(isActive){
			    
				//removes the listeners
				Iterator it;
				SVGFrame frm=null;
				LinkedList toBeRemoved=new LinkedList();
				Object mouseListener=null;
				
				//removes all the motion adapters from the frames
				for(it=mouseAdapterFrames.keySet().iterator(); it.hasNext();){
				    
					try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
					
					if(frm!=null){
					    
						//resets the information displayed
						frm.getStateBar().setSVGW(""); //$NON-NLS-1$
						frm.getStateBar().setSVGH(""); //$NON-NLS-1$
						frm.getScrollPane().getSVGCanvas().setSVGCursor(frm.getSVGEditor().getCursors().getCursor("default")); //$NON-NLS-1$
						
						try{
							mouseListener=mouseAdapterFrames.get(frm);
							frm.getScrollPane().getSVGCanvas().removeMouseListener((MouseAdapter)mouseListener);
							frm.getScrollPane().getSVGCanvas().removeMouseMotionListener((MouseMotionListener)mouseListener);
						}catch (Exception ex){}
						
						toBeRemoved.add(frm);
					}
				}
			
				//removes the frames that have been closed
				for(it=toBeRemoved.iterator(); it.hasNext();){
				    
					try{mouseAdapterFrames.remove(it.next());}catch (Exception ex){}
				}
			
				isActive=false;	
			}
		}
		
		/**
		 * the action to be done
		 * @param e the event
		 */
		public void actionPerformed(ActionEvent e){

			if((e.getSource() instanceof JMenuItem && ! toolItem.isSelected()) || (e.getSource() instanceof JToggleButton)){

				getSVGEditor().cancelActions(false);
				
				if(getSVGEditor().getFrameManager().getCurrentFrame()!=null){
					
					toolItem.removeActionListener(textAction);
					toolItem.setSelected(true);
					toolItem.addActionListener(textAction);
			
					//the listener is active
					isActive=true;
					source=e.getSource();
	
					Collection frames=getSVGEditor().getFrameManager().getFrames();
	
					Iterator it;
					SVGFrame frm=null;
					TextMouseListener tml=null;
					
					//adds the new motion adapters
					for(it=frames.iterator(); it.hasNext();){
					    
						try{frm=(SVGFrame)it.next();}catch (Exception ex){frm=null;}
					
						if(frm!=null){
	
							tml=new TextMouseListener(frm);
	
							try{
								frm.getScrollPane().getSVGCanvas().addMouseListener(tml);
								frm.getScrollPane().getSVGCanvas().addMouseMotionListener(tml);
								frm.getScrollPane().getSVGCanvas().setSVGCursor(createCursor);
							}catch (Exception ex){}
							
							mouseAdapterFrames.put(frm, tml);
						}
					}
				}
			}
		}
			
		protected class TextMouseListener extends MouseAdapter implements MouseMotionListener{
		
			/**
			 * the points of the area corresponding to the future rectangle
			 */		
			private Point2D.Double point=null;
			
			private SVGFrame frame;
		
			/**
			 * the constructor of the class
			 * @param frame
			 */
			public TextMouseListener(SVGFrame frame){
			    
				this.frame=frame;
			}

			/**
			 * @param evt the event
			 */
			public void mouseDragged(MouseEvent evt) {
			}
				
			/**
			 * @param evt the event
			 */
			public void mouseMoved(MouseEvent evt) {
			}
			
			/**
			 * @param evt the event
			 */
			public void mouseReleased(MouseEvent evt){
				
				if(evt.getPoint()!=null){
					
					final Point2D.Double point=frame.getAlignedWithRulersPoint(evt.getPoint());
					
					//gets the text value by displaying a dialog box
					TextDialog dialog=null;
					
					if(getSVGEditor().getParent() instanceof JFrame){
						
						dialog=new TextDialog((JFrame)getSVGEditor().getParent());
						
					}else{
						
						dialog=new TextDialog(new JFrame("")); //$NON-NLS-1$
					}
					
					dialog.setVisible(true);
					final String value=dialog.getText();
					
					// attaches the element to the svg root element
					Runnable runnable=new Runnable(){
						
						public void run() {

							svgText.drawText(frame, point, value);
						}
					};
					
					frame.enqueue(runnable);
				}

				getSVGEditor().cancelActions(true);
			}
		}	
	}
}
