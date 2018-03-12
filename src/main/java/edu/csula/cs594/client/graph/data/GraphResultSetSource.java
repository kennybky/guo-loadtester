package edu.csula.cs594.client.graph.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphResultSetSource<K extends Integer, V extends Integer> implements Iterator<Map.Entry<K, V>> {

    private static final Logger logger = LoggerFactory.getLogger(GraphResultSetSource.class);
    private final ResultSet resultSet;
    private boolean done = false;
    private boolean labelIsString;
    private String labelType;
    private String valueType;

    public GraphResultSetSource(ResultSet resultSet, boolean labelIsString) {
        this.resultSet = resultSet;
        this.labelIsString = labelIsString;
    }

    public GraphResultSetSource(ResultSet resultSet) {
        this(resultSet, false);
    }

    public GraphResultSetSource(ResultSet resultSet, String labelType, String valueType) {
        this(resultSet, false);
        this.labelType = labelType;
        this.valueType = valueType;
    }

    @Override
    public boolean hasNext() {
        return !done;
    }

    @Override
    public Map.Entry<K, V> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        Map.Entry<K, V> e;
        try {
            boolean hasNext = resultSet.next();
            if (hasNext) {
                if ("timestamp".equals(labelType) && "double".equals(valueType)) {                
                    e = new HashMap.SimpleEntry(resultSet.getTimestamp(1), resultSet.getDouble(2));
                } else if ("integer".equals(labelType) && "double".equals(valueType)) {    
                    e = new HashMap.SimpleEntry(resultSet.getInt(1), resultSet.getDouble(2));
                } else {
                    if (labelIsString) {
                        e = new HashMap.SimpleEntry(resultSet.getInt(1), resultSet.getString(2));
                    } else {
                        e = new HashMap.SimpleEntry(resultSet.getInt(1), resultSet.getInt(2));
                    }
                }
            } else {
                done = true;
                e = null;
            }
        } catch (SQLException ex) {
            e = null;
            logger.error("Unable to get database result", ex);
        }

        return e;
    }

}
