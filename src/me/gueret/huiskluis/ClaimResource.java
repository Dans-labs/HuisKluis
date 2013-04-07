package me.gueret.huiskluis;

import java.util.logging.Logger;

import me.gueret.huiskluis.restricted.RestrictedData;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class ClaimResource extends ServerResource {
	// Logger instance
	protected static final Logger logger = Logger.getLogger(ClaimResource.class
			.getName());

	// The post code
	private String postCode = null;

	// The number of the house, with eventual addition
	private String number = null;

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

		// If no ID has been given, return a 404
		if (postCode == null || number == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			setExisting(false);
		}
	}

	/**
	 * @return
	 */
	@Put
	public Representation updateInformation() {
		String loggedInUser = getQuery().getFirstValue("email");
		String description = getQuery().getFirstValue("description");
		if (loggedInUser == null && description == null) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		} else {
			RestrictedData restrictedData = new RestrictedData();
			if (loggedInUser != null) {
				restrictedData.set(postCode, number, RestrictedData.OWNER,
						loggedInUser);
			}
			if (description != null) {
				restrictedData.set(postCode, number,
						RestrictedData.DESCRIPTION, description);
			}
			setStatus(Status.SUCCESS_OK);
		}
		return new StringRepresentation("ok", MediaType.TEXT_PLAIN);
	}
}
