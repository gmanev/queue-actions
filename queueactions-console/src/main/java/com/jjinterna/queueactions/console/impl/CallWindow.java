package com.jjinterna.queueactions.console.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.jjinterna.queueactions.model.CallConnect;
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
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		setWidth(300.0f, Unit.PIXELS);
		setHeight(300.0f, Unit.PIXELS);
		setResizable(false);
		setIcon(FontAwesome.PHONE);
		center();
		
		label.addStyleName(ValoTheme.LABEL_H2);
		label.setContentMode(ContentMode.HTML);
		
		layout.setMargin(true);
		layout.setSizeFull();
		
		setContent(layout);
	}
	
	public void update(QueueActionsCallEvent call) {
		setCaption(call.getClass().getSimpleName());
		
		String waitTime = "00:00:00";
		String talkTime = "00:00:00";

		if (call.getQueueEnterTime() != null) {
			long t = (call.getQueueConnectTime() != null) ? call.getQueueConnectTime() : (System.currentTimeMillis() / 1000);
			Date date = new Date((t - call.getQueueEnterTime()) * 1000);
			waitTime = dateFormat.format(date);			
		}
		
		if (call.getQueueConnectTime() != null) {
			Date date = new Date((System.currentTimeMillis() / 1000 - call.getQueueEnterTime()) * 1000);
			talkTime = dateFormat.format(date);			
		}

		if (call instanceof CallConnect) {
			label.addStyleName(ValoTheme.LABEL_COLORED);
		}

		label.setValue("<strong>" + call.getCallerId() + "</strong><br><br>" +
				waitTime + " / " + talkTime + "<br>" +
				call.getDid() + "<br>" + call.getQueue() + "<br>" +
				(call.getAgent() != null ? call.getAgent() : ""));
	}

}
