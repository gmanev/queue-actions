package com.jjinterna.queueactions.subscriber.jms.impl;

import java.util.Collection;
import java.util.Dictionary;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JaxbDataFormat;

import com.jjinterna.queueactions.model.QueueActionsEvent;

public class HttpActionRoute extends RouteBuilder {

	private String routeId;
	private String endpoint;
	private Collection<String> eventSelector;
	private String queueSelector;
	private String httpMethod;
	
	public HttpActionRoute(String routeId, Dictionary properties) {
		this.routeId = routeId;
		this.endpoint = (String) properties.get("Endpoint");
		this.eventSelector = (Collection<String>) properties.get("EventSelector");
		this.queueSelector = (String) properties.get("QueueSelector");
		this.httpMethod = (String) properties.get("HTTPMethod");
		if (httpMethod == null) {
			httpMethod = "GET";
		}
	}
	
	public String getRouteId() {
		return HttpActionRoute.class.getSimpleName() + "-" + routeId;
	}
	
	@Override
	public void configure() throws Exception {
		
		JaxbDataFormat jaxb = new JaxbDataFormat();
		jaxb.setContextPath(QueueActionsEvent.class.getPackage().getName());

		fromF("activemq:topic:QueueActions?username={{jmsUsername}}&password={{jmsPassword}}%s", getSelectorString()).id(getRouteId())
		.setHeader(Exchange.HTTP_METHOD, constant(httpMethod))
		.choice()
			.when(header(Exchange.HTTP_METHOD).isEqualTo("GET"))
				.process(new QueueEvent2HttpQuery())
				.setBody(constant(""))
				.endChoice()
			.when(header(Exchange.HTTP_METHOD).isEqualTo("POST"))
				.marshal(jaxb)
				.setHeader(Exchange.CONTENT_TYPE, constant("application/xml"))
				.endChoice()
		.end()
		.removeHeaders("JMS*")
		.removeHeaders("QActions*")
		.setHeader(Exchange.HTTP_URI, constant(endpoint))
		.to("http4://uri");
	}

	private String getSelectorString() {
		String selector = null;
		if (eventSelector != null) {
			for (String ev : eventSelector) {
				if (selector == null) {
					selector = "";
				} else {
					selector += ",";
				}
				selector += "'" + ev + "'";
			}
			selector = "QActionsEvent IN(" + selector + ")";
		}
		if (queueSelector != null && queueSelector.length() > 0) {
			if (selector == null) {
				selector = "";
			} else {
				selector += " AND ";
			}
			selector += "QActionsQueue='" + queueSelector + "'";
		}
		if (selector == null)
			return "";
		return "&selector=" + selector;
	}

}
