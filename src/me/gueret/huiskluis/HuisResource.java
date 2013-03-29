package me.gueret.huiskluis;

import java.io.StringWriter;
import java.util.logging.Logger;

import me.gueret.huiskluis.datasources.DataSource;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

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
	private Model model = ModelFactory.createDefaultModel();

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

		// The name of the resource
		Resource r = ResourceFactory.createResource(getRequest()
				.getOriginalRef().toString());

		// If no ID has been given, return a 404
		if (postCode == null || houseNumber == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			setExisting(false);
		}

		// Extend the graph with the data from the different sources
		for (DataSource dataSource : ((Main) getApplication()).getDataSources())
			dataSource.addDataFor(model, r, postCode, houseNumber,
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
		StringWriter output = new StringWriter();
		RDFWriter writer = model.getWriter("RDF/XML");
		writer.write(model, output, "http://example.org");
		StringRepresentation representation = new StringRepresentation(output
				.getBuffer().toString(), MediaType.TEXT_XML);
		return representation;
	}

	/**
	 * Returns an RDF/XML representation of the resource
	 * 
	 * @return an RDF/XML representation of the resource
	 */
	@Get("rdf")
	public Representation toRDFXML() {
		StringWriter output = new StringWriter();
		RDFWriter writer = model.getWriter("RDF/XML");
		writer.write(model, output, "http://example.org");
		StringRepresentation representation = new StringRepresentation(output
				.getBuffer().toString(), MediaType.TEXT_XML);
		return representation;
	}

}
