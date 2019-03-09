package com.github.stephengardner.mother.data;

public class ThreadInfo {

  private String threadID;
  private String userID;
  private String timestamp;

  public ThreadInfo(String threadID, String userID, String timestamp) {
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
