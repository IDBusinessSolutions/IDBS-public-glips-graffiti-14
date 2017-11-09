/*
 * Created on 23 mars 2004
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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.resources.*;

/**
 * @author Jordi SUC
 * the class that creates the static menu item open in the menu bar
 */
public class SVGNewOpen extends SVGModuleAdapter{

	/**
	 * the ids of the module
	 */
	final private String idnewopen="NewOpen", idnew="New", idopen="Open", idopenrecent="OpenRecent"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	
	/**
	 * the labels
	 */
	private String labelnew="", labelopen="", labeluntitled=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	/**
	 * labels
	 */
	private String 	labelok="", labelcancel="", newopenlabelwidth="", newopenlabelheight="", newwarningmessage="",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
							 newwarningtitle="", openwarningmessage="", openwarningtitle="", messageformaterror=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	/**
	 * the menu items
	 */
	private JMenuItem newit, open;
	
	/**
	 * the editor
	 */
	private SVGEditor editor;
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public SVGNewOpen(SVGEditor theEditor)
    {		
		this.editor = theEditor;
		
		//gets the labels from the resources
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
				labelnew=bundle.getString("labelnewopennew"); //$NON-NLS-1$
				labelopen=bundle.getString("labelnewopenopen"); //$NON-NLS-1$
				labeluntitled=bundle.getString("labeluntitled"); //$NON-NLS-1$
				labelok=bundle.getString("labelok"); //$NON-NLS-1$
				labelcancel=bundle.getString("labelcancel"); //$NON-NLS-1$
				newopenlabelwidth=bundle.getString("labelwidth"); //$NON-NLS-1$
				newopenlabelheight=bundle.getString("labelheight"); //$NON-NLS-1$
				newwarningmessage=bundle.getString("newwarningmessage"); //$NON-NLS-1$
				newwarningtitle=bundle.getString("newwarningtitle");	 //$NON-NLS-1$
				openwarningmessage=bundle.getString("openwarningmessage"); //$NON-NLS-1$
				openwarningtitle=bundle.getString("openwarningtitle"); //$NON-NLS-1$
				messageformaterror=bundle.getString("messageformaterror"); //$NON-NLS-1$
			}catch (Exception ex){}
		}
		
		//getting the icons
		ImageIcon newIcon=SVGResource.getIcon("New", false), //$NON-NLS-1$
						dnewIcon=SVGResource.getIcon("New", true), //$NON-NLS-1$
						openIcon=SVGResource.getIcon("Open", false), //$NON-NLS-1$
						dopenIcon=SVGResource.getIcon("Open", true); //$NON-NLS-1$
						
		newit=new JMenuItem(labelnew, newIcon);
		newit.setDisabledIcon(dnewIcon);
		newit.setAccelerator(KeyStroke.getKeyStroke("ctrl N")); //$NON-NLS-1$
		
		open=new JMenuItem(labelopen, openIcon);
		open.setDisabledIcon(dopenIcon);
		open.setAccelerator(KeyStroke.getKeyStroke("ctrl O")); //$NON-NLS-1$
		
		//adds a listener to the menu item
		newit.addActionListener(new NewActionListener());	
				
		//adds a listener to the menu item
		open.addActionListener(
		        
			new ActionListener(){
			    
				public void actionPerformed(ActionEvent e){

                    JFileChooser fileChooser=new JFileChooser();
                    
                    if(getSVGEditor().getResource().getCurrentDirectory()!=null){
                        
                        fileChooser.setCurrentDirectory(getSVGEditor().getResource().getCurrentDirectory());
                    }
                    
                    SVGFileFilter fileFilter=new SVGFileFilter();
                    fileChooser.setFileFilter(fileFilter);
                    fileChooser.setMultiSelectionEnabled(true); 
                    
                    int returnVal=fileChooser.showOpenDialog(editor.getParent());
                    
                    if(returnVal == JFileChooser.APPROVE_OPTION) {
                        
                        getSVGEditor().getResource().setCurrentDirectory(fileChooser.getCurrentDirectory());
                        
                        //opens all the selected files
                        File[] files=fileChooser.getSelectedFiles();

                        for(int i=0;i<files.length;i++){
                            
                            if(files[i]!=null && fileFilter.acceptFile(files[i])){
                                
                                open(files[i]);
                                
                            }else if(files[i]!=null){
                                
                                JOptionPane.showMessageDialog(getSVGEditor().getParent(), messageformaterror);
                            }
                        }
                    }
				}
			});
		
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
		menuItems.put(idnew,newit);
		menuItems.put(idopen,open);
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
	 * the method called to open a svg file
	 */
	public void openAction(){

        SwingUtilities.invokeLater(new Runnable() {
            
            public void run() {

                //launches a file chooser
                JFileChooser fileChooser=new JFileChooser();
                fileChooser.setFileFilter(new SVGFileFilter());
                fileChooser.setMultiSelectionEnabled(false); 

                int returnVal = fileChooser.showOpenDialog(getSVGEditor().getParent());
                
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    
                    open(fileChooser.getSelectedFile());
                }
            }
        });
	}
	
	/**
	 * opens a new SVG picture
	 * @param file the file to be opened
	 */
	public void open(File file){

		SVGFrame frame=null;
		boolean hasSucceeded=false;
		
		if(file!=null && file.exists()){
		    
			//checking if an svg file having the same name does not already exist
			String path=file.toURI().toString();
			
			frame=getSVGEditor().getFrameManager().getFrame(path);
			
			if(frame!=null){
				
				getSVGEditor().getFrameManager().setCurrentFrame(path);
				
			}else{
				
				frame=getSVGEditor().getFrameManager().createFrame(file.toURI().toString());
				
				//sets the uri of the svg file
				frame.getScrollPane().getSVGCanvas().setURI(file.toURI().toString());
			}
			
			hasSucceeded=true;
		}
		
		//if the file could not be opened, a dialog is displayed to show that an error occured
		if(! hasSucceeded){
			
			JOptionPane.showMessageDialog(getSVGEditor().getParent(), openwarningmessage, openwarningtitle, JOptionPane.ERROR_MESSAGE);
			
			if(frame!=null){
			    
			    getSVGEditor().getFrameManager().removeFrame(frame.getName());
			}
		}
	}
	
	/**
	 * creates a new document
	 * @param width the width of the new document
	 * @param height the height of the new document
	 */
	public void newDoc(String width, String height){
	    
		SVGFrame frame=getSVGEditor().getFrameManager().createFrame(labeluntitled);
		
		if(frame!=null){
		    
		    frame.getScrollPane().getSVGCanvas().newDocument(width, height);
		}
	}
	
	/**
	 * gets the module's name
	 * @return the module's name
	 */
	public String getName(){
		return idnewopen;
	}
	
	/**
	 * The class of the action that will be executed when clicking on the "New" menu item
	 */
	protected class NewActionListener implements ActionListener{
		
		/**
		 * the dialog box displayed when clicking on the "New" menu item
		 */
		private NewDialog dialog;
		
		/**
		 * the constructor of the class
		 */
		protected NewActionListener(){
		    
			if(getSVGEditor().getParent() instanceof JFrame){
				
				dialog=new NewDialog((JFrame)getSVGEditor().getParent());

			}else{

				dialog=new NewDialog(new JFrame("")); //$NON-NLS-1$
			}

	        /*final AWTEventListener keyListener=new AWTEventListener(){
	            
	            public void eventDispatched(AWTEvent e) {
	                
	                if(e instanceof KeyEvent && dialog.isVisible()){
	                    
	                    KeyEvent kev=(KeyEvent)e;
                        
	                    if(kev.getID()==KeyEvent.KEY_PRESSED){
	                        
			                if(kev.getKeyCode()==KeyEvent.VK_ENTER){
			                    
			                    ok.doClick();
			                    
			                }else if(kev.getKeyCode()==KeyEvent.VK_CANCEL){

			                    cancel.doClick();
			                }
	                    }
	                }
	            }
	        };
	        
	        Toolkit.getDefaultToolkit().addAWTEventListener(keyListener, AWTEvent.KEY_EVENT_MASK);*/
		}
	
		/**
		 * the method called when receiving an event
		 * @param e the event
		 */
		public void actionPerformed(ActionEvent e){
            
            //sets the location of the dialog box
            int     x=(int)(getSVGEditor().getParent().getLocationOnScreen().getX()+getSVGEditor().getParent().getWidth()/2-dialog.getSize().getWidth()/2), 
                     y=(int)(getSVGEditor().getParent().getLocationOnScreen().getY()+getSVGEditor().getParent().getHeight()/2-dialog.getSize().getHeight()/2);
            
            dialog.setLocation(x,y);

			int res=dialog.showDialog();
			
			if(res==NewDialog.OK){
				
				String width=dialog.getWidthString(), height=dialog.getHeightString();
				
				newDoc(width, height);
			}
		}
		
		/**
		 * the class of the dialog that will be displayed when cliking on the New menu item
		 */
		protected class NewDialog extends JDialog{

			/**
			 * the OK constant
			 */
			public static final int OK=0;
			
			/**
			 * the CANCEL constant
			 */
			public static final int CANCEL=1;
			
			/**
			 * the width and height
			 */
			private String width="400", height="400"; //$NON-NLS-1$ //$NON-NLS-2$
			
			/**
			 * the unit
			 */
			private String unit="px"; //$NON-NLS-1$
			
			/**
			 * whether to preserve the ratio aspect or not
			 */
			private boolean preserveRatio=false;
			
			/**
			 * the ratio
			 */
			private double ratio=1;
			
			/**
			 * the returned type
			 */
			private int returnedRes=CANCEL;
						
			/**
			 * the constructor of the class
			 * @param parent the parent frame
			 */
			protected NewDialog(JFrame parent){
			    
				super(parent, labelnew, true);
				
				//creating the combo box used to change the units
				final JComboBox unitsCombo=getSVGEditor().getSVGToolkit().getUnitsComboBoxChooser();

				//the labels for the textfields
				JLabel widthLabel=new JLabel(newopenlabelwidth.concat(" :")); //$NON-NLS-1$
				JLabel heightLabel=new JLabel(newopenlabelheight.concat(" :")); //$NON-NLS-1$
				
				widthLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				heightLabel.setHorizontalAlignment(SwingConstants.RIGHT);	
				
				//the textfields that will be used to enter the size values
				SpinnerNumberModel widthModel=new SpinnerNumberModel(400, 0, Double.MAX_VALUE, 1);
				SpinnerNumberModel heightModel=new SpinnerNumberModel(400, 0, Double.MAX_VALUE, 1);
				final JSpinner widthSpinner=new JSpinner(widthModel), heightSpinner=new JSpinner(heightModel);
				((JSpinner.DefaultEditor)widthSpinner.getEditor()).getTextField().setColumns(5);
				((JSpinner.DefaultEditor)heightSpinner.getEditor()).getTextField().setColumns(5);
				
				//the preserve ratio toggle button
				final JToggleButton preserveRatioButton=new JToggleButton("p"); //$NON-NLS-1$
				preserveRatioButton.setSelected(false);
				
				//creating the listener to the preserve ratio button
				preserveRatioButton.addActionListener(new ActionListener(){
					
					public void actionPerformed(ActionEvent evt) {

						preserveRatio=preserveRatioButton.isSelected();
						
						if(preserveRatio){
							
							ratio=((Double)widthSpinner.getValue()).doubleValue()/((Double)heightSpinner.getValue()).doubleValue();
						}
					}
				});
				
				//the buttons used to cancel or validate the values entered
				final JButton okButton=new JButton(labelok), cancelButton=new JButton(labelcancel);

				//adding the listener to the combo
				unitsCombo.addActionListener(new ActionListener(){
					
					public void actionPerformed(ActionEvent evt) {

						if(unitsCombo.getSelectedItem()!=null){
							
							//storing the old units values
							String oldUnit=unit;
							double oldWidthValue=SVGToolkit.getPixelledNumber(width+oldUnit);
							double oldHeightValue=SVGToolkit.getPixelledNumber(height+oldUnit);
							
							unit=unitsCombo.getSelectedItem().toString();

							//computing the new values
							double newWidthValue=SVGToolkit.convertFromPixelToUnit(oldWidthValue, unit);
							double newHeightValue=SVGToolkit.convertFromPixelToUnit(oldHeightValue, unit);

							//setting the new values
							width=newWidthValue+""; //$NON-NLS-1$
							height=newHeightValue+""; //$NON-NLS-1$
							
							widthSpinner.setValue(newWidthValue);
							heightSpinner.setValue(newHeightValue);
						}
					}
				});
				
				//adding the listener to the spinners
				ChangeListener changeListener=new ChangeListener(){
					
					public void stateChanged(ChangeEvent evt) {

						if(evt.getSource().equals(widthSpinner)){
							
							width=widthSpinner.getValue()+""; //$NON-NLS-1$

							if(preserveRatio){
								
								heightSpinner.removeChangeListener(this);
								double h=((Double)widthSpinner.getValue()).doubleValue()/ratio;
								height=h+""; //$NON-NLS-1$
								heightSpinner.setValue(h);
								
								heightSpinner.addChangeListener(this);
							}
							
						}else{
							
							height=heightSpinner.getValue()+""; //$NON-NLS-1$
							
							if(preserveRatio){
								
								widthSpinner.removeChangeListener(this);
								
								double w=((Double)heightSpinner.getValue()).doubleValue()*ratio;
								width=w+""; //$NON-NLS-1$
								widthSpinner.setValue(w);
								
								widthSpinner.addChangeListener(this);
							}
						}
					}
				};
				
				widthSpinner.addChangeListener(changeListener);
				heightSpinner.addChangeListener(changeListener);

				//adding the listener to the buttons
				ActionListener buttonsListener=new ActionListener(){
					
					public void actionPerformed(ActionEvent evt) {
						
						if(evt.getSource().equals(okButton)){
							
							returnedRes=OK;
							
						}else if(evt.getSource().equals(cancelButton)){
							
							returnedRes=CANCEL;
						}
						
						setVisible(false);
					}
				};
				
				okButton.addActionListener(buttonsListener);
				cancelButton.addActionListener(buttonsListener);
				
				//building the dialog
				JPanel contentPanel=new JPanel();
				
				GridBagLayout gridBag=new GridBagLayout();
				contentPanel.setLayout(gridBag);
				GridBagConstraints c=new GridBagConstraints();
				c.fill=GridBagConstraints.HORIZONTAL;
				c.insets=new Insets(2, 2, 2, 2);
				
				c.anchor=GridBagConstraints.EAST;
				c.gridwidth=1;
				gridBag.setConstraints(widthLabel, c);
				contentPanel.add(widthLabel);
				
				c.anchor=GridBagConstraints.WEST;
				c.gridwidth=1;
				gridBag.setConstraints(widthSpinner, c);
				contentPanel.add(widthSpinner);
				
				JLabel wpxLabel=new JLabel(" px"); //$NON-NLS-1$
				c.anchor=GridBagConstraints.WEST;
				c.gridwidth=GridBagConstraints.REMAINDER;
				c.fill=GridBagConstraints.NONE;
				gridBag.setConstraints(wpxLabel, c);
				contentPanel.add(wpxLabel);
				
				
				/*c.anchor=GridBagConstraints.CENTER;
				c.gridwidth=GridBagConstraints.REMAINDER;
				c.gridheight=2;
				gridBag.setConstraints(unitsCombo, c);
				contentPanel.add(unitsCombo);*/
				
				
				
				/*c.anchor=GridBagConstraints.CENTER;
				c.gridwidth=1;
				c.gridheight=2;
				gridBag.setConstraints(unitsCombo, c);
				contentPanel.add(unitsCombo);
				
				c.anchor=GridBagConstraints.CENTER;
				c.gridwidth=GridBagConstraints.REMAINDER;
				c.gridheight=2;
				gridBag.setConstraints(preserveRatioButton, c);
				contentPanel.add(preserveRatioButton);*/
				
				c.anchor=GridBagConstraints.EAST;
				c.fill=GridBagConstraints.HORIZONTAL;
				c.gridwidth=1;
				gridBag.setConstraints(heightLabel, c);
				contentPanel.add(heightLabel);
				
				c.anchor=GridBagConstraints.WEST;
				c.gridwidth=1;
				gridBag.setConstraints(heightSpinner, c);
				contentPanel.add(heightSpinner);
				
				JLabel hpxLabel=new JLabel(" px"); //$NON-NLS-1$
				c.anchor=GridBagConstraints.WEST;
				c.gridwidth=GridBagConstraints.REMAINDER;
				c.fill=GridBagConstraints.NONE;
				gridBag.setConstraints(hpxLabel, c);
				contentPanel.add(hpxLabel);
				
				//the buttons panel
				JPanel buttonsPanel=new JPanel();
				buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
				buttonsPanel.add(okButton);
				buttonsPanel.add(cancelButton);
				
				getContentPane().setLayout(new BorderLayout(0, 4));
				getContentPane().add(contentPanel, BorderLayout.CENTER);
				getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

				pack();
			}
		
			/**
			 * @return the width string
			 */
			public String getWidthString() {

				return width+unit;
			}

			/**
			 * @return the width string
			 */
			public String getHeightString() {

				return height+unit;
			}
		
			/**
			 * showing the new dialog
			 * @return whether the user clicked on the OK or CANCEL button
			 */
			public int showDialog(){
				
				setVisible(true);
				
				return returnedRes;
			}
		}
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
}
