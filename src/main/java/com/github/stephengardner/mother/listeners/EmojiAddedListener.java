package com.github.stephengardner.mother.listeners;

import com.github.stephengardner.mother.Conversation;
import com.github.stephengardner.mother.Mother;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.ReactionAdded;
import com.ullink.slack.simpleslackapi.listeners.ReactionAddedListener;

public class EmojiAddedListener implements ReactionAddedListener {

  private Mother mom;

  public EmojiAddedListener(Mother mom) {
    this.mom = mom;
  }

  public void registerEvent() {
    mom.getSession().addReactionAddedListener(this);
  }

  @Override
  @SuppressWarnings("Duplicates")
  public void onEvent(ReactionAdded ev, SlackSession s) {
    if (s.sessionPersona().getId().equals(ev.getUser().getId())) return;

    Conversation conv = mom.findConversation(ev.getMessageID(), false);

    if (conv == null) return;

    conv.setReaction(
        ev.getMessageID(),
        ev.getEmojiName(),
        ev.getChannel().getId().equals(conv.getDirectChannelID()),
        false);
  }
}
