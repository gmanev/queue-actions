package com.jjinterna.queueactions.model.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.jjinterna.queueactions.model.QueueActionsCallEvent;

public class CallEventURLEncoder {

	private static final String enc = "UTF-8";
	
	public static String encode(QueueActionsCallEvent ev) throws UnsupportedEncodingException {
		String q = "Event=" + ev.getClass().getSimpleName();
		if (ev.getAgent() != null) {
			q += "&Agent=" + URLEncoder.encode(ev.getAgent(), enc);
		}
		if (ev.getCallerId() != null) {
			q += "&CallerId=" + URLEncoder.encode(ev.getCallerId(), enc);
		}
		if (ev.getCallId() != null) {
			q += "&CallId=" + URLEncoder.encode(ev.getCallId(), enc);
		}
		if (ev.getDid() != null) {
			q += "&DID=" + URLEncoder.encode(ev.getDid(), enc);
		}
		if (ev.getQueue() != null) {
			q += "&Queue=" + URLEncoder.encode(ev.getQueue(), enc);
		}
		if (ev.getQueueConnectTime() != null) {
			q += "&QueueConnectTime=" + ev.getQueueConnectTime();
		}
		if (ev.getQueueEnterTime() != null) {
			q += "&QueueEnterTime=" + ev.getQueueEnterTime();
		}
		if (ev.getQueueLeaveTime() != null) {
			q += "&QueueLeaveTime=" + ev.getQueueLeaveTime();
		}
		return q;
	}
}
