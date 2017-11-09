/*
 * Created on 18 févr. 2005
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
package fr.itris.glips.svgeditor.colorchooser;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.colorchooser.*;

import fr.itris.glips.svgeditor.*;

/**
 * the panel enabling to choose a svg standard w3c color 
 * 
 * @author Jordi SUC
 */
public class SVGW3CColorChooserPanel extends AbstractColorChooserPanel{

    /**
     * the label of the panel
     */
    private String label=""; //$NON-NLS-1$
    
    /**
     * the labels
     */
    private String memoryLabel=""; //$NON-NLS-1$

    /**
     * the bundle used to get labels
     */
    private ResourceBundle bundle=null;
    
    /**
     * the editor
     */
    private SVGEditor editor;

    /**
     * the constructor of the class 
     * @param editor the editor
     */
    public SVGW3CColorChooserPanel(SVGEditor editor){
        
        this.editor=editor;
        
        //gets the labels from the resources
        bundle=SVGEditor.getBundle();
        
        if(bundle!=null){
            
            try{
                label=bundle.getString("svgColorChooserPanelLabel"); //$NON-NLS-1$
                memoryLabel=bundle.getString("colorChooserMemoryLabel"); //$NON-NLS-1$
            }catch (Exception ex){}
        }
    }

    /**
     * @see javax.swing.colorchooser.AbstractColorChooserPanel#buildChooser()
     */
    protected void buildChooser() {

        //the panel containing the color and memory panels
        JPanel colorsAndMemoryPanel=new JPanel();
        
        //the panel containing the panels displaying the colors
        JPanel colorsPanel=new JPanel();
        int colorsNbPerLine=25;
        
        //the elements for the last colors panel functionnality
        final int memoryNb=35, memoryRowNb=7;
        
        //creating the memory panel
        final JPanel memoryPanel=new JPanel();
        
        //creating the list of the panels that will be contained in the memory panel
        final LinkedList lastColorPanels=new LinkedList();
        
        //creating the list of the lastly selected colors
        final LinkedList lastSelectedColors=new LinkedList();
        
        //the label panel displaying the name and the corresponding rgb values of a color
        final JLabel colorLabel=new JLabel("", JLabel.CENTER); //$NON-NLS-1$
        
        //the list of the colors
        java.util.List colorsList=editor.getResource().getW3CColors();
        
        colorsPanel.setLayout(new GridLayout((int)(Math.floor(colorsList.size()/colorsNbPerLine)+1), colorsNbPerLine, 1, 1));
        SVGW3CColor color=null;
        JPanel panel=null;
        
        for(Iterator it=colorsList.iterator(); it.hasNext();){
            
            color=(SVGW3CColor)it.next();
            
            if(color!=null){
                
                panel=new JPanel();
                
                //setting the properties of the panel
                panel.setBorder(new LineBorder(Color.black, 1));
                panel.setBackground(color);
                panel.setPreferredSize(new Dimension(15, 15));
                panel.setToolTipText(color.getStringRepresentation());
                
                final JPanel fpanel=panel;
                final SVGW3CColor fcolor=color;
                
                //adding a listener to the clicks on the color panels
                panel.addMouseListener(new MouseAdapter(){

                    public void mouseClicked(MouseEvent evt) {

                        SVGW3CColor color=null, selectedColor=fcolor;
                        JPanel panel=null;
                        
                        //sets the new selected color
                        getColorSelectionModel().setSelectedColor(selectedColor);
                        
                        //removes the last color of the last selected colors, if the list is full
                        if(lastSelectedColors.size()>0 && lastSelectedColors.size()>=memoryNb){
                            
                            lastSelectedColors.removeLast();
                        }
                        
                        //adds the new selected color to the list
                        lastSelectedColors.addFirst(selectedColor);

                        //for each panel contained in the memory, sets its new color and tooltip
                        for(int i=0; i<lastColorPanels.size(); i++){
                            
                            panel=(JPanel)lastColorPanels.get(i);
                            
                            if(panel!=null){
                                
                                if(i<lastSelectedColors.size()){
                                    
                                    color=(SVGW3CColor)lastSelectedColors.get(i);
                                    
                                    if(color!=null){
                                        
                                        panel.setBorder(new LineBorder(Color.black, 1));
                                        panel.setBackground(color);
                                        panel.setToolTipText(color.getStringRepresentation());
                                    }
                                    
                                }else{
                                    
                                    panel.setBorder(new LineBorder(Color.lightGray, 1));
                                    panel.setBackground(getParent().getBackground());
                                    panel.setToolTipText(null);
                                }
                            }
                        }
                        
                        memoryPanel.repaint();
                    }

                    public void mouseEntered(MouseEvent arg0){

                        colorLabel.setText(fcolor.getStringRepresentation());
                    }

                    public void mouseExited(MouseEvent arg0){

                        colorLabel.setText(""); //$NON-NLS-1$
                    }
                });
                
                colorsPanel.add(panel);
            }
        }
        
        //filling the memory panel
        memoryPanel.setLayout(new GridLayout(memoryRowNb, (int)(Math.floor(memoryNb/memoryRowNb))+1, 1, 1));
        memoryPanel.setBorder(new TitledBorder(memoryLabel));
        
        JPanel memPanel=null;
        
        for(int i=0; i<memoryNb; i++){
            
            memPanel=new JPanel();
            memPanel.setBorder(new LineBorder(Color.lightGray, 1));
            memPanel.setPreferredSize(new Dimension(15, 15));
            memPanel.setBackground(getParent().getBackground());
            
            lastColorPanels.add(memPanel);
            memoryPanel.add(memPanel);
            
            final int fi=i;

            //adding a mouse listener to the panel
            memPanel.addMouseListener(new MouseAdapter(){
 
				public void mouseClicked(MouseEvent evt) {

				    SVGW3CColor color=null;
				    
				    if(fi<lastSelectedColors.size()){
				        
				        color=(SVGW3CColor)lastSelectedColors.get(fi);
				        
				        if(color!=null){
				            
				            getColorSelectionModel().setSelectedColor(color);
				        }
				    }
				}
				
                public void mouseEntered(MouseEvent arg0){
                    
				    SVGW3CColor color=null;
				    
				    if(fi<lastSelectedColors.size()){
				        
				        color=(SVGW3CColor)lastSelectedColors.get(fi);
				        
				        if(color!=null){
				            
		                    colorLabel.setText(color.getStringRepresentation());
				        }
				    }
                }

                public void mouseExited(MouseEvent arg0){

				    SVGW3CColor color=null;
				    
				    if(fi<lastSelectedColors.size()){
				        
				        color=(SVGW3CColor)lastSelectedColors.get(fi);
				        
				        if(color!=null){
				            
		                    colorLabel.setText(""); //$NON-NLS-1$
				        }
				    }
                }
            });
        }
        
        //adding the two panels
        colorsAndMemoryPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        colorsAndMemoryPanel.add(colorsPanel);
        colorsAndMemoryPanel.add(memoryPanel);
        
        //adding the colors and memory panel and the color label widget to the color chooser panel
        setLayout(new BorderLayout(10, 10));
        add(colorsAndMemoryPanel, BorderLayout.CENTER);
        
        //the panel containing the label
        JPanel colorLabelPanel=new JPanel();
        colorLabelPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        colorLabelPanel.add(colorLabel);
        colorLabelPanel.setPreferredSize(new Dimension(25, 30));
        colorLabelPanel.setBorder(new CompoundBorder(new EmptyBorder(1, 10, 1, 10), new SoftBevelBorder(SoftBevelBorder.LOWERED)));
        
        add(colorLabelPanel, BorderLayout.SOUTH);
    }
    
     /**
     * @see javax.swing.colorchooser.AbstractColorChooserPanel#getDisplayName()
     */
    public String getDisplayName() {

        return label;
    }
    
    /**
     * @see javax.swing.colorchooser.AbstractColorChooserPanel#getLargeDisplayIcon()
     */
    public Icon getLargeDisplayIcon() {

        return null;
    }
    
    /**
     * @see javax.swing.colorchooser.AbstractColorChooserPanel#getSmallDisplayIcon()
     */
    public Icon getSmallDisplayIcon() {

        return null;
    }
    
    /**
     * @see javax.swing.colorchooser.AbstractColorChooserPanel#updateChooser()
     */
    public void updateChooser() {


    }
}
