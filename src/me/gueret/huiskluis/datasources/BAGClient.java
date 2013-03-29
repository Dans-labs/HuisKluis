package me.gueret.huiskluis.datasources;

import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class BAGClient extends DataSource {
	// Logger
	protected static final Logger logger = Logger
			.getLogger(BAGClient.class.getName());

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
	public void addDataFor(Model model, Resource r, String postCode,
			String houseNumber, String houseNumberToevoeging) {
		// Compose the query
		String query = new String(queryTemplate);
		query = query.replace("{POSTCODE}", postCode);
		query = query.replace("{NUMMER}", houseNumber);
		query = query.replace("{TOEVOEGING}", houseNumberToevoeging);

		// Execute
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				"http://lod.geodan.nl/BAG/sparql", query);

		ResultSet results = qexec.execSelect();
		Property p;
		if (results.hasNext()) {
			QuerySolution result = results.next();
			p = ResourceFactory.createProperty("http://www.example.org#street");
			model.add(r, p, result.get("straat"));
			p = ResourceFactory
					.createProperty("http://www.example.org#construction");
			model.add(r, p, result.get("bouwjaar"));
		}

	}

	/**
	 * @return
	 */
	public String getQueryTemplate() {
		return queryTemplate;
	}
}
