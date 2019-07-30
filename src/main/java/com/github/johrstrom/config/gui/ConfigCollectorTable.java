package com.github.johrstrom.config.gui;

import javax.swing.ComboBoxEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableColumn;

import org.apache.jorphan.reflect.Functor;

import com.github.johrstrom.collector.BaseCollectorConfig;
import com.github.johrstrom.collector.BaseCollectorConfig.JMeterCollectorType;
import com.github.johrstrom.collector.gui.AbstractCollectorTable;
import com.github.johrstrom.collector.gui.Flatten;


public class ConfigCollectorTable extends AbstractCollectorTable<BaseCollectorConfig>  
	implements Flatten {

	public static JComboBox<String> typeComboBox; 
	
	public static int METRIC_NAME_INDEX = 0;
	public static int HELP_INDEX = 1;
	public static int LABEL_NAME_INDEX = 2;
	public static int TYPE_INDEX = 3;
	public static int QUANTILE_OR_BUCKET_INDEX = 4;
	public static int MAX_AGE_SECONDS_INDEX = 5;
	public static int BASE_COLUMN_SIZE = 6;
	
	private static final long serialVersionUID = 8675797078488652676L;
	
	static {
		typeComboBox = new JComboBox<>();
		typeComboBox.addItem(JMeterCollectorType.COUNTER.toString());
		typeComboBox.addItem(JMeterCollectorType.SUMMARY.toString());
		typeComboBox.addItem(JMeterCollectorType.HISTOGRAM.toString());
		typeComboBox.addItem(JMeterCollectorType.GAUGE.toString());
		typeComboBox.addItem(JMeterCollectorType.SUCCESS_RATIO.toString());
	}
	
	public ConfigCollectorTable() {
		super(BaseCollectorConfig.class);
	}

	@Override
	public Flatten getGuiHelper() {
		return this;
	}

	@Override
	public void modifyColumns() {
		TableColumn column = this.table.getColumnModel().getColumn(TYPE_INDEX);
		column.setCellEditor(new DefaultCellEditor(typeComboBox));
	}
		
	@Override
	public Functor[] getReadFunctors() {
		Functor[] functors = new Functor[BASE_COLUMN_SIZE];
		
		functors[METRIC_NAME_INDEX] = new Functor("getMetricName");
		functors[HELP_INDEX] = new Functor("getHelp");
		functors[LABEL_NAME_INDEX] = new Functor("getLabelsAsString");
		functors[TYPE_INDEX] = new Functor("getType");
		functors[QUANTILE_OR_BUCKET_INDEX] = new Functor("getQuantileOrBucket");
		functors[MAX_AGE_SECONDS_INDEX] = new Functor("getMaxAgeSeconds");

		return functors;
	}

	@Override
	public Functor[] getWriteFunctors() {
		Functor[] functors = new Functor[BASE_COLUMN_SIZE];
		
		functors[METRIC_NAME_INDEX] = new Functor("setMetricName");
		functors[HELP_INDEX] = new Functor("setHelp");
		functors[LABEL_NAME_INDEX] = new Functor("setLabels");
		functors[TYPE_INDEX] = new Functor("setType");
		functors[QUANTILE_OR_BUCKET_INDEX] = new Functor("setQuantileOrBucket");
		functors[MAX_AGE_SECONDS_INDEX] = new Functor("setMaxAgeSeconds");
		return functors;
	}

	@Override
	public String[] getHeaders() {
		String[] headers = new String[BASE_COLUMN_SIZE];
		
		headers[METRIC_NAME_INDEX] = "Name";
		headers[HELP_INDEX] = "Help";
		headers[LABEL_NAME_INDEX] = "Labels";
		headers[TYPE_INDEX] = "Type";
		headers[QUANTILE_OR_BUCKET_INDEX] = "Buckets or Quantiles";
		headers[MAX_AGE_SECONDS_INDEX] = "Max Age Seconds";
		
		return headers;
	}

	@Override
	public Class<?>[] getEditorClasses() {
		Class<?>[] clazzes = new Class<?>[BASE_COLUMN_SIZE];
		
		clazzes[METRIC_NAME_INDEX] = String.class;
		clazzes[HELP_INDEX] = String.class;
		clazzes[LABEL_NAME_INDEX] = String.class;
		clazzes[TYPE_INDEX] = ComboBoxEditor.class;
		clazzes[QUANTILE_OR_BUCKET_INDEX] = String.class;
		clazzes[MAX_AGE_SECONDS_INDEX] = Long.class;
		
		return clazzes;
	}

}
