package com.github.johrstrom.listener.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ComboBoxEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableColumn;

import org.apache.jorphan.reflect.Functor;

import com.github.johrstrom.collector.gui.AbstractCollectorTable;
import com.github.johrstrom.collector.gui.Flatten;
import com.github.johrstrom.config.gui.ConfigCollectorTable;
import com.github.johrstrom.listener.ListenerCollectorConfig;
import com.github.johrstrom.listener.ListenerCollectorConfig.Measurable;

public class ListenerCollectorTable extends AbstractCollectorTable<ListenerCollectorConfig> 
	implements Flatten {



	private static final long serialVersionUID = 4429063284832140575L;
	
	public static JComboBox<String> listenToComboBox, measuringComboBox; 
	private static ConfigCollectorTable stealFrom = new ConfigCollectorTable();
	
	public static int LISTEN_TO_INDEX = ConfigCollectorTable.BASE_COLUMN_SIZE;
	public static int MEARSURING_INDEX = LISTEN_TO_INDEX + 1;
	
	static {
		listenToComboBox = new JComboBox<>();
		listenToComboBox.addItem(ListenerCollectorConfig.SAMPLES);
		listenToComboBox.addItem(ListenerCollectorConfig.ASSERTIONS);
		
		measuringComboBox = measuringBox();
	}
	
	public ListenerCollectorTable() {
		super(ListenerCollectorConfig.class);
	}
	
	@Override
	public Flatten getGuiHelper() {
		return this;
	}

	@Override
	public void modifyColumns() {
		TableColumn column = table.getColumnModel().getColumn(ConfigCollectorTable.TYPE_INDEX);
		column.setCellEditor(new DefaultCellEditor(ConfigCollectorTable.typeComboBox));
		
		column = table.getColumnModel().getColumn(ListenerCollectorTable.LISTEN_TO_INDEX);
		column.setCellEditor(new DefaultCellEditor(listenToComboBox));
		
		column = table.getColumnModel().getColumn(ListenerCollectorTable.MEARSURING_INDEX);
		column.setCellEditor(new DefaultCellEditor(measuringComboBox));
	}
	
	@Override
	public Functor[] getReadFunctors() {
	
		List<Functor> functors = new ArrayList<>(Arrays.asList(stealFrom.getReadFunctors()));
		
		functors.add(new Functor("getListenTo"));
		functors.add(new Functor("getMeasuring"));
		
		return functors.toArray(new Functor[functors.size()]);
	}

	@Override
	public Functor[] getWriteFunctors() {
		List<Functor> functors = new ArrayList<>(Arrays.asList(stealFrom.getWriteFunctors()));
		
		functors.add(new Functor("setListenTo"));
		functors.add(new Functor("setMeasuring"));
		
		return functors.toArray(new Functor[functors.size()]);
	}

	@Override
	public String[] getHeaders() {
		List<String> headers = new ArrayList<>(Arrays.asList(stealFrom.getHeaders()));
		
		headers.add("Listen to");
		headers.add("Measuring");
		
		return headers.toArray(new String[headers.size()]);
	}

	@Override
	public Class<?>[] getEditorClasses() {
		List<Class<?>> editors = new ArrayList<>(Arrays.asList(stealFrom.getEditorClasses()));
		
		editors.add(ComboBoxEditor.class);
		editors.add(ComboBoxEditor.class);
		
		return editors.toArray(new Class<?>[editors.size()]);
	}
	
	public static JComboBox<String> measuringBox() {
		JComboBox<String> box = new JComboBox<String>();
		for (Measurable value : Measurable.values()) {
			box.addItem(value.toString());
		}
		return box;
	}

}
