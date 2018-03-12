package edu.csula.cs594.client.graph;

import edu.csula.cs594.client.dao.StatusResponse;
import edu.csula.cs594.client.graph.dao.Category;
import edu.csula.cs594.client.graph.dao.CategoryLabel;
import edu.csula.cs594.client.graph.dao.Chart;
import edu.csula.cs594.client.graph.dao.Series;
import edu.csula.cs594.client.graph.dao.SeriesData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LineGraph implements GraphInterface {
    
    private static final Logger logger = LoggerFactory.getLogger(LineGraph.class);

    private final StatusResponse result;
    private final List<Series> seriesList = new ArrayList<>();

    private final Category category = new Category();
    private final List<CategoryLabel> categoryLabels = new ArrayList<>();
    
    private final Set<Integer> plotXValues = new TreeSet<>();

    int minTime = Integer.MAX_VALUE;
    int maxTime = 0;        
    private String caption;
    private String xaxis;
    private String yaxis;    
            
    public LineGraph() {        
                
        result = new StatusResponse();           
        category.setCategory(categoryLabels);        
               
    }

    @Override
    public LineGraph addSeries(Iterator<Map.Entry<Object,Object>> iterator, String seriesName) {

        Series series = new Series();
        List<SeriesData> seriesData = new ArrayList<>();
        series.setData(seriesData);

        int recordCount = 0;
        while(iterator.hasNext()) {
            Entry<Object,Object> e = iterator.next();
            recordCount++;
            if (null == e) {
                logger.info("LineGraph.addSeries has a null data element, early-out after " + recordCount + " iterations");
                continue;
            }
            final int relativeTime = (int) e.getKey();
            final int duration = (Integer) e.getValue();
            logger.info("" + relativeTime + "ms: " + duration);

            SeriesData data = new SeriesData();

            // After
            data.setLabel(relativeTime + "");
            data.setValue(duration + "");

            if (!plotXValues.contains(relativeTime)) {
                plotXValues.add(relativeTime);
            }

            if (relativeTime > maxTime) {
                maxTime = relativeTime;
            }
            if (relativeTime < minTime) {
                minTime = relativeTime;
            }

            seriesData.add(data);

        }

        series.setSeriesname(seriesName);
        seriesList.add(series);

        for (int i : plotXValues) {
            logger.info("graph x-value: " + i);
            addLabel(i, true, null);
        }

        return this;
    }

    @Override
    public StatusResponse getResult() {

        Chart chart = new Chart();
        chart.setCaption(getCaption());
        chart.setxAxisName(getXaxis());
        chart.setYaxisname(getYaxis());
        chart.setTheme("fint");
        
        chart.setXaxismaxvalue(maxTime);
        chart.setXaxisminvalue(minTime);     
        
        result.setChart(chart);
        
        result.setDataset(seriesList);
        result.setCategory(category);
        
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
