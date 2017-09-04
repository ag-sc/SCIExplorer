/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql;

/**
 * SPARQL-database interface
 * 
 * @author ABOROWI
 */
public interface SPARQLDatabaseInterface {
	public abstract QueryResult select(String query);

	public abstract QueryResult selectFromConstruct(String selectQuery, String constructQuery);
}
