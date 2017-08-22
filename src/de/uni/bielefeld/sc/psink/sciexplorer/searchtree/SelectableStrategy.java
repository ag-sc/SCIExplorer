/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.bielefeld.sc.psink.sciexplorer.searchtree;

import org.primefaces.model.TreeNode;

/**
 * Interface to define the selectable-strategy.
 * @author ABOROWI
 */
public abstract class SelectableStrategy
{
    public abstract boolean isSelectableVariable(TreeNode node);
    
    public abstract boolean isSelectableConstraint(TreeNode node);
}
