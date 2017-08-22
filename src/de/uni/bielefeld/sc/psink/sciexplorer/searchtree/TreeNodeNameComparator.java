/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.bielefeld.sc.psink.sciexplorer.searchtree;

import java.util.Comparator;

import org.primefaces.model.TreeNode;

/**
 * Standard tree-node-comparator. Is used to compare tree-nodes by their names.
 * 
 * @author ABOROWI
 */
public class TreeNodeNameComparator implements Comparator<TreeNode> {
	public static final TreeNodeNameComparator INSTANCE = new TreeNodeNameComparator();

	@Override
	public int compare(TreeNode o1, TreeNode o2) {
		return TreeUtility.getNodeName(o1).compareTo(TreeUtility.getNodeName(o2));
	}
}
