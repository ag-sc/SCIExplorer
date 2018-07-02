package main.java.de.uni.bielefeld.sc.psink.sciexplorer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;

import main.java.de.uni.bielefeld.sc.psink.sciexplorer.misc.Table;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.searchtree.SearchTreeGenerator;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.searchtree.TreeNodeNameComparator;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.searchtree.TreeUtility;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.QueryGenerator;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.QueryResult;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.RDFObject;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.SPARQLDatabase;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.VariableManager;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.query.AbstractStatement;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.query.NotExistsStatement;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.utility.GZIPUtility;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.visualization.ResultTableGenerator;

/**
 * Explorer main-class Manages data and provides all functions for the
 * web-application.
 * 
 * @author ABOROWI
 * @author Maik Fruhner
 */
@ManagedBean
@ViewScoped
public class Explorer implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1709475478663829056L;
	/**
	 * Ontology tree.
	 */
	private static final TreeNode ROOT = SearchTreeGenerator.generateInstance();
	private static final String TRISTATECHECKBOX_UNSELECTED = "0";
	private static final String TRISTATECHECKBOX_SELECTED = "1";
	private static final String TRISTATECHECKBOX_EXCLUDED = "2";
	public static final TreeNode NODE_JUDGEMENT_POSITIVE = TreeUtility.getNodeByPath(ROOT,
			Configuration.PATH_JUDGEMENT_POSITIVE);
	public static final TreeNode NODE_JUDGEMENT_NEGATIVE = TreeUtility.getNodeByPath(ROOT,
			Configuration.PATH_JUDGEMENT_NEGATIVE);
	public static final TreeNode NODE_JUDGEMENT_NEUTRAL = TreeUtility.getNodeByPath(ROOT,
			Configuration.PATH_JUDGEMENT_NEUTRAL);
	private static final TreeNode[] NODES_JUDGEMENT = new TreeNode[] { NODE_JUDGEMENT_POSITIVE, NODE_JUDGEMENT_NEGATIVE,
			NODE_JUDGEMENT_NEUTRAL };

	public static enum Mode {
		QUERY, DETAILS, EXPORT, TABLES
	};

	private static final boolean FACESREDIRECT_YES = true;
	private static final boolean FACESREDIRECT_NO = false;

	private Mode mode;

	private final BidiMap<TreeNode, TreeNode> nodeMap = new DualHashBidiMap<TreeNode, TreeNode>();

	private final Map<TreeNode, String> nodeSelectionMap = new HashMap<TreeNode, String>();

	private TableManager tableManager = new TableManager();
	/**
	 * Variable-manager for statement-generation.
	 */
	private VariableManager variableManager;
	/**
	 * Generated SPARQL-statements.
	 */
	private List<AbstractStatement> statements;
	/**
	 * Active filter-map for statement-generation.
	 */
	private Map<TreeNode, String> filterMap = new HashMap<TreeNode, String>();
	/**
	 * Indicates if subclasses are included in the query.
	 */
	private boolean includeSubtypes = true;

	private String treatmentSearchTerm = "";

	private final List<TreeNode> treatmentNodes = getTreatmentNodes();

	private final List<TreeNode> selectedNodes = new LinkedList<TreeNode>();
	private final List<TreeNode> excludedNodes = new LinkedList<TreeNode>();

	private int numberOfResults;

	private final List<TreeNode> animalModelNodes = sortNodes(getAnimalModelNodes());
	private final TreeNode animalModelRoot = generateListTree(animalModelNodes);
	private final List<TreeNode> investigationMethodNodes = sortNodes(getInvestigationMethodNodes());
	private final TreeNode investigationMethodRoot = generateListTree(investigationMethodNodes);
	private final List<TreeNode> injuryTypeNodes = sortNodes(getInjuryTypeNodes());
	private final TreeNode injuryTypeRoot = generateListTree(injuryTypeNodes);
	private final List<TreeNode> locationNodes = sortNodes(getLocationNodes());
	private final List<TreeNode> deliveryMethodNodes = sortNodes(getDeliveryMethodNodes());

	private BarChartModel animalModelBarChartModel;
	private BarChartModel investigationMethodBarChartModel;
	private BarChartModel injuryTypeBarChartModel;
	private BarChartModel dosageBarChartModel;
	private BarChartModel locationBarChartModel;
	private BarChartModel deliveryMethodBarChartModel;

	private final TreeNode animalModelPropertiesRoot = generateAnimalModelPropertiesListTree();
	private List<TreeNode> clonedAnimalModelDatatypePropertyNodes;
	private final TreeNode animalModelDatatypePropertiesRoot = generateAnimalModelDatatypePropertyListTree();
	private final TreeNode investigationMethodPropertiesRoot = generateInvestigationMethodPropertiesListTree();
	private List<TreeNode> clonedInvestigationMethodDatatypePropertyNodes;
	private final TreeNode investigationMethodDatatypePropertiesRoot = generateInvestigationMethodDatatypePropertyListTree();
	private final TreeNode injuryTypePropertiesRoot = generateInjuryTypePropertiesListTree();
	private List<TreeNode> clonedInjuryTypeDatatypePropertyNodes;
	private final TreeNode injuryTypeDatatypePropertiesRoot = generateInjuryTypeDatatypePropertyListTree();
	private final TreeNode treatmentPropertiesRoot = generateTreatmentPropertiesListTree();
	private List<TreeNode> clonedTreatmentDatatypePropertyNodes;
	private final TreeNode treatmentDatatypePropertiesRoot = generateTreatmentDatatypePropertyListTree();

	/**
	 * Filter-Ausdruck-Map
	 */
	private final Map<TreeNode, String> filterExpressionMap = initializeFilterExpressionMap();

	private boolean integrateSpecialInParameter = false;
	private TreeNode specialSelected;
	private TreeNode specialJudgement;
	private String specialFilter;

	private QueryResult exportData;

	private final TreeNode dosageNode = TreeUtility.getNodeByPath(ROOT, Configuration.PATH_TREATMENTS_COMPOUND_DOSAGE);

	public List<List<String>> getSelectedStringTable() {
		return generateSelectionStringTable(selectedNodes, filterMap);
	}

	public List<List<String>> getExcludedStringTable() {
		return generateSelectionStringTable(excludedNodes, filterMap);
	}

	private static List<List<String>> generateSelectionStringTable(List<TreeNode> nodes,
			Map<TreeNode, String> filterMap) {
		List<String> line;
		List<List<String>> lines = new LinkedList<List<String>>();
		for (Entry<TreeNode, List<TreeNode>> entry : getAnchorMap(nodes).entrySet()) {
			line = new LinkedList<String>();
			line.add(getNodeName(entry.getKey()) + ": ");
			if (TreeUtility.isRelation(entry.getKey())) {
				if (TreeUtility.isRelationNodeDatatypeProperty(entry.getKey())) {
					line.add("\"" + filterMap.get(entry.getKey()) + "\"");
				}
			} else {
				line.add("(" + joinNodeNames(" | ", entry.getValue()) + ")");
			}
			lines.add(line);
		}
		return lines;
	}

	private static String joinNodeNames(String separator, List<TreeNode> nodes) {
		StringBuilder builder = new StringBuilder();
		for (TreeNode node : nodes) {
			builder.append(getNodeName(node));
			builder.append(separator);
		}
		if (builder.length() > 0) {
			builder.setLength(builder.length() - separator.length());
		}
		return builder.toString();
	}

	/**
	 * Determines the relation-anchors of the specified nodes.
	 * 
	 * @param nodes
	 * @return anchor-map
	 */
	private static Map<TreeNode, List<TreeNode>> getAnchorMap(List<TreeNode> nodes) {
		Map<TreeNode, List<TreeNode>> map = new LinkedHashMap<TreeNode, List<TreeNode>>();
		TreeNode anchor;
		List<TreeNode> list;
		for (TreeNode node : nodes) {
			if (TreeUtility.isSubclass(node)) {
				anchor = getSubclassAnchor(node);
			} else {
				anchor = node;
			}
			if (map.containsKey(anchor)) {
				map.get(anchor).add(node);
			} else {
				list = new LinkedList<TreeNode>();
				list.add(node);
				map.put(anchor, list);
			}
		}
		return map;
	}

	private static TreeNode getSubclassAnchor(TreeNode subclassNode) {
		if (TreeUtility.isSubclassNodeDefaultSubclass(subclassNode)) {
			return subclassNode;
		}
		return getSubclassAnchor(subclassNode.getParent());
	}

	/**
	 * Delivers the display-name of the node.
	 * 
	 * @param node
	 * @return
	 */
	public static String getNodeName(TreeNode node) {
		return TreeUtility.getNodeName(node);
	}

	public String getTreatmentSearchTerm() {
		return treatmentSearchTerm;
	}

	public void setTreatmentSearchTerm(String treeSearchTerm) {
		this.treatmentSearchTerm = treeSearchTerm;
	}

	private List<TreeNode> getTreatmentNodes() {
		List<TreeNode> treatmentNodes = new LinkedList<TreeNode>();
		TreeUtility.getDirectSubclassDescendants(treatmentNodes,
				TreeUtility.getNodeByPath(ROOT, Configuration.PATH_TREATMENTS_COMPOUND));
		Collections.sort(treatmentNodes, TreeNodeNameComparator.INSTANCE);
		return treatmentNodes;
	}

	/**
	 * Delivers treatment auto-complete-suggestions for the entered query.
	 * 
	 * @param query
	 * @return
	 */
	public List<String> completeTreatmentSearchTerm(String query) {
		String searchTerm = query.toLowerCase();
		List<String> results = new LinkedList<String>();
		String name;
		for (TreeNode node : treatmentNodes) {
			name = getNodeName(node);
			if (name.toLowerCase().contains(searchTerm)) {
				results.add(name);
			}
		}
		return results;
	}

	public Map<TreeNode, String> getNodeSelectionMap() {
		return nodeSelectionMap;
	}

	private List<TreeNode> sortNodes(List<TreeNode> nodes) {
		TreeUtility.sortByName(nodes);
		return nodes;
	}

	private List<TreeNode> getAnimalModelNodes() {
		List<TreeNode> nodes = new LinkedList<TreeNode>();
		TreeUtility.getDirectSubclassLeaves(nodes, TreeUtility.getNodeByPath(ROOT, Configuration.PATH_ANIMALMODELS));
		return nodes;
	}

	public TreeNode getAnimalModelRoot() {
		return animalModelRoot;
	}

	private TreeNode generateListTree(List<TreeNode> nodes) {
		TreeNode root = new DefaultTreeNode("root");
		TreeNode newNode;
		for (TreeNode node : nodes) {
			newNode = TreeUtility.cloneNode(node);
			root.getChildren().add(newNode);
			nodeMap.put(newNode, node);
			nodeSelectionMap.put(newNode, TRISTATECHECKBOX_UNSELECTED);
		}
		return root;
	}

	private List<TreeNode> getInvestigationMethodNodes() {
		List<TreeNode> nodes = new LinkedList<TreeNode>();
		TreeUtility.getDirectSubclassLeaves(nodes,
				TreeUtility.getNodeByPath(ROOT, Configuration.PATH_INVESTIGATIONMETHODS));
		return nodes;
	}

	public TreeNode getInvestigationMethodRoot() {
		return investigationMethodRoot;
	}

	private List<TreeNode> getInjuryTypeNodes() {
		List<TreeNode> nodes = new LinkedList<TreeNode>();
		TreeUtility.getDirectSubclassLeaves(nodes, TreeUtility.getNodeByPath(ROOT, Configuration.PATH_INJURYTYPES));
		return nodes;
	}

	public TreeNode getInjuryTypeRoot() {
		return injuryTypeRoot;
	}

	private List<TreeNode> getLocationNodes() {
		List<TreeNode> nodes = new LinkedList<TreeNode>();
		TreeUtility.getDirectSubclassLeaves(nodes, TreeUtility.getNodeByPath(ROOT, Configuration.PATH_LOCATION));
		return nodes;
	}

	private List<TreeNode> getDeliveryMethodNodes() {
		List<TreeNode> nodes = new LinkedList<TreeNode>();
		TreeUtility.getDirectSubclassLeaves(nodes, TreeUtility.getNodeByPath(ROOT, Configuration.PATH_DELIVERYMETHOD));
		return nodes;
	}

	private final Map<BarChartModel, List<TreeNode>> modelMap = new HashMap<BarChartModel, List<TreeNode>>();

	/**
	 * Generates a bar-chart.
	 * 
	 * @param variableManager
	 * @param constraintStatements
	 * @param node
	 * @return
	 */
	private BarChartModel generateBarChartModel(VariableManager variableManager,
			List<AbstractStatement> constraintStatements, List<TreeNode> classNodes, String name,
			Collection<TreeNode> forcedNodes) {
		List<AbstractStatement> statements_positive = new LinkedList<AbstractStatement>(constraintStatements);
		QueryGenerator.buildSubclassVariableBranch(new VariableManager(variableManager), statements_positive,
				NODE_JUDGEMENT_POSITIVE, QueryGenerator.LEAVES_IGNORE);
		ChartSeries positiveSeries = new ChartSeries("Positive");

		List<AbstractStatement> statements_negative = new LinkedList<AbstractStatement>(constraintStatements);
		QueryGenerator.buildSubclassVariableBranch(new VariableManager(variableManager), statements_negative,
				NODE_JUDGEMENT_NEGATIVE, QueryGenerator.LEAVES_IGNORE);
		ChartSeries negativeSeries = new ChartSeries("Negative");

		List<AbstractStatement> statements_neutral = new LinkedList<AbstractStatement>(constraintStatements);
		QueryGenerator.buildSubclassVariableBranch(new VariableManager(variableManager), statements_neutral,
				NODE_JUDGEMENT_NEUTRAL, QueryGenerator.LEAVES_IGNORE);
		ChartSeries neutralSeries = new ChartSeries("Neutral");

		List<AbstractStatement> statements;
		String statementString;
		String nodeText;
		int numberOfEntries = 0;
		int positive, negative, neutral;
		List<TreeNode> nodes = new LinkedList<TreeNode>();

		for (TreeNode classNode : classNodes) {
			nodeText = getNodeName(classNode);

			statements = new LinkedList<AbstractStatement>(statements_positive);
			QueryGenerator.buildSubclassVariableBranch(new VariableManager(variableManager), statements, classNode,
					QueryGenerator.LEAVES_INCLUDE);
			statementString = QueryGenerator.generateStatementString(statements);
			// System.out.println("StatementString: " + statementString);
			positive = SPARQLDatabase.countWhere(statementString);

			statements = new LinkedList<AbstractStatement>(statements_negative);
			QueryGenerator.buildSubclassVariableBranch(new VariableManager(variableManager), statements, classNode,
					QueryGenerator.LEAVES_INCLUDE);
			statementString = QueryGenerator.generateStatementString(statements);
			// System.out.println("StatementString: " + statementString);
			negative = SPARQLDatabase.countWhere(statementString);

			statements = new LinkedList<AbstractStatement>(statements_neutral);
			QueryGenerator.buildSubclassVariableBranch(new VariableManager(variableManager), statements, classNode,
					QueryGenerator.LEAVES_INCLUDE);
			statementString = QueryGenerator.generateStatementString(statements);
			// System.out.println("StatementString: " + statementString);
			neutral = SPARQLDatabase.countWhere(statementString);

			if (positive + negative + neutral > 0 || forcedNodes.contains(classNode)) {
				positiveSeries.set(nodeText, positive);
				negativeSeries.set(nodeText, negative);
				neutralSeries.set(nodeText, neutral);
				numberOfEntries++;
				nodes.add(classNode);
			}
		}

		if (numberOfEntries == 0) {
			return null;
		}

		BarChartModel model = generateModel(name, positiveSeries, negativeSeries, neutralSeries);
		modelMap.put(model, nodes);

		return model;
	}

	/**
	 * Generates a bar-chart for named individuals.
	 * 
	 * @param variableManager
	 * @param constraintStatements
	 * @param node
	 * @return
	 */
	private BarChartModel generateBarChartModel(VariableManager variableManager,
			List<AbstractStatement> constraintStatements, TreeNode node, String name) {
		List<AbstractStatement> statements = new LinkedList<AbstractStatement>(constraintStatements);
		VariableManager privateVariableManager = new VariableManager(variableManager);
		QueryGenerator.attachBranch(privateVariableManager, statements, node);
		String where = QueryGenerator.generateStatementString(statements);
		String valueVariable = privateVariableManager.getValueVariable(node);
		String variable = privateVariableManager.getVariable(node);
		Set<String> set = new LinkedHashSet<String>();

		for (List<RDFObject> row : SPARQLDatabase.selectWhere(valueVariable, where).getData()) {
			set.add(row.get(0).toString());
		}

		List<String> values = new LinkedList<String>(set);
		Collections.sort(values);

		List<AbstractStatement> statements_positive = new LinkedList<AbstractStatement>(statements);
		QueryGenerator.buildSubclassVariableBranch(new VariableManager(privateVariableManager), statements_positive,
				NODE_JUDGEMENT_POSITIVE, QueryGenerator.LEAVES_IGNORE);
		String statementsString_positive = QueryGenerator.generateStatementString(statements_positive);
		ChartSeries positiveSeries = new ChartSeries("Positive");

		List<AbstractStatement> statements_negative = new LinkedList<AbstractStatement>(statements);
		QueryGenerator.buildSubclassVariableBranch(new VariableManager(privateVariableManager), statements_negative,
				NODE_JUDGEMENT_NEGATIVE, QueryGenerator.LEAVES_IGNORE);
		String statementsString_negative = QueryGenerator.generateStatementString(statements_negative);
		ChartSeries negativeSeries = new ChartSeries("Negative");

		List<AbstractStatement> statements_neutral = new LinkedList<AbstractStatement>(statements);
		QueryGenerator.buildSubclassVariableBranch(new VariableManager(privateVariableManager), statements_neutral,
				NODE_JUDGEMENT_NEUTRAL, QueryGenerator.LEAVES_IGNORE);
		String statementsString_neutral = QueryGenerator.generateStatementString(statements_neutral);
		ChartSeries neutralSeries = new ChartSeries("Neutral");

		int numberOfEntries = 0;
		int positive, negative, neutral;
		List<TreeNode> nodes = new LinkedList<TreeNode>();

		for (String value : values) {
			positive = getValueCount(statementsString_positive, variable, value);

			negative = getValueCount(statementsString_negative, variable, value);

			neutral = getValueCount(statementsString_neutral, variable, value);

			if (positive + negative + neutral > 0) {
				positiveSeries.set(value, positive);
				negativeSeries.set(value, negative);
				neutralSeries.set(value, neutral);
				numberOfEntries++;
				nodes.add(node);
			}
		}

		if (numberOfEntries == 0) {
			return null;
		}

		BarChartModel model = generateModel(name, positiveSeries, negativeSeries, neutralSeries);
		modelMap.put(model, nodes);

		return model;
	}

	private static BarChartModel generateModel(String name, ChartSeries positive, ChartSeries negative,
			ChartSeries neutral) {
		BarChartModel model = new BarChartModel();
		model.setTitle(name);
		model.setLegendPosition("ne");
		model.setDatatipFormat("%2$d");

		Axis yAxis = model.getAxis(AxisType.Y);
		yAxis.setLabel("Count");

		model.addSeries(positive);
		model.addSeries(negative);
		model.addSeries(neutral);

		model.setSeriesColors(Configuration.BARCHART_COLOR_STRING);

		return model;
	}

	private static int getValueCount(String where, String variable, String value) {
		// System.out.println("Value-Count-Query: " + where + " " +
		// QueryGenerator.generateSpecificValueStatement(variable, value));
		return SPARQLDatabase.countWhere(where + " " + QueryGenerator.generateSpecificValueStatement(variable, value));
	}

	public BarChartModel getAnimalModelBarChartModel() {
		return animalModelBarChartModel;
	}

	public BarChartModel getInvestigationMethodBarChartModel() {
		return investigationMethodBarChartModel;
	}

	public BarChartModel getInjuryTypeBarChartModel() {
		return injuryTypeBarChartModel;
	}

	public BarChartModel getDosageBarChartModel() {
		return dosageBarChartModel;
	}

	public BarChartModel getLocationBarChartModel() {
		return locationBarChartModel;
	}

	public BarChartModel getDeliveryMethodBarChartModel() {
		return deliveryMethodBarChartModel;
	}

	private void updateConstraints() {
		selectedNodes.clear();
		excludedNodes.clear();
		for (Entry<TreeNode, String> entry : nodeSelectionMap.entrySet()) {
			switch (Integer.parseInt(entry.getValue())) {
			case 1:
				selectedNodes.add(nodeMap.get(entry.getKey()));
				break;

			case 2:
				excludedNodes.add(nodeMap.get(entry.getKey()));
				break;
			}
		}
	}

	private void updateConstraintsAndGenerateStatements() {
		updateConstraints();
		generateStatements();
	}

	/**
	 * Generates SPARQL-statements.
	 */
	private void generateStatements() {
		variableManager = new VariableManager();
		filterMap = generateFilterMap();
		statements = new LinkedList<AbstractStatement>();

		// Selected and special node statements:
		HashSet<TreeNode> selectedNodeSet = new HashSet<TreeNode>(selectedNodes);
		if (specialSelected != null && TreeUtility.isDatatypeProperty(specialSelected)) {
			selectedNodeSet.add(nodeMap.get(specialSelected));
		}
		Map<TreeNode, String> privateFilterMap = new HashMap<TreeNode, String>(filterMap);
		if (specialFilter != null) {
			privateFilterMap.put(nodeMap.get(specialSelected), specialFilter);
		}
		QueryGenerator.generateStatements(statements, ROOT, selectedNodeSet, privateFilterMap, variableManager,
				QueryGenerator.TYPEVARIABLES_IGNORE);
		if (specialSelected != null) {
			if (!TreeUtility.isDatatypeProperty(specialSelected)) {
				QueryGenerator.buildSubclassVariableBranch(variableManager, statements, nodeMap.get(specialSelected),
						QueryGenerator.LEAVES_IGNORE);
			}
			QueryGenerator.buildSubclassVariableBranch(variableManager, statements, specialJudgement,
					QueryGenerator.LEAVES_IGNORE);
		}

		// Treatment statements:
		for (TreeNode node : treatmentNodes) {
			if (treatmentSearchTerm.equals(Explorer.getNodeName(node))) {
				QueryGenerator.buildSubclassVariableBranch(variableManager, statements, node, includeSubtypes);
				break;
			}
		}

		// Excluded node statements:
		VariableManager privateVariableManager;
		List<AbstractStatement> privateStatements;
		for (TreeNode excludedNode : excludedNodes) {
			privateVariableManager = new VariableManager(variableManager);
			privateStatements = new LinkedList<AbstractStatement>();
			QueryGenerator.buildNodeBranch(privateVariableManager, privateStatements, excludedNode,
					QueryGenerator.LEAVES_IGNORE, filterMap);
			statements.add(new NotExistsStatement(privateStatements));
		}
	}

	private Map<TreeNode, String> generateFilterMap() {
		Map<TreeNode, String> map = new HashMap<TreeNode, String>();
		for (Entry<TreeNode, String> entry : filterExpressionMap.entrySet()) {
			if (!entry.getValue().isEmpty()) {
				map.put(nodeMap.get(entry.getKey()), entry.getValue());
			}
		}
		return map;
	}

	/**
	 * Sets the query-settings specified by the url-parameter.
	 * 
	 * @param parameter
	 */
	public void setQuery(String parameter) {
		System.out.println("Parameter: " + parameter);
		if (parameter != null && !parameter.isEmpty()) {
			String string;
			try {
				byte[] bytes = Base64.getUrlDecoder().decode(parameter);
				string = new String(GZIPUtility.decompress(bytes));
			} catch (Exception ex) {
				System.err.println("#ERROR: Could not decode query:");
				ex.printStackTrace();
				return;
			}
			System.out.print("Decoded: ");
			System.out.println(string);
			setSettings(string);
		}
		update();
	}

	/**
	 * Sets the query-settings specified by the json-parameter-string.
	 * 
	 * @param parameter
	 */
	private void setSettings(String jsonString) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject jsonRoot = (JSONObject) parser.parse(jsonString);

			treatmentSearchTerm = (String) jsonRoot.get("treatment");

			includeSubtypes = (Boolean) jsonRoot.get("includeSubtypes");

			JSONArray selected = (JSONArray) jsonRoot.get("selected");
			Iterator<JSONArray> selectedIterator = selected.iterator();
			TreeNode node;
			while (selectedIterator.hasNext()) {
				node = TreeUtility.getNodeByIndexPath(ROOT, (JSONArray) selectedIterator.next());
				nodeSelectionMap.put(nodeMap.getKey(node), TRISTATECHECKBOX_SELECTED);
			}
			JSONArray excluded = (JSONArray) jsonRoot.get("excluded");
			Iterator<JSONArray> excludedIterator = excluded.iterator();
			while (excludedIterator.hasNext()) {
				node = TreeUtility.getNodeByIndexPath(ROOT, (JSONArray) excludedIterator.next());
				nodeSelectionMap.put(nodeMap.getKey(node), TRISTATECHECKBOX_EXCLUDED);
			}

			JSONArray filters = (JSONArray) jsonRoot.get("filters");
			Iterator<JSONObject> filtersIterator = filters.iterator();
			JSONObject filter;
			while (filtersIterator.hasNext()) {
				filter = filtersIterator.next();
				filterExpressionMap.put(
						nodeMap.getKey(TreeUtility.getNodeByIndexPath(ROOT, (JSONArray) filter.get("node"))),
						(String) filter.get("expression"));
			}

			if (jsonRoot.containsKey("special")) {
				JSONObject special = (JSONObject) jsonRoot.get("special");
				specialSelected = nodeMap
						.getKey(TreeUtility.getNodeByIndexPath(ROOT, (JSONArray) special.get("selected")));
				specialJudgement = TreeUtility.getNodeByIndexPath(ROOT, (JSONArray) special.get("judgement"));
				if (special.containsKey("filter")) {
					specialFilter = (String) special.get("filter");
				}
			}
		} catch (Exception ex) {
			System.err.println("#ERROR: Could not set settings");
			ex.printStackTrace();
		}
	}

	public void setMode(String newMode) {
		mode = Mode.valueOf(newMode);
	}

	/**
	 * Updates all components.
	 */
	private void update() {
		updateConstraintsAndGenerateStatements();

		String statementString = QueryGenerator.generateStatementString(statements);
		System.out.println("Constraint-Query: " + statementString);
		System.out.println("Mode: " + mode);
		switch (mode) {
		case QUERY:
			updateBarChartModels();
			numberOfResults = SPARQLDatabase.countWhere(statementString);
			break;

		case DETAILS:
			details = ResultTableGenerator.generateTable(statements);
			break;

		case EXPORT:
			exportData = SPARQLDatabase.selectTriplesFromSubgraph(
					statementString + " " + QueryGenerator.ROOT_VARIABLE + " (x:|!x:)* ?s . " + "?s ?p ?o .");
			break;

		case TABLES:
			this.tableManager.update(investigationMethodNodes, injuryTypeNodes, locationNodes);
			break;
		}
	}

	public int getNumberOfResults() {
		return numberOfResults;
	}

	private Table details;

	public Table getDetails() {
		return details;
	}

	public String query() {
		return generateQueryPageUrl();
	}

	public String generateQueryPageUrl() {
		return generateQueryPageUrl(FACESREDIRECT_YES);
	}

	private String generateQueryPageUrl(boolean facesRedirect) {
		return generateUrl("index.xhtml", facesRedirect);
	}

	private String generateUrl(String page, boolean facesRedirect) {
		StringBuilder builder = new StringBuilder(page);

		String string = generateQueryParameterString();
		if (string != null) {
			builder.append("?");
			if (facesRedirect) {
				builder.append("faces-redirect=true");
			}
			builder.append("&query=");
			builder.append(string);
		}

		return builder.toString();
	}

	private String generateQueryParameterString() {
		String string = generateJSONString();
		System.out.println("JSON: " + string);
		byte[] bytes;
		try {
			bytes = GZIPUtility.compress(string);
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		string = Base64.getUrlEncoder().encodeToString(bytes);
		// System.out.print("Encoded: ");
		// System.out.println(string);

		return string;
	}

	private String generateJSONString() {
		JSONObject jsonRoot = new JSONObject();

		jsonRoot.put("treatment", treatmentSearchTerm);

		jsonRoot.put("includeSubtypes", includeSubtypes);

		JSONArray selected = new JSONArray();
		JSONArray excluded = new JSONArray();
		for (Entry<TreeNode, String> entry : nodeSelectionMap.entrySet()) {
			switch (Integer.parseInt(entry.getValue())) {
			case 1:
				selected.add(generatePathJSONArray(nodeMap.get(entry.getKey())));
				break;

			case 2:
				excluded.add(generatePathJSONArray(nodeMap.get(entry.getKey())));
				break;
			}
		}
		jsonRoot.put("selected", selected);
		jsonRoot.put("excluded", excluded);

		JSONArray filters = new JSONArray();
		JSONObject filter;
		for (Entry<TreeNode, String> entry : filterExpressionMap.entrySet()) {
			if (!entry.getValue().isEmpty()) {
				filter = new JSONObject();
				filter.put("node", generatePathJSONArray(nodeMap.get(entry.getKey())));
				filter.put("expression", entry.getValue());
				filters.add(filter);
			}
		}
		jsonRoot.put("filters", filters);

		if (integrateSpecialInParameter) {
			JSONObject special = new JSONObject();
			special.put("selected", generatePathJSONArray(nodeMap.get(specialSelected)));
			special.put("judgement", generatePathJSONArray(specialJudgement));
			if (specialFilter != null) {
				special.put("filter", specialFilter);
			}
			jsonRoot.put("special", special);
		}

		return jsonRoot.toString();
	}

	private JSONArray generatePathJSONArray(TreeNode node) {
		JSONArray path = new JSONArray();
		for (int segment : TreeUtility.getIndexPath(node)) {
			path.add(segment);
		}
		return path;
	}

	private void updateBarChartModels() {
		modelMap.clear();
		animalModelBarChartModel = generateBarChartModel(variableManager, statements, animalModelNodes, "AnimalModel",
				selectedNodes);
		investigationMethodBarChartModel = generateBarChartModel(variableManager, statements, investigationMethodNodes,
				"InvestigationMethod", selectedNodes);
		injuryTypeBarChartModel = generateBarChartModel(variableManager, statements, injuryTypeNodes, "InjuryType",
				selectedNodes);
		dosageBarChartModel = generateBarChartModel(variableManager, statements, dosageNode, "Dosage");
		locationBarChartModel = generateBarChartModel(variableManager, statements, locationNodes, "Location",
				selectedNodes);
		deliveryMethodBarChartModel = generateBarChartModel(variableManager, statements, deliveryMethodNodes,
				"DeliveryMethod", selectedNodes);
	}

	private TreeNode generateAnimalModelPropertiesListTree() {
		TreeNode treeRoot = new DefaultTreeNode("root");
		addToListTree(treeRoot, "Gender",
				TreeUtility.getNodeByPath(ROOT, Configuration.PATH_ANIMALMODEL_PROPERTY_GENDER).getChildren());
		addToListTree(treeRoot, "AgeCategory",
				TreeUtility.getNodeByPath(ROOT, Configuration.PATH_ANIMALMODEL_PROPERTY_AGECATEGORY).getChildren());
		addToListTree(treeRoot, "OrganismSpecies", sortNodes(TreeUtility.getDirectSubclassLeaves(
				TreeUtility.getNodeByPath(ROOT, Configuration.PATH_ANIMALMODEL_PROPERTY_ORGANISMSPECIES))));
		return treeRoot;
	}

	private void addToListTree(TreeNode root, String containerName, List<TreeNode> nodes) {
		if (nodes.isEmpty()) {
			return;
		}
		TreeNode container = new DefaultTreeNode(containerName);
		container.setType("container");
		root.getChildren().add(container);
		TreeNode newNode;
		for (TreeNode node : nodes) {
			newNode = TreeUtility.cloneNode(node);
			newNode.setType("property");
			container.getChildren().add(newNode);
			nodeMap.put(newNode, node);
			nodeSelectionMap.put(newNode, TRISTATECHECKBOX_UNSELECTED);
		}
	}

	public TreeNode getAnimalModelPropertiesRoot() {
		return animalModelPropertiesRoot;
	}

	private TreeNode generateAnimalModelDatatypePropertyListTree() {
		List<TreeNode> relationDescendants = TreeUtility
				.getNearestRelationDescendants(TreeUtility.getNodeByPath(ROOT, Configuration.PATH_ORGANISMMODELS));
		List<TreeNode> nodes = new LinkedList<TreeNode>();
		for (TreeNode relationDescendant : relationDescendants) {
			if (TreeUtility.isDatatypeProperty(relationDescendant)) {
				nodes.add(relationDescendant);
			}
		}
		sortNodes(nodes);
		clonedAnimalModelDatatypePropertyNodes = new LinkedList<TreeNode>();
		return generateListTree(nodes, clonedAnimalModelDatatypePropertyNodes);
	}

	public TreeNode getAnimalModelDatatypePropertiesRoot() {
		return animalModelDatatypePropertiesRoot;
	}

	private TreeNode generateInvestigationMethodPropertiesListTree() {
		List<TreeNode> relationDescendants = TreeUtility
				.getNearestRelationDescendants(TreeUtility.getNodeByPath(ROOT, Configuration.PATH_INVESTIGATIONMETHOD));
		TreeNode treeRoot = new DefaultTreeNode("root");
		for (TreeNode relationDescendant : relationDescendants) {
			addToListTree(treeRoot, getNodeName(relationDescendant),
					sortNodes(TreeUtility.getDirectSubclassLeaves(relationDescendant)));
		}
		return treeRoot;
	}

	public TreeNode getInvestigationMethodPropertiesRoot() {
		return investigationMethodPropertiesRoot;
	}

	private TreeNode generateInvestigationMethodDatatypePropertyListTree() {
		List<TreeNode> relationDescendants = TreeUtility
				.getNearestRelationDescendants(TreeUtility.getNodeByPath(ROOT, Configuration.PATH_INVESTIGATIONMETHOD));
		List<TreeNode> nodes = new LinkedList<TreeNode>();
		for (TreeNode relationDescendant : relationDescendants) {
			if (TreeUtility.isDatatypeProperty(relationDescendant)) {
				nodes.add(relationDescendant);
			}
		}
		sortNodes(nodes);
		clonedInvestigationMethodDatatypePropertyNodes = new LinkedList<TreeNode>();
		return generateListTree(nodes, clonedInvestigationMethodDatatypePropertyNodes);
	}

	public TreeNode getInvestigationMethodDatatypePropertiesRoot() {
		return investigationMethodDatatypePropertiesRoot;
	}

	private TreeNode generateInjuryTypePropertiesListTree() {
		List<TreeNode> relationDescendants = TreeUtility
				.getNearestRelationDescendants(TreeUtility.getNodeByPath(ROOT, Configuration.PATH_INJURYMODEL));
		TreeNode treeRoot = new DefaultTreeNode("root");
		for (TreeNode relationDescendant : relationDescendants) {
			addToListTree(treeRoot, getNodeName(relationDescendant),
					sortNodes(TreeUtility.getDirectSubclassLeaves(relationDescendant)));
		}
		return treeRoot;
	}

	public TreeNode getInjuryTypePropertiesRoot() {
		return injuryTypePropertiesRoot;
	}

	private TreeNode generateInjuryTypeDatatypePropertyListTree() {
		List<TreeNode> relationDescendants = TreeUtility
				.getNearestRelationDescendants(TreeUtility.getNodeByPath(ROOT, Configuration.PATH_INJURYMODEL));
		List<TreeNode> nodes = new LinkedList<TreeNode>();
		for (TreeNode relationDescendant : relationDescendants) {
			if (TreeUtility.isDatatypeProperty(relationDescendant)) {
				nodes.add(relationDescendant);
			}
		}
		sortNodes(nodes);
		clonedInjuryTypeDatatypePropertyNodes = new LinkedList<TreeNode>();
		return generateListTree(nodes, clonedInjuryTypeDatatypePropertyNodes);
	}

	public TreeNode getInjuryTypeDatatypePropertiesRoot() {
		return injuryTypeDatatypePropertiesRoot;
	}

	private TreeNode generateTreatmentPropertiesListTree() {
		List<TreeNode> relationDescendants = TreeUtility
				.getNearestRelationDescendants(TreeUtility.getNodeByPath(ROOT, Configuration.PATH_TREATMENT));
		TreeNode treeRoot = new DefaultTreeNode("root");
		for (TreeNode relationDescendant : relationDescendants) {
			addToListTree(treeRoot, getNodeName(relationDescendant),
					sortNodes(TreeUtility.getDirectSubclassLeaves(relationDescendant)));
		}
		return treeRoot;
	}

	public TreeNode getTreatmentPropertiesRoot() {
		return treatmentPropertiesRoot;
	}

	private TreeNode generateTreatmentDatatypePropertyListTree() {
		List<TreeNode> relationDescendants = TreeUtility
				.getNearestRelationDescendants(TreeUtility.getNodeByPath(ROOT, Configuration.PATH_TREATMENT));
		List<TreeNode> nodes = new LinkedList<TreeNode>();
		for (TreeNode relationDescendant : relationDescendants) {
			if (TreeUtility.isDatatypeProperty(relationDescendant)) {
				nodes.add(relationDescendant);
			}
		}
		sortNodes(nodes);
		clonedTreatmentDatatypePropertyNodes = new LinkedList<TreeNode>();
		return generateListTree(nodes, clonedTreatmentDatatypePropertyNodes);
	}

	public TreeNode getTreatmentDatatypePropertiesRoot() {
		return treatmentDatatypePropertiesRoot;
	}

	private TreeNode generateListTree(List<TreeNode> nodes, List<TreeNode> clonedNodes) {
		TreeNode root = new DefaultTreeNode("root");
		TreeNode newNode;
		for (TreeNode node : nodes) {
			newNode = TreeUtility.cloneNode(node);
			root.getChildren().add(newNode);
			nodeMap.put(newNode, node);
			clonedNodes.add(newNode);
		}
		return root;
	}

	private Map<TreeNode, String> initializeFilterExpressionMap() {
		Map<TreeNode, String> map = new HashMap<TreeNode, String>();
		populateFilterExpressions(map, clonedAnimalModelDatatypePropertyNodes);
		populateFilterExpressions(map, clonedInvestigationMethodDatatypePropertyNodes);
		populateFilterExpressions(map, clonedInjuryTypeDatatypePropertyNodes);
		populateFilterExpressions(map, clonedTreatmentDatatypePropertyNodes);
		return map;
	}

	private static void populateFilterExpressions(Map<TreeNode, String> map, List<TreeNode> nodes) {
		for (TreeNode node : nodes) {
			map.put(node, "");
		}
	}

	public Map<TreeNode, String> getFilterExpressionMap() {
		return filterExpressionMap;
	}

	public void barSelected(ItemSelectEvent event) {
		System.out.println("Item Index: " + event.getItemIndex() + ", Series Index:" + event.getSeriesIndex()
				+ ", Source: " + event.getSource());

		BarChartModel selectedModel = (BarChartModel) event.getComponent().getAttributes().get("selectedBarChartModel");
		specialSelected = nodeMap.getKey(modelMap.get(selectedModel).get(event.getItemIndex()));
		specialJudgement = NODES_JUDGEMENT[event.getSeriesIndex()];
		if (TreeUtility.isDatatypeProperty(specialSelected)) {
			specialFilter = selectedModel.getTicks().get(event.getItemIndex());
		}
		integrateSpecialInParameter = true;

		redirect(generateDetailsPageUrl(FACESREDIRECT_NO));
	}

	private static void redirect(String page) {
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect(page);
		} catch (IOException ex) {
			System.err.print("Explorer - ERROR: Could not redirect to '" + page + "'");
			ex.printStackTrace();
		}
		FacesContext.getCurrentInstance().responseComplete();
	}

	public void treatmentChanged() {
		redirect(generateQueryPageUrl(FACESREDIRECT_NO));
	}

	public String reset() {
		return "index.xhtml?faces-redirect=true";
	}

	public boolean isIncludeSubtypes() {
		return includeSubtypes;
	}

	public void setIncludeSubtypes(boolean includeSubtypes) {
		this.includeSubtypes = includeSubtypes;
	}

	public String details() {
		return generateDetailsPageUrl();
	}

	public String tables() {
		return generateTablesPageUrl();
	}

	public String generateDetailsPageUrl() {
		return generateDetailsPageUrl(FACESREDIRECT_YES);
	}

	public String generateTablesPageUrl() {
		return generateTablesPageUrl(FACESREDIRECT_YES);
	}

	private String generateDetailsPageUrl(boolean facesRedirect) {
		return generateUrl("details.xhtml", facesRedirect);
	}

	private String generateTablesPageUrl(boolean facesRedirect) {
		//return generateUrl("tables.xhtml", facesRedirect);
		String url = "tables.xhtml";
		if(facesRedirect) {
			url += "?faces-redirect=true";
		}
		return url;
	}

	public String generateExportPageUrl() {
		return generateExportPageUrl(FACESREDIRECT_YES);
	}

	public void export() {
		updateConstraintsAndGenerateStatements();
		String statementString = QueryGenerator.generateStatementString(statements);
		exportData = SPARQLDatabase.selectTriplesFromSubgraph(
				statementString + " " + QueryGenerator.ROOT_VARIABLE + " (x:|!x:)* ?s . " + "?s ?p ?o .");

		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();

		ec.responseReset(); // Some JSF component library or some Filter might
							// have set some headers in the buffer beforehand.
							// We want to get rid of them, else it may collide.
		ec.setResponseContentType("application/octet-stream"); // Check
																// http://www.iana.org/assignments/media-types
																// for all
																// types. Use if
																// necessary
																// ExternalContext#getMimeType()
																// for
																// auto-detection
																// based on
																// filename.
		// ec.setResponseContentLength(contentLength); // Set it with the file
		// size. This header is optional. It will work if it's omitted, but the
		// download progress will be unknown.
		ec.setResponseHeader("Content-Disposition", "attachment; filename=\"export.n-triples\""); // The
																									// Save
																									// As
																									// popup
																									// magic
																									// is
																									// done
																									// here.
																									// You
																									// can
																									// give
																									// it
																									// any
																									// file
																									// name
																									// you
																									// want,
																									// this
																									// only
																									// won't
																									// work
																									// in
																									// MSIE,
																									// it
																									// will
																									// use
																									// current
																									// request
																									// URL
																									// as
																									// file
																									// name
																									// instead.

		try {
			OutputStreamWriter writer = new OutputStreamWriter(ec.getResponseOutputStream(), "UTF-8");
			for (List<RDFObject> line : exportData.getData()) {
				for (RDFObject object : line) {
					if (object.isResource()) {
						writer.append("<");
						writer.append(object.toString());
						writer.append("> ");
					} else {
						writer.append("\"");
						writer.append(object.toString());
						writer.append("\" ");
					}
				}
				writer.append(".\n");
			}
			writer.flush();
		} catch (IOException e) {
			System.err.println("ERROR: could not send rdf-data");
			e.printStackTrace();
		}

		fc.responseComplete(); // Important! Otherwise JSF will attempt to
								// render the response which obviously will fail
								// since it's already written with a file and
								// closed.
	}

	private String generateExportPageUrl(boolean facesRedirect) {
		return generateUrl("export.xhtml", facesRedirect);
	}

	public QueryResult getExportData() {
		return exportData;
	}

	public TableManager getTableManager() {
		return tableManager;
	}
}