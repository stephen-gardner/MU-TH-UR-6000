package com.github.stephengardner.mother.commands;

import com.github.stephengardner.mother.Conversation;
import com.github.stephengardner.mother.Mother;
import com.github.stephengardner.mother.Util;
import com.github.stephengardner.mother.data.Msg;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;

public class CmdActive implements CommandExecutor {

  private Mother mom;

  public CmdActive(Mother mom) {
    this.mom = mom;
  }

  @Override
  public boolean onCommand(
      SlackChannel chan, SlackUser user, String[] args, String threadTimestamp) {
    if (args.length != 0) return false;

    StringBuilder sb = new StringBuilder();
    boolean empty = true;

    sb.append(Msg.ACTIVE_CONVS.toString());

    for (Conversation conv : mom.getAllConversations()) {
      if (empty) empty = false;

      String link =
          Util.getThreadLink(mom.getSession(), mom.getConvChannelID(), conv.getThreadTimestamp());

      sb.append(String.format(Msg.ACTIVE_INFO.toString(), link, conv.getUserID()));
    }

    if (empty) sb.append(Msg.LIST_NONE.toString());

    mom.sendToChannel(chan, sb.toString(), threadTimestamp);
    return true;
  }
}
