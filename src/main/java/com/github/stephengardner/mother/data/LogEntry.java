package com.github.stephengardner.mother.data;

public class LogEntry {

  private final String userID;
  private final String message;
  private final String timestamp;
  private final boolean original;

  public LogEntry(String userID, String message, String timestamp, boolean original) {
    this.userID = userID;
    this.message = message;
    this.timestamp = timestamp;
    this.original = original;
  }

  public boolean isOriginal() {
    return original;
  }

  public String getMessage() {
    return message;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public String getUserID() {
    return userID;
  }
}
