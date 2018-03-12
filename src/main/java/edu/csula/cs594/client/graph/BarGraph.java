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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BarGraph implements GraphInterface {
    
    private static final Logger logger = LoggerFactory.getLogger(BarGraph.class);

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
    
    private final Series series = new Series();
    private final List<SeriesData> seriesData = new ArrayList<>();
    
            
    public BarGraph(boolean calcStdDev) {
        result = new StatusResponse();           
        category.setCategory(categoryLabels);
        stdDevGraph = new StdDevGraph(this);
        this.calcStdDev = calcStdDev;
    }

    @Override
    public BarGraph addSeries(Iterator<Map.Entry<Object,Object>> iterator, String seriesName) {
        
        
        double domainSum = 0.0;        
        
        series.setData(seriesData);        
                
        int recordCount = 0;
        while(iterator.hasNext()) {
            Map.Entry<Object,Object> e = iterator.next();
            if (null == e) {
                break;
            }
            recordCount++;
            
            final int value = (int) e.getKey();
            final String domain = e.getValue() + "";
            rspTimes.add(value);
            
            
            SeriesData data = new SeriesData();
            
            data.setLabel(domain + "");
            data.setValue(value + "");

            seriesData.add(data);            
        }
                                
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
                
        result.setChart(chart);          
        
        System.out.println("Size of rsp times in java: " + rspTimes.size());
        result.setRspTimes(rspTimes);
        
        result.setSeries(series);
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
