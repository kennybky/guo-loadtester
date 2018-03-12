package edu.csula.cs594.client.graph.dao;

/**
 * TODO: Implement FusionChart JSON as a Java object so that we can
 * switch the way each graph renders easily.
 */
public class FusionChart {
    private String type;
    private String renderAt;
    private String width;
    private String height;
    private String dataFormat;
    private DataSource dataSource;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRenderAt() {
        return renderAt;
    }

    public void setRenderAt(String renderAt) {
        this.renderAt = renderAt;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
