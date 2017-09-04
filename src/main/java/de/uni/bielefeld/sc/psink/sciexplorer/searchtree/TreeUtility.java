/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.searchtree;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 * Tree-utility.
 * Provides a lot of usful functions to work with tree-nodes.
 * @author ABOROWI
 */
public class TreeUtility
{
    public static boolean isRelation(TreeNode node)
    {
        return getTreeObject(node).getClass() == Relation.class;
    }
    
    public static boolean isSubclass(TreeNode node)
    {
        return getTreeObject(node).getClass() == Subclass.class;
    }
    
    public static boolean hasSubclassChildren(TreeNode node)
    {
        for(TreeNode child : node.getChildren())
        {
            if(isSubclass(child))
            {
                return true;
            }
        }
        return false;
    }
    
    public static Relation getRelation(TreeNode node)
    {
        return (Relation)node.getData();
    }
    
    public static Subclass getSubclass(TreeNode node)
    {
        return (Subclass)node.getData();
    }
    
    public static boolean isRoot(TreeNode node)
    {
        return node.getParent() == null;
    }
    
    public static List<TreeNode> getDescendants(TreeNode node)
    {
        List<TreeNode> descendants = new LinkedList();
        getDescendants(descendants, node);
        return descendants;
    }
    
    public static void getDescendants(List<TreeNode> descendants, TreeNode node)
    {
        for(TreeNode child : node.getChildren())
        {
            descendants.add(child);
            getDescendants(descendants, child);
        }
    }
    
    public static List<TreeNode> getDirectSubclassDescendants(TreeNode node)
    {
        List<TreeNode> descendants = new LinkedList();
        getDirectSubclassDescendants(descendants, node);
        return descendants;
    }
    
    public static void getDirectSubclassDescendants(List<TreeNode> descendants, TreeNode node)
    {
        for(TreeNode child : node.getChildren())
        {
            if(isSubclass(child))
            {
                descendants.add(child);
                getDirectSubclassDescendants(descendants, child);
            }
        }
    }
    
    public static List<TreeNode> getNearestRelationDescendants(TreeNode node)
    {
        List<TreeNode> descendants = new LinkedList();
        TreeUtility.getNearestRelationDescendants(descendants, node);
        return descendants;
    }
    
    public static void getNearestRelationDescendants(List<TreeNode> descendants, TreeNode node)
    {
        for(TreeNode child : node.getChildren())
        {
            if(isRelation(child))
            {
                descendants.add(child);
            }
            else
            {
                TreeUtility.getNearestRelationDescendants(descendants, child);
            }
        }
    }
    
    public static TreeObject getTreeObject(TreeNode node)
    {
        return ((TreeObject)node.getData());
    }
    
    public static int getResultCount(TreeNode node)
    {
        return getTreeObject(node).getResultCount();
    }
    
    public static List<TreeNode> getLeaves(TreeNode node)
    {
        List<TreeNode> leaves = new LinkedList();
        getLeaves(leaves, node);
        return leaves;
    }
    
    public static void getLeaves(List<TreeNode> leaves, TreeNode node)
    {
        for(TreeNode child : node.getChildren())
        {
            if(child.isLeaf())
            {
                leaves.add(child);
            }
            else
            {
                getLeaves(leaves, child);
            }
        }
    }
    
    public static List<TreeNode> getDirectSubclassLeaves(TreeNode node)
    {
        List<TreeNode> leaves = new LinkedList();
        getDirectSubclassLeaves(leaves, node);
        return leaves;
    }
    
    public static void getDirectSubclassLeaves(List<TreeNode> leaves, TreeNode node)
    {
        for(TreeNode child : node.getChildren())
        {
            if(isSubclass(child))
            {
                if(child.isLeaf())
                {
                    leaves.add(child);
                }
                else
                {
                    getDirectSubclassLeaves(leaves, child);
                }
            }
        }
    }
    
