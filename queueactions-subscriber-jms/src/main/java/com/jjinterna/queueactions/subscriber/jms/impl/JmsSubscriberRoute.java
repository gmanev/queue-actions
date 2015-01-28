package com.jjinterna.queueactions.subscriber.jms.impl;

import org.apache.camel.builder.RouteBuilder;

public class JmsSubscriberRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:start").id("myroute").log("test");
	}

}
