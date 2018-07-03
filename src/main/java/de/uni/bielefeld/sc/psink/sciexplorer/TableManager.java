package main.java.de.uni.bielefeld.sc.psink.sciexplorer;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.primefaces.model.TreeNode;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;

import main.java.de.uni.bielefeld.sc.psink.sciexplorer.searchtree.Subclass;
import main.java.de.uni.bielefeld.sc.psink.sciexplorer.sparql.SPARQLDatabase;

/**
 * TableManager provides the data for the tables.xhtml page
 * 
 * @author Maik Fruhner
 */

public class TableManager {

	/** data **/

	private List<List<BarChartModel>> functionalInjuryAreaData;

	private String[] judgements = { "Positive", "Negative", "Neutral" };
	private List<Subclass> injuryTypes;
	private List<Subclass> locations;

	private List<Subclass> functionalTests;

	public TableManager() {
		this.injuryTypes = new ArrayList<Subclass>();
		this.locations = new ArrayList<Subclass>();
		this.functionalTests = new ArrayList<Subclass>();

		this.functionalInjuryAreaData = new ArrayList<List<BarChartModel>>();
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

		// functional injury area
		this.functionalInjuryAreaData.clear();

		// go over all injury types
		for (Subclass injuryType : injuryTypes) {
			System.out.println(injuryType.getName() + ":");

			// add a new row for all locations for the current injury type
			List<BarChartModel> locationData = new ArrayList<BarChartModel>();
			this.functionalInjuryAreaData.add(locationData);

			// go over all locations
			for (Subclass location : locations) {

				// create PieChartModels from data

				BarChartModel barModel = this.initBarChartModel();
				locationData.add(barModel);

				//boolean hasData = false;

				System.out.println("\t" + location.getName() + ":");

				// build the query and fill in the count for all three judgements
				for (String judgement : judgements) {
					String where = "?Result <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/Result>. "
							+ "?Result <http://psink.de/scio/hasJudgement> <http://psink.de/scio/" + judgement + ">. "
							+ "?Result <http://psink.de/scio/hasTargetGroup> ?TargetExperimentalGroup. "
							+ "?TargetExperimentalGroup <http://psink.de/scio/hasInjuryModel> ?InjuryModel. "
							+ "?InjuryModel <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/"
							+ injuryType.getName() + ">. "
							+ "?InjuryModel <http://psink.de/scio/hasInjuryLocation> <http://psink.de/scio/"
							+ location.getName() + "> .";

					int count = SPARQLDatabase.countWhere(where);
					System.out.println("\t\t" + judgement + ": " + count);

					ChartSeries series = new ChartSeries();
					series.set("Judgement", count);
					series.setLabel(judgement);
					barModel.addSeries(series);
					
					/*if (count != 0) {
						hasData = true;
					}*/
				}
				/*if (hasData) {
					// only add if there are non zero values
					locationData.add(barModel);
				} else {
					// else add null and display a message
					locationData.add(null);
				}*/
			}
		}
	}

	private BarChartModel initBarChartModel() {

		BarChartModel model = new BarChartModel();
		//model.setLegendPosition("ne");
		
		model.setDatatipFormat("%2$d");

		Axis yAxis = model.getAxis(AxisType.Y);
		yAxis.setLabel("Count");
        
		model.setSeriesColors(Configuration.BARCHART_COLOR_STRING);

		return model;
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

	public List<List<BarChartModel>> getFunctionalInjuryAreaData() {
		return functionalInjuryAreaData;
	}
}
