/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.primefaces.model.TreeNode;

/**
 * Variable-manager.
 * Is used to generate SPARQL-Statements and queries.
 * @author ABOROWI
 */
public class VariableManager
{
    private static Map<String,AtomicInteger> cloneOccurrenceMap(Map<String,AtomicInteger> occurrenceMap)
    {
        Map<String,AtomicInteger> clonedOccurrenceMap = new LinkedHashMap<String, AtomicInteger>();
        for(Entry<String,AtomicInteger> entry : occurrenceMap.entrySet())
        {
            clonedOccurrenceMap.put(entry.getKey(), new AtomicInteger(entry.getValue().get()));
        }
        return clonedOccurrenceMap;
    }
    
    private final Map<TreeNode,String> variableMap;
    private final Map<String,TreeNode> nodeMap;
    private final Map<String,AtomicInteger> occurrenceMap;
    private final Map<TreeNode,String> typeVariableMap;
    private final Map<String,AtomicInteger> typeOccurrenceMap;
    private final Map<TreeNode,String> valueVariableMap;
    private final Map<String,AtomicInteger> valueOccurrenceMap;
    
    public VariableManager()
    {
        variableMap = new LinkedHashMap<TreeNode, String>();
        nodeMap = new LinkedHashMap<String, TreeNode>();
        occurrenceMap = new LinkedHashMap<String, AtomicInteger>();
        typeVariableMap = new LinkedHashMap<TreeNode, String>();
        typeOccurrenceMap = new LinkedHashMap<String, AtomicInteger>();
        valueVariableMap = new LinkedHashMap<TreeNode, String>();
        valueOccurrenceMap = new LinkedHashMap<String, AtomicInteger>();
    }
    
    
    public VariableManager(VariableManager manager)
    {
        variableMap = new LinkedHashMap<TreeNode, String>(manager.getVariableMap());
        nodeMap = new LinkedHashMap<String, TreeNode>(manager.getNodeMap());
        occurrenceMap = cloneOccurrenceMap(manager.getOccurrenceMap());
        typeVariableMap = new LinkedHashMap<TreeNode, String>(manager.getTypeVariableMap());
        typeOccurrenceMap = cloneOccurrenceMap(manager.getTypeOccurrenceMap());
        valueVariableMap = new LinkedHashMap<TreeNode, String>(manager.getValueVariableMap());
        valueOccurrenceMap = cloneOccurrenceMap(manager.getValueOccurrenceMap());
    }

    public Map<TreeNode, String> getVariableMap()
    {
        return variableMap;
    }

    public Map<String, TreeNode> getNodeMap()
    {
        return nodeMap;
    }

    public Map<String, AtomicInteger> getOccurrenceMap()
    {
        return occurrenceMap;
    }

    public Map<TreeNode, String> getTypeVariableMap()
    {
        return typeVariableMap;
    }

    public Map<String, AtomicInteger> getTypeOccurrenceMap()
    {
        return typeOccurrenceMap;
    }

    public Map<TreeNode, String> getValueVariableMap()
    {
        return valueVariableMap;
    }

    public Map<String, AtomicInteger> getValueOccurrenceMap()
    {
        return valueOccurrenceMap;
    }
    
    /**
     * Reserviert die gewünschte Variable für den spezifizierten Knoten.
     * Wenn die Variable bereits existiert wird eine Zahl angehangen und die 
     * Variable zurückgegeben.
     * @param desiredVariable
     * @param node
     * @return 
     */
    public String generateVariable(String desiredVariable, TreeNode node)
    {
        if(occurrenceMap.containsKey(desiredVariable))
        {
            String variable = desiredVariable + occurrenceMap.get(desiredVariable).getAndIncrement();
            variableMap.put(node, variable);
            nodeMap.put(variable, node);
            //System.out.println("VariableManager: variable '" + variable + "' created! " + variableMap.size() + " variables overall.");
            return variable;
        }
        occurrenceMap.put(desiredVariable, new AtomicInteger(1));
        variableMap.put(node, desiredVariable);
        nodeMap.put(desiredVariable, node);
        return desiredVariable;
    }
    
    public String generateTypeVariable(String desiredTypeVariable, TreeNode node)
    {
        if(typeOccurrenceMap.containsKey(desiredTypeVariable))
        {
            String variable = desiredTypeVariable + typeOccurrenceMap.get(desiredTypeVariable).getAndIncrement();
            return variable;
        }
        typeOccurrenceMap.put(desiredTypeVariable, new AtomicInteger(1));
        typeVariableMap.put(node, desiredTypeVariable);
        return desiredTypeVariable;
    }
    
    public String generateValueVariable(String desiredValueVariable, TreeNode node)
    {
        if(valueOccurrenceMap.containsKey(desiredValueVariable))
        {
            String variable = desiredValueVariable + valueOccurrenceMap.get(desiredValueVariable).getAndIncrement();
            return variable;
        }
        valueOccurrenceMap.put(desiredValueVariable, new AtomicInteger(1));
        valueVariableMap.put(node, desiredValueVariable);
        return desiredValueVariable;
    }
    
    public String getVariable(TreeNode node)
    {
        return variableMap.get(node);
    }
    
    public boolean hasVariable(TreeNode node)
    {
        return variableMap.containsKey(node);
    }
    
    public boolean hasVariable(String variable)
    {
        return occurrenceMap.containsKey(variable);
    }
    
    public TreeNode getNode(String variable)
    {
        return nodeMap.get(variable);
    }
    
    public Set<String> getVariables()
    {
        return occurrenceMap.keySet();
    }
    
    public String getTypeVariable(TreeNode node)
    {
        return typeVariableMap.get(node);
    }
    
    public boolean hasTypeVariable(TreeNode node)
    {
        return typeVariableMap.containsKey(node);
    }
    
    public Set<String> getTypeVariables()
    {
        return typeOccurrenceMap.keySet();
    }
    
    public String getValueVariable(TreeNode node)
    {
        return valueVariableMap.get(node);
    }
    
    public boolean hasValueVariable(TreeNode node)
    {
        return valueVariableMap.containsKey(node);
    }
    
    public Set<String> getValueVariables()
    {
        return valueOccurrenceMap.keySet();
    }
}
