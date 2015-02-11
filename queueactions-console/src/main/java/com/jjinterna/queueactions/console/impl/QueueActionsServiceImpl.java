package com.jjinterna.queueactions.console.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.jjinterna.queueactions.model.CallConnect;
import com.jjinterna.queueactions.model.CallEnterQueue;
import com.jjinterna.queueactions.model.QueueActionsCallEvent;
import com.jjinterna.queueactions.model.QueueActionsEvent;
import com.jjinterna.queueactions.service.QueueActionsService;

public class QueueActionsServiceImpl implements QueueActionsService {

	public static final ConcurrentHashMap<String, QueueActionsCallEvent> activeCalls =
			new ConcurrentHashMap<>();

	@Override
	public Boolean updateEvent(QueueActionsEvent event) {
		if (event instanceof QueueActionsCallEvent) {
			final QueueActionsCallEvent callEvent = (QueueActionsCallEvent) event;
			if (callEvent instanceof CallConnect || 
					callEvent instanceof CallEnterQueue) {
				activeCalls.put(callEvent.getCallId(), callEvent);
			} else {
				activeCalls.remove(callEvent.getCallId());
			}
			return true;
		}
		return false;
	}

	public void init() {
		activeCalls.clear();
	}
	
	public void destroy() {
		
	}
}
