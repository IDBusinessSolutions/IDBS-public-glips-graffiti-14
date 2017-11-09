package fr.itris.glips.svgeditor.menutool;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import fr.itris.glips.svgeditor.*;

/**
 * @author Jordi SUC
 *
 * the class creating and managing the color toolbar
 */
public class ColorToolBar extends JToolBar{
	
	/**
	 * the editor
	 */
	private SVGEditor editor;
	
	/**
	 * the constructor of the class
	 * @param editor the editor
	 */
	public ColorToolBar(SVGEditor editor){
		
		super(SwingConstants.HORIZONTAL);
		
		this.editor=editor;
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		//setting the properties of the tool bar
		setRollover(true);
		setFloatable(true);
		setBorderPainted(false);
		setMargin(new Insets(0, 0, 0, 0));
	}
	
	/**
	 * @return editor the editor
	 */
	public SVGEditor getSVGEditor(){
		return editor;
	}
	
	/**
	 * layout elements in the tool bar
	 */
	public void layoutElements(){

		//getting the color button
		 JButton currentColorButton=getColorButton();
		 add(currentColorButton);
		 addSeparator();
	}

	/**
	 * creating and returning the color button
	 * @return the color button
	 */
	protected JButton getColorButton(){
		
		//getting the label
		String labelcolortooltip=""; //$NON-NLS-1$
		ResourceBundle bundle=SVGEditor.getBundle();
		
		if(bundle!=null){
			
			try{
				labelcolortooltip=bundle.getString("labelcolortooltip"); //$NON-NLS-1$
			}catch(Exception ex){}
		}
		
		//the button displaying the current color
		final JButton colorButton=new JButton();
		Insets buttonInsets=new Insets(1, 1, 1, 1);
		colorButton.setMargin(buttonInsets);
		colorButton.setToolTipText(labelcolortooltip);
		
		final JPanel colorPanel=new JPanel();
		colorPanel.setPreferredSize(new Dimension(20, 15));
		colorPanel.setBackground(getSVGEditor().getSVGColorManager().getCurrentColor());
		colorPanel.setBorder(new LineBorder(Color.white));
		colorButton.add(colorPanel);
		
		//the listener to the color button actions
		final ActionListener buttonListener=new ActionListener(){
			
			public void actionPerformed(ActionEvent evt) {
				
            	Color color=SVGEditor.getColorChooser().showColorChooserDialog(SVGEditor.getColorChooser().getColor());
                
				if(color!=null){
					
					editor.getSVGColorManager().setCurrentColor(color);
				}
                
				colorButton.setSelected(false);
			}
		};
		
		colorButton.addActionListener(buttonListener);
		
		//adding the listener to the changes of the current color
		getSVGEditor().getSVGColorManager().addColorListener(new SVGColorManager.ColorListener(){
			
			public void colorChanged(Color color) {
				
				colorPanel.setBackground(color);
			} 
		});
		
		/**************drag and drop support************************/
		
		//the transferable
		final Transferable transferable=new Transferable(){
			
			/**
			 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
			 */
			public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
				
				if(df!=null && getSVGEditor().getColorChooser().isColorDataFlavor(df)){
					
					return colorPanel.getBackground();
				}
				
				return null;
			}
			
			/**
			 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
			 */
			public DataFlavor[] getTransferDataFlavors(){
				
				Collection dataFlavors=getSVGEditor().getColorChooser().getColorDataFlavors();
				DataFlavor df=null;
				
				//the array that will be returned
				DataFlavor[] dfArray=new DataFlavor[dataFlavors.size()];
				int i=0;
				
				for(Iterator it=dataFlavors.iterator(); it.hasNext();){
					
					df=(DataFlavor)it.next();
					
					if(df!=null){
						
						dfArray[i]=df;
						i++;
					}
				}
				
				return dfArray;
			}
			
			/**
			 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
			 */
			public boolean isDataFlavorSupported(DataFlavor df) {
				
				if(df!=null && getSVGEditor().getColorChooser().isColorDataFlavor(df)){
					
					return true;
				}
				
				return false;
			}
		};
		
		//adding a drag gesture listener to the color panel
		DragGestureListener dragGestureListener=new DragGestureListener(){
			
			public void dragGestureRecognized(DragGestureEvent evt) {
				
				getSVGEditor().getDragSource().startDrag(
						evt,
						getSVGEditor().getSVGToolkit().createCursorImageFromColor(colorPanel.getBackground()), 
						null, 
						new Point(5, 5), 
						transferable, 
						new DragSourceAdapter(){});
				
				colorButton.removeActionListener(buttonListener);
				colorButton.doClick();
				colorButton.setSelected(false);
				colorButton.addActionListener(buttonListener);
			}
		};
		
		//adding a drag gesture tokenizer to this component
		getSVGEditor().getDragSource().createDefaultDragGestureRecognizer(
				colorButton, DnDConstants.ACTION_COPY, dragGestureListener);
		
		return colorButton;
	}
}