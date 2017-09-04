/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.de.uni.bielefeld.sc.psink.sciexplorer.visualization;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import main.java.de.uni.bielefeld.sc.psink.sciexplorer.Configuration;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.misc.Table;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.QueryGenerator;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.QueryResult;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.RDFObject;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.SPARQLDatabase;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.query.AbstractStatement;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.utility.HTMLUtility;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.utility.StringUtility;

/**
 * Result-table generator to generate result-tables for the details-page.
 * 
 * @author ABOROWI
 */
public class ResultTableGenerator {
	private static final List<String> VARIABLES = Arrays.asList("Study", "Result", "Observation",
			"TargetExperimentalGroup", "ReferenceExperimentalGroup", "Treatment", "InjuryModel");

	public static Table generateTable(List<AbstractStatement> statements) {
		QueryResult results = SPARQLDatabase.selectWhere("?publication " + QueryGenerator.ROOT_VARIABLE,
				"?publication " + QueryGenerator.PREDICATE_TYPE + " " + QueryGenerator.prepareValue("Publication")
						+ ". " + "?publication " + QueryGenerator.prepareValue("describes") + " ?Experiment. "
						+ "?Experiment " + QueryGenerator.prepareValue("hasResult") + " " + QueryGenerator.ROOT_VARIABLE
						+ ". " + QueryGenerator.generateStatementString(statements),
				QueryGenerator.ORDER_BY_ROOTVARIABLE);

		String publication;
		Map<String, List<String>> publicationResultMap = new LinkedHashMap();
		for (List<RDFObject> resultRow : results.getData())
                {
			publication = resultRow.get(0).toString();
			publicationResultMap.putIfAbsent(publication, new LinkedList());
			publicationResultMap.get(publication).add(resultRow.get(1).toString());
		}

		Map<String, Map<String, List<String>>> tripleMap;
		List<List<String>> table = new LinkedList();
		int resultCounter;
		for (Entry<String, List<String>> entry : publicationResultMap.entrySet()) {
			tripleMap = generateTripleMap(SPARQLDatabase.selectTriplesFromResultSubgraph(entry.getKey()));
			resultCounter = 1;
			for (String result : entry.getValue()) {
				table.add(generateRow(entry.getKey(), resultCounter, result, tripleMap));
				resultCounter++;
			}
		}

		return new Table(VARIABLES, table);
	}

	private static Map<String, Map<String, List<String>>> generateTripleMap(QueryResult triples) {
		String s, p, o;
		Map<String, Map<String, List<String>>> tripleMap = new LinkedHashMap();
		for (List<RDFObject> triple : triples.getData()) {
			s = triple.get(0).toString();
			if (!tripleMap.containsKey(s)) {
				tripleMap.put(s, new LinkedHashMap());
			}
			p = optimizeValue(triple.get(1).toString());
			if (!tripleMap.get(s).containsKey(p)) {
				tripleMap.get(s).put(p, new LinkedList());
			}
			o = triple.get(2).toString();
			tripleMap.get(s).get(p).add(o);
		}
		return tripleMap;
	}

	private static List<String> generateRow(String publication, int resultNumber, String result,
			Map<String, Map<String, List<String>>> tripleMap) {
		List<String> row = new LinkedList();
		row.add(getStudyColumnValue(publication, resultNumber, tripleMap));
		row.add(getResultColumnValue(result, tripleMap));
		row.add(getObservationColumnValue(result, tripleMap));
		row.add(getTargetExperimentalGroupColumnValue(result, tripleMap));
		row.add(getReferenceExperimentalGroupColumnValue(result, tripleMap));
		row.add(getTreatmentColumnValue(result, tripleMap));
		row.add(getInjuryModelColumnValue(result, tripleMap));
		return row;
	}

	private static String getStudyColumnValue(String publication, int resultNumber,
			Map<String, Map<String, List<String>>> tripleMap) {
		List<String> lines = new LinkedList();

		lines.add(HTMLUtility.noLineBreak(
				HTMLUtility.bold("PublicationID: ") + publication.substring(publication.lastIndexOf("/") + 1)));
		addBoldPropertyLine("- PublicationYear", publication, new String[] { "hasPublicationYear" }, tripleMap, lines);
		addBoldMultiPropertyLine("- Author(s)", publication, new String[] { "hasAuthor", "hasName" }, tripleMap, lines);

		String pubmedID = getValue(publication, new String[] { "hasPubmedID" }, tripleMap);
		lines.add(HTMLUtility.noLineBreak(HTMLUtility.bold("- PubmedID: ")
				+ HTMLUtility.linkInNewTab(Configuration.PREFIX_URL_PUBMED + pubmedID, pubmedID)));

		lines.add(HTMLUtility.noLineBreak(HTMLUtility.bold("Result-Number: ") + resultNumber));

		return StringUtility.join(HTMLUtility.NEWLINE, lines);
	}

