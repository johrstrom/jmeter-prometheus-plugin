package com.gitub.johrstrom.config.gui; //com.gitub.johrstrom.config.gui.PrometheusMetricsConfigGui

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.johrstrom.config.CollectorConfig;
import com.github.johrstrom.config.PrometheusMetricsConfig;

import io.prometheus.client.Collector.Type;

public class PrometheusMetricsConfigGui extends AbstractConfigGui implements ActionListener {
	
	private static final long serialVersionUID = 6741986237897976082L;
	private JTable table;
	private DefaultTableModel model;
	
	private Logger log = LoggerFactory.getLogger(PrometheusMetricsConfigGui.class);
	
	public enum Column {
		NAME("Metric Name"), 
		HELP("Help message"), 
		LABELS("Labels"),
		TYPE("Metric Type"), 
		QUANTILE_OR_BUCKETS("Quantiles or Buckets");
		
		private String printString;
		
		Column(String prettyPrint) {
			this.printString = prettyPrint;
		}
		
		public String toString() {
			return this.printString;
		}
	}
	
	private static final Object[] EMPTY_ROW = {"","","","",""}; 
	
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
		ArrayList<CollectorConfig> collectors = new ArrayList<>();
		PrometheusMetricsConfig config = (PrometheusMetricsConfig) ele;

		log.debug("modifying test element " + ele.toString() + ". row count in model is " + rows);
		
		for(int i = 0; i < rows; i++) {
			CollectorConfig cfg = new CollectorConfig();
			
			cfg.setHelp(this.getValueAt(i,Column.HELP.ordinal()));
			cfg.setMetricName(this.getValueAt(i,Column.NAME.ordinal()));
			cfg.setType(this.getValueAt(i,Column.TYPE.ordinal()).toString());
			cfg.setQuantileOrBucket(this.getValueAt(i,Column.QUANTILE_OR_BUCKETS.ordinal()));
			cfg.setLabels(this.getValueAt(i, Column.LABELS.ordinal()).split(","));
			
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
		
		int rows = this.model.getRowCount();
		for(int i = rows-1; i >= 0; i--) {
			this.model.removeRow(i);
			log.debug("removed row " + i + " from table.");
		}
		
		PropertyIterator it = collectors.iterator();
		while(it.hasNext()) {
			CollectorConfig cfg = (CollectorConfig) it.next().getObjectValue();
			String[] arr = new String[Column.values().length];
			
			arr[Column.NAME.ordinal()] = cfg.getMetricName();
			arr[Column.HELP.ordinal()] = cfg.getHelp();
			arr[Column.TYPE.ordinal()] = cfg.getType();
			arr[Column.QUANTILE_OR_BUCKETS.ordinal()] = cfg.getQuantileOrBucket();
			arr[Column.LABELS.ordinal()] = cfg.getLabelsAsString();
			 
			this.model.addRow(arr);
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
		String columns[] = Arrays.stream(Column.class.getEnumConstants()).map(Column::toString).toArray(String[]::new);
		this.model = new DefaultTableModel(new Object[][]{}, columns);
		//model.add
		this.table = new JTable(this.model);
		
		TableColumn typeColumn = table.getColumnModel().getColumn(Column.TYPE.ordinal());

		JComboBox<String> comboBox = new JComboBox<>();
		comboBox.addItem(Type.COUNTER.name());
		comboBox.addItem(Type.SUMMARY.name());
		comboBox.addItem(Type.HISTOGRAM.name());
		comboBox.addItem(Type.GAUGE.name());
		typeColumn.setCellEditor(new DefaultCellEditor(comboBox));
		
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
			this.model.addRow(EMPTY_ROW);
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







