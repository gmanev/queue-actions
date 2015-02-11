package com.jjinterna.queueactions.console.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jjinterna.queueactions.model.QueueActionsCallEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class CallWindow extends Window {

	DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	Label label = new Label();
	FormLayout layout = new FormLayout(label);


	public CallWindow() {
		setWidth(300.0f, Unit.PIXELS);
		setHeight(300.0f, Unit.PIXELS);
		setResizable(false);
		setIcon(FontAwesome.PHONE);
		center();
		
		label.setStyleName(ValoTheme.LABEL_H2);
		label.setContentMode(ContentMode.HTML);
		
		layout.setMargin(true);
		layout.setSizeFull();
		
		setContent(layout);
	}
	
	public void update(QueueActionsCallEvent call) {

		setCaption(call.getClass().getSimpleName());
				
		String dateString = "";
		if (call.getQueueEnterTime() != null) {
			Date date = new Date(System.currentTimeMillis() - call.getQueueEnterTime() * 1000);
			dateString = dateFormat.format(date);
		}
		
		label.setValue("<strong>" + call.getCallerId() + "</strong><br><br>" + 
				call.getDid() + "<br>" + 
				call.getQueue() + "<br>" + 
				dateString);
	}

}
