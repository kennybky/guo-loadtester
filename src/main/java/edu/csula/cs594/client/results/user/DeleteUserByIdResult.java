package edu.csula.cs594.client.results.user;

public class DeleteUserByIdResult {
  private int id;
  private boolean successful;

  public DeleteUserByIdResult(int id, boolean successful) {
    this.id = id;
    this.successful = successful;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public boolean isSuccessful() {
    return successful;
  }

  public void setSuccessful(boolean successful) {
    this.successful = successful;
  }
}
