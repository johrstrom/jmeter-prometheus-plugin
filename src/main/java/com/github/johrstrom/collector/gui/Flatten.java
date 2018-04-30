package com.github.johrstrom.collector.gui;

import org.apache.jorphan.reflect.Functor;

public interface Flatten {
	
	public Functor[] getReadFunctors();
	public Functor[] getWriteFunctors();
	public String[] getHeaders();
	public Class<?>[] getEditorClasses();
}
