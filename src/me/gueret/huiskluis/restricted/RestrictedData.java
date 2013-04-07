package me.gueret.huiskluis.restricted;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class RestrictedData {
	// Entity type for the data store
	private final static String DATA_ENTITY = "RestrictedData";

	// Key for owner
	public final static String OWNER = "owner";

	// Key for description
	public final static String DESCRIPTION = "description";

	/**
	 * @param postCode
	 * @param number
	 * @return
	 */
	public String get(String postCode, String number, String property) {
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Key k = KeyFactory.createKey(DATA_ENTITY, postCode + "-" + number);
		try {
			Entity entity = datastore.get(k);
			return (String) entity.getProperty(property);
		} catch (EntityNotFoundException e) {
			return "";
		}
	}

	/**
	 * @param postCode
	 * @param number
	 */
	public void set(String postCode, String number, String property,
			String value) {
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Entity entity = null;
		try {
			Key k = KeyFactory.createKey(DATA_ENTITY, postCode + "-" + number);
			entity = datastore.get(k);
		} catch (EntityNotFoundException e) {
			entity = new Entity(DATA_ENTITY, postCode + "-" + number);
		}
		entity.setProperty(property, value);
		datastore.put(entity);
	}
}
