package main.java.de.uni.bielefeld.sc.psink.sciexplorer;

import java.util.List;
import java.io.Serializable;
import java.util.ArrayList;
import org.primefaces.model.TreeNode;

import main.java.de.uni.bielefeld.sc.psink.sciexplorer.searchtree.Subclass;


public class TableManager implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7786371982375044075L;
	
	private List<Subclass> injuryTypes;
	
	public List<Subclass> getInjuryTypes() {
		return injuryTypes;
	}

	public TableManager() {
		this.injuryTypes = new ArrayList<Subclass>();
	}
	
	public void update(List<TreeNode> injuryTypeNodes) {
		// injury types
		for (TreeNode treeNode : injuryTypeNodes) {
			Subclass sc = (Subclass)(treeNode.getData());
			this.injuryTypes.add(sc);
		}
	}
	
	
}
