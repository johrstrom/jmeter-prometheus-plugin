package com.github.johrstrom.config.gui;

import java.util.Arrays;
import java.util.Collection;

import javax.swing.DefaultCellEditor;
import javax.swing.JPopupMenu;
import javax.swing.table.TableColumn;

import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.GUIFactory;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;

import com.github.johrstrom.collector.BaseCollectorConfig;
import com.github.johrstrom.collector.gui.AbstractCollectorGui;
import com.github.johrstrom.collector.gui.BaseCollectorGuiHelper;
import com.github.johrstrom.collector.gui.Flatten;
import com.github.johrstrom.collector.gui.SampleCollectorGuiHelper;
import com.github.johrstrom.config.PrometheusMetricsConfig;


public class PrometheusMetricsConfigGui extends AbstractCollectorGui<BaseCollectorConfig>  {
	
	private static final long serialVersionUID = 6741986237897976082L;
	
	static {
		GUIFactory.registerIcon(PrometheusMetricsConfigGui.class.getName(),GUIFactory.getIcon(AbstractConfigGui.class));
		GUIFactory.registerIcon(PrometheusMetricsConfig.class.getName(),GUIFactory.getIcon(AbstractConfigGui.class));
	}
	
	public PrometheusMetricsConfigGui(){
		super(BaseCollectorConfig.class);
	}
	
	@Override
	public TestElement createTestElement() {
		if(this.getCollector() == null)
			this.setCollector(new PrometheusMetricsConfig());
		
		this.getCollector().setProperty(TestElement.GUI_CLASS, PrometheusMetricsConfigGui.class.getName());
		this.getCollector().setProperty(TestElement.TEST_CLASS, PrometheusMetricsConfig.class.getName());
		this.modifyTestElement(getCollector());
		
		return (TestElement) getCollector();
	}

	@Override
	public String getLabelResource() {
		return getClass().getCanonicalName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.gui.AbstractJMeterGuiComponent#getStaticLabel()
	 */
	@Override
	public String getStaticLabel() {
		return "Prometheus Metrics";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.gui.AbstractJMeterGuiComponent#getName()
	 */
	@Override
	public String getName() {
		if (super.getName() == null) {
			return this.getStaticLabel();
		} else {
			return super.getName();
		}
	}

	@Override
	public JPopupMenu createPopupMenu() {
		return MenuFactory.getDefaultConfigElementMenu();
	}

	@Override
	public Collection<String> getMenuCategories() {
		 return Arrays.asList(MenuFactory.CONFIG_ELEMENTS);
	}

	@Override
	public Flatten getGuiHelper() {
		return new BaseCollectorGuiHelper();
	}

	@Override
	public void modifyColumns() {
		TableColumn column = table.getColumnModel().getColumn(SampleCollectorGuiHelper.TYPE_INDEX);
		column.setCellEditor(new DefaultCellEditor(SampleCollectorGuiHelper.typeComboBox));
	}


	

}







