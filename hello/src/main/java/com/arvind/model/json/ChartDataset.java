package com.arvind.model.json;

import java.math.BigDecimal;
import java.util.List;

public class ChartDataset {

	private List<BigDecimal> data;
	
	private String label;
	
	private Integer lineTension;
	
	private String backgroundColor;
	
	private String borderColor;
	
	private Integer borderWidth;
	
	private String pointBackgroundColor;

	public List<BigDecimal> getData() {
		return data;
	}

	public void setData(List<BigDecimal> data) {
		this.data = data;
	}

	public Integer getLineTension() {
		return lineTension;
	}

	public void setLineTension(Integer lineTension) {
		this.lineTension = lineTension;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(String borderColor) {
		this.borderColor = borderColor;
	}

	public Integer getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(Integer borderWidth) {
		this.borderWidth = borderWidth;
	}

	public String getPointBackgroundColor() {
		return pointBackgroundColor;
	}

	public void setPointBackgroundColor(String pointBackgroundColor) {
		this.pointBackgroundColor = pointBackgroundColor;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
}
