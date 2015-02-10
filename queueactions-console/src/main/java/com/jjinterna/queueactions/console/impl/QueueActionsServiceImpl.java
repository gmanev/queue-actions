package com.jjinterna.queueactions.console.impl;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jjinterna.queueactions.model.CallConnect;
import com.jjinterna.queueactions.model.CallEnterQueue;
import com.jjinterna.queueactions.model.QueueActionsCallEvent;
import com.jjinterna.queueactions.model.QueueActionsEvent;
import com.jjinterna.queueactions.service.QueueActionsService;

public class QueueActionsServiceImpl implements QueueActionsService {

	public interface CallEventListener {
		void receive(QueueActionsCallEvent event);
	}

	static ExecutorService executorService = Executors
			.newSingleThreadExecutor();

	public static final ConcurrentHashMap<String, QueueActionsCallEvent> calls =
			new ConcurrentHashMap<>();

	private static LinkedList<CallEventListener> listeners = new LinkedList<CallEventListener>();

	public static synchronized void register(CallEventListener listener) {
		listeners.add(listener);
	}

	public static synchronized void unregister(CallEventListener listener) {
		listeners.remove(listener);
	}

	@Override
	public Boolean updateEvent(QueueActionsEvent event) {
		if (event instanceof QueueActionsCallEvent) {
			final QueueActionsCallEvent callEvent = (QueueActionsCallEvent) event;
			if (callEvent instanceof CallConnect || 
					callEvent instanceof CallEnterQueue) {
				calls.put(callEvent.getCallId(), callEvent);
			} else {
				calls.remove(callEvent.getCallId());
			}
			for (final CallEventListener listener : listeners)
				executorService.execute(new Runnable() {
					@Override
					public void run() {
						listener.receive(callEvent);
					}
				});
			return true;
		}
		return false;
	}

}
