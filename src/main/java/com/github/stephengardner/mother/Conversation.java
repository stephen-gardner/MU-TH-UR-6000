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
    private String dmChanID;
    private String threadTimestamp;
    private long lastUpdate;
    private HashMap<String, LogEntry> logs;
    private HashMap<String, String> chanIndex;
    private HashMap<String, String> directIndex;
    private ArrayList<LogEntry> archivedLogs;

    private Conversation() {
        logs = new HashMap<>();
        chanIndex = new HashMap<>();
        directIndex = new HashMap<>();
        archivedLogs = new ArrayList<>();
    }

    public Conversation(Mother mom, String userID, String dmChanID, String threadTimestamp) {
        this();
        this.mom = mom;
        this.userID = userID;
        this.dmChanID = dmChanID;
        this.threadTimestamp = threadTimestamp;
        lastUpdate = System.currentTimeMillis();
    }

    public void addLog(String directTimestamp, String chanTimestamp, LogEntry log) {
        LogEntry prev = logs.put(directTimestamp, log);

        if (prev != null) {
            archivedLogs.add(prev);
        }

        directIndex.put(directTimestamp, chanTimestamp);
        chanIndex.put(chanTimestamp, directTimestamp);
        lastUpdate = System.currentTimeMillis();
    }

    public ArrayList<LogEntry> getAllLogs() {
        ArrayList<LogEntry> allLogs = new ArrayList<>();

        allLogs.addAll(logs.values());
        allLogs.addAll(archivedLogs);
        return allLogs;
    }

    public boolean hasLog(String timestamp) {
        return timestamp.equals(threadTimestamp)
                || directIndex.containsKey(timestamp)
                || chanIndex.containsKey(timestamp);
    }

    public SlackChannel getChannel() {
        return mom.getChannel();
    }

    public SlackChannel getDirectChannel() {
        return mom.getSession().findChannelById(dmChanID);
    }

    public String getDirectChannelID() {
        return dmChanID;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public String getThreadTimestamp() {
        return threadTimestamp;
    }

    public SlackUser getUser() {
        return mom.getSession().findUserById(userID);
    }

    public String getUserID() {
        return userID;
    }

    public void expire() {
        lastUpdate = System.currentTimeMillis() - Main.getConfig().getSessionTimeout();
    }

    public SlackMessageReply sendToThread(String msg) {
        return mom.sendToChannel(msg, threadTimestamp);
    }

    public SlackMessageReply sendToUser(String msg) {
        return mom.getSession().sendMessage(getDirectChannel(), msg).getReply();
    }

    public void setReaction(String timestamp, String emojiCode, boolean isDirect, boolean removed) {
        SlackChannel chan;

        BidiMessage bm = getBidiMessage(timestamp, isDirect);

        if (bm == null) return;

        if (isDirect) {
            chan = getChannel();
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
        lastUpdate = System.currentTimeMillis();
    }

    public void updateMessage(String timestamp, String content, boolean isDirect) {
        String userID;
        String taggedContent;

        BidiMessage bm = getBidiMessage(timestamp, isDirect);

        if (isDirect) {
            userID = getUserID();
            taggedContent = String.format(Msg.MESSAGE_COPY_FMT.toString(), userID, content);

            if (mom.getSession()
                    .updateMessage(bm.getChanTimestamp(), getChannel(), taggedContent)
                    .getReply()
                    .getTimestamp()
                    == null) return;
        } else {
            userID = logs.get(bm.getDirectTimestamp()).getUserID();
            taggedContent = String.format(Msg.MESSAGE_COPY_FMT.toString(), userID, content);

            if (mom.getSession()
                    .updateMessage(bm.getDirectTimestamp(), getDirectChannel(), taggedContent)
                    .getReply()
                    .getTimestamp()
                    == null) return;
        }

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
}
