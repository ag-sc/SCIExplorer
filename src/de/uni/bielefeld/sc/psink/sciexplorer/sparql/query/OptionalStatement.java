/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.bielefeld.sc.psink.sciexplorer.sparql.query;

import java.util.LinkedList;
import java.util.List;

import de.uni.bielefeld.sc.psink.sciexplorer.utility.StringUtility;

/**
 *
 * @author ABOROWI
 */
public class OptionalStatement extends AbstractStatement
{
    private List<AbstractStatement> statements;

    public OptionalStatement()
    {
        this.statements = new LinkedList();
    }
    
    public OptionalStatement(Statement statement)
    {
        this.statements = new LinkedList();
        this.statements.add(statement);
    }
    
    public OptionalStatement(String subject, String predicate, String object)
    {
        this.statements = new LinkedList();
        this.statements.add(new Statement(subject, predicate, object));
    }
    
    public OptionalStatement(List<AbstractStatement> statements)
    {
        this.statements = new LinkedList(statements);
    }
    
    public void addStatement(String subject, String predicate, String object)
    {
        statements.add(new Statement(subject, predicate, object));
    }
    
    public void addStatement(AbstractStatement statement)
    {
        statements.add(statement);
    }
    
    public void addStatements(List<AbstractStatement> statements)
    {
        statements.addAll(statements);
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("OPTIONAL{");
        builder.append(StringUtility.join(" ", statements));
        builder.append("}");
        return builder.toString();
    }
}
