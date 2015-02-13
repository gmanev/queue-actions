package com.jjinterna.queueactions.console.impl;

import com.jjinterna.vaadin.vaadinbridge.ApplicationFactory;
import com.vaadin.ui.UI;

public class ApplicationFactoryImpl implements ApplicationFactory {

	String imageURL, callerURL;
	
	public ApplicationFactoryImpl(String imageURL, String callerURL) {
		if (!"".equals(imageURL)) {
			this.imageURL = imageURL;
		}
		if (!"".equals(callerURL)) {
			this.callerURL = callerURL;
		}
	}
	@Override
	public UI getInstance() {
		return new UIImpl(imageURL, callerURL);
	}

	@Override
	public Class<? extends UI> getUIClass() {
		return UIImpl.class;
	}

}
