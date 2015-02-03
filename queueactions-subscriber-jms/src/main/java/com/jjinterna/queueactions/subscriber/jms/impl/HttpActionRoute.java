package com.jjinterna.queueactions.subscriber.jms.impl;

import java.util.Dictionary;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JaxbDataFormat;

import com.jjinterna.queueactions.model.QueueActionsCallEvent;
import com.jjinterna.queueactions.model.QueueActionsEvent;

public class HttpActionRoute extends RouteBuilder {

	private String routeId;
	private String endpoint;
	
	public HttpActionRoute(String routeId, Dictionary properties) {
		this.routeId = routeId;
		this.endpoint = (String) properties.get("Endpoint");
	}
	
	public String getRouteId() {
		return HttpActionRoute.class.getSimpleName() + "-" + routeId;
	}

	@Override
	public void configure() throws Exception {
		
		JaxbDataFormat jaxb = new JaxbDataFormat();
		jaxb.setContextPath(QueueActionsEvent.class.getPackage().getName());

		fromF("activemq:topic:QueueActions?username=karaf&password=karaf").id(getRouteId())
		.process(new Processor() {
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
					exchange.getIn().setHeader(Exchange.HTTP_QUERY, constant(q));
				}
			}			
		})
		.marshal(jaxb)
		.removeHeaders("JMS*")
		.removeHeaders("QActions*")
		.setHeader(Exchange.HTTP_METHOD, constant("GET"))		
		.setHeader(Exchange.HTTP_URI, constant(endpoint))
		.to("http4://uri");
	}

}
