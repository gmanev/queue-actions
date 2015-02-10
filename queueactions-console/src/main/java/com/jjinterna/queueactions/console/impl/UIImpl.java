package com.jjinterna.queueactions.console.impl;

import com.jjinterna.queueactions.model.CallConnect;
import com.jjinterna.queueactions.model.CallEnterQueue;
import com.jjinterna.queueactions.model.QueueActionsCallEvent;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.server.ClientConnector.DetachListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@Theme("valo")
@Push
public class UIImpl extends UI implements QueueActionsServiceImpl.CallEventListener, DetachListener {

    private static final Object[] VISIBLE_COLUMNS = new Object[] {"callId", "callerId", "did", "queue", "agent"};

	private BeanContainer<String, QueueActionsCallEvent> calls = 
			new BeanContainer<String, QueueActionsCallEvent>(QueueActionsCallEvent.class);

	@Override
	protected void init(VaadinRequest request) {
		QueueActionsServiceImpl.register(this);

		calls.setBeanIdProperty("callId");
		calls.addAll(QueueActionsServiceImpl.calls.values());

		Table table = new Table();
		table.setImmediate(true);
		table.setContainerDataSource(calls);
		table.setSizeFull();
		table.setVisibleColumns(VISIBLE_COLUMNS);
		
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();		
		layout.addComponent(table);
		
		setContent(layout);
	}
	@Override
	public void receive(final QueueActionsCallEvent event) {
		access(new Runnable() {

			@Override
			public void run() {
				if (event instanceof CallConnect || event instanceof CallEnterQueue) {
					calls.addBean(event);
				} else {
					calls.removeItem(event.getCallId());
				}
			}
			
		});
		
	}
	@Override
	public void detach(DetachEvent event) {
		System.out.println("Detached");
		QueueActionsServiceImpl.unregister(this);		
	}

}
