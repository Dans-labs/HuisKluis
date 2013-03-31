package me.gueret.huiskluis.datasources;

import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class DataSourceBAG extends DataSource {
	// Logger
	protected static final Logger logger = Logger.getLogger(DataSourceBAG.class
			.getName());

	// The query
	private final String queryTemplate;

	/**
	 * 
	 */
	public DataSourceBAG(Context context) {
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

			// Add the street
			p = ResourceFactory.createProperty("http://www.example.org#street");
			model.add(r, p, result.get("straat"));

			// Add the construction year
			p = ResourceFactory
					.createProperty("http://www.example.org#construction");
			model.add(r, p, result.get("bouwjaar"));

			// Add the lat and long
			String point = result.get("point").asLiteral().getLexicalForm();
			String[] pts = point.replace("POINT(", "").replace(")", "")
					.split(" ");
			p = ResourceFactory
					.createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
			Literal lat = ResourceFactory.createTypedLiteral(Float
					.valueOf(pts[1]));
			model.add(r, p, lat);
			p = ResourceFactory
					.createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#long");
			Literal lng = ResourceFactory.createTypedLiteral(Float
					.valueOf(pts[0]));
			model.add(r, p, lng);

			// Add the polygon
			p = ResourceFactory
					.createProperty("http://www.example.org#polygon");
			model.add(r, p, result.get("polygon"));

		}

	}

	/**
	 * @return
	 */
	public String getQueryTemplate() {
		return queryTemplate;
	}
}
