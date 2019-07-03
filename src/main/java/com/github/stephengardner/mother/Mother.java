package com.github.stephengardner.mother;

import com.github.stephengardner.mother.commands.*;
import com.github.stephengardner.mother.data.Database;
import com.github.stephengardner.mother.data.MotherConfig;
import com.github.stephengardner.mother.data.Msg;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Mother {

  private MotherConfig mc;
  private Database db;
  private SlackSession session;
  private AtomicBoolean online;
  private long lastUpdate;

  private HashMap<String, CommandExecutor> commands;
  private ConcurrentHashMap<String, Conversation> convos;
  private ConcurrentLinkedQueue<Conversation> expiredConvos;
  private ArrayList<String> joinedChannels;

  private Mother() {
    commands = new HashMap<>();
    convos = new ConcurrentHashMap<>();
    expiredConvos = new ConcurrentLinkedQueue<>();
    joinedChannels = new ArrayList<>();
    online = new AtomicBoolean(false);
    lastUpdate = 0;
  }

  public Mother(MotherConfig mc) {
    this();
    this.mc = mc;
    commands.put("active", new CmdActive(this));
    commands.put("close", new CmdClose(this));
    commands.put("contact", new CmdContact(this));
    commands.put("help", new CmdHelp(this));
    commands.put("history", new CmdHistory(this));
    commands.put("logs", new CmdLogs(this));
    commands.put("resume", new CmdResume(this));
    commands.put("shutdown", new CmdShutdown(this));

    try {
      db = new Database(this, mc.getDbPath());
      session = SlackSessionFactory.createWebSocketSlackSession(mc.getAuthToken());
      session.connect();
      online.compareAndSet(false, true);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    for (SlackChannel chan : session.getChannels()) {
      if (chan.isMember()) joinedChannels.add(chan.getId());
    }
  }

  public void startConversation(SlackUser user, String directChanID, boolean notifyUser) {
    String timestamp = sendToConvChannel(Msg.SESSION_NOTICE.get(this, user.getId())).getTimestamp();
    Conversation conv = new Conversation(this, user.getId(), directChanID, timestamp);

    addConversation(conv);

    if (notifyUser) conv.sendToUser(Msg.SESSION_START.get(this));
  }

  public void resumeConversation(Conversation conv) {
    conv.sendToThread(Msg.SESSION_RESUME_CONV.get(this));
    conv.sendToUser(Msg.SESSION_RESUME_DIRECT.get(this));
    addConversation(conv);
  }

  private void addConversation(Conversation conv) {
    Conversation prev = convos.put(conv.getDirectChannelID(), conv);

    if (prev == null) return;

    String link;

    link = Util.getThreadLink(this, mc.getConvChannelID(), conv.getThreadTimestamp());
    prev.sendToThread(Msg.SESSION_CONTEXT_SWITCHED_TO.get(this, link));
    link = Util.getThreadLink(this, mc.getConvChannelID(), prev.getThreadTimestamp());
    conv.sendToThread(Msg.SESSION_CONTEXT_SWITCHED_FROM.get(this, link));
    expiredConvos.add(prev);
  }

  public void reapConversations(final long sessionTimeout) throws SQLException {
    Conversation conv;
    Iterator<String> active = convos.keySet().iterator();

    while (active.hasNext()) {
      conv = convos.get(active.next());

      if (System.currentTimeMillis() - conv.getLastUpdate() < sessionTimeout) continue;

      active.remove();
      sendExpiredNotice(conv);
      db.saveMessages(conv);
    }

    Iterator<Conversation> expired = expiredConvos.iterator();

    while (expired.hasNext()) {
      conv = expired.next();
      expired.remove();
      db.saveMessages(conv);
    }
  }

  public void expireConversation(Conversation conv) {
    expiredConvos.add(convos.remove(conv.getDirectChannelID()));
    sendExpiredNotice(conv);
  }

  private void sendExpiredNotice(Conversation conv) {
    conv.sendToUser(Msg.SESSION_EXPIRED_DIRECT.get(this));
    conv.sendToThread(Msg.SESSION_EXPIRED_CONV.get(this, conv.getThreadTimestamp()));
  }

  public Conversation findConversation(String timestamp, boolean loadInactive) {
    for (Conversation conv : convos.values()) {
      if (conv.hasLog(timestamp)) return conv;
    }

    if (!loadInactive) return null;

    Conversation conv = findExpiredConversation(timestamp, false);

    if (conv != null) return conv;

    try {
      return db.loadConversation(timestamp);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  public Conversation findConversationByUserID(String userID) {
    for (Conversation conv : convos.values()) {
      if (conv.getUserID().equals(userID)) return conv;
    }

    return null;
  }

  public Conversation findExpiredConversation(String id, boolean isUser) {
    Iterator<Conversation> expired = expiredConvos.iterator();

    while (expired.hasNext()) {
      Conversation conv = expired.next();

      if ((isUser && !conv.getUserID().equals(id)) || !conv.hasLog(id)) continue;

      expired.remove();
      conv.update();
      resumeConversation(conv);
      return conv;
    }

    return null;
  }

  public boolean hasConversation(String directChanID) {
    return convos.containsKey(directChanID);
  }

  public Conversation getConversation(String directChanID) {
    return convos.get(directChanID);
  }

  public Collection<Conversation> getAllConversations() {
    return convos.values();
  }

  public void runCommands(SlackMessagePosted ev, String threadTimestamp) {
    String[] args = ev.getMessageContent().trim().split("\\s+");
    String cmdName = args[0].substring(1).toLowerCase();
    CommandExecutor cmd = commands.get(cmdName);

    if (cmd == null) {
      session.addReactionToMessage(ev.getChannel(), ev.getTimeStamp(), Msg.REACT_UNKNOWN.get(this));
      return;
    }

    String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);
    boolean success = cmd.onCommand(ev.getChannel(), ev.getUser(), cmdArgs, threadTimestamp);

    session.addReactionToMessage(
        ev.getChannel(),
        ev.getTimeStamp(),
        (success) ? Msg.REACT_SUCCESS.get(this) : Msg.REACT_FAILURE.get(this));
  }

  public SlackMessageReply sendToChannel(SlackChannel chan, String msg) {
    return session.sendMessage(chan, msg).getReply();
  }

  public SlackMessageReply sendToThread(SlackChannel chan, String msg, String threadTimestamp) {
    SlackPreparedMessage pm =
        new SlackPreparedMessage.Builder()
            .withThreadTimestamp(threadTimestamp)
            .withMessage(msg)
            .build();

    return session.sendMessage(chan, pm).getReply();
  }

  public SlackMessageReply sendToConvChannel(String msg) {
    return sendToChannel(getConvChannel(), msg);
  }

  public SlackMessageReply sendToConvThread(String msg, String threadTimestamp) {
    return sendToThread(getConvChannel(), msg, threadTimestamp);
  }

  public boolean inConvChannel(String userID) {
    for (SlackUser user : getConvChannel().getMembers()) {
      if (user.getId().equals(userID)) return true;
    }

    return false;
  }

  public SlackChannel getConvChannel() {
    return session.findChannelById(mc.getConvChannelID());
  }

  public SlackChannel getUserChannel(SlackUser user) {
    return session.openDirectMessageChannel(user).getReply().getSlackChannel();
  }

  public void update() {
    try {
      reapConversations(mc.getSessionTimeout());
      lastUpdate = System.currentTimeMillis();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public long getLastUpdate() {
    return lastUpdate;
  }

  public void shutdown() {
    online.compareAndSet(true, false);

    try {
      session.disconnect();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean isOnline() {
    return online.get();
  }

  public MotherConfig getConfig() {
    return mc;
  }

  public Database getDatabase() {
    return db;
  }

  public SlackSession getSession() {
    return session;
  }

  public HashMap<String, CommandExecutor> getCommands() {
    return commands;
  }

  public void addJoinedChannel(String chanID) {
    joinedChannels.add(chanID);
  }

  public boolean hasJoinedChannel(String chanID) {
    return joinedChannels.contains(chanID);
  }
}
