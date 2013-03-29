package me.gueret.huiskluis.datasources;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public abstract class DataSource {
	public abstract void addDataFor(Model model, Resource r, String postCode,
			String houseNumber, String houseNumberToevoeging);
}
