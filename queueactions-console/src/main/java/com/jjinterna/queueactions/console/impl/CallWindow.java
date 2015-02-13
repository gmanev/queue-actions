package com.jjinterna.queueactions.console.impl;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.jjinterna.queueactions.model.CallConnect;
import com.jjinterna.queueactions.model.QueueActionsCallEvent;
import com.jjinterna.queueactions.model.util.CallEventURLEncoder;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class CallWindow extends Window {

	DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	Label caller = new Label();
	Label timers = new Label();	
	Label label = new Label();
	
	FormLayout layout = new FormLayout(caller, timers, label);

	private QueueActionsCallEvent call;
	private String callerURL;

	public CallWindow() {
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		setWidth(300.0f, Unit.PIXELS);
		setHeight(300.0f, Unit.PIXELS);
		setResizable(false);
		setIcon(FontAwesome.PHONE);
		center();
		
		caller.addStyleName(ValoTheme.LABEL_HUGE);
		caller.setContentMode(ContentMode.HTML);

		timers.addStyleName(ValoTheme.LABEL_LARGE);
		
		label.addStyleName(ValoTheme.LABEL_LARGE);		
		label.setContentMode(ContentMode.HTML);

		layout.setMargin(true);
		layout.setSizeFull();
		
		setContent(layout);
	}

	public void setCallerURL(String callerURL) {
		this.callerURL = callerURL;
	}

	private void updateTimers() {
		String waitTime = "00:00:00";
		String talkTime = "00:00:00";

		if (call.getQueueEnterTime() != null) {
			long t = (call.getQueueConnectTime() != null) ? call.getQueueConnectTime() : (System.currentTimeMillis() / 1000);
			Date date = new Date((t - call.getQueueEnterTime()) * 1000);
			waitTime = dateFormat.format(date);		
		}
		
		if (call.getQueueConnectTime() != null) {
			Date date = new Date((System.currentTimeMillis() / 1000 - call.getQueueConnectTime()) * 1000);
			talkTime = dateFormat.format(date);			
		}
		timers.setValue(waitTime + "/" + talkTime);
	}

	public void update(QueueActionsCallEvent newCall) {
		
		if (newCall == null) {
			return;
		}
		
		if (!newCall.equals(call)) {
			call = newCall;
			setCaption(call.getClass().getSimpleName());
			if (callerURL != null) {
				String callerId = call.getCallerId();
				if (callerId == null || callerId.length() == 0) {
					callerId = "   ";
				}
				try {
					caller.setValue("<a href=\"" + callerURL + "?" + CallEventURLEncoder.encode(call) + 
							"\" target=\"_blank\">" + callerId + "</a>");
				} catch (UnsupportedEncodingException e) {
					caller.setValue(call.getCallerId());					
				}
			}
			else {
				caller.setValue(call.getCallerId());
			}
			if (call instanceof CallConnect) {
				caller.addStyleName(ValoTheme.LABEL_COLORED);
				timers.addStyleName(ValoTheme.LABEL_COLORED);
				label.addStyleName(ValoTheme.LABEL_COLORED);
			}
			label.setValue(call.getDid() + "<br>" + 
					call.getQueue() + "<br>" +
					(call.getAgent() != null ? call.getAgent() : ""));
		}

		updateTimers();
	}

}
