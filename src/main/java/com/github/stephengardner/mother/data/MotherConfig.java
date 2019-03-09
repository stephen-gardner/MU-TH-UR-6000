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

  public String getAuthToken() {
    return authToken;
  }

  public String getDbPath() {
    return dbPath;
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

  public void initDefaults() {
    authToken = "";
    convChannelID = "";
    dbPath = "messages.db";
    sessionTimeout = 1800000;
    timeoutCheckInterval = 60000;
    threadsPerPage = 10;
    lang = new HashMap<>();
    lang.put("ACTIVE_CONVS", "*Active conversations:*\n");
    lang.put("ACTIVE_INFO", ">%s (*<@%s>*)\n");
    lang.put("CONFIG_ERROR", "Error: %s is missing or invalid");
    lang.put("HELP_ACTIVE", ">`active` - List active conversations\n");
    lang.put("HELP_CLOSE", ">`close @user/thread_id` - End active conversation\n");
    lang.put("HELP_CONTACT", ">`contact @user` - Start conversation with user\n");
    lang.put(
        "HELP_HISTORY", ">`history [@user] [page]` - List thread IDs of recent conversations\n");
    lang.put(
        "HELP_LOGS", ">`logs @user/thread_id` - Upload logs associated with user or thread ID\n");
    lang.put("HELP_RESUME", ">`resume @user/thread_id` - Resume conversation under a new thread\n");
    lang.put("HELP_SHUTDOWN", ">`shutdown` - Disconnect bot\n");
    lang.put(
        "IN_CONV_CHANNEL",
        ">_*Users in `#%s` can not start conversations. Send `!help` for a list of available commands.*_");
    lang.put("LIST_COMMANDS", "*Commands:*\n");
    lang.put("LIST_NONE", ">_(None)_\n");
    lang.put("LIST_THREADS_USER", "*<@%s>'s recently expired threads _(page %d):_*\n");
    lang.put("LIST_THREADS", "*Recently expired threads _(page %d):_*\n");
    lang.put("LIST_THREADS_ELE", ">%s (*<@%s>*) _%s_\n");
    lang.put("LIST_THREADS_ELE_USER", ">%s _%s_\n");
    lang.put("LOG", "%s %s: %s\n");
    lang.put("LOG_EDITED", "%s %s: %s (edited)\n");
    lang.put("LOG_TIMESTAMP_FMT", "[yyyy-MM-dd hh:mm:ss a]");
    lang.put("LOG_TIMESTAMP_ZONE", "PST");
    lang.put("MESSAGE_COPY_FMT", "*<@%s>:* %s");
    lang.put("MESSAGE_LINK", "<https://%s.slack.com/archives/%s/p%s|%s>");
    lang.put("NO_GROUPS", ">_*This bot does not support group communications.*_");
    lang.put("REACT_FAILURE", "x");
    lang.put("REACT_SUCCESS", "white_check_mark");
    lang.put("REACT_UNKNOWN", "question");
    lang.put("SESSION_CONTEXT_SWITCHED_FROM", ">_*Session context switched from [%s].*_");
    lang.put("SESSION_CONTEXT_SWITCHED_TO", ">_*Session context switched to [%s].*_");
    lang.put(
        "SESSION_EXPIRED_CONV",
        ">_*Session [%s] has expired.*_\n>Edits/reactions to previous messages will no longer be reflected in communications.");
    lang.put(
        "SESSION_EXPIRED_DIRECT",
        ">_*Session has expired.*_\n>If your issue has not yet been resolved, an RA will be contacting you ASAP.\n>Edits/reactions to previous messages will no longer be reflected in communications.");
    lang.put(
        "SESSION_NOTICE",
        "_*Conversation started with <@%s>* (converse in thread under this message)_");
    lang.put("SESSION_RESUME_CONV", ">_*Session resumed.*_");
    lang.put("SESSION_RESUME_DIRECT", ">_*An RA has resumed your session.*_");
    lang.put(
        "SESSION_START",
        ">_*A dialogue has been started with the RA team. An RA will reach out to you shortly.*_");
  }
}
