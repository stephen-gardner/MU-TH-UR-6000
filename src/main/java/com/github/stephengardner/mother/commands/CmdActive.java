package com.github.stephengardner.mother.commands;

import com.github.stephengardner.mother.Conversation;
import com.github.stephengardner.mother.Mother;
import com.github.stephengardner.mother.Util;
import com.github.stephengardner.mother.data.Msg;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;

import java.util.Collection;

public class CmdActive implements CommandExecutor {

  private Mother mom;

  public CmdActive(Mother mom) {
    this.mom = mom;
  }

  @Override
  public boolean onCommand(
      SlackChannel chan, SlackUser user, String[] args, String threadTimestamp) {
    if (args.length != 0) return false;

    Collection<Conversation> convos = mom.getAllConversations();
    StringBuilder sb = new StringBuilder();

    sb.append(Msg.ACTIVE_CONVS.get(mom));

    for (Conversation conv : convos) {
      String link =
          Util.getThreadLink(mom, mom.getConfig().getConvChannelID(), conv.getThreadTimestamp());

      sb.append(Msg.ACTIVE_INFO.get(mom, link, conv.getUserID()));
    }

    if (convos.isEmpty()) sb.append(Msg.LIST_NONE.get(mom));

    mom.sendToThread(chan, sb.toString(), threadTimestamp);
    return true;
  }
}
