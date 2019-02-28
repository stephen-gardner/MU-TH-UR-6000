package com.github.stephengardner.mother.listeners;

import com.github.stephengardner.mother.Conversation;
import com.github.stephengardner.mother.Mother;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessageUpdated;
import com.ullink.slack.simpleslackapi.listeners.SlackMessageUpdatedListener;

public class UpdatedListener implements SlackMessageUpdatedListener {

  private Mother mom;

  public UpdatedListener(Mother mom) {
    this.mom = mom;
  }

  public void registerEvent() {
    mom.getSession().addMessageUpdatedListener(this);
  }

  @Override
  public void onEvent(SlackMessageUpdated ev, SlackSession s) {
    Conversation conv = mom.findConversation(ev.getMessageTimestamp(), false);

    if (conv == null) return;

    conv.updateMessage(
        ev.getMessageTimestamp(),
        ev.getNewMessage(),
        ev.getChannel().getId().equals(conv.getDirectChannelID()));
  }
}
