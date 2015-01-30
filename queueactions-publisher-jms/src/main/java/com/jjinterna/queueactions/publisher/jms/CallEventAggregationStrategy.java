package com.jjinterna.queueactions.publisher.jms;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import com.jjinterna.queueactions.model.CallEvent;
import com.jjinterna.queueactions.model.CallState;
import com.jjinterna.queueactions.model.QueueLog;

public class CallEventAggregationStrategy implements AggregationStrategy {

	@Override
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
		CallEvent callEvent;
		Exchange ex;
		if (oldExchange == null) {
			callEvent = new CallEvent();
			ex = newExchange;
		} else {
			callEvent = (CallEvent) oldExchange.getIn().getBody();
			ex = oldExchange;
		}

		if (newExchange.getIn().getBody() instanceof QueueLog) {
			QueueLog queueLog = (QueueLog) newExchange.getIn().getBody();
			switch (queueLog.getVerb()) {
			case DID:
				callEvent.setDid(queueLog.getData1());
				callEvent.setCallId(queueLog.getCallId());
				break;
			case ENTERQUEUE:
				callEvent.setQueue(queueLog.getQueue());
				callEvent.setCallerId(queueLog.getData2());
				callEvent.setQueueEnterTime(queueLog.getTimeId());
				callEvent.setState(CallState.ENTERQUEUE);
				break;
			case EXITWITHTIMEOUT:
				callEvent.setQueueLeaveTime(queueLog.getTimeId());
				callEvent.setState(CallState.EXITWITHTIMEOUT);
				break;
			case ABANDON:
				callEvent.setQueueLeaveTime(queueLog.getTimeId());				
				callEvent.setState(CallState.ABANDON);
				break;
			case CONNECT:
				callEvent.setQueueConnectTime(queueLog.getTimeId());				
				callEvent.setAgent(queueLog.getAgent());
				callEvent.setState(CallState.CONNECT);
				break;
			case COMPLETEAGENT:
			case COMPLETECALLER:
				callEvent.setQueueLeaveTime(queueLog.getTimeId());				
				callEvent.setState(CallState.COMPLETE);
				break;
			}
			// callEvent.getLog().add(queueLog);
		} else if (newExchange.getIn().getBody() instanceof CallEvent){
			callEvent = (CallEvent) newExchange.getIn().getBody();
		}
		ex.getIn().setBody(callEvent);
		ex.getIn().setHeader("State", callEvent.getState() != null ? callEvent.getState().toString() : null);
		ex.getIn().setHeader("DID", callEvent.getDid());		
		return ex;
	}

}
