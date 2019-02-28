package com.github.stephengardner.mother.commands;

import com.github.stephengardner.mother.Conversation;
import com.github.stephengardner.mother.Mother;
import com.github.stephengardner.mother.Util;
import com.github.stephengardner.mother.data.Msg;
import com.ullink.slack.simpleslackapi.SlackUser;

public class CmdActive implements CommandExecutor {

  private Mother mom;

  public CmdActive(Mother mom) {
    this.mom = mom;
  }

  @Override
  public boolean onCommand(SlackUser user, String[] args, String threadTimestamp) {
    if (args.length != 0) return false;

    StringBuilder sb = new StringBuilder();
    boolean first = true;

    for (Conversation conv : mom.getAllConversations()) {
      if (first) first = false;
      else sb.append(", ");

      String link =
          Util.getThreadLink(mom.getSession(), mom.getConvChannelID(), conv.getThreadTimestamp());

      sb.append(String.format(Msg.ACTIVE_INFO.toString(), link, conv.getUserID()));
    }

    if (first) sb.append(Msg.ACTIVE_CONVS_NONE.toString());

    mom.sendToChannel(String.format(Msg.ACTIVE_CONVS.toString(), sb.toString()), threadTimestamp);
    return true;
  }
}
