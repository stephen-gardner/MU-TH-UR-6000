package com.github.stephengardner.mother.listeners;

import com.github.stephengardner.mother.Mother;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackChannelJoined;
import com.ullink.slack.simpleslackapi.listeners.SlackChannelJoinedListener;

public class JoinedChannelListener implements SlackChannelJoinedListener {

  private Mother mom;

  public JoinedChannelListener(Mother mom) {
    this.mom = mom;
  }

  public void registerEvent() {
    mom.getSession().addChannelJoinedListener(this);
  }

  @Override
  public void onEvent(SlackChannelJoined ev, SlackSession s) {
    s.leaveChannel(ev.getSlackChannel());
  }
}
