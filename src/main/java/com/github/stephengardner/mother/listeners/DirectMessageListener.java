package com.github.stephengardner.mother.listeners;

import com.github.stephengardner.mother.Conversation;
import com.github.stephengardner.mother.Mother;
import com.github.stephengardner.mother.data.LogEntry;
import com.github.stephengardner.mother.data.Msg;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

public class DirectMessageListener implements SlackMessagePostedListener {

    private Mother mom;

    public DirectMessageListener(Mother mom) {
        this.mom = mom;
    }

    public void registerEvent() {
        mom.getSession().addMessagePostedListener(this);
    }

    @Override
    public void onEvent(SlackMessagePosted ev, SlackSession s) {
        SlackUser user = ev.getUser();
        SlackChannel chan = ev.getChannel();

        if (s.sessionPersona().getId().equals(user.getId()) || user.getId().equals("USLACKBOT")) return;

        if (!chan.isDirect()) {
            if (!mom.hasJoinedChannel(chan.getId())) {
                s.sendMessage(chan, Msg.NO_GROUPS.toString());
                mom.addJoinedChannel(chan.getId());
            }

            return;
        }

        if (!mom.hasConversation(chan.getId())) mom.startConversation(user, chan.getId(), true);

        Conversation conv = mom.getConversation(chan.getId());
        String content =
                String.format(Msg.MESSAGE_COPY_FMT.toString(), user.getId(), ev.getMessageContent());
        String chanTimestamp = conv.sendToThread(content).getTimestamp();
        LogEntry log = new LogEntry(user.getId(), ev.getMessageContent(), chanTimestamp, true);

        conv.addLog(ev.getTimestamp(), chanTimestamp, log);
    }
}
