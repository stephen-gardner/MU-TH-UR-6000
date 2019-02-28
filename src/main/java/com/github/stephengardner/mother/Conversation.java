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
  private HashMap<String, String> chanIndex;
  private HashMap<String, String> directIndex;
  private ArrayList<LogEntry> editedLogs;
  private long lastUpdate;

  private Conversation() {
    logs = new HashMap<>();
    chanIndex = new HashMap<>();
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
        || chanIndex.containsKey(timestamp);
  }

  public ArrayList<LogEntry> getLogs() {
    ArrayList<LogEntry> allLogs = new ArrayList<>();

    allLogs.addAll(logs.values());
    allLogs.addAll(editedLogs);
    return allLogs;
  }

  public void addLog(String directTimestamp, String chanTimestamp, LogEntry log) {
    LogEntry prev = logs.put(directTimestamp, log);

    if (prev != null) editedLogs.add(prev);

    directIndex.put(directTimestamp, chanTimestamp);
    chanIndex.put(chanTimestamp, directTimestamp);
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
    return mom.sendToChannel(msg, threadTimestamp);
  }

  public SlackMessageReply sendToUser(String msg) {
    return mom.getSession().sendMessage(getDirectChannel(), msg).getReply();
  }

  public void setReaction(String timestamp, String emojiCode, boolean isDirect, boolean removed) {
    SlackChannel chan;
    BidiMessage bm;

    if ((bm = getBidiMessage(timestamp, isDirect)) == null) return;

    if (isDirect) {
      chan = mom.getConvChannel();
      timestamp = bm.getChanTimestamp();
    } else {
      chan = getDirectChannel();
      timestamp = bm.getDirectTimestamp();
    }

    if (removed) {
      mom.getSession().removeReactionFromMessage(chan, timestamp, emojiCode);
      return;
    }

    mom.getSession().addReactionToMessage(chan, timestamp, emojiCode);
    update();
  }

  public void updateMessage(String timestamp, String content, boolean isDirect) {
    SlackChannel chan;
    String userID;
    BidiMessage bm;

    if ((bm = getBidiMessage(timestamp, isDirect)) == null) return;

    if (isDirect) {
      chan = mom.getConvChannel();
      userID = getUserID();
      timestamp = bm.getChanTimestamp();
    } else {
      chan = getDirectChannel();
      userID = logs.get(bm.getDirectTimestamp()).getUserID();
      timestamp = bm.getDirectTimestamp();
    }

    String tagged = String.format(Msg.MESSAGE_COPY_FMT.toString(), userID, content);

    if (mom.getSession().updateMessage(timestamp, chan, tagged).getReply().getTimestamp() == null)
      return;

    addLog(
        bm.getDirectTimestamp(),
        bm.getChanTimestamp(),
        new LogEntry(userID, content, bm.getChanTimestamp(), false));
  }

  private BidiMessage getBidiMessage(String timestamp, boolean isDirect) {
    if (isDirect) {
      if (!directIndex.containsKey(timestamp)) return null;

      return new BidiMessage(timestamp, directIndex.get(timestamp));
    }

    if (!chanIndex.containsKey(timestamp)) return null;

    return new BidiMessage(chanIndex.get(timestamp), timestamp);
  }

  public void expire() {
    lastUpdate = System.currentTimeMillis() - Main.getConfig().getSessionTimeout();
  }

  private void update() {
    lastUpdate = System.currentTimeMillis();
  }
}
