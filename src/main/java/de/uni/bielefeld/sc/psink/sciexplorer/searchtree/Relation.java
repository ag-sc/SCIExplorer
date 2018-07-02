package main.java.de.uni.bielefeld.sc.psink.sciexplorer.searchtree;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Relation datastructure
 * 
 * @author ABOROWI
 */
public class Relation extends TreeObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3064453491765083303L;
	
	private final String domainClass;
	private final String relation;
	private final String rangeClass;
	private final boolean dataTypeProperty;
	private final String mergedName;

	public Relation(String domainClass, String relation, String rangeClass, boolean dataTypeProperty,
			String mergedName) {
		this.domainClass = domainClass;
		this.relation = relation;
		this.rangeClass = rangeClass;
		this.dataTypeProperty = dataTypeProperty;
		this.mergedName = mergedName;
	}

	public String getDomainClass() {
		return domainClass;
	}

	public String getRelation() {
		return relation;
	}

	public String getRangeClass() {
		return rangeClass;
	}

	public boolean isDataTypeProperty() {
		return dataTypeProperty;
	}

	public String getMergedName() {
		return mergedName;
	}
}
