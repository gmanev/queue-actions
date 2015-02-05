package com.jjinterna.queueactions.subscriber.jms.impl;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.jjinterna.queueactions.model.QueueActionsEvent;

public class JAXBElementWrap implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		QueueActionsEvent event = (QueueActionsEvent) exchange.getIn().getBody();
		exchange.getIn().setBody(new JAXBElement(
				new QName("http://queueactions.jjinterna.com/model", event.getClass().getSimpleName()),
				event.getClass(),
				event));
	}

}