	private static String getResultColumnValue(String result, Map<String, Map<String, List<String>>> tripleMap) {
		List<String> lines = new LinkedList();
		lines.add(getBoldPropertyLine("Trend", result, new String[] { "hasTrend", QueryGenerator.PREDICATE_TYPE_VALUE },
				tripleMap));
		lines.add(getBoldPropertyLine("Judgement", result, new String[] { "hasJudgement" }, tripleMap));
		lines.add(getBoldPropertyLine("InvestigationMethod", result,
				new String[] { "hasInvestigationMethod", QueryGenerator.PREDICATE_TYPE_VALUE }, tripleMap));
		addBoldMultiPropertyLine("- MakesUseOfApparatus", result,
				new String[] { "hasInvestigationMethod", "makesUseOf", QueryGenerator.PREDICATE_TYPE_VALUE }, tripleMap,
				lines);
		return StringUtility.join(HTMLUtility.NEWLINE, lines);
	}

	private static String getObservationColumnValue(String result, Map<String, Map<String, List<String>>> tripleMap) {
		List<String> lines = new LinkedList();
		for (String observation : getValues(result, new String[] { "hasObservation" }, tripleMap)) {
			lines.add(HTMLUtility.bold(optimizeValue(observation)));
			addBoldPropertyLine("- belongsTo", observation, new String[] { "belongsTo" }, tripleMap, lines);
			addBoldPropertyLine("- hasNumericValue", observation, new String[] { "hasNumericValue" }, tripleMap, lines);
			List<String> temporalIntervals = getValues(observation, new String[] { "hasTemporalInterval" }, tripleMap);
			if (!temporalIntervals.isEmpty()) {
				lines.add(HTMLUtility.bold("- hasTemporalInterval:"));
				for (String timepoint : temporalIntervals) {
					lines.add("-- " + optimizeValue(timepoint) + ":");
					addPropertyLine("--- hasEventBefore", timepoint, new String[] { "hasEventBefore" }, tripleMap,
							lines);
					addPropertyLine("--- hasDuration", timepoint, new String[] { "hasDuration" }, tripleMap, lines);
					addPropertyLine("--- hasEventAfter", timepoint, new String[] { "hasEventAfter" }, tripleMap, lines);
				}
			}
			lines.add(HTMLUtility.NEWLINE);
		}

		return StringUtility.join(HTMLUtility.NEWLINE, lines);
	}

	private static String getTargetExperimentalGroupColumnValue(String result,
			Map<String, Map<String, List<String>>> tripleMap) {
		List<String> lines = new LinkedList();
		getExperimentalGroupColumnValue(lines, getValue(result, new String[] { "hasTargetGroup" }, tripleMap),
				tripleMap);
		return StringUtility.join(HTMLUtility.NEWLINE, lines);
	}

	private static String getReferenceExperimentalGroupColumnValue(String result,
			Map<String, Map<String, List<String>>> tripleMap) {
		List<String> lines = new LinkedList();
		getExperimentalGroupColumnValue(lines, getValue(result, new String[] { "hasReferenceGroup" }, tripleMap),
				tripleMap);
		return StringUtility.join(HTMLUtility.NEWLINE, lines);
	}

	private static void getExperimentalGroupColumnValue(List<String> lines, String experimentalGroup,
			Map<String, Map<String, List<String>>> tripleMap) {
		lines.add(HTMLUtility.bold(optimizeValue(experimentalGroup)));

		String organismModel = getValue(experimentalGroup, new String[] { "hasOrganismModel" }, tripleMap);
		addBoldPropertyLine("OrganismModel", organismModel, new String[] { QueryGenerator.PREDICATE_TYPE_VALUE },
				tripleMap, lines);
		addBoldNamedIndividualPropertyLine("- hasGender", organismModel, new String[] { "hasGender" }, tripleMap,
				lines);
		addBoldNamedIndividualPropertyLine("- hasAgeCategory", organismModel, new String[] { "hasAgeCategory" },
				tripleMap, lines);
		addBoldNamedIndividualPropertyLine("- hasSpecies", organismModel, new String[] { "hasSpecies" }, tripleMap,
				lines);
		addBoldPropertyLine("- hasWeight", organismModel, new String[] { "hasWeight" }, tripleMap, lines);
		addBoldMultiPropertyLine("GroupNames", experimentalGroup, new String[] { "hasGroupName" }, tripleMap, lines);
		addBoldPropertyLine("TotalPopulationSize", experimentalGroup, new String[] { "hasTotalPopulationSize" },
				tripleMap, lines);
		addBoldPropertyLine("hasNNumber", experimentalGroup, new String[] { "hasNNumber" }, tripleMap, lines);
		addBoldPropertyLine("hasGroupNumber", experimentalGroup, new String[] { "hasGroupNumber" }, tripleMap, lines);
		addBoldPropertyLine("hasInjuryModel", experimentalGroup, new String[] { "hasInjuryModel" }, tripleMap, lines);
		List<String> treatmentTypes = getValues(experimentalGroup, new String[] { "hasTreatmentType" }, tripleMap);
		if (!treatmentTypes.isEmpty()) {
			lines.add(HTMLUtility.bold("hasTreatmentType:"));
			for (String treatmentType : treatmentTypes) {
				lines.add("- " + optimizeValue(treatmentType));
			}
		}
	}

