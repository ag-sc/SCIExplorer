/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.primefaces.model.TreeNode;

import main.java.de.uni.bielefeld.sc.psink.sciexplorer.Configuration;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.searchtree.Relation;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.searchtree.Subclass;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.searchtree.TreeUtility;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.query.AbstractStatement;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.query.FilterStatement;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.query.MinusStatement;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.query.Statement;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.query.UnionStatement;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.utility.StringUtility;

/**
 * SPARQL-query generator. Generates SPARQL-statements and whole SPARQL-queries.
 * 
 * @author ABOROWI
 */
public class QueryGenerator {
	public static final boolean LEAVES_INCLUDE = true;
	public static final boolean LEAVES_IGNORE = false;
	public static final boolean TYPEVARIABLES_INCLUDE = true;
	public static final boolean TYPEVARIABLES_IGNORE = false;
	public static final String PREDICATE_TYPE_VALUE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	public static final String PREDICATE_TYPE = "<" + PREDICATE_TYPE_VALUE + ">";

	public static final String ROOT_VARIABLE = prepareVariable(Configuration.SPARQL_ROOT_VARIABLENAME);
	private static final Statement ROOT_STATEMENT = new Statement(ROOT_VARIABLE, PREDICATE_TYPE,
			prepareValue(Configuration.SPARQL_ROOT_VARIABLENAME));
	public static final String ORDER_BY_ROOTVARIABLE = "ORDER BY " + ROOT_VARIABLE;

	public static String prepareValue(String value) {
		return "<" + Configuration.RDF_URI_PREFIX_ONTOLOGY + value + ">";
	}

	private static String prepareVariable(String variable) {
		return "?" + variable;
	}

	public static String generateConstructQuery(String what, String where) {
		return "CONSTRUCT {" + what + "} WHERE {" + where + "}";
	}

	public static String generateSelectCountQuery(String where) {
		return generateSelectQuery("(COUNT(*) AS ?count)", where, null);
	}

	public static String generateSelectQuery(String variables, String where) {
		return generateSelectQuery(variables, where, null);
	}

	public static String generateSelectQuery(String variables, String where, String suffix) {
		return "SELECT " + variables + " WHERE {" + where + "}" + (suffix != null ? " " + suffix : "");
	}

	public static String generateTableVariableString(VariableManager variableManager) {
		return StringUtility.join(" ", ROOT_VARIABLE, StringUtility.join(" ", variableManager.getTypeVariables()),
				StringUtility.join(" ", variableManager.getValueVariables()));
	}

	public static AbstractStatement generateSpecificValueStatement(String variable, String value) {
		return new FilterStatement(variable, value);
	}

	public static String generateStatementBlockString(TreeNode root, List<TreeNode> constraints,
			Map<TreeNode, String> filters, boolean includeTypeVariables) {
		return generateStatementString(QueryGenerator.generateStatements(root, new HashSet<TreeNode>(constraints), filters,
				new VariableManager(), includeTypeVariables));
	}

	/**
	 * Generates statements for the specified (sub-)class-node.
	 * 
	 * @param subclassNode
	 * @param includeLeaves
	 * @return
	 */
	public static String generateSubclassStatementBlockString(TreeNode subclassNode, boolean includeLeaves) {
		List<AbstractStatement> statements = new LinkedList<AbstractStatement>();
		buildSubclassVariableBranch(new VariableManager(), statements, subclassNode, includeLeaves);
		return generateStatementString(statements);
	}

	/**
	 * Generates statements for the specified (sub-)class-node. If nodes on the
	 * way to the root-node are already visited, connection-statements are
	 * added.
	 * 
	 * @param variableManager
	 * @param statements
	 * @param subclassNode
	 * @param includeLeaves
	 */
	public static void buildSubclassVariableBranch(VariableManager variableManager, List<AbstractStatement> statements,
			TreeNode subclassNode, boolean includeLeaves) {
		String parentVariable = attachBranch(variableManager, statements, subclassNode);
		if (!includeLeaves || subclassNode.isLeaf()) {
			statements.add(generateSubclassStatement(variableManager, parentVariable, subclassNode));
		} else {
			List<TreeNode> nodes = TreeUtility.getDirectSubclassDescendants(subclassNode);
			nodes.add(subclassNode);
			UnionStatement union = new UnionStatement();
			for (TreeNode node : nodes) {
				union.addBlock(generateSubclassStatement(variableManager, parentVariable, node));
			}
			statements.add(union);
		}
	}

