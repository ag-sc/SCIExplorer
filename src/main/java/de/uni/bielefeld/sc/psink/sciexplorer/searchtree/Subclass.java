/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.searchtree;

/**
 * (Sub-)class datastructure
 * @author ABOROWI
 */
public class Subclass extends TreeObject
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1425072832766009234L;

	public static final boolean DEFAULT_SUBCLASS = true;
    
    private final String name;
    private final boolean namedIndividual;
    private final boolean defaultSubclass;
    private int subclassResultCount;
    
    public Subclass(String name)
    {
        this(name, false);
    }
    
    public Subclass(String name, boolean namedIndividual)
    {
        this(name, namedIndividual, false);
    }
    
    public Subclass(String name, boolean namedIndividual, boolean defaultSubclass)
    {
        this.name = name;
        this.namedIndividual = namedIndividual;
        this.defaultSubclass = defaultSubclass;
        this.subclassResultCount = -1;
    }

    public String getName()
    {
        return name;
    }
    
    public boolean isDefaultSubclass()
    {
        return defaultSubclass;
    }
    
    public boolean isNamedIndividual()
    {
        return namedIndividual;
    }

    public int getSubclassResultCount()
    {
        return subclassResultCount;
    }

    public void setSubclassResultCount(int subclassResultCount)
    {
        this.subclassResultCount = subclassResultCount;
    }
}
