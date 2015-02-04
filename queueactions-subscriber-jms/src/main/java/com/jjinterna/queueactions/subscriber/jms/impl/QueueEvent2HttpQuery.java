package com.jjinterna.queueactions.subscriber.jms.impl;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.jjinterna.queueactions.model.QueueActionsCallEvent;

public class QueueEvent2HttpQuery implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		if (exchange.getIn().getBody() instanceof QueueActionsCallEvent) {
			QueueActionsCallEvent ev = (QueueActionsCallEvent) exchange.getIn().getBody();
			String q = "Event=" + ev.getClass().getSimpleName();
			if (ev.getAgent() != null) {
				q += "&Agent=" + ev.getAgent();
			}
			if (ev.getCallerId() != null) {
				q += "&CallerId=" + ev.getCallerId();
			}
			if (ev.getCallId() != null) {
				q += "&CallId=" + ev.getCallId();
			}
			if (ev.getDid() != null) {
				q += "&DID=" + ev.getDid();
			}
			if (ev.getQueue() != null) {
				q += "&Queue=" + ev.getQueue();
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
			exchange.getIn().setHeader(Exchange.HTTP_QUERY, q);
		}
	}

}
