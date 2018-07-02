/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql;

import java.io.Serializable;
import java.util.List;

/**
 * Query-Result.
 * Simple datastructure to handle the results of SPARQL-queries.
 * @author ABOROWI
 */
public class QueryResult implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -5362613505888611720L;
	
	private final List<String> variables;
    private final List<List<RDFObject>> data;
    
    public QueryResult(List<String> variables, List<List<RDFObject>> data)
    {
        this.variables = variables;
        this.data = data;
    }

    public List<String> getVariables()
    {
        return variables;
    }

    public List<List<RDFObject>> getData()
    {
        return data;
    }
}
