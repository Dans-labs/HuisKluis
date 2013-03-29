package me.gueret.huiskluis.datasources;

import org.restlet.ext.rdf.Graph;

public abstract class DataSource {
	public abstract void addDataFor(Graph graph, String postCode,
			String houseNumber, String houseNumberToevoeging);
}
