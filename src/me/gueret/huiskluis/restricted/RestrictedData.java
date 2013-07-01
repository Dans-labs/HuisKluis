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

	// Key for description
	public final static String COMMENT = "comment";

	/**
	 * @param identifier
	 * @return
	 */
	public String get(String identifier, String property) {
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Key k = KeyFactory.createKey(DATA_ENTITY, identifier);
		try {
			Entity entity = datastore.get(k);
			return (String) entity.getProperty(property);
		} catch (EntityNotFoundException e) {
			return "";
		}
	}

	/**
	 * @param identifier
	 */
	public void set(String identifier, String property, String value) {
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Entity entity = null;
		try {
			Key k = KeyFactory.createKey(DATA_ENTITY, identifier);
			entity = datastore.get(k);
		} catch (EntityNotFoundException e) {
			entity = new Entity(DATA_ENTITY, identifier);
		}
		entity.setProperty(property, value);
		datastore.put(entity);
	}
}
