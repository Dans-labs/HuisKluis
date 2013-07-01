package me.gueret.huiskluis.widgets;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class MapWidget extends ServerResource {
	// The identifier of the house
	private String identifier = "";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.restlet.resource.UniformResource#doInit()
	 */
	@Override
	protected void doInit() throws ResourceException {
		// Get the dataset name from the URI template
		identifier = (String) getRequest().getAttributes().get("IDENTIFIER");
		identifier = identifier.toUpperCase().replace(" ", "").replace("+", "");

		// If no ID has been given, return a 404
		if (identifier == null || identifier == "") {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			setExisting(false);
		}
	}

	/**
	 * @return
	 */
	@Get
	public Representation getJSONData() {
		// Split the identifier into house number parts
		String[] parts = identifier.split("-");
		String postCode = parts[0];
		String houseNumber = parts[1];
		String houseNumberToevoeging = (parts.length == 3 ? parts[2] : "");

		// Load the SPARQL template from the WAR file
		Restlet client = getContext().getClientDispatcher();
		Request req = new Request(Method.GET,
				"war:///WEB-INF/data/query_map_widget.rq");
		String query = client.handle(req).getEntityAsText();

		// Instantiate the query
		query = query.replace("{POSTCODE}", postCode);
		query = query.replace("{NUMMER}", houseNumber);
		if (houseNumberToevoeging == null || houseNumberToevoeging.equals("")) {
			query = query.replace("{TOEVOEGING}", "");
		} else {
			query = query.replace("{TOEVOEGING}",
					"?huis vocab:nummeraanduiding_huisnummertoevoeging \""
							+ houseNumberToevoeging + "\"^^xsd:string.");
		}

		// Execute the query
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				"http://lod.geodan.nl/BAG/sparql", query);
		qexec.setTimeout(0);
		ResultSet results = qexec.execSelect();

		// Fill in the reply
		JSONObject reply = new JSONObject();
		try {
			if (results.hasNext()) {
				QuerySolution result = results.next();

				// Get the latitude and longitude
				String pt = result.get("point").toString();
				pt = pt.replace(
						")^^http://lod.geodan.nl/geosparql_vocab_all.rdf#WKTLiteral",
						"").replace("POINT(", "");

				String[] latlong = pt.split(" ");
				reply.put("lat", latlong[1]);
				reply.put("long", latlong[0]);
				
				// Get the contour
				String contour = result.get("polygon").toString();
				contour = contour.replace(
						")^^http://lod.geodan.nl/geosparql_vocab_all.rdf#WKTLiteral",
						"").replace("POLYGON(", "");

				reply.put("polygon", contour);
				
			}
		} catch (JSONException e) {
		}

		JsonRepresentation output = new JsonRepresentation(reply);
		return output;
	}

}
