package com.github.stephengardner.mother.data;

import java.util.HashMap;

public class MotherConfig {

    private String authToken;
    private String privateChanID;
    private String dbPath;
    private long sessionTimeout;
    private long timeoutCheckInterval;
    private int threadsPerPage;
    private HashMap<String, String> lang;

    public String getAuthToken() {
        return authToken;
    }

    public String getDbPath() {
        return dbPath;
    }

    public String getMsg(String key) {
        return lang.get(key);
    }

    public String getPrivateChanID() {
        return privateChanID;
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
