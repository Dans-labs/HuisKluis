package me.gueret.huiskluis;

import java.util.logging.Logger;

import me.gueret.huiskluis.widgets.CommentWidget;
import me.gueret.huiskluis.widgets.DataWidget;
import me.gueret.huiskluis.widgets.MapWidget;
import me.gueret.huiskluis.widgets.PhotosWidget;

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

		// Handler for the different widgets
		router.attach("/widget/map/{IDENTIFIER}", MapWidget.class);
		router.attach("/widget/photo/{IDENTIFIER}", PhotosWidget.class);
		router.attach("/widget/data/{IDENTIFIER}", DataWidget.class);
		router.attach("/widget/comment/{IDENTIFIER}", CommentWidget.class);
		
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
}
