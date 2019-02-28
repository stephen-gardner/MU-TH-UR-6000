package com.github.stephengardner.mother.listeners;

import com.github.stephengardner.mother.Mother;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.UserTyping;
import com.ullink.slack.simpleslackapi.listeners.UserTypingListener;

public class DirectTypingListener implements UserTypingListener {

  private Mother mom;

  public DirectTypingListener(Mother mom) {
    this.mom = mom;
  }

  public void registerEvent() {
    mom.getSession().addUserTypingListener(this);
  }

  @Override
  public void onEvent(UserTyping ev, SlackSession s) {
    if (!ev.getChannel().isDirect()) return;

    s.sendTyping(mom.getConvChannel());
  }
}