    public static TreeNode getRoot(Collection<TreeNode> nodes)
    {
        for(TreeNode node : nodes)
        {
            if(isRoot(node))
            {
                return node;
            }
        }
        return null;
    }

    public static List<TreeNode> getSubclassChildren(TreeNode node)
    {
        List<TreeNode> subclasses = new LinkedList();
        getSubclassChildren(subclasses, node);
        return subclasses;
    }
    
    public static void getSubclassChildren(List<TreeNode> children, TreeNode node)
    {
        for(TreeNode child : node.getChildren())
        {
            if(isSubclass(child))
            {
                children.add(child);
            }
        }
    }
    
    public static TreeNode getSuperiorRelation(TreeNode node)
    {
        if(isRoot(node))
        {
            return node;
        }
        if(TreeUtility.isRelation(node))
        {
            return node;
        }
        return getSuperiorRelation(node.getParent());
    }
    
    public static boolean isRelationNodeDatatypeProperty(TreeNode node)
    {
        return getRelation(node).isDataTypeProperty();
    }
    
    public static boolean isDatatypeProperty(TreeNode node)
    {
        if(isRelation(node))
        {
            return isRelationNodeDatatypeProperty(node);
        }
        return false;
    }
    
    public static List<TreeNode> getContainedNodes(Collection<TreeNode> referenceNodes, Collection<TreeNode> nodes)
    {
        List<TreeNode> containedNodes = new LinkedList();
        for(TreeNode node : nodes)
        {
            if(referenceNodes.contains(node))
            {
                containedNodes.add(node);
            }
        }
        return containedNodes;
    }
    
    public static boolean hasRelationParent(TreeNode node)
    {
        if(isRoot(node))
        {
            return false;
        }
        return isRelation(node.getParent());
    }
    
    public static boolean isSubclassNodeDefaultSubclass(TreeNode node)
    {
        return getSubclass(node).isDefaultSubclass();
    }
    
    public static void collapseAll(TreeNode node)
    {
        node.setExpanded(false);
        for(TreeNode child : node.getChildren())
        {
            collapseAll(child);
        }
    }
    
    public static void expand(TreeNode node)
    {
        node.setExpanded(true);
        if(!isRoot(node))
        {
            expand(node.getParent());
        }
    }
    
    public static void expand(Collection<TreeNode> nodes)
    {
        for(TreeNode node : nodes)
        {
            expand(node);
        }
    }
    
    public static void expandParent(TreeNode node)
    {
        if(!TreeUtility.isRoot(node))
        {
            expand(node.getParent());
        }
    }
    
    public static void expandParents(Collection<TreeNode> nodes)
    {
        for(TreeNode node : nodes)
        {
            expandParent(node);
        }
    }
    
    public static boolean isSubclassNodeNamedIndividual(TreeNode subclassNode)
    {
        return getSubclass(subclassNode).isNamedIndividual();
    }
    
    public static void deselectAll(TreeNode node)
    {
        node.setSelected(false);
        for(TreeNode child : node.getChildren())
        {
            deselectAll(child);
        }
    }
    
    /**
     * Liefert den Namen des Knoten.
     * @param node
     * @return 
     */
    public static String getNodeName(TreeNode node)
    {
        if(isRelation(node))
        {
            return getRelation(node).getMergedName();
        }
        else if(isSubclass(node))
        {
            return getSubclass(node).getName();
        }
        return null;
    }
    
    public static TreeNode getNodeByPath(TreeNode node, String ... path)
    {
        if(path.length == 0)
        {
            return null;
        }
        TreeNode currentNode = node;
        boolean segmentFound;
        for(String pathSegment : path)
        {
            segmentFound = false;
            for(TreeNode child : currentNode.getChildren())
            {
                if(getNodeName(child).equals(pathSegment))
                {
                    currentNode = child;
                    segmentFound = true;
                    break;
                }
            }
            if(!segmentFound)
            {
                return null;
            }
        }
        return currentNode;
    }
    
