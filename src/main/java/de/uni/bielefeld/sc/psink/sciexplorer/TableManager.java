package main.java.de.uni.bielefeld.sc.psink.sciexplorer;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.primefaces.model.TreeNode;

import main.java.de.uni.bielefeld.sc.psink.sciexplorer.searchtree.Subclass;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.QueryGenerator;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.QueryResult;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.RDFObject;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.SPARQLDatabase;

/**
 * TableManager provides the data for the tables.xhtml page
 * 
 * @author Maik Fruhner
 */

public class TableManager {

	/** data **/
	private List<Subclass> injuryTypes;
	private List<Subclass> locations;

	private List<Subclass> functionalTests;

	public TableManager() {
		this.injuryTypes = new ArrayList<Subclass>();
		this.locations = new ArrayList<Subclass>();

		this.functionalTests = new ArrayList<Subclass>();
	}

	public void update(List<TreeNode> investigationMethodNodes, List<TreeNode> injuryTypeNodes,
			List<TreeNode> locationNodes) {

		// investigation methods, only functional for now
		// TODO include other 3 categories

		for (TreeNode treeNode : investigationMethodNodes) {
			// get the leave node
			Subclass investigationMethod = (Subclass) (treeNode.getData());
			// System.out.println(investigationMethod.getName());

			TreeNode current = treeNode;

			// search for a parent, which is a functional test or investigation method
			while (true) {
				Subclass parent = (Subclass) (current.getParent().getData());
				// System.out.println(parent.getName());

				if (parent.getName().equals("FunctionalTest")) {
					// current leave is a functional test
					this.functionalTests.add(investigationMethod);
					break;
				}

				if (parent.getName().equals("InvestigationMethod")) {
					// current leave is a not a functional test
					break;
				}

				current = current.getParent();

			}
			// System.out.println(this.functionalTests.size());
			// System.out.println("--------");
		}

		// injury types
		for (TreeNode treeNode : injuryTypeNodes) {
			Subclass sc = (Subclass) (treeNode.getData());
			this.injuryTypes.add(sc);
		}

		// locations, only take the 4 important for now
		List<String> importantLocations = Arrays.asList("Thoracic", "Lumbar", "Cervical", "Sacral");
		for (TreeNode treeNode : locationNodes) {
			Subclass sc = (Subclass) (treeNode.getData());
			if (importantLocations.contains(sc.getName())) {
				locations.add(sc);
			}
		}

		this.queryData();
	}

	private void queryData() {

		String where = "?Result <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/Result>. ?Result <http://psink.de/scio/hasTargetGroup> ?TargetExperimentalGroup. ?TargetExperimentalGroup <http://psink.de/scio/hasInjuryModel> ?InjuryModel. ?InjuryModel <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/Compression>. ?InjuryModel <http://psink.de/scio/hasInjuryLocation> <http://psink.de/scio/Thoracic> .";

		QueryResult results = SPARQLDatabase.selectWhere(QueryGenerator.ROOT_VARIABLE, where,
				QueryGenerator.ORDER_BY_ROOTVARIABLE);

		for (List<RDFObject> resultRow : results.getData()) {
			System.out.println(resultRow.get(0).toString());
		}
	}

	/** Getters **/

	public List<Subclass> getInjuryTypes() {
		return injuryTypes;
	}

	public List<Subclass> getLocations() {
		return locations;
	}

	public List<Subclass> getFunctionalTests() {
		return functionalTests;
	}
}
