package fr.itris.glips.svgeditor.canvas;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import fr.itris.glips.svgeditor.*;

/**
 * the class of the scroll bar us
 * @author Jordi
 */
public class CanvasToolBar extends JToolBar{

	/**
	 * the scrollpane
	 */
	private SVGScrollPane scrollpane=null;

	/**
	 * the dispose runnables
	 */
	private HashSet<Runnable> disposeRunnables=new HashSet<Runnable>();
	
	/**
	 * the update runnables
	 */
	private HashSet<Runnable> updateRunnables=new HashSet<Runnable>();
	
	/**
	 * the constructor of the class
	 * @param scrollpane the scrollpane
	 */
	public CanvasToolBar(SVGScrollPane scrollpane){
		
		this.scrollpane=scrollpane;
		setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		
		//creating the zoom widget
		//JComboBox comboBox=getZoomWidget();
		
		
	}
	
	/**
	 * @return the zoom widget
	 */
	protected JComboBox getZoomWidget(){
		
		final JComboBox combo=new JComboBox();
		combo.setEditable(false);
		
		//creating the map of the values
		final HashMap<Double, ComboItem> map=new HashMap<Double, ComboItem>();
		
		//adding the other zoom factors
		map.put(0.05, new ComboItem("5 %", 0.05)); //$NON-NLS-1$
		map.put(0.1, new ComboItem("10 %", 0.1)); //$NON-NLS-1$
		map.put(0.2, new ComboItem("20 %", 0.2)); //$NON-NLS-1$
		map.put(0.5, new ComboItem("50 %", 0.5)); //$NON-NLS-1$
		map.put(0.75, new ComboItem("75 %", 0.75)); //$NON-NLS-1$
		map.put(1.0, new ComboItem("100 %", 1)); //$NON-NLS-1$
		map.put(1.5, new ComboItem("150 %", 1.5)); //$NON-NLS-1$
		map.put(2.0, new ComboItem("200 %", 2)); //$NON-NLS-1$
		map.put(2.5, new ComboItem("250 %", 2.5)); //$NON-NLS-1$
		map.put(3.0, new ComboItem("300 %", 3)); //$NON-NLS-1$
		map.put(4.0, new ComboItem("400 %", 4)); //$NON-NLS-1$
		map.put(5.0, new ComboItem("500 %", 5)); //$NON-NLS-1$
		map.put(6.0, new ComboItem("600 %", 6)); //$NON-NLS-1$
		map.put(7.0, new ComboItem("700 %", 7)); //$NON-NLS-1$
		map.put(7.5, new ComboItem("750 %", 7.5)); //$NON-NLS-1$
		map.put(8.0, new ComboItem("800 %", 8)); //$NON-NLS-1$
		map.put(9.0, new ComboItem("900 %", 9)); //$NON-NLS-1$
		map.put(10.0, new ComboItem("1000 %", 10)); //$NON-NLS-1$
		
		//adding the current zoom item
		HashMap<Double, ComboItem> cmap=new HashMap<Double, ComboItem>(map);
		double currentScale=scrollpane.getSVGCanvas().getScale();
		ComboItem initialItem=new ComboItem(SVGEditor.getFormat().format(currentScale*100)+" %", currentScale); //$NON-NLS-1$
		cmap.put(currentScale, initialItem);
		
		//adding the items
		for(double scale : cmap.keySet()){
			
			combo.addItem(cmap.get(scale));
		}
		
		//setting the selected item
		combo.setSelectedItem(initialItem);
		
		//adding the listener to the combo
		final ActionListener comboListener=new ActionListener(){
			
			public void actionPerformed(ActionEvent evt) {

				//getting the current item
				ComboItem item=(ComboItem)combo.getSelectedItem();
				
				if(item!=null){
					
					scrollpane.renderZoom(item.getValue());
				}
			}
		};
		
		combo.addActionListener(comboListener);
		
		//adding an update runnable
		updateRunnables.add(new Runnable(){
			
			public void run() {

				//checking whether the combo box should be updated
				boolean shouldUpdate=true;
				double cScale=scrollpane.getSVGCanvas().getScale();
				ComboItem item=(ComboItem)combo.getSelectedItem();
				
				if(item!=null){
					
					double comboScale=item.getValue();
					
					if(comboScale==cScale){
						
						shouldUpdate=false;
					}
				}
				
				if(shouldUpdate){
					
					//removing the combo listener
					combo.removeActionListener(comboListener);
					
					if(map.containsKey(cScale)){
						
						item=map.get(cScale);
						combo.setSelectedItem(item);
						
					}else{
						
						//creating the new map of the combo
						HashMap<Double, ComboItem> newMap=new HashMap<Double, ComboItem>(map);
						item=new ComboItem(SVGEditor.getFormat().format(cScale*100)+" %", cScale); //$NON-NLS-1$
						newMap.put(cScale, item);
						combo.removeAllItems();

						//adding the items
						for(double scale : newMap.keySet()){
							
							combo.addItem(newMap.get(scale));
						}
						
						//setting the selected item
						combo.setSelectedItem(item);
						
					}

					//adding the combo listener
					combo.addActionListener(comboListener);
				}
			}
		});
		
		//adding a dispose runnable
		disposeRunnables.add(new Runnable(){
			
			public void run() {

				combo.removeActionListener(comboListener);
			}
		});
		
		return combo;
	}
	
	/**
	 * disposing the tool bar
	 */
	public void dispose(){
		
		for(Runnable runnable : disposeRunnables){
			
			runnable.run();
		}
	}
	
	/**
	 * updates the tool bar
	 */
	public void update(){
		
		for(Runnable runnable : updateRunnables){
			
			runnable.run();
		}
	}
	
	/**
	 * the class of the item of the zoom combo
	 * @author Jordi SUC
	 */
	protected class ComboItem{
		
		/**
		 * the label
		 */
		private String label=""; //$NON-NLS-1$
		
		/**
		 * the zoom factor
		 */
		private double value=1.0;
		
		/**
		 * the constructor of the class
		 * @param label a label
		 * @param value the zoom factor
		 */
		protected ComboItem(String label, double value){
			
			this.label=label;
			this.value=value;
		}

		@Override
		public String toString() {
			return label;
		}

		/**
		 * @return Returns the value.
		 */
		public double getValue() {
			return value;
		}
	}

}
