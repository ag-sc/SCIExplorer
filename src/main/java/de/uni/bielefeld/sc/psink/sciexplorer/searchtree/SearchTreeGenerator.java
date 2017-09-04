/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.searchtree;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.opencsv.CSVReader;

import main.java.de.uni.bielefeld.sc.psink.sciexplorer.Configuration;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.ResourceUtility;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.QueryGenerator;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.SPARQLDatabase;

/**
 * Search-tree-generator. Is used to generate the ontology-tree from the
 * csv-files.
 * 
 * @author ABOROWI
 */
public class SearchTreeGenerator {
	private static final List<String[]> RELATIONS = getCSVData(
			ResourceUtility.getResourceAsStream(Configuration.CSV_RELATIONS_FILE));
	private static final List<String[]> CLASSES = getCSVData(
			ResourceUtility.getResourceAsStream(Configuration.CSV_CLASSES_FILE));
	private static final List<String[]> SUBCLASSES = getCSVData(
			ResourceUtility.getResourceAsStream(Configuration.CSV_SUBCLASSES_FILE));

	private static final HashSet EXCLUDED_RELATIONS = new HashSet(Arrays.asList(Configuration.EXCLUDED_RELATIONS));
	private static final TreeNode ROOT = createTree();

	public static TreeNode generateInstance() {
		return TreeUtility.cloneTree(ROOT);
	}

