package me.gueret.huiskluis;

import java.util.logging.Logger;

import me.gueret.huiskluis.datasources.DataSource;

import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.rdf.Graph;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * @author Christophe Gueret <christophe.gueret@gmail.com>
 */
public class HuisResource extends ServerResource {
	// Logger instance
	protected static final Logger logger = Logger.getLogger(HuisResource.class
			.getName());

	// The post code
	private String postCode = null;

	// The number of the house, with eventual addition
	private String number = null;

	// The graph that will contain the data about that resource
	private Graph graph = new Graph();

	// The name of the resource;
	private Reference resource = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.restlet.resource.UniformResource#doInit()
	 */
	@Override
	protected void doInit() throws ResourceException {
		// Get the dataset name from the URI template
		postCode = (String) getRequest().getAttributes().get("POSTCODE");
		number = (String) getRequest().getAttributes().get("NUMBER");

		// Split the house number
		String[] parts = number.split("-");
		String houseNumber = parts[0];
		String houseNumberToevoeging = (parts.length == 2 ? parts[1] : "");

		// If no ID has been given, return a 404
		if (postCode == null || houseNumber == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			setExisting(false);
		}

		// Extend the graph with the data from the different sources
		for (DataSource dataSource : ((Main) getApplication()).getDataSources())
			dataSource.addDataFor(graph, postCode, houseNumber,
					houseNumberToevoeging);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.restlet.resource.Resource#getApplication()
	 */
	@Override
	public Main getApplication() {
		return (Main) super.getApplication();
	}

	/**
	 * Returns an HTML representation of the resource
	 * 
	 * @return an HTML representation of the resource
	 */
	@Get("html")
	public Representation toHTML() {
		return graph.getRdfXmlRepresentation();
	}

	/**
	 * Returns an RDF/XML representation of the resource
	 * 
	 * @return an RDF/XML representation of the resource
	 */
	@Get("rdf")
	public Representation toRDFXML() {
		return graph.getRdfXmlRepresentation();
	}

}
