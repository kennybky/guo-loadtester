package edu.csula.cs594.client.graph.dao;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CategoryLabel implements Comparable<CategoryLabel> {
    private String label;
    private String x;
    private String showverticalline;
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    @Override
    public int compareTo(CategoryLabel o) {
        if (null == label || null == o) {
            throw new IllegalArgumentException("this CategoryLabel is null");
        }
        return this.label.compareTo(o.label);
    }
    

    @Override
    public int hashCode() {
        return new HashCodeBuilder(32932, 392).
            append(label).            
            toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
       if (!(obj instanceof CategoryLabel))
            return false;
        if (obj == this)
            return true;

        CategoryLabel rhs = (CategoryLabel) obj;
        return new EqualsBuilder().            
            append(label, rhs.label).
            isEquals();
    }    

    public String getShowverticalline() {
        return showverticalline;
    }

    public void setShowverticalline(String showverticalline) {
        this.showverticalline = showverticalline;
    }
    
}
