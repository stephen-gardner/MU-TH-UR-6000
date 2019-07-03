package com.github.stephengardner.mother.data;

public class ConvInfo {

  private String threadID;
  private String userID;
  private String timestamp;

  public ConvInfo(String threadID, String userID, String timestamp) {
    this.threadID = threadID;
    this.userID = userID;
    this.timestamp = timestamp;
  }

  public String getThreadID() {
    return threadID;
  }

  public String getUserID() {
    return userID;
  }

  public String getTimestamp() {
    return timestamp;
  }
}
