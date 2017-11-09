/*
 * Created on 26 août 2004
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
package fr.itris.glips.svgeditor.visualresources;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import fr.itris.glips.svgeditor.*;
import fr.itris.glips.svgeditor.canvas.*;

/**
 * the dialog for choosing the id of a shape
 * 
 * @author Jordi SUC
 */
public class SVGVisualResourceShapeIdChooser {

    /**
     * a small font
     */
    private static final Font smallFont=new Font("smallFont", Font.ROMAN_BASELINE, 9); //$NON-NLS-1$
    
    /**
     * the font
     */
    private static final Font theFont=new Font("theFont", Font.ROMAN_BASELINE, 10); //$NON-NLS-1$
    
    /**
     * the last id selected
     */
    private static String selectedId=""; //$NON-NLS-1$
    
    /**
     * the labels
     */
    private static String titleLabel="", titledBorderLabel="", okLabel="", cancelLabel="", alertMessage="", errorTitle=""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    
    static{
    	
        //getting the bundle
        ResourceBundle bundle=SVGEditor.getBundle();
        
        //getting the labels
        if(bundle!=null){
            
            try{
                titleLabel=bundle.getString("labelnew"); //$NON-NLS-1$
                titledBorderLabel=bundle.getString("vresource_displaywindowchooserforshapeid"); //$NON-NLS-1$
                okLabel=bundle.getString("labelok"); //$NON-NLS-1$
                cancelLabel=bundle.getString("labelcancel"); //$NON-NLS-1$
                alertMessage=bundle.getString("vresource_idnotselected"); //$NON-NLS-1$
                errorTitle=bundle.getString("labelerror"); //$NON-NLS-1$
            }catch (Exception ex){}
        }
    }
    
    /**
     * shows the id shape chooser dialog
     *@param frame a frame
     *@return the selected id
     */
    public static String showShapeChooserIdDialog(SVGFrame frame){
        
        selectedId=""; //$NON-NLS-1$
        
        if(frame!=null){
        	
        	final SVGFrame fframe=frame;
        	
            //creating the dialog
        	Container parentContainer=frame.getSVGEditor().getParent();
        	JFrame parentFrame=null;
        	
        	if(parentContainer instanceof JFrame){
        		
        		parentFrame=(JFrame)parentContainer;
        		
        	}else{
        		
        		parentFrame=new JFrame(""); //$NON-NLS-1$
        	}
        	
            final JDialog dialog=new JDialog(parentFrame, titleLabel, true);
            
            JPanel dialogPanel=new JPanel();
            dialogPanel.setLayout(new BorderLayout());
            
            //the combo box
            final JComboBox combo=new JComboBox();
            combo.setFont(theFont);
            
            //the list of the ids of the nodes contained in the document
            LinkedList nodesIds=frame.getShapeNodesIds();
            
            //filling the combo box with the items
            SVGComboItem item=null;
            String id=""; //$NON-NLS-1$
            
            //the empty item
            item=new SVGComboItem("", ""); //$NON-NLS-1$ //$NON-NLS-2$
            combo.addItem(item);
            
            //for each ids contained in the list
            for(Iterator it=nodesIds.iterator(); it.hasNext();){
                
                try{
                    id=(String)it.next();
                }catch (Exception ex){id=null;}
                
                if(id!=null && ! id.equals("")){ //$NON-NLS-1$
                    
                    item=new SVGComboItem(id, id);
                    combo.addItem(item);
                }
            }

            //the listener to the combo box
            final ActionListener comboListener=new ActionListener(){
                
                public void actionPerformed(ActionEvent evt) {
                    
                    String value=""; //$NON-NLS-1$
                    
                    if(combo.getSelectedItem()!=null){
                        
                        value=((SVGComboItem)combo.getSelectedItem()).getValue();
                    }
                    
                    //modifies the widgetValue of the property item
                    if(value!=null && ! value.equals("")){ //$NON-NLS-1$
                        
                        selectedId=value;
                    }
                }
            };
            
            //adds a listener to the combo box
            combo.addActionListener(comboListener);
            
            //creating the buttons
            final JButton okBt=new JButton(okLabel), cancelBt=new JButton(cancelLabel);
            final String falertMessage=alertMessage, ferrorTitle=errorTitle;
            okBt.setFont(theFont);
            cancelBt.setFont(theFont);
            
            //the listener to the buttons
            final ActionListener buttonsListener=new ActionListener(){
                
                public void actionPerformed(ActionEvent evt) {
                    
                    if(evt.getSource().equals(okBt)){
                        
                        if(selectedId==null || (selectedId!=null && selectedId.equals(""))){ //$NON-NLS-1$
                            
                            JOptionPane.showMessageDialog(fframe.getSVGEditor().getParent(), falertMessage, ferrorTitle, JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        
                    }else{
                        
                        selectedId=""; //$NON-NLS-1$
                    }
                    
                    //removes the listeners
                    combo.removeActionListener(comboListener);
                    okBt.removeActionListener(this);
                    cancelBt.removeActionListener(this);
                    dialog.setVisible(false);
                } 
            };
            
            //adding the listener to the buttons
            okBt.addActionListener(buttonsListener);
            cancelBt.addActionListener(buttonsListener);
            
            //dealing with the dialog close button
            dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            
            //the panel containing the combo box
            JPanel cPanel=new JPanel();
            cPanel.setLayout(new BoxLayout(cPanel, BoxLayout.X_AXIS));
            cPanel.setBorder(new EmptyBorder(0, 20, 0, 0));
            cPanel.add(combo);
            
            //the panel that will be displayed
            JPanel comboPanel=new JPanel();
            comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.X_AXIS));
            comboPanel.add(cPanel);
            
            //setting the border
            TitledBorder border=new TitledBorder(titledBorderLabel);
            border.setTitleFont(theFont);
            comboPanel.setBorder(border);
            
            //the buttons panel
            JPanel buttons=new JPanel();
            buttons.setLayout(new FlowLayout(FlowLayout.CENTER));
            buttons.add(okBt);
            buttons.add(cancelBt);
            
            //the content panel
            JPanel content=new JPanel();
            content.setLayout(new BorderLayout());
            content.add(comboPanel, BorderLayout.CENTER);
            content.add(buttons, BorderLayout.SOUTH);
            
            //adding the content pane to the dialog box
            dialog.getContentPane().add(content);

            //packing the dialog
            dialog.pack();
            
            //seting the location for the dialog
            dialog.setLocation(
                    (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2-dialog.getSize().width/2), 
                    (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2-dialog.getSize().height/2));

            //displays the dialog
            dialog.setVisible(true);
            
            while(dialog.isVisible()){
                
                try{
                    Thread.sleep((long)100.0);
                }catch (Exception ex){} 
            }
        }
        
        return selectedId;
    }

}


