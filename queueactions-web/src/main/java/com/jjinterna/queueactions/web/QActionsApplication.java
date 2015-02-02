package com.jjinterna.queueactions.web;

import java.io.IOException;
import java.util.Dictionary;
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
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("valo")
public class QActionsApplication extends UI {

	private static final String CONFIG_PID = "com.jjinterna.queueactions.subscriber";
	private static final String filter =  "(service.factoryPid=" + CONFIG_PID + ")";
	
	private ConfigurationAdmin configAdmin;
	private String title;

	private Table actionList = new Table();
	private Button addNewActionButton = new Button("New");
	private Button removeActionButton = new Button("Remove this action");	
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
        initAddRemoveButtons();        
	}

	private void initLayout() {

		/* Root of the user interface component tree is set */
		HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		setContent(splitPanel);

		/* Build the component tree */
		VerticalLayout leftLayout = new VerticalLayout();
		splitPanel.addComponent(leftLayout);
		splitPanel.addComponent(editorLayout);
		leftLayout.addComponent(actionList);
		HorizontalLayout bottomLeftLayout = new HorizontalLayout();
		leftLayout.addComponent(bottomLeftLayout);
		bottomLeftLayout.addComponent(addNewActionButton);

		/* Set the contents in the left of the split panel to use all the space */
		leftLayout.setSizeFull();
		leftLayout.setExpandRatio(actionList, 1);
		actionList.setSizeFull();

		bottomLeftLayout.setWidth("100%");

		/* Put a little margin around the fields in the right side editor */
		editorLayout.setMargin(true);
		editorLayout.setVisible(false);
	}

	private void initEditor() {

		editorLayout.addComponent(removeActionButton);

		/* User interface can be created dynamically to reflect underlying data. */
		String fieldName;
		
		fieldName = "Name";
		TextField field = new TextField(fieldName);
		editorLayout.addComponent(field);
		field.setWidth("100%");

		editorFields.bind(field, fieldName);

		/*
		 * Data can be buffered in the user interface. When doing so, commit()
		 * writes the changes to the data source. Here we choose to write the
		 * changes automatically without calling commit().
		 */
		editorFields.setBuffered(false);
	}

	private void initActionTable() {
		actionList.setContainerDataSource(actionContainer);
		actionList.setVisibleColumns(new String[] { "Name" });
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

	private void initAddRemoveButtons() {
		addNewActionButton.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				actionContainer.removeAllContainerFilters();
				Object actionId = actionContainer.addItemAt(0);

				/*
				 * Each Item has a set of Properties that hold values. Here we
				 * set a couple of those.
				 */
				actionList.getContainerProperty(actionId, "Name").setValue(
						"New");
				
				try {
					Dictionary<String, String> props = new Hashtable<>();
					props.put("foo", "bar");
					Configuration conf = configAdmin.createFactoryConfiguration(CONFIG_PID, null);
					conf.update(props);
					actionList.getContainerProperty(actionId, "Configuration").setValue(conf);
					actionList.select(actionId);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		removeActionButton.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				Object itemId = actionList.getValue();
				Configuration conf = (Configuration) actionList.getItem(itemId).getItemProperty("Configuration").getValue();
				try {
					conf.delete();
					actionList.removeItem(itemId);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private IndexedContainer createDatasource() {
		IndexedContainer ic = new IndexedContainer();

        ic.addContainerProperty("Name", String.class, "");
        ic.addContainerProperty("Configuration", Configuration.class, "");        

		try {
			Configuration[] configurations = configAdmin.listConfigurations(filter);
			if (configurations != null) {
				for (Configuration conf : configurations) {
					String itemId = conf.getPid();

					Item item = ic.addItem(itemId);
					item.getItemProperty("Name").setValue(itemId);
					item.getItemProperty("Configuration").setValue(conf);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ic;
	}
}
