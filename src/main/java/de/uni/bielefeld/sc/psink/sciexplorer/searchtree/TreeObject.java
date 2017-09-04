/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.searchtree;

import java.io.Serializable;

/**
 * Abstract tree-object.
 * Is implemented by Relation- and Subclass-class.
 * @author ABOROWI
 */
public abstract class TreeObject implements Serializable
{
    public static boolean isRelation(TreeObject object)
    {
        return object.getClass() == Relation.class;
    }
    
    public static boolean isSubclass(TreeObject object)
    {
        return object.getClass() == Subclass.class;
    }
    
    private boolean selectableVariable;
    private boolean selectableConstraint;
    private int resultCount;

    public boolean isSelectableVariable()
    {
        return selectableVariable;
    }

    public void setSelectableVariable(boolean selectableAsVariable)
    {
        this.selectableVariable = selectableAsVariable;
    }

    public boolean isSelectableConstraint()
    {
        return selectableConstraint;
    }

    public void setSelectableConstraint(boolean selectableAsConstraint)
    {
        this.selectableConstraint = selectableAsConstraint;
    }
    
    public int getResultCount()
    {
        return resultCount;
    }

    public void setResultCount(int resultCount)
    {
        this.resultCount = resultCount;
    }
    
    public boolean isDatatypeProperty()
    {
        return ((Relation)this).isDataTypeProperty();
    }
    
    public boolean isRelation()
    {
        return isRelation(this);
    }
    
    public boolean isSubclass()
    {
        return isSubclass(this);
    }
}
