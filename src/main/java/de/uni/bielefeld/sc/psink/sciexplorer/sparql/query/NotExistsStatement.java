/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.query;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import main.java.de.uni.bielefeld.sc.psink.sciexplorer.utility.StringUtility;

/**
 *
 * @author ABOROWI
 */
public class NotExistsStatement extends AbstractStatement implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -1225537189839640066L;
	
	private List<AbstractStatement> statements;

    public NotExistsStatement()
    {
        this.statements = new LinkedList<AbstractStatement>();
    }
    
    public NotExistsStatement(Statement statement)
    {
        this.statements = new LinkedList<AbstractStatement>();
        this.statements.add(statement);
    }
    
    public NotExistsStatement(List<AbstractStatement> statements)
    {
        this.statements = new LinkedList<AbstractStatement>(statements);
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
        StringBuilder builder = new StringBuilder("FILTER NOT EXISTS{");
        builder.append(StringUtility.join(" ", statements));
        builder.append("}");
        return builder.toString();
    }
    
}
