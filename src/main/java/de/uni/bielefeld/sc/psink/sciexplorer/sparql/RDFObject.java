/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql;

/**
 *
 * @author ABOROWI
 */
public class RDFObject
{
    private String value;
    private boolean isResource;
    
    public RDFObject(String value, boolean isResource)
    {
        this.value = value;
        this.isResource = isResource;
    }
    
    public boolean isResource()
    {
        return isResource;
    }
    
    public boolean isLiteral()
    {
        return !isResource();
    }
    
    @Override
    public String toString()
    {
        return value;
    }
    
    public String toStringNoPrefix() {
    	return value.replace("http://psink.de/scio/", "");
    }
}
