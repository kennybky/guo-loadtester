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
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScatterGraph implements GraphInterface {
    
    private static final Logger logger = LoggerFactory.getLogger(ScatterGraph.class);

    StatusResponse result;
    List<Series> seriesList = new ArrayList<>();
    List<Integer> rspTimes = new ArrayList<>();
    List<Integer> domainValues = new ArrayList<>();

    Category category = new Category();
    List<CategoryLabel> categoryLabels = new ArrayList<>();
    int minTime = Integer.MAX_VALUE;
    int maxTime = 0;    
    private String caption;
    private String xaxis;
    private String yaxis;
    private final StdDevGraph stdDevGraph;
    private final boolean calcStdDev;
    Set<Integer> plotXValues = new TreeSet<>();
            
    public ScatterGraph(boolean calcStdDev) {
        result = new StatusResponse();           
        category.setCategory(categoryLabels);
        stdDevGraph = new StdDevGraph(this);
        this.calcStdDev = calcStdDev;
    }

    @Override
    public ScatterGraph addSeries(Iterator<Map.Entry<Object,Object>> iterator, String seriesName) {
        
        
        double domainSum = 0.0;        
        
        Series series = new Series();
        List<SeriesData> seriesData = new ArrayList<>();
        series.setData(seriesData);        
                
        int recordCount = 0;
        while(iterator.hasNext()) {
            Map.Entry<Object,Object> e = iterator.next();
            if (null == e) {
                break;
            }
            recordCount++;
            
            final int value = (int) e.getKey();
            final int domain = (Integer) e.getValue();
            rspTimes.add(value);
            domainValues.add(domain);
            
            SeriesData data = new SeriesData();
            
            data.setX(domain + "");
            data.setY(value + "");
            domainSum = domainSum + domain;
            
            if (!plotXValues.contains(domain)) {
                plotXValues.add(domain);
            }            

            if (domain > maxTime) {
                maxTime = domain;
            }
            if (domain < minTime) {
                minTime = domain;
            }

            seriesData.add(data);            
        }
                
        if (calcStdDev) {
            final double mean = domainSum / domainValues.size();
            final double stdDev = StdDevGraph.stdDev(mean, domainValues);            
            stdDevGraph.setLabelValues(mean, stdDev, minTime, maxTime);
        } else {
//            for (int i : plotXValues) {
//                logger.info("graph x-value: " + i);
//                addLabel(i, true, null);
//            } 

            int step = (maxTime - minTime) / 5;
            for (int i = minTime; i < maxTime; i = i + step) {
                logger.info("graph x-value: " + i);
                addLabel(i, true, null);                
            }
        }
        
        series.setSeriesname(seriesName);
        series.setShowregressionline("1");
        seriesList.add(series);
        logger.info("Series named \"" + seriesName + "\" has " + recordCount + " data points");
        
        return this;    
    }
    
    @Override
    public void addLabel(int relativeTime, boolean showVerticalLine, String specialLabel) {
        CategoryLabel label = new CategoryLabel();
        label.setX((relativeTime) + "");
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
    public StatusResponse getResult() {
        Chart chart = new Chart();
        chart.setCaption(getCaption());
        chart.setxAxisName(getXaxis());
        chart.setYaxisname(getYaxis());
        chart.setTheme("fint");
        
        chart.setXaxismaxvalue(maxTime);
        chart.setXaxisminvalue(minTime);
        
        result.setChart(chart);          
        
        System.out.println("Size of rsp times in java: " + rspTimes.size());
        result.setRspTimes(rspTimes);
        result.setDataset(seriesList);
        result.setCategory(category);
         
        return result;
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
