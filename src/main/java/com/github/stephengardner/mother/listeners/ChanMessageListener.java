package com.github.stephengardner.mother.listeners;

import com.github.stephengardner.mother.Conversation;
import com.github.stephengardner.mother.Mother;
import com.github.stephengardner.mother.data.LogEntry;
import com.github.stephengardner.mother.data.Msg;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

public class ChanMessageListener implements SlackMessagePostedListener {

  private Mother mom;

  public ChanMessageListener(Mother mom) {
    this.mom = mom;
  }

  public void registerEvent() {
    mom.getSession().addMessagePostedListener(this);
  }

  @Override
  public void onEvent(SlackMessagePosted ev, SlackSession s) {
    String userID = ev.getUser().getId();
    String threadTimestamp = ev.getThreadTimestamp();

    if (!ev.getChannel().getId().equals(mom.getConfig().getConvChannelID())
        || s.sessionPersona().getId().equals(userID)
        || userID.equals("USLACKBOT")) return;

    if (threadTimestamp != null && converse(ev, userID, threadTimestamp)) return;

    if (ev.getMessageContent().startsWith("!"))
      mom.runCommands(ev, (threadTimestamp != null) ? threadTimestamp : ev.getTimestamp());
  }

  private boolean converse(SlackMessagePosted ev, String userID, String threadTimestamp) {
    Conversation conv = mom.findConversation(threadTimestamp, true);

    if (conv == null) return false;

    String content = String.format(Msg.MESSAGE_COPY_FMT.get(mom), userID, ev.getMessageContent());
    String directTimestamp = conv.sendToUser(content).getTimestamp();
    LogEntry log = new LogEntry(userID, ev.getMessageContent(), ev.getTimestamp(), true);

    conv.addLog(directTimestamp, ev.getTimestamp(), log);
    return true;
  }
}
