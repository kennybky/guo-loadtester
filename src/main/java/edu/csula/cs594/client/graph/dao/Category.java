package edu.csula.cs594.client.graph.dao;

import java.util.List;


public class Category {
    private List<CategoryLabel> category;

    public List<CategoryLabel> getCategory() {
        return category;
    }

    public void setCategory(List<CategoryLabel> category) {
        this.category = category;
    }
}
