package com.jjinterna.queueactions.subscriber.jms.impl;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsRouteFactory implements ManagedServiceFactory {

	private CamelContext camelContext;
	private BundleContext bundleContext;

	private static final String CONFIG_PID = "com.jjinterna.queueactions.subscriber";
	private ServiceRegistration managedServiceReg;

	private Map<String, HttpActionRoute> routes = new HashMap<>();

	private static final Logger LOG = LoggerFactory
			.getLogger(JmsRouteFactory.class);
	
	public void setCamelContext(CamelContext camelContext) {
		this.camelContext = camelContext;
	}
	
	public void setContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	
	public void init() {
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.put(Constants.SERVICE_PID, CONFIG_PID);
		managedServiceReg = bundleContext.registerService(ManagedServiceFactory.class.getName(), this, properties);
	}
	
	public void destroy() {
		managedServiceReg.unregister();
	}

	@Override
	public String getName() {
		return "Factory for " + HttpActionRoute.class.getSimpleName();
	}

	@Override
	public void updated(String pid, Dictionary properties)
			throws ConfigurationException {
		LOG.debug("Updating...");

		Boolean routeEnable = false;
		Object obj = properties.get("Enable");
		if (obj != null) {
			routeEnable = (Boolean) obj;
		}
		String routeName = (String) properties.get("Name");

		HttpActionRoute route = routes.get(pid);
		if (route == null) {
			LOG.info("Building new route");
			if (routeEnable) {
				addRoute(pid, new HttpActionRoute(routeName, properties));
			}
		} else {
			LOG.info("Updating existing route");			
			removeRoute(pid, route);
			if (routeEnable) {
				addRoute(pid, new HttpActionRoute(routeName, properties));
			}
		}
	}

	@Override
	public void deleted(String pid) {
		LOG.debug("Deleting...");
		if (routes.get(pid) != null)
			removeRoute(pid, routes.get(pid));
	}

	private void removeRoute(final String pid, final HttpActionRoute route) {
		try {
			camelContext.stopRoute(route.getRouteId());
			camelContext.removeRoute(route.getRouteId());
		} catch (Exception e) {
			LOG.error("Failed to stop and remove route " + route.getRouteId());
		}
		routes.remove(pid);
	}

	private void addRoute(final String pid, final HttpActionRoute route) {
		try {
			camelContext.addRoutes(route);
		} catch (Exception e) {
			LOG.error("Failed to add route", e);
		}
		routes.put(pid, route);
	}

}
