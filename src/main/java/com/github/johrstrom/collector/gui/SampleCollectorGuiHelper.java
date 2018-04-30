package com.github.johrstrom.collector.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;

import org.apache.jorphan.reflect.Functor;


public class SampleCollectorGuiHelper extends BaseCollectorGuiHelper implements Flatten {

	public static JComboBox<String> listenToComboBox; 
	
	public static int LISTEN_TO_INDEX = BASE_COLUMN_SIZE;
	
	static {
		listenToComboBox = new JComboBox<>();
		listenToComboBox.addItem("samples");
		listenToComboBox.addItem("assertions");
	}
	
	@Override
	public Functor[] getReadFunctors() {
		List<Functor> functors = new ArrayList<>(Arrays.asList(super.getReadFunctors()));
		
		functors.add(new Functor("getListenTo"));
		
		return functors.toArray(new Functor[functors.size()]);
	}

	@Override
	public Functor[] getWriteFunctors() {
		List<Functor> functors = new ArrayList<>(Arrays.asList(super.getWriteFunctors()));
		
		functors.add(new Functor("setListenTo"));
		
		return functors.toArray(new Functor[functors.size()]);
	}

	@Override
	public String[] getHeaders() {
		List<String> headers = new ArrayList<>(Arrays.asList(super.getHeaders()));
		
		headers.add("Listen to");
		
		return headers.toArray(new String[headers.size()]);
	}

	@Override
	public Class<?>[] getEditorClasses() {
		List<Class<?>> editors = new ArrayList<>(Arrays.asList(super.getEditorClasses()));
		
		editors.add(ComboBoxEditor.class);
		
		return editors.toArray(new Class<?>[editors.size()]);
	}

}
