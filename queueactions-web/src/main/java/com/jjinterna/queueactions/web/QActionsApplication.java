package com.jjinterna.queueactions.web;

import org.osgi.service.cm.ConfigurationAdmin;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;

@Theme("valo")
public class QActionsApplication extends UI {
	
	private ConfigurationAdmin configAdmin;
	private String title;
	private static final String[][] menuItems = {
		{ "actions", null, "QueueActions" }
	};

	private Navigator navigator;

	public QActionsApplication(ConfigurationAdmin configAdmin, String title) {
		this.configAdmin = configAdmin;
		this.title = title;
	}
	
	@Override
	protected void init(VaadinRequest request) {
        initLayout();
	}


	private void initLayout() {
		getPage().setTitle(title);		
        setSizeFull();
                
		final Tree menu = new Tree();
		menu.setItemCaptionPropertyId("name");
		menu.getContainerDataSource().addContainerProperty("name", String.class, "");
		menu.setSizeFull();
		
		for (String[] s : menuItems) {
			String itemId = s[0];
			String parentId = s[1];
			
			Item item = menu.addItem(itemId);
			menu.setChildrenAllowed(itemId, false);
			item.getItemProperty("name").setValue(s[2]);
			if (parentId != null) {
				menu.setChildrenAllowed(parentId, true);
				menu.setParent(itemId, parentId);
				menu.expandItem(parentId);
			}
		}
		
		menu.addItemClickListener(new ItemClickListener() {

			@Override
			public void itemClick(ItemClickEvent event) {
				if (event.getItemId() != null) {
					navigator.navigateTo(event.getItemId().toString());
				}
			}
			
		});

		Panel panel = new Panel();
		panel.setSizeFull();

        HorizontalSplitPanel split = new HorizontalSplitPanel();
        split.setFirstComponent(menu);
        split.setSecondComponent(panel);
        split.setSplitPosition(15);
       
        
		setContent(split);
		
		navigator = new Navigator(this, panel);
		navigator.setErrorView(new Navigator.EmptyView());
		navigator.addView("actions", new ActionsView(configAdmin));
		
	}

}
