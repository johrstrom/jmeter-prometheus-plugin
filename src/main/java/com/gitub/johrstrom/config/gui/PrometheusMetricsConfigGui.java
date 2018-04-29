package com.gitub.johrstrom.config.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jorphan.gui.ObjectTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.johrstrom.collector.BaseCollectorConfig;
import com.github.johrstrom.collector.BaseCollectorGuiHelper;
import com.github.johrstrom.config.PrometheusMetricsConfig;


public class PrometheusMetricsConfigGui extends AbstractConfigGui implements ActionListener {
	
	private static final long serialVersionUID = 6741986237897976082L;
	private JTable table;
	private ObjectTableModel model;
	
	private Logger log = LoggerFactory.getLogger(PrometheusMetricsConfigGui.class);

	
	private JButton add,delete;
	
	private static final String ADD = "Add";
	private static final String DELETE = "Delete";

	public PrometheusMetricsConfigGui(){
		super();
		this.createGUI();
		
	}
	
	@Override
	public TestElement createTestElement() {
		PrometheusMetricsConfig config = new PrometheusMetricsConfig();
		
		config.setProperty(TestElement.GUI_CLASS, PrometheusMetricsConfigGui.class.getName());
		config.setProperty(TestElement.TEST_CLASS, PrometheusMetricsConfig.class.getName());
		this.modifyTestElement(config);
		
		return config;
	}

	@Override
	public String getLabelResource() {
		return getClass().getCanonicalName();
	}

	@Override
	public void modifyTestElement(TestElement ele) {
		if(!(ele instanceof PrometheusMetricsConfig)) {
			return;
		}
		
		int rows = this.model.getRowCount();
		ArrayList<BaseCollectorConfig> collectors = new ArrayList<>();
		PrometheusMetricsConfig config = (PrometheusMetricsConfig) ele;

		log.debug("modifying test element " + ele.toString() + ". row count in model is " + rows);
		
		@SuppressWarnings("unchecked")
		Iterator<BaseCollectorConfig> iter = (Iterator<BaseCollectorConfig>) model.iterator();
		
		while(iter.hasNext()) {
			BaseCollectorConfig cfg = iter.next();
			collectors.add(cfg);
			log.debug("populated config: " + cfg.toString() + " from table.");
		}
		
		config.setCollectorDefinitions(collectors);
	}
	
	public String getValueAt(int row, int column) {
		String value = this.model.getValueAt(row, column).toString();
		log.debug(String.format("retrieved %s from table from position (%d,%d)", value,row,column));
		return value;
	}
	
	
	
	@Override
	public void configure(TestElement element) {
		super.configure(element);
		
		if(!(element instanceof PrometheusMetricsConfig)) {
			return;
		}
		PrometheusMetricsConfig config = (PrometheusMetricsConfig) element;
		CollectionProperty collectors = config.getCollectorDefinitions();
		log.debug("Configuring GUI with " + collectors.size() + " collectors.");
		
		this.model.clearData();
		
		PropertyIterator it = collectors.iterator();
		while(it.hasNext()) {
			BaseCollectorConfig cfg = (BaseCollectorConfig) it.next().getObjectValue();			 
			this.model.addRow(cfg);
			log.debug("added row into table: " + cfg.toString());
		}
		
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
	
	/**
	 * Private helper function to initialize all the Swing components.
	 */
	protected void createGUI() {
		this.setLayout(new BorderLayout(0, 5));
		this.setBorder(makeBorder());
		
		this.add(makeTitlePanel(), BorderLayout.NORTH);
		this.add(makeMainPanel(), BorderLayout.CENTER);
	}
	

	protected JPanel makeMainPanel() {
		VerticalPanel panel = new VerticalPanel();
		
		panel.add(makeTablePanel());
		panel.add(makeButtonPanel());
		
		return panel;
	}
	
	protected Component makeTablePanel() {
		BaseCollectorGuiHelper helper = new BaseCollectorGuiHelper();
		this.model = new ObjectTableModel(
				helper.getHeaders(),
				BaseCollectorConfig.class,
				helper.getReadFunctors(),
				helper.getWriteFunctors(),
				helper.getEditorClasses()
		);
		//model.add
		this.table = new JTable(this.model);
		
		TableColumn typeColumn = table.getColumnModel().getColumn(BaseCollectorGuiHelper.TYPE_INDEX);
		typeColumn.setCellEditor(new DefaultCellEditor(BaseCollectorGuiHelper.typeComboBox));
		
		JScrollPane scrollPane = new JScrollPane(this.table);
		table.setFillsViewportHeight(true);
		
		return scrollPane;
	}
	
	protected JPanel makeButtonPanel() {
		
		add = new JButton(ADD); 
		add.setActionCommand(ADD);
		add.setEnabled(true);
		add.addActionListener(this);
		
		delete = new JButton(DELETE); 
		delete.setActionCommand(DELETE);
		delete.setEnabled(true);
		delete.addActionListener(this);
		
		HorizontalPanel panel = new HorizontalPanel();
		panel.add(add);
		panel.add(delete);
		
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		switch (event.getActionCommand()) {
		case ADD:
			this.model.addRow(new BaseCollectorConfig());
			break;
		case DELETE:
			deleteSelectedRows();
			break;
		default:
			break;
		}
	}
	
	protected void deleteSelectedRows() {
		int[] rows = table.getSelectedRows();
		
		for(int i = 0; i < rows.length; i++) {
			this.model.removeRow(rows[i]);
		}
	}
	

}