	private static String getTreatmentColumnValue(String result, Map<String, Map<String, List<String>>> tripleMap) {
		List<String> lines = new LinkedList();
		getTreatmentLines(lines, result, new String[] { "hasTargetGroup", "hasTreatmentType" }, tripleMap);
		getTreatmentLines(lines, result, new String[] { "hasReferenceGroup", "hasTreatmentType" }, tripleMap);
		return StringUtility.join(HTMLUtility.NEWLINE, lines);
	}

	private static void getTreatmentLines(List<String> lines, String result, String[] treatmentPath,
			Map<String, Map<String, List<String>>> tripleMap) {
		for (String treatment : getValues(result, treatmentPath, tripleMap)) {
			lines.add(HTMLUtility.bold(HTMLUtility.noLineBreak(optimizeValue(treatment) + ": " + optimizeValue(
					getValue(treatment, new String[] { QueryGenerator.PREDICATE_TYPE_VALUE }, tripleMap)))));
			addBoldPropertyLine("- hasDuration", treatment, new String[] { "hasDuration" }, tripleMap, lines);
			addBoldPropertyLine("- hasDeliveryMethod", treatment,
					new String[] { "hasDeliveryMethod", QueryGenerator.PREDICATE_TYPE_VALUE }, tripleMap, lines);
			addBoldPropertyLine("- hasInterval", treatment, new String[] { "hasInterval" }, tripleMap, lines);
			addBoldPropertyLine("- hasTreatmentLocation", treatment,
					new String[] { "hasTreatmentLocation", QueryGenerator.PREDICATE_TYPE_VALUE }, tripleMap, lines);
			addBoldPropertyLine("- hasApplicationInstrument", treatment,
					new String[] { "hasApplicationInstrument", QueryGenerator.PREDICATE_TYPE_VALUE }, tripleMap, lines);
			addBoldPropertyLine("- hasFrequency", treatment, new String[] { "hasFrequency" }, tripleMap, lines);
			addBoldNamedIndividualPropertyLine("- hasSuppliedCompound", treatment,
					new String[] { "hasSuppliedCompound", "hasCompound" }, tripleMap, lines);
			addBoldPropertyLine("- hasDosage", treatment, new String[] { "hasDosage" }, tripleMap, lines);
			lines.add(HTMLUtility.NEWLINE);
		}
	}

	private static String getInjuryModelColumnValue(String result, Map<String, Map<String, List<String>>> tripleMap) {
		List<String> lines = new LinkedList();
		getInjuryModelLines(lines, result, new String[] { "hasTargetGroup", "hasInjuryModel" }, tripleMap);
		getInjuryModelLines(lines, result, new String[] { "hasReferenceGroup", "hasInjuryModel" }, tripleMap);
		return StringUtility.join(HTMLUtility.NEWLINE, lines);
	}

	private static void getInjuryModelLines(List<String> lines, String result, String[] injuryModelPath,
			Map<String, Map<String, List<String>>> tripleMap) {
		String injuryModel = getValue(result, injuryModelPath, tripleMap);
		if (!injuryModel.isEmpty()) {
			lines.add(HTMLUtility.bold(HTMLUtility.noLineBreak(optimizeValue(injuryModel) + ": " + optimizeValue(
					getValue(injuryModel, new String[] { QueryGenerator.PREDICATE_TYPE_VALUE }, tripleMap)))));
			addBoldPropertyLine("- hasInjuryArea", injuryModel,
					new String[] { "hasInjuryArea", QueryGenerator.PREDICATE_TYPE_VALUE }, tripleMap, lines);
			addBoldPropertyLine("- hasInjuryDevice", injuryModel,
					new String[] { "hasInjuryDevice", QueryGenerator.PREDICATE_TYPE_VALUE }, tripleMap, lines);
			addBoldPropertyLine("- hasInjuryLocation", injuryModel,
					new String[] { "hasInjuryLocation", QueryGenerator.PREDICATE_TYPE_VALUE }, tripleMap, lines);
			lines.add(HTMLUtility.NEWLINE);
		}
	}

	private static String getPropertyLine(String propertyName, String key, String[] path,
			Map<String, Map<String, List<String>>> tripleMap) {
		return HTMLUtility.noLineBreak(propertyName + ": " + optimizeValue(getValue(key, path, tripleMap)));
	}

