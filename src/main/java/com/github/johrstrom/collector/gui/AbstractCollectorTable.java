package com.github.johrstrom.collector.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jorphan.gui.ObjectTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.johrstrom.collector.BaseCollectorConfig;
import com.github.johrstrom.collector.CollectorElement;

public abstract class AbstractCollectorTable<C extends BaseCollectorConfig> 
	extends JPanel implements ActionListener {

	public static final String ADD = "Add";
	public static final String DELETE = "Delete";
	
	protected transient JTable table;
	protected transient ObjectTableModel model;
	protected JButton add,delete;
	
	
	private final Class<C> clazzType;
	private static final long serialVersionUID = 2027712606129940455L;
	private Logger log = LoggerFactory.getLogger(AbstractCollectorTable.class);
	
	
	
	/**
	 * @return
	 */
	public abstract Flatten getGuiHelper();
	
	
	/**
	 * 
	 */
	public abstract void modifyColumns();
	
	
	public AbstractCollectorTable(Class<C> collectorType) {
		clazzType = collectorType;
		this.init();
		this.modifyColumns();
	}
	
	public List<C> getRowsAsCollectors(){
		ArrayList<C> collectors = new ArrayList<>();
		
		@SuppressWarnings("unchecked")
		Iterator<C> iter = (Iterator<C>) this.model.iterator();
		
		while(iter.hasNext()) {
			C cfg = this.clazzType.cast(iter.next());
			collectors.add(cfg);
			log.debug("populated config: " + cfg.toString() + " from table.");
		}
		
		return collectors;
	}
	
	public void clearModelData() {
		this.model.clearData();
	}
	
//	public void modifyTestElement(CollectorElement<C> ele) {
//		
//		if(!(ele instanceof CollectorElement)) {
//			return;
//		}
//		
//		int rows = this.model.getRowCount();
//		ArrayList<C> collectors = new ArrayList<>();
//		
//		@SuppressWarnings("unchecked")
//		CollectorElement<C> config = (CollectorElement<C>) ele;
//
//		log.debug("modifying test element " + ele.toString() + ". row count in model is " + rows);
//		
//		@SuppressWarnings("unchecked")
//		Iterator<C> iter = (Iterator<C>) model.iterator();
//		
//		while(iter.hasNext()) {
//			C cfg = this.clazzType.cast(iter.next());
//			collectors.add(cfg);
//			log.debug("populated config: " + cfg.toString() + " from table.");
//		}
//		
//		config.setCollectorConfigs(collectors);
//		this.setCollector(config);
//	}
	
	
	public void populateTable(CollectorElement<C> config) {
		
		CollectionProperty collectors = config.getCollectorConfigs();
		log.debug("Configuring table with " + collectors.size() + " collectors.");
		
		this.model.clearData();
		
		PropertyIterator it = collectors.iterator();
		while(it.hasNext()) {
			BaseCollectorConfig cfg = (BaseCollectorConfig) it.next().getObjectValue();			 
			this.model.addRow(cfg);
			log.debug("added row into table: " + cfg.toString());
		}
		
	}
	
	
	/**
	 * Private helper function to initialize all the Swing components.
	 */
	protected void init() {
		this.setLayout(new BorderLayout(0, 5));
		
		VerticalPanel panel = new VerticalPanel();
		panel.add(makeTablePanel());
		panel.add(makeButtonPanel());
		
		this.add(panel, BorderLayout.CENTER);
	}
	
	
	protected Component makeTablePanel() {
		Flatten helper = this.getGuiHelper();
		
		this.model = new ObjectTableModel(
				helper.getHeaders(),
				this.clazzType,
				helper.getReadFunctors(),
				helper.getWriteFunctors(),
				helper.getEditorClasses()
		);
		
		this.table = new JTable(this.model);
		
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
			try {
				this.model.addRow(this.clazzType.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				log.error("Couldn't add to model becuase of error. ", e);
			}
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