	/**
	 * Generates statements for the specified node. If nodes on the way to the
	 * root-node are already visited, connection-statements are added.
	 * 
	 * @param variableManager
	 * @param statements
	 * @param node
	 * @param filterMap
	 * @param includeLeaves
	 */
	public static void buildNodeBranch(VariableManager variableManager, List<AbstractStatement> statements,
			TreeNode node, boolean includeLeaves, Map<TreeNode, String> filterMap) {
		String parentVariable = attachBranch(variableManager, statements, node);
		if (TreeUtility.isSubclass(node)) {
			if (!includeLeaves || node.isLeaf()) {
				statements.add(generateSubclassStatement(variableManager, parentVariable, node));
			} else {
				List<TreeNode> directSubclassDescendants = TreeUtility.getDirectSubclassDescendants(node);
				directSubclassDescendants.add(node);
				UnionStatement union = new UnionStatement();
				for (TreeNode directSubclassDescendant : directSubclassDescendants) {
					union.addBlock(
							generateSubclassStatement(variableManager, parentVariable, directSubclassDescendant));
				}
				statements.add(union);
			}
		} else {
			if (filterMap.containsKey(node)) {
				statements.add(new FilterStatement(variableManager.getValueVariable(node), filterMap.get(node), true));
			}
		}
	}

	/**
	 * Attaches the specified node and generates (connection-)statements.
	 * 
	 * @param variableManager
	 * @param statements
	 * @param node
	 * @return
	 */
	public static String attachBranch(VariableManager variableManager, List<AbstractStatement> statements,
			TreeNode node) {
		if (TreeUtility.isRoot(node)) {
			if (!variableManager.hasVariable(ROOT_VARIABLE)) {
				variableManager.generateVariable(ROOT_VARIABLE, node);
				statements.add(ROOT_STATEMENT);
			}
			return ROOT_VARIABLE;
		}
		if (TreeUtility.isRelation(node)) {
			if (variableManager.hasVariable(node)) {
				return variableManager.getVariable(node);
			}
			String parentVariable = attachBranch(variableManager, statements, node.getParent());
			return generateAndAddRelationStatement(new HashMap<TreeNode, String>(), variableManager, parentVariable, statements, node);
		}
		return attachBranch(variableManager, statements, node.getParent());
	}

	private static String generateAndAddRelationStatement(Map<TreeNode, String> filterMap,
			VariableManager variableManager, String parentVariable, List<AbstractStatement> statements, TreeNode node) {
		Relation relation = TreeUtility.getRelation(node);
		String variable = generateVariable(variableManager, relation, node);
		String predicate = prepareValue(relation.getRelation());
		statements.add(new Statement(parentVariable, predicate, variable));
		if (relation.isDataTypeProperty()) {
			String valueVariable = variableManager.generateValueVariable(getValueVariable(variable), node);
			statements.add(new Statement(parentVariable, predicate, valueVariable));
			if (filterMap.containsKey(node)) {
				statements.add(new FilterStatement(valueVariable, filterMap.get(node), true));
			}
		}
		return variable;
	}

	public static String generateStatementBlockString(TreeNode root, TreeNode node, boolean includeTypeVariables) {
		return generateStatementString(QueryGenerator.buildStatementBlock(root, node, includeTypeVariables));
	}

	private static List<AbstractStatement> buildStatementBlock(TreeNode root, TreeNode node,
			boolean includeTypeVariables) {
		Set<TreeNode> selectedNodes = new HashSet<TreeNode>();
		selectedNodes.add(node);
		return generateStatements(root, selectedNodes, new HashMap<TreeNode, String>(), new VariableManager(), includeTypeVariables);
	}

	/**
	 * Generates statements.
	 * 
	 * @param root
	 * @param nodes
	 * @param filterMap
	 * @param variableManager
	 * @param includeTypeVariables
	 * @return
	 */
	public static List<AbstractStatement> generateStatements(TreeNode root, Set<TreeNode> nodes,
			Map<TreeNode, String> filterMap, VariableManager variableManager, boolean includeTypeVariables) {
		List<AbstractStatement> statements = new LinkedList<AbstractStatement>();
		generateStatements(statements, root, nodes, filterMap, variableManager, includeTypeVariables);
		return statements;
	}

