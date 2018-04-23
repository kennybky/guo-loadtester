package edu.csula.cs594.client.results;

public class GenericResult {
    private boolean successful;
    private int id;

    public boolean isSuccessful() {
        return successful;
    }

    public GenericResult(boolean successful, int id) {
        this.successful = successful;
        this.id = id;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
