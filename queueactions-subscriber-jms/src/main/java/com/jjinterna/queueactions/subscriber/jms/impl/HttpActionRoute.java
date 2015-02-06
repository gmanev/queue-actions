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
	private String endpointProtocol;
	
	public HttpActionRoute(String routeId, Dictionary properties) {
		this.routeId = routeId;
		this.endpoint = (String) properties.get("EndpointURI");
		this.eventSelector = (Collection<String>) properties.get("EventSelector");
		this.queueSelector = (String) properties.get("QueueSelector");
		this.endpointProtocol = (String) properties.get("EndpointProtocol");
		if (endpointProtocol == null) {
			endpointProtocol = "GET";
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
		.choice()
			.when(constant(endpointProtocol).isEqualTo("HTTP GET"))
				.process(new QueueEvent2HttpQuery())
				.setProperty("Endpoint", constant("http4://uri"))				
				.setHeader(Exchange.HTTP_URI, constant(endpoint))				
				.setHeader(Exchange.HTTP_METHOD, constant("GET"))
				.setBody(constant(""))
				.endChoice()
			.when(constant(endpointProtocol).isEqualTo("HTTP POST"))
				.process(new JAXBElementWrap())
				.marshal(jaxb)
				.setProperty("Endpoint", constant("http4://uri"))
				.setHeader(Exchange.HTTP_URI, constant(endpoint))
				.setHeader(Exchange.HTTP_METHOD, constant("POST"))				
				.setHeader(Exchange.CONTENT_TYPE, constant("application/xml"))
				.endChoice()
			.when(constant(endpointProtocol).isEqualTo("SOAP"))
				.setProperty("Endpoint", constant("cxf://" + endpoint + 
						"?serviceClass=com.jjinterna.queueactions.service.QueueActionsService&loggingFeatureEnabled=false"))
				.setHeader("SOAPAction", constant("updateEvent"))
				.endChoice()
		.end()
		.removeHeaders("JMS*")
		.removeHeaders("QActions*")
		.recipientList(simple("${property.Endpoint}"));
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
