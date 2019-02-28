package com.github.stephengardner.mother.data;

public class LogEntry {

  private final String userID;
  private final String message;
  private final String chanTimestamp;
  private final boolean original;

  public LogEntry(String userID, String message, String chanTimestamp, boolean original) {
    this.userID = userID;
    this.message = message;
    this.chanTimestamp = chanTimestamp;
    this.original = original;
  }

  public boolean isOriginal() {
    return original;
  }

  public String getMessage() {
    return message;
  }

  public String getChanTimestamp() {
    return chanTimestamp;
  }

  public String getUserID() {
    return userID;
  }
}