	/**
	 * Loads all rows of the specified CSV-file-stream.
	 * 
	 * @param stream
	 * @return CSV-rows
	 */
	private static List<String[]> getCSVData(InputStream stream) {
		try (CSVReader reader = new CSVReader(new InputStreamReader(stream), '\t', '"', 1);) {
			return reader.readAll();
		} catch (IOException ex) {
			System.err.println("SearchTreeStructure: Could not read stream because of '" + ex + "'!");
			Logger.getLogger(SearchTreeGenerator.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;
	}

	/**
	 * Looks for rows which specified column matches the specified value.
	 * 
	 * @param rows
	 * @param column
	 * @param value
	 * @return rows that match
	 */
	private static List<String[]> findRowsByColumnValue(List<String[]> rows, int column, String value) {
		List<String[]> hits = new LinkedList();
		for (String[] row : rows) {
			if (row[column].equals(value)) {
				hits.add(row);
			}
		}
		return hits;
	}

	/**
	 * Looks for the first row which specified column matches the specified
	 * value.
	 * 
	 * @param rows
	 * @param column
	 * @param value
	 * @return row that match
	 */
	private static String[] findFirstRowByColumnValue(List<String[]> rows, int column, String value) {
		for (String[] row : rows) {
			if (row[column].equals(value)) {
				return row;
			}
		}
		return null;
	}

	/**
	 * Generates the ontology-tree defined by the CSV-files.
	 * 
	 * @return
	 */
	private static TreeNode createTree() {
		TreeNode root = buildTree();
		List<TreeNode> nodes = TreeUtility.getDescendants(root);
		String where;
		for (TreeNode node : nodes) {
			where = QueryGenerator.generateStatementBlockString(root, node, QueryGenerator.TYPEVARIABLES_IGNORE);
			// System.out.println("Count-block: " + where);
			TreeUtility.getTreeObject(node).setResultCount(SPARQLDatabase.countWhere(where));
			if (TreeUtility.isSubclass(node)) {
				if (TreeUtility.hasSubclassChildren(node)) {
					where = QueryGenerator.generateSubclassStatementBlockString(node, QueryGenerator.LEAVES_INCLUDE);
					// System.out.println("Count-block: " + where);
					TreeUtility.getSubclass(node).setSubclassResultCount(SPARQLDatabase.countWhere(where));
				}
			}
		}
		return root;
	}

	/**
	 * Recursive function for the tree-creation.
	 * 
	 * @return ontology-tree
	 */
	private static TreeNode buildTree() {
		TreeNode root = new DefaultTreeNode(Configuration.SPARQL_ROOT_VARIABLENAME);
		HashSet<String> searchTerms = new HashSet();
		// searchTerms.add(Configuration.SPARQL_ROOT_VARIABLENAME);
		buildTree(searchTerms, root, Configuration.SPARQL_ROOT_VARIABLENAME);
		return root;
	}

	private static void buildTree(HashSet<String> searchTerms, TreeNode parent, String parentName) {
		if (searchTerms.contains(parentName)) {
			// System.out.println("SearchTreeGenerator: relation-search for '" +
			// parentName + "' aborted...");
			return;
		}
		// System.out.println("SearchTreeGenerator: searching child-relations
		// for '" + parentName + "'...");
		searchTerms.add(parentName);
		List<String[]> rows = findRowsByColumnValue(RELATIONS, 0, parentName);
		/*
		 * if(rows.isEmpty()) {
		 * System.out.println("SearchTreeGenerator: no child-relations for '" +
		 * parentName + "' found!"); }
		 */
		Relation relation;
		TreeNode node;
		Subclass subclass;
		TreeNode defaultSubclassNode;
		for (String[] row : rows) {
			if (EXCLUDED_RELATIONS.contains(row[6])) {
				continue;
			}
			relation = new Relation(row[0], row[1], row[2], row[5].equals("true"), row[6]);
			node = new DefaultTreeNode(relation);
			buildTree(searchTerms, node, relation.getRangeClass());

			subclass = new Subclass(relation.getRangeClass(), isNamedIndividual(relation.getRangeClass()),
					Subclass.DEFAULT_SUBCLASS);
			defaultSubclassNode = new DefaultTreeNode(subclass);
			// System.out.println("SearchTreeGenerator: adding subclasses to
			// default-subclass '" + subclass.getName() + "'...");
			if (addSubclasses(searchTerms, defaultSubclassNode, relation.getRangeClass())) {
				subclass.setSelectableVariable(true);
				subclass.setSelectableConstraint(true);
				defaultSubclassNode.setSelectable(false);
				TreeUtility.connectParentAndChild(node, defaultSubclassNode);
			}

			relation.setSelectableVariable(Configuration.TREE_SELECTABLESTRATEGY_RELATION.isSelectableVariable(node));
			relation.setSelectableConstraint(
					Configuration.TREE_SELECTABLESTRATEGY_RELATION.isSelectableConstraint(node));
			TreeUtility.connectParentAndChild(parent, node);
		}
		searchTerms.remove(parentName);
	}

	private static boolean isNamedIndividual(String className) {
		return findFirstRowByColumnValue(CLASSES, 0, className)[1].equals("true");
	}

	private static boolean addSubclasses(HashSet<String> searchTerms, TreeNode parent, String className) {
		if (searchTerms.contains(className)) {
			// System.out.println("SearchTreeGenerator: subclass-search for '" +
			// className + "' aborted...");
			return false;
		}
		// System.out.println("SearchTreeGenerator: searching child-subclasses
		// for '" + className + "'...");
		searchTerms.add(className);
		List<String[]> rows = findRowsByColumnValue(SUBCLASSES, 0, className);
		if (rows.isEmpty()) {
			// System.out.println("SearchTreeGenerator: no child-subclasses for
			// '" + className + "' found!");
			searchTerms.remove(className);
			return false;
		}
		Subclass subclass;
		TreeNode node;
		for (String[] row : rows) {
			subclass = new Subclass(row[1], isNamedIndividual(row[1]));
			node = new DefaultTreeNode(subclass);
			buildTree(searchTerms, node, subclass.getName());
			addSubclasses(searchTerms, node, subclass.getName());
			subclass.setSelectableVariable(Configuration.TREE_SELECTABLESTRATEGY_SUBCLASS.isSelectableVariable(node));
			subclass.setSelectableConstraint(
					Configuration.TREE_SELECTABLESTRATEGY_SUBCLASS.isSelectableConstraint(node));
			// node.setSelectable(false);
			TreeUtility.connectParentAndChild(parent, node);
		}
		searchTerms.remove(className);
		return true;
	}
}
