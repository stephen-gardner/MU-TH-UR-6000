package com.github.stephengardner.mother;

import com.github.stephengardner.mother.data.LogEntry;
import com.github.stephengardner.mother.data.Msg;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply;

import java.util.ArrayList;
import java.util.HashMap;

public class Conversation {

  private Mother mom;
  private String userID;
  private String directChanID;
  private String threadTimestamp;
  private HashMap<String, LogEntry> logs;
  private HashMap<String, String> convIndex;
  private HashMap<String, String> directIndex;
  private ArrayList<LogEntry> editedLogs;
  private long lastUpdate;

  private Conversation() {
    logs = new HashMap<>();
    convIndex = new HashMap<>();
    directIndex = new HashMap<>();
    editedLogs = new ArrayList<>();
  }

  public Conversation(Mother mom, String userID, String directChanID, String threadTimestamp) {
    this();
    this.mom = mom;
    this.userID = userID;
    this.directChanID = directChanID;
    this.threadTimestamp = threadTimestamp;
    update();
  }

  public boolean hasLog(String timestamp) {
    return timestamp.equals(threadTimestamp)
        || directIndex.containsKey(timestamp)
        || convIndex.containsKey(timestamp);
  }

  public ArrayList<LogEntry> getLogs() {
    ArrayList<LogEntry> allLogs = new ArrayList<>();

    allLogs.addAll(logs.values());
    allLogs.addAll(editedLogs);
    return allLogs;
  }

  public void addLog(String directTimestamp, String convTimestamp, LogEntry log) {
    LogEntry prev = logs.put(directTimestamp, log);

    if (prev != null) editedLogs.add(prev);

    directIndex.put(directTimestamp, convTimestamp);
    convIndex.put(convTimestamp, directTimestamp);
    update();
  }

  public SlackUser getUser() {
    return mom.getSession().findUserById(userID);
  }

  public String getUserID() {
    return userID;
  }

  public SlackChannel getDirectChannel() {
    return mom.getSession().findChannelById(directChanID);
  }

  public String getDirectChannelID() {
    return directChanID;
  }

  public String getThreadTimestamp() {
    return threadTimestamp;
  }

  public long getLastUpdate() {
    return lastUpdate;
  }

  public SlackMessageReply sendToThread(String msg) {
    return mom.sendToConvChannel(msg, threadTimestamp);
  }

  public SlackMessageReply sendToUser(String msg) {
    return mom.getSession().sendMessage(getDirectChannel(), msg).getReply();
  }

  public void setReaction(String timestamp, String emojiCode, boolean isDirect, boolean removed) {
    SlackChannel chan;

    if (isDirect) {
      if (!directIndex.containsKey(timestamp)) return;

      timestamp = directIndex.get(timestamp);
      chan = mom.getConvChannel();
    } else {
      if (!convIndex.containsKey(timestamp)) return;

      timestamp = convIndex.get(timestamp);
      chan = getDirectChannel();
    }

    if (removed) {
      mom.getSession().removeReactionFromMessage(chan, timestamp, emojiCode);
      return;
    }

    mom.getSession().addReactionToMessage(chan, timestamp, emojiCode);
    update();
  }

  public void updateMessage(String timestamp, String content, boolean isDirect) {
    String userID;
    String directTimestamp;
    String convTimestamp;
    SlackChannel chan;

    if (isDirect) {
      if (!directIndex.containsKey(timestamp)) return;

      convTimestamp = directIndex.get(timestamp);
      directTimestamp = timestamp;
      timestamp = convTimestamp;
      userID = getUserID();
      chan = mom.getConvChannel();
    } else {
      if (!convIndex.containsKey(timestamp)) return;

      convTimestamp = timestamp;
      directTimestamp = convIndex.get(timestamp);
      timestamp = directTimestamp;
      userID = logs.get(directTimestamp).getUserID();
      chan = getDirectChannel();
    }

    String tagged = String.format(Msg.MESSAGE_COPY_FMT.get(mom), userID, content);

    if (mom.getSession().updateMessage(timestamp, chan, tagged).getReply().getTimestamp() == null)
      return;

    addLog(directTimestamp, convTimestamp, new LogEntry(userID, content, convTimestamp, false));
  }

  private void update() {
    lastUpdate = System.currentTimeMillis();
  }
}