    public static TreeNode getNodeByPath(TreeNode node, Iterable<String> path)
    {
        if(!path.iterator().hasNext())
        {
            return null;
        }
        TreeNode currentNode = node;
        boolean segmentFound;
        for(String pathSegment : path)
        {
            segmentFound = false;
            for(TreeNode child : currentNode.getChildren())
            {
                if(getNodeName(child).equals(pathSegment))
                {
                    currentNode = child;
                    segmentFound = true;
                    break;
                }
            }
            if(!segmentFound)
            {
                return null;
            }
        }
        return currentNode;
    }
    
    public static TreeNode getNodeByIndexPath(TreeNode node, Integer[] indexPath)
    {
        if(indexPath.length == 0)
        {
            return null;
        }
        TreeNode currentNode = node;
        for(int segmentIndex : indexPath)
        {
            if(currentNode.getChildren().size() < segmentIndex)
            {
                return null;
            }
            currentNode = currentNode.getChildren().get(segmentIndex);
        }
        return currentNode;
    }
    
    /*
    public static TreeNode getNodeByIndexPath(TreeNode node, Iterable<Integer> indexPath)
    {
        if(!indexPath.iterator().hasNext())
        {
            return null;
        }
        TreeNode currentNode = node;
        for(int segmentIndex : indexPath)
        {
            if(currentNode.getChildren().size() < segmentIndex)
            {
                return null;
            }
            currentNode = currentNode.getChildren().get(segmentIndex);
        }
        return currentNode;
    }
    */
    
    public static TreeNode getNodeByIndexPath(TreeNode node, Iterable<Long> indexPath)
    {
        if(!indexPath.iterator().hasNext())
        {
            return null;
        }
        TreeNode currentNode = node;
        for(long segmentIndex : indexPath)
        {
            if(currentNode.getChildren().size() < segmentIndex)
            {
                return null;
            }
            currentNode = currentNode.getChildren().get((int)segmentIndex);
        }
        return currentNode;
    }
    
    public static String[] getPath(TreeNode node)
    {
        LinkedList<String> segments = new LinkedList();
        TreeNode currentNode = node;
        while(currentNode != null)
        {
            if(isRoot(currentNode))
            {
                break;
            }
            segments.addFirst(getNodeName(currentNode));
            currentNode = currentNode.getParent();
        }
        return segments.toArray(new String[segments.size()]);
    }
    
    public static Integer[] getIndexPath(TreeNode node)
    {
        LinkedList<Integer> segmentIndices = new LinkedList();
        TreeNode currentNode = node;
        while(currentNode != null)
        {
            if(isRoot(currentNode))
            {
                break;
            }
            segmentIndices.addFirst(indexOfNode(currentNode.getParent().getChildren(), currentNode));
            currentNode = currentNode.getParent();
        }
        return segmentIndices.toArray(new Integer[segmentIndices.size()]);
    }
    
    private static int indexOfNode(List<TreeNode> list, TreeNode node)
    {
        Iterator<TreeNode> iterator = list.iterator();
        for(int counter = 0; iterator.hasNext(); counter++)
        {
            if(iterator.next().equals(node))
            {
                return counter;
            }
        }
        return -1;
    }

    public static void connectParentAndChild(TreeNode parent, TreeNode child) {
        child.setParent(parent);
        parent.getChildren().add(child);
    }
    
    public static TreeNode cloneNode(TreeNode node)
    {
        return new DefaultTreeNode(node.getData());
    }
    
    public static TreeNode cloneTree(TreeNode source)
    {
        TreeNode node = new DefaultTreeNode(source.getData());
        cloneTree(source, node);
        return node;
    }
    
    public static void cloneTree(TreeNode source, TreeNode target)
    {
        TreeNode node;
        for(TreeNode child : source.getChildren())
        {
            node = new DefaultTreeNode(child.getData());
            connectParentAndChild(target, node);
            cloneTree(child, node);
        }
    }
    
    public static void sortByName(List<TreeNode> nodes)
    {
        Collections.sort(nodes, TreeNodeNameComparator.INSTANCE);
    }
}
