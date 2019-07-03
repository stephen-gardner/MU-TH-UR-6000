package com.github.stephengardner.mother.data;

import java.util.HashMap;

public class MotherConfig {

  private String authToken;
  private String convChannelID;
  private String dbPath;
  private long sessionTimeout;
  private long timeoutCheckInterval;
  private int threadsPerPage;
  private HashMap<String, String> lang;

  public boolean isValid() {
    if (authToken == null
        || authToken.isEmpty()
        || convChannelID == null
        || convChannelID.isEmpty()
        || dbPath == null
        || dbPath.isEmpty()
        || sessionTimeout <= 0
        || timeoutCheckInterval <= 0
        || threadsPerPage <= 0) return false;

    for (Msg m : Msg.values()) {
      if (!lang.containsKey(m.name()) || lang.get(m.name()).isEmpty())
        System.err.println(String.format("'%s' lang configuration is missing/invalid", m.name()));
    }

    return true;
  }

  public void initDefaults() {
    authToken = "";
    convChannelID = "";
    dbPath = "messages.db";
    sessionTimeout = 1800000;
    timeoutCheckInterval = 60000;
    threadsPerPage = 10;
    lang = new HashMap<>();

    for (Msg m : Msg.values()) lang.put(m.name(), m.getDefault());
  }

  public String getAuthToken() {
    return authToken;
  }

  public String getDbPath() {
    return dbPath;
  }

  public boolean hasMsg(String key) {
    return lang.containsKey(key);
  }

  public String getMsg(String key) {
    return lang.get(key);
  }

  public String getConvChannelID() {
    return convChannelID;
  }

  public long getSessionTimeout() {
    return sessionTimeout;
  }

  public long getTimeoutCheckInterval() {
    return timeoutCheckInterval;
  }

  public int getThreadsPerPage() {
    return threadsPerPage;
  }
}