	private static void addPropertyLine(String propertyName, String key, String[] path,
			Map<String, Map<String, List<String>>> tripleMap, List<String> lines) {
		String value = getValue(key, path, tripleMap);
		if (!value.isEmpty()) {
			lines.add(HTMLUtility.noLineBreak(propertyName + ": " + optimizeValue(value)));
		}
	}

	private static String getMultiPropertyLine(String propertyName, String key, String[] path,
			Map<String, Map<String, List<String>>> tripleMap) {
		return HTMLUtility.noLineBreak(
				propertyName + ": " + StringUtility.join(", ", optimizeValues(getValues(key, path, tripleMap))));
	}

	private static void addMultiPropertyLine(String propertyName, String key, String[] path,
			Map<String, Map<String, List<String>>> tripleMap, List<String> lines) {
		List<String> values = getValues(key, path, tripleMap);
		if (!values.isEmpty()) {
			lines.add(HTMLUtility.noLineBreak(propertyName + ": " + StringUtility.join(", ", optimizeValues(values))));
		}
	}

	private static String getBoldPropertyLine(String propertyName, String key, String[] path,
			Map<String, Map<String, List<String>>> tripleMap) {
		return HTMLUtility
				.noLineBreak(HTMLUtility.bold(propertyName + ": ") + optimizeValue(getValue(key, path, tripleMap)));
	}

	private static void addBoldPropertyLine(String propertyName, String key, String[] path,
			Map<String, Map<String, List<String>>> tripleMap, List<String> lines) {
		String value = getValue(key, path, tripleMap);
		if (!value.isEmpty()) {
			lines.add(HTMLUtility.noLineBreak(HTMLUtility.bold(propertyName + ": ") + optimizeValue(value)));
		}
	}

	private static void addBoldNamedIndividualPropertyLine(String propertyName, String key, String[] path,
			Map<String, Map<String, List<String>>> tripleMap, List<String> lines) {
		String value = getValue(key, path, tripleMap);
		if (!value.isEmpty()) {
			String value2 = getValue(value, new String[] { QueryGenerator.PREDICATE_TYPE_VALUE }, tripleMap);
			if (!value2.isEmpty()) {
				value = value2;
			}
			lines.add(HTMLUtility.noLineBreak(HTMLUtility.bold(propertyName + ": ") + optimizeValue(value)));
		}
	}

	private static String getBoldMultiPropertyLine(String propertyName, String key, String[] path,
			Map<String, Map<String, List<String>>> tripleMap) {
		return HTMLUtility.noLineBreak(HTMLUtility.bold(propertyName + ": ")
				+ StringUtility.join(", ", optimizeValues(getValues(key, path, tripleMap))));
	}

	private static void addBoldMultiPropertyLine(String propertyName, String key, String[] path,
			Map<String, Map<String, List<String>>> tripleMap, List<String> lines) {
		List<String> values = getValues(key, path, tripleMap);
		if (!values.isEmpty()) {
			lines.add(HTMLUtility.noLineBreak(
					HTMLUtility.bold(propertyName + ": ") + StringUtility.join(", ", optimizeValues(values))));
		}
	}

	private static String getValue(String key, String[] path, Map<String, Map<String, List<String>>> tripleMap) {
		List<String> values = getValues(key, path, tripleMap);
		if (values.isEmpty()) {
			return "";
		}
		return values.get(0);
	}

	private static List<String> getValues(String key, String[] path, Map<String, Map<String, List<String>>> tripleMap) {
		List<String> values = new LinkedList();
		getValues(key, path, 0, tripleMap, values);
		return values;
	}

	private static void getValues(String key, String[] path, int index,
			Map<String, Map<String, List<String>>> tripleMap, List<String> values) {
		if (tripleMap.containsKey(key)) {
			String segment = path[index];
			if (tripleMap.get(key).containsKey(segment)) {
				int newIndex = index + 1;
				if (newIndex < path.length) {
					for (String child : tripleMap.get(key).get(segment)) {
						getValues(child, path, newIndex, tripleMap, values);
					}
				} else {
					values.addAll(tripleMap.get(key).get(segment));
				}
			}
		}
	}

	private static String optimizeValue(String value) {
		if (value.startsWith(Configuration.RDF_URI_PREFIX_ONTOLOGY)) {
			return value.substring(Configuration.RDF_URI_PREFIX_ONTOLOGY.length());
		} else if (value.startsWith(Configuration.RDF_URI_PREFIX_DATA)) {
			return value.substring(Configuration.RDF_URI_PREFIX_DATA.length());
		}
		return value;
	}

	private static List<String> optimizeValues(List<String> values) {
		List<String> list = new LinkedList();
		for (String value : values) {
			list.add(optimizeValue(value));
		}
		return list;
	}
}
