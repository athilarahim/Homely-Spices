package com.firstProject.Homely_Spices.DTO;
import com.firstProject.Homely_Spices.model.ChartType;

import java.util.Map;

public class ChartDataDTO {

    private ChartType chartType;

    private Map<String,Integer> values;

    public ChartType getChartType() {
        return chartType;
    }

    public void setChartType(ChartType chartType) {
        this.chartType = chartType;
    }

    public Map<String, Integer> getValues() {
        return values;
    }

    public void setValues(Map<String, Integer> values) {
        this.values = values;
    }
}
