/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.query;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author ABOROWI
 */
public class UnionStatement extends AbstractStatement implements Serializable
{   
    /**
	 * 
	 */
	private static final long serialVersionUID = 3231904094044918857L;
	private final List<List<AbstractStatement>> blocks;
    
    public UnionStatement()
    {
        this.blocks = new LinkedList<List<AbstractStatement>>();
    }
    
    public UnionStatement(List<AbstractStatement> block)
    {
        this();
        blocks.add(block);
    }
    
    public UnionStatement(AbstractStatement block)
    {
        this();
        LinkedList<AbstractStatement> statements = new LinkedList<AbstractStatement>();
        statements.add(block);
        blocks.add(statements);
    }
    
    public UnionStatement(String subject, String predicate, String object)
    {
        this(new Statement(subject, predicate, object));
    }
    
    public void addBlock(List<AbstractStatement> block)
    {
        blocks.add(block);
    }
    
    public void addBlock(AbstractStatement statement)
    {
        LinkedList<AbstractStatement> statements = new LinkedList<AbstractStatement>();
        statements.add(statement);
        addBlock(statements);
    }
    
    public void addBlock(String subject, String predicate, String object)
    {
        addBlock(new Statement(subject, predicate, object));
    }

    public List<List<AbstractStatement>> getBlocks()
    {
        return blocks;
    }
    
    /**
     * Generiert einen String aus diesem Statement.
     * Ist keiner oder nur ein Block vorhanden, wird das UNION-Schlüsselwort
     * nicht genutzt.
     * @return 
     */
    @Override
    public String toString()
    {
        if(blocks.isEmpty())
        {
            return "";
        }
        Iterator<List<AbstractStatement>> iterator = blocks.iterator();
        String blockString = generateBlockString(iterator.next());
        if(!iterator.hasNext())
        {
            return blockString;
        }
        StringBuilder builder = new StringBuilder("{");
        builder.append(blockString);
        builder.append("}");
        do
        {
            builder.append(" UNION{");
            builder.append(generateBlockString(iterator.next()));
            builder.append("}");
        }
        while(iterator.hasNext());
        builder.append(".");
        return builder.toString();
    }
    
    private String generateBlockString(List<AbstractStatement> statements)
    {
        StringBuilder builder = new StringBuilder();
        for(AbstractStatement statement : statements)
        {
            builder.append(statement.toString());
            builder.append(" ");
        }
        return builder.toString();
    }
}
