/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.bielefeld.sc.psink.sciexplorer.sparql;

import java.io.Serializable;
import java.util.List;

/**
 * Query-Result.
 * Simple datastructure to handle the results of SPARQL-queries.
 * @author ABOROWI
 */
public class QueryResult implements Serializable
{
    private final List<String> variables;
    private final List<List<String>> data;
    
    public QueryResult(List<String> variables, List<List<String>> data)
    {
        this.variables = variables;
        this.data = data;
    }

    public List<String> getVariables()
    {
        return variables;
    }

    public List<List<String>> getData()
    {
        return data;
    }
}
