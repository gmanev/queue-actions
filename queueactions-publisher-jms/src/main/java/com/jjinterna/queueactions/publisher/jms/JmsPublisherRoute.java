package com.jjinterna.queueactions.publisher.jms;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.CsvDataFormat;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import com.jjinterna.queueactions.model.CallEvent;
import com.jjinterna.queueactions.model.CallState;
import com.jjinterna.queueactions.model.QueueLog;
import com.jjinterna.queueactions.model.QueueLogType;

public class JmsPublisherRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		
		AggregationStrategy callEventAggregationStrategy = new CallEventAggregationStrategy();

		from("direct:callEventLog").id("callEventLog")
		.toF("activemq:topic:%s?deliveryPersistent=false&username={{jmsUsername}}&password={{jmsPassword}}", CallEvent.class.getName());		

		from("direct:callCompleteRoute").id("callCompleteRoute")
		.aggregate(header("QActionsCallID"), callEventAggregationStrategy)
			.completionTimeout(3600 * 1000)
			.eagerCheckCompletion()
			.completionPredicate(new Predicate() {
				
				@Override
				public boolean matches(Exchange exchange) {
					if (exchange.getIn().getBody() instanceof QueueLog) {
						QueueLog queueLog = (QueueLog) exchange.getIn().getBody();
						return queueLog.getVerb().equals(QueueLogType.COMPLETEAGENT) ||
								queueLog.getVerb().equals(QueueLogType.COMPLETECALLER);
					}
					return false;
				}
			})
		.to("direct:callEventLog");
		
		from("direct:callConnectRoute").id("callConnectRoute")
		.aggregate(header("QActionsCallID"), callEventAggregationStrategy)
			.completionTimeout(3600 * 1000)		
			.eagerCheckCompletion()
			.completionPredicate(new Predicate() {				
				@Override
				public boolean matches(Exchange exchange) {
					if (exchange.getIn().getBody() instanceof QueueLog) {
						QueueLog queueLog = (QueueLog) exchange.getIn().getBody();
						return queueLog.getVerb().equals(QueueLogType.ABANDON) ||
								queueLog.getVerb().equals(QueueLogType.EXITWITHTIMEOUT) ||
								queueLog.getVerb().equals(QueueLogType.CONNECT);
					}
					return false;
				}
			})
		.multicast()
			.to("direct:callEventLog")
			.filter(header("QActionsState").isEqualTo(CallState.CONNECT)).to("direct:callCompleteRoute").end()
		.end();

		from("direct:callEnterQueueRoute").id("callEnterQueueRoute")
		.aggregate(header("QActionsCallID"), callEventAggregationStrategy)
			.completionTimeout(60 * 1000)
			.completionPredicate(header("QActionsState").isEqualTo(CallState.ENTERQUEUE))
		.multicast()
			.to("direct:callEventLog", "direct:callConnectRoute");

		from("direct:queueLogRoute").id("queueLogRoute")
		.choice()
			.when(header("QActionsState").in(
					QueueLogType.DID,
					QueueLogType.ENTERQUEUE))
				.to("direct:callEnterQueueRoute").stop()
			.when(header("QActionsState").in(
					QueueLogType.ABANDON,
					QueueLogType.CONNECT,
					QueueLogType.EXITWITHTIMEOUT,
					QueueLogType.RINGNOANSWER))
				.to("direct:callConnectRoute").stop()
			.when(header("QActionsState").in(
					QueueLogType.COMPLETEAGENT,
					QueueLogType.COMPLETECALLER))
				.to("direct:callCompleteRoute").stop()
		.end();

		from("stream:file?fileName={{fileName}}&scanStream=true&scanStreamDelay=1000").id("queue_log")
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
		        
		        QueueLog queueLog = new QueueLog(
		        		Integer.parseInt(fields.get(0)),
		        		fields.get(1),
		        		fields.get(2),
		        		fields.get(3),
		        		QueueLogType.fromValue(fields.get(4)),
		        		fields.get(5),
		        		fields.get(6),
		        		fields.get(7),
		        		fields.get(8),
		        		fields.get(9));
		        
		        in.setHeader("QActionsCallID", queueLog.getCallId());
		        in.setHeader("QActionsState", queueLog.getVerb().toString());
		        in.setHeader("QActionsQueue", queueLog.getQueue());
		        
		        in.setBody(queueLog);
			}
		})
		.to("direct:queueLogRoute");
		
	}

}