	/**
	 * Generates statements.
	 * 
	 * @param statements
	 * @param root
	 * @param nodes
	 * @param filterMap
	 * @param variableManager
	 * @param includeTypeVariables
	 */
	public static void generateStatements(List<AbstractStatement> statements, TreeNode root, Set<TreeNode> nodes,
			Map<TreeNode, String> filterMap, VariableManager variableManager, boolean includeTypeVariables) {
		statements.add(ROOT_STATEMENT);
		variableManager.generateVariable(ROOT_VARIABLE, root);
		if (nodes.isEmpty()) {
			return;
		}
		Set<TreeNode> pathNodes = getPathNodes(nodes);
		generateStatements(nodes, pathNodes, filterMap, variableManager, statements, root, includeTypeVariables);
	}

	private static void generateStatements(Set<TreeNode> selectedNodes, Set<TreeNode> pathNodes,
			Map<TreeNode, String> filterMap, VariableManager variableManager, List<AbstractStatement> statements,
			TreeNode parent, boolean includeTypeVariables) {
		List<TreeNode> relevantSubclasses = new LinkedList<TreeNode>();
		for (TreeNode child : parent.getChildren()) {
			if (pathNodes.contains(child)) {
				if (TreeUtility.isRelation(child)) {
					generateAndAddRelationStatement(filterMap, variableManager, variableManager.getVariable(parent),
							statements, child);
					generateStatements(selectedNodes, pathNodes, filterMap, variableManager, statements, child,
							includeTypeVariables);
				} else {
					getRelevantSubclasses(selectedNodes, pathNodes, child, relevantSubclasses);
				}
			}
		}
		if (relevantSubclasses.size() > 0) {
			generateUnionStatement(selectedNodes, pathNodes, filterMap, variableManager, statements, parent,
					relevantSubclasses, includeTypeVariables);
		}
	}

	private static void getRelevantSubclasses(Set<TreeNode> selectedNodes, Set<TreeNode> pathNodes,
			TreeNode subclassNode, List<TreeNode> relevantSubclasses) {
		boolean isSelected = selectedNodes.contains(subclassNode);
		boolean hasPathRelationChildren = false;
		for (TreeNode child : subclassNode.getChildren()) {
			if (pathNodes.contains(child)) {
				if (TreeUtility.isRelation(child)) {
					hasPathRelationChildren = true;
				} else {
					getRelevantSubclasses(selectedNodes, pathNodes, child, relevantSubclasses);
				}
			}
		}
		if (isSelected
				|| (hasPathRelationChildren && !hasSelectedDirectSubclassDescendants(selectedNodes, subclassNode))) {
			relevantSubclasses.add(subclassNode);
		}
	}

