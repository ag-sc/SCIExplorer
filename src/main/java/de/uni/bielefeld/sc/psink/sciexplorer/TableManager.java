package main.java.de.uni.bielefeld.sc.psink.sciexplorer;

import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.primefaces.model.TreeNode;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;

import main.java.de.uni.bielefeld.sc.psink.sciexplorer.searchtree.Subclass;
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

	// investigation method -> injury type -> location -> chart
	private List<List<List<BarChartModel>>> injuryAreaData;

	// investigation method -> injury type -> delivery method -> chart
	private List<List<List<BarChartModel>>> deliveryMethodData;

	// investigation method -> injury type -> organism model -> chart
	private List<List<List<BarChartModel>>> organismModelData;

	private String[] judgements = { "Positive", "Negative", "Neutral" };
	private String[] investigationMethods = { "FunctionalTest", "NonFunctionalTest", "ImagingTest",
			"ObservationOfAnimalBehaviour" };

	private Map<String, List<Subclass>> investigationMethodsMap;

	private List<Subclass> injuryTypes;
	private List<Subclass> locations;
	private List<Subclass> deliveryMethods;
	private List<Subclass> organismModels;

	public TableManager() {
		this.injuryTypes = new ArrayList<Subclass>();
		this.locations = new ArrayList<Subclass>();
		this.deliveryMethods = new ArrayList<Subclass>();
		this.organismModels = new ArrayList<Subclass>();

		this.investigationMethodsMap = new HashMap<String, List<Subclass>>();
		for (String invMethod : this.investigationMethods) {
			this.investigationMethodsMap.put(invMethod, new ArrayList<Subclass>());
		}

		this.injuryAreaData = new ArrayList<List<List<BarChartModel>>>();
		this.deliveryMethodData = new ArrayList<List<List<BarChartModel>>>();
		this.organismModelData = new ArrayList<List<List<BarChartModel>>>();
	}

	public void update(String treatment, List<TreeNode> investigationMethodNodes, List<TreeNode> injuryTypeNodes,
			List<TreeNode> locationNodes, List<TreeNode> deliveryMethodNodes, List<TreeNode> organismModelNodes) {

		this.sortInvestigationMethods(investigationMethodNodes);

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
				this.locations.add(sc);
			}
		}

		// delivery methods
		for (TreeNode treeNode : deliveryMethodNodes) {
			Subclass sc = (Subclass) (treeNode.getData());
			this.deliveryMethods.add(sc);
		}

		// organism models
		for (TreeNode treeNode : organismModelNodes) {
			Subclass sc = (Subclass) (treeNode.getData());
			this.organismModels.add(sc);
		}

		this.queryData(treatment);
	}

	private void sortInvestigationMethods(List<TreeNode> investigationMethodNodes) {

		List<String> superClasses = Arrays.asList(this.investigationMethods);

		for (TreeNode treeNode : investigationMethodNodes) {
			// get the leave node
			Subclass investigationMethod = (Subclass) (treeNode.getData());

			TreeNode current = treeNode;

			// search for a parent
			while (true) {
				Subclass parent = (Subclass) (current.getParent().getData());

				if (superClasses.contains(parent.getName())) {
					List<Subclass> subclasses = this.investigationMethodsMap.get(parent.getName());
					subclasses.add(investigationMethod);
					break;
				}

				if (parent.getName().equals("InvestigationMethod")) {
					break;
				}
				current = current.getParent();

			}
		}
	}

	private void queryData(String treatment) {
		this.queryInjuryAreaData(treatment);
		this.queryDeliveryMethodData(treatment);
		this.queryOrganismModelData(treatment);
	}

	private void queryInjuryAreaData(String treatment) {

		int maxCount = 3;
		this.injuryAreaData.clear();

		for (String investigationMethod : investigationMethods) {
			QueryResult result = SPARQLDatabase.selectWhere("?Judgement ?InjuryType ?InjuryLocationType",
					this.buildInjuryLocationWhereStatement(treatment, investigationMethod));

			List<List<BarChartModel>> investigationMethodData = new ArrayList<List<BarChartModel>>();
			this.injuryAreaData.add(investigationMethodData);

			// go over all injury types
			for (Subclass injuryType : injuryTypes) {

				// add a new row for all locations for the current injury type
				List<BarChartModel> locationData = new ArrayList<BarChartModel>();
				investigationMethodData.add(locationData);

				// go over all locations
				for (Subclass location : locations) {

					BarChartModel barModel = this.initBarChartModel();
					locationData.add(barModel);

					// build the query and fill in the count for all three judgements
					for (String judgement : judgements) {

						int count = 0;
						for (List<RDFObject> data : result.getData()) {
							String resultJudgement = data.get(0).toStringNoPrefix();
							String resultInjuryType = data.get(1).toStringNoPrefix();
							String resultInjuryLocation = data.get(2).toStringNoPrefix();

							// find matches
							if (resultJudgement.equals(judgement) && resultInjuryLocation.equals(location.getName())
									&& resultInjuryType.equals(injuryType.getName())) {
								count++;
							}

						}

						ChartSeries series = new ChartSeries();
						series.set("Judgement", count);
						series.setLabel(judgement);
						barModel.addSeries(series);

						maxCount = Math.max(count, maxCount);

					}
				}
			}
		}
		this.makeEvenScales(injuryAreaData, maxCount);
	}

	private void queryDeliveryMethodData(String treatment) {

		int maxCount = 3;
		this.deliveryMethodData.clear();

		for (String investigationMethod : investigationMethods) {

			QueryResult result = SPARQLDatabase.selectWhere("?Judgement ?InjuryType ?DeliveryMethodType",
					this.buildDelilveryMethodWhereStatement(treatment, investigationMethod));

			List<List<BarChartModel>> investigationMethodData = new ArrayList<List<BarChartModel>>();
			this.deliveryMethodData.add(investigationMethodData);

			// go over all delivery methods
			for (Subclass injuryType : injuryTypes) {
				// System.out.println(injuryType.getName() + ":");

				// add a new row for all methods for the current injury type
				List<BarChartModel> deliveryMethodData = new ArrayList<BarChartModel>();
				investigationMethodData.add(deliveryMethodData);

				// go over all locations
				for (Subclass deliveryMethod : deliveryMethods) {

					BarChartModel barModel = this.initBarChartModel();
					deliveryMethodData.add(barModel);

					// System.out.println("\t" + location.getName() + ":");

					// build the query and fill in the count for all three judgements
					for (String judgement : judgements) {

						int count = 0;
						for (List<RDFObject> data : result.getData()) {
							String resultJudgement = data.get(0).toStringNoPrefix();
							String resultInjuryType = data.get(1).toStringNoPrefix();
							String resultDeliveryMethodType = data.get(2).toStringNoPrefix();

							// find matches
							if (resultJudgement.equals(judgement)
									&& resultDeliveryMethodType.equals(deliveryMethod.getName())
									&& resultInjuryType.equals(injuryType.getName())) {
								count++;
							}
						}
						ChartSeries series = new ChartSeries();
						series.set("Judgement", count);
						series.setLabel(judgement);
						barModel.addSeries(series);

						maxCount = Math.max(count, maxCount);

					}
				}
			}
		}
		this.makeEvenScales(deliveryMethodData, maxCount);
	}

	private void queryOrganismModelData(String treatment) {

		int maxCount = 3;
		this.organismModelData.clear();

		for (String investigationMethod : investigationMethods) {

			QueryResult result = SPARQLDatabase.selectWhere("?Judgement ?InjuryType ?OrganismModelType",
					this.buildOrganismModelWhereStatement(treatment, investigationMethod));

			List<List<BarChartModel>> investigationMethodData = new ArrayList<List<BarChartModel>>();
			this.organismModelData.add(investigationMethodData);

			// go over all delivery methods
			for (Subclass injuryType : injuryTypes) {
				// System.out.println(injuryType.getName() + ":");

				// add a new row for all methods for the current injury type
				List<BarChartModel> organismModelData = new ArrayList<BarChartModel>();
				investigationMethodData.add(organismModelData);

				for (Subclass organismModel : organismModels) {

					BarChartModel barModel = this.initBarChartModel();
					organismModelData.add(barModel);

					// System.out.println("\t" + location.getName() + ":");

					// build the query and fill in the count for all three judgements
					for (String judgement : judgements) {

						int count = 0;
						for (List<RDFObject> data : result.getData()) {
							String resultJudgement = data.get(0).toStringNoPrefix();
							String resultInjuryType = data.get(1).toStringNoPrefix();
							String resultOrganismModelType = data.get(2).toStringNoPrefix();

							// find matches
							if (resultJudgement.equals(judgement)
									&& resultOrganismModelType.equals(organismModel.getName())
									&& resultInjuryType.equals(injuryType.getName())) {
								count++;
							}
						}

						ChartSeries series = new ChartSeries();
						series.set("Judgement", count);
						series.setLabel(judgement);
						barModel.addSeries(series);

						maxCount = Math.max(count, maxCount);

					}
				}
			}
		}
		this.makeEvenScales(organismModelData, maxCount);
	}

	private void makeEvenScales(List<List<List<BarChartModel>>> charts, int max) {
		for (List<List<BarChartModel>> list1 : charts) {
			for (List<BarChartModel> list2 : list1) {
				for (BarChartModel chart : list2) {
					chart.getAxis(AxisType.Y).setMax(max);
					chart.getAxis(AxisType.Y).setMin(0);
					chart.getAxis(AxisType.Y).setTickFormat("%d");
				}
			}
		}
	}
	
	private String buildUnionForInvestigationMethod(String investigationMethod) {
		String union = "";
		List<Subclass> subclasses = this.investigationMethodsMap.get(investigationMethod);
		for (Subclass subclass : subclasses) {
			if (!union.isEmpty()) {
				union += " UNION ";
			}
			union += "{ ";
			union += "?InvestigationMethod <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/"
					+ subclass.getName() + ">. } ";
		}

		// investigation method itself
		if (!union.isEmpty()) {
			union += " UNION ";
		}
		union += "{ ";
		union += "?InvestigationMethod <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/"
				+ investigationMethod + ">. } ";

		return union;
	}

	private String buildInjuryLocationWhereStatement(String treatment, String investigationMethod) {
		String where = "?Result <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/Result>. "
				+ "?Result <http://psink.de/scio/hasJudgement>  ?Judgement. "
				+ "?Result <http://psink.de/scio/hasInvestigationMethod> ?InvestigationMethod. "
				+ this.buildUnionForInvestigationMethod(investigationMethod)
				+ "?Result <http://psink.de/scio/hasTargetGroup> ?TargetExperimentalGroup. "
				+ "?TargetExperimentalGroup <http://psink.de/scio/hasInjuryModel> ?InjuryModel. "
				+ "?InjuryModel <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?InjuryType. "
				+ "?InjuryModel <http://psink.de/scio/hasInjuryLocation> ?InjuryLocation. "
				+ "?InjuryLocation <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?InjuryLocationType. ";

		if (treatment != null && !treatment.isEmpty()) {
			where += "?TargetExperimentalGroup <http://psink.de/scio/hasTreatmentType> ?TreatmentTypes. "
					+ "?TreatmentTypes <http://psink.de/scio/hasCompound> ?Compound. "
					+ "?Compound <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/" + treatment
					+ ">.";
		}
		return where;
	}

	private String buildDelilveryMethodWhereStatement(String treatment, String investigationMethod) {
		String where = "?Result <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/Result>. "
				+ "?Result <http://psink.de/scio/hasJudgement>  ?Judgement. "
				+ "?Result <http://psink.de/scio/hasInvestigationMethod> ?InvestigationMethod. "
				+ this.buildUnionForInvestigationMethod(investigationMethod)
				+ "?Result <http://psink.de/scio/hasTargetGroup> ?TargetExperimentalGroup. "
				+ "?TargetExperimentalGroup <http://psink.de/scio/hasInjuryModel> ?InjuryModel. "
				+ "?InjuryModel <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?InjuryType. "
				+ "?TargetExperimentalGroup <http://psink.de/scio/hasTreatmentType> ?TreatmentTypes. "
				+ "?TreatmentTypes <http://psink.de/scio/hasDeliveryMethod> ?DeliveryMethod. "
				+ "?DeliveryMethod <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?DeliveryMethodType. ";
		if (treatment != null && !treatment.isEmpty()) {
			where += "?TreatmentTypes <http://psink.de/scio/hasCompound> ?Compound. "
					+ "?Compound <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/" + treatment
					+ ">.";
		}
		return where;
	}

	private String buildOrganismModelWhereStatement(String treatment, String investigationMethod) {
		String where = "?Result <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/Result>. "
				+ "?Result <http://psink.de/scio/hasJudgement>  ?Judgement. "
				+ "?Result <http://psink.de/scio/hasInvestigationMethod> ?InvestigationMethod. "
				+ this.buildUnionForInvestigationMethod(investigationMethod)
				+ "?Result <http://psink.de/scio/hasTargetGroup> ?TargetExperimentalGroup. "
				+ "?TargetExperimentalGroup <http://psink.de/scio/hasInjuryModel> ?InjuryModel. "
				+ "?InjuryModel <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?InjuryType. "
				+ "?TargetExperimentalGroup <http://psink.de/scio/hasOrganismModel> ?OrganismModel. "
				+ "?OrganismModel <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?OrganismModelType. ";

		if (treatment != null && !treatment.isEmpty()) {
			where += "?TargetExperimentalGroup <http://psink.de/scio/hasTreatmentType> ?TreatmentTypes. "
					+ "?TreatmentTypes <http://psink.de/scio/hasCompound> ?Compound. "
					+ "?Compound <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/" + treatment
					+ ">.";
		}
		return where;
	}

	/*
	 * private String buildInjuryLocationWhereStatement(String treatment, String
	 * judgement, String investigationMethod, Subclass injuryType, Subclass
	 * injuryLocation) { String where =
	 * "?Result <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/Result>. "
	 * + "?Result <http://psink.de/scio/hasJudgement> <http://psink.de/scio/" +
	 * judgement + ">. " +
	 * "?Result <http://psink.de/scio/hasInvestigationMethod> ?InvestigationMethod. "
	 * + this.buildUnionForInvestigationMethod(investigationMethod) +
	 * "?Result <http://psink.de/scio/hasTargetGroup> ?TargetExperimentalGroup. " +
	 * "?TargetExperimentalGroup <http://psink.de/scio/hasInjuryModel> ?InjuryModel. "
	 * +
	 * "?InjuryModel <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/"
	 * + injuryType.getName() + ">. " +
	 * "?InjuryModel <http://psink.de/scio/hasInjuryLocation> <http://psink.de/scio/"
	 * + injuryLocation.getName() + "> . ";
	 * 
	 * if (treatment != null && !treatment.isEmpty()) { where +=
	 * "?TargetExperimentalGroup <http://psink.de/scio/hasTreatmentType> ?TreatmentTypes. "
	 * + "?TreatmentTypes <http://psink.de/scio/hasCompound> ?Compound. " +
	 * "?Compound <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/"
	 * + treatment + ">."; } return where; }
	 */
	/*
	 * private String buildDelilveryMethodWhereStatement(String treatment, String
	 * judgement, String investigationMethod, Subclass injuryType, Subclass
	 * deliveryMethod) { String where =
	 * "?Result <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/Result>. "
	 * + "?Result <http://psink.de/scio/hasJudgement> <http://psink.de/scio/" +
	 * judgement + ">. " +
	 * "?Result <http://psink.de/scio/hasInvestigationMethod> ?InvestigationMethod. "
	 * + this.buildUnionForInvestigationMethod(investigationMethod) +
	 * "?Result <http://psink.de/scio/hasTargetGroup> ?TargetExperimentalGroup. " +
	 * "?TargetExperimentalGroup <http://psink.de/scio/hasInjuryModel> ?InjuryModel. "
	 * +
	 * "?InjuryModel <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/"
	 * + injuryType.getName() + ">. " +
	 * "?TargetExperimentalGroup <http://psink.de/scio/hasTreatmentType> ?TreatmentTypes. "
	 * +
	 * "?TreatmentTypes <http://psink.de/scio/hasDeliveryMethod> ?DeliveryMethod. "
	 * +
	 * "?DeliveryMethod <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/"
	 * + deliveryMethod.getName() + ">. "; if (treatment != null &&
	 * !treatment.isEmpty()) { where +=
	 * "?TreatmentTypes <http://psink.de/scio/hasCompound> ?Compound. " +
	 * "?Compound <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/"
	 * + treatment + ">."; } return where; }
	 */

	/*
	 * private String buildOrganismModelWhereStatement(String treatment, String
	 * judgement, String investigationMethod, Subclass injuryType, Subclass
	 * organismModel) { String where =
	 * "?Result <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/Result>. "
	 * + "?Result <http://psink.de/scio/hasJudgement> <http://psink.de/scio/" +
	 * judgement + ">. " +
	 * "?Result <http://psink.de/scio/hasInvestigationMethod> ?InvestigationMethod. "
	 * + this.buildUnionForInvestigationMethod(investigationMethod) +
	 * "?Result <http://psink.de/scio/hasTargetGroup> ?TargetExperimentalGroup. " +
	 * "?TargetExperimentalGroup <http://psink.de/scio/hasInjuryModel> ?InjuryModel. "
	 * +
	 * "?InjuryModel <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/"
	 * + injuryType.getName() + ">. " +
	 * "?TargetExperimentalGroup <http://psink.de/scio/hasOrganismModel> ?OrganismModel. "
	 * +
	 * "?OrganismModel <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/"
	 * + organismModel.getName() + ">. ";
	 * 
	 * if (treatment != null && !treatment.isEmpty()) { where +=
	 * "?TargetExperimentalGroup <http://psink.de/scio/hasTreatmentType> ?TreatmentTypes. "
	 * + "?TreatmentTypes <http://psink.de/scio/hasCompound> ?Compound. " +
	 * "?Compound <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://psink.de/scio/"
	 * + treatment + ">."; } return where; }
	 */

	private BarChartModel initBarChartModel() {

		BarChartModel model = new BarChartModel();
		// model.setLegendPosition("ne");

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

	public List<Subclass> getDeliveryMethods() {
		return deliveryMethods;
	}

	public List<Subclass> getOrganismModels() {
		return organismModels;
	}

	public List<List<List<BarChartModel>>> getInjuryAreaData() {
		return injuryAreaData;
	}

	public List<List<List<BarChartModel>>> getDeliveryMethodData() {
		return deliveryMethodData;
	}

	public List<List<List<BarChartModel>>> getOrganismModelData() {
		return organismModelData;
	}
}
