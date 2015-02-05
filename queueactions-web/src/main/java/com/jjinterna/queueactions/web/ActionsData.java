package com.jjinterna.queueactions.web;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public class ActionsData extends IndexedContainer {

	private static final String CONFIG_PID = "com.jjinterna.queueactions.subscriber";
	private static final String filter =  "(service.factoryPid=" + CONFIG_PID + ")";

	private final ConfigurationAdmin configAdmin;

	public static final String NAME = "Name";
	public static final String ENDPOINT_URI = "EndpointURI";
	public static final String ENDPOINT_PROTOCOL = "EndpointProtocol";	
	public static final String ENABLE = "Enable";
	public static final String EVENT_SELECTOR = "EventSelector";
	public static final String QUEUE_SELECTOR = "QueueSelector";
	public static final String CONFIGURATION = "Configuration";	
	
	public ActionsData(ConfigurationAdmin configAdmin) {
		this.configAdmin = configAdmin;

		addContainerProperty(NAME, String.class, "New");
        addContainerProperty(ENDPOINT_URI, String.class, "");
        addContainerProperty(ENDPOINT_PROTOCOL, String.class, "HTTP GET");
        addContainerProperty(ENABLE, Boolean.class, false);        
        addContainerProperty(CONFIGURATION, Configuration.class, null);        
        addContainerProperty(EVENT_SELECTOR, Collection.class, null);        
        addContainerProperty(QUEUE_SELECTOR, String.class, null);
	}
	
	public void loadData() {
		try {
			Configuration[] configurations = configAdmin.listConfigurations(filter);
			if (configurations != null) {
				for (Configuration conf : configurations) {
					String itemId = conf.getPid();
					Item item = addItem(itemId);
					item.getItemProperty(CONFIGURATION).setValue(conf);
					item.getItemProperty(NAME).setValue(conf.getProperties().get(NAME));
					item.getItemProperty(ENABLE).setValue(conf.getProperties().get(ENABLE));
					item.getItemProperty(ENDPOINT_URI).setValue(conf.getProperties().get(ENDPOINT_URI));
					item.getItemProperty(ENDPOINT_PROTOCOL).setValue(conf.getProperties().get(ENDPOINT_PROTOCOL));					
					Collection<String> c = (Collection<String>) conf.getProperties().get(EVENT_SELECTOR);
					if (c != null) {
						item.getItemProperty(EVENT_SELECTOR).setValue(new HashSet(c));
					}
					item.getItemProperty(QUEUE_SELECTOR).setValue(conf.getProperties().get(QUEUE_SELECTOR));					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void commit(Object itemId) throws Exception {
		Configuration conf = (Configuration) getContainerProperty(itemId, CONFIGURATION).getValue();
		if (conf == null) {
			conf = configAdmin.createFactoryConfiguration(CONFIG_PID, null);
			getContainerProperty(itemId, CONFIGURATION).setValue(conf);
		}
		Dictionary<String, Object> props = new Hashtable<>();
		props.put(NAME, getContainerProperty(itemId, NAME).getValue());
		props.put(ENABLE, getContainerProperty(itemId, ENABLE).getValue());
		props.put(ENDPOINT_URI, getContainerProperty(itemId, ENDPOINT_URI).getValue());
		props.put(ENDPOINT_PROTOCOL, getContainerProperty(itemId, ENDPOINT_PROTOCOL).getValue());					
		Collection<String> c = (Collection<String>) getContainerProperty(itemId, EVENT_SELECTOR).getValue();
		if (c != null && !c.isEmpty()) {
			props.put(EVENT_SELECTOR, c);
		}
		Object value = getContainerProperty(itemId, QUEUE_SELECTOR).getValue();
		if (value != null)
			props.put(QUEUE_SELECTOR, value);					
		conf.update(props);
	}
}
