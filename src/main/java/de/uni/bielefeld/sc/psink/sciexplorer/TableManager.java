package main.java.de.uni.bielefeld.sc.psink.sciexplorer;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.primefaces.model.TreeNode;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.searchtree.Subclass;

/**
 * TableManager provides the data for the tables.xhtml page
 * 
 * @author Maik Fruhner
 */

public class TableManager {

	/** data **/
	private List<Subclass> injuryTypes;
	private List<Subclass> locations;

	public TableManager() {
		this.injuryTypes = new ArrayList<Subclass>();
		this.locations = new ArrayList<Subclass>();
	}

	public void update(List<TreeNode> injuryTypeNodes, List<TreeNode> locationNodes) {
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
	}

	/** Getters **/

	public List<Subclass> getInjuryTypes() {
		return injuryTypes;
	}

	public List<Subclass> getLocations() {
		return locations;
	}
}
