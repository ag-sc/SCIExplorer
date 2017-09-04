/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.query;

import java.io.Serializable;

/**
 * Standard SPARQL-statement.
 * Consisting of subject, predicate and object.
 * @author ABOROWI
 */
public class Statement extends AbstractStatement implements Serializable
{   
    private final String subject;
    private final String predicate;
    private final String object;
    
    public Statement(String subject, String predicate, String object)
    {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public String getSubject()
    {
        return subject;
    }

    public String getPredicate()
    {
        return predicate;
    }

    public String getObject()
    {
        return object;
    }

    @Override
    public String toString()
    {
        return subject + " " + predicate + " " + object + ".";
    }
}
