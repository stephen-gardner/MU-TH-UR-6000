package com.github.stephengardner.mother;

import com.github.stephengardner.mother.commands.*;
import com.github.stephengardner.mother.data.Database;
import com.github.stephengardner.mother.data.Msg;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Mother {

    private Database db;
    private SlackSession session;
    private SlackChannel channel;
    private ArrayList<String> joinedChannels;
    private HashMap<String, CommandExecutor> commands;
    private ConcurrentHashMap<String, Conversation> convos;
    private AtomicBoolean online;

    private Mother() {
        joinedChannels = new ArrayList<>();
        commands = new HashMap<>();
        commands.put("active", new CmdActive(this));
        commands.put("close", new CmdClose(this));
        commands.put("contact", new CmdContact(this));
        commands.put("help", new CmdHelp(this));
        commands.put("history", new CmdHistory(this));
        commands.put("logs", new CmdLogs(this));
        commands.put("reload", new CmdReload(this));
        commands.put("resume", new CmdResume(this));
        commands.put("shutdown", new CmdShutdown(this));
        convos = new ConcurrentHashMap<>();
        online = new AtomicBoolean(true);
    }

    public Mother(String authToken, String privateChanID, String dbPath) {
        this();

        try {
            db = new Database(this, dbPath);
            session = SlackSessionFactory.createWebSocketSlackSession(authToken);
            session.connect();
            channel = session.findChannelById(privateChanID);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.err.println(Msg.NO_CONNECTION.toString());
            System.exit(1);
        }

        for (SlackChannel chan : session.getChannels()) {
            if (chan.isMember()) joinedChannels.add(chan.getId());
        }
    }

    public HashMap<String, CommandExecutor> getCommands() {
        return commands;
    }

    public Database getDatabase() {
        return db;
    }

    public void addJoinedChannel(String chanID) {
        joinedChannels.add(chanID);
    }

    public boolean hasJoinedChannel(String chanID) {
        return joinedChannels.contains(chanID);
    }

    public boolean isOnline() {
        return online.get();
    }

    public SlackChannel getChannel() {
        return channel;
    }

    public SlackSession getSession() {
        return session;
    }

    public Conversation addConversation(String directChanID, Conversation conv) {
        Conversation prev = convos.put(directChanID, conv);

        if (prev != null) {
            prev.sendToThread(
                    String.format(
                            Msg.SESSION_CONTEXT_SWITCHED_TO.toString(),
                            Util.getThreadLink(
                                    getSession(), conv.getChannel().getId(), conv.getThreadTimestamp())));
            conv.sendToThread(
                    String.format(
                            Msg.SESSION_CONTEXT_SWITCHED_FROM.toString(),
                            Util.getThreadLink(
                                    getSession(), prev.getChannel().getId(), prev.getThreadTimestamp())));

            try {
                db.saveMessages(prev);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return prev;
    }

    public Collection<Conversation> getAllConversations() {
        return convos.values();
    }

    public Conversation getConversation(String directChanID) {
        return convos.getOrDefault(directChanID, null);
    }

    public boolean hasConversation(String directChanID) {
        return convos.containsKey(directChanID);
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

    public void reapConversations(final long sessionTimeout) throws SQLException {
        Iterator<String> it = convos.keySet().iterator();

        while (it.hasNext()) {
            Conversation conv = convos.get(it.next());

            if (System.currentTimeMillis() - conv.getLastUpdate() < sessionTimeout) continue;

            it.remove();
            conv.sendToUser(Msg.SESSION_EXPIRED_DIRECT.toString());
            conv.sendToThread(
                    String.format(Msg.SESSION_EXPIRED_CHAN.toString(), conv.getThreadTimestamp()));
            db.saveMessages(conv);
        }
    }

    public void startConversation(SlackUser user, String directChanID, boolean notifyUser) {
        String notice = String.format(Msg.SESSION_NOTICE.toString(), user.getUserName());
        String threadTimestamp = sendToChannel(notice).getTimestamp();
        Conversation conv = new Conversation(this, user.getId(), directChanID, threadTimestamp);

        if (notifyUser) conv.sendToUser(Msg.SESSION_START.toString());

        addConversation(directChanID, conv);
    }

    public SlackMessageReply sendToChannel(String msg) {
        return session.sendMessage(channel, msg).getReply();
    }

    public SlackMessageReply sendToChannel(String msg, String threadTimestamp) {
        SlackPreparedMessage pm =
                new SlackPreparedMessage.Builder()
                        .withThreadTimestamp(threadTimestamp)
                        .withMessage(msg)
                        .build();

        return session.sendMessage(channel, pm).getReply();
    }

    public void shutdown() {
        try {
            session.disconnect();
            reapConversations(0L);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        online.compareAndSet(true, false);
    }
}
