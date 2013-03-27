package me.gueret.huiskluis;

import java.util.logging.Logger;

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

	/**
	 * Creates a root Restlet that will receive all incoming calls and route
	 * them to the corresponding handlers
	 */
	@Override
	public Restlet createInboundRoot() {
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
		// Create the HTTP server and listen on port 8182
		// Server server = new Server(Protocol.HTTP, 8182);
		// Application a = new Main();
		// a.setContext(server.getContext());
		// a.start();
		// System.out.println(a.getContext());

		Component component = new Component();
		component.getClients().add(Protocol.WAR);
		component.getServers().add(Protocol.HTTP, 8080);
		component.getDefaultHost().attach(new Main());
		component.start();

	}

}
