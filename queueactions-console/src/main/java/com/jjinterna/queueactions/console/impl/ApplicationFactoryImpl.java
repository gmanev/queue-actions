package com.jjinterna.queueactions.console.impl;

import com.jjinterna.vaadin.vaadinbridge.ApplicationFactory;
import com.vaadin.ui.UI;

public class ApplicationFactoryImpl implements ApplicationFactory {

	@Override
	public UI getInstance() {
		return new UIImpl();
	}

	@Override
	public Class<? extends UI> getUIClass() {
		return UIImpl.class;
	}

}
