/*
 * Created on 11 mai 2005
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
package fr.itris.glips.svgeditor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 * the dialog that will display a progress bar
 * 
 * @author Jordi SUC
 */
public class SVGProgressBarDialog extends JDialog{

    /**
     * the progress bar
     */
    private JProgressBar progressBar;
    
    /**
     * whether the action should be cancelled or not
     */
    private boolean cancelAction=false;
    
    /**
     * the runnable used to dispose the listeners
     */
    private Runnable disposeRunnable=null;
    
    /**
     * the labels
     */
    private String initLabel="", processingLabel=""; //$NON-NLS-1$ //$NON-NLS-2$
    
    /**
     * the min and the max
     */
    private int min=0, max=0, currentValue=0;
    
    /**
     * the cancel runnable
     */
    private Runnable cancelRunnable=null;

    /**
     * the constructor of the class
     * @param mainFrame the main frame
     * @param title the title of the dialog
     */
    public SVGProgressBarDialog(JFrame mainFrame, String title){
        
        super(mainFrame, false);
        
        String dialogTitle=title, cancelLabel=""; //$NON-NLS-1$
        ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
		    
			try{
			    cancelLabel=bundle.getString("labelcancel"); //$NON-NLS-1$
                initLabel=bundle.getString("progressbarinitialize"); //$NON-NLS-1$
                processingLabel=bundle.getString("progressbarprocessing"); //$NON-NLS-1$
			}catch (Exception ex){}
		}
		
		setTitle(dialogTitle);

        //creating the progress bar
        progressBar=new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(400, 30));
        
        //creating the panel containing the progress bar
        JPanel progressBarPanel=new JPanel();
        progressBarPanel.setLayout(new BoxLayout(progressBarPanel, BoxLayout.X_AXIS));
        progressBarPanel.add(progressBar);
        progressBarPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        //building the dialog
        getContentPane().setLayout(new BorderLayout(0, 0));
        
        //the cancel button
        final JButton cancelButton=new JButton(cancelLabel);

        //adding the listener to the cancel button
        final ActionListener cancelButtonListener=new ActionListener(){

            public void actionPerformed(ActionEvent evt) {

                cancelAction=true;
                
                if(cancelRunnable!=null){
                	
                	cancelRunnable.run();
                }
                
                disposeRunnable.run();
            }
        };
        
        cancelButton.addActionListener(cancelButtonListener);
        
        //the listener to the close button
        final WindowAdapter windowAdapter=new WindowAdapter(){

        	@Override
            public void windowClosing(WindowEvent evt) {

                cancelAction=true;
                disposeRunnable.run();
            }
        }; 
        
        addWindowListener(windowAdapter);
        
        //the runnable used to remove the listeners
        disposeRunnable=new Runnable(){

	        public void run() {

	            cancelButton.removeActionListener(cancelButtonListener);
                removeWindowListener(windowAdapter);
                
                setVisible(false);
	        } 
        };
        
        //the panel containing the cancel button
        JPanel buttonsPanel=new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.add(cancelButton);
        
        getContentPane().add(progressBarPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
        
        //setting the bounds of the dialog
        pack();
        
        Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width/2-getWidth()/2, screenSize.height/2-getHeight()/2);
    }
    
    /**
     * disposes the dialog
     */
    public void disposeDialog() {
    	
        if(disposeRunnable!=null) {
        	
        	disposeRunnable.run();
        }
    }
    
    /**
     * sets the cancel runnable
     * @param runnable a runnable
     */
    public void setCancelRunnable(Runnable runnable){
    	
    	this.cancelRunnable=runnable;
    }

    /**
     * @return Returns the cancelAction.
     */
    public boolean cancelAction() {
        return cancelAction;
    }

    /**
	 * @param max The max to set.
	 */
	public void setMax(int max) {
		this.max = max;
	}

	/**
	 * @param min The min to set.
	 */
	public void setMin(int min) {
		this.min = min;
	}
	
	/**
     * setting the value of the progress bar
     * @param currentValue the current value : minValue<=currentValue<=maxValue
     */
	public void incrementProgressBarValue(){
		
		currentValue++;
		final int value=currentValue;

        if(value>=min && value<=max){
            
            double progressBarValue=((double)value)/(double)((max-min))*100;

            progressBar.setValue((int)progressBarValue);
        }
	}

	/**
     * setting the value of the progress bar
     * @param currentValue the current value : minValue<=currentValue<=maxValue
     * @param minValue the min value
     * @param maxValue the max value
     */
    public void setProgressBarValue(final double currentValue, final double minValue, final double maxValue){
        
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                
                if(currentValue>=minValue && currentValue<=maxValue){
                    
                    double progressBarValue=currentValue/(maxValue-minValue)*100;

                    progressBar.setValue((int)progressBarValue);
                }
            }
        });
    }
    
	/**
     * setting the value of the progress bar
     * @param currentValue the current value : minValue<=currentValue<=maxValue
     * @param minValue the min value
     * @param maxValue the max value
     * @param progressBarLabel the progress bar label
     */
    public void setProgressBarValueThreadSafe(double currentValue, double minValue, double maxValue, String progressBarLabel){
        
        if(currentValue>=minValue && currentValue<=maxValue){
            
            progressBar.setString(progressBarLabel);
            double progressBarValue=currentValue/(maxValue-minValue)*100;
            progressBar.setValue((int)progressBarValue);
        }
    }
    
    /**
     * sets whether the progress bar is in an indeterminate state
     * @param indeterminate whether the progress bar is in an indeterminate state or not
     * @param initializing whether the displayed string should be an initializing info or a processing info
     */
    public void setIndeterminate(final boolean indeterminate, final boolean initializing) {

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                progressBar.setIndeterminate(indeterminate);
                
                if(indeterminate) {
                    
                    if(initializing) {
                        
                        progressBar.setString(initLabel);
                        
                    }else {
                        
                        progressBar.setString(processingLabel);
                    }

                }else {
                    
                    progressBar.setString(null);
                }
            }
        });

    }
    
    /**
     * sets whether the progress bar is in an indeterminate state
     * @param indeterminate whether the progress bar is in an indeterminate state or not
     * @param initializing whether the displayed string should be an initializing info or a processing info
     */
    public void setIndeterminateThreadSafe(final boolean indeterminate, final boolean initializing) {

        if(indeterminate) {
            
            if(initializing) {
                
                progressBar.setString(initLabel);
                
            }else {
                
                progressBar.setString(processingLabel);
            }

        }else {
            
            progressBar.setString(null);
        }
    }
    
}
