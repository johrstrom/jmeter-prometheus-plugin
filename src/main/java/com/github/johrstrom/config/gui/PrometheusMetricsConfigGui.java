package com.github.johrstrom.config.gui;

import java.awt.BorderLayout;
import java.util.List;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.johrstrom.collector.BaseCollectorConfig;
import com.github.johrstrom.collector.CollectorElement;
import com.github.johrstrom.config.PrometheusMetricsConfig;


public class PrometheusMetricsConfigGui<C> extends AbstractConfigGui {
	
	private static final long serialVersionUID = 6741986237897976082L;
//	private PrometheusMetricsConfig config;
	private ConfigCollectorTable table = new ConfigCollectorTable();

	private Logger log = LoggerFactory.getLogger(PrometheusMetricsConfigGui.class);
	
	public PrometheusMetricsConfigGui(){
		super();
		log.debug("making a new config gui: {}", this.toString());
		init();
	}
	
	@Override
	public TestElement createTestElement() {
		PrometheusMetricsConfig cfg = new PrometheusMetricsConfig();
		
		cfg.setProperty(TestElement.GUI_CLASS, PrometheusMetricsConfigGui.class.getName());
		cfg.setProperty(TestElement.TEST_CLASS, PrometheusMetricsConfig.class.getName());
		this.modifyTestElement(cfg);
		
		return cfg;
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
		return super.getName() == null ? this.getStaticLabel() : super.getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void modifyTestElement(TestElement ele) {
		
		if(!(ele instanceof CollectorElement)) {
			return;
		}
		
		CollectorElement<BaseCollectorConfig> config = (CollectorElement<BaseCollectorConfig>) ele;
		
		List<BaseCollectorConfig> collectors = this.table.getRowsAsCollectors();
		config.setCollectorConfigs(collectors);	
		

		config.setName(this.getName());
		config.setComment(this.getComment());
	}

	private void init() {
		this.setLayout(new BorderLayout(0, 5));
		
		this.add(makeTitlePanel(), BorderLayout.NORTH);
		this.add(this.table, BorderLayout.CENTER);
	}
	
	

	@SuppressWarnings("unchecked")
	@Override
	public void configure(TestElement ele) {
		super.configure(ele);
		
		if(ele instanceof CollectorElement<?>) {
			try {
				this.table.populateTable((CollectorElement<BaseCollectorConfig>) ele);
			} catch(Exception e) {
				log.error("didn't modify test element because {}:{}", e.getClass(), e.getMessage());
			}
		}
		
		this.setName(ele.getName());
		this.setComment(ele.getComment());
	}

	@Override
	public void clearGui() {
		super.clearGui();
		this.table.clearModelData();
	}
	
	@Override
	protected PrometheusMetricsConfigGui<C> clone() throws CloneNotSupportedException {
		return new PrometheusMetricsConfigGui<C>();
	}
	
	
	
}







