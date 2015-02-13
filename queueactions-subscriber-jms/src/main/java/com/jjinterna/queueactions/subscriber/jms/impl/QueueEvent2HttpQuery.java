package com.jjinterna.queueactions.subscriber.jms.impl;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.jjinterna.queueactions.model.QueueActionsCallEvent;
import com.jjinterna.queueactions.model.util.CallEventURLEncoder;

public class QueueEvent2HttpQuery implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		if (exchange.getIn().getBody() instanceof QueueActionsCallEvent) {
			QueueActionsCallEvent ev = (QueueActionsCallEvent) exchange.getIn().getBody();
			String q = CallEventURLEncoder.encode(ev);
			exchange.getIn().setHeader(Exchange.HTTP_QUERY, q);
		}
	}

}
