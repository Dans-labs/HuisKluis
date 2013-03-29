package me.gueret.huiskluis.datasources;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.ext.rdf.Graph;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;

public class BAGClient extends DataSource {
	// Logger
	protected static final Logger logger = Logger.getLogger(BAGClient.class
			.getName());

	// The query
	private final String queryTemplate;

	/**
	 * 
	 */
	public BAGClient(Context context) {
		// Load the query
		String request = "war:///WEB-INF/data/query_geodan.rq";
		Restlet client = context.getClientDispatcher();
		Response response = client.handle(new Request(Method.GET, request));
		queryTemplate = response.getEntityAsText();
	}

	/**
	 * @param postCode
	 * @param houseNumber
	 */
	public void addDataFor(Graph graph, String postCode, String houseNumber,
			String houseNumberToevoeging) {
		// Location of the end point
		String endPoint = "http://lod.geodan.nl/BAG/sparql?format=ttl&query=";

		// Compose the query
		String query = new String(queryTemplate);
		query = query.replace("{POSTCODE}", postCode);
		query = query.replace("{NUMMER}", houseNumber);
		query = query.replace("{TOEVOEGING}", houseNumberToevoeging);

		try {
			// Compose the query URL
			StringBuffer urlString = new StringBuffer(endPoint);
			urlString.append(URLEncoder.encode(query.toString(), "utf-8"));
			URL url = new URL(urlString.toString());
			System.out.println(url.toString());

			// Issue the request
			StringBuffer response = new StringBuffer();
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setConnectTimeout(0);
			connection.setRequestProperty("Accept", "application/rdf+xml");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();
			Representation re = new StringRepresentation(response);
			System.out.println(re);

			// Parse the output
			Model model = ModelFactory.createDefaultModel();
			RDFReader r = model.getReader("RDF/XML");
			r.read(model, new StringReader(response.toString()),
					"http://example.org");

			System.out.println(model);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getQueryTemplate() {
		return queryTemplate;
	}
}
