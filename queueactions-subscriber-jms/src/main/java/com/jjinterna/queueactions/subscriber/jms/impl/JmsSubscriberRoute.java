package com.jjinterna.queueactions.subscriber.jms.impl;

import java.util.Dictionary;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JaxbDataFormat;

import com.jjinterna.queueactions.model.CallEvent;

public class JmsSubscriberRoute extends RouteBuilder {

	private String routeId;
	private Dictionary<String, String> properties;
	
	public JmsSubscriberRoute(String routeId, Dictionary<String, String> properties) {
		this.routeId = routeId;
		this.properties = properties;
	}
	
	@Override
	public void configure() throws Exception {
		String topicName = properties.get("topicName");

		JaxbDataFormat jaxb = new JaxbDataFormat();
		jaxb.setContextPath(CallEvent.class.getPackage().getName());
		jaxb.setPartClass(CallEvent.class.getName());
		jaxb.setPartNamespace("{http://queueactions.jjinterna.com/model}CallEvent");

		fromF("activemq:topic:%s?username=karaf&password=karaf", topicName).id(routeId)
		.marshal(jaxb)
		.log("${body}");
	}

}
