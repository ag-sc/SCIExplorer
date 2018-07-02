/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer;

import org.primefaces.model.TreeNode;

import main.java.de.uni.bielefeld.sc.psink.sciexplorer.searchtree.SelectableStrategy;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.searchtree.TreeUtility;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.ApacheJenaDatabase;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.SPARQLDatabaseInterface;

/**
 *
 * @author ABOROWI
 */
public class Configuration {
	
	private static final int ONTOLOGY_VERSION = 51;
	/**
	 * Filenames of CSV-Files
	 */
	public static final String CSV_RELATIONS_FILE = "scio_v_" + ONTOLOGY_VERSION + "_relations.csv";
	public static final String CSV_CLASSES_FILE = "scio_v_" + ONTOLOGY_VERSION + "_classes.csv";
	public static final String CSV_SUBCLASSES_FILE = "scio_v_" + ONTOLOGY_VERSION + "_subclasses.csv";

	/**
	 * During the generation of search tree, exclude relations mentioned here.
	 */
	public static final String[] EXCLUDED_RELATIONS = new String[] { "BelongsToExperimentalGroup" };

	/**
	 * File name of the RDF triple data.
	 */
	public static final String RDF_TRIPLES_FILE = "brigitte_full25_v" + ONTOLOGY_VERSION + ".n-triples";

	/**
	 * URIs of resources
	 */
	public static final String RDF_URI_PREFIX_ONTOLOGY = "http://psink.de/scio/";
	public static final String RDF_URI_PREFIX_DATA = "http://scio/data/";

	public static final SPARQLDatabaseInterface SPARQL_DATABASE = new ApacheJenaDatabase();

	/**
	 * Query-Root. (Depends on the ontology)
	 */
	public static final String SPARQL_ROOT_VARIABLENAME = "Result";

	public static final SelectableStrategy TREE_SELECTABLESTRATEGY_RELATION = new SelectableStrategy() {
		@Override
		public boolean isSelectableVariable(TreeNode node) {
			if (TreeUtility.isRelationNodeDatatypeProperty(node)) {
				return true;
			}
			return false;
		}

		@Override
		public boolean isSelectableConstraint(TreeNode node) {
			return true;
		}
	};
	public static final SelectableStrategy TREE_SELECTABLESTRATEGY_SUBCLASS = new SelectableStrategy() {
		@Override
		public boolean isSelectableVariable(TreeNode node) {
			return TreeUtility.hasSubclassChildren(node);
		}

		@Override
		public boolean isSelectableConstraint(TreeNode node) {
			return true;
		}
	};

	public static final String COLOR_POSITIVE = "81F781";
	public static final String COLOR_NEGATIVE = "F78181";
	public static final String COLOR_NEUTRAL = "D8D8D8";

	public static final String BARCHART_COLOR_STRING = COLOR_POSITIVE + "," + COLOR_NEGATIVE + "," + COLOR_NEUTRAL;

	/**
	 * The following variables describe the construction of the search tree.
	 * Settings to build the search tree nodes. The paths start at the root
	 * {@value #SPARQL_ROOT_VARIABLENAME}.
	 */

	public static final String[] PATH_TREATMENTS_COMPOUND = new String[] { "TargetExperimentalGroup", "TreatmentTypes",
			"Treatment", "CompoundTreatment", "SuppliedCompound", "Compound", "Compound" };

	public static final String[] PATH_TREATMENTS_COMPOUND_DOSAGE = new String[] { "TargetExperimentalGroup",
			"TreatmentTypes", "Treatment", "CompoundTreatment", "Dosage" };

	public static final String[] PATH_TREATMENT = new String[] { "TargetExperimentalGroup", "TreatmentTypes" };

	public static final String[] PATH_ANIMALMODELS = new String[] { "TargetExperimentalGroup", "OrganismModel",
			"OrganismModel", "AnimalModel" };

	public static final String[] PATH_ORGANISMMODELS = new String[] { "TargetExperimentalGroup", "OrganismModel" };

	public static final String[] PATH_INVESTIGATIONMETHODS = new String[] { "InvestigationMethod",
			"InvestigationMethod" };

	public static final String[] PATH_INVESTIGATIONMETHOD = new String[] { "InvestigationMethod" };

	public static final String[] PATH_INJURYMODEL = new String[] { "TargetExperimentalGroup", "InjuryModel" };

	public static final String[] PATH_INJURYTYPES = new String[] { "TargetExperimentalGroup", "InjuryModel", "Injury" };

	public static final String[] PATH_JUDGEMENT_POSITIVE = new String[] { "Judgement", "Judgement", "Positive" };

	public static final String[] PATH_JUDGEMENT_NEGATIVE = new String[] { "Judgement", "Judgement", "Negative" };

	public static final String[] PATH_JUDGEMENT_NEUTRAL = new String[] { "Judgement", "Judgement", "Neutral" };

	public static final String[] PATH_ANIMALMODEL_PROPERTY_GENDER = new String[] { "TargetExperimentalGroup",
			"OrganismModel", "Gender", "Gender" };

	public static final String[] PATH_ANIMALMODEL_PROPERTY_AGECATEGORY = new String[] { "TargetExperimentalGroup",
			"OrganismModel", "AgeCategory", "AgeCategory" };

	public static final String[] PATH_ANIMALMODEL_PROPERTY_ORGANISMSPECIES = new String[] { "TargetExperimentalGroup",
			"OrganismModel", "OrganismSpecies", "OrganismSpecies" };

	public static final String[] PATH_DELIVERYMETHOD = new String[] { "TargetExperimentalGroup", "TreatmentTypes",
			"DeliveryMethod", "DeliveryMethod" };

	public static final String[] PATH_LOCATION = new String[] { "TargetExperimentalGroup", "TreatmentTypes",
			"TreatmentLocation", "Location" };

	public static final String PREFIX_URL_PUBMED = "https://www.ncbi.nlm.nih.gov/pubmed/";
}
