package com.github.johrstrom.listener.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.github.johrstrom.listener.PrometheusSaveConfig;

public class PrometheusConfigureDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 7132092878660788111L;
	private static final Logger log = LoggingManager.getLoggerForClass();

	private PrometheusSaveConfig config;
	Map<String, Method> accessors;
	Map<String, Method> mutators;
	JTextArea portField;

	PrometheusConfigureDialog(Frame owner, String title, boolean modal, PrometheusSaveConfig config) {
		super(owner, title, modal);
		this.config = config;
		init();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		Method func = mutators.get(action);
		if (action.equals("Done")){
			config.setPort(Integer.parseInt(portField.getText()));
			dispose();
			return;
		}
		if (func != null) {
			try {
				func.invoke(config, new Object[] { Boolean.valueOf(((JCheckBox) e.getSource()).isSelected()) });
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
				log.error(
						"Couldn't save property. No function available " + func.toString() + ". Did not set property.");
			}
		} else {
			log.error("Couldn't save property.  No function available to modify configuration.");
		}
	}

	private void init() {
		this.initConfigFunctions();

		this.getContentPane().setLayout(new BorderLayout());
		final int configCount = (PrometheusSaveConfig.SAVE_CONFIG_NAMES.size() / 2) + 1;

		JPanel checkPanel = new JPanel(new GridLayout(configCount, 2));
		for (String name : PrometheusSaveConfig.SAVE_CONFIG_NAMES) {

			if (name.equals("Port")) {
				int port = this.config.getPort();
				JLabel portLabel = new JLabel(name);
				checkPanel.add(portLabel);
				portField = new JTextArea();
				portField.setColumns(8);
				portField.setRows(1);
				portField.setEditable(true);
				portField.setText(String.valueOf(port));				
				checkPanel.add(portField);

			} else {
				Method m = this.accessors.get(name);
				Boolean b = true;
				try {
					b = (Boolean) m.invoke(this.config);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					log.error("Cannot invoke method " + m.toString() + "");
				}
				JCheckBox check = new JCheckBox(name, b);

				check.addActionListener(this);
				check.setActionCommand(name);
				checkPanel.add(check, BorderLayout.NORTH);
			}
		}

		JButton exit = new JButton("Done");
		this.getContentPane().add(exit, BorderLayout.SOUTH);
		exit.addActionListener(this);

		this.add(checkPanel);

	}

	private void initConfigFunctions() {
		String getPrefix = "save";
		String setPrefix = "setSave";
		this.accessors = new HashMap<String, Method>();
		this.mutators = new HashMap<String, Method>();

		for (String name : PrometheusSaveConfig.SAVE_CONFIG_NAMES) {
			if (name.equals("Port"))
				continue;
			String setFuncName = setPrefix + name;
			String getFuncName = getPrefix + name;

			try {
				Method mm = PrometheusSaveConfig.class.getMethod(setFuncName, boolean.class);
				Method am = PrometheusSaveConfig.class.getMethod(getFuncName);

				this.mutators.put(name, mm);
				this.accessors.put(name, am);

			} catch (NoSuchMethodException | SecurityException e) {
				log.warn("Couldn't load method for PrometheusSaveConfig: " + e.getMessage());
			}

		}

	}

}
