package com.jjinterna.queueactions.web;

import org.osgi.service.cm.ConfigurationAdmin;

import com.jjinterna.vaadin.vaadinbridge.ApplicationFactory;
import com.vaadin.ui.UI;

public class QActionsAppFactory implements ApplicationFactory {

	private ConfigurationAdmin configAdmin;
	private String title;
	
    public QActionsAppFactory(ConfigurationAdmin configAdmin, String title) {
        this.title = title;
        this.configAdmin = configAdmin;
    }	
	
	@Override
	public UI getInstance() {
		return new QActionsApplication(configAdmin, title);
	}

	@Override
	public Class<? extends UI> getUIClass() {
		return QActionsApplication.class;
	}

}
