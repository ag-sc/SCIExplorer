/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.bielefeld.sc.psink.sciexplorer.sparql;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import de.uni.bielefeld.sc.psink.sciexplorer.Configuration;
import de.uni.bielefeld.sc.psink.sciexplorer.ResourceUtility;

/**
 * Apache Jena SPARQL-database. Implements the SPARQL-database interface.
 * 
 * @author ABOROWI
 */
public class ApacheJenaDatabase implements SPARQLDatabaseInterface {
	private final Model model;

	public ApacheJenaDatabase() {
		model = ModelFactory.createDefaultModel();
		model.read(ResourceUtility.getResourceAsStream(Configuration.RDF_TRIPLES_FILE), null, "N-TRIPLES");
	}

	@Override
	public QueryResult select(String queryString) {
		return select(model, queryString);
	}

	private static QueryResult select(Model model, String queryString) {
		// System.out.println("Query: " + queryString);
		Query query = QueryFactory.create(queryString);
		List<List<String>> data = new LinkedList();
		List<String> variables;
		List<String> row;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			variables = results.getResultVars();
			QuerySolution solution;

			while (results.hasNext()) {
				solution = results.nextSolution();
				row = new LinkedList();
				for (String variable : variables) {
					try {
						row.add(solution.getResource(variable).toString());
					} catch (ClassCastException e) {
						row.add(solution.getLiteral(variable).toString());
					}

					/*
					 * try { Resource res = solution.getResource(variable);
					 * if(res == null) { row.add(""); } else {
					 * row.add(res.toString()); } } catch(ClassCastException e)
					 * { Literal lit = solution.getLiteral(variable); if(lit ==
					 * null) { row.add(""); } else { row.add(lit.toString()); }
					 * }
					 */

				}
				data.add(row);
			}
		}
		return new QueryResult(variables, data);
	}

	private Model construct(String queryString) {
		Query query = QueryFactory.create(queryString);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			return qexec.execConstruct();
		}
	}

	@Override
	public QueryResult selectFromConstruct(String selectQuery, String constructQuery) {
		return select(construct(constructQuery), selectQuery);
	}
}
