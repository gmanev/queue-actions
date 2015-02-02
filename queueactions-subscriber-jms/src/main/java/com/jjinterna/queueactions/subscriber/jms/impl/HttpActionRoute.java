package com.jjinterna.queueactions.subscriber.jms.impl;

import java.util.Dictionary;

import org.apache.camel.Component;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

public class HttpActionRoute extends RouteBuilder {

	private String routeId;
	private Dictionary<String, String> properties;
	
	public HttpActionRoute(String routeId, Dictionary<String, String> properties) {
		this.routeId = routeId;
		this.properties = properties;
	}
	
	@Override
	public void configure() throws Exception {
		//String componentName = properties.get("componentName");
		//String httpMethod = properties.get("httpMethod");

		Component component = getContext().getComponent("http4");
		
//		JaxbDataFormat jaxb = new JaxbDataFormat();
//		jaxb.setContextPath(CallEvent.class.getPackage().getName());
//		jaxb.setPartClass(CallEvent.class.getName());
//		jaxb.setPartNamespace("{http://queueactions.jjinterna.com/model}CallEvent");

		fromF("activemq:topic:%s?username=%s&password=%s", 
				properties.get("topicName"), 
				properties.get("jmsUsername"), 
				properties.get("jmsPassword")).id(routeId)
//		.marshal(jaxb)
		.removeHeaders("JMS*")
		.removeHeaders("QActions*")
		.setHeader(Exchange.HTTP_QUERY, simple(properties.get("httpQuery")))
		.setBody(constant(""))
		.to(properties.get("endpointUri"));
	}

}
