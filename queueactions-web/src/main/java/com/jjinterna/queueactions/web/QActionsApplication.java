package com.jjinterna.queueactions.web;

import java.io.IOException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("valo")
public class QActionsApplication extends UI {

	private static final String CONFIG_PID = "com.jjinterna.queueactions.subscriber";
	private static final String filter =  "(service.factoryPid=" + CONFIG_PID + ")";
	private static final String NAME = "Name";
	private static final String ENDPOINT = "Endpoint";
	private static final String ENABLE = "Enable";
	private static final String EVENT_SELECTOR = "EventSelector";
	private static final String QUEUE_SELECTOR = "QueueSelector";
	
	private static final String CONFIGURATION = "Configuration";	
	private static final String[] eventNames = new String[] {
		"CallEnterQueue",
		"CallConnect",
		"CallComplete",
		"CallAbandon",
		"CallExitWithTimeout"
	};
	
	private ConfigurationAdmin configAdmin;
	private String title;

	private Table actionList = new Table();
	private Button addNewActionButton = new Button("New");
	private Button saveChangesButton = new Button("Save");	
	private Button removeActionButton = new Button("Delete this action");	
	private FormLayout editorLayout = new FormLayout();
    private FieldGroup editorFields = new FieldGroup();
    private IndexedContainer actionContainer;

	public QActionsApplication(ConfigurationAdmin configAdmin, String title) {
		this.configAdmin = configAdmin;
		this.title = title;
        actionContainer = createDatasource();		
	}
	
	@Override
	protected void init(VaadinRequest request) {
		getPage().setTitle(title);
        initLayout();
        initActionTable();
        initEditor();
        initButtons();
	}

	private void initLayout() {

		/* Root of the user interface component tree is set */
		HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		setContent(splitPanel);

		/* Build the component tree */
		VerticalLayout leftLayout = new VerticalLayout();
		splitPanel.addComponent(leftLayout);
		splitPanel.addComponent(editorLayout);

		HorizontalLayout hLayout = new HorizontalLayout();
		hLayout.addComponent(addNewActionButton);
//		hLayout.setComponentAlignment(addNewActionButton, Alignment.TOP_RIGHT);
		
		leftLayout.addComponent(actionList);
		leftLayout.addComponent(hLayout);		

		/* Set the contents in the left of the split panel to use all the space */
		leftLayout.setSizeFull();
		leftLayout.setExpandRatio(actionList, 1);
		actionList.setSizeFull();

		hLayout.setWidth("100%");

		/* Put a little margin around the fields in the right side editor */
		editorLayout.setMargin(true);
		editorLayout.setVisible(false);
	}

	private void initEditor() {

		editorLayout.addComponent(removeActionButton);

		String fieldName;
		
		fieldName = NAME;
		TextField field = new TextField(fieldName);
		editorLayout.addComponent(field);
		field.setWidth("100%");
		field.setRequired(true);
		editorFields.bind(field, fieldName);

		fieldName = ENDPOINT;
		TextField ep = new TextField(fieldName);
		editorLayout.addComponent(ep);
		ep.setWidth("100%");
		ep.setRequired(true);		
		editorFields.bind(ep, fieldName);

		fieldName = ENABLE;
		CheckBox cb = new CheckBox("Action enable");
		editorLayout.addComponent(cb);
		cb.setWidth("100%");
		editorFields.bind(cb, fieldName);

		fieldName = EVENT_SELECTOR;
		OptionGroup eventSelector = new OptionGroup(EVENT_SELECTOR);
		eventSelector.setMultiSelect(true);
		eventSelector.addItems(eventNames);
		editorLayout.addComponent(eventSelector);
		editorFields.bind(eventSelector, fieldName);

		fieldName = QUEUE_SELECTOR;
		TextField qs = new TextField(fieldName);
		editorLayout.addComponent(qs);
		editorFields.bind(qs, fieldName);

		editorLayout.addComponent(saveChangesButton);
	}

	private void initActionTable() {
		actionList.setContainerDataSource(actionContainer);
		actionList.setVisibleColumns(NAME, ENDPOINT, ENABLE);
		actionList.setSelectable(true);
		actionList.setImmediate(true);

		actionList.addValueChangeListener(new Property.ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				Object contactId = actionList.getValue();

				if (contactId != null)
					editorFields.setItemDataSource(actionList
							.getItem(contactId));
				
				editorLayout.setVisible(contactId != null);
			}
		});
	}

	private void initButtons() {
		addNewActionButton.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				actionContainer.removeAllContainerFilters();
				Object actionId = actionContainer.addItemAt(0);
				actionList.select(actionId);
			}
		});

		removeActionButton.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				Object itemId = actionList.getValue();
				Configuration conf = (Configuration) actionList.getItem(itemId).getItemProperty(CONFIGURATION).getValue();
				try {
					if (conf != null) {
						conf.delete();
					}
					actionList.removeItem(itemId);
		            Notification.show("Deleted");					
				} catch (IOException e) {
		            Notification.show(e.getMessage());
				}
			}
		});
		
		saveChangesButton.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					Object itemId = actionList.getValue();
					Configuration conf = (Configuration) actionList.getContainerProperty(itemId, CONFIGURATION).getValue();
					if (conf == null) {
						conf = configAdmin.createFactoryConfiguration(CONFIG_PID, null);
						actionList.getContainerProperty(itemId, CONFIGURATION).setValue(conf);
					}
					Dictionary<String, Object> props = new Hashtable<>();
					props.put(NAME, editorFields.getField(NAME).getValue());
					props.put(ENABLE, editorFields.getField(ENABLE).getValue());
					props.put(ENDPOINT, editorFields.getField(ENDPOINT).getValue());
					Collection<String> c = (Collection<String>) editorFields.getField(EVENT_SELECTOR).getValue();
					if (!c.isEmpty()) {
						props.put(EVENT_SELECTOR, c);
					}
					conf.update(props);
					editorFields.commit();
		            Notification.show("Saved");					
				} catch (Exception e) {
					e.printStackTrace();
		            Notification.show(e.getMessage());					
				}	
			}
			
		});
	
	}

	private IndexedContainer createDatasource() {
		IndexedContainer ic = new IndexedContainer();

        ic.addContainerProperty(NAME, String.class, "New");
        ic.addContainerProperty(ENDPOINT, String.class, "");
        ic.addContainerProperty(ENABLE, Boolean.class, false);        
        ic.addContainerProperty(CONFIGURATION, Configuration.class, null);        
        ic.addContainerProperty(EVENT_SELECTOR, Collection.class, null);        
        ic.addContainerProperty(QUEUE_SELECTOR, String.class, "");
        
		try {
			Configuration[] configurations = configAdmin.listConfigurations(filter);
			if (configurations != null) {
				for (Configuration conf : configurations) {
					String itemId = conf.getPid();
					Item item = ic.addItem(itemId);
					item.getItemProperty(CONFIGURATION).setValue(conf);
					item.getItemProperty(NAME).setValue(conf.getProperties().get(NAME));
					item.getItemProperty(ENABLE).setValue(conf.getProperties().get(ENABLE));
					item.getItemProperty(ENDPOINT).setValue(conf.getProperties().get(ENDPOINT));
					Collection<String> c = (Collection<String>) conf.getProperties().get(EVENT_SELECTOR);
					if (c != null) {
						item.getItemProperty(EVENT_SELECTOR).setValue(new HashSet(c));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ic;
	}
}
