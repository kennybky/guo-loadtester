package edu.csula.cs594.client.graph.dao;

import java.util.List;

public class DataSource {
    
    private Chart chart;    
    private List<Category> categories;        
    private List<Series> dataset;

    public Chart getChart() {
        return chart;
    }

    public void setChart(Chart chart) {
        this.chart = chart;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Series> getDataset() {
        return dataset;
    }

    public void setDataset(List<Series> dataset) {
        this.dataset = dataset;
    }
    
}
