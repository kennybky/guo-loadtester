package edu.csula.cs594.client.graph;

import edu.csula.cs594.client.graph.dao.AngularChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by jsunthon on 11/12/2016.
 */
public class AngularLineGraph {

    private static final Logger logger = LoggerFactory.getLogger(AngularLineGraph.class);
    private AngularChart chart;
    private Iterator<Map.Entry<Object, Object>> dataIterator;
    private final String labelType;
    private final String valueType;

    public AngularLineGraph(AngularChart chart, Iterator<Map.Entry<Object, Object>> iterator,
                            String labelType, String valueType) {
        this.chart = chart;
        this.dataIterator = iterator;
        this.labelType = labelType;
        this.valueType = valueType;
    }

    public void populateChart() {

        List<Number> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();

        while (dataIterator.hasNext()) {
            Map.Entry<Object, Object> e = dataIterator.next();
            if (null == e) {
                break;
            }

            if (e.getKey() == null) {
                logger.info("null key");
            } else {
                Number key = null;
                switch (labelType) {
                    case "integer":
                        key = (Integer) e.getKey();
                        break;
                    case "double":
                        key = (Double) e.getKey();
                        break;
                    default:
                        logger.info("Couldn't determine label type.");
                        break;
                }

                if (key != null) {
                    labels.add(key);
                }
            }

            if (e.getValue() == null) {
                logger.info("null value");
            } else {
                Number value = null;

                switch (valueType) {
                    case "integer":
                        value = (Integer) e.getValue();
                        break;
                    case "double":
                        value = (Double) e.getValue();
                        break;
                    default:
                        logger.info("Couldn't determine value type.");
                        break;
                }

                if (value != null) {
                    data.add(value);
                }
            }
        }

        chart.setLabels(labels);
        chart.setData(data);
    }

    public AngularChart getChart() {
        return chart;
    }

    public void setChart(AngularChart chart) {
        this.chart = chart;
    }

    public Iterator<Map.Entry<Object, Object>> getDataIterator() {
        return dataIterator;
    }

    public void setDataIterator(Iterator<Map.Entry<Object, Object>> dataIterator) {
        this.dataIterator = dataIterator;
    }
}
