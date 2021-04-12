package com.arvind.model.json;

import java.util.List;

public class ChartData {

	private List<String> labels;
	private List<ChartDataset> datasets;
	
	public List<ChartDataset> getDatasets() {
		return datasets;
	}
	public void setDatasets(List<ChartDataset> datasets) {
		this.datasets = datasets;
	}
	public List<String> getLabels() {
		return labels;
	}
	public void setLabels(List<String> labels) {
		this.labels = labels;
	}
}
