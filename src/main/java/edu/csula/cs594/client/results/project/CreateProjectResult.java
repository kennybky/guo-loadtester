package edu.csula.cs594.client.results.project;


import edu.csula.cs594.client.dao.model.Project;

public class CreateProjectResult {
    private Project project;
    private boolean successful;

    public CreateProjectResult(int id, Project project, boolean successful) {
        this.project = project;
        this.project.setId(id);
        this.successful = successful;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}
