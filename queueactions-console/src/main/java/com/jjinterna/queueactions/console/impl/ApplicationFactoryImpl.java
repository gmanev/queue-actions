package com.jjinterna.queueactions.console.impl;

import com.jjinterna.vaadin.vaadinbridge.ApplicationFactory;
import com.vaadin.ui.UI;

public class ApplicationFactoryImpl implements ApplicationFactory {

	String imageURL;
	
	public ApplicationFactoryImpl(String imageURL) {
		this.imageURL = imageURL;
	}
	@Override
	public UI getInstance() {
		return new UIImpl(imageURL);
	}

	@Override
	public Class<? extends UI> getUIClass() {
		return UIImpl.class;
	}

}
