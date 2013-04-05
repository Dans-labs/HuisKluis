package me.gueret.huiskluis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import me.gueret.huiskluis.datasources.DataSourceBAG;
import me.gueret.huiskluis.datasources.DataSource;
import me.gueret.huiskluis.datasources.DataSourceEPBD;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;


/**
 * @author Christophe Gueret <christophe.gueret@gmail.com>
 */
public class Main extends Application {
	// Logger
	protected static final Logger logger = Logger.getLogger(Main.class.getName());

	// List of data sources
	private Map<String, DataSource> dataSources = new HashMap<String, DataSource>();
	
	/**
	 * Creates a root Restlet that will receive all incoming calls and route
	 * them to the corresponding handlers
	 */
	@Override
	public Restlet createInboundRoot() {
		// Create the data sources
		dataSources.put("BAG", new DataSourceBAG(getContext()));
		dataSources.put("EPBD", new DataSourceEPBD(getContext()));
		
		// Create the router
		Router router = new Router(getContext());

		// Handler for requests to parameters setting
		router.attach("/data/{POSTCODE}/{NUMBER}", HuisResource.class);
		
		// Activate content filtering based on extensions
		getTunnelService().setExtensionsTunnel(true);

		return router;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Component component = new Component();
		component.getClients().add(Protocol.WAR);
		component.getServers().add(Protocol.HTTP, 8080);
		component.getDefaultHost().attach(new Main());
		component.start();
	}


	/**
	 * @return
	 */
	public Collection<DataSource> getDataSources() {
		return dataSources.values();
	}

}
