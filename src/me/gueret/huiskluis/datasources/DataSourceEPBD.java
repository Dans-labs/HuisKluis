package me.gueret.huiskluis.datasources;


import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

// http://ep-online.nl/
// http://stackoverflow.com/questions/4576822/some-help-scraping-a-page-in-java
// http://htmlunit.sourceforge.net/gettingStarted.html
// http://stackoverflow.com/questions/3549890/how-to-programmatically-access-web-page-in-java
	
public class DataSourceEPBD extends DataSource {
	Restlet client;

	/**
	 * 
	 */
	public DataSourceEPBD(Context context) {
		// Get a pointer to the client
		client = context.getClientDispatcher();
		
		// Test
		Request request = new Request(Method.POST, "http://ep-online.nl/EnergieLabel.aspx");
		Form form = new Form ();
		form.add("ctl00$MainPlaceHolder$SmartSearch$SearchValueTextBox", "9415RD, 2");
		request.setEntity(form.toString(), MediaType.APPLICATION_WWW_FORM);
		request.getAttributes().put("Content-Type"	, "application/x-www-form-urlencoded");
		Response response = client.handle(request);
		System.out.println(response.getStatus().toString());
		System.out.println(response.getEntityAsText());
	}

	@Override
	public void addDataFor(Model model, Resource r, String postCode,
			String houseNumber, String houseNumberToevoeging) {

	}

}
