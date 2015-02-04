package com.jjinterna.queueactions.web;

import java.io.IOException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Select;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

public class ActionsView extends VerticalLayout implements View {

	private static final String CONFIG_PID = "com.jjinterna.queueactions.subscriber";
	private static final String filter =  "(service.factoryPid=" + CONFIG_PID + ")";
	private static final String NAME = "Name";
	private static final String ENDPOINT = "Endpoint";
	private static final String HTTP_METHOD = "HttpMethod";	
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

	private TabSheet tabsheet = new TabSheet();	
	private Table actionList = new Table();
	private MenuBar menubar = new MenuBar();
	private MenuItem deleteItem;
	private Button saveChangesButton = new Button("Save");	
    private FieldGroup editorFields = new FieldGroup();
	private FormLayout generalEditorLayout = new FormLayout();    
	private FormLayout selectorsEditorLayout = new FormLayout();
	private VerticalLayout editorLayout = new VerticalLayout(tabsheet, saveChangesButton);
    private IndexedContainer actionContainer;

	private final ConfigurationAdmin configAdmin;

	public ActionsView(ConfigurationAdmin configAdmin) {
		this.configAdmin = configAdmin;
        actionContainer = createDatasource();
        initLayout();
        initEditor();
        initActionTable();
        initButtons();
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub

	}

	private void initLayout() {
		setSizeFull();

		editorLayout.setVisible(false);
		VerticalSplitPanel splitPanel = new VerticalSplitPanel();
		splitPanel.setFirstComponent(actionList);
		splitPanel.setSecondComponent(editorLayout);

		actionList.setSizeFull();

		editorLayout.setSizeFull();
		editorLayout.setExpandRatio(tabsheet, 1);

		tabsheet.setSizeFull();
		tabsheet.addTab(generalEditorLayout, "General");
		tabsheet.addTab(selectorsEditorLayout, "Selectors");		
		
		generalEditorLayout.setMargin(true);
		selectorsEditorLayout.setMargin(true);		

		addComponent(menubar);
		addComponent(splitPanel);
		setExpandRatio(splitPanel, 1);
	}

	private void initEditor() {
		String fieldName;
		
		fieldName = NAME;
		TextField field = new TextField(fieldName);
		generalEditorLayout.addComponent(field);
		field.setWidth("100%");
		field.setRequired(true);
		editorFields.bind(field, fieldName);

		fieldName = ENDPOINT;
		TextField ep = new TextField("HTTP URL");
		generalEditorLayout.addComponent(ep);
		ep.setWidth("100%");
		ep.setRequired(true);		
		editorFields.bind(ep, fieldName);

		ComboBox comboBox = new ComboBox("HTTP Method");
		comboBox.addItem("GET");
		comboBox.addItem("POST");
		comboBox.setRequired(true);
		comboBox.setNullSelectionAllowed(false);
		generalEditorLayout.addComponent(comboBox);
		editorFields.bind(comboBox, HTTP_METHOD);
		
		fieldName = ENABLE;
		CheckBox cb = new CheckBox(fieldName);
		generalEditorLayout.addComponent(cb);
		cb.setWidth("100%");
		editorFields.bind(cb, fieldName);

		fieldName = EVENT_SELECTOR;
		OptionGroup eventSelector = new OptionGroup("Event");
		eventSelector.setMultiSelect(true);
		eventSelector.addItems(eventNames);
		selectorsEditorLayout.addComponent(eventSelector);
		editorFields.bind(eventSelector, fieldName);

		fieldName = QUEUE_SELECTOR;
		TextField qs = new TextField("Queue");
		qs.setNullRepresentation("");
		selectorsEditorLayout.addComponent(qs);
		editorFields.bind(qs, fieldName);
	}

	private void initActionTable() {
		actionList.setContainerDataSource(actionContainer);
		actionList.setVisibleColumns(NAME, ENDPOINT, ENABLE);
		actionList.setSelectable(true);
		actionList.setImmediate(true);

		actionList.addValueChangeListener(new Property.ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				Object id = actionList.getValue();
				boolean isSelected = (id != null);

				if (isSelected)
					editorFields.setItemDataSource(actionList
							.getItem(id));
				
				editorLayout.setVisible(isSelected);
				deleteItem.setEnabled(isSelected);
			}
		});
	}

	private void initButtons() {
		
		MenuItem actions = menubar.addItem("Actions", null);
		
		actions.addItem("New", new Command() {			
			@Override
			public void menuSelected(MenuItem selectedItem) {
				Object actionId = actionContainer.addItemAt(0);
				actionList.select(actionId);
			}
		});
		deleteItem = actions.addItem("Delete", new Command() {

			@Override
			public void menuSelected(MenuItem selectedItem) {
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
		deleteItem.setEnabled(false);
		
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
					props.put(HTTP_METHOD, editorFields.getField(HTTP_METHOD).getValue());					
					Collection<String> c = (Collection<String>) editorFields.getField(EVENT_SELECTOR).getValue();
					if (!c.isEmpty()) {
						props.put(EVENT_SELECTOR, c);
					}
					Object value = editorFields.getField(QUEUE_SELECTOR).getValue();
					if (value != null)
						props.put(QUEUE_SELECTOR, value);					
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
        ic.addContainerProperty(HTTP_METHOD, String.class, "GET");
        ic.addContainerProperty(ENABLE, Boolean.class, false);        
        ic.addContainerProperty(CONFIGURATION, Configuration.class, null);        
        ic.addContainerProperty(EVENT_SELECTOR, Collection.class, null);        
        ic.addContainerProperty(QUEUE_SELECTOR, String.class, null);
        
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
					item.getItemProperty(HTTP_METHOD).setValue(conf.getProperties().get(HTTP_METHOD));					
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

		return ic;
	}

}
