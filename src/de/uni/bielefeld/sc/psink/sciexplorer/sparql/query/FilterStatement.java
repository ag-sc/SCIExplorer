/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.bielefeld.sc.psink.sciexplorer.sparql.query;

import java.io.Serializable;

/**
 *
 * @author ABOROWI
 */
public class FilterStatement extends AbstractStatement implements Serializable
{
    private final String variable;
    private final String expression;
    private final boolean caseSensitive;
    
    public FilterStatement(String variable, String expression)
    {
        this.variable = variable;
        this.expression = expression;
        this.caseSensitive = false;
    }
    
    public FilterStatement(String variable, String expression, boolean caseSensitive)
    {
        this.variable = variable;
        this.expression = expression;
        this.caseSensitive = caseSensitive;
    }
    
    @Override
    public String toString()
    {
        return "FILTER regex(" + variable + ", \"" + expression + "\"" + (caseSensitive ? ", \"i\"" : "") + ")";
    }
    
}
