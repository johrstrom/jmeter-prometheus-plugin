package org.ohrstrom.listener.gui;

import java.awt.BorderLayout;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.ohrstrom.listener.PrometheusListener;

public class PrometheusListenerGui extends AbstractListenerGui {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4984653136457108054L;

	public PrometheusListenerGui(){
		super();
		init();
	}
	
	public TestElement createTestElement() {		
		PrometheusListener list = new PrometheusListener(getStaticLabel());
		modifyTestElement(list);
		
		list.setProperty(TestElement.GUI_CLASS, org.ohrstrom.listener.gui.PrometheusListenerGui.class.getName());
		list.setProperty(TestElement.TEST_CLASS, org.ohrstrom.listener.PrometheusListener.class.getName());
		
		return list;
	}

	public String getLabelResource() {
		return getClass().getCanonicalName();
	}	

	@Override
	public String getStaticLabel() {
		return "Prometheus Listener";
	}

	public void modifyTestElement(TestElement arg0) {
		super.configureTestElement(arg0);
		
		configureTestElement(arg0);
	}
	
	
	
	
	@Override
	public String getName() {
		if (super.getName() == null) {
			return this.getStaticLabel();
		}else{
			return super.getName();
		}
	}


	@Override
	public void configure(TestElement element) {
		super.configure(element);
	}

	private void init(){
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		

	}

}
