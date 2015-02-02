package com.jjinterna.queueactions.publisher.jms;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import com.jjinterna.queueactions.model.CallAbandon;
import com.jjinterna.queueactions.model.CallComplete;
import com.jjinterna.queueactions.model.CallConnect;
import com.jjinterna.queueactions.model.CallEnterQueue;
import com.jjinterna.queueactions.model.CallExitWithTimeout;
import com.jjinterna.queueactions.model.QueueActionsCallEvent;
import com.jjinterna.queueactions.model.QueueLog;

public class CallEventAggregationStrategy implements AggregationStrategy {

	@Override
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
		QueueActionsCallEvent callEvent;
		Exchange ex;
		if (oldExchange == null) {
			callEvent = new QueueActionsCallEvent();
			ex = newExchange;
		} else {
			callEvent = (QueueActionsCallEvent) oldExchange.getIn().getBody();
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
				callEvent = copy(callEvent, new CallEnterQueue());
				break;
			case EXITWITHTIMEOUT:
				callEvent.setQueueLeaveTime(queueLog.getTimeId());
				callEvent = copy(callEvent, new CallExitWithTimeout());
				break;
			case ABANDON:
				callEvent.setQueueLeaveTime(queueLog.getTimeId());				
				callEvent = copy(callEvent, new CallAbandon());
				break;
			case CONNECT:
				callEvent.setQueueConnectTime(queueLog.getTimeId());				
				callEvent.setAgent(queueLog.getAgent());
				callEvent = copy(callEvent, new CallConnect());
				break;
			case COMPLETEAGENT:
			case COMPLETECALLER:
				callEvent.setQueueLeaveTime(queueLog.getTimeId());				
				callEvent = copy(callEvent, new CallComplete());
				break;
			}
			// callEvent.getLog().add(queueLog);
		} else if (newExchange.getIn().getBody() instanceof QueueActionsCallEvent){
			callEvent = (QueueActionsCallEvent) newExchange.getIn().getBody();
		}
		ex.getIn().setBody(callEvent);
		ex.getIn().setHeader("QActionsEvent", callEvent.getClass().getSimpleName());
		ex.getIn().setHeader("QActionsDID", callEvent.getDid());		
		return ex;
	}

	private static QueueActionsCallEvent copy(QueueActionsCallEvent from, QueueActionsCallEvent to) {
		to.setAgent(from.getAgent());
		to.setCallerId(from.getCallerId());
		to.setCallId(from.getCallId());
		to.setDid(from.getDid());
		to.setQueue(from.getQueue());
		to.setQueueConnectTime(from.getQueueConnectTime());
		to.setQueueEnterTime(from.getQueueEnterTime());
		to.setQueueLeaveTime(from.getQueueLeaveTime());
		return to;
	}
}
