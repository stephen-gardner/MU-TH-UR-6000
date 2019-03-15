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
import java.util.concurrent.atomic.AtomicBoolean;

public class Mother {

  private MotherConfig mc;
  private Database db;
  private SlackSession session;
  private ArrayList<String> joinedChannels;
  private HashMap<String, CommandExecutor> commands;
  private ConcurrentHashMap<String, Conversation> convos;
  private AtomicBoolean online;
  private long lastUpdate;

  private Mother() {
    joinedChannels = new ArrayList<>();
    commands = new HashMap<>();
    convos = new ConcurrentHashMap<>();
    online = new AtomicBoolean(true);
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
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    for (SlackChannel chan : session.getChannels()) {
      if (chan.isMember()) joinedChannels.add(chan.getId());
    }
  }

  public Conversation addConversation(String directChanID, Conversation conv) {
    Conversation prev = convos.put(directChanID, conv);

    if (prev != null) {
      prev.sendToThread(
          String.format(
              Msg.SESSION_CONTEXT_SWITCHED_TO.get(this),
              Util.getThreadLink(this, mc.getConvChannelID(), conv.getThreadTimestamp())));
      conv.sendToThread(
          String.format(
              Msg.SESSION_CONTEXT_SWITCHED_FROM.get(this),
              Util.getThreadLink(this, mc.getConvChannelID(), prev.getThreadTimestamp())));

      try {
        db.saveMessages(prev);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    return prev;
  }

  public Conversation findConversation(String timestamp, boolean forceLoad) {
    for (Conversation conv : convos.values()) {
      if (conv.hasLog(timestamp)) return conv;
    }

    if (forceLoad) {
      try {
        return db.loadConversation(timestamp);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    return null;
  }

  public Conversation findConversationByUserID(String userID) {
    for (Conversation conv : convos.values()) {
      if (conv.getUserID().equals(userID)) return conv;
    }

    return null;
  }

  public boolean hasConversation(String directChanID) {
    return convos.containsKey(directChanID);
  }

  public Conversation getConversation(String directChanID) {
    return convos.getOrDefault(directChanID, null);
  }

  public Collection<Conversation> getAllConversations() {
    return convos.values();
  }

  public void reapConversations(final long sessionTimeout) throws SQLException {
    Iterator<String> it = convos.keySet().iterator();

    while (it.hasNext()) {
      Conversation conv = convos.get(it.next());

      if (System.currentTimeMillis() - conv.getLastUpdate() < sessionTimeout) continue;

      it.remove();
      conv.sendToUser(Msg.SESSION_EXPIRED_DIRECT.get(this));
      conv.sendToThread(
          String.format(Msg.SESSION_EXPIRED_CONV.get(this), conv.getThreadTimestamp()));
      db.saveMessages(conv);
    }
  }

  public void startConversation(SlackUser user, String directChanID, boolean notifyUser) {
    String notice = String.format(Msg.SESSION_NOTICE.get(this), user.getId());
    String threadTimestamp = sendToConvChannel(notice).getTimestamp();
    Conversation conv = new Conversation(this, user.getId(), directChanID, threadTimestamp);

    if (notifyUser) conv.sendToUser(Msg.SESSION_START.get(this));

    addConversation(directChanID, conv);
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

  public SlackMessageReply sendToConvChannel(String msg) {
    return sendToChannel(getConvChannel(), msg);
  }

  public SlackMessageReply sendToChannel(SlackChannel chan, String msg, String threadTimestamp) {
    SlackPreparedMessage pm =
        new SlackPreparedMessage.Builder()
            .withThreadTimestamp(threadTimestamp)
            .withMessage(msg)
            .build();

    return session.sendMessage(chan, pm).getReply();
  }

  public SlackMessageReply sendToConvChannel(String msg, String threadTimestamp) {
    return sendToChannel(getConvChannel(), msg, threadTimestamp);
  }

  public SlackChannel getConvChannel() {
    return session.findChannelById(mc.getConvChannelID());
  }

  public boolean inConvChannel(String userID) {
    for (SlackUser user : getConvChannel().getMembers()) {
      if (user.getId().equals(userID)) return true;
    }

    return false;
  }

  public SlackChannel getUserChannel(SlackUser user) {
    return session.openDirectMessageChannel(user).getReply().getSlackChannel();
  }

  public long getLastUpdate() {
    return lastUpdate;
  }

  public void update() {
    SlackChannel chan = getUserChannel(session.findUserById("USLACKBOT"));

    try {
      session.sendTyping(chan); // Fool Slack into thinking bot is always active
      reapConversations(mc.getSessionTimeout());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public boolean isOnline() {
    return online.get();
  }

  public void shutdown() {
    online.compareAndSet(true, false);

    try {
      session.disconnect();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean hasJoinedChannel(String chanID) {
    return joinedChannels.contains(chanID);
  }

  public void addJoinedChannel(String chanID) {
    joinedChannels.add(chanID);
  }

  public Database getDatabase() {
    return db;
  }

  public HashMap<String, CommandExecutor> getCommands() {
    return commands;
  }

  public MotherConfig getConfig() {
    return mc;
  }

  public SlackSession getSession() {
    return session;
  }
}
