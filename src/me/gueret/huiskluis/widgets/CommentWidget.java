package me.gueret.huiskluis.widgets;

import me.gueret.huiskluis.restricted.RestrictedData;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class CommentWidget extends ServerResource {
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
	 * Get the comment
	 * @return
	 */
	@Get
	public Representation getComment() {
		// Fill in the reply
		JSONObject reply = new JSONObject();
		RestrictedData restrictedData = new RestrictedData();
		String comment = restrictedData.get(identifier, RestrictedData.COMMENT);
		try {
			reply.put("comment", comment);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JsonRepresentation output = new JsonRepresentation(reply);
		return output;
	}

	/**
	 * Update the comment
	 * 
	 * @return
	 */
	@Put
	public Representation updateComment() {
		String comment = getQuery().getFirstValue("comment");

		if (comment != null) {
			RestrictedData restrictedData = new RestrictedData();
			restrictedData.set(identifier, RestrictedData.COMMENT, comment);
			setStatus(Status.SUCCESS_OK);
			return new StringRepresentation("ok", MediaType.TEXT_PLAIN);
		}

		setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		return new StringRepresentation("error", MediaType.TEXT_PLAIN);
	}

}
