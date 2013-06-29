package me.gueret.huiskluis.datasources;

import java.util.HashMap;
import java.util.Map;
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
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class DataSourceBAG extends DataSource {
	// Logger
	protected static final Logger logger = Logger.getLogger(DataSourceBAG.class
			.getName());

	// The query templates
	private final Map<String, String> queryTemplates = new HashMap<String, String>();

	/**
	 * 
	 */
	public DataSourceBAG(Context context) {
		Request req = null;
		Response response = null;

		// Load the query templates
		Restlet client = context.getClientDispatcher();
		req = new Request(Method.GET, "war:///WEB-INF/data/query_BAG_Geodan.rq");
		response = client.handle(req);
		queryTemplates.put("Geodan", response.getEntityAsText());
		req = new Request(Method.GET, "war:///WEB-INF/data/query_BAG_Bart.rq");
		response = client.handle(req);
		queryTemplates.put("Bart", response.getEntityAsText());
	}

	/**
	 * @param postCode
	 * @param houseNumber
	 */
	public void addDataFor(Model model, Resource r, String postCode,
			String houseNumber, String houseNumberToevoeging) {
		getDataFromBart(model, r, postCode, houseNumber, houseNumberToevoeging);
	}

	/**
	 * @param model
	 * @param r
	 * @param postCode
	 * @param houseNumber
	 * @param houseNumberToevoeging
	 */
	protected void getDataFromBart(Model model, Resource r, String postCode,
			String houseNumber, String houseNumberToevoeging) {
		// Compose the query
		String query = new String(queryTemplates.get("Geodan"));
		query = query.replace("{POSTCODE}", postCode);
		query = query.replace("{NUMMER}", houseNumber);
		if (houseNumberToevoeging == null || houseNumberToevoeging.equals("")) {
			query = query.replace("{TOEVOEGING}", "");
		} else {
			// query = query.replace("{TOEVOEGING}",
			// "?huis <http://www.w3.org/ns/locn#locatorDesignator> \""
			// + houseNumberToevoeging + "\".");
			query = query.replace("{TOEVOEGING}",
					"?huis vocab:nummeraanduiding_huisnummertoevoeging \""
							+ houseNumberToevoeging + "\"^^xsd:string.");
		}

		// Execute it
		// QueryExecution qexec = QueryExecutionFactory.sparqlService(
		// "http://data.resc.info/bag/sparql", query);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				"http://lod.geodan.nl/BAG/sparql", query);

		ResultSet results = qexec.execSelect();
		qexec.setTimeout(0);

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

			String a = result.get("point").toString();
			a = a.replace(
					")^^http://lod.geodan.nl/geosparql_vocab_all.rdf#WKTLiteral",
					"");
			a = a.replace("POINT(", "");
			String[] pp = a.split(" ");

			// Add the latitude
			p = ResourceFactory
					.createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
			Literal lat = ResourceFactory.createTypedLiteral(Float
					.valueOf(pp[1]));
			model.add(r, p, lat);

			// Add the longitude
			p = ResourceFactory
					.createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#long");
			Literal lng = ResourceFactory.createTypedLiteral(Float
					.valueOf(pp[0]));
			model.add(r, p, lng);

			/*
			 * // Add the latitude p = ResourceFactory
			 * .createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
			 * Literal lat = ResourceFactory.createTypedLiteral(Float
			 * .valueOf(result.get("lat").toString())); model.add(r, p, lat);
			 * 
			 * // Add the longitude p = ResourceFactory
			 * .createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#long");
			 * Literal lng = ResourceFactory.createTypedLiteral(Float
			 * .valueOf(result.get("lng").toString())); model.add(r, p, lng);
			 */

			// Add the polygon
			p = ResourceFactory
					.createProperty("http://www.example.org#polygon");
			Literal poly = ResourceFactory.createPlainLiteral(result.get(
					"polygon").toString());
			model.add(r, p, poly);

		}
	}

	/**
	 * @param model
	 * @param r
	 * @param postCode
	 * @param houseNumber
	 * @param houseNumberToevoeging
	 */
	protected void getDataFromGeodan(Model model, Resource r, String postCode,
			String houseNumber, String houseNumberToevoeging) {
		// Compose the query
		String query = new String(queryTemplates.get("Geodan"));
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
			Literal poly = ResourceFactory.createPlainLiteral(result.get(
					"polygon").toString());
			model.add(r, p, poly);
		}
	}
}
