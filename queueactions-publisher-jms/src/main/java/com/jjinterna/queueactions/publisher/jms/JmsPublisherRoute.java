package com.jjinterna.queueactions.publisher.jms;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.CsvDataFormat;
import org.apache.camel.model.dataformat.JaxbDataFormat;

import com.jjinterna.queueactions.model.QueueEvent;
import com.jjinterna.queueactions.model.QueueEventType;

public class JmsPublisherRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {

		JaxbDataFormat jaxb = new JaxbDataFormat();
		jaxb.setContextPath(QueueEvent.class.getPackage().getName());
		jaxb.setPartClass(QueueEvent.class.getName());
		jaxb.setPartNamespace("{http://queueactions.jjinterna.com/model}QueueEvent");

		from("stream:file?fileName={{fileName}}&scanStream=true&scanStreamDelay=1000").id("queue_log2jms")
		.unmarshal(new CsvDataFormat("|"))
		.process(new Processor() {			
			@Override
			public void process(Exchange exchange) throws Exception {
		        Message in = exchange.getIn();
		        List<List<String>> csvData = (List<List<String>>) in.getBody();
		        List<String> fields = csvData.get(0);
		        for (int i = fields.size(); i<10; i++) {
		        	fields.add("");
		        }
		        
		        QueueEvent queueEvent = new QueueEvent(
		        		Integer.parseInt(fields.get(0)),
		        		fields.get(1),
		        		fields.get(2),
		        		fields.get(3),
		        		QueueEventType.fromValue(fields.get(4)),
		        		fields.get(5),
		        		fields.get(6),
		        		fields.get(7),
		        		fields.get(8),
		        		fields.get(9));
		        
		        in.setBody(queueEvent);
			}
		})
		.marshal(jaxb)
		.to("activemq:queue:QueueActions?username={{jmsUsername}}&password={{jmsPassword}}");
	}

}
