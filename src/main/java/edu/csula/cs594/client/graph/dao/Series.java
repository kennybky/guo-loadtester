package edu.csula.cs594.client.graph.dao;

import java.util.List;

public class Series {
    private String seriesname;
    private List<SeriesData> data;
    private String showregressionline;

    public String getSeriesname() {
        return seriesname;
    }

    public void setSeriesname(String seriesname) {
        this.seriesname = seriesname;
    }

    public List<SeriesData> getData() {
        return data;
    }

    public void setData(List<SeriesData> data) {
        this.data = data;
    }

    public String getShowregressionline() {
        return showregressionline;
    }

    public void setShowregressionline(String showregressionline) {
        this.showregressionline = showregressionline;
    }
    
}
