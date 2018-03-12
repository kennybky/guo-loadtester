package edu.csula.cs594.client.graph;

import edu.csula.cs594.client.dao.StatusResponse;
import edu.csula.cs594.client.graph.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by James on 11/24/16.
 */
public class ScrollLineGraph implements GraphInterface {

    private static final Logger logger = LoggerFactory.getLogger(ScrollLineGraph.class);

    private final StatusResponse result;
    private final List<Series> seriesList = new ArrayList<>();
    private final List<Category> categoriesList = new ArrayList<>();
    private final Category category = new Category();
    private final List<CategoryLabel> categoryLabels = new ArrayList<>();

    private String caption;
    private String xaxis;
    private String yaxis;

    public ScrollLineGraph() {
        result = new StatusResponse();
        category.setCategory(categoryLabels);
        categoriesList.add(category);
    }

    @Override
    public ScrollLineGraph addSeries(Iterator<Map.Entry<Object, Object>> iterator, String seriesName) {
        Series series = new Series();
        List<SeriesData> seriesData = new ArrayList<>();
        series.setData(seriesData);
        seriesList.add(series);
        while (iterator.hasNext()) {
            Map.Entry<Object, Object> e = iterator.next();
            if (null == e) {
                continue;
            }

            final Date timeStamp = (Date) e.getKey();
            final double avgResponseTime = (double) e.getValue();
//            logger.info("Time: " + timeStamp);
//            logger.info("avg: " + avgResponseTime);

            CategoryLabel label = new CategoryLabel();
            String formattedLabel = new SimpleDateFormat("MM/dd/yyyy, hh:mm:ss aaa").format(timeStamp);
            label.setLabel(formattedLabel);
            categoryLabels.add(label);
            SeriesData data = new SeriesData();
            data.setValue(avgResponseTime + "");
            seriesData.add(data);
        }
        series.setSeriesname(seriesName);

        return this;
    }

    @Override
    public StatusResponse getResult() {
        FusionChart fusionChart = new FusionChart();
        fusionChart.setType("scrollline2d");
        fusionChart.setDataFormat("json");
        fusionChart.setRenderAt("chart-container");
        fusionChart.setWidth("100%");
        fusionChart.setHeight("400");
        DataSource dataSource = new DataSource();

        Chart chart = new Chart();
        chart.setCaption(getCaption());
        chart.setxAxisName(getXaxis());
        chart.setYaxisname(getYaxis());
        chart.setShowValues("0");
        chart.setShowBorder("0");
        chart.setBgColor("#ffffff");
        chart.setPaletteColors("#008ee4");
        chart.setShowCanvasBorder("0");
        chart.setShowAlternateHGridColor("0");
        chart.setDivlineAlpha("100");
        chart.setDivlineThickness("1");
        chart.setDivLineIsDashed("1");
        chart.setDivLineDashLen("1");
        chart.setDivLineGapLen("1");
        chart.setLineThickness("3");
        chart.setFlatScrollBars("1");
        chart.setScrollHeight("10");
        chart.setNumVisiblePlot("12");
        chart.setShowHoverEffect("1");
        dataSource.setChart(chart);
        dataSource.setCategories(categoriesList);
        dataSource.setDataset(seriesList);

        fusionChart.setDataSource(dataSource);
        result.setFusionChart(fusionChart);

        return result;
    }

    @Override
    public void addLabel(int relativeTime, boolean showVerticalLine, String specialLabel) {
        CategoryLabel label = new CategoryLabel();
        if (specialLabel == null) {
            label.setLabel((relativeTime) + "");
        } else {
            label.setLabel(specialLabel);
        }

        if (showVerticalLine) {
            label.setShowverticalline("1");
        }

        if (!categoryLabels.contains(label)) {
            categoryLabels.add(label);
        }
    }

    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public void setCaption(String caption) {
        this.caption = caption;
    }

    @Override
    public String getXaxis() {
        return xaxis;
    }

    @Override
    public void setXaxis(String xaxis) {
        this.xaxis = xaxis;
    }

    @Override
    public String getYaxis() {
        return yaxis;
    }

    @Override
    public void setYaxis(String yaxis) {
        this.yaxis = yaxis;
    }
}
