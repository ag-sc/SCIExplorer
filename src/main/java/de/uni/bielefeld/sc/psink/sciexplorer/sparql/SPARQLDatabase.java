/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql;

import main.java.de.uni.bielefeld.sc.psink.sciexplorer.Configuration;

/**
 * SPARQL-Database
 * 
 * @author ABOROWI
 */
public class SPARQLDatabase {
	public static QueryResult select(String query) {
		return Configuration.SPARQL_DATABASE.select(query);
	}

	public static QueryResult selectWhere(String variables, String where) {
		return selectWhere(variables, where, null);
	}

	public static QueryResult selectWhere(String variables, String where, String suffix) {
		String query = QueryGenerator.generateSelectQuery(variables, where, suffix);
		// System.out.println("Query: " + query);
		return select(query);
	}

	public static int countWhere(String where) {
		// System.out.println("Count: " + where);
		return SPARQLUtility.getIntegerValue(select(QueryGenerator.generateSelectCountQuery(where)).getData().get(0).get(0).toString());
	}

	public static QueryResult selectFromConstruct(String selectQuery, String constructQuery) {
		return Configuration.SPARQL_DATABASE.selectFromConstruct(selectQuery, constructQuery);
	}

	public static QueryResult selectFromConstruct(String selectVariables, String selectWhere, String constructWhat,
			String constructWhere) {
		return selectFromConstruct(QueryGenerator.generateSelectQuery(selectVariables, selectWhere),
				QueryGenerator.generateConstructQuery(constructWhat, constructWhere));
	}

	public static QueryResult selectTriplesFromResultSubgraph(String result) {
		String selectQuery = "SELECT ?s ?p ?o WHERE {?s ?p ?o .}";
		String constructQuery = "PREFIX x: <urn:ex:> CONSTRUCT {?s ?p ?o} WHERE {<" + result
				+ "> (x:|!x:)* ?s . ?s ?p ?o . }";

		return selectFromConstruct(selectQuery, constructQuery);
	}

	public static QueryResult selectTriplesFromSubgraph(String where) {
		String selectQuery = "SELECT ?s ?p ?o WHERE {?s ?p ?o .}";
		String constructQuery = "PREFIX x: <urn:ex:> CONSTRUCT {?s ?p ?o} WHERE {" + where + "}";

		return selectFromConstruct(selectQuery, constructQuery);
	}
}
