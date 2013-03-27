package me.gueret.huiskluis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.restlet.ext.rdf.Graph;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;

public class BAGClient {

	private Graph graph;

	public BAGClient(Graph graph) {
		this.graph = graph;
	}

	/**
	 * @param postCode
	 * @param houseNumber
	 */
	public void addDataFor(String postCode, String houseNumber) {
		// Location of the end point
		String endPoint = "http://data.resc.info/bag/sparql?format=ttl&query=";

		// Compose the query
		StringBuffer query = new StringBuffer();
		query.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
		query.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
		query.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>");
		query.append("PREFIX locn: <http://www.w3.org/ns/locn#>");
		query.append("PREFIX bag: <http://data.resc.info/bag/resource/vocab/>");
		query.append("CONSTRUCT { ?huis bag:pandactueel_bouwjaar ?bouwjaar} WHERE {");
		query.append("?huis locn:postCode \"").append(postCode).append("\".");
		String[] l = houseNumber.split("-");
		query.append("?huis locn:locatorDesignator \"").append(l[0])
				.append("\"^^xsd:decimal.");
		if (l.length == 2)
			query.append("?huis locn:locatorDesignator \"").append(l[1])
					.append("\".");

		query.append("?huis bag:adres_adresseerbaarobject ?obj.");
		query.append("?obj bag:verblijfsobjectactueel_pand ?pand.");
		query.append("?pand bag:pandactueel_bouwjaar ?bouwjaar.");
		query.append("}");

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
}
