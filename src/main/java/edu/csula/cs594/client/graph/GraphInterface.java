package edu.csula.cs594.client.graph;

import edu.csula.cs594.client.dao.StatusResponse;

import java.util.Iterator;
import java.util.Map;

public interface GraphInterface {

    public Object addSeries(Iterator<Map.Entry<Object,Object>> data, String seriesName);

    public void addLabel(int relativeTime, boolean showVerticalLine, String specialLabel);
    
    public StatusResponse getResult();
    
    public String getCaption();

    public void setCaption(String caption);

    public String getXaxis();

    public void setXaxis(String xaxis);

    public String getYaxis();

    public void setYaxis(String yaxis);
    
}
