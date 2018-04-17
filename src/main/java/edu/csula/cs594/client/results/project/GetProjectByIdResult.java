package edu.csula.cs594.client.results.project;


import edu.csula.cs594.client.dao.model.Project;

public class GetProjectByIdResult {

    private int id;
    private Project project;
    private boolean success;

    public GetProjectByIdResult(int id, Project project) {
        this.id = id;
        this.project = project;
        this.success = project != null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
