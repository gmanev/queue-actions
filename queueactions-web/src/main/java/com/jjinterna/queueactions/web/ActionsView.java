package com.jjinterna.queueactions.web;

import java.io.IOException;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.FieldGroup;
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
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

public class ActionsView extends VerticalLayout implements View {

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
	private ActionsData actionContainer;

	public ActionsView(ConfigurationAdmin configAdmin) {
        actionContainer = new ActionsData(configAdmin);
        actionContainer.loadData();
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
		
		fieldName = ActionsData.NAME;
		TextField field = new TextField(fieldName);
		generalEditorLayout.addComponent(field);
		field.setWidth("100%");
		field.setRequired(true);
		editorFields.bind(field, fieldName);

		fieldName = ActionsData.ENDPOINT_URI;
		TextField ep = new TextField("Endpoint URI");
		generalEditorLayout.addComponent(ep);
		ep.setWidth("100%");
		ep.setRequired(true);		
		editorFields.bind(ep, fieldName);

		ComboBox comboBox = new ComboBox("Endpoint Protocol");
		comboBox.addItem("HTTP GET");
		comboBox.addItem("HTTP POST");
		comboBox.addItem("SOAP");
		comboBox.setRequired(true);
		comboBox.setNullSelectionAllowed(false);
		generalEditorLayout.addComponent(comboBox);
		editorFields.bind(comboBox, ActionsData.ENDPOINT_PROTOCOL);
		
		fieldName = ActionsData.ENABLE;
		CheckBox cb = new CheckBox(fieldName);
		generalEditorLayout.addComponent(cb);
		cb.setWidth("100%");
		editorFields.bind(cb, fieldName);

		fieldName = ActionsData.EVENT_SELECTOR;
		OptionGroup eventSelector = new OptionGroup("Event");
		eventSelector.setMultiSelect(true);
		eventSelector.addItems(eventNames);
		selectorsEditorLayout.addComponent(eventSelector);
		editorFields.bind(eventSelector, fieldName);

		fieldName = ActionsData.QUEUE_SELECTOR;
		TextField qs = new TextField("Queue");
		qs.setNullRepresentation("");
		selectorsEditorLayout.addComponent(qs);
		editorFields.bind(qs, fieldName);
	}

	private void initActionTable() {
		actionList.setContainerDataSource(actionContainer);
		actionList.setVisibleColumns(ActionsData.NAME, ActionsData.ENDPOINT_URI, ActionsData.ENABLE);
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
				Configuration conf = (Configuration) actionList.getItem(itemId).getItemProperty(ActionsData.CONFIGURATION).getValue();
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
					editorFields.commit();
					actionContainer.commit(itemId);
		            Notification.show("Saved");					
				} catch (Exception e) {
					e.printStackTrace();
		            Notification.show(e.getMessage());					
				}	
			}
			
		});
	
	}
}