	private static boolean hasSelectedDirectSubclassDescendants(Set<TreeNode> selectedNodes, TreeNode node) {
		for (TreeNode descendant : TreeUtility.getDirectSubclassDescendants(node)) {
			if (selectedNodes.contains(descendant)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Generates UNION-statements for the specified (sub-)class-nodes.
	 * 
	 * @param selectedNodes
	 * @param pathNodes
	 * @param filterMap
	 * @param variableManager
	 * @param statements
	 * @param relationParentNode
	 * @param subclassNodes
	 * @param includeTypeVariables
	 */
	private static void generateUnionStatement(Set<TreeNode> selectedNodes, Set<TreeNode> pathNodes,
			Map<TreeNode, String> filterMap, VariableManager variableManager, List<AbstractStatement> statements,
			TreeNode relationParentNode, List<TreeNode> subclassNodes, boolean includeTypeVariables) {
		String relationParentVariable = variableManager.getVariable(relationParentNode);
		UnionStatement unionStatement = new UnionStatement();
		List<AbstractStatement> block;
		VariableManager branchVariableManager;
		for (TreeNode subclassNode : subclassNodes) {
			block = new LinkedList<AbstractStatement>();
			branchVariableManager = new VariableManager(variableManager);
			if (selectedNodes.contains(subclassNode)) {
				block.add(generateSubclassStatement(branchVariableManager, relationParentVariable, subclassNode));
			}
			generateInheritedStatements(selectedNodes, pathNodes, filterMap, branchVariableManager, block,
					subclassNode.getParent(), relationParentVariable, includeTypeVariables);
			generateSubclassRelationStatements(selectedNodes, pathNodes, filterMap, variableManager, block,
					subclassNode, relationParentVariable, includeTypeVariables);
			unionStatement.addBlock(block);
		}
		statements.add(unionStatement);
		if (includeTypeVariables) {
			generateTypeVariableStatement(variableManager, relationParentNode, statements);
		}
	}

	/**
	 * Generates statements for inherited properties.
	 * 
	 * @param selectedNodes
	 * @param pathNodes
	 * @param filterMap
	 * @param variableManager
	 * @param statements
	 * @param node
	 * @param relationParentVariable
	 * @param includeTypeVariables
	 */
	private static void generateInheritedStatements(Set<TreeNode> selectedNodes, Set<TreeNode> pathNodes,
			Map<TreeNode, String> filterMap, VariableManager variableManager, List<AbstractStatement> statements,
			TreeNode node, String relationParentVariable, boolean includeTypeVariables) {
		if (TreeUtility.isRelation(node)) {
			return;
		}
		generateInheritedStatements(selectedNodes, pathNodes, filterMap, variableManager, statements, node.getParent(),
				relationParentVariable, includeTypeVariables);
		generateSubclassRelationStatements(selectedNodes, pathNodes, filterMap, variableManager, statements, node,
				relationParentVariable, includeTypeVariables);
	}

	private static void generateSubclassRelationStatements(Set<TreeNode> selectedNodes, Set<TreeNode> pathNodes,
			Map<TreeNode, String> filterMap, VariableManager variableManager, List<AbstractStatement> statements,
			TreeNode subclassNode, String relationParentVariable, boolean includeTypeVariables) {
		for (TreeNode child : subclassNode.getChildren()) {
			if (pathNodes.contains(child) && TreeUtility.isRelation(child)) {
				generateAndAddRelationStatement(filterMap, variableManager, relationParentVariable, statements, child);
				generateStatements(selectedNodes, pathNodes, filterMap, variableManager, statements, child,
						includeTypeVariables);
			}
		}
	}

	/**
	 * Generates type-variable-statement for tables.
	 * 
	 * @param variableManager
	 * @param relationParentNode
	 * @param statements
	 */
	private static void generateTypeVariableStatement(VariableManager variableManager, TreeNode relationParentNode,
			List<AbstractStatement> statements) {
		String parentVariable = variableManager.getVariable(relationParentNode);
		String typeVariable = variableManager.generateTypeVariable(getTypeVariable(parentVariable), relationParentNode);
		UnionStatement typeUnionStatement = new UnionStatement();
		List<AbstractStatement> block = new LinkedList<AbstractStatement>();
		String superiorRelationParentVariable = variableManager
				.getVariable(TreeUtility.getSuperiorRelation(relationParentNode.getParent()));
		block.add(new Statement(superiorRelationParentVariable,
				prepareValue(TreeUtility.getRelation(relationParentNode).getRelation()), typeVariable));
		block.add(new MinusStatement(typeVariable, PREDICATE_TYPE, "?anything"));
		typeUnionStatement.addBlock(block);
		typeUnionStatement.addBlock(parentVariable, PREDICATE_TYPE, typeVariable);
		statements.add(typeUnionStatement);
	}

	private static String getTypeVariable(String variable) {
		return variable + "Type";
	}

	private static AbstractStatement generateSubclassStatement(VariableManager variableManager,
			String relationParentVariable, TreeNode subclassNode) {
		Subclass subclass = TreeUtility.getSubclass(subclassNode);
		if (subclass.isNamedIndividual()) {
			TreeNode parent = variableManager.getNode(relationParentVariable);
			String superiorRelationParentVariable = variableManager
					.getVariable(TreeUtility.getSuperiorRelation(parent.getParent()));
			return new Statement(superiorRelationParentVariable,
					prepareValue(TreeUtility.getRelation(parent).getRelation()), prepareValue(subclass.getName()));
		}
		return new Statement(relationParentVariable, PREDICATE_TYPE, prepareValue(subclass.getName()));
	}

	public static String generateStatementString(List<AbstractStatement> statements) {
		return StringUtility.join(" ", statements);
	}

	private static Set<TreeNode> getPathNodes(Collection<TreeNode> nodes) {
		Set<TreeNode> pathNodes = new HashSet<TreeNode>();
		for (TreeNode node : nodes) {
			getPathNodes(pathNodes, node);
		}
		return pathNodes;
	}

	private static void getPathNodes(Set<TreeNode> pathNodes, TreeNode node) {
		pathNodes.add(node);
		if (!TreeUtility.isRoot(node)) {
			getPathNodes(pathNodes, node.getParent());
		}
	}

	private static String generateVariable(VariableManager variableManager, Relation relation, TreeNode node) {
		return variableManager.generateVariable(prepareVariable(relation.getMergedName()), node);
	}

	private static String getValueVariable(String variable) {
		return variable + "Value";
	}
}
